package com.example.demo.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import lombok.Data;

import static java.lang.Math.abs;
@Slf4j
@Component
@Data
public class Model {

    public float Focus;            // Фокусное расстояние линзы
    public float ObjectDistance;   // Расстояние от предмета до линзы
    public float ImageDistance;    // Расстояние от линзы до изображения
    public float ObjectHeight;     // Высота предмета
    public float ImageHeight;      // Высота изображения
    public float IncreaseLens;     // Увеличение линзы
    public boolean IsValid;        // Булева переменная которая нам говорит: "изображение существует либо нет"
    public String ImageType;       // Хранение типа изображения предмета

    // Начало функций проверки
    public void isValidEnter(){          // Проверка на правильность ввода
        IsValid = Focus > 0 & ObjectDistance != 0 & Focus != ObjectDistance;
    }

    public void checkImageType() {
        if (IsValid) {
            // Главное исправление: тип зависит от дистанции до изображения
            if (ImageDistance > 0) {
                ImageType = "Действительное, перевёрнутое, ";
            } else {
                ImageType = "Мнимое, прямое, ";
            }

            if (IncreaseLens < 1) {
                ImageType += "уменьшенное";
            } else if (IncreaseLens > 1) {
                ImageType += "увеличенное";
            } else {
                ImageType += "в натуральную величину";
            }
        } else {
            if (ObjectDistance == Focus) {
                ImageType = "Изображение в бесконечности";
            } else {
                ImageType = "Нет изображения (ошибка ввода)";
            }
        }
    }
    // Конец функций проверки

    // Начало функций вычислений
    public void calculateImageDistance(){  // Вычисляем расстояние от линзы до изображения из введенных данных
        this.ImageDistance = (this.ObjectDistance * this.Focus) / (this.ObjectDistance - this.Focus);
    }

    public void calculateImageHeight(){    // Вычисляем высоту изображения из введенных данных
        this.ImageHeight = abs(this.ImageDistance / this.ObjectDistance) * this.ObjectHeight;
    }

    public void calculateIncrease() {      // Вычисляем увеличение линзы из введенных данных
        this.IncreaseLens = abs(this.ImageDistance / this.ObjectDistance);
    }
    // Конец функций вычислений

    public static void main(String[] args) {
        log.info("start");
        Model model = new Model();
    }
}
