package com.example.demo.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ErrorCalculator {

    private static final double SYSTEMATIC_ERROR = 0.05; // системная погрешность в см

    private final List<Measurement> measurements = new ArrayList<>();
    @Getter
    public static class Measurement {
        private final double objectDistance;  // d
        private final double imageDistance;   // f
        private final double calculatedFocus; // F
        private final double imageHeight;     // H (добавлено)

        public Measurement(double objectDistance, double imageDistance, double calculatedFocus, double imageHeight) {
            this.objectDistance = objectDistance;
            this.imageDistance = imageDistance;
            this.calculatedFocus = calculatedFocus;
            this.imageHeight = imageHeight;
        }
    }

    // Добавление измерения с учетом случайной погрешности
    public void addMeasurement(double focusSet, double objectDistance, double objectHeight) {
        if (Math.abs(objectDistance - focusSet) < 0.1) {
            log.warn("Попытка добавить измерение с d = F");
            return;
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

        double measuredImageHeight = objectHeight * (measuredImageDistance / measuredObjectDistance);

        // Передаем H в конструктор
        Measurement measurement = new Measurement(measuredObjectDistance, measuredImageDistance, calculatedFocus, measuredImageHeight);
        measurements.add(measurement);

        log.info("Добавлено измерение: d={}, f={}, F={}, H={}",
                measuredObjectDistance, measuredImageDistance, calculatedFocus, measuredImageHeight);

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

    // Универсальный расчет случайной погрешности
    private double calculateRandomError(java.util.function.ToDoubleFunction<Measurement> mapper) {
        if (measurements.size() <= 1) return 0.0;
        double avg = measurements.stream().mapToDouble(mapper).average().orElse(0.0);
        double variance = measurements.stream()
                .mapToDouble(m -> Math.pow(mapper.applyAsDouble(m) - avg, 2))
                .sum() / (measurements.size() * (measurements.size() - 1));
        return Math.sqrt(variance) * 2.5;
    }

    // Получение статистики для отображения
    public ErrorStats getErrorStats() {
        double randErrF = calculateRandomError(Measurement::getImageDistance);
        double randErrH = calculateRandomError(Measurement::getImageHeight);

        double totalErrF = Math.sqrt(Math.pow(SYSTEMATIC_ERROR, 2) + Math.pow(randErrF, 2));
        double totalErrH = Math.sqrt(Math.pow(SYSTEMATIC_ERROR, 2) + Math.pow(randErrH, 2));

        return new ErrorStats(totalErrF, totalErrH, SYSTEMATIC_ERROR, measurements.size());
    }

    public record ErrorStats(double totalErrorF, double totalErrorH, double systematicError, int measurementsCount) {
            public ErrorStats(double totalErrorF, double totalErrorH, double systematicError, int measurementsCount) {
                this.totalErrorF = (double) Math.round(totalErrorF * 100) / 100;
                this.totalErrorH = (double) Math.round(totalErrorH * 100) / 100;
                this.systematicError = (double) Math.round(systematicError * 100) / 100;
                this.measurementsCount = measurementsCount;
            }
        }
}