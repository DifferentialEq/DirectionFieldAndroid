package com.differentialeq.directionfield;

import com.differentialeq.directionfield.MatrixMath.Complex;

/**
 * Created by eliselkin on 10/26/15.
 */
public class testMainTest {

    public static void main (String[] arg){
        Complex a = new Complex(3.0);
        System.out.println(a.toString());
        a = new Complex(4);
        System.out.println(a.toString());
        a = new Complex("-3+.0002i");
        System.out.println(a);
        a = new Complex("30000.0002-2.0002i");
        System.out.println(a);
        a = new Complex(".000-.000i");
        System.out.println(a);
    }
}