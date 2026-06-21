package com.fitmind.ai.ml;

public class SimpleLinearRegression {
    private final double slope;
    private final double intercept;
    private final double rSquared;

    public SimpleLinearRegression(double[] x, double[] y) {
        if (x.length != y.length || x.length == 0) {
            throw new IllegalArgumentException("Arrays must have same non-zero length");
        }

        int n = x.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
            sumY2 += y[i] * y[i];
        }

        double denominator = (n * sumX2) - (sumX * sumX);
        if (denominator == 0) {
            slope = 0;
            intercept = sumY / n;
        } else {
            slope = ((n * sumXY) - (sumX * sumY)) / denominator;
            intercept = (sumY - (slope * sumX)) / n;
        }

        // Calculate R-squared
        double meanY = sumY / n;
        double ssTot = 0, ssRes = 0;
        for (int i = 0; i < n; i++) {
            double predictedY = (slope * x[i]) + intercept;
            ssTot += Math.pow(y[i] - meanY, 2);
            ssRes += Math.pow(y[i] - predictedY, 2);
        }
        
        rSquared = ssTot == 0 ? 1.0 : 1.0 - (ssRes / ssTot);
    }

    public double predict(double x) {
        return (slope * x) + intercept;
    }

    public double getSlope() { return slope; }
    public double getIntercept() { return intercept; }
    public double getRSquared() { return rSquared; }
}
