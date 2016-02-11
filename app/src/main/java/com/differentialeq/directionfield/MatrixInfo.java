package com.differentialeq.directionfield;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;

import com.differentialeq.directionfield.MatrixMath.Complex;
import com.differentialeq.directionfield.MatrixMath.TwoDimMatrix;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;

public class MatrixInfo extends AppCompatActivity {
    private GestureDetector GD;
    private Intent matrixListScreen ;

    //tutorial variable
    public TourGuide mTutorialHandler, mTutorialHandler2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GD = new GestureDetector(this, new ScrollListener());

        // Theme
        setTheme(android.R.style.Theme_Holo);

        setContentView(R.layout.activity_matrix_info);
        // Make the connection to the database
        final DBManager DBmgr = new DBManager(this);

        Bundle Matrix = getIntent().getExtras();

        //Set up to be able to pass Matrix data to this intended activity
        final Intent selectMatrixIntent = new Intent(this, EditPoints.class);
        selectMatrixIntent.putExtra("Name", Matrix.getString("Name"));
        selectMatrixIntent.putExtra("Eig1", Matrix.getString("Eig1"));
        selectMatrixIntent.putExtra("Eig2", Matrix.getString("Eig2"));
        selectMatrixIntent.putExtra("EigVec1", Matrix.getString("EigVec1"));
        selectMatrixIntent.putExtra("EigVec2", Matrix.getString("EigVec2"));
        selectMatrixIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        ArrayList<String> points = new ArrayList<>();
        String PointsList = Matrix.getString("Pts");
        Pattern patternPoint = Pattern.compile("([(][+-]?[0-9]*[.][0-9]+[,][+-]?[0-9]*[.][0-9]+[)])");
        Matcher matchPoint = patternPoint.matcher(PointsList);
        while (matchPoint.find())
            points.add(matchPoint.group(1));
        selectMatrixIntent.putStringArrayListExtra("Pts", points);


        // Go back and refresh from matrix list
        matrixListScreen = new Intent(this, SelectMatrixOrNew.class);

        Complex EigenValue1 = new Complex(Matrix.getString("Eig1"));
        Complex EigenValue2 = new Complex(Matrix.getString("Eig2"));

        // Build the 2x2 Matrix of Complex objects
        Pattern MatrixPattern = Pattern.compile("([+-]?[0-9]*[.]?[0-9]+[+-][0-9]*[.]?[0-9]+[i])");
        final String MatrixName = Matrix.getString("Name") + ""; // At least "" if nothing there in the getString
        Matcher MatrixMatcher = MatrixPattern.matcher(MatrixName);
        TwoDimMatrix MatrixBase = new TwoDimMatrix();
        int i = 1;
        int j = 1;
        while (MatrixMatcher.find()) {
            MatrixBase.setValue(i, j, new Complex(MatrixMatcher.group(0)));
            if (j == 3) {
                j = 0;
                i++;
            }
            j++;
        }

        String EigVec1 = Matrix.getString("EigVec1") + "";
        String EigVec2 = Matrix.getString("EigVec2") + "";

        // Build the Matrix for the Eigenvector 1
        MatrixMatcher = MatrixPattern.matcher(EigVec1);
        TwoDimMatrix Eig1Matrix = new TwoDimMatrix(new Complex(0), new Complex(0));
        i = 1;
        j = 1;
        while (MatrixMatcher.find()) {
            Eig1Matrix.setValue(i, j, new Complex(MatrixMatcher.group(1)));
            i++;
        }

        // Build the Matrix for the Eigenvector 2
        MatrixMatcher = MatrixPattern.matcher(EigVec2);
        TwoDimMatrix Eig2Matrix = new TwoDimMatrix(new Complex(0), new Complex(0));
        i = 1;
        while (MatrixMatcher.find()) {
            Eig2Matrix.setValue(i, j, new Complex(MatrixMatcher.group(1)));
            i++;
        }

        // Put in the details into the Text Views
        TextView A11Text = (TextView) findViewById(R.id.A11Text);
        TextView A12Text = (TextView) findViewById(R.id.A12Text);
        TextView A21Text = (TextView) findViewById(R.id.A21Text);
        TextView A22Text = (TextView) findViewById(R.id.A22Text);
        A11Text.setText(MatrixBase.getValue(1, 1).toString());
        A12Text.setText(MatrixBase.getValue(1, 2).toString());
        A21Text.setText(MatrixBase.getValue(2, 1).toString());
        A22Text.setText(MatrixBase.getValue(2, 2).toString());

        TextView E11 = (TextView) findViewById(R.id.E11);
        TextView E12 = (TextView) findViewById(R.id.E12);
        TextView E21 = (TextView) findViewById(R.id.E21);
        TextView E22 = (TextView) findViewById(R.id.E22);
        E11.setText(Eig1Matrix.getValue(1, 1).toString());
        E12.setText(Eig1Matrix.getValue(2, 1).toString());
        E21.setText(Eig2Matrix.getValue(1, 1).toString());
        E22.setText(Eig2Matrix.getValue(2, 1).toString());

        TextView EigValue1 = (TextView) findViewById(R.id.EigValue1);
        TextView EigValue2 = (TextView) findViewById(R.id.EigValue2);
        EigValue1.setText(EigenValue1.toString());
        EigValue2.setText(EigenValue2.toString());
        EigValue1.setTextColor(Color.parseColor("#ffa500"));
        EigValue2.setTextColor(Color.parseColor("#ffa500"));

        // Here we request the DBManager delete the system from the list then return to the previous
        // selectMatrixOrNew activity which will refresh the list by polling the DB once more for the
        // new list of matrices.
        Button Delete = (Button) findViewById(R.id.deleteMatrixButton);
        Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBmgr.delSystem(MatrixName);
                startActivity(matrixListScreen);
            }
        });

        // To continue to the Edit Points Screen
        Button EditPoints = (Button) findViewById(R.id.editPointsButton);
        EditPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(selectMatrixIntent);
            }
        });

        if (!DBmgr.getTutorialFinished()) {

            //Tutorial sequence method 2
            Animation animation = new TranslateAnimation(0f, 0f, 200f, 0f);
            animation.setDuration(1000);
            animation.setFillAfter(true);
            animation.setInterpolator(new BounceInterpolator());

            Pointer editbuttonpointer = new Pointer().setColor(Color.parseColor("#3498db")).setGravity(Gravity.CENTER);
            Pointer deletebuttonpointer = new Pointer().setColor(Color.parseColor("#3498db")).setGravity(Gravity.CENTER);


            Overlay editbuttonoverlay = new Overlay()
                    .setBackgroundColor(Color.TRANSPARENT)
                    .disableClick(false)
                    .setStyle(Overlay.Style.Rectangle);

            Overlay deletebuttonoverlay = new Overlay()
                    .setBackgroundColor(Color.TRANSPARENT)
                    .disableClick(false)
                    .setStyle(Overlay.Style.Rectangle);


            ToolTip editbuttontoolTip = new ToolTip()
                    .setTitle("Hint")
                    .setDescription("Proceed with the Matrix")
                    .setTextColor(Color.parseColor("#EE2c3e50"))
                    .setBackgroundColor(Color.parseColor("#3498db"))
                    .setShadow(true)
                    .setGravity(Gravity.TOP)
                    .setEnterAnimation(animation);

            ToolTip deletebuttontoolTip = new ToolTip()
                    .setTitle("Hint")
                    .setDescription("Discard Matrix and go back")
                    .setTextColor(Color.parseColor("#EE2c3e50"))
                    .setBackgroundColor(Color.parseColor("#3498db"))
                    .setShadow(true)
                    .setGravity(Gravity.TOP)
                    .setEnterAnimation(animation);

            TourGuide EditButtonTourHandler = TourGuide.init(this).with(TourGuide.Technique.Click)
                    .setPointer(editbuttonpointer)
                    .setToolTip(editbuttontoolTip)
                    .setOverlay(editbuttonoverlay)
                    .playOn(EditPoints);

            TourGuide deleteButtonTourHandler = TourGuide.init(this).with(TourGuide.Technique.Click)
                    .setPointer(deletebuttonpointer)
                    .setToolTip(deletebuttontoolTip)
                    .setOverlay(deletebuttonoverlay)
                    .playOn(Delete);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return GD.onTouchEvent(event);
    }

    /**
     * LISTENER CLASS FOR
     */
    public class ScrollListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (distanceX < 0) {
                startActivity(matrixListScreen);
            }
            return true;
        }
    }
}
