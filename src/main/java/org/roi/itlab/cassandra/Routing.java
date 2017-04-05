package org.roi.itlab.cassandra;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.json.geo.Point;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.VirtualEdgeIteratorState;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Routing {

    private static final String TEST_OSM = "./src/test/resources/org/roi/payg/saint-petersburg_russia.osm.pbf";
    private static final GraphHopper hopper = new GraphHopperOSM().setOSMFile(TEST_OSM).
            forDesktop().
            setEnableInstructions(false).
            setGraphHopperLocation("./target").setInMemory().
            setEncodingManager(new EncodingManager("car,bike")).
            importOrLoad();

    private static final Map<Integer, Edge> EDGES_STORAGE = new HashMap<>();
    private static final DistanceCalcEarth DIST_EARTH = new DistanceCalcEarth();

    private static final long MAX_ROUTE_TIME = 1000L * 60 * 60 * 3;

    //not sure if it should be an utility class
    // or instanced to initialize graphhopper with custom properties
    private Routing() {
        throw new IllegalAccessError("Utility class");
    }

    public static Route route(Poi from, Poi to) {
        return route(from.getLoc().getLatitude(), from.getLoc().getLongitude(), to.getLoc().getLatitude(), to.getLoc().getLongitude());
    }

    public static Route route(Trip trip) {
        return route(trip.getFrom(), trip.getTo());
    }

    public static Route route(org.mongodb.morphia.geo.Point from, org.mongodb.morphia.geo.Point to) {
        return route(from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude());
    }

    public static Route route(double fromLat, double fromLon, double toLat, double toLon) {
        Path path = calcPath(fromLat, fromLon, toLat, toLon);
        NodeAccess nodes = hopper.getGraphHopperStorage().getBaseGraph().getNodeAccess();
        List<Edge> edges = new ArrayList<>();
        for (EdgeIteratorState edgeIteratorState :
                path.calcEdges()) {
            if (!(edgeIteratorState instanceof VirtualEdgeIteratorState)) {
                int id = edgeIteratorState.getEdge();
                if (EDGES_STORAGE.containsKey(id)) {
                    edges.add(EDGES_STORAGE.get(id));
                } else {
                    //getting Edge details
                    int baseNodeId = edgeIteratorState.getBaseNode();
                    int adjNodeId = edgeIteratorState.getAdjNode();
                    double x1 = nodes.getLongitude(adjNodeId);
                    double x2 = nodes.getLongitude(baseNodeId);
                    double x3 = nodes.getLat(adjNodeId);
                    double x4 = nodes.getLat(baseNodeId);
                    double distance = edgeIteratorState.getDistance();
                    FlagEncoder encoder = hopper.getEncodingManager().getEncoder("car");
                    long flags = edgeIteratorState.getFlags();
                    double speed = encoder.getSpeed(flags);
                    boolean oneWay = encoder.isBackward(flags) ^ encoder.isForward(flags);
                    PointList geometry = edgeIteratorState.fetchWayGeometry(3);
                    int time = (int) (distance * 3600 / (speed));
                    Edge tempedge = new Edge(id, new Point(x4, x2), new Point(x3, x1), geometry, distance, time, speed, oneWay);
                    EDGES_STORAGE.put(id, tempedge);
                    edges.add(tempedge);
                }
            }
        }
        return new Route(edges.toArray(new Edge[0]));

    }

    public static Path calcPath(Poi from, Poi to) {
        return calcPath(from.getLoc().getLatitude(), from.getLoc().getLongitude(), to.getLoc().getLatitude(), to.getLoc().getLongitude());
    }

    public static Path calcPath(double fromLat, double fromLon, double toLat, double toLon) {
        GHRequest req = new GHRequest(fromLat, fromLon, toLat, toLon).
                setWeighting("fastest").
                setVehicle("car").
                setLocale(Locale.US);
        GHResponse rsp = new GHResponse();
        List<Path> paths = hopper.calcPaths(req, rsp);
        if (rsp.hasErrors()) {
            throw new IllegalStateException("routing failed");
        }
        if (paths.get(0).getDistance() > 5 * DIST_EARTH.calcDist(fromLat, fromLon, toLat, toLon))
            throw new IllegalStateException("routing failed");
        if (paths.get(0).getTime() > MAX_ROUTE_TIME)
            throw new IllegalStateException("routing failed");
        return paths.get(0);
    }

    public static Edge getEdge(int id) {
        if (EDGES_STORAGE.containsKey(id)) {
            return EDGES_STORAGE.get(id);
        }
        return null;
    }

    public static Map<Integer, Edge> getEdgesStorage() {
        return EDGES_STORAGE;
    }

    public static void saveEdgesStorage(OutputStreamWriter writer) throws IOException {
        for (Edge e :
                EDGES_STORAGE.values()) {
            writer.write(e.write() + '\n');
        }
        writer.close();
    }

    public static void loadEdgesStorage(String filename) throws IOException {
        Files.lines(Paths.get(filename)).forEach(s -> {
            Edge e = new Edge(s);
            EDGES_STORAGE.put(e.id, e);
        });
    }
}
