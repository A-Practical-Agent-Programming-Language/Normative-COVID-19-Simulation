package main.java.nl.uu.iss.ga.pansim.visit;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.disease.DiseaseState;
import main.java.nl.uu.iss.ga.pansim.state.AgentStateMap;
import main.java.nl.uu.iss.ga.util.Constants;
import nl.uu.cs.iss.ga.sim2apl.core.deliberation.DeliberationResult;
import nl.uu.cs.iss.ga.sim2apl.core.tick.TickExecutor;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.vector.*;
import org.apache.arrow.vector.ipc.ArrowFileWriter;
import org.apache.arrow.vector.types.pojo.Field;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class VisitDataFrame {
    private BigIntVector lid;
    private BigIntVector pid;
    private TinyIntVector group;
    private TinyIntVector state;
    private TinyIntVector behavior;
    private IntVector start_time;
    private IntVector end_time;
    private HashMap<String, TinyIntVector> attrs;

    private VectorSchemaRoot schemaRoot;

    public VisitDataFrame(int max_rows, BufferAllocator allocator) {
        this.lid = new BigIntVector("lid", allocator);
        this.pid = new BigIntVector("pid", allocator);
        this.group = new TinyIntVector("group", allocator);
        this.state = new TinyIntVector("state", allocator);
        this.behavior = new TinyIntVector("behavior", allocator);
        this.start_time = new IntVector("start_time", allocator);
        this.end_time = new IntVector("end_time", allocator);
        this.attrs = new HashMap<>();

        for (int i = 0; i < Constants.VISIBLE_ATTRIBUTES.length; i++) {
            this.attrs.put(Constants.VISIBLE_ATTRIBUTES[i], new TinyIntVector(Constants.VISIBLE_ATTRIBUTES[i], allocator));
        }

        this.lid.allocateNew(max_rows);
        this.pid.allocateNew(max_rows);
        this.group.allocateNew(max_rows);
        this.state.allocateNew(max_rows);
        this.behavior.allocateNew(max_rows);
        this.start_time.allocateNew(max_rows);
        this.end_time.allocateNew(max_rows);

        for (String name: this.attrs.keySet()) {
            this.attrs.get(name).allocateNew(max_rows);
        }

        ArrayList<Field> fields = new ArrayList<>();
        fields.add(this.lid.getField());
        fields.add(this.pid.getField());
        fields.add(this.group.getField());
        fields.add(this.state.getField());
        fields.add(this.behavior.getField());
        fields.add(this.start_time.getField());
        fields.add(this.end_time.getField());
        for (String name: this.attrs.keySet()) {
            fields.add(this.attrs.get(name).getField());
        }

        ArrayList<FieldVector> vectors = new ArrayList<>();
        vectors.add(this.lid);
        vectors.add(this.pid);
        vectors.add(this.group);
        vectors.add(this.state);
        vectors.add(this.behavior);
        vectors.add(this.start_time);
        vectors.add(this.end_time);
        for (String name: this.attrs.keySet()) {
            vectors.add(this.attrs.get(name));
        }

        this.schemaRoot = new VectorSchemaRoot(fields, vectors);
    }

    public void addRow(int index, CandidateActivity activity, DiseaseState state) {
        this.lid.set(index, activity.getActivity().getLocation().getLocationID());
        this.pid.set(index, activity.getActivity().getPid());
        this.group.set(index, 0);
        this.state.set(index, state.getCode());
        this.behavior.set(index, activity.getRiskMitigationPolicy().getCode());
        this.start_time.set(index, activity.getActivity().getStart_time().getSeconds());
        this.end_time.set(index, activity.getActivity().getStart_time().getSeconds() + activity.getActivity().getDuration());
        this.attrs.get(Constants.VISIBLE_ATTRIBUTE_SYMPTOMATIC).set(index, state.equals(DiseaseState.INFECTED_SYMPTOMATIC) ? 1 : 0);
        this.attrs.get(Constants.VISIBLE_ATTRIBUTE_MASK).set(index, activity.getRiskMitigationPolicy().isMask() ? 1 : 0);
        this.attrs.get(Constants.VISIBLE_ATTRIBUTE_DISTANCING).set(index, activity.getRiskMitigationPolicy().isDistance() ? 1 : 0);
    }

    public void setValueCount(int count) {
        this.lid.setValueCount(count);
        this.pid.setValueCount(count);
        this.group.setValueCount(count);
        this.state.setValueCount(count);
        this.behavior.setValueCount(count);
        this.start_time.setValueCount(count);
        this.end_time.setValueCount(count);
        for (String name: attrs.keySet()) {
            this.attrs.get(name).setValueCount(count);
        }
        this.schemaRoot.setRowCount(count);
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArrowFileWriter writer = new ArrowFileWriter(this.schemaRoot, null, Channels.newChannel(out));

        writer.start();
        writer.writeBatch();
        writer.end();

        writer.close();
        byte[] outb = out.toByteArray();
        out.close();

        return outb;
    }

    private static int get_max_rows_for_visits(List<Future<DeliberationResult<CandidateActivity>>> agentActions) {
        int max_rows = 0;
        try {
            for (int i = 0; i < agentActions.size(); i++) {
                max_rows += agentActions.get(i).get().getActions().size();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return max_rows;
    }

    /**
     * Encode agent actions on a data frame using the main thread of the JVM.
     *
     * This method serves as a reference for the same multi-threaded procedure.
     * @param agentActions
     * @param stateMap
     * @param allocator
     * @return
     */
    public static VisitDataFrame fromAgentActions(
        List<Future<DeliberationResult<CandidateActivity>>> agentActions,
        AgentStateMap stateMap,
        BufferAllocator allocator
    ) {
        int max_rows = get_max_rows_for_visits(agentActions);

        VisitDataFrame dataFrame = new VisitDataFrame(max_rows, allocator);

        int i = 0;
        try {
            for(Future<DeliberationResult<CandidateActivity>> futureResult : agentActions) {
                    for(CandidateActivity activity : futureResult.get().getActions()) {
                        DiseaseState state = stateMap.getAgentState(futureResult.get().getAgentID()).getState();
                        dataFrame.addRow(i, activity, state);
                        activity.setDiseaseState(state);
                        i++;
                    }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        assert i == max_rows;
        dataFrame.setValueCount(i);
        return dataFrame;
    }

    public void close() {
        lid.close();
        pid.close();
        group.close();
        state.close();
        behavior.close();
        start_time.close();
        end_time.close();
        for (String name: attrs.keySet()) {
            attrs.get(name).close();
        }
    }

    public VectorSchemaRoot getSchemaRoot() {
        return schemaRoot;
    }
}
