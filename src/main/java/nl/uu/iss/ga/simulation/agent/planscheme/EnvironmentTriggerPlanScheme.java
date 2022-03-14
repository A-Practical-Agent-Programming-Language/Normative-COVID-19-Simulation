package nl.uu.iss.ga.simulation.agent.planscheme;

import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Trigger;
import nl.uu.cs.iss.ga.sim2apl.core.plan.Plan;
import nl.uu.cs.iss.ga.sim2apl.core.plan.PlanScheme;

public class EnvironmentTriggerPlanScheme implements PlanScheme<CandidateActivity> {

    @Override
    public Plan<CandidateActivity> instantiate(Trigger trigger, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return Plan.UNINSTANTIATED();
    }
}
