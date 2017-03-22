package org.roi.itlab.cassandra.random_attributes;

import org.apache.commons.math3.random.MersenneTwister;

/**
 * author Anush
 */
public class ExperienceRandomGenerator extends RandomGeneratorBuilder{

    private int age;
    private final int LICENSE_AGE = 18;
    private final int MIN_EXP = 0;

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public void buildGenerator(int seed) {

        int max_exp = age - LICENSE_AGE;

        double[] x = {0.0,5.0,30.0,60.0, 91.0};
        double[] y  = {1.0,2.0,4.0,2.0, 0.1};
        randomGenerator = new RandomGenerator(new MersenneTwister(seed),x,y);
        randomGenerator.setMax(max_exp);
    }

}
