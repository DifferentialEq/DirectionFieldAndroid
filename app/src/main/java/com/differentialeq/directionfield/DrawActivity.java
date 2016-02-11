package com.differentialeq.directionfield;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.differentialeq.directionfield.MatrixMath.Complex;
import com.differentialeq.directionfield.MatrixMath.TwoDimMatrix;
import java.util.ArrayList;
import java.util.concurrent.Delayed;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import tourguide.tourguide.Overlay;
import tourguide.tourguide.Sequence;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;

public class DrawActivity extends AppCompatActivity implements InputsToPlaneFragment.InputsToPlaneListener, PhasePlane.PointListener {

    private int StatusBarHeight;
    private RelativeLayout RelativePhasePlane;
    private PhasePlane PhasePlaneFragment;
    private InputsToPlaneFragment InputsFragment;
    private TwoDimMatrix MatrixBase;
    private TwoDimMatrix Eig1Matrix;
    private TwoDimMatrix Eig2Matrix;
    private String MatrixName;
    private DBManager DBMgr;
    private DisplayMetrics metrics;
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private boolean allowScreenShots = false;

    public TourGuide mTutorialHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(DrawActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(DrawActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Please Allow DirectionField to use your storage", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(DrawActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE); // stores either 0 or 1 from response by user
            }
        } else {
            allowScreenShots = true;
        }

        DBMgr = new DBManager(this);
        super.onCreate(savedInstanceState);
        // Theme
        setTheme(android.R.style.Theme_Holo);
        setContentView(R.layout.activity_draw);
        RelativePhasePlane = (RelativeLayout) findViewById(R.id.RelativePhasePlane);
        PhasePlaneFragment = (PhasePlane) getSupportFragmentManager().findFragmentById(R.id.PhasePlaneFragment);

        // Set up Phase Plane View Dimensions
        View PhasePlaneView = PhasePlaneFragment.getView();
        int Dimension = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getConfiguration().screenWidthDp, getResources().getDisplayMetrics());
        RelativeLayout.LayoutParams PhaseLayout = new RelativeLayout.LayoutParams(Dimension, Dimension);
        PhasePlaneView.setLayoutParams(PhaseLayout);
        PhasePlaneView.requestLayout();
        PhasePlaneFragment.setDimension(Dimension);

        // Set up Inputs Fragment related to Phase plane fragment dimensions
        InputsFragment = (InputsToPlaneFragment) getSupportFragmentManager().findFragmentById(R.id.inputFragment);
        int DimensionInput = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getConfiguration().screenHeightDp, getResources().getDisplayMetrics());
        View InputsView = InputsFragment.getView();
        RelativeLayout.LayoutParams InputsLayout = new RelativeLayout.LayoutParams(Dimension, DimensionInput-Dimension);
        InputsLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativePhasePlane.getId());
        InputsView.setLayoutParams(InputsLayout);
        InputsView.requestLayout();
        InputsFragment.setAllowScreenshot(allowScreenShots);

        Bundle Matrix = getIntent().getExtras();
        Complex EigenValue1 = new Complex(Matrix.getString("Eig1"));
        Complex EigenValue2 = new Complex(Matrix.getString("Eig2"));

        //get bar height from device resource
        StatusBarHeight = getStatusBarHeight();

        // Build the 2x2 Matrix of Complex objects
        Pattern MatrixPattern = Pattern.compile("([+-]?[0-9]*[.]?[0-9]+[+-][0-9]*[.]?[0-9]+[i])");
        MatrixName = Matrix.getString("Name") + ""; // At least "" if nothing there in the getString
        Matcher MatrixMatcher = MatrixPattern.matcher(MatrixName);
        MatrixBase = new TwoDimMatrix();
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
        InputsFragment.nameMatrix(MatrixName);// send this matrix to the inputs

        String EigVec1 = Matrix.getString("EigVec1") + "";
        String EigVec2 = Matrix.getString("EigVec2") + "";

        // Build the Matrix for the Eigenvector 1
        MatrixMatcher = MatrixPattern.matcher(EigVec1);
        Eig1Matrix = new TwoDimMatrix(new Complex(0), new Complex(0));
        i = 1;
        j = 1;
        while (MatrixMatcher.find()) {
            Eig1Matrix.setValue(i, j, new Complex(MatrixMatcher.group(1)));
            i++;
        }

        // Build the Matrix for the Eigenvector 2
        MatrixMatcher = MatrixPattern.matcher(EigVec2);
        Eig2Matrix = new TwoDimMatrix(new Complex(0), new Complex(0));
        i = 1;
        while (MatrixMatcher.find()) {
            Eig2Matrix.setValue(i, j, new Complex(MatrixMatcher.group(1)));
            i++;
        }

        // Send the plane the information
        PhasePlaneFragment.receiveMatrix(MatrixBase, Eig1Matrix, Eig2Matrix, EigenValue1, EigenValue2);

        // Points array list is in form (1.1,-232.2) or (2,+0) or (+1.1,-2)
        // Now add all points through the Phase Plane Fragment
        ArrayList<String> PointsArrayList = Matrix.getStringArrayList("Points");
        ArrayList<String> PointsNotSelected = Matrix.getStringArrayList("PointsNotSelected");

        for (String point : PointsArrayList) {
            Pattern Xs = Pattern.compile("([+-]?[0-9]*[.]?[0-9]+)");
            Matcher XMatcher = Xs.matcher(point);
            String[] pair = new String[2];
            i = 0;
            while (XMatcher.find()) {
                pair[i] = XMatcher.group(1);
                i++;
            }
            float x[] = new float[]{Float.parseFloat(pair[0]), Float.parseFloat(pair[1])};
            PhasePlaneFragment.addPoint(x[0], x[1]);
            InputsFragment.addPoint(point);
        }
        // Tell the phase plane how many points there are, graphed or not so that we don't go overboard
        PhasePlaneFragment.setNumPoints(PointsArrayList.size(),PointsNotSelected.size());

        if (!DBMgr.getTutorialFinished()) {
            DBMgr.setTutorialFinished(true);

            Animation mEnterAnimation, mExitAnimation;

            //Starting Tutorial sequence
            mEnterAnimation = new AlphaAnimation(0f, 1f);
            mEnterAnimation.setDuration(600);
            mEnterAnimation.setFillAfter(true);

            mExitAnimation = new AlphaAnimation(1f, 0f);
            mExitAnimation.setDuration(600);
            mExitAnimation.setFillAfter(true);

            Button Button1 = (Button) findViewById(R.id.addTrajectoryPoint);
            Button Button2 = (Button) findViewById(R.id.displayVectorsButton);
            ImageButton Button3 = (ImageButton) findViewById(R.id.tweetButton);
            ImageButton Button4 = (ImageButton) findViewById(R.id.cameraButton);

            TourGuide tourGuide1 = TourGuide.init(this)
                    .setToolTip(new ToolTip()
                                    .setTitle("Hint")
                                    .setDescription("Use this button to Add points manually")
                                            //.setBackgroundColor(Color.parseColor("#EE2c3e50"))
                                    .setGravity(Gravity.BOTTOM | Gravity.RIGHT)
                    )
                    .setOverlay(new Overlay()
                            .setEnterAnimation(mEnterAnimation)
                            .setExitAnimation(mExitAnimation)
                            .setBackgroundColor(Color.parseColor("#EE2c3e50")))
                            // note that there is not Overlay here, so the default one will be used
                    .playLater(Button1);

            TourGuide tourGuide2 = TourGuide.init(this)
                    .setToolTip(new ToolTip()
                                    .setTitle("Hint")
                                    .setDescription("Hit this button to display direction field")
                                            //.setBackgroundColor(Color.parseColor("#EE2c3e50"))
                                    .setGravity(Gravity.BOTTOM | Gravity.LEFT)
                    )
                    .setOverlay(new Overlay()
                                    .setEnterAnimation(mEnterAnimation)
                                    .setExitAnimation(mExitAnimation)
                                    .setBackgroundColor(Color.parseColor("#EE2c3e50"))
                                    .setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mTutorialHandler.next();
                                        }
                                    })
                    )
                    .playLater(Button2);

            TourGuide tourGuide3 = TourGuide.init(this)
                    .setToolTip(new ToolTip()

                                    .setTitle("Hint")
                                    .setDescription("Tweet your data with this button")
                                    .setGravity(Gravity.CENTER | Gravity.LEFT)
                    )
                    .setOverlay(new Overlay()
                            .setEnterAnimation(mEnterAnimation)
                            .setExitAnimation(mExitAnimation)
                            .setBackgroundColor(Color.parseColor("#EE2c3e50")))
                            // note that there is not Overlay here, so the default one will be used
                    .playLater(Button3);


            TourGuide tourGuide4 = TourGuide.init(this)
                    .setToolTip(new ToolTip()
                                    .setTitle("Hint")
                                    .setDescription("Take Picture of your graph")
                                    .setGravity(Gravity.CENTER | Gravity.LEFT)
                    )
                    .setOverlay(new Overlay()
                            .setEnterAnimation(mEnterAnimation)
                            .setExitAnimation(mExitAnimation)
                            .setBackgroundColor(Color.parseColor("#EE2c3e50")))
                            // note that there is not Overlay here, so the default one will be used
                    .playLater(Button4);

            TourGuide tourGuide5 = TourGuide.init(this)
                    .setToolTip(new ToolTip()
                                    .setTitle("Hint")
                                    .setDescription("Double tap to add trajectory points\n" +
                                            "Pinch to zoom and slide to pan")
                                    .setGravity(Gravity.BOTTOM)
                    )
                    .setOverlay(new Overlay()
                            .setEnterAnimation(mEnterAnimation)
                            .setExitAnimation(mExitAnimation)
                            .setStyle(Overlay.Style.Rectangle)
                            .setBackgroundColor(Color.parseColor("#EE2c3e50")))

                            // note that there is not Overlay here, so the default one will be used
                    .playLater(PhasePlaneFragment.getView());


            Sequence sequence = new Sequence.SequenceBuilder()
                    .add(tourGuide1, tourGuide2, tourGuide3, tourGuide4, tourGuide5)
                    .setDefaultOverlay(new Overlay()
                                    .setEnterAnimation(mEnterAnimation)
                                    .setExitAnimation(mExitAnimation)
                                    .setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mTutorialHandler.next();
                                        }
                                    })
                    )
                    .setDefaultPointer(null)
                    .setContinueMethod(Sequence.ContinueMethod.OverlayListener)
                    .build();

            mTutorialHandler = TourGuide.init(this).playInSequence(sequence);
        }

    }

    /**
     * We are required to ask for permission to write to the user's storage (external only)
     * When we ask this the screen pops up to show allow/deny. If allowed
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // Yay we can use the SD card
                    InputsFragment.setAllowScreenshot(true);
                } else {
                    // we were not allowed to use the SD card
                    InputsFragment.setAllowScreenshot(false);
                }
            }
        }
    }


    /**
     * Messenger which sends input from Input Fragment to Phase Plane Fragment. Redraws the Fragment
     * @param axes boolean True to display False to Remove
     * @param grid boolean
     * @param ticks boolean
     * @param border boolean
     */
    @Override
    public void alterStyle(boolean axes, boolean grid, boolean ticks, boolean border, boolean numbers) {
        PhasePlaneFragment.changeStyle(axes,grid,ticks,border,numbers);
    }

    /**
     * Messenger which sends points from Input Fragment to the PhasePlane Fragment also add them to the DB
     * @param x1pt
     * @param x2pt
     */
    @Override
    public void addPointToGraph(float x1pt, float x2pt) {
        // Add some restrictions to the type of point we can solve for!
        // We don't want to do calculations on huge numbers
        if (x1pt > -1000 && x1pt < 1000 && x2pt > -1000 && x2pt < 1000) {
            // Handle writing points to the DB
            if (PhasePlaneFragment.addPoint(x1pt, x2pt)) { // Only prevents adding one point that was checked in this view
                String PointString = "(" + String.format("%.2f", x1pt) + "," + String.format("%.2f",x2pt) + ")";
                DBMgr.addPoint(MatrixName, PointString);
                DBMgr.close();
                InputsFragment.addPoint(PointString);
            }
        }
    }

    /**
     * Calls to Phase Plane Fragment's generate Vector field function
     */
    @Override
    public void genVectorField(){
        PhasePlaneFragment.generateVectorField();

    }

    //get the status bar height from device resource
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    // Clean up memory (possibly, though doesn't seem to work)
    public void clearMemory(){
        PhasePlaneFragment.clearMemory();
    }

    /**
     * Just a helper to clean up memory and return to the select a matrix screen.
     */
    @Override
    public void goBack() {
        final Intent goToMatrices = new Intent(this, SelectMatrixOrNew.class);
        PhasePlaneFragment.clearMemory();
        PhasePlaneFragment.onDestroy();
        InputsFragment.onDestroy();
        goToMatrices.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMatrices);
    }

    /**
     * Opens the Twitter Activity when the button is pushed on the Inputs To Phase Plane fragment. Also submitted
     * is the URI of the file just saved when the button is clicked. This image is handled in the media upload to
     * Twitter.
     * @param UriString a string format of the Uri location of the screen shot just taken of phase plane.
     */
    @Override
    public void openTwitter(String UriString) {
        final Intent tweet= new Intent(this, Twitter.class);
        tweet.putExtra("imageUri", UriString);
        tweet.putExtra("MatrixName", MatrixName);
        startActivity(tweet);
    }

    @Override
    public void onPause() {
        super.onPause();
        clearMemory();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        clearMemory();
        goBack();
    }
}
