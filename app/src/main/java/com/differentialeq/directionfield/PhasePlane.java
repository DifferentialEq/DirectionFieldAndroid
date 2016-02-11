package com.differentialeq.directionfield;
/**
 * (c)2015 DifferentialEq.com
 * Eli Selkin, Chang Zhang, Ahmed Ali, Ben Chen
 */

import android.content.Context;
import android.graphics.DashPathEffect;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.differentialeq.directionfield.MatrixMath.Complex;
import com.differentialeq.directionfield.MatrixMath.MathZero;
import com.differentialeq.directionfield.MatrixMath.TwoDimMatrix;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class PhasePlane extends Fragment {
    PointListener activityCommander;

    private RelativeLayout RelLayout;
    //private Random rand;
    // Metrics handling
    Bundle bundleMetrics;
    DisplayMetrics metrics;
    Message messageMetrics;

    // Zoom, ticks are some distance apart
    private float tickDistance;   // Tick distance will start at 48, because screen is really 480x480, but doesn't matter
    private float scale = 1.0f;   // Start with a scale of 1
    private float offsetX = 0.0f; // start at origin
    private float offsetY = 0.0f;
    private float pixelDimension;

    //DEBUG
    private float WidthDP; // density unit in width
    private float HeightDP; // density unit in height


    // Border
    private float Border;

    // Style
    private boolean showAxes;
    private boolean showGrid;
    private boolean showTicks;
    private boolean showBorder;
    private boolean showNumbers;

    // Set up drawing
    private Canvas phasePlaneCanvas;
    private Bitmap background;
    private Paint paintSettings;

    // THE VIEW
    private View view;
    private boolean viewCreated;

    // Handle ScaleEvents
    private ScaleGestureDetector SGD;
    private GestureDetector GD;

    // ThreadPoolExecutor for drawing
    ThreadPoolExecutor DrawExecutor;
    ThreadPoolExecutor FieldExecutor;

    // POINTS AND SOLUTIONS
    private int totalPoints;
    private ArrayList<Solution> Solutions;
    private ArrayList<Solution> VectorField;
    // Here we will hold while this activity is open all the data for the different points.
    // Coming in the Matrix will have already had its eigenvalues, eigenvectors, and possibly some points.
    // Those points will be stored in the array list for points and one solution will have been created for each.
    // Each Solution (or iSolution by polymorphism) will store all of its values for a certain specified number of segments
    // and can be repainted by having it return its points when changing the graph aspect or style. It should not have to be
    // computed twice (while in the same activity). Once you leave the activity, solutions will have to recalculate all points
    // because you might select a new matrix to graph.

    private TwoDimMatrix Matrix;
    private TwoDimMatrix Eigenvector1, Eigenvector2;
    private Complex[] Eigenvalues;

    public class SolutionAnimator extends View {
        private Path path;
        private ArrayList<float[]> points;
        private PathMeasure pathMeasure;
        private float pathLength;

        public SolutionAnimator(Context context) {
            super(context);
            paintSettings.setAntiAlias(true);
            paintSettings.setStrokeWidth(2);
            paintSettings.setColor(Color.argb(100, 0, 0, 200));
            paintSettings.setStrokeCap(Paint.Cap.ROUND);
            paintSettings.setStrokeJoin(Paint.Join.ROUND);
            paintSettings.setStyle(Paint.Style.STROKE);
            path = new Path();

        }

        // Does the path drawing (the single concave arrow in the direction of t's increase.
        public void setPoints(ArrayList<float[]> pointsToPath){
            synchronized (pointsToPath) {
                this.points = pointsToPath;
                float[] startPoint = convertXYToLinePoint(points.get(0));
                path.moveTo(startPoint[0], startPoint[1]);
                for (int i = 0; i<this.points.size(); i++) {
                    float[] linePoint = convertXYToLinePoint(points.get(i));
                    path.lineTo(linePoint[0],linePoint[1]);
                }
            }
            pathMeasure = new PathMeasure(path, false);
            pathLength = pathMeasure.getLength(); // the interpolated length of the entire path as it would be drawn on the screen in dp
            PathEffect pathEffect = new PathDashPathEffect(makeConvexArrow(15.0f, 15.0f), 5.0f, 0.0f, PathDashPathEffect.Style.ROTATE);
            paintSettings.setPathEffect(pathEffect);
            invalidate();
        }


        //https://github.com/romainguy/road-trip/blob/master/application/src/main/java/org/curiouscreature/android/roadtrip/IntroView.java
        private Path makeConvexArrow(float length, float height) {
            Path p = new Path();
            p.moveTo(0.0f, -height / 2.0f);
            p.lineTo(length - height / 4.0f, -height / 2.0f);
            p.lineTo(length, 0.0f);
            p.lineTo(length - height / 4.0f, height / 2.0f);
            p.lineTo(0.0f, height / 2.0f);
            p.lineTo(0.0f + height / 4.0f, 0.0f);
            p.close();
            return p;
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawPath(path, paintSettings);
            super.invalidate();
        }
    }
    // Handlers are the way you update graphics on the screen since it is bad form to update directly from the onCreate or from a runnable
    // These Handlers will generally be used from threads or user interactions in real time
    Handler drawField = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            paintSettings.setStrokeWidth(1);
            paintSettings.setColor(Color.argb(100,0,139,139));
            for (Solution RandomSolution : VectorField) {
                if (!RandomSolution.isDrawn()) {
                    final ArrayList<float[]> pointsToDraw = RandomSolution.getPoints();
                    final ArrayList<Float> pointSlopes = RandomSolution.getDerivativeValues();
                    synchronized (pointsToDraw) {
                        synchronized (pointSlopes) {
                            if (pointsToDraw.size() == pointSlopes.size()) {
                                for (int i = 0; i < pointSlopes.size(); i++) {
                                    float[] xy0 = pointsToDraw.get(i).clone();
                                    float[] xy1 = unitVector(xy0, pointSlopes.get(i));
                                    float[] LinePoint0 = convertXYToLinePoint(xy0);
                                    float[] LinePoint1 = convertXYToLinePoint(xy1);
                                    phasePlaneCanvas.drawLine(LinePoint0[0], LinePoint0[1], LinePoint1[0], LinePoint1[1], paintSettings);
                                }
                                RandomSolution.setDrawn(true);
                            }
                        }
                    }
                }
            }
            paintSettings.setColor(Color.BLACK);
        }
    };


    /**
     * Helper for both solution arrow of direction and making vector field.
     * @param xy0
     * @param slope
     * @return
     */
    public float[] unitVector(float[]xy0, float slope){
        double secSqTheta = Math.pow(slope,2) + 1;
        double cosSqTheta = 1/secSqTheta;
        double sinSqTheta = 1-cosSqTheta;
        cosSqTheta = Math.sqrt(cosSqTheta); // now cos(theta)
        sinSqTheta = Math.sqrt(sinSqTheta); // now sin(theta)
        if ( slope <= 0 ){
            sinSqTheta *= -1;
        }
        return new float[]{xy0[0]+(float)(cosSqTheta)*tickDistance*scale,xy0[1]+(float)(sinSqTheta)*tickDistance*scale};
    }

    /**
     * Helper for Arrow For Solution curve. From a single point to the next make a path and draw the concave arrow
     * @param xy  float initial point
     * @param xy1 float terminating point
     */
    public void drawUnitArrow(float[]xy, float[]xy1 ){
        SolutionAnimator sa = new SolutionAnimator(getContext());
        ArrayList<float[]> unitpoints = new ArrayList<>();
        unitpoints.add(xy);
        unitpoints.add(xy1);
        sa.setPoints(unitpoints);
        sa.draw(phasePlaneCanvas);
    }


    Handler drawSolution = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            synchronized (Solutions) {
                for (Solution eachSolution : Solutions) {
                    if (!eachSolution.isDrawn()) {
                        final ArrayList<float[]> pointsToDraw = eachSolution.getPoints();
                        float[] X = eachSolution.getXY(0);
                        float[] previousPoint = pointsToDraw.get(0).clone();
                        int iForArrow = 0;
                        boolean arrowNotSet = true;
                        previousPoint = convertXYToLinePoint(previousPoint);
                        synchronized (pointsToDraw) {
                            for (int i = 0; i < pointsToDraw.size(); i++) {

                                paintSettings.reset();
                                paintSettings.setStrokeWidth(2);
                                paintSettings.setColor(Color.argb(100, 0, 35, 102));

                                float[] xy0 = pointsToDraw.get(i).clone();
                                if ( arrowNotSet && MathZero.approxEqual(xy0[0],X[0],.01f) && MathZero.approxEqual(xy0[1],X[1],.01f) ) {
                                    iForArrow = i;
                                    arrowNotSet = false;
                                }
                                float[] linePoint = convertXYToLinePoint(xy0);
                                if (linePoint[0] >= -500 && linePoint[0] <= metrics.widthPixels+500 && linePoint[1] >= -500 && linePoint[1] <= metrics.widthPixels+500)
                                    phasePlaneCanvas.drawLine(previousPoint[0],previousPoint[1],linePoint[0],linePoint[1], paintSettings);
                                previousPoint = linePoint;
                                eachSolution.setDrawn(true);
                            }
                            drawUnitArrow(pointsToDraw.get(iForArrow), pointsToDraw.get(iForArrow + 1));
                        }
                    }
                }
            }
            paintSettings.reset();
            paintSettings.setColor(Color.BLACK);
        }
    };
    Handler setUpScreen = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // Set up the screen, resolution and give us objects that we can use to paint on
            background = Bitmap.createBitmap(480, 480, Bitmap.Config.ARGB_8888);
            paintSettings = new Paint();
            phasePlaneCanvas = new Canvas(background);
            RelLayout.setBackground(new BitmapDrawable(getActivity().getBaseContext().getResources(), background));
        }
    };
    Handler clearCanvas = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            paintSettings.reset();
            RelLayout.setBackground(new BitmapDrawable(getActivity().getBaseContext().getResources(), background));
        }
    };
    Handler redrawAxes = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            float CenterCanvas = (6 - Border) / 2.0f;

            // Clear the solutions and vector field from saying they have been drawn
            for (Solution RandomSolution : VectorField) {
                RandomSolution.setDrawn(false);
            }
            for (Solution aSolution : Solutions) {
                aSolution.setDrawn(false);
            }
            // Draw area border
            if (showBorder) {
                paintSettings.setStrokeWidth(2);
                paintSettings.setColor(Color.BLACK);
                phasePlaneCanvas.drawRect(Border, Border, 480 - Border, 480 - Border, paintSettings);
                paintSettings.setColor(Color.WHITE);
                phasePlaneCanvas.drawRect(Border + 2, Border + 2, 480 - Border - 2, 480 - Border - 2, paintSettings);
            }

            if (showAxes) {
                paintSettings.setColor(Color.BLACK);
                phasePlaneCanvas.drawLine(Border, 240, (480 - Border), 240, paintSettings);
                phasePlaneCanvas.drawLine(240, Border, 240, (480 - Border), paintSettings);
            }

            float ticks;
            if (showTicks) {
                paintSettings.setColor(Color.BLACK);
                for (float i = 2 * tickDistance; i < CenterCanvas; i += 2 * tickDistance) {
                    phasePlaneCanvas.drawLine(240 + (240 * i), 235, 240 + (i * 240), 245, paintSettings);
                    phasePlaneCanvas.drawLine(240 - (i * 240), 235, 240 - (i * 240), 245, paintSettings);
                    phasePlaneCanvas.drawLine(235, 240 - (i * 240), 245, 240 - (i * 240), paintSettings);
                    phasePlaneCanvas.drawLine(235, 240 + (i * 240), 245, 240 + (i * 240), paintSettings);
                }
            }

            if (showNumbers) {
                paintSettings.setColor(Color.BLACK);

                for (float i = 2 * tickDistance; i < CenterCanvas; i += 2 * tickDistance) {
                    phasePlaneCanvas.drawText(String.format("%.3f", (scale * i + offsetX)), 240 + (i * 240), 255, paintSettings);
                    phasePlaneCanvas.drawText(String.format("%.3f", (-1 * (scale * i) + offsetX)), 240 - (i * 240), 255, paintSettings);
                    phasePlaneCanvas.drawText(String.format("%.3f", (-1 * (scale * i) + offsetY)), 245, 240 + (i * 240), paintSettings);
                    phasePlaneCanvas.drawText(String.format("%.3f", (scale * i + offsetY)), 245, 240 - (i * 240), paintSettings);
                }
            }
            if (showGrid) {
                paintSettings.setColor(Color.BLACK);
                for (float i = 0; i < CenterCanvas; i += 4 * tickDistance) {
                    Paint fgPaintSel = new Paint();
                    fgPaintSel.setARGB(255, 0, 0, 0);
                    fgPaintSel.setStyle(Paint.Style.STROKE);
                    fgPaintSel.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));
                    phasePlaneCanvas.drawLine(240 + (i * 240), Border, 240 + (i * 240), 480 - Border, fgPaintSel);
                    phasePlaneCanvas.drawLine(240 - (i * 240), Border, 240 - (i * 240), 480 - Border, fgPaintSel);
                    phasePlaneCanvas.drawLine(Border, 240 + (i * 240), 480 - Border, 240 + (i * 240), fgPaintSel);
                    phasePlaneCanvas.drawLine(Border, 240 - (i * 240), 480 - Border, 240 - (i * 240), fgPaintSel);
                }
            }
        }
    };


    // An attempt, when leaving to get rid of solutions and vector fields in memory
    public void clearMemory(){
        VectorField.clear();
        Solutions.clear();
                DrawExecutor.purge();
        FieldExecutor.purge();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Solutions = new ArrayList<>();
        VectorField = new ArrayList<>();

        // These 2 threadpool Executors operate on two entirely different sets of data. The blocking is on the data within
        // each, not between the two. So we don't cross over.
        DrawExecutor = new ThreadPoolExecutor(5, 30, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        FieldExecutor = new ThreadPoolExecutor(5, 30, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

        View view = inflater.inflate(R.layout.fragment_phase_plane, container, false); // View group object
        viewCreated = false;
        // Gestures
        SGD = new ScaleGestureDetector(this.getContext(), new PinchZoomListener());
        GD = new GestureDetector(this.getContext(), new ScrollListener());
        // Set up main listener to distribute to the two others.
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                GD.onTouchEvent(event);
                SGD.onTouchEvent(event);
                return true;
            }
        });

        RelLayout = (RelativeLayout) view.findViewById(R.id.PlaneLayout); // The layout for the drawing screen where we'll position the bitmap

        // Set up dimensional configuration
        metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        //DEBUG
        WidthDP = pxToDp(metrics.widthPixels); // XDP is converted from absolute width pixel of device, which includes status bar height but excludes home button bar
        // can also use function getResources().getConfiguration().screenWidthDp;
        HeightDP = pxToDp(metrics.heightPixels);// YDP is converted from absolute Height pixel of device, which includes status bar height but excludes home button bar

        // give tickDistance a value
        tickDistance = 0.1f; // This will be the same on X1 and X2 or X and Y

        // Set Border default
        Border = 0.0f;

        // Style
        showAxes = true;
        showGrid = true;
        showTicks = true;
        showBorder = true;
        showNumbers = true;

        // Set up screen
        setUpScreen.sendEmptyMessage(0);
        redrawAxes.sendEmptyMessage(0);
        return view;
    }

    public void setDimension(int boxDimension) {
        this.pixelDimension = boxDimension;
    }

    /**
     * Passed information from the parent Activity about the matrix we are solving
     *
     * @param M   TwoDimMatrix
     * @param E1  TwoDimMatrix
     * @param E2  TwoDimMatrix
     * @param EV1 Complex
     * @param EV2 Complex
     */
    public void receiveMatrix(TwoDimMatrix M, TwoDimMatrix E1, TwoDimMatrix E2, Complex EV1, Complex EV2) {
        viewCreated = true;
        Matrix = M;
        Eigenvector1 = E1;
        Eigenvector2 = E2;
        Eigenvalues = new Complex[]{EV1, EV2};
    }

    // Add a point to the vector field list
    public boolean addFieldPoint(float x1, float x2) {
        return addPoints(VectorField, x1, x2);
    }

    // Add a point to the solution list
    public boolean addPoint(float x1, float x2) {
        return addPoints(Solutions, x1, x2);
    }

    public void setNumPoints(int pts, int ptsnotselected) {
        this.totalPoints = pts + ptsnotselected;
    }

    /**
     * addPoints functions to generate a solution from a matrix of real coefficients with the solution to t=0 being x1,x2
     *
     * @param SolutionList ArrayList to add solution to
     * @param x1           float
     * @param x2           float
     * @return true if point/solution was added, false if already exists.
     */
    public boolean addPoints(ArrayList<Solution> SolutionList, float x1, float x2) {
        try {
            boolean hasPoint = false;
            for (Solution i : SolutionList) {
                if (i.getX()[0] == x1 && i.getX()[1] == x2)
                    hasPoint = true;
            }
            // When we are adding add points we first have to see if the limit will be too great
            // Overloading on solutions is really dangerous for redraws and scaling.
            if ((SolutionList == VectorField) || (totalPoints <= 25 && !hasPoint)) {
                if (SolutionList == Solutions)
                    totalPoints++;
                synchronized (SolutionList) {
                    if (Eigenvalues[0].getiValue() != 0 || Eigenvalues[1].getiValue() != 0) {
                        // we have imaginary solutions
                        iSolution imaginary = new iSolution(x1, x2);
                        // using symbols from Boyce & DiPrima
                        float Lambda0 = (float) Eigenvalues[1].getrValue();
                        float Mu0 = (float) Eigenvalues[1].getiValue();
                        float A00 = (float) Eigenvector1.getValue(1, 1).getrValue();
                        float B00 = (float) Eigenvector1.getValue(1, 1).getiValue();
                        float A01 = (float) Eigenvector1.getValue(2, 1).getrValue();
                        float B01 = (float) Eigenvector1.getValue(2, 1).getiValue();
                        imaginary.addMus(Mu0);
                        imaginary.addLambdas(Lambda0);
                        imaginary.addAs(new float[]{A00, A01});
                        imaginary.addBs(new float[]{B00, B01});
                        imaginary.determineC();
                        imaginary.generatePoints();
                        imaginary.genDerivativeForPoints();
                        SolutionList.add(imaginary);
                    } else {
                        Solution real = new Solution(x1, x2);
                        real.setRs(Eigenvalues);
                        real.setEigenvectors(new TwoDimMatrix[]{Eigenvector1, Eigenvector2});
                        real.determineC();
                        real.generatePoints();
                        real.genDerivativeForPoints();
                        SolutionList.add(real);
                    }
                    Runnable addPointRunnable = new Runnable() {
                        @Override
                        public void run() {
                            drawSolution.sendEmptyMessage(0);
                        }
                    };
                    // We are threading calculations
                    DrawExecutor.submit(addPointRunnable);
                }
                return true;
            }
        } catch(RuntimeException re){
            // do nothing
            Toast.makeText(getContext(), "Error in matrix", Toast.LENGTH_SHORT).show();
        }
        return false;

    }

    // Accessed from fragment parent
    public void changeStyle(boolean axes, boolean grid, boolean ticks, boolean border, boolean numbers) {
        showGrid = grid;
        showTicks = ticks;
        showBorder = border;
        showAxes = axes;
        showNumbers = numbers;
        setUpScreen.sendEmptyMessage(0);
        //clearCanvas.sendEmptyMessage(0);
        redrawAxes.sendEmptyMessage(0);
        drawField.sendEmptyMessage(0);
        drawSolution.sendEmptyMessage(0);
    }


    /**
     * Generate 100 random points that are within the bounds of the current screen after zoom or pan and gather their points. Multithreaded
     */
    public void generateVectorField() {
        if (FieldExecutor.getActiveCount() == 0) {
            VectorField.clear();
            float[] ScreenXYBegin = convertScreenToXY((float) 0, (float) getStatusBarHeight());
            float[] ScreenXYEnd = convertScreenToXY(metrics.widthPixels, metrics.widthPixels+getStatusBarHeight());
            if (viewCreated) {
                synchronized (VectorField) {
                    float yinterval = Math.abs(ScreenXYBegin[1]-ScreenXYEnd[1]) / 10;
                    float xinterval = Math.abs(ScreenXYEnd[0]-ScreenXYBegin[0]) / 10;
                    for (float i = ScreenXYBegin[0]+xinterval; i <= ScreenXYEnd[0]; i += xinterval) {
                        for (float j = ScreenXYEnd[1]+yinterval; j<= ScreenXYBegin[1]; j += yinterval) {
                            addFieldPoint(i, j);
                        }
                    }
                    for (Solution eachSolution : VectorField) {
                        FieldExecutor.submit(eachSolution);
                    }
                }
                //setUpScreen.sendEmptyMessage(0);
                clearCanvas.sendEmptyMessage(0);
                redrawAxes.sendEmptyMessage(0);
                drawField.sendEmptyMessage(0);
                drawSolution.sendEmptyMessage(0);
            }
        }
    }

    /**
     * When we are drawing from a list of Solution/iSolution generated real points, we need the corresponding location on the bitmap
     *
     * @param xy a float array
     * @return a float array corresponding to the point for drawing lines on the canvas
     */
    public float[] convertXYToLinePoint(float[] xy) {
        return new float[]{(240f + 240 * ((xy[0] - offsetX)/scale )), (240 - 240 * ((xy[1] - offsetY)/scale ))};
    }

    /**
     * Convert location from raw X/Y position from touch on device to XY position relative to the graph
     *
     * @param x float
     * @param y float
     * @return float array corresponding to the graph's new point
     */
    public float[] convertScreenToXY(float x, float y) {
        y = y - getStatusBarHeight(); // move the axis
        float halfScreen = getResources().getConfiguration().screenWidthDp / 2;
        float dpX = pxToDp(x);
        float dpY = pxToDp(y);
        float proportionToOriginX = (Math.abs(halfScreen - dpX) / halfScreen) * (dpX < halfScreen ? -1 : 1); // Relative to 1, proportion of distance from origin along X axis
        float proportionToOriginY = (Math.abs(halfScreen - dpY) / halfScreen) * (dpY < halfScreen ? 1 : -1); // along Y-axis
        float ScaleDistFromOriginX = (scale * proportionToOriginX) + offsetX; // Offsets are never scaled
        float ScaleDistFromOriginY = (scale * proportionToOriginY) + offsetY;
        return new float[]{ScaleDistFromOriginX, ScaleDistFromOriginY};
    }

    /**
     * convert from dp to pixel
     *
     * @param dp the density independent pixel you wanted to convert
     * @return
     */
    public float dpToPx(float dp) {
        //pixel = dp * (dpi /160)
        return dp * (metrics.densityDpi / 160);
    }

    /**
     * convert from pixel to dp
     *
     * @param px pixel
     * @return
     */
    public float pxToDp(float px) {
        //dp =( pixel/dpi )*160
        return (px / metrics.densityDpi) * 160;
    }

    /**
     * get the height of status bar in pixels
     * @return
     */
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * On attach signals that it is listening to the activityCommander (the DrawActivity context)
     * @param context Draw Activity
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            activityCommander = (PointListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString());
        }
    }

    /**
     * A requirement for the DrawActivity which will implement this. It is the agreement that it will listen to this command
     */
    public interface PointListener {
        void addPointToGraph(float x1pt, float x2pt);
    }


    /// LISTENERS FOR SCROLL, DOUBLE TAP, AND PINCH/ZOOM

    /**
     * LISTENER CLASS FOR SCALE SCROLL OR DOUBLE TAP.
     */
    private class ScrollListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            offsetX += (distanceX/metrics.widthPixels) * tickDistance * 10 * scale;
            offsetY -= (distanceY/metrics.widthPixels) * tickDistance * 10 * scale;
            if (offsetY < -100f) offsetY = -100f;
            if (offsetX < -100f) offsetX = -100f;
            if (offsetY > 100f) offsetY = 100f;
            if (offsetX > 100f) offsetX = 100f;

            setUpScreen.sendEmptyMessage(0);
            //clearCanvas.sendEmptyMessage(0);

            redrawAxes.sendEmptyMessage(0);
            // Pick new vector field to scale with
            // generate 10 points to draw a vector field from on the current screen
            drawSolution.sendEmptyMessage(0);
            return true;
        }


        /**
         * Double tap adds a point at the selected spot and then derives a solution and adds it to the list of solutions
         *
         * @param e
         * @return
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float OnScreenX = e.getRawX();
            float OnScreenY = e.getRawY();
            //if (OnScreenX <= metrics.widthPixels - Border * 2 && OnScreenX > Border && OnScreenY <= metrics.widthPixels + 20 - Border * 2 && OnScreenY > Border)
            if (OnScreenX <= metrics.widthPixels && OnScreenY <= (metrics.widthPixels + dpToPx(getStatusBarHeight()))) {
                float[] point = convertScreenToXY(OnScreenX, OnScreenY);
                activityCommander.addPointToGraph(point[0], point[1]); // this is a weird cycle to get added to the graph, but it'll do sends to activity which sends to phaseplane
            }
            drawSolution.sendEmptyMessage(0);
            return true;
        }
    }

    /**
     * LISTENER CLASS FOR SCALE ZOOM
     */
    private class PinchZoomListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale /= detector.getScaleFactor();
            if (detector.getScaleFactor() < 1)
                scale = (float) Math.max(1E-3, scale);
            else
                scale = (float) Math.min(100, scale);
            setUpScreen.sendEmptyMessage(0);
            //clearCanvas.sendEmptyMessage(0);
            redrawAxes.sendEmptyMessage(0);
            drawSolution.sendEmptyMessage(0);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }
    }
}