package main.java.nl.uu.iss.ga.util;

import org.apache.commons.math3.distribution.BetaDistribution;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Methods {

    private static final int SAMPLE_SIZE = 100;

    private static final Logger LOGGER = Logger.getLogger(Methods.class.getSimpleName());

    public static BetaDistribution getBetaDistribution(Random random, double mode) {
        mode = mode == 0.0 ? 0.01 : mode == 1.0 ? 0.99 : mode;
        JavaUtilRandomGenerator generator = new JavaUtilRandomGenerator(random);
        return new BetaDistribution(generator, mode * SAMPLE_SIZE, (1-mode) * SAMPLE_SIZE);
    }

    public static boolean createOutputFile(File fout){
        try {
            if (!(fout.getParentFile().exists() || fout.getParentFile().mkdirs())) {
                throw new IOException("Failed to create file " + fout.getParentFile().getAbsolutePath());
            }
            if (!(fout.exists() || fout.createNewFile())) {
                throw new IOException("Failed to create file " + fout.getName());
            }
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create output file " + fout.getAbsolutePath(), e);
            return false;
        }
    }
}
