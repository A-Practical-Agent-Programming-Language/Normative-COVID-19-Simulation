package main.java.nl.uu.iss.ga.util;

import java.util.Random;

public class Methods {
    public static double nextSkewedBoundedDouble(Random rnd, double mode) {
        return sigmoid(rnd.nextGaussian() + skewModeVariable(rnd,mode));
    }

    private static double skewModeVariable(Random rnd, double mode) {
        return rnd.nextDouble() * ((mode - .5) * 2);
    }

    public static double sigmoid(double x) {
        return 1 / (1 + Math.pow(Math.E, -1 * x));
    }

    public static double sigmoidRangeZeroOne(double x) {
        return sigmoid(x * 2 - 1);
    }
}
