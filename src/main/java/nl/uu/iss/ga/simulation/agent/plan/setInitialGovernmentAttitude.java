package main.java.nl.uu.iss.ga.simulation.agent.plan;

import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import main.java.nl.uu.iss.ga.simulation.agent.environment.EnvironmentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.PlanToAgentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.plan.PlanExecutionError;
import nl.uu.cs.iss.ga.sim2apl.core.plan.builtin.RunOncePlan;

public class setInitialGovernmentAttitude extends RunOncePlan {

    private final double attitudeMode;
    private final double bias;

    public setInitialGovernmentAttitude(double attitudeMode, double bias) {
        this.attitudeMode = attitudeMode;
        this.bias = bias;
    }

    @Override
    public Object executeOnce(PlanToAgentInterface planToAgentInterface) throws PlanExecutionError {
        BeliefContext beliefContext = planToAgentInterface.getContext(BeliefContext.class);
        EnvironmentInterface ei = beliefContext.getEnvironmentInterface();

        beliefContext.setGovernmentTrustFactor(getFromSkewedDistribution(ei.getRnd().nextGaussian()));
        return null;
    }

    /**
     * From https://stackoverflow.com/questions/5853187/skewing-java-random-number-generation-toward-a-certain-number
     */
    private double getFromSkewedDistribution(double unitGaussian) {
        double range = 6;
        double mid = 0;
        double biasFactor = Math.exp(bias);
        return mid+(range*(biasFactor/(biasFactor+Math.exp(-unitGaussian/this.attitudeMode))-0.5));
    }
}
