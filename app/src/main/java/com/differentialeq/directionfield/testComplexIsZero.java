package com.differentialeq.directionfield;

import com.differentialeq.directionfield.MatrixMath.Complex;

/**
 * Created by eliselkin on 10/27/15.
 */
public class testComplexIsZero {

    public static void main(String[] args){
        Complex a = new Complex("0.01+1i");
        System.out.println(a+" is zero: "+a.isZero());
        a = new Complex("0.01+.1i");
        System.out.println(a+" is zero: "+a.isZero());
        a = new Complex("0.01+.01i");
        System.out.println(a+" is zero: "+a.isZero());
        a = new Complex("0.0000+.0000i");
        System.out.println(a+" is zero: "+a.isZero());
        a = new Complex("0.00000000000001+.00001i");
        System.out.println(a+" is zero: "+a.isZero());
        a = new Complex("0.00001+.0000000000001i");
        System.out.println(a+" is zero: "+a.isZero());
        a = new Complex("0.000000000000000000000000001+.000000000000000000000000001i");
        System.out.println(a+" is zero: "+a.isZero());
        a = new Complex("-0.00000000099999999999999999-.00000000099999999999999999i");
        System.out.println(a+" is zero closest to 0 to fail: "+a.isZero());
        a = new Complex("+0.00000000099999999999999999+.00000000099999999999999999i");
        System.out.println(a+" is zero closest to 0 to fail: "+a.isZero());
        a = new Complex("+0.000000000099999999999999999+.000000000099999999999999999i");
        System.out.println(a+" is zero closest to 0 to fail: "+a.isZero());


    }
}
