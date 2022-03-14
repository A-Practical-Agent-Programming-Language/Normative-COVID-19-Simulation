package nl.uu.iss.ga.mock;

import nl.uu.cs.iss.ga.sim2apl.core.agent.Agent;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Context;
import nl.uu.cs.iss.ga.sim2apl.core.agent.ContextContainer;
import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.model.data.Person;
import nl.uu.iss.ga.model.data.dictionary.Designation;
import nl.uu.iss.ga.simulation.agent.context.BeliefContext;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

public class MockAgent {

    public static Agent<CandidateActivity> createAgent(
            double trust,
            Designation designation,
            Context... contexts
    ) {
        Agent<CandidateActivity> agent = mock(Agent.class);
        getOrCreateContextContainer(agent);

        Person personContext = mock(Person.class);
        when(personContext.getDesignation()).thenReturn(designation);

        BeliefContext beliefContext = mock(BeliefContext.class);
        doCallRealMethod().when(beliefContext).setPriorTrustAttitude(anyDouble());
        when(beliefContext.getPriorTrustAttitude()).thenCallRealMethod();
        beliefContext.setPriorTrustAttitude(trust);

        System.out.println(BeliefContext.class);
        System.out.println(beliefContext.getClass());
        System.out.println(mockingDetails(beliefContext).getMockCreationSettings().getTypeToMock());

        addContext(agent, beliefContext, personContext);
        addContext(agent, contexts);

        return agent;
    }

    public static void addContext(Agent<CandidateActivity> agent, Context... contexts) {
        ContextContainer container = getOrCreateContextContainer(agent);
        for(Context context : contexts) {
            Class<? extends Context> clazz;
            if (mockingDetails(context).isMock()) {
                Class<?> mockedClass = mockingDetails(context).getMockCreationSettings().getTypeToMock();
                clazz = (Class<? extends Context>) mockedClass;
            } else {
                clazz = context.getClass();
            }
            container.addImplementedContext(context, clazz);
        }
    }

    public static void setTrust(Agent<CandidateActivity> agent, double trust) {
        agent.getContext(BeliefContext.class).setPriorTrustAttitude(trust);
    }

    public static double getTrust(Agent<CandidateActivity> agent) {
        return agent.getContext(BeliefContext.class).getPriorTrustAttitude();
    }

    private static ContextContainer getOrCreateContextContainer(Agent<CandidateActivity> agent) {
        ContextContainer contextContainer = new ContextContainer();
        try {
            Field field = agent.getClass().getSuperclass().getDeclaredField("contextContainer");
            field.setAccessible(true);
            if (field.get(agent) != null) {
                contextContainer = (ContextContainer) field.get(agent);
            } else {
                field.set(agent, contextContainer);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return contextContainer;
    }
}
