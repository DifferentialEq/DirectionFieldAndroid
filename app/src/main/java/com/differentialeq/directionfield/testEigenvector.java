package com.differentialeq.directionfield;

import com.differentialeq.directionfield.MatrixMath.Complex;
import com.differentialeq.directionfield.MatrixMath.MatrixMath;
import com.differentialeq.directionfield.MatrixMath.TwoDimMatrix;

/**
 * Created by eliselkin on 10/28/15.
 */
public class testEigenvector {
    public static void main(String[] args){
        TwoDimMatrix EVs = new TwoDimMatrix(new Complex(0),new Complex(0));
        TwoDimMatrix TEST = new TwoDimMatrix(new Complex(.75), new Complex(-2), new Complex(1), new Complex(-1.25), new Complex(0), new Complex(0));
        TwoDimMatrix[] EigVecs0 = MatrixMath.EigenVectors(TEST, EVs);
        for (TwoDimMatrix t: EigVecs0){
            System.out.println(t);
        }
        // Boyce And DiPrima 7.5 Realvalued
        TwoDimMatrix MATRIX = new TwoDimMatrix(new Complex(1), new Complex(1), new Complex(4), new Complex(1), new Complex(0), new Complex(0));
        TwoDimMatrix[] EigVecs = MatrixMath.EigenVectors(MATRIX, EVs);
        for (TwoDimMatrix t: EigVecs){
            System.out.println(t);
        }
        System.out.println(" ^Wolfram gives: [[1],[2]] and [[-1],[2]]");

        TwoDimMatrix MATRIX2 = new TwoDimMatrix(new Complex(-3), new Complex(Math.sqrt(2)), new Complex(Math.sqrt(2)), new Complex(-2), new Complex(0), new Complex(0));
        TwoDimMatrix[] EigVecs2 = MatrixMath.EigenVectors(MATRIX2, EVs);
        for (TwoDimMatrix t: EigVecs2){
            System.out.println(t);
        }
        System.out.println(" ^Wolfram gives: [[-sqrt(2)],[1]] and [[1/sqrt(2)],[1]]");

        // B&DiP Problem 1 7.5 back of the book shows [[1],[2]] and [[2],[1]]
        TwoDimMatrix MATRIX3 = new TwoDimMatrix(new Complex(3), new Complex(-2), new Complex(2), new Complex(-2), new Complex(0), new Complex(0));
        TwoDimMatrix[] EigVecs3 = MatrixMath.EigenVectors(MATRIX3, EVs);
        for (TwoDimMatrix t: EigVecs3){
            System.out.println(t);
        }
        System.out.println(" ^Wolfram gives: [[2],[1]] and [[1],[2]]");

        // http://tutorial.math.lamar.edu/Classes/DE/RealEigenvalues.aspx
        TwoDimMatrix MATRIX4 = new TwoDimMatrix(new Complex(1), new Complex(2), new Complex(3), new Complex(2), new Complex(0), new Complex(0));
        TwoDimMatrix[] EigVecs4 = MatrixMath.EigenVectors(MATRIX4, EVs);
        for (TwoDimMatrix t: EigVecs4){
            System.out.println(t);
        }
        System.out.println(" ^Wolfram gives: [[2],[3]] and [[-1],[1]]");

        // http://tutorial.math.lamar.edu/Classes/DE/RealEigenvalues.aspx
        TwoDimMatrix MATRIX5 = new TwoDimMatrix(new Complex(-5), new Complex(1), new Complex(4), new Complex(-2), new Complex(0), new Complex(0));
        TwoDimMatrix[] EigVecs5 = MatrixMath.EigenVectors(MATRIX5, EVs);
        for (TwoDimMatrix t: EigVecs5){
            System.out.println(t);
        }
        System.out.println(" ^Wolfram gives: [[-1],[1]] and [[1],[4]]");

        // Eli: Identity test case
        TwoDimMatrix MATRIX6 = new TwoDimMatrix(new Complex(2), new Complex(0), new Complex(0), new Complex(4), new Complex(0), new Complex(0));
        TwoDimMatrix[] EigVecs6 = MatrixMath.EigenVectors(MATRIX6, EVs);
        for (TwoDimMatrix t: EigVecs6){
            System.out.println(t);
        }
        System.out.println(" ^Wolfram gives: [[0],[1]] and [[1],[0]]");

        // Eli: test case GETTING ERROR HERE
        TwoDimMatrix MATRIX7 = new TwoDimMatrix(new Complex(2), new Complex(0), new Complex(2), new Complex(0), new Complex(0), new Complex(0));
        TwoDimMatrix[] EigVecs7 = MatrixMath.EigenVectors(MATRIX7, EVs);
        for (TwoDimMatrix t: EigVecs7){
            System.out.println(t);
        }
        System.out.println(" ^Wolfram gives: [[1],[1]] and [[0],[1]]");


        // Eli: test case
        TwoDimMatrix MATRIX8 = new TwoDimMatrix(new Complex(5), new Complex(1), new Complex(-2), new Complex(0), new Complex(0), new Complex(0));
        TwoDimMatrix[] EigVecs8 = MatrixMath.EigenVectors(MATRIX8, EVs);
        for (TwoDimMatrix t: EigVecs8){
            System.out.println(t);
        }
        System.out.println(" ^Wolfram gives: [[1/4(-5-sqrt(17))],[1]] and [[1/4(-5+sqrt(17))],[1]] ");
        System.out.println(" ^Wolfram gives: [[-2.2807764064],[1]] and [[-0.21922359],[1]] ");
        System.out.println(" Which is actually the same as ours, just inverted. ");


        // Eli: test case
        TwoDimMatrix MATRIX9 = new TwoDimMatrix(new Complex(1), new Complex(3), new Complex(2), new Complex(5), new Complex(0), new Complex(0));
        TwoDimMatrix[] EigVecs9 = MatrixMath.EigenVectors(MATRIX9, EVs);
        for (TwoDimMatrix t: EigVecs9){
            System.out.println(t);
        }
        System.out.println(" ^Wolfram gives: [[1/2(-2+sqrt(10))],[1]] and [[1/2(-2-sqrt(10))],[1]] ");
        System.out.println(" ^Wolfram gives: [[.581139],[1]] and [[-2.58114],[1]] ");
        System.out.println(" Which is actually the same as ours, just inverted. ");

        // Eli: test case SHOULD INVOKE solvei
        TwoDimMatrix MATRIX10 = new TwoDimMatrix(new Complex(3), new Complex(-9), new Complex(4), new Complex(-3), new Complex(0), new Complex(0));
        TwoDimMatrix[] EigVecs10 = MatrixMath.EigenVectors(MATRIX10, EVs);
        for (TwoDimMatrix t: EigVecs10){
            System.out.println(t);
        }

        // Eli: test case SHOULD INVOKE solvei
        TwoDimMatrix MATRIX11 = new TwoDimMatrix(new Complex(-.5), new Complex(1), new Complex(-1), new Complex(-.5), new Complex(0), new Complex(0));
        TwoDimMatrix[] EigVecs11 = MatrixMath.EigenVectors(MATRIX11, EVs);
        for (TwoDimMatrix t: EigVecs11){
            System.out.println(t);
        }
        Complex[] EigVals = MatrixMath.EigenValues(MATRIX11);
        System.out.println("EVs:"+EVs);
        System.out.println(EigVals[0]);
        System.out.println(EigVals[1]);


    }
}
