package com.differentialeq.directionfield.MatrixMath;

/**
 * (c)2015 DifferentialEq.com
 * Eli Selkin, Chang Zhang, Ahmed Ali, Ben Chen
 */


public class Quadratic {
    private Complex a;
    private Complex b;
    private Complex c;

    public Quadratic() {
    }
    public Quadratic(Complex a, Complex b, Complex c){
        this.a = a;
        this.b = b;
        this.c = c;
    }
    public Complex[] solve()
    {
        Complex[] roots = new Complex[2];

        Complex rValue = Complex.mul(Complex.mul(new Complex(0.5), a), b);
        Complex iValue = Complex.mul(Complex.mul(new Complex(0.5), a), Complex.realSqrt(Complex.sub(Complex.square(b), Complex.mul(new Complex(4), Complex.mul(a, c)))));
        roots[0] = Complex.sub(rValue, iValue);
        roots[1]= Complex.add(rValue,iValue);
        //return an array of data type Complex it's easier,so the quadratic can be called once
        return roots;
    }
    public Complex[] solvei()
    {
        Complex[] roots = new Complex[2];
        // -b+/-sqrt(b^2-4ac)
        // -b+sqrt(-(b^2-4ac))i
        Complex rValue = Complex.mul(Complex.mul(new Complex(0.5), a), b);
        Complex iValue = Complex.mul(Complex.mul(new Complex(0.5), a), Complex.mul(new Complex(0.0,1.0), Complex.realSqrt(Complex.mul(new Complex(-1), Complex.sub(Complex.square(b), Complex.mul(new Complex(4), Complex.mul(a, c)))))));
        roots[0] = Complex.sub(rValue, iValue);
        roots[1]= Complex.add(rValue,iValue);
        //return an array of data type Complex it's easier,so the quadratic can be called once
        return roots;
    }
}
