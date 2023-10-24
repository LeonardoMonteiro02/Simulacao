package io.sim;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class CSV {

    public static final String CSV_FILE_PATH = "C:/Users/Technolog/Desktop/Aula/sim/arquivo_"
            + System.currentTimeMillis() + ".csv";

    public void appendDataToCSV(List<DrivingData> drivingReport) {
        try {
            PrintWriter csvWriter = new PrintWriter(new FileWriter(CSV_FILE_PATH)); // Cria um novo arquivo a cada vez

            csvWriter.println(
                    "Timestamp, ID car, ID, Route, Speed, Distance, FuelConsumption, FuelType, CO2Emission, Longitude, Latitude");

            for (DrivingData data : drivingReport) {
                csvWriter.print(data.getTimeStamp());
                csvWriter.print(",");
                csvWriter.print(data.getAutoID());
                csvWriter.print(",");
                csvWriter.print(data.getDriverID());
                csvWriter.print(",");
                csvWriter.print(data.getRoadIDSUMO());
                csvWriter.print(",");
                csvWriter.print(data.getSpeed());
                csvWriter.print(",");
                csvWriter.print(data.getOdometer());
                csvWriter.print(",");
                csvWriter.print(data.getFuelConsumption());
                csvWriter.print(",");
                csvWriter.print(data.getFuelType());
                csvWriter.print(",");
                csvWriter.print(data.getCo2Emission());
                csvWriter.print(",");
                csvWriter.print(data.getX_Position()); // Longitude
                csvWriter.print(",");
                csvWriter.print(data.getY_Position()); // Latitude
                csvWriter.println();
            }

            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
