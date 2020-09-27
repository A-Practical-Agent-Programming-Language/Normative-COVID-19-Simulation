package main.java.nl.uu.iss.ga.simulation.agent.planscheme;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.simulation.agent.plan.environment.UpdateDiseaseStatePlan;
import main.java.nl.uu.iss.ga.simulation.agent.trigger.DiseaseStateUpdatedTrigger;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Trigger;
import nl.uu.cs.iss.ga.sim2apl.core.plan.Plan;
import nl.uu.cs.iss.ga.sim2apl.core.plan.PlanScheme;

public class ExternalTriggerPlanScheme implements PlanScheme<CandidateActivity> {
    @Override
    public Plan<CandidateActivity> instantiate(Trigger trigger, AgentContextInterface<CandidateActivity> agentContextInterface) {
        if(trigger instanceof DiseaseStateUpdatedTrigger) {
            return new UpdateDiseaseStatePlan((DiseaseStateUpdatedTrigger) trigger);
        }
        return Plan.UNINSTANTIATED();
    }

}
