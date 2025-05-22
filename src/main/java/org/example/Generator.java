package org.example;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Generator {
    public static void main(String[] args) {
        int numberOfPoints = 10000000; // Укажите нужное количество точек
        String filename = "points.txt"; // Имя файла для сохранения

        generateAndSavePoints(numberOfPoints, filename);
        System.out.println("Точки успешно сохранены в файл: " + filename);
    }

    public static void generateAndSavePoints(int numberOfPoints, String filename) {
        Random random = new Random();

        try (FileWriter writer = new FileWriter(filename)) {
            for (int i = 0; i < numberOfPoints; i++) {
                double x = random.nextDouble() * 100;
                double y = random.nextDouble() * 100;

                writer.write(x + " " + y + "\n");
            }
        } catch (IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }
}
