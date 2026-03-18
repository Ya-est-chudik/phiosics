package com.example.demo.web.ui;

import com.example.demo.model.Model;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
public class SimulationController {

    @GetMapping("/")
    public String index(ModelMap modelMap) {
        Model lensModel = new Model();
        lensModel.setFocus(10);
        lensModel.setObjectDistance(15);
        lensModel.setObjectHeight(5);
        performCalculations(lensModel);
        modelMap.addAttribute("lensModel", lensModel);
        return "index";
    }


    @PostMapping("/api/update")
    @ResponseBody
    public Model updateApi(
            @RequestParam float Focus,
            @RequestParam float ObjectDistance,
            @RequestParam float ObjectHeight) {

        Model lensModel = new Model();
        lensModel.setFocus(Focus);
        lensModel.setObjectDistance(ObjectDistance);
        lensModel.setObjectHeight(ObjectHeight);
        performCalculations(lensModel);
        return lensModel;
    }

    private void performCalculations(Model lensModel) {
        lensModel.isValidEnter();
        if (lensModel.IsValid) {
            lensModel.calculateImageDistance();
            lensModel.calculateIncrease();
            lensModel.calculateImageHeight();
            lensModel.checkImageType();
            log.info("Расчет выполнен: f={}, d={}, h={}, image_d={}, image_h={}",
                    lensModel.getFocus(), lensModel.getObjectDistance(),
                    lensModel.getObjectHeight(), lensModel.getImageDistance(),
                    lensModel.getImageHeight());
        } else {
            lensModel.checkImageType();
            log.warn("Изображение не существует для параметров: focus={}, distance={}",
                    lensModel.getFocus(), lensModel.getObjectDistance());
        }
    }
}
