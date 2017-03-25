package org.roi.itlab.cassandra;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public class PoiTest {

    private static final String testPois = "./src/test/resources/org/roi/payg/saint-petersburg_russia.csv";

    @Test
    public void testPoiLoad() throws IOException {

        List<Poi> pois = PoiLoader.loadFromCsv(testPois);

        Assert.assertEquals(pois.size(), 39327);

        Poi poi = pois.get(0);
        Assert.assertEquals(poi.getType(), 161);
        Assert.assertEquals(poi.getName(), "Биржевая площадь");
        Assert.assertEquals(poi.getLoc().getLatitude(), 59.9440931, 0.0001);
        Assert.assertEquals(poi.getLoc().getLongitude(), 30.3055493, 0.0001);
    }
}