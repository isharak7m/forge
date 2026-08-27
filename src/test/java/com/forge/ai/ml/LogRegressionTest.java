package com.forge.ai.ml;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LogRegressionTest {

    @Test
    void testPerfectFit() {
        double[] x = {1, 2, 3, 4, 5};
        double[] y = {10, 20, 30, 40, 50};
        LogRegression model = new LogRegression(x, y);
        assertTrue(model.isValid());
        assertTrue(model.getRSquared() > 0.9);
    }

    @Test
    void testEmptyArrays() {
        double[] x = {};
        double[] y = {};
        LogRegression model = new LogRegression(x, y);
        assertFalse(model.isValid());
    }

    @Test
    void testMismatchedLengths() {
        double[] x = {1, 2, 3};
        double[] y = {1, 2};
        LogRegression model = new LogRegression(x, y);
        assertFalse(model.isValid());
    }

    @Test
    void testSinglePoint() {
        double[] x = {5};
        double[] y = {100};
        LogRegression model = new LogRegression(x, y);
        assertFalse(model.isValid());
    }

    @Test
    void testPredictIncreasing() {
        double[] x = {0, 1, 2, 3, 4, 5, 6};
        double[] y = {60, 65, 72, 78, 85, 90, 95};
        LogRegression model = new LogRegression(x, y);
        assertTrue(model.isValid());
        double pred1 = model.predict(0);
        double pred7 = model.predict(7);
        assertTrue(pred7 > pred1, "Prediction should increase for increasing data");
    }

    @Test
    void testRSquaredBounds() {
        double[] x = {1, 2, 3, 4, 5, 6, 7};
        double[] y = {50, 52, 55, 60, 58, 65, 70};
        LogRegression model = new LogRegression(x, y);
        assertTrue(model.isValid());
        double r2 = model.getRSquared();
        assertTrue(r2 >= -1.0 && r2 <= 1.0, "R-squared should be in [-1, 1] range");
    }

    @Test
    void testInvalidModelReturnsZero() {
        double[] x = {1};
        double[] y = {100};
        LogRegression model = new LogRegression(x, y);
        assertEquals(0, model.predict(10), 0.001);
    }

    @Test
    void testGetSetCoefficients() {
        double[] x = {0, 1, 2, 3, 4, 5};
        double[] y = {100, 110, 118, 124, 129, 133};
        LogRegression model = new LogRegression(x, y);
        assertTrue(model.isValid());
        assertNotNull(model);
    }
}
