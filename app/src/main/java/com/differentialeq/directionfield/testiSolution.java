package com.differentialeq.directionfield;

import com.differentialeq.directionfield.MatrixMath.Complex;
import com.differentialeq.directionfield.MatrixMath.MatrixMath;
import com.differentialeq.directionfield.MatrixMath.TwoDimMatrix;
import com.differentialeq.directionfield.iSolution;

import java.util.Arrays;


public class testiSolution {
    public static void main(String[] args) {
        TwoDimMatrix EVs = new TwoDimMatrix(new Complex(0),new Complex(0));
        TwoDimMatrix MATRIX10 = new TwoDimMatrix(new Complex(3), new Complex(-9), new Complex(4), new Complex(-3), new Complex(0), new Complex(0));
        TwoDimMatrix[] EigVecs10 = MatrixMath.EigenVectors(MATRIX10, EVs);
        for (TwoDimMatrix t : EigVecs10) {
            System.out.println(t);
        }
        TwoDimMatrix Eigenvector1 = EigVecs10[0];
        Complex[] Eigenvalues = MatrixMath.EigenValues(MATRIX10);
        if (Eigenvalues[0].getiValue() != 0 || Eigenvalues[1].getiValue() != 0) {
            // we have imaginary solutions
            iSolution testSolution = new iSolution(1, 4);
            // using symbols from Boyce & DiPrima
            float Lambda0 = (float) Eigenvalues[0].getrValue();
            float Mu0 = (float) Eigenvalues[0].getiValue();
            float A00 = (float) Eigenvector1.getValue(1, 1).getrValue();
            float B00 = (float) Eigenvector1.getValue(1, 1).getiValue();
            float A01 = (float) Eigenvector1.getValue(2, 1).getrValue();
            float B01 = (float) Eigenvector1.getValue(2, 1).getiValue();
            testSolution.addMus(Mu0);
            testSolution.addLambdas(Lambda0);
            testSolution.addAs(new float[]{A00, A01});
            testSolution.addBs(new float[]{B00, B01});

            testSolution.determineC();
            for (float i = 0; i < Math.PI; i+=.001) {
                float[] temp;
                temp = testSolution.getXY(i);
                System.out.println(""+temp[0]+","+temp[1]+"");
            }
        }
    }
}
