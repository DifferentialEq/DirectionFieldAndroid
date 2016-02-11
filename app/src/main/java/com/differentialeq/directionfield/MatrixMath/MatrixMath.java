package com.differentialeq.directionfield.MatrixMath;

import android.util.Log;

/**
 * (c)2015 DifferentialEq.com
 * Eli Selkin, Chang Zhang, Ahmed Ali, Ben Chen
 */

public class MatrixMath {
    //private final TwoDimMatrix Matrix; // actually I don't think we need an object held here
    public MatrixMath() {
        //Matrix = new TwoDimMatrix(); // initialize Matrix to [[0.0,0.0],[0.0,0.0]]
    }

    /**
     * Called by EigenVectors
     *
     * @param Matrix
     * @return Complex[] with 2 values
     */
    public static Complex[] EigenValues(TwoDimMatrix Matrix) {
        Complex[] EigenValues;

        Complex a = Matrix.getValue(1, 1),
                b = Matrix.getValue(1, 2),
                e = Matrix.getValue(1, 3),
                c = Matrix.getValue(2, 1),
                d = Matrix.getValue(2, 2),
                f = Matrix.getValue(2, 3);

        if (Matrix.getDimension() != 3)
            throw new RuntimeException("oops"); // Subclass this exception ZZZ

        // Create a,b,c for (-b +/- sqrt(b^2-4*a*c))/2a
        Complex quada = new Complex(1);
        Complex quadb = Complex.add(a, d);
        Complex quadc = Complex.sub(Complex.mul(a, d), Complex.mul(b, c));
        Quadratic Lambdas = new Quadratic(quada, quadb, quadc);

        if (Complex.realGreaterOrEqual(Complex.mul(new Complex(4), Complex.sub(Complex.mul(a, d), Complex.mul(c, b))), Complex.square(Complex.add(a, d))))
            EigenValues = Lambdas.solvei(); // imaginary version
        else
            EigenValues = Lambdas.solve();
        return EigenValues;
    }


    /**
     * @return Array of 2 TwoDimMatrixs with dimension 2x1
     * @parameter Matrix is
     */
    public static TwoDimMatrix[] EigenVectors(TwoDimMatrix Matrix, TwoDimMatrix EigenValues) {
        TwoDimMatrix EigenVectors[] = new TwoDimMatrix[2];
        EigenVectors[0] = new TwoDimMatrix(new Complex(0.0), new Complex(0.0));
        EigenVectors[1] = new TwoDimMatrix(new Complex(0.0), new Complex(0.0));
        Complex[] Lambdas = EigenValues(Matrix); // Lambdas is array of 2 complex
        /*
         * From the Epsilons find the actual vectors that when multiplied by x[A]

         */

        // 3) on Algorithm a)
        for (int i = 0; i < Lambdas.length; i++) {
            EigenValues.setValue(i+1,1, Lambdas[i]);
            // Make a matrix Mi
            TwoDimMatrix I = new TwoDimMatrix(new Complex(1.0), new Complex(0.0), new Complex(0.0), new Complex(1.0), new Complex(0.0), new Complex(0.0));
            I = TwoDimMatrix.scalarMult(Lambdas[i], I); //Lambda*I
            TwoDimMatrix M = TwoDimMatrix.subtract(Matrix, I); //A-lambda*I // just reversed
            // Now we have the matrix we are working on
            //Log.i("ZZZ", "for lambda:"+Lambdas[i]+" Gauss elim on:" + M.toString());
            Gauss_Elim(M); // in place alteration of matrix
            //Log.i("ZZZ", "yields:" + M.toString());
            // Reduced form can take
            // 1 // [[0, 0],[0, 0]]
            // 2 // [[0, b],[0, 0]]
            // 3 // [[1, 0],[0, 0]]
            // 4 // [[1, 0],[0, 1]]
            // 5 // [[1, b],[0, 0]]
            if (M.getValue(1, 1).isZero()) {
                // matrix begins in 1,1 with 0
                if (M.getValue(1, 2).isZero()) {
                    // Category 1
                    continue; // Eigenvector will also be
                } else {
                    // Category 2
                    EigenVectors[i].setValue(2, 1, new Complex(1.0));
                    continue;
                }
            } else {
                if (M.getValue(1, 2).isZero() && M.getValue(2, 2).isZero()) {
                    EigenVectors[i].setValue(1,1, new Complex(1.0));
                    // category 3 ... simple first
                    continue;
                } else if (M.getValue(1, 2).isZero()) {
                    // Category 4 ... Identity matrix
                    continue;
                } else {
                    // Category 5
                    EigenVectors[i].setValue(2, 1, M.getValue(1, 1));
                    EigenVectors[i].setValue(1, 1, Complex.mul(new Complex(-1), M.getValue(1, 2)));
                    // If those values are fractions, try to make them some whole number
                     if (EigenVectors[i].getValue(1, 1).distFromZero() < 1.0) {
                        EigenVectors[i].setValue(2, 1, Complex.div(EigenVectors[i].getValue(2, 1), EigenVectors[i].getValue(1, 1)));
                        EigenVectors[i].setValue(1, 1, Complex.div(EigenVectors[i].getValue(1, 1), EigenVectors[i].getValue(1, 1)));
                     }
                    if (EigenVectors[i].getValue(1,1).getiValue() != 0)
                        EigenVectors[i].swapRows();
                }
            }

        }
        //Log.i("ZZZ","Eigen1"+EigenVectors[0]+ " Eigen2:"+EigenVectors[1]);
        return EigenVectors;
    }



    // Matrix [[A,B,E],[C,D,F]]
    /**
     * Conduct Gaussian Elimination on the matrix M. Not iterative process of Gauss-Seidel because of the small size.
     * @param M, TwoDimMatrix 2x2 expected
     */
    public static void Gauss_Elim(TwoDimMatrix M) {
        if (M.getValue(1, 1).isZero()) // if the matrix element at (1,1) is zero.
        {
            M.swapRows(); // swap row 1 with row 2
            // test if A (was C) is 0+0i // 3) a) iii)
            if (M.getValue(1, 1).isZero()) // if the switched row 1's (1,1) element is still zero
            {
                // 3) a) iii) 1) a) i) both A,C == 0
                if (M.getValue(1, 2).isZero() && M.getValue(2, 2).isZero()) {
                    // A, B, C, D == 0+0i
                    // Matrix is [[0,0],[0,0]] // Do nothing!
                    return;
                } else {
                    // A, C still 0
                    // 3) a) iii) 1) a) ii) Since we already checked if both are 0+0i
                    // Now we can just test if the top is 0 and if it is, then swap
                    // and make the B (row 1 column 2) = 1
                    if (M.getValue(1, 2).isZero()) {
                        // B is 0 but C is nonzero
                        M.swapRows(); 
                        // New (after swap) E/B
                        M.setValue(1, 3, Complex.div(M.getValue(1, 3), M.getValue(1, 2)));
                        M.setValue(1, 2, new Complex(1.0));
                        return;
                    } else {
                        // A, C == 0
                        // B is non Zero which would 0 out C with elementary row operations
                        M.setValue(1, 2, new Complex(1.0));
                        M.setValue(1, 3, Complex.div(M.getValue(1,3), M.getValue(1,2)));
                        Complex fSub = Complex.sub(M.getValue(2, 3), Complex.mul(M.getValue(2, 2), M.getValue(1, 3))) ;
                        if (!fSub.isZero())
                            throw new RuntimeException("Matrix Error: 0+0 != non-zero number");
                        
                        M.setValue(2, 2, new Complex(0.0));
                        M.setValue(2, 3, new Complex(0.0));
                        
                        return;
                    }
                }
            }
        }

        // If we have gotten here, A is nonzero and C may be 0+0i, but it doesn't matter yet
        if (!M.getValue(1, 1).isZero()) {
            // 3) iv) In B, store B/A, in A, store A/A
            M.setValue(1, 2, Complex.div(M.getValue(1, 2), M.getValue(1, 1)));
            M.setValue(1, 3, Complex.div(M.getValue(1, 3), M.getValue(1, 1)));
            M.setValue(1, 1, new Complex(1.0)); // A/A
            // -1*C*B
            Complex bMult = Complex.mul(Complex.mul(new Complex(-1), M.getValue(2, 1)), M.getValue(1, 2));
            // -1*C*E
            Complex fMult = Complex.mul(Complex.mul(new Complex(-1), M.getValue(2, 1)), M.getValue(1, 3));
            M.setValue(2, 1, new Complex(0)); // C is now 0
            // Store the values in bMult so that we can test if it's 0
            bMult = Complex.add(M.getValue(2, 2), bMult);
            fMult = Complex.add(M.getValue(2, 3), fMult);
            M.setValue(2, 2, bMult);
            M.setValue(2, 3, fMult);
            if (M.getValue(2,2).isZero()) {
                // HAD TO ADD THIS TEST TO PREVENT DIVIDE BY 0
                if (M.getValue(2,1).isZero() && !M.getValue(2,3).isZero()) {
                    throw new RuntimeException("Matrix error: 0+0 != non-zero number");
                }
            } else {
                // D is nonzero
                M.setValue(2,3, Complex.div(M.getValue(2,3),M.getValue(2,2)));
                M.setValue(2, 2, new Complex(1));
                // [[1,B,E],[0,1,F]]
                Complex eMult = Complex.mul(new Complex(-1), Complex.mul(M.getValue(1,2), M.getValue(2,2))); // because 2,2 should be 1.0
                M.setValue(1,2, new Complex(0));
                eMult = Complex.mul(eMult, M.getValue(2,3));
                eMult = Complex.add(eMult, M.getValue(1,3));
                M.setValue(1,3, eMult);
            }
        }
    }
}