package com.fitmind.ai.ml;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SimpleLinearRegression Tests")
class SimpleLinearRegressionTest {

    @Test
    @DisplayName("Should calculate correct slope and intercept for perfect linear data")
    void fit_ShouldCalculateCorrectCoefficients_ForPerfectLinearData() {
        // y = 2x + 1
        double[] x = {1, 2, 3, 4, 5};
        double[] y = {3, 5, 7, 9, 11};

        SimpleLinearRegression model = new SimpleLinearRegression(x, y);

        assertThat(model.getSlope()).isCloseTo(2.0, within(0.001));
        assertThat(model.getIntercept()).isCloseTo(1.0, within(0.001));
    }

    @Test
    @DisplayName("Should predict correctly for given x")
    void predict_ShouldReturnCorrectValue() {
        double[] x = {0, 1, 2, 3, 4};
        double[] y = {1, 3, 5, 7, 9};  // y = 2x + 1

        SimpleLinearRegression model = new SimpleLinearRegression(x, y);

        assertThat(model.predict(5)).isCloseTo(11.0, within(0.01));
        assertThat(model.predict(10)).isCloseTo(21.0, within(0.01));
    }

    @Test
    @DisplayName("R-squared should be 1.0 for perfect linear data")
    void rSquared_ShouldBeOne_ForPerfectData() {
        double[] x = {1, 2, 3, 4, 5};
        double[] y = {2, 4, 6, 8, 10};

        SimpleLinearRegression model = new SimpleLinearRegression(x, y);

        assertThat(model.getRSquared()).isCloseTo(1.0, within(0.001));
    }

    @Test
    @DisplayName("R-squared should be lower for noisy data")
    void rSquared_ShouldBeLower_ForNoisyData() {
        double[] x = {1, 2, 3, 4, 5};
        double[] y = {2.1, 3.8, 6.2, 7.9, 11.1}; // noisy

        SimpleLinearRegression model = new SimpleLinearRegression(x, y);

        assertThat(model.getRSquared()).isBetween(0.95, 1.0);
    }

    @Test
    @DisplayName("Should handle flat data (zero slope)")
    void fit_ShouldHandleFlatData() {
        double[] x = {1, 2, 3, 4, 5};
        double[] y = {5, 5, 5, 5, 5};

        SimpleLinearRegression model = new SimpleLinearRegression(x, y);

        assertThat(model.getSlope()).isCloseTo(0.0, within(0.001));
        assertThat(model.getIntercept()).isCloseTo(5.0, within(0.001));
        assertThat(model.predict(10)).isCloseTo(5.0, within(0.001));
    }

    @Test
    @DisplayName("Should model weight gain trend correctly")
    void fit_ShouldModelWeightGainTrend() {
        // Simulate 10 days of weight data: gradual gain ~0.1kg/day
        double[] days = {0, 3, 7, 10, 14, 17, 21, 25, 28, 30};
        double[] weights = {75.0, 75.3, 75.8, 76.1, 76.5, 76.8, 77.2, 77.6, 77.9, 78.1};

        SimpleLinearRegression model = new SimpleLinearRegression(days, weights);

        // Slope should be approximately 0.1 kg/day
        assertThat(model.getSlope()).isBetween(0.08, 0.12);
        // Predict day 60 (30 days into future)
        double predicted60 = model.predict(60);
        assertThat(predicted60).isGreaterThan(78.0);
    }
}
