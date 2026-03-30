package com.example.demo.web.ui;

import com.example.demo.model.Model;
import com.example.demo.service.ErrorCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
public class SimulationController {

    @Autowired
    private ErrorCalculator errorCalculator;

    @GetMapping("/")
    public String index(ModelMap modelMap) {
        Model lensModel = new Model();
        lensModel.setFocus(10);
        lensModel.setObjectDistance(15);
        lensModel.setObjectHeight(5);
        lensModel.setLensType("converging");
        performCalculations(lensModel);

        modelMap.addAttribute("lensModel", lensModel);
        modelMap.addAttribute("measurements", errorCalculator.getMeasurements());
        modelMap.addAttribute("errorStats", errorCalculator.getErrorStats());

        return "index";
    }

    @PostMapping("/api/update")
    @ResponseBody
    public Model updateApi(
            @RequestParam float Focus,
            @RequestParam float ObjectDistance,
            @RequestParam float ObjectHeight,
            @RequestParam(defaultValue = "converging") String LensType) {

        Model lensModel = new Model();
        lensModel.setFocus(Focus);
        lensModel.setObjectDistance(ObjectDistance);
        lensModel.setObjectHeight(ObjectHeight);
        lensModel.setLensType(LensType);
        performCalculations(lensModel);
        return lensModel;
    }

    @PostMapping("/api/measurement/add")
    @ResponseBody
    public ErrorCalculator.ErrorStats addMeasurement(
            @RequestParam float Focus,
            @RequestParam float ObjectDistance,
            @RequestParam float ObjectHeight) {

        errorCalculator.addMeasurement(Focus, ObjectDistance, ObjectHeight);
        return errorCalculator.getErrorStats();
    }

    @PostMapping("/api/measurement/reset")
    @ResponseBody
    public ErrorCalculator.ErrorStats resetMeasurements() {
        errorCalculator.resetMeasurements();
        return errorCalculator.getErrorStats();
    }

    @GetMapping("/api/measurements")
    @ResponseBody
    public ErrorCalculator.ErrorStats getMeasurements() {
        return errorCalculator.getErrorStats();
    }

    private void performCalculations(Model lensModel) {
        lensModel.isValidEnter();
        if (lensModel.IsValid) {
            lensModel.calculateImageDistance();
            lensModel.calculateIncrease();
            lensModel.calculateImageHeight();
            lensModel.checkImageType();
            log.info("Расчет выполнен: type={}, f={}, d={}, h={}, image_d={}, image_h={}",
                    lensModel.getLensType(), lensModel.getFocus(), lensModel.getObjectDistance(),
                    lensModel.getObjectHeight(), lensModel.getImageDistance(),
                    lensModel.getImageHeight());
        } else {
            lensModel.checkImageType();
            log.warn("Изображение не существует для: type={}, focus={}, distance={}",
                    lensModel.getLensType(), lensModel.getFocus(), lensModel.getObjectDistance());
        }
    }
}