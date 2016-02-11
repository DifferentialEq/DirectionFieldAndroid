package com.differentialeq.directionfield;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.Sequence;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;

/**
 * This class is devoted to editing the points made available by querying the DB and reading the Points column
 * That information is passed to this class/activity through the SelectMatrix or new class/activity.
 * The ability to repeatedly write to the DB through the DBManager class is present in this class, because
 * You might want to first remove some points and then remove some more.
 */
public class EditPoints extends AppCompatActivity {
    private Button Display;
    private Button DeleteButton;
    private DBManager DBMgr;
    private ArrayList<CheckBox> PointsArrayList;

    //tutorial variable
    public TourGuide displaybuttontour, deletebuttontour;
    private boolean tutorialcleaned = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo);
        setContentView(R.layout.activity_edit_points);
        // Set up to send user to the graph
        final Intent displayPhasePlane = new Intent(this, DrawActivity.class);
        displayPhasePlane.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Connect to the DB so we can delete points if necessary from there
        DBMgr = new DBManager(this);

        // These are the values passed in as Extras to the bundle passed in as an intent
        Bundle MatrixData = getIntent().getExtras(); // Has all the matrix data in Strings
        final LinearLayout PointsLayout = (LinearLayout) findViewById(R.id.PointsLayout);
        final ScrollView ScrollPoints = (ScrollView) findViewById(R.id.ScrollPoints);
        final TextView EditPointsText = (TextView) findViewById(R.id.EditPointsText);
        final String MatrixName = MatrixData.getString("Name");
        final String EigenValue1 = MatrixData.getString("Eig1");
        final String EigenValue2 = MatrixData.getString("Eig2");
        final String EigenVector1 = MatrixData.getString("EigVec1");
        final String EigenVector2 = MatrixData.getString("EigVec2");
        final ArrayList<String> Points = MatrixData.getStringArrayList("Pts");
        final CheckBox CheckAll = new CheckBox(this);
        CheckAll.setText("Check/Uncheck All Boxes");
        PointsLayout.addView(CheckAll);
        PointsArrayList = new ArrayList<>();

        // Set up common button to both with points and without
        Display = new Button(this);
        DeleteButton = new Button(this);
        final Bundle MatrixPoints = new Bundle();

        if (!DBMgr.getTutorialFinished()) {
            //Tutorial for this activity

            // Start of Tutorial for this activity
            Animation animation = new TranslateAnimation(0f, 0f, 200f, 0f);
            animation.setDuration(1000);
            animation.setFillAfter(true);
            animation.setInterpolator(new BounceInterpolator());

            Pointer displaypointer = new Pointer().setColor(Color.parseColor("#3498db")).setGravity(Gravity.CENTER);
            Pointer deletepointer = new Pointer().setColor(Color.parseColor("#3498db")).setGravity(Gravity.CENTER);


            Overlay displayoverlay = new Overlay()
                    .setBackgroundColor(Color.TRANSPARENT)
                    .disableClick(false)
                    .setStyle(Overlay.Style.Rectangle);

            Overlay deleteoverlay = new Overlay()
                    .setBackgroundColor(Color.TRANSPARENT)
                    .disableClick(false)
                    .setStyle(Overlay.Style.Rectangle);


            ToolTip displaytoolTip = new ToolTip()
                    .setTitle("Hint")
                    .setDescription("Display the matrix with selected points")
                    .setTextColor(Color.parseColor("#EE2c3e50"))
                    .setBackgroundColor(Color.parseColor("#3498db"))
                    .setShadow(true)
                    .setGravity(Gravity.BOTTOM)
                    .setEnterAnimation(animation);

            ToolTip deletetoolTip = new ToolTip()
                    .setTitle("Hint")
                    .setDescription("Delete the selected points")
                    .setTextColor(Color.parseColor("#EE2c3e50"))
                    .setBackgroundColor(Color.parseColor("#3498db"))
                    .setShadow(true)
                    .setGravity(Gravity.TOP)
                    .setEnterAnimation(animation);

            deletebuttontour = TourGuide.init(this).with(TourGuide.Technique.Click)
                    .setPointer(deletepointer)
                    .setToolTip(deletetoolTip)
                    .setOverlay(deleteoverlay)
                    .playOn(DeleteButton);

            displaybuttontour = TourGuide.init(this).with(TourGuide.Technique.Click)
                    .setPointer(displaypointer)
                    .setToolTip(displaytoolTip)
                    .setOverlay(displayoverlay)
                    .playOn(Display);
            //End of Tutorial Sequence
        }




        // This ArrayList is used in either condition. In the second, it will be empty
        final ArrayList<String> PointsSelected = new ArrayList<>();
        final ArrayList<String> PointsNotSelected = new ArrayList<>();
        if (Points.size() > 0) {
            // If the value passed in for the Points is not empty (i.e. we have some points)
            // Split the points that are in format (x,y),(x,y) format
            // Then make a checkbox for each Array and also put a reference to that checkbox into an ArrayList
            for (String point : Points) {
                CheckBox newCheckBox = new CheckBox(this); // Make a checkbox for
                newCheckBox.setText(point);
                PointsLayout.addView(newCheckBox);
                newCheckBox.setId(PointsArrayList.size());
                PointsArrayList.add(newCheckBox);
            }

            CheckAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (!DBMgr.getTutorialFinished();
                        clearTutorial();
                    if (CheckAll.isChecked()) {
                        for (CheckBox PointBox : PointsArrayList) {
                            PointBox.setChecked(true);
                        }
                    } else {
                        for (CheckBox PointBox : PointsArrayList) {
                            PointBox.setChecked(false);
                        }
                    }
                }
            });

            // Standard text for the delete button
            DeleteButton.setText("Delete Checked");

            // Action when the delete button is clicked
            DeleteButton.setOnClickListener(new View.OnClickListener() {
                ArrayList<String> PointsToRemove;

                @Override
                public void onClick(View v) {
//                    if (!DBMgr.getTutorialFinished())
                        clearTutorial();

                    PointsToRemove = new ArrayList<String>();
                    for (CheckBox c : PointsArrayList) {
                        if (c.isChecked()) {
                            DBMgr.delPoint(MatrixName, c.getText().toString());
                            c.setChecked(false);
                            PointsLayout.removeView(c); // If we're deleting it from the DB we should delete it from the list too
                            PointsToRemove.add(c.getText().toString());
                        }
                    }
                    // Trying to do this in the above loop became impossible since we were working with two lists
                    for (String cbToRemove : PointsToRemove) {
                        for (int i = 0; i < PointsArrayList.size(); i++) {
                            if (PointsArrayList.get(i).getText().toString().equals(cbToRemove)) {
                                PointsArrayList.remove(i);
                                break;
                            }
                        }
                    }

                    // If we have removed all points then Make it a little more obvious that you can't select
                    // Draw the graph with selected points! Remove te button from the view that lets you delete points
                    // and change the text of the display button
                    // The button still has the clickListener which goes through looking for checked boxes.
                    if (PointsArrayList.size() == 0) {
                        EditPointsText.setText("Display Graph");
                        Display.setText("Display Graph");
                        PointsLayout.removeView(DeleteButton);
                    }
                }
            });
            // the standard display button when we've got points
            Display.setText("Display with selected Points");

            // What to do when the Display button is clicked
            // Basically go through the list of points (if there is one)
            // and gather all points that were checked
            Display.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Add checked points to the PointsSelected ArrayList so that we can pass it as a list to the bundle
//                    if (!DBMgr.getTutorialFinished())
                        clearTutorial();

                    for (CheckBox c : PointsArrayList) {
                        if (c.isChecked()) {
                            c.setChecked(false);
                            PointsSelected.add(c.getText().toString());
                        } else {
                            PointsNotSelected.add(c.getText().toString());
                        }
                    }
                    // Since we will be starting the graphing activity, we need to pass all the Matrix information
                    // We will put it into an actual Matrix object once we get to that activity
                    displayPhasePlane.putStringArrayListExtra("Points", PointsSelected);
                    displayPhasePlane.putStringArrayListExtra("PointsNotSelected", PointsNotSelected);
                    displayPhasePlane.putExtra("Name", MatrixName);
                    displayPhasePlane.putExtra("Eig1", EigenValue1);
                    displayPhasePlane.putExtra("Eig2", EigenValue2);
                    displayPhasePlane.putExtra("EigVec1", EigenVector1);
                    displayPhasePlane.putExtra("EigVec2", EigenVector2);
                    startActivity(displayPhasePlane);
                }
            });

            // Just to make it more readable, add space between the display and the delete buttons
            Space Spacer = new Space(this);
            Spacer.setMinimumHeight(20);
            PointsLayout.addView(DeleteButton);
            PointsLayout.addView(Spacer);
            }else{
                PointsLayout.removeView(CheckAll); // no points to check

            // If we have no points to select, all we do is show the display button.
                EditPointsText.setText("Display Graph");
            // There are no points to select, so we just pass the empty ArrayList
            Display.setText("Display Graph");
                Display.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        displayPhasePlane.putStringArrayListExtra("Points", PointsSelected);
                        displayPhasePlane.putStringArrayListExtra("PointsNotSelected", PointsNotSelected);
                        displayPhasePlane.putExtra("Name", MatrixName);
                        displayPhasePlane.putExtra("Eig1", EigenValue1);
                        displayPhasePlane.putExtra("Eig2", EigenValue2);
                        displayPhasePlane.putExtra("EigVec1", EigenVector1);
                        displayPhasePlane.putExtra("EigVec2", EigenVector2);
                        startActivity(displayPhasePlane);
                }
            });
        }
        // Every view of this activity has a display, they just act differently
        PointsLayout.addView(Display);


    }

    // clear up the tutorial on the app
    private void clearTutorial()
    {
        if(tutorialcleaned == false) {
            if (displaybuttontour != null)
                displaybuttontour.cleanUp();
            if (deletebuttontour != null)
                deletebuttontour.cleanUp();
            tutorialcleaned = true;
        }
    }

}
