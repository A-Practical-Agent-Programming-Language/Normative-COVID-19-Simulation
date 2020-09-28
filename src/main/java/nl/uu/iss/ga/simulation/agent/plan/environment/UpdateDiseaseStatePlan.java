package main.java.nl.uu.iss.ga.simulation.agent.plan.environment;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.disease.DiseaseState;
import main.java.nl.uu.iss.ga.simulation.agent.context.DiseaseRiskContext;
import main.java.nl.uu.iss.ga.simulation.agent.trigger.DiseaseStateUpdatedTrigger;
import nl.uu.cs.iss.ga.sim2apl.core.agent.PlanToAgentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.plan.PlanExecutionError;
import nl.uu.cs.iss.ga.sim2apl.core.plan.builtin.RunOncePlan;

public class UpdateDiseaseStatePlan extends RunOncePlan<CandidateActivity> {

    private DiseaseStateUpdatedTrigger trigger;

    public UpdateDiseaseStatePlan(DiseaseStateUpdatedTrigger trigger) {
        this.trigger = trigger;
    }

    @Override
    public CandidateActivity executeOnce(PlanToAgentInterface<CandidateActivity> planInterface) throws PlanExecutionError {
        DiseaseRiskContext context = planInterface.getContext(DiseaseRiskContext.class);

        context.setState(trigger.getNewDiseaseState());

        // TODO will agents always believe they are symptomatic?
        context.setSymptomatic(trigger.getNewDiseaseState().equals(DiseaseState.INFECTED_SYMPTOMATIC) && trigger.isSymptomatic());

        return null;
    }
}
