package com.example.demo.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    private Model model;
    @BeforeEach
    void setUp() {
        model = new Model();
        model.setFocus(100f);
        model.setObjectHeight(50f);
    }

    @Test
    void calculateImageDistance() {

        model.setObjectDistance(150f);
        model.calculateImageDistance();
        assertEquals(300f, model.getImageDistance(), 0.01);

        model.setObjectDistance(50f);
        model.calculateImageDistance();
        assertEquals(-100f, model.getImageDistance(), 0.01);
    }

    @Test
    void calculateIncrease() {
        model.setObjectDistance(150f);
        model.calculateImageDistance();
        model.calculateIncrease();

        assertEquals(2.0f, model.getIncreaseLens(), 0.01);
    }

}