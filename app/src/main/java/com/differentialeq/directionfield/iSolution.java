package com.differentialeq.directionfield;

import android.util.Log;

import com.differentialeq.directionfield.MatrixMath.Complex;
import com.differentialeq.directionfield.MatrixMath.MathZero;
import com.differentialeq.directionfield.MatrixMath.MatrixMath;
import com.differentialeq.directionfield.MatrixMath.TwoDimMatrix;

/**
 * (c)2015 DifferentialEq.com
 * Eli Selkin, Chang Zhang, Ahmed Ali, Ben Chen
 */

public class iSolution extends Solution implements Runnable {
    private float lambda;
    private float mu;
    private float[] As;
    private float[] Bs;

    public iSolution(float x1, float x2)
    {
        super(x1,x2);
    }
    public void addAs(float[] a){
        As = a;
    }
    public void addBs(float[] b){
        Bs = b;
    }
    public void addMus(float mu){
        this.mu = mu;
    }
    public void addLambdas(float lambda){
        this.lambda = lambda;
    }

    /**
     * Determine C1 C2 from GJ elimination of solution at time t=0 with given x1,x2 (solution of IVP)
     */
    public void determineC(){
        TwoDimMatrix result = solveForT(0);
        result.setValue(1,3, new Complex(x[0]));
        result.setValue(2,3, new Complex(x[1]));
        MatrixMath.Gauss_Elim(result);
        Log.i("ZZZ", result.toString());
        Cs[0] = (float)result.getValue(1,3).getrValue();
        Cs[1] = (float)result.getValue(2,3).getrValue();
    }

    /**
     *
     * @param t
     * @return
     */
    public float[] getXY(float t){
        TwoDimMatrix result = solveForT(t);
        float powerE = (float)Math.pow(Math.E,lambda*t);
        float x1 = (float)(powerE*(result.getValue(1,1).getrValue() + result.getValue(1,2).getrValue()));
        float x2 = (float)(powerE*(result.getValue(2,1).getrValue() + result.getValue(2,2).getrValue()));
        return new float[]{x1,x2};
    }

    public float getDerivativeAtT(float t) {
        TwoDimMatrix resultDeriv = solveDerivForT(t);
        TwoDimMatrix result = solveForT(t);

        float powerE = (float) Math.pow(Math.E, lambda * t);
        float powerEDeriv = lambda * powerE;
        float x1 = (float)(powerE * resultDeriv.getValue(1, 1).getrValue() + powerEDeriv * result.getValue(1,1).getrValue() + powerE * resultDeriv.getValue(1, 2).getrValue() + powerEDeriv * result.getValue(1, 2).getrValue());
        float x2 = (float)(powerE * resultDeriv.getValue(2, 1).getrValue() + powerEDeriv * result.getValue(2,1).getrValue() + powerE * resultDeriv.getValue(2, 2).getrValue() + powerEDeriv * result.getValue(2, 2).getrValue());
        if (x1!= 0)
            return x2/x1;
        else
            return Float.POSITIVE_INFINITY;
    }

    /**
     * Here the return is the matrix of derivatives found in solveForT
     * @param t float, parameter to solve for x1,x2
     * @return
     */
    public TwoDimMatrix solveDerivForT(float t) {
        float muT = mu * t;
        TwoDimMatrix xMatrix = new TwoDimMatrix();
        xMatrix.setValue(1, 1, new Complex(-1 * Cs[0] * (As[0] * mu * Math.sin(muT) + Bs[0] * mu * Math.cos(muT))));
        xMatrix.setValue(1, 2, new Complex(Cs[1] * (As[0] * mu * Math.cos(muT) - Bs[0] * mu * Math.sin(muT))));
        xMatrix.setValue(2, 1, new Complex(-1 * Cs[0] * (As[1] * mu * Math.sin(muT) + Bs[1] * mu * Math.cos(muT))));
        xMatrix.setValue(2, 2, new Complex(Cs[1] * (As[1] * mu * Math.cos(muT) - Bs[1] * mu * Math.sin(muT))));

        return xMatrix;
    }

    /**
     * Here the return is the matrix of the function returning x1, x2
     * @param t
     * @return
     */
    public TwoDimMatrix solveForT(float t) {
        float muT = mu * t;
        TwoDimMatrix xMatrix = new TwoDimMatrix();
        xMatrix.setValue(1, 1, new Complex(Cs[0] * (As[0] * Math.cos(muT) - Bs[0] * Math.sin(muT))));
        xMatrix.setValue(1, 2, new Complex(Cs[1] * (As[0] * Math.sin(muT) + Bs[0] * Math.cos(muT))));
        xMatrix.setValue(2, 1, new Complex(Cs[0] * (As[1] * Math.cos(muT) - Bs[1] * Math.sin(muT))));
        xMatrix.setValue(2, 2, new Complex(Cs[1] * (As[1] * Math.sin(muT) + Bs[1] * Math.cos(muT))));

        // HERE we should do a solution to find x1 and x2 or u(t) and v(t) and return as float
        return xMatrix;
    }

    public void generatePoints() {
        this.points.clear();
        for (float i = -5.0f; i < 5.0f; i += .1) {
            points.add(this.getXY(i));
        }
    }

    public void genDerivativeForPoints() {
        this.derivativeValues.clear();
        for (float i = -5.0f; i < 5.0f; i += .1) {
            derivativeValues.add(this.getDerivativeAtT(i)); // now we will have the slopes at each of the generated points. The question is whether we should go from -2 to 2
        }
    }

    @Override
    public void run() {
        synchronized (points) {
            synchronized (derivativeValues) {
                this.generatePoints();
                this.genDerivativeForPoints();
            }
        }
    }
}
