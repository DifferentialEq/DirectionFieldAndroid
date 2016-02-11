package com.differentialeq.directionfield;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * (c)2015 DifferentialEq.com
 * Eli Selkin, Chang Zhang, Ahmed Ali, Ben Chen
 */

public class DBManager extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "DirectionFieldDB";
    public static final String TABLE_NAME = "Matrices";
    public static final String MATRIX_COL_NAME = "Matrix";
    public static final String MATRIX_COL_TYPE = "VARCHAR(128)"; // This will be the primary key
    public static final String EIG1_COL_NAME = "EigenValue1";
    public static final String EIG1_COL_TYPE = "VARCHAR(64)";
    public static final String EIG2_COL_NAME = "EigenValue2";
    public static final String EIG2_COL_TYPE = "VARCHAR(64)";
    public static final String EIGVEC1_COL_NAME = "EigenVector1";
    public static final String EIGVEC1_COL_TYPE = "VARCHAR(128)";
    public static final String EIGVEC2_COL_NAME = "EigenVector2";
    public static final String EIGVEC2_COL_TYPE = "VARCHAR(128)";
    public static final String PTS_COL_NAME = "Points";
    public static final String PTS_COL_TYPE = "VARCHAR(256)";
    public static final String TABLE_TUTORIAL = "TUTORIALTABLE";
    public static final String TUTWORD_COL_NAME = "TUTORIAL";
    public static final String TUTWORD_COL_TYPE = "VARCHAR(10)";
    public static final String TUTORIAL_COL_NAME = "COMPLETED";
    public static final String TUTORIAL_COL_TYPE = "INTEGER";

    public DBManager(Context context)
    {
        super(context, DATABASE_NAME , null, 1);
    }

    // Sample row as Strings
    //                           Matrix   Eigenvalue1 Eigenvalue2  EigenVector1                      EigenVector2         Points (varchar)
    // "[[0.0+0i,0.1-1i],[0.1-1i,0.0+0i]]", "0.0+0i", "0.0+0i", "[[0.0+0.0i],[0.0+0.0i]]", "[[0.0+0.0i],[0.0+0.0i]]", "(0,0),(3,4),(-2.-3)");

    /**
     * Whenever we create an object of the DBManager it will run this only the first time.
     * @param directionfieldDB SQLiteDatabase, It actually finds itself so it's not getting passed in anything by us
     */
    @Override
    public void onCreate(SQLiteDatabase directionfieldDB) {
        // Create the tutorial DB so we can store whether complete or not
        String tutQueryString = "CREATE TABLE IF NOT EXISTS " + TABLE_TUTORIAL + "(" +
                TUTWORD_COL_NAME + " " + TUTWORD_COL_TYPE + " primary key, " +
                TUTORIAL_COL_NAME + " " + TUTORIAL_COL_TYPE + " );";
        directionfieldDB.execSQL(tutQueryString);

        String queryString = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +"(" +
                MATRIX_COL_NAME + " " + MATRIX_COL_TYPE + " primary key, " +
                EIG1_COL_NAME + " " + EIG1_COL_TYPE + ", " +
                EIG2_COL_NAME + " " + EIG2_COL_TYPE + ", " +
                EIGVEC1_COL_NAME + " " + EIGVEC1_COL_TYPE + ", " +
                EIGVEC2_COL_NAME + " " + EIGVEC2_COL_TYPE + ", " +
                PTS_COL_NAME + " " + PTS_COL_TYPE + ");";
        directionfieldDB.execSQL(queryString);
        directionfieldDB.execSQL("INSERT INTO " + TABLE_TUTORIAL + " VALUES (\"Tutorial\", 0);");
    }

    /**
     * Only when you are upgrading DB versions (i.e. updating your device, I think, even then it doesn't always happen)
     * @param db The SQLiteDatabase
     * @param oldVersion Old Version Number
     * @param newVersion New Version Number
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TUTORIAL);
        onCreate(db);
    }

    /**
     * Tutorial bit to set in the database
     * @param finished
     * @return false if could not add to DB
     */
    public void setTutorialFinished(boolean finished){

        SQLiteDatabase db = this.getWritableDatabase();
        // now make a new contents list and put those values in
        ContentValues contentValues = new ContentValues();
        contentValues.put(TUTORIAL_COL_NAME, finished);
        // IF insert returns -1 it could not insert!
        db.update(TABLE_TUTORIAL, contentValues, TUTWORD_COL_NAME + " = ?", new String[]{"Tutorial"});
        db.close();
    }

    /**
     * Returns state of table in the DB with one row and two columns showing the state of the tutorial completedness
     * @return true/false if table holds 1/0 respectively
     */
    public boolean getTutorialFinished(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor resource = db.rawQuery("SELECT * FROM " + TABLE_TUTORIAL, null);
        resource.moveToFirst(); // position the cursor resource returned to the initial value acts like an iterator
        if (resource.isAfterLast())
            return false;
        int value = resource.getInt(resource.getColumnIndex(TUTORIAL_COL_NAME));
        db.close();
        return value == 1;
    }




    /**
     * A function which gets every row in the Table (TABLE_NAME) and returns just the first column (the Matrix Name)
     * i.e. The string that can be used to build the matrix object
     * @return ArrayList of Strings with all matrices stored in elements.
     */
    public ArrayList<String> getMatrixNames(){
        ArrayList<String> stringListMatrices = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor resource = db.rawQuery("SELECT " + MATRIX_COL_NAME + " FROM " + TABLE_NAME, null);
        resource.moveToFirst(); // position the cursor resource returned to the initial value acts like an iterator
        while(!resource.isAfterLast()){
            stringListMatrices.add(resource.getString(resource.getColumnIndex(MATRIX_COL_NAME)));
            resource.moveToNext();
        }
        return stringListMatrices;
    }

    /**
     * Presumably the user has selected a Matrix from the ListView in SelectMatrixOrNew and is now
     * pulling all information from the row corresponding to that Matrix Name.
     * @param Matrix String, Checked against all Matrix Names in the Table
     * @return an Array of Strings containing Matrix Name, Eigenvalue 1, Eig 2, Eigenvector1, Eigenvector2, Points
     */
    public String[] getMatrixRow(String Matrix){
        String[] MatrixResources = new String[6];
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor resource = db.query(TABLE_NAME, new String[]{"*"}, MATRIX_COL_NAME + "=?", new String[]{Matrix}, null, null, null); // get all columns
        resource.moveToFirst(); // position the cursor resource returned to the initial value acts like an iterator
        MatrixResources[0] = resource.getString(resource.getColumnIndex(MATRIX_COL_NAME));
        MatrixResources[1] = resource.getString(resource.getColumnIndex(EIG1_COL_NAME));
        MatrixResources[2] = resource.getString(resource.getColumnIndex(EIG2_COL_NAME));
        MatrixResources[3] = resource.getString(resource.getColumnIndex(EIGVEC1_COL_NAME));
        MatrixResources[4] = resource.getString(resource.getColumnIndex(EIGVEC2_COL_NAME));
        MatrixResources[5] = resource.getString(resource.getColumnIndex(PTS_COL_NAME));
        return MatrixResources;
    }

    /**
     * If we want to add a new Matrix into the DB we need some data. Matrix Name is the only required column though.
     * @param Matrix Primary Key of the Table, this string corresponds to the actual matrix A values
     * @param Eig1 The 1st eigenvalue (not ordered)
     * @param Eig2 The 2nd eigenvalue (not ordered)
     * @param EigVec1 The 1st Eigenvector (Corresponding to Eigenvalue 1)
     * @param EigVec2 The 2nd Eigenvector (Corresponding to Eigenvalue 2)
     * @param Pts The list of points (x11,x21),(x12,x22),...
     * @return True if DB successfully added row to column
     */
    public boolean addSystem(String Matrix, String Eig1, String Eig2, String EigVec1, String EigVec2, String Pts){
        // Add all pertinent information to a row with Matrix as the primary key
        SQLiteDatabase db = this.getWritableDatabase();
        // This is the ORM built in
        ContentValues contents = new ContentValues();
        contents.put(MATRIX_COL_NAME, Matrix);
        contents.put(EIG1_COL_NAME, Eig1);
        contents.put(EIG2_COL_NAME, Eig2);
        contents.put(EIGVEC1_COL_NAME, EigVec1);
        contents.put(EIGVEC2_COL_NAME, EigVec2);
        contents.put(PTS_COL_NAME, Pts);
        db.insert(TABLE_NAME, null, contents);
        return true; // handled by function
    }

    /**
     * Delete an entire row connected with a matrix named Matrix
     * @param Matrix String, The primary key of the row associated with the matrix
     * @return True if deleted, False otherwise
     */
    public boolean delSystem(String Matrix){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, MATRIX_COL_NAME+" = ?", new String[]{Matrix}); // replaces ? with string and select the first element
        return true;
    }

    /**
     * Add a point Point to the list (or empty list) already in the Points column of the Matrix table
     * @param Matrix String, The primary key searching for the points for
     * @param Point String, The point to be added Format: "(+/-0.00,+/-0.00)"
     * @return True is update was successful, False otherwise
     */
    public boolean addPoint(String Matrix, String Point){
        SQLiteDatabase db = this.getWritableDatabase();
        // Query first the points of the matrix row with Matrix id
        // select points and add points
        Cursor resource = db.query(TABLE_NAME, new String[]{"*"}, MATRIX_COL_NAME + "=?", new String[]{Matrix}, null, null, null); // get all columns
        resource.moveToFirst();
        String newPoints;
        if (resource.getString((resource.getColumnIndex(PTS_COL_NAME))).equals(""))
            newPoints = Point;
        else
            newPoints = resource.getString(resource.getColumnIndex(PTS_COL_NAME))+","+Point;
        ContentValues contents = new ContentValues();
        contents.put(PTS_COL_NAME, newPoints);
        db.update(TABLE_NAME,contents,MATRIX_COL_NAME+" = ?", new String[]{Matrix});
        return true;
    }

    /**
     * Delete a point Point from the list of points associated with the matrix named Matrix
     * @param Matrix The key of the row you are looking for
     * @param Point The value that you want to remove from the list. If this is not found, no point is removed.
     * @return True is successfully updated database
     */
    public boolean delPoint(String Matrix, String Point){
        // For the row with id Matrix
        // Query the points and delete Point in String
        // Update Points with String
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor resource = db.query(TABLE_NAME, new String[]{"*"}, MATRIX_COL_NAME + "=?", new String[]{Matrix}, null, null, null); // get all columns
        resource.moveToFirst();
        String points = resource.getString(resource.getColumnIndex(PTS_COL_NAME));
        String newPoints = "";
        // Split into (x1,x2) groups
        // Long pattern matches (+2.3,-3.2), or (+2,3), or (-3,-.4), etc
        Pattern pattern = Pattern.compile("[(][+-]?[0-9]*[.]?[0-9]+[,][+-]?[0-9]*[.]?[0-9]+[)]");
        Matcher matcher = pattern.matcher(points);
        // Only put in the groups that don't match the one we are removing
        while (matcher.find()) {
            String group = points.substring(matcher.start(), matcher.end());
            if (!group.equals(Point)) {
                newPoints += group + ",";
            }
        }
        // Remove trailing , if necessary
        if (!newPoints.equals("") && newPoints.charAt(newPoints.length()-1) == ',')
            newPoints = newPoints.substring(0,newPoints.length()-1);
        ContentValues contents = new ContentValues();
        contents.put(PTS_COL_NAME, newPoints);
        db.update(TABLE_NAME, contents, MATRIX_COL_NAME + " = ?", new String[]{Matrix});
        return true;
    }
}
