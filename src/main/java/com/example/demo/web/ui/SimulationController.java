package com.example.demo.web.ui;

import com.example.demo.model.Model;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.ui.ModelMap;

@Slf4j
@Controller
@NoArgsConstructor
public class SimulationController {

    private  Model lensModel;

    @GetMapping("/")
    public String index(ModelMap modelMap) {
        // Создаем и инициализируем модель через setter'ы
        lensModel.setObjectDistance(0);
        lensModel.setFocus(0);
        lensModel.setObjectHeight(0);

        // Проверяем валидность и выполняем все вычисления через методы модели
        lensModel.isValidEnter();

        if (lensModel.IsValid) {
            lensModel.calculateImageDistance();
            lensModel.calculateIncrease();
            lensModel.calculateImageHeight();
            lensModel.checkImageType();

            // Логируем результаты, полученные через геттеры модели
            log.info("Результаты расчета:");
            log.info("Расстояние до изображения (из модели): {}", lensModel.getImageDistance());
            log.info("Высота изображения (из модели): {}", lensModel.getImageHeight());
            log.info("Увеличение (из модели): {}", lensModel.getIncreaseLens());
            log.info("Тип изображения (из модели): {}", lensModel.ImageType);
        }

        // Передаем модель в представление - все значения будут взяты из модели
        modelMap.addAttribute("lensModel", lensModel);

        return "index";
    }
}