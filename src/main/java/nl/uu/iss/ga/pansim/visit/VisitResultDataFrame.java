package main.java.nl.uu.iss.ga.pansim.visit;

import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import main.java.nl.uu.iss.ga.util.Constants;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.vector.BigIntVector;
import org.apache.arrow.vector.Float8Vector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.ipc.ArrowFileReader;
import org.apache.arrow.vector.ipc.message.ArrowBlock;
import org.apache.arrow.vector.util.ByteArrayReadableSeekableByteChannel;

import java.io.IOException;
import java.util.HashMap;

public class VisitResultDataFrame {

    private BigIntVector lid;
    private BigIntVector pid;
    private Float8Vector inf_prob;
    private IntVector n_contacts;
    private HashMap<String, IntVector> attrs;

    private VectorSchemaRoot schemaRoot;

    public VisitResultDataFrame(byte[] inb, BufferAllocator allocator) throws IOException {
        ByteArrayReadableSeekableByteChannel in = new ByteArrayReadableSeekableByteChannel(inb);
        ArrowFileReader reader = new ArrowFileReader(in, allocator);

        ArrowBlock block = reader.getRecordBlocks().get(0);
        reader.loadRecordBatch(block);
        schemaRoot = reader.getVectorSchemaRoot();

        lid = (BigIntVector) schemaRoot.getVector("lid");
        pid = (BigIntVector) schemaRoot.getVector("pid");
        inf_prob = (Float8Vector) schemaRoot.getVector("inf_prob");
        n_contacts = (IntVector) schemaRoot.getVector("n_contacts");

        attrs = new HashMap<>();
        for (String name : Constants.VISIBLE_ATTRIBUTES) {
            IntVector vector = (IntVector) schemaRoot.getVector(name);
            attrs.put(name, vector);
        }
    }

    public LocationHistoryContext.Visit getAgentVisit(int row) {
        return new LocationHistoryContext.Visit(
                this.pid.get(row),
                this.lid.get(row),
                this.inf_prob.get(row),
                this.n_contacts.get(row),
                this.attrs.get(Constants.VISIBLE_ATTRIBUTES[0]).get(row),
                this.attrs.get(Constants.VISIBLE_ATTRIBUTES[1]).get(row),
                this.attrs.get(Constants.VISIBLE_ATTRIBUTES[2]).get(row)
        );
    }

    public VectorSchemaRoot getSchemaRoot() {
        return schemaRoot;
    }
}
