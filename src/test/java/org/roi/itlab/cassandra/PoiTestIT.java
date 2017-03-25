package org.roi.itlab.cassandra;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.graphhopper.util.DistanceCalcEarth;
import com.mongodb.AggregationOptions;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.MongoClient;

public class PoiTestIT {

	private static final String testPois = "./src/test/resources/org/roi/payg/saint-petersburg_russia.csv";

	Morphia morphia = new Morphia();
	Datastore datastore;

	@Before
	public void init() {
		// tell Morphia where to find your classes
		// can be called multiple times with different packages or classes
		morphia.mapPackage("org.roi.payg");

		// create the Datastore connecting to the default port on the local host
		datastore = morphia.createDatastore(new MongoClient(), "payg_test");
		datastore.ensureIndexes();
		datastore.delete(datastore.createQuery(Poi.class));
	}

//	@Test
	public void testPoiLoad() throws IOException {
    	long countAll = datastore.createQuery(Poi.class).countAll();
    	Assert.assertEquals(countAll, 0);

		final Function<String, Poi> mapToPoi = (line) -> {
			String[] p = line.split("\\|");
			return new Poi(p[1], p[4], Integer.parseInt(p[0]), Double.parseDouble(p[2]), Double.parseDouble(p[3]));
		};

		List<Poi> pois = Files.lines(Paths.get(testPois)).map(mapToPoi)
				.collect(Collectors.toList());

		Assert.assertEquals(pois.size(), 39327);
		
		datastore.save(pois);

    	countAll = datastore.createQuery(Poi.class).countAll();
    	Assert.assertEquals(countAll, 39327);
    	
		Poi poidb = datastore.get(Poi.class, "W363084481");
		Assert.assertNotNull(poidb);
		Assert.assertEquals(poidb.getName(), "Spar");
		
    	countAll = datastore.createQuery(Poi.class).field("type").equal(21).countAll();
    	Assert.assertEquals(countAll, 939);
    	
		Poi school = datastore.get(Poi.class, "W26980665");
    	
    	double dist = new DistanceCalcEarth().calcDist(poidb.getLoc().getLatitude(), poidb.getLoc().getLongitude(), school.getLoc().getLatitude(), school.getLoc().getLongitude());
    	Assert.assertEquals(dist, 11985, 1);
	}
}
