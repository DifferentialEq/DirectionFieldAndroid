package com.differentialeq.directionfield.MatrixMath;

import android.util.Log;

/**
 * Created by eliselkin on 11/23/15.
 */
public class MathZero {
    //http://floating-point-gui.de/errors/comparison/
    public static boolean approxEqual(float a, float b, float epsilon) {
        final float absA = Math.abs(a);
        final float absB = Math.abs(b);
        final float diff = Math.abs(a - b);
        if (a == b){
            return true;
        }else if (a == 0 || b == 0 || diff < Float.MIN_NORMAL) {
            return diff < (epsilon*Float.MIN_NORMAL);
        }
        else
            return diff / Math.min((absA + absB), Float.MAX_VALUE) < epsilon;
    }
}
