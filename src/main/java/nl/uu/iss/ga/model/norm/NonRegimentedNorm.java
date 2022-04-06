package nl.uu.iss.ga.model.norm;

import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.iss.ga.model.data.Activity;
import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.model.factor.IFactor;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;

public abstract class NonRegimentedNorm extends Norm {

    private static final double POSTERIOR_LOGISTIC_GROWTH_RATE = 10f;
    public static final int N_DAYS_LOOKBACK = 14;

    /**
     * https://www.desmos.com/calculator/gocrbtoaqj
     *
     *
     * @param government_trust_factor
     * @param positive_evidence_probabilities
     * @return
     */
    protected static double norm_compliance_posterior(double government_trust_factor, double... positive_evidence_probabilities) {
        double x0 = 1 - government_trust_factor;
        OptionalDouble evidence = Arrays.stream(positive_evidence_probabilities).average();
        if(evidence.isPresent()) {
            return 1 / (
                    1 + Math.pow(
                            Math.E,
                            (-POSTERIOR_LOGISTIC_GROWTH_RATE * (evidence.getAsDouble() - x0))
                    )
            );
        } else {
            return government_trust_factor;
        }
    }

    protected static double norm_violation_posterior(double government_trust_factor, double... positive_evidence_probabilities) {
        return 1 - norm_compliance_posterior(government_trust_factor, positive_evidence_probabilities);
    }

    /**
     * Calculates the agents attitude towards the norm, where the value indicates the agents tendency to ignore
     * this norm for the given activity.
     *
     * @param agentContextInterface Interface to agent's belief context
     * @param activity              Activity for which to consider the norm
     * @return                      Value between 0 and 1, with 1 indicating a higher tendency to violate this norm
     */
    public abstract double calculateAttitude(AgentContextInterface<CandidateActivity> agentContextInterface, Activity activity);

    public abstract List<IFactor> getFactors();

    /**
     * Produces a weight for the given value, that is close to zero, if the value is close to 0.5 (which indicates
     * an agent is ambivalent).
     *
     * Note that the weights should be used to divide the sum of the factors for weighing a norm
     *
     * See @url{https://www.desmos.com/calculator/vicfmok1xi}
     *
     * @param value     A value that should be weighted
     * @return          A weight between 0 and 1 (inclusive)
     */
    @Deprecated
    protected static double weight(double value) {
        return Math.abs(Math.pow((2 * value) - 1, 3));
    }
}
