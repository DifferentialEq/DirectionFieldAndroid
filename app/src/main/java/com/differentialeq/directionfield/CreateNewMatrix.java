package com.differentialeq.directionfield;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Toast;

import com.differentialeq.directionfield.MatrixMath.Complex;
import com.differentialeq.directionfield.MatrixMath.MatrixMath;
import com.differentialeq.directionfield.MatrixMath.TwoDimMatrix;

import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;

public class CreateNewMatrix extends AppCompatActivity {
    TwoDimMatrix currentMatrix;
    private DBManager DBMgr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent selectMatrixIntent = new Intent(this, MatrixInfo.class);

        // Connect to the DB so we can delete points if necessary from there
        DBMgr = new DBManager(this);

        // Theme
        setTheme(android.R.style.Theme_Holo);

        setContentView(R.layout.activity_create_new_matrix);
        // Get control over the grid Layout
        final GridLayout MatrixLayout = (GridLayout) findViewById(R.id.EnterMatrixLayout);

        // Get control over the elements of the matrix
        final EditText a11 = (EditText)findViewById(R.id.a11);
        final EditText a12 = (EditText)findViewById(R.id.a12);
        final EditText a21 = (EditText)findViewById(R.id.a21);
        final EditText a22 = (EditText)findViewById(R.id.a22);

        // Set up the Button for onClickListener
        Button CalculateButton = (Button)findViewById(R.id.computeMatrixButton);
        CalculateButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Double.parseDouble(a11.getText().toString());
                            Double.parseDouble(a12.getText().toString());
                            Double.parseDouble(a21.getText().toString());
                            Double.parseDouble(a22.getText().toString());
                        } catch(Exception e){
                            return;
                        }
                        currentMatrix = new TwoDimMatrix(
                                new Complex(Double.parseDouble(a11.getText().toString())),
                                new Complex(Double.parseDouble(a12.getText().toString())),
                                new Complex(Double.parseDouble(a21.getText().toString())),
                                new Complex(Double.parseDouble(a22.getText().toString())),
                                new Complex(0), new Complex(0));

                        // Now we've created the matrix.
                        // We need to add it it to the DB first
                        //Complex[] EigenValues = new Complex[]{EigenValues0[1],EigenValues0[0]}; // Something happens here Where EigenVectors switches the values so to correspond, we need this.
                        try {
                            TwoDimMatrix EVs = new TwoDimMatrix(new Complex(0), new Complex(0));
                            TwoDimMatrix[] EigenVectors = MatrixMath.EigenVectors(currentMatrix, EVs);
                            Complex[] EigenValues = new Complex[] {EVs.getValue(1,1), EVs.getValue(2,1)};
                                /*
                                if (EigenValues[0].getrValue() > 5 || EigenValues[0].getiValue() > 4 ||
                                    EigenValues[1].getrValue() > 5 || EigenValues[1].getiValue() > 4 ||
                                    EigenValues[0].getrValue() < -5 || EigenValues[0].getiValue() < -4 ||
                                    EigenValues[1].getrValue() < -5 || EigenValues[1].getiValue() < -4)
                                throw new RuntimeException("EigenValue magnitude too large");
                                */
                            DBMgr.addSystem(currentMatrix.toString(), EigenValues[0].toString(), EigenValues[1].toString(), EigenVectors[0].toString(), EigenVectors[1].toString(), "");
                            selectMatrixIntent.putExtra("Name", currentMatrix.toString());
                            selectMatrixIntent.putExtra("Eig1", EigenValues[0].toString());
                            selectMatrixIntent.putExtra("Eig2", EigenValues[1].toString());
                            selectMatrixIntent.putExtra("EigVec1", EigenVectors[0].toString());
                            selectMatrixIntent.putExtra("EigVec2", EigenVectors[1].toString());
                            selectMatrixIntent.putExtra("Pts", "");
                            selectMatrixIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(selectMatrixIntent);
                        } catch (RuntimeException r){
                            Toast.makeText(CreateNewMatrix.this, "Could not create that matrix. Eigenvalues out of bounds", Toast.LENGTH_SHORT).show();
                        }
                        // Then we need to pass this information to Edit Points.

                        // It will show no points, but it will


                    }
                }
        );
        if (!DBMgr.getTutorialFinished() ) {
            //Tutorial for this activity
            Animation animation = new TranslateAnimation(0f, 0f, 200f, 0f);
            animation.setDuration(1000);
            animation.setFillAfter(true);
            animation.setInterpolator(new BounceInterpolator());

            Pointer pointer = new Pointer().setColor(Color.parseColor("#EE2c3e50")).setGravity(Gravity.CENTER);


            Overlay overlay = new Overlay()
                    .setBackgroundColor(Color.TRANSPARENT)
                    .disableClick(false)
                    .setStyle(Overlay.Style.Rectangle);


            ToolTip toolTip = new ToolTip()
                    .setTitle("Hint")
                    .setDescription("Enter a 2 x 2 matrix and press Compute Matrix to calculate it")
                    .setBackgroundColor(Color.parseColor("#EE2c3e50"))
                    .setShadow(true)
                    .setGravity(Gravity.BOTTOM)
                    .setEnterAnimation(animation);

            TourGuide mTourGuideHandler = TourGuide.init(this).with(TourGuide.Technique.Click)
                    .setPointer(pointer)
                    .setToolTip(toolTip)
                    .setOverlay(overlay)
                    .playOn(CalculateButton);
            // Set up a Button so we can Edit the Matrix and solve again
        }
        //Make the TwoDimMatrix


    }

}
