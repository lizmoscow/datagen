package org.roi.itlab.cassandra;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.roi.itlab.cassandra.person.Person;
import org.roi.itlab.cassandra.random_attributes.IntensityNormalGenerator;
import org.roi.itlab.cassandra.random_attributes.PersonGenerator;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Аня on 22.03.2017.
 */
public class AccidentRateIT {

    private static final int ALL_DRIVERS_COUNT = 10_000;
    private static final int DRIVERS_COUNT = 1_000;
    private AccidentRate accidentRate;
    private ArrayList<Person> drivers;
    private RandomGenerator rng = new MersenneTwister(2);

    @Before
    public void setUp() throws IOException {
        PersonGenerator personGenerator = new PersonGenerator(rng);
        drivers = new ArrayList<>(ALL_DRIVERS_COUNT);
        for (int i = 0; i < ALL_DRIVERS_COUNT; i++) {
            drivers.add(personGenerator.getResult());
        }

        IntensityMap intensityMap = new IntensityMap(drivers);
        int maxIntensity = intensityMap.getMaxIntensity();
        accidentRate = new AccidentRate(intensityMap, new IntensityNormalGenerator(maxIntensity, rng), rng);
    }

    @Test
    public void accidentRateTest() {
        int accidents = 0;
        for (Person person : drivers.subList(0, DRIVERS_COUNT)) {
            accidents += accidentRate.getAccidents(person, 365);
        }
        Assert.assertEquals((double) accidents / DRIVERS_COUNT, 0.35, 0.2);
        System.out.println("Accidents: " + accidents);
    }
}
