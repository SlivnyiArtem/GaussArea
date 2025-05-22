package org.example;

import jromp.JROMP;
import jromp.task.ForTask;
import jromp.var.AtomicVariable;
import jromp.var.SharedVariable;
import jromp.operation.Operations;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static jromp.JROMP.getThreadNum;

public class GaussAreaCalculation {
    //Добавил только для удобного логгирования
    private static final ConcurrentHashMap<Long, StringBuilder> loggBuffs = new ConcurrentHashMap<>();

    public static SharedVariable<ArrayList<Point2D>> Points;
    public static AtomicVariable<Double> FloatSum;

    private static class CalcArea implements ForTask {
        private final Integer FullLength;
        public CalcArea(int fl){
            this.FullLength = fl;
        }

        @Override
        public void run(int i, int i1) {
            var threadId = Thread.currentThread().threadId();
            double localSum=0;

            StringBuilder localBuffer = loggBuffs.computeIfAbsent(threadId, _ -> new StringBuilder());

            for (var j=i; j<i1; j++){
                var curPoint = Points.value().get(j);
                var nextPoint = Points.value().get((j+1)%this.FullLength);
                var area = calculateGaussArea(curPoint, nextPoint);
                localSum += area.doubleValue();

                //Только для логгирования
                var resString = String.format("Compute matrix determinant of {%f, %f} and {%f, %f} points in %d thread. Res=%f\n",
                        curPoint.getX(), curPoint.getY(), nextPoint.getX(), nextPoint.getY(),getThreadNum(), area);


                localBuffer.append(resString);

            }
            FloatSum.update(Operations.add(localSum));
        }
    }
    private static BigDecimal calculateGaussArea(Point2D cur, Point2D next) {
        var curX = BigDecimal.valueOf(cur.getX());
        var curY = BigDecimal.valueOf(cur.getY());
        var nextX = BigDecimal.valueOf(next.getX());
        var nextY = BigDecimal.valueOf(next.getY());
        BigDecimal gArea = curX.multiply(nextY).subtract(nextX.multiply(curY)).abs();
        return gArea.setScale(4, RoundingMode.HALF_UP);
    }


    public GaussAreaCalculation(){
        try {
            var reader = new BufferedReader(new FileReader("points.txt"));
            var points = new ArrayList<Point2D>();

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                String[] parts = line.split("\\s+");
                points.add(new Point2D.Float(Float.parseFloat(parts[0]), Float.parseFloat(parts[1])));
            }
            Points = new SharedVariable<>(points);
        }
        catch (IOException exc){
            System.out.println(exc);
        }
        FloatSum = new AtomicVariable<>(0.0);
    }

    public void calculate() {
        int fullLength = Points.value().size();
        var numThreads = Integer.parseInt(System.getenv("JROMP_NUM_THREADS"));
        long startTime = System.currentTimeMillis();
        JROMP.withThreads(numThreads).parallelFor(0, fullLength, new CalcArea(fullLength)).join();

        long endTime = System.currentTimeMillis();
        System.out.println(" ");
        System.out.println(endTime - startTime);
        var res = String.valueOf(FloatSum.value() /2);
        System.out.println(res);
        //Только для логгирования
        try (var fw = new FileWriter("res.txt", false)){
            for (StringBuilder buffer : loggBuffs.values())
                fw.write(buffer.toString());
            fw.write(res);
        }
        catch (IOException exc){
            System.out.println(exc);
        }
    }
}
