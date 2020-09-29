package main.java.nl.uu.iss.ga.pansim.visit;

import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import main.java.nl.uu.iss.ga.util.Constants;
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

    public VisitResultDataFrame(BufferAllocator allocator) {
        lid = new BigIntVector("lid", allocator);
        pid = new BigIntVector("pid", allocator);
        inf_prob = new Float8Vector("inf_prob", allocator);
        n_contacts = new IntVector("n_contacts", allocator);
        attrs = new HashMap<>();
        for(String attr : Constants.VISIBLE_ATTRIBUTES) {
            attrs.put(attr, new IntVector(attr, allocator));
        }

        lid.allocateNew(0);
        pid.allocateNew(0);
        inf_prob.allocateNew(0);
        n_contacts.allocateNew(0);
        for (String name: attrs.keySet()) {
            attrs.get(name).allocateNew(0);
        }

        ArrayList<Field> fields = new ArrayList<>();
        fields.add(lid.getField());
        fields.add(pid.getField());
        fields.add(inf_prob.getField());
        fields.add(n_contacts.getField());
        for (String name: attrs.keySet()) {
            fields.add(attrs.get(name).getField());
        }

        ArrayList<FieldVector> vectors = new ArrayList<>();
        vectors.add(lid);
        vectors.add(pid);
        vectors.add(inf_prob);
        vectors.add(n_contacts);
        for (String name: attrs.keySet()) {
            vectors.add(attrs.get(name));
        }

        schemaRoot = new VectorSchemaRoot(fields, vectors);
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
