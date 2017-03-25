package org.roi.itlab.cassandra;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.mongodb.morphia.geo.GeoJson;
import org.mongodb.morphia.geo.Point;

import java.util.Random;

/**
 * Created by mkuperman on 3/18/2017.
 */
public class Location {

    protected double radius;
    protected Point center;
    protected double weight = 0;
    private RandomGenerator rng;

    public Location(Point center, double radius) {
        this(new MersenneTwister(), center, radius, 0);
    }

    public Location(RandomGenerator rng, Point center, double radius, double weight) {
        this.rng = rng;
        this.center = center;
        this.radius = radius;
        this.weight = weight;
    }

    public double getRadius() {
        return radius;
    }

    public Point getCenter() {
        return center;
    }

    public double getWeight() {
        return weight;
    }

/*
    public Point getPoint() {
        Random r = new Random(System.nanoTime());
        int metersPerLatDeg = 111135;
        int metersPerLngDeg = 55660;

        double distance = r.nextDouble() * radius;
        double angle = r.nextDouble() * 2 * Math.PI;
        double dlat = distance * Math.cos(angle) / metersPerLatDeg;
        double dlng = distance * Math.sin(angle) / metersPerLngDeg;
        double lat = center.getLatitude() + dlat;
        double lng = center.getLongitude() + dlng;

        return GeoJson.point(lat, lng);
    }
*/

    public Point getPoint() {
        // Convert radius from meters to degrees
        double radiusInDegrees = radius / 111135;

        double u = rng.nextDouble();
        double v = rng.nextDouble();
        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        // Adjust the x-coordinate for the shrinking of the east-west distances
        double new_x = x / Math.cos(Math.toRadians(center.getLatitude()));

        double foundLongitude = new_x + center.getLongitude();
        double foundLatitude = y + center.getLatitude();

        return GeoJson.point(foundLatitude, foundLongitude);
    }
}
