package com.differentialeq.directionfield;

import com.differentialeq.directionfield.MatrixMath.Complex;
import com.differentialeq.directionfield.MatrixMath.TwoDimMatrix;

/**
 * Created by eliselkin on 10/26/15.
 */
public class testMatrixRowSwap {
    public static void main(String[] args){
        TwoDimMatrix twobytwo = new TwoDimMatrix(new Complex("5+0i"), new Complex("2-1i"), new Complex("1+2i"), new Complex(0), new Complex(0), new Complex(0));
        System.out.println(twobytwo);
        twobytwo.swapRows();
        System.out.println(twobytwo);
        twobytwo.swapRows();
        System.out.println(twobytwo);
        TwoDimMatrix twobyone = new TwoDimMatrix(new Complex("5+0.1212i"), new Complex("22.1-1i"));
        System.out.println(twobyone);
        twobyone.swapRows();
        System.out.println(twobyone);
        twobyone.swapRows();
        System.out.println(twobyone);

    }
}
