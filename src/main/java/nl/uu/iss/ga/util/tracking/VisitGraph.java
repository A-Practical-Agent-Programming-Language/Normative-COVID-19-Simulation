package nl.uu.iss.ga.util.tracking;

import nl.uu.iss.ga.model.data.Activity;
import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.pansim.state.AgentState;
import nl.uu.iss.ga.pansim.state.AgentStateMap;
import nl.uu.iss.ga.util.Methods;
import nl.uu.cs.iss.ga.sim2apl.core.deliberation.DeliberationResult;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class VisitGraph {

    private final String date;
    private final HashMap<Long, VisitLocation> locations;
    private final AgentStateMap agentStateMap;

    private FileOutputStream fos;
    private BufferedWriter bw;
    private OutputStreamWriter osw;

    public VisitGraph(String date, List<Future<DeliberationResult<CandidateActivity>>> agentActions, AgentStateMap agentStateMap) throws ExecutionException, InterruptedException {
        this.date = date;
        this.locations = new HashMap<>();
        this.agentStateMap = agentStateMap;
        createVisitLocations(agentActions);
    }

    public void createVisitEdges(File outputDir) {
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

    private void createVisitLocations(List<Future<DeliberationResult<CandidateActivity>>> agentActions) throws ExecutionException, InterruptedException {
        for(Future<DeliberationResult<CandidateActivity>> future : agentActions) {
            DeliberationResult<CandidateActivity> result = future.get();
            Long pid = this.agentStateMap.getAgentToPidMap().get(result.getAgentID());
            for(CandidateActivity activity : result.getActions()) {
                Long lid = activity.getActivity().getLocation().getLocationID();
                if(!locations.containsKey(lid)) {
                    locations.put(lid, new VisitLocation());
                }
                locations.get(lid).addVisit(pid, activity);
            }
        }
    }

    class VisitLocation {

        private final List<PersonVisitTuple> visits = new ArrayList<>();;

        public void addVisit(long pid, CandidateActivity activity) {
            this.visits.add(new PersonVisitTuple(pid, activity));
        }

        public void findAndWriteEdges() throws IOException {
            this.visits.sort(null);

            // TODO perhaps this can be parallelized? But we hardly use it, so leave for future work

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

        private final long pid;
        private final Integer startTime;
        private int duration;
        private final int endTime;

        public PersonVisitTuple(long pid, CandidateActivity candidateActivity){
            this.pid = pid;
            Activity activity = candidateActivity.getActivity();
            this.startTime = activity.getStart_time().getSeconds();
            this.duration = activity.getDuration();
            this.endTime = activity.getStart_time().getSeconds() + activity.getDuration();
        }

        boolean hasMet(PersonVisitTuple personVisitTuple) {
            return this.startTime <= personVisitTuple.endTime && this.endTime >= personVisitTuple.startTime;
        }

        long get_duration(PersonVisitTuple personVisitTuple) {
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

    public void createVisitNodes(File outputDir) {
        File fout = new File(outputDir, "visits-nodes.csv");
        boolean writeHeader = !fout.exists();

        Methods.createOutputFile(fout);

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

    private void createOutputWriter(File outputDir) throws IOException {
        File fout = new File(outputDir, "visits-edges.csv");
        boolean writeHeader = !fout.exists();
        Methods.createOutputFile(fout);

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
