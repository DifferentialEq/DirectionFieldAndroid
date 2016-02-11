package com.differentialeq.directionfield;
/**
 * (c)2015 DifferentialEq.com
 * Eli Selkin, Chang Zhang, Ahmed Ali, Ben Chen
 */

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import java.util.ArrayList;

import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;

public class SelectMatrixOrNew extends AppCompatActivity {
    private ListView mainListView; // The primary interaction with this activity will be through elements of this list
    private DBManager DBmgr;  //Allow class to connect to the database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call super class
        super.onCreate(savedInstanceState);

        // Style
        setTheme(android.R.style.Theme_Holo);

        // Assign the content view from the XML file containing the layout
        setContentView(R.layout.activity_select_matrix_or_new);

        // Make the connection to the database
        final DBManager DBmgr = new DBManager(this);

        if (DBmgr.getMatrixNames().size() == 0)
            DBmgr.setTutorialFinished(false);

        //Just To Test the DB, requires a fresh install to rewrite this row to the DB
        DBmgr.addSystem("[[+1+0i,+1-0i,0.0-0.0i],[4-0i,+1.0+0i,0.0+0.0i]]", "-1.0+0.0i", "3.0+0i", "[[1.0+0.0i],[-2.0+0.0i]]", "[[1.0+0i],[2+0i]]", "(-.33,.19),(.52,.12),(.05,-.17),(0.17,-0.31),(-.04,.13),(-.2,0.0)");

        // Set up intended activity to be started if user clicks create new matrix
        final Intent createNewMatrixIntent = new Intent(this, CreateNewMatrix.class);

        //Set up to be able to pass Matrix data to this intended activity
        final Intent selectMatrixIntent = new Intent(this, MatrixInfo.class);

        // Access the DB (First) and get the list of all Matrices to put in the ListView
        final ArrayList<String> MatrixNames = DBmgr.getMatrixNames();

        // In the list we will add a first element before the matrices that will always be to create a new Matrix
        MatrixNames.add(0, "Create a new Matrix");

        // This allows the ArrayList to be put into a wrapper to be used in a ListView
        ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, MatrixNames);

        // Find and give a variable to the object of the ListView
        ListView mainListView = (ListView) findViewById(R.id.SelectMatrixListView);
        // Attach the array adapter so we can funnel an arrayList into the ListView
        mainListView.setAdapter(arrayAdapter);

        // What happens when a user clicks on an item in the ListView
        mainListView.setOnItemClickListener(
                new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(SelectMatrixOrNew.this, MatrixNames.get(position), Toast.LENGTH_SHORT).show();
                        // Instead of just making a toast this should redirect to a new intent that will be add/remove points
                        // we'll need to add the values to the intent with the addString or whatever it is called

                        // So if position == 0 We send the user to the intent where calculations are done to solve a system and
                        // get eigenvalues etc.
                        if (position == 0){
                            startActivity(createNewMatrixIntent);
                        } else {
                            String[] MatrixElements = DBmgr.getMatrixRow(MatrixNames.get(position));
                            selectMatrixIntent.putExtra("Name", MatrixElements[0]);
                            selectMatrixIntent.putExtra("Eig1", MatrixElements[1]);
                            selectMatrixIntent.putExtra("Eig2", MatrixElements[2]);
                            selectMatrixIntent.putExtra("EigVec1", MatrixElements[3]);
                            selectMatrixIntent.putExtra("EigVec2", MatrixElements[4]);
                            selectMatrixIntent.putExtra("Pts", MatrixElements[5]);
                            startActivity(selectMatrixIntent);
                        }
                    }
                }
        );

        if (!DBmgr.getTutorialFinished()) {
            //Tutorial for this activity

            Animation animation = new TranslateAnimation(0f, 0f, 200f, 0f);
            animation.setDuration(1000);
            animation.setFillAfter(true);
            animation.setInterpolator(new BounceInterpolator());

            // Pointer pointer = new Pointer().setColor(Color.RED).setGravity(Gravity.CENTER);


            Overlay overlay = new Overlay()
                    .setBackgroundColor(Color.TRANSPARENT)
                    .disableClick(false)
                    .setStyle(Overlay.Style.Rectangle);


            ToolTip toolTip = new ToolTip()
                    .setTitle("Hint")
                    .setDescription("select \"Create New Matrix\" to input new matrix input\nor select the matrix from history ")
                    .setTextColor(Color.parseColor("#EE2c3e50"))
                    //.setBackgroundColor(Color.parseColor("#e74c3c"))
                    .setShadow(true)
                    .setGravity(Gravity.BOTTOM)
                    .setEnterAnimation(animation);

            TourGuide mTourGuideHandler = TourGuide.init(this).with(TourGuide.Technique.Click)
                    .setPointer(null)
                    .setToolTip(toolTip)
                    .setOverlay(overlay)
                    .playOn(mainListView);
        }
    }

}
