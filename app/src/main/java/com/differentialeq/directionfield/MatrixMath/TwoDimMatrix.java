package com.differentialeq.directionfield.MatrixMath;

/**
 * (c)2015 DifferentialEq.com
 * Eli Selkin, Chang Zhang, Ahmed Ali, Ben Chen
 */
public class TwoDimMatrix {
    private Complex[][] Matrix;

    /**
     * Initialize 0 argument constructor to [[0.0,0.0,0.0],[0.0,0.0,0.0]] NOT [[0.0],[0.0]] BE CAREFUL!
     */
    public TwoDimMatrix(){
        this(new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0));
    }

    /**
     * Makeshift Constructor From Strings that will just call the Complex String constructor
     * @param StringMatrix A two dimensional (equal size to desired Matrix) String array
     */
    public TwoDimMatrix(String[][] StringMatrix){
        this.Matrix = new Complex[2][];
        if (StringMatrix[0].length == 1) {
            this.Matrix[0] = new Complex[1];
            this.Matrix[1] = new Complex[1];
            this.Matrix[0][0] = new Complex(StringMatrix[0][0]);
            this.Matrix[1][0] = new Complex(StringMatrix[1][0]); // most logical assignment orientation
        } else {
            this.Matrix[0] = new Complex[3];
            this.Matrix[1] = new Complex[3];
            this.Matrix[0][0] = new Complex(StringMatrix[0][0]);
            this.Matrix[0][1] = new Complex(StringMatrix[0][1]);
            this.Matrix[0][2] = new Complex(StringMatrix[0][2]);
            this.Matrix[1][0] = new Complex(StringMatrix[1][0]);
            this.Matrix[1][1] = new Complex(StringMatrix[1][1]);
            this.Matrix[1][2] = new Complex(StringMatrix[1][2]);
        }
    }
    /**
     * Initialize 2 argument constructor to [[a11],[a21]] 2x1 matrix
     */
    public TwoDimMatrix(Complex a11, Complex a21){
        this.Matrix = new Complex[2][];
        this.Matrix[0] = new Complex[1];
        this.Matrix[1] = new Complex[1];
        this.Matrix[0][0] = a11;
        this.Matrix[1][0] = a21; // most logical assignment orientation
    }
    /**
     * Initialize 4 argument constructor to [[a11,a12],[a21,a22]] 2x2 matrix 1,3 and 2,3 added last as they are the solution matrix adjoined
     */
    public TwoDimMatrix(Complex a11, Complex a12, Complex a21, Complex a22, Complex a13, Complex a23){
        this.Matrix = new Complex[2][];
        this.Matrix[0] = new Complex[3];
        this.Matrix[1] = new Complex[3];
        this.Matrix[0][0] = a11;
        this.Matrix[0][1] = a12;
        this.Matrix[0][2] = a13;
        this.Matrix[1][0] = a21;
        this.Matrix[1][1] = a22; 
        this.Matrix[1][2] = a23;// most logical assignment orientation
    }

    /**
     * Get a value from matrix (works for either 2x2 or 2x1
     * @param row int in human logical format<br> row 0 is inputted as row 1
     * @param col int in human logical format<br> col 0 is inputted as col 1
     * @return Complex value at location accessed
     */
    public Complex getValue(int row, int col) {
        return Matrix[row-1][col-1];
    }

    /**
     * @param row int in human logical format<br> row 0 is inputted as row 1
     * @param col int in human logical format<br> col 0 is inputted as col 1
     * @param value Complex value to put in location of Matrix
     */
    public void setValue(int row, int col, Complex value){
        this.Matrix[row-1][col-1] = value;
    }

    /**
     * @return the second dimension length. <br> Returns 1 for a 2x1, 3 for a 2x3
     */
    public int getDimension(){
        return this.Matrix[0].length;
    }

    public String toString(){
        String output = "";
        output += "[[";
        for (Complex a : this.Matrix[0]){
            output += a.toString() + ",";
        }
        output = output.substring(0,output.length()-1) +"],[";

        for (Complex a : this.Matrix[1]){
            output += a.toString() + ",";
        }
        output = output.substring(0,output.length()-1) +"]]";
        return output;
    }
    
    public void swapRows() {
        if (getDimension() == 1) {
            Complex[] temp = new Complex[1];
            temp[0] = getValue(1, 1);
            setValue(1, 1, getValue(2, 1));
            setValue(2, 1, temp[0]);
        } else {
            Complex[] temp = new Complex[3];
            temp[0] = getValue(1, 1);
            temp[1] = getValue(1, 2);
            temp[2] = getValue(1, 3);
            setValue(1, 1, getValue(2, 1));
            setValue(1, 2, getValue(2, 2));
            setValue(1, 3, getValue(2, 3));
            setValue(2, 1, temp[0]);
            setValue(2, 2, temp[1]);
            setValue(2, 3, temp[2]);
        }
    }

    //Rows always will be two. get column size with getDimension();
    public static TwoDimMatrix add (TwoDimMatrix one, TwoDimMatrix two) throws RuntimeException {
        TwoDimMatrix result;
        //check size
        if (one.getDimension() != two.getDimension())
            throw new RuntimeException("Size Mismatch");

        if (one.getDimension() == 3)
            result = new TwoDimMatrix();
        else
            result = new TwoDimMatrix(new Complex(0), new Complex(0));

        for (int i = 1; i <= 2; i++) // the rows
            for (int j = 1; j <= one.getDimension(); j++)
                result.setValue(i, j, Complex.add(one.getValue(i, j), two.getValue(i, j)));
        return result;
    }

    //Rows will always be two. get column size with getDimension();
    public static TwoDimMatrix subtract(TwoDimMatrix one, TwoDimMatrix two) throws RuntimeException {
        TwoDimMatrix result;
        //check size
        if (one.getDimension() != two.getDimension())
            throw new RuntimeException("Size Mismatch");

        if (one.getDimension() == 3)
            result = new TwoDimMatrix();
        else
            result = new TwoDimMatrix(new Complex(0), new Complex(0));

        for (int i = 1; i <= 2; i++) // this 2 is the rows
            for (int j = 1; j <= one.getDimension(); j++)
                result.setValue(i, j, Complex.sub(one.getValue(i, j), two.getValue(i, j)));
        return result;
    }

    //use for multiplication of eigvenvalue with identity matrix
    public static TwoDimMatrix scalarMult (Complex scalar, TwoDimMatrix Matrix) {
        TwoDimMatrix result;

        if (Matrix.getDimension() == 3)
            result = new TwoDimMatrix(); //2x2
        else
            result = new TwoDimMatrix(new Complex(0), new Complex(0)); // 2x1

        for (int i = 1; i <= 2; i++) { // 2 is the rows
            for (int j = 1; j <= Matrix.getDimension(); j++) {
                result.setValue(i, j, Complex.mul(Matrix.getValue(i, j), scalar));
            }
        }
        return result;
    }
}
