package main.java.nl.uu.iss.ga.util.tracking;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.pansim.state.AgentState;
import main.java.nl.uu.iss.ga.pansim.state.AgentStateMap;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VisitGraph {

    private final String date;
    private final HashMap<Long, VisitLocation> locations;
    private final AgentStateMap agentStateMap;

    private FileOutputStream fos;
    private BufferedWriter bw;
    private OutputStreamWriter osw;

    public VisitGraph(String date, HashMap<AgentID, List<CandidateActivity>> hashMap, AgentStateMap agentStateMap) {
        this.date = date;
        this.locations = new HashMap<>();
        this.agentStateMap = agentStateMap;
        createVisitLocations(hashMap);
    }

    public void createVisitEdges(String outputDir) {
        try {
            createOutputWriter(outputDir);
            for (VisitLocation location : locations.values()) {
                location.findAndWriteEdges();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeFile();
        }
    }

    private void createVisitLocations(HashMap<AgentID, List<CandidateActivity>> hashMap) {
        for(AgentID agentID : hashMap.keySet()) {
            Long pid = this.agentStateMap.getAgentToPidMap().get(agentID);
            for(CandidateActivity activity : hashMap.get(agentID)) {
                Long lid = activity.getActivity().getLocation().getLocationID();
                if(!locations.containsKey(lid)) {
                    locations.put(lid, new VisitLocation());
                }
                locations.get(lid).addVisit(pid, activity);
            }
        }
    }

    class VisitLocation {

        private List<PersonVisitTuple> visits = new ArrayList<>();;

        public void addVisit(long pid, CandidateActivity activity) {
            this.visits.add(new PersonVisitTuple(pid, activity));
        }

        public void findAndWriteEdges() throws IOException {
            this.visits.sort(null);

            for(int i = 0; i < visits.size(); i++) {
                for(int j = i + 1; j < visits.size(); j++) {
                    PersonVisitTuple pvt1 = this.visits.get(i);
                    PersonVisitTuple pvt2 = this.visits.get(j);
                    if(pvt1.pid != pvt2.pid && pvt1.hasMet(pvt2)) {
                        String line = String.format(
                                "%d;%d;Undirected;%d;%s\n",
                                pvt1.pid,
                                pvt2.pid,
                                pvt1.get_duration(pvt2),
                                VisitGraph.this.date
                        );
                        bw.write(line);
                    }

                    // DO not perform more comparisons than necessary. Break out of J-loop and continue I-loop
                    if(pvt2.startTime > pvt1.endTime) break;
                }
            }
        }
    }

    static class PersonVisitTuple implements Comparable<PersonVisitTuple> {

        private long pid;
        private Activity activity;
        private Integer startTime;
        private int duration;
        private int endTime;

        public PersonVisitTuple(long pid, CandidateActivity candidateActivity){
            this.pid = pid;
            this.activity = candidateActivity.getActivity();
            this.startTime = this.activity.getStart_time().getSeconds();
            this.duration = this.activity.getDuration();
            this.endTime = this.activity.getStart_time().getSeconds() + this.activity.getDuration();
        }

        boolean hasMet(PersonVisitTuple personVisitTuple) {
            return this.startTime <= personVisitTuple.endTime && this.endTime >= personVisitTuple.startTime;
        }

        long get_duration(PersonVisitTuple personVisitTuple) {
            long duration = this.activity.getDuration();
            if (this.startTime < personVisitTuple.startTime) {
                duration -= (personVisitTuple.startTime - this.startTime);
            }
            if (this.endTime > personVisitTuple.endTime) {
                duration -= (this.endTime - personVisitTuple.endTime);
            }
            return duration;
        }

        @Override
        public int compareTo(PersonVisitTuple personVisitTuple) {
            return startTime.compareTo(personVisitTuple.startTime);
        }
    }

    public void createVisitNodes(String outputDir) {
        File fout = (Path.of(outputDir).isAbsolute() ?
                Paths.get(outputDir, "visits-nodes.csv") :
                Paths.get("output", outputDir, "visits-nodes.csv")).toFile();

        boolean writeHeader = !fout.exists();
        try {
            if (!(fout.getParentFile().exists() || fout.getParentFile().mkdirs()) ||
                    !(fout.exists() || fout.createNewFile())) {
                throw new IOException("Failed to create file " + fout.getAbsolutePath());
            }
            if (!(fout.exists() || fout.createNewFile())) {
                throw new IOException("Failed to create file " + fout.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try(FileOutputStream fos = new FileOutputStream(fout, true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))
        ) {
            if(writeHeader) {
                // TODO maybe track more than just this, but what ever
                bw.write("Id;State;Date\n");
            }
            for(AgentState agentState : this.agentStateMap.getAllAgentStates()) {
                bw.write(String.format(
                        "%d;%s;%s\n",
                        agentState.getPid(),
                        agentState.getState(),
                        this.date
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createOutputWriter(String outputDir) throws IOException {
        File fout = (Path.of(outputDir).isAbsolute() ?
                Paths.get(outputDir, "visits-edges.csv") :
                Paths.get("output", outputDir, "visits-edges.csv")).toFile();

        boolean writeHeader = !fout.exists();

        if (!(fout.getParentFile().exists() || fout.getParentFile().mkdirs()) ||
                !(fout.exists() || fout.createNewFile())) {
            throw new IOException("Failed to create file " + fout.getAbsolutePath());
        }
        if (!(fout.exists() || fout.createNewFile())) {
            throw new IOException("Failed to create file " + fout.getName());
        }

        this.fos = new FileOutputStream(fout, true);
        this.osw = new OutputStreamWriter(fos);
        this.bw = new BufferedWriter(osw);

        if(writeHeader) {
            bw.write("Source;Target;Type;Weight;Date\n");
        }
    }

    private void closeFile() {
        try {
            if(this.bw != null)bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if(this.osw != null) this.osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if(this.fos != null) fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
