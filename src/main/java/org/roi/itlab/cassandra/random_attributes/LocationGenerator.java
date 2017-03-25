package org.roi.itlab.cassandra.random_attributes;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.mongodb.morphia.geo.Point;
import org.roi.itlab.cassandra.Location;
import org.roi.itlab.cassandra.Poi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mkuperman on 3/18/2017.
 */
public class LocationGenerator {

    private EnumeratedDistribution<Location> locations;

    public LocationGenerator(org.apache.commons.math3.random.RandomGenerator rng, List<Poi> pois, Pair<Integer, Double>... types) {
        List<Pair<Location, Double>> pmf = new ArrayList<>();
        for (Poi poi : pois) {
            for (Pair<Integer, Double> type : types) {
                if (poi.getType() == type.getKey()) {
                    double value = type.getValue();
                    Location location = new Location(rng, poi.getLoc(), value, value * value);
                    pmf.add(new Pair<Location, Double>(location, location.getWeight()));
                }
            }
        }
        this.locations = new EnumeratedDistribution<>(rng, pmf);
    }

    public LocationGenerator(org.apache.commons.math3.random.RandomGenerator rng, List<Location> locations) {
        List<Pair<Location, Double>> pmf = new ArrayList<>();
        for (Location location : locations) {
            pmf.add(new Pair<Location, Double>(location, location.getWeight()));
        }
        this.locations = new EnumeratedDistribution<>(rng, pmf);
    }

    public Point sample() {
        return locations.sample().getPoint();
    }

    public List<Point> sample(int size) {
        List<Point> points = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            points.add(sample());
        }

        return points;
    }

}
