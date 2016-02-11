package com.differentialeq.directionfield;
import com.differentialeq.directionfield.MatrixMath.Complex;
import com.differentialeq.directionfield.MatrixMath.TwoDimMatrix;

/**
 * Created by eliselkin on 10/28/15.
 */
public class testScalarMultAddSubMatrix {
    public static void main (String[] args){
        TwoDimMatrix test = new TwoDimMatrix(new Complex(1),new Complex(0),new Complex(0),new Complex(1), new Complex(0), new Complex(0));
        TwoDimMatrix test2 = new TwoDimMatrix(new Complex(1),new Complex(0),new Complex(0),new Complex(1), new Complex(0), new Complex(0));
        TwoDimMatrix test3 = new TwoDimMatrix(new Complex(1),new Complex(0));
        TwoDimMatrix test4 = new TwoDimMatrix(new Complex(0),new Complex(1));
        TwoDimMatrix test5 = TwoDimMatrix.add(test, test2);
        System.out.println(test5);
        TwoDimMatrix.subtract(test5, test2); // subtract two from 5
        System.out.println(test5);
        TwoDimMatrix test6 = TwoDimMatrix.scalarMult(new Complex(5.0), test5);
        System.out.println(test6);
        TwoDimMatrix test7 = TwoDimMatrix.scalarMult(new Complex("3-2i"), test4);
        System.out.println(test7);
        TwoDimMatrix test8 = TwoDimMatrix.add(test7, test3);
        System.out.println(test8);
    }
}
