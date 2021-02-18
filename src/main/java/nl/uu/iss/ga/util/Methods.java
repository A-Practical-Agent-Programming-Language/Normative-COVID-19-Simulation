package main.java.nl.uu.iss.ga.util;

import org.apache.commons.math3.distribution.BetaDistribution;

import java.util.Random;

public class Methods {

    private static final int SAMPLE_SIZE = 100;

    public static BetaDistribution getBetaDistribution(Random random, double mode) {
        mode = mode == 0.0 ? 0.01 : mode == 1.0 ? 0.99 : mode;
        JavaUtilRandomGenerator generator = new JavaUtilRandomGenerator(random);
        return new BetaDistribution(generator, mode * SAMPLE_SIZE, (1-mode) * SAMPLE_SIZE);
    }
}
