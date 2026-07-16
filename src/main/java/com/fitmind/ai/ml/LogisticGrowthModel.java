package com.fitmind.ai.ml;

import jnumpy.core.Arrays;
import jnumpy.ndarray.NDArray;
import static jnumpy.core.Arrays.*;

public class LogisticGrowthModel {

    private double L;
    private double k;
    private double t0;
    private double rSquared;
    private final boolean valid;

    public LogisticGrowthModel(double[] x, double[] y) {
        if (x.length != y.length || x.length < 3) {
            valid = false;
            L = 0; k = 0; t0 = 0; rSquared = 0;
            return;
        }

        double maxY = 0;
        double sumX = 0;
        for (int i = 0; i < x.length; i++) {
            if (y[i] > maxY) maxY = y[i];
            sumX += x[i];
        }
        L = maxY * 1.3;
        k = 0.3;
        t0 = (sumX / x.length) * 0.5;

        NDArray xArr = array(x);
        NDArray yArr = array(y);

        double lr = 0.005;
        int maxIter = 8000;

        for (int iter = 0; iter < maxIter; iter++) {
            NDArray z = multiply(subtract(xArr, nd(t0)), nd(-k));
            NDArray expZ = exp(z);
            NDArray denom = add(expZ, nd(1.0));
            NDArray yPred = divide(nd(L), denom);
            NDArray diff = subtract(yPred, yArr);

            NDArray dL = divide(diff, denom);
            NDArray xMinusT0 = subtract(xArr, nd(t0));
            NDArray dk = divide(multiply(multiply(diff, nd(L)), multiply(xMinusT0, expZ)), power(denom, nd(2.0)));
            NDArray dt0 = divide(multiply(multiply(multiply(diff, nd(L)), nd(k)), expZ), power(denom, nd(2.0)));

            L -= lr * 2.0 * mean(dL).getDouble(0);
            k -= lr * 2.0 * mean(dk).getDouble(0);
            t0 -= lr * 2.0 * mean(dt0).getDouble(0);

            if (k < 0) k = 0.01;
            if (L < maxY) L = maxY * 1.1;
        }

        NDArray z = multiply(subtract(xArr, nd(t0)), nd(-k));
        NDArray yPred = divide(nd(L), add(exp(z), nd(1.0)));
        double meanY = mean(yArr).getDouble(0);
        NDArray resid = subtract(yArr, yPred);
        NDArray yDiff = subtract(yArr, nd(meanY));
        double ssRes = sum(power(resid, nd(2.0))).getDouble(0);
        double ssTot = sum(power(yDiff, nd(2.0))).getDouble(0);
        rSquared = ssTot == 0 ? 1.0 : 1.0 - (ssRes / ssTot);
        valid = true;
    }

    public double predict(double t) {
        return L / (1.0 + Math.exp(-k * (t - t0)));
    }

    public double getL() { return L; }
    public double getK() { return k; }
    public double getT0() { return t0; }
    public double getRSquared() { return rSquared; }
    public boolean isValid() { return valid; }

    private static NDArray nd(double v) {
        return array(new double[]{v});
    }
}
