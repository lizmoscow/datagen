package org.roi.itlab.cassandra.random_attributes;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by Vadim on 20.03.2017.
 */

//TODO rename ?
public class CSVReader  {

    private final String filePath = "distribution.csv";

    public void Read() throws FileNotFoundException {
        final String dir = System.getProperty("user.dir");
        System.out.println("current dir = " + dir);

        Scanner scanner = new Scanner(new File(filePath));
        scanner.useDelimiter(",");
        while(scanner.hasNext()){
            System.out.print(scanner.next()+"|");
        }
        scanner.close();
    }
}
