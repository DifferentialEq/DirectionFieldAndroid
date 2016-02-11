package com.differentialeq.directionfield;

import com.differentialeq.directionfield.MatrixMath.Complex;
import com.differentialeq.directionfield.MatrixMath.MatrixMath;
import com.differentialeq.directionfield.MatrixMath.TwoDimMatrix;

/**
 * Created by eliselkin on 11/4/15.
 */
public class testGaussElim {
    public static void main(String[] args) {
        TwoDimMatrix MATRIX1 = new TwoDimMatrix(new Complex(1), new Complex(2), new Complex(4), new Complex(5), new Complex(3), new Complex(6));
        System.out.println("Original matrix:" + MATRIX1);
        MatrixMath.Gauss_Elim(MATRIX1);
        System.out.println("Reduced Row echelon form:"+MATRIX1);


        TwoDimMatrix MATRIX2 = new TwoDimMatrix(new Complex(1,-3), new Complex(2,5), new Complex(4,1), new Complex(5,0), new Complex(0,3), new Complex(6));
        System.out.println("Original matrix:" + MATRIX2);
        MatrixMath.Gauss_Elim(MATRIX2);
        System.out.println("Reduced Row echelon form:"+MATRIX2);

        TwoDimMatrix MATRIX3 = new TwoDimMatrix(new Complex(5), new Complex(0), new Complex(0), new Complex(3), new Complex(0), new Complex(0));
        System.out.println("Original matrix:"+MATRIX3);
        MatrixMath.Gauss_Elim(MATRIX3);
        System.out.println("Reduced Row echelon form:" + MATRIX3);

        TwoDimMatrix MATRIX4 = new TwoDimMatrix(new Complex(1), new Complex(1), new Complex(0), new Complex(0), new Complex(4), new Complex(9));
        System.out.println("Original matrix:"+MATRIX4);
        try {
            MatrixMath.Gauss_Elim(MATRIX4);
            System.out.println("Reduced Row echelon form:" + MATRIX4);
        } catch(RuntimeException r){
            System.out.println("Caught expected error with bottom row 0+0=9");
        }

    }


}
