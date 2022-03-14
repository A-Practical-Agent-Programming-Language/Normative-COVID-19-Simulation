package nl.uu.iss.ga.util;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomGeneratorFactory;

import java.util.Random;

/**
 * Stupid extra class I wish I wouldn't need, but the RandomGenerator interface is, for some reason, loaded
 * by a different class loader than the rest of the project, so casting a Java.util.Random object to a JDKRandomGenerator
 * fails.
 *
 * I don't know how to solve the class loader issue quickly. Perhaps it is caused by incompatible imports in Maven?
 */
public class JavaUtilRandomGenerator implements RandomGenerator {

    private final Random random;

    public JavaUtilRandomGenerator(Random random) {
        this.random = random;
    }

    public void setSeed(int seed) {
        this.random.setSeed((long)seed);
    }

    public void setSeed(int[] seed) {
        this.random.setSeed(RandomGeneratorFactory.convertToLong(seed));
    }

    @Override
    public void setSeed(long l) {
        this.random.setSeed(l);
    }

    @Override
    public void nextBytes(byte[] bytes) {
        this.random.nextBytes(bytes);
    }

    @Override
    public int nextInt() {
        return this.random.nextInt();
    }

    @Override
    public int nextInt(int i) {
        return this.random.nextInt(i);
    }

    @Override
    public long nextLong() {
        return this.random.nextLong();
    }

    @Override
    public boolean nextBoolean() {
        return this.random.nextBoolean();
    }

    @Override
    public float nextFloat() {
        return this.random.nextFloat();
    }

    @Override
    public double nextDouble() {
        return this.random.nextDouble();
    }

    @Override
    public double nextGaussian() {
        return this.random.nextGaussian();
    }
}
