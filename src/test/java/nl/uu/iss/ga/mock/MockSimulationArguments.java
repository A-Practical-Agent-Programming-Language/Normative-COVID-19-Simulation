package nl.uu.iss.ga.mock;

import nl.uu.iss.ga.util.config.SimulationArguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MockSimulationArguments {

    private static SimulationArguments simulationArguments;

    public static void ensureInstance(boolean symptomaticLinearApproach) {
        List<String> args = Arrays.asList(
                "--mode-conservative", "0.5",
                "--mode-liberal", "0.5",
                "--fatigue", "0",
                "--fatigue-start", "0",
                "--config", "src/main/resources/config.toml"
        );
        if (symptomaticLinearApproach) {
            args.add("--use-linear-for-symptomatic-factor");
        }

        try {
            if (simulationArguments == null) {
                simulationArguments = SimulationArguments.parseArguments(args.toArray(new String[0]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
