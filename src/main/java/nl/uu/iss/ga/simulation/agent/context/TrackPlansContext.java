package main.java.nl.uu.iss.ga.simulation.agent.context;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.factor.IFactor;
import main.java.nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Context;

import java.util.*;

public class TrackPlansContext implements Context {

    private final HashMap<Long, Location> locationMap = new HashMap();

    public void reset() {
        locationMap.clear();
    }

    public void addLocation(CandidateActivity activity, List<Norm> norms) {
        long lid = activity.getActivity().getLocation().getLocationID();
        if(!this.locationMap.containsKey(lid)) {
            this.locationMap.put(lid, new Location());
        }
        this.locationMap.get(lid).addActivity(activity, norms);
    }

    public Collection<Location> getLocationBasedActivities() {
        return this.locationMap.values();
    }

    public static class Location {
        private long locationID;
        private final List<CandidateActivity> activityList = new ArrayList<>();
        private final List<Norm> norms = new ArrayList<>();

        private void addActivity(CandidateActivity activity, List<Norm> norms) {
            this.locationID = activity.getActivity().getLocation().getLocationID();
            this.activityList.add(activity);
            this.norms.addAll(norms);
        }

        public long getLocationID() {
            return locationID;
        }

        public List<CandidateActivity> getActivityList() {
            return activityList;
        }

        public List<Norm> getNorms() {
            return norms;
        }

        public List<IFactor> getUniqueFactors() {
            List<IFactor> factors = new ArrayList<>();
            for(Norm norm : this.norms) {
                if (norm instanceof NonRegimentedNorm) {
                    for (IFactor factor : ((NonRegimentedNorm) norm).getFactors()) {
                        if (!factors.contains(factor)) {
                            factors.add(factor);
                        }
                    }
                }
            }
            return factors;
        }
    }

}
