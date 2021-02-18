package main.java.nl.uu.iss.ga;

import main.java.nl.uu.iss.ga.util.Methods;
import org.apache.commons.math3.distribution.BetaDistribution;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ShowDistribution {

    final static public Random RANDOM = new Random(System.currentTimeMillis());

    public static void main(String[] args) {
        Map<Integer, CountedDouble> countMap = new TreeMap<>();
        BetaDistribution dist = Methods.getBetaDistribution(RANDOM, 0.5);
        for(int i = 0; i < 100000; i++) {
            double d = dist.sample();
            int di = (int)Math.round(d * 100);
            if(countMap.containsKey(di)) {
                countMap.get(di).increment();
            } else {
                countMap.put(di, new CountedDouble(di));
            }
        }

        for(CountedDouble cd : countMap.values().stream().sorted().collect(Collectors.toList())) {
            System.out.println(cd);
        }
    }

    static class CountedDouble implements Comparable<CountedDouble> {
        double d;
        int count;

        public CountedDouble(double d) {
            this.d = d;
            this.count = 1;
        }

        public void increment() {
            this.count++;
        }

        @Override
        public String toString() {
            return String.format("%.5f:\t\t%s", this.d / 100, "+".repeat(this.count / 10));
        }

        public int compareTo(CountedDouble countedDouble) {
            return countedDouble.d - this.d < 0 ? -1 : countedDouble.d - this.d == 0 ? 0 : 1;
        }
    }
}
