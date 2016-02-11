package com.differentialeq.directionfield.MatrixMath;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * (c)2015 DifferentialEq.com
 * Eli Selkin, Chang Zhang, Ahmed Ali, Ben Chen
 */
public class Complex {
    final private static double epsilon = 1E-10;
    private double rValue;
    private double iValue;


    // Constructors

    /**
     * Zero argument constructor initializes this Complex number to 0+0i
     */
    public Complex(){
        this.rValue = 0.0;
        this.iValue = 0.0;
    }

    /**
     * 1 integer parameter constructor
     * @param integerConst integer, transforms integer to real component of a Complex object
     */
    public Complex(int integerConst){
        this.rValue = integerConst*1.0;
        this.iValue = 0.0;
    }

    /**
     * 1 double parameter constructor
     * @param doubleConst
     */
    public Complex(double doubleConst){
        this.rValue = doubleConst;
        this.iValue = 0.0;
    }


    public Complex (int intReal, int intImag){
        this.rValue = intReal*1.0;
        this.iValue = intImag*1.0;
    }

    public Complex (double dblReal, double dblImag){
        this.rValue = dblReal;
        this.iValue = dblImag;
    }


    /**
     * A constructor for Strings +100.01-20.1i or 1+0i or 1-0i or .01-.01i
     */
    public Complex(String stringComplex){
        Pattern pattImag = Pattern.compile("^([+-]?[0-9]*[.]?[0-9]+)([+-][0-9]*[.]?[0-9]+)[i]$");
        Matcher matchImag = pattImag.matcher(stringComplex); // Make the groups
        if (matchImag.find()){
            rValue = Double.parseDouble(matchImag.group(1));
            iValue = Double.parseDouble(matchImag.group(2)); // This is the only matcher where 1 and 2 make sense in the same find
        }
    }

    /**
     * Non static mutator
     * @param real double, the real component of the Complex conjugate
     */
    public void setrValue(double real){
        this.rValue = real;
    }

    /**
     * Non-static accessor for the real component
     * @return double the real component of the Complex conjugate
     */
    public double getrValue(){
        return this.rValue;
    }

    /**
     * Set the imaginary component of this Complex conjugate in this non-static mutator
     * @param imaginary double value for multiplier times i
     */
    public void setiValue(double imaginary){
        this.iValue = imaginary;
    }

    /**
     * @return double != 0 then we have an imaginary component, where this is the multiplier times i<br>
     *         or<br>
     *         double == 0 then we have only a real value
     */
    public double getiValue(){
        return this.iValue;
    }

    /**
     * Addition of Complex objects
     * @param a Complex
     * @param b Complex
     * @return Complex result of adding real to real and imaginary to imaginary components
     */
    public static Complex add(Complex a, Complex b){
        double rResult = a.rValue + b.rValue;
        double iResult = a.iValue + b.iValue ;
        return new Complex(rResult,iResult);
    }

    /**
     *
     * @param a
     * @param b
     * @return
     */
    public static Complex sub(Complex a, Complex b){
        double rResult = a.rValue - b.rValue;
        double iResult = a.iValue - b.iValue;
        return new Complex(rResult,iResult);
    }

    /**
     * @param a Complex object
     * @param b Complex object
     * @return Complex multiple of a * b
     */
    public static Complex mul(Complex a, Complex b){
        double real;
        double _rValue = a.rValue * b.rValue;
        double _iValue = a.rValue * b.iValue;
        _iValue += a.iValue * b.rValue;
        _rValue -= a.iValue * b.iValue; // i^2 = -1, therefore -=
        return new Complex(_rValue,_iValue);
    }

    /**
     * Using Complex object math
     * @param a
     * @param b
     * @return Complex quotient of a / b
     */
    public static Complex div(Complex a, Complex b){
        Complex result = new Complex();
        double _rValue = a.rValue * b.rValue;
        double _iValue = a.rValue * b.iValue * (-1);
        _iValue += a.iValue * b.rValue;
        _rValue -= a.iValue * b.iValue * (-1);
        double denom = (b.rValue * b.rValue) + (b.iValue * b.iValue);
        _iValue /= denom;
        _rValue /= denom;
        return new Complex(_rValue,_iValue);

    }


    /**
     * a^2 is just a * a in complex math
     * @param a Complex object
     * @return new Complex object result of multiplying a * a
     */
    public static Complex square(Complex a){
        return Complex.mul(a, a);
    }

    /**
     * Since there are no reasons in this program to have an imaginary square root
     * @param a Complex
     * @return Complex
     */
    public static Complex realSqrt(Complex a) {
        return new Complex(Math.sqrt(a.rValue));
    }

    // Also not using imaginary values
    public static boolean realGreaterOrEqual(Complex a, Complex b) {
        return a.rValue > b.rValue;
    }

    /**
     * the toString that implements a specific formatting for the float components of the real and imaginary of this complex number
     * @return a String formatted to +0.00000+0.00000i or larger depending on the size before the decimal
     */
    @Override
    public String toString() {
        return String.format("%+5.4f", rValue)+String.format("%+5.4f", iValue)+"i";
    }

    /**
     * http://floating-point-gui.de/errors/comparison/ Comparing complex conjugate to 0.0+0.0i
     */
    public boolean isZero(){
        final double absR = Math.abs(rValue);
        final double absI = Math.abs(iValue);
        if (rValue == 0 && iValue == 0){
            return true;
        } else if (absR <= epsilon && absI <= epsilon ){
            rValue = iValue = 0;
            return true;
        }
        return false;
    }

    /**
     * Distance to the origin on the CxR plane
     * @return
     */
    public double distFromZero(){
        return Math.sqrt(Math.pow(rValue, 2.0) + Math.pow(iValue, 2.0));
    }

}
