package nl.uu.iss.ga.pansim.state;

import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;
import nl.uu.iss.ga.model.disease.AgentGroup;
import nl.uu.iss.ga.model.disease.DiseaseState;
import nl.uu.cs.iss.ga.sim2apl.core.tick.TickExecutor;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.vector.*;
import org.apache.arrow.vector.ipc.ArrowFileReader;
import org.apache.arrow.vector.ipc.ArrowFileWriter;
import org.apache.arrow.vector.ipc.message.ArrowBlock;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.util.ByteArrayReadableSeekableByteChannel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class StateDataFrame {
    private BigIntVector pid;
    private TinyIntVector group;
    private TinyIntVector current_state;
    private TinyIntVector next_state;
    private IntVector dwell_time;
    private BigIntVector seed;

    private VectorSchemaRoot schemaRoot;

    public StateDataFrame(int max_rows, BufferAllocator allocator) {
        pid = new BigIntVector("pid", allocator);
        group = new TinyIntVector("group", allocator);
        current_state = new TinyIntVector("current_state", allocator);
        next_state = new TinyIntVector("next_state", allocator);
        dwell_time = new IntVector("dwell_time", allocator);
        seed = new BigIntVector("seed", allocator);

        pid.allocateNew(max_rows);
        group.allocateNew(max_rows);
        current_state.allocateNew(max_rows);
        next_state.allocateNew(max_rows);
        dwell_time.allocateNew(max_rows);
        seed.allocateNew(max_rows);

        List<Field> fields = Arrays.asList(
                pid.getField(),
                group.getField(),
                current_state.getField(),
                next_state.getField(),
                dwell_time.getField(),
                seed.getField()
        );

        List<FieldVector> vectors = Arrays.asList(
                pid,
                group,
                current_state,
                next_state,
                dwell_time,
                seed
        );

        schemaRoot = new VectorSchemaRoot(fields, vectors);
    }

    public StateDataFrame(byte[] inb, BufferAllocator allocator) throws IOException {
        ByteArrayReadableSeekableByteChannel in = new ByteArrayReadableSeekableByteChannel(inb);
        ArrowFileReader reader = new ArrowFileReader(in, allocator);

        ArrowBlock block = reader.getRecordBlocks().get(0);
        reader.loadRecordBatch(block);
        schemaRoot = reader.getVectorSchemaRoot();

        pid = (BigIntVector) schemaRoot.getVector("pid");
        group = (TinyIntVector) schemaRoot.getVector("group");
        current_state = (TinyIntVector) schemaRoot.getVector("current_state");
        next_state = (TinyIntVector) schemaRoot.getVector("next_state");
        dwell_time = (IntVector) schemaRoot.getVector("dwell_time");
        seed = (BigIntVector) schemaRoot.getVector("seed");
    }

    public static StateDataFrame fromAgentStateMap(List<AgentState> agentStates, BufferAllocator allocator) {
        int max_rows = agentStates.size();
        StateDataFrame dataFrame = new StateDataFrame(max_rows, allocator);
        for(int i = 0; i < agentStates.size(); i++) {
            dataFrame.addRow(i, agentStates.get(i));
        }
        dataFrame.setValueCount(max_rows);
        return dataFrame;
    }

    public AgentState getAgentState(int row) {
        return new AgentState(
                pid.get(row),
                CodeTypeInterface.parseAsEnum(AgentGroup.class, group.get(row)),
                CodeTypeInterface.parseAsEnum(DiseaseState.class, String.valueOf(current_state.get(row))),
                CodeTypeInterface.parseAsEnum(DiseaseState.class, String.valueOf(next_state.get(row))),
                dwell_time.get(row),
                seed.get(row)
        );
    }

    public void addRow(int index, AgentState state) {
        this.pid.set(index, state.getPid());
        this.group.set(index, state.getGroup().getCode());
        this.current_state.set(index, state.getState().getCode());
        this.next_state.set(index, state.getNextState().getCode());
        this.dwell_time.set(index, state.getDwell_time());
        this.seed.set(index, state.getRandom().nextInt());
    }

    public void setValueCount(int count) {
        pid.setValueCount(count);
        group.setValueCount(count);
        current_state.setValueCount(count);
        next_state.setValueCount(count);
        dwell_time.setValueCount(count);
        seed.setValueCount(count);
        schemaRoot.setRowCount(count);
    }

    public void close() {
        pid.close();
        group.close();
        current_state.close();
        next_state.close();
        dwell_time.close();
        seed.close();
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArrowFileWriter writer = new ArrowFileWriter(schemaRoot, null, Channels.newChannel(out));

        writer.start();
        writer.writeBatch();
        writer.end();

        writer.close();
        byte[] outb = out.toByteArray();
        out.close();

        return outb;
    }

    public VectorSchemaRoot getSchemaRoot() {
        return schemaRoot;
    }
}
