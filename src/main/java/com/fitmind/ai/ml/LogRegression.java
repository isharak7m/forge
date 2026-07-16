package com.fitmind.ai.ml;

public class LogRegression {
    private final double a;
    private final double b;
    private final double rSquared;
    private final boolean valid;

    public LogRegression(double[] x, double[] y) {
        if (x.length != y.length || x.length < 2) {
            valid = false;
            a = 0;
            b = 0;
            rSquared = 0;
            return;
        }

        int n = x.length;
        double[] lx = new double[n];
        for (int i = 0; i < n; i++) {
            lx[i] = Math.log(Math.max(x[i], 0) + 1);
        }

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += lx[i];
            sumY += y[i];
            sumXY += lx[i] * y[i];
            sumX2 += lx[i] * lx[i];
            sumY2 += y[i] * y[i];
        }

        double denom = (n * sumX2) - (sumX * sumX);
        if (Math.abs(denom) < 1e-10) {
            valid = false;
            a = 0;
            b = sumY / n;
            rSquared = 0;
            return;
        }

        a = ((n * sumXY) - (sumX * sumY)) / denom;
        b = (sumY - (a * sumX)) / n;

        double meanY = sumY / n;
        double ssTot = 0, ssRes = 0;
        for (int i = 0; i < n; i++) {
            double pred = a * lx[i] + b;
            ssTot += Math.pow(y[i] - meanY, 2);
            ssRes += Math.pow(y[i] - pred, 2);
        }
        rSquared = ssTot == 0 ? 1.0 : 1.0 - (ssRes / ssTot);
        valid = true;
    }

    public double predict(double daysFromStart) {
        return a * Math.log(Math.max(daysFromStart, 0) + 1) + b;
    }

    public double getA() { return a; }
    public double getB() { return b; }
    public double getRSquared() { return rSquared; }
    public boolean isValid() { return valid; }
}
