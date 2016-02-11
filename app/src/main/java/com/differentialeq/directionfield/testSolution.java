package com.differentialeq.directionfield;

import com.differentialeq.directionfield.MatrixMath.Complex;
import com.differentialeq.directionfield.MatrixMath.MatrixMath;
import com.differentialeq.directionfield.MatrixMath.TwoDimMatrix;
import com.differentialeq.directionfield.Solution;
import com.differentialeq.directionfield.iSolution;


public class testSolution {
    public static void main(String[] args) {

        // Boyce And DiPrima 7.5 Realvalued
        TwoDimMatrix EVs = new TwoDimMatrix(new Complex(0),new Complex(0));
        TwoDimMatrix MATRIX = new TwoDimMatrix(new Complex(1), new Complex(1), new Complex(4), new Complex(1), new Complex(0), new Complex(0));
        TwoDimMatrix[] EigVecs = MatrixMath.EigenVectors(MATRIX, EVs);
        for (TwoDimMatrix t: EigVecs){
            System.out.println(t);
        }
        Complex[] Eigenvalues = MatrixMath.EigenValues(MATRIX);
        Solution testSolution = new Solution((float)3, -5);
        // using symbols from Boyce & DiPrima

        testSolution.setRs(Eigenvalues);
        testSolution.setEigenvectors(EigVecs);
        testSolution.determineC();

        for (float i = 0; i < 1; i+=.01) {
            float[] temp;
            temp = testSolution.getXY(i);
            System.out.println(""+temp[0]+","+temp[1]+"");
        }
    }
}
