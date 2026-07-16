package com.fitmind.ai.ml;

import jnumpy.core.Arrays;
import jnumpy.ndarray.NDArray;
import static jnumpy.core.Arrays.*;

public class LinearRegressionModel {

    private double slope;
    private double intercept;
    private double rSquared;
    private final boolean valid;

    public LinearRegressionModel(double[] x, double[] y) {
        if (x.length != y.length || x.length < 2) {
            valid = false;
            slope = 0; intercept = 0; rSquared = 0;
            return;
        }

        int n = x.length;
        double[][] xMat = new double[n][2];
        double[][] yCol = new double[n][1];
        for (int i = 0; i < n; i++) {
            xMat[i][0] = 1.0;
            xMat[i][1] = x[i];
            yCol[i][0] = y[i];
        }

        NDArray X = array(xMat);
        NDArray Y = array(yCol);
        NDArray XT = X.T();
        NDArray XTX = matmul(XT, X);
        NDArray XTy = matmul(XT, Y);
        NDArray theta = matmul(inv(XTX), XTy);

        intercept = theta.getDouble(0, 0);
        slope = theta.getDouble(1, 0);

        NDArray yPred = matmul(X, theta);
        double meanY = mean(Y).getDouble(0);
        NDArray resid = subtract(Y, yPred);
        NDArray yDiff = subtract(Y, nd(meanY));
        double ssRes = sum(power(resid, nd(2.0))).getDouble(0);
        double ssTot = sum(power(yDiff, nd(2.0))).getDouble(0);
        rSquared = ssTot == 0 ? 1.0 : 1.0 - (ssRes / ssTot);
        valid = true;
    }

    public double predict(double x) {
        return slope * x + intercept;
    }

    public double getSlope() { return slope; }
    public double getIntercept() { return intercept; }
    public double getRSquared() { return rSquared; }
    public boolean isValid() { return valid; }

    private static NDArray nd(double v) {
        return array(new double[]{v});
    }
}
