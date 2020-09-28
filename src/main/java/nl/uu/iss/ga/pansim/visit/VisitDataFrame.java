package main.java.nl.uu.iss.ga.pansim.visit;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.util.Constants;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
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

    public void addRow(int index, CandidateActivity activity) {
        this.lid.set(index, activity.getActivity().getLocation().getLocationID());
        this.pid.set(index, activity.getActivity().getPid());
        this.group.set(index, 0);
        this.state.set(index, 0); // TODO find state
        this.behavior.set(index, activity.getRiskMitigationPolicy().getCode());
        this.start_time.set(index, activity.getActivity().getStart_time().getSeconds());
        this.end_time.set(index, activity.getActivity().getStart_time().getSeconds() + activity.getActivity().getDuration());
        for(String name : Constants.VISIBLE_ATTRIBUTES) {
            this.attrs.get(name).set(index, 0); // TODO extract from activity & state file
        }
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

    public static VisitDataFrame fromAgentActions(HashMap<AgentID, List<CandidateActivity>> agentActions, BufferAllocator allocator) {
        int max_rows = agentActions.values().stream().map(List::size).reduce(Integer::sum).orElse(0);
        VisitDataFrame dataFrame = new VisitDataFrame(max_rows, allocator);

        int i = 0;
        for(AgentID agentID : agentActions.keySet()) {
            for(CandidateActivity activity : agentActions.get(agentID)) {
                dataFrame.addRow(i, activity);
                i++;
            }
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