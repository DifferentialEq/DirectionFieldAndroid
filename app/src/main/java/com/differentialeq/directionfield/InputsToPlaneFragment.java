package com.differentialeq.directionfield;
/**
 * (c)2015 DifferentialEq.com
 * Eli Selkin, Chang Zhang, Ahmed Ali, Ben Chen
 */


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class InputsToPlaneFragment extends Fragment {

    InputsToPlaneListener activityCommander;

    private static CheckBox showGrid;
    private static CheckBox showAxes;
    private static CheckBox showTicks;
    private static CheckBox showBorder;
    private static CheckBox showNumbers;
    private static TextView DisplayHeading;
    private static Button addPoint;
    private static EditText x1;
    private static EditText x2;
    private static String Points;
    private static TextView PointView;
    private static TextView MatrixView;
    private static Button displayVectorsButton;
    private static LinearLayout PointsLayoutInput;
    private static ScrollView ScrollPoints;
    private GestureDetector GD;
    private Intent goToMatrices;
    private ImageButton tweetButton;
    private ImageButton cameraButton;
    private boolean allowScreenShot;

    public void nameMatrix(String MatrixBase) {
        String Matrix = MatrixBase;
        MatrixView.setText("Matrix: " + Matrix);
    }

    public void addPoint(String pointtoadd) {
        TextView PointToAdd = new TextView(this.getContext());
        PointToAdd.setText(pointtoadd);
        PointToAdd.setTextSize(10);
        PointsLayoutInput.addView(PointToAdd);
        ScrollPoints.post(new Runnable() {
            @Override
            public void run() {
                ScrollPoints.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
    public void setAllowScreenshot(boolean state){
        allowScreenShot = state;
    }

    /**
     * Following multiple suggestions on stackoverflow.com this seems like the best result.
     * In modifications we select the fragment ONLY to get the drawing cache of (who wants to see the
     * inputs fragment?). We take a snapshot and save it at the date-time.png in the format of PNG
     * (through the FileOutputStream which handles the conversion at 100 quality with Bitmap's compression)
     *
     * Toasts show the location of the file created or that a photo was not created.
     * Pratik on http://stackoverflow.com/questions/8179064/how-to-save-the-drawing-canvas-in-android
     * @return
     */
    public Uri takeSnapshot(){
        View phase = (View)getActivity().findViewById(R.id.PhasePlaneFragment);
        phase.setDrawingCacheEnabled(true);
        phase.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap image = phase.getDrawingCache();
        String path = Environment.getExternalStorageDirectory().getPath();
        //File path = new File("/storage/emulated/0/");
        new File(path+"/DirectionField/").mkdirs();
        String simpleFormat = "yyyy-MM-dd-HH-mm-ss";
        SimpleDateFormat sdf = new SimpleDateFormat(simpleFormat, Locale.US);
        String filename = sdf.format(new Date()).toString(); // make sure the format is correct to collide the fewest photos (yes down to the second)
        FileOutputStream ostream;
        String fileLocation = path + "/DirectionField/" + filename + ".png";
        File imagefile = new File(fileLocation);
        try {
            imagefile.createNewFile();
            ostream = new FileOutputStream(imagefile);
            image.compress(Bitmap.CompressFormat.PNG, 100, ostream);
            ostream.flush();
            ostream.close();
            Toast.makeText(getContext(), "Image saved as: " + filename, Toast.LENGTH_SHORT).show();
            Uri imageUri = Uri.fromFile(imagefile); // here we make the URI out of the file path
            return imageUri;
        } catch (Exception e){
            Toast.makeText(getContext(), "Cannot create photo", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.inputs_to_plane, container, false);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                GD.onTouchEvent(event);
                return true;
            }
        });
        ScrollPoints = (ScrollView) view.findViewById(R.id.scrollViewPoints);
        showAxes = (CheckBox) view.findViewById(R.id.showAxes);
        showGrid = (CheckBox) view.findViewById(R.id.showGrid);
        showTicks = (CheckBox) view.findViewById(R.id.showTickMarks);
        showBorder = (CheckBox) view.findViewById(R.id.showBorder);
        showNumbers = (CheckBox) view.findViewById(R.id.showNumbers);
        DisplayHeading = (TextView) view.findViewById(R.id.DisplayHeading);
        addPoint = (Button) view.findViewById(R.id.addTrajectoryPoint);
        x1 = (EditText) view.findViewById(R.id.x1Coordinate);
        x2 = (EditText) view.findViewById(R.id.x2Coordinate);
        displayVectorsButton = (Button) view.findViewById(R.id.displayVectorsButton);
        PointView = (TextView) view.findViewById(R.id.PointsName);
        MatrixView = (TextView) view.findViewById(R.id.MatrixName);
        PointsLayoutInput = (LinearLayout) view.findViewById(R.id.PointsLayoutInput);
        tweetButton = (ImageButton) view.findViewById(R.id.tweetButton);
        cameraButton = (ImageButton) view.findViewById(R.id.cameraButton);
        tweetButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Only allow screenshots if we have access to media
             * @param v
             */
            @Override
            public void onClick(View v) {
                if (allowScreenShot) {
                    Uri imageUri = takeSnapshot();
                    activityCommander.openTwitter(imageUri.toString());
                }
            }
        });
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allowScreenShot)
                    takeSnapshot();
            }
        });

        View.OnClickListener GenericCheckBoxClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkboxClicked();
            }
        };
        displayVectorsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityCommander.genVectorField();
            }
        });
        showTicks.setOnClickListener(GenericCheckBoxClick);
        showAxes.setOnClickListener(GenericCheckBoxClick);
        showBorder.setOnClickListener(GenericCheckBoxClick);
        showGrid.setOnClickListener(GenericCheckBoxClick);
        showNumbers.setOnClickListener(GenericCheckBoxClick);


        addPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Only operate if we have text (which can only be inputted as numbers, so we're good)
                if (!x1.getText().toString().equals("") && !x2.getText().toString().equals("")) {
                    activityCommander.addPointToGraph(Float.parseFloat(x1.getText().toString()), Float.parseFloat(x2.getText().toString()));
                    String pointadded = "(" + x1 + "," + x2 + ") Added...";
                    x1.setText("");
                    x2.setText("");
                }
            }
        });

        // Set up where to go when swipped right on the inputs fragment
        GD = new GestureDetector(this.getContext(), new ScrollListener());
        return view;
    }

    public void checkboxClicked() {
        activityCommander.alterStyle(showAxes.isChecked(), showGrid.isChecked(), showTicks.isChecked(), showBorder.isChecked(), showNumbers.isChecked());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            activityCommander = (InputsToPlaneListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString());
        }
    }


    public interface InputsToPlaneListener {
        void alterStyle(boolean axes, boolean grid, boolean ticks, boolean border, boolean numbers);

        void addPointToGraph(float x1pt, float x2pt);

        void clearMemory();

        void genVectorField();

        void goBack();

        void openTwitter(String UriString);
    }

    /**
     * LISTENER CLASS FOR scroll to go back
     */
    public class ScrollListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (distanceX < 0) {
                activityCommander.clearMemory();
                activityCommander.goBack();
            }
            return true;
        }
    }
}
