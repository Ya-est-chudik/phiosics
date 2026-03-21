package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ErrorCalculator {

    private static final double SYSTEMATIC_ERROR = 0.10; // системная погрешность в см

    private List<Measurement> measurements = new ArrayList<>();

    public static class Measurement {
        private double objectDistance;  // d
        private double imageDistance;   // f (расчетное)
        private double calculatedFocus; // F (вычисленное)

        public Measurement(double objectDistance, double imageDistance, double calculatedFocus) {
            this.objectDistance = objectDistance;
            this.imageDistance = imageDistance;
            this.calculatedFocus = calculatedFocus;
        }

        public double getObjectDistance() { return objectDistance; }
        public double getImageDistance() { return imageDistance; }
        public double getCalculatedFocus() { return calculatedFocus; }
    }

    // Добавление измерения с учетом случайной погрешности
    public Measurement addMeasurement(double focusSet, double objectDistance) {
        if (Math.abs(objectDistance - focusSet) < 0.1) {
            log.warn("Попытка добавить измерение с d = F");
            return null;
        }

        // Идеальное фокусное расстояние
        double idealImageDistance = (objectDistance * focusSet) / (objectDistance - focusSet);

        // Добавляем случайную погрешность (±0.15 см)
        double noise = (Math.random() - 0.5) * 0.15;
        double measuredObjectDistance = objectDistance + noise;
        double measuredImageDistance = idealImageDistance + noise;

        // Вычисляем фокусное расстояние по измеренным данным
        double calculatedFocus = (measuredObjectDistance * measuredImageDistance) /
                (measuredObjectDistance + measuredImageDistance);

        Measurement measurement = new Measurement(measuredObjectDistance, measuredImageDistance, calculatedFocus);
        measurements.add(measurement);

        log.info("Добавлено измерение: d={}, f={}, F={}",
                measuredObjectDistance, measuredImageDistance, calculatedFocus);

        return measurement;
    }

    // Сброс всех измерений
    public void resetMeasurements() {
        measurements.clear();
        log.info("Сброшены все измерения");
    }

    // Получение списка измерений
    public List<Measurement> getMeasurements() {
        return new ArrayList<>(measurements);
    }

    // Расчет случайной погрешности
    public double calculateRandomError() {
        if (measurements.size() <= 1) {
            return 0.0;
        }

        // Вычисляем среднее значение F
        double avgF = measurements.stream()
                .mapToDouble(Measurement::getCalculatedFocus)
                .average()
                .orElse(0.0);

        // Вычисляем дисперсию
        double variance = measurements.stream()
                .mapToDouble(m -> Math.pow(m.getCalculatedFocus() - avgF, 2))
                .sum() / (measurements.size() * (measurements.size() - 1));

        // Случайная погрешность (коэффициент Стьюдента для 95% доверительного интервала ~2.5)
        return Math.sqrt(variance) * 2.5;
    }

    // Получение системной погрешности
    public double getSystematicError() {
        return SYSTEMATIC_ERROR;
    }

    // Расчет общей погрешности
    public double calculateTotalError() {
        double randomError = calculateRandomError();
        return Math.sqrt(Math.pow(SYSTEMATIC_ERROR, 2) + Math.pow(randomError, 2));
    }

    // Получение статистики для отображения
    public ErrorStats getErrorStats() {
        double randomError = calculateRandomError();
        double totalError = Math.sqrt(Math.pow(SYSTEMATIC_ERROR, 2) + Math.pow(randomError, 2));

        return new ErrorStats(randomError, SYSTEMATIC_ERROR, totalError, measurements.size());
    }

    public static class ErrorStats {
        private final double randomError;
        private final double systematicError;
        private final double totalError;
        private final int measurementsCount;

        public ErrorStats(double randomError, double systematicError, double totalError, int measurementsCount) {
            this.randomError = randomError;
            this.systematicError = systematicError;
            this.totalError = totalError;
            this.measurementsCount = measurementsCount;
        }

        public double getRandomError() { return randomError; }
        public double getSystematicError() { return systematicError; }
        public double getTotalError() { return totalError; }
        public int getMeasurementsCount() { return measurementsCount; }
    }
}