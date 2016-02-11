package com.differentialeq.directionfield;
import com.differentialeq.directionfield.MatrixMath.Complex;
import com.differentialeq.directionfield.MatrixMath.MatrixMath;
import com.differentialeq.directionfield.MatrixMath.TwoDimMatrix;

import java.util.ArrayList;
/**
 * (c)2015 DifferentialEq.com
 * Eli Selkin, Chang Zhang, Ahmed Ali, Ben Chen
 */


public class Solution implements Runnable {
    protected float[] x;
    protected Complex[] Rs;
    protected TwoDimMatrix[] EigVecs;
    protected ArrayList<float[]> points;
    protected ArrayList<Float> derivativeValues;
    protected float Cs[];
    protected boolean drawn;

    /**
     * Construct solution from point
     * @param x1
     * @param x2
     */
    public Solution(float x1, float x2)
    {
        points = new ArrayList<>();
        derivativeValues = new ArrayList<>();
        Cs = new float[]{1,1};
        x = new float[2];
        x[0]= x1;
        x[1]= x2;
    }

    public float[] getX(){
        return this.x;
    }

    /**
     * Use GJ elimination from MatrixMath package to compute Cs from IVPs
     */
    public void determineC(){
        TwoDimMatrix getC = new TwoDimMatrix();
        getC.setValue(1,1, EigVecs[0].getValue(1, 1));
        getC.setValue(1,2, EigVecs[1].getValue(1, 1));
        getC.setValue(2,1, EigVecs[0].getValue(2, 1));
        getC.setValue(2,2, EigVecs[1].getValue(2, 1));
        getC.setValue(1,3, new Complex(x[0]));
        getC.setValue(2,3, new Complex(x[1]));
        MatrixMath.Gauss_Elim(getC);
        Cs[0] = (float)getC.getValue(1,3).getrValue();
        Cs[1] = (float)getC.getValue(2,3).getrValue();
    }

    /**
     * mutator
     * @param r
     */
    public void setRs(Complex[] r){
        this.Rs = r;
    }

    /**
     * mutator
     * @param EigenVecs
     */
    public void setEigenvectors(TwoDimMatrix[] EigenVecs){
        this.EigVecs = EigenVecs;
    }

    /**
     * Accessor
     * @return
     */
    public ArrayList<float[]> getPoints(){
        return points;
    }

    /**
     * Accessor
     * @return
     */
    public ArrayList<Float> getDerivativeValues(){
        return derivativeValues;
    }

    /**
     * Calculate for a given t value the x1 and x2 component of that curve
     * @param t
     * @return
     */
    public float[] getXY(float t)
    {
        float[] xy = new float[2];
        xy[0] = (float)((Cs[0]*EigVecs[0].getValue(1,1).getrValue()*Math.pow(Math.E,Rs[0].getrValue()*t))+(Cs[1]*EigVecs[1].getValue(1,1).getrValue()*Math.pow(Math.E,Rs[1].getrValue()*t)));
        xy[1] = (float)((Cs[0]*EigVecs[0].getValue(2,1).getrValue()*Math.pow(Math.E,Rs[0].getrValue()*t))+(Cs[1]*EigVecs[1].getValue(2,1).getrValue()*Math.pow(Math.E,Rs[1].getrValue()*t)));
        return xy;
    }

    /**
     * Calculate the tangent at a particular t value
     * @param t float, time
     * @return
     */
    public float getDerivativeAtT(float t){
        float[] xy = new float[2];
        double r1 = Rs[0].getrValue();
        double r2 = Rs[1].getrValue();
        double c1 = Cs[0];
        double c2 = Cs[1];
        xy[0] = (float)((r1*c1*EigVecs[0].getValue(1,1).getrValue()*Math.pow(Math.E,r1*t))+(r2*c2*EigVecs[1].getValue(1,1).getrValue()*Math.pow(Math.E,r2*t)));
        xy[1] = (float)((r1*c1*EigVecs[0].getValue(2,1).getrValue()*Math.pow(Math.E,r1*t))+(r2*c2*EigVecs[1].getValue(2,1).getrValue()*Math.pow(Math.E,r2*t)));
        if (xy[0]!= 0)
            return xy[1]/xy[0];
        else
            return Float.POSITIVE_INFINITY;
    }

    /**
     * Repeat getXY for a string of points following time t
     */
    public void generatePoints() {
        points.clear();
        for (float i = -4f; i < 4f; i += .1) {
            points.add(this.getXY(i));
        }
    }

    /**
     * Repeate getDerivative for points along t for curve
     */
    public void genDerivativeForPoints() {
        derivativeValues.clear();
        for (float i = -4f; i < 4f; i += .1) {
            derivativeValues.add(this.getDerivativeAtT(i)); // now we will have the slopes at each of the generated points. The question is whether we should go from -2 to 2
        }
    }
    public boolean isDrawn() {
        return drawn;
    }
    public void setDrawn(boolean status){
        drawn = status;
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
