package com.kaist.ninjas.cs408ninjas;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.util.Pair;

import com.kaist.ninjas.cs408ninjas.detection.HandDetector;
import com.kaist.ninjas.cs408ninjas.detection.Gesture;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FrameProcessor {

    public static String TAG = "FRAME PROCESSOR";
    public static void test(String inDir, String outDir){
        String inputFileName="image";
        String inputExtension = "jpg";
        String outputExtension = "png";
        String inputFilePath = inDir + File.separator + inputFileName + "." + inputExtension;

        Log.d (TAG, "loading " + inputFilePath + "...");
        Mat image = Imgcodecs.imread(inputFilePath);
        Log.d (TAG, "width of " + inputFileName + ": " + image.width());
        // if width is 0 then it did not read your image.


        // for the canny edge detection algorithm, play with these to see different results
        int threshold1 = 70;
        int threshold2 = 100;

        Mat im_canny = new Mat();  // you have to initialize output image before giving it to the Canny method
        Imgproc.Canny(image, im_canny, threshold1, threshold2);
        String cannyFilename = outDir + File.separator + inputFileName + "_canny-" + threshold1 + "-" + threshold2 + "." + outputExtension;
        Log.d (TAG, "Writing " + cannyFilename);
        Imgcodecs.imwrite(cannyFilename, im_canny);
    }

    public static void convertColor(Mat src, Mat dst){
        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_BGR2GRAY);
    }



    public static MatOfInt getHull(MatOfPoint contour){
        MatOfInt hull = new MatOfInt();
        Imgproc.convexHull(contour, hull);
        return hull;
    }

    // Assume src is already in HSV area
    public static MatOfPoint getMaxContour(Mat src){
        Mat dst = new Mat();
//        Imgproc.cvtColor(src, dst, Imgproc.COLOR_HSV2BGR);
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(dst, dst, 0, 255, 0);

        // Get contours
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(dst, contours, new Mat(),Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        int maxIndex = 0;
        double maxArea = 0;
        Log.i("CONTOURS SIZE", ""+contours.size());
        for(int i=0;i<contours.size();i++){
            MatOfPoint cnt = contours.get(i);
            double area = Imgproc.contourArea(cnt);
            if(area>maxArea){
                maxIndex = i;
                maxArea = area;
            }
        }
        if (contours.size() == 0) return null;
        return contours.get(maxIndex);
    }

    public MatOfInt4 getDefects(MatOfPoint contour){
        MatOfInt hull = new MatOfInt();
        MatOfInt4 defect = new MatOfInt4();
        Imgproc.convexHull(contour, hull);
        if (hull != null /*and len(hull > 3) and len(contour) > 3*/){
             Imgproc.convexityDefects(contour, hull, defect);
             return defect;
        }
        return null;
    }

    public Pair getCentroid(MatOfPoint contour){
        Moments moments = Imgproc.moments(contour);
        if(moments.m00 != 0){
            int cx = (int) (moments.m10/moments.m00);
            int cy = (int) (moments.m01/moments.m00);
            return new Pair(cx, cy);
        }
        return null;
    }

    public void getContourInterior(Mat src, MatOfPoint contour){
        RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
        Mat box = new Mat();
        Imgproc.boxPoints(rect, box);
        int rows = src.width();
        int cols = src.height();

    }


    public static Mat getHandHist (Mat frame){
        Size size = frame.size();
        Mat handHistImage = new Mat(120, 120, frame.type());

        for(int i = 0; i<9; i++){
            int x = (7+3*(i%3))*(int) size.width/20;
            int y = (6+4*(i/3))*(int) size.height/20;
            Rect roi = new Rect(x, y, 40, 40);
            Rect dstRange = new Rect((i%3)*40,(i/3)*40, 40, 40);
            frame.submat(roi).copyTo(handHistImage.submat(dstRange));
        }

        // Convert area to HSV
        Imgproc.cvtColor(handHistImage, handHistImage, Imgproc.COLOR_BGR2HSV);

        try{
            Mat hist = new Mat();
            Imgproc.calcHist(Arrays.asList(handHistImage), new MatOfInt(0,1),new Mat(), hist, new MatOfInt(180,256), new MatOfFloat(0, 180, 0, 256)  );
            Core.normalize(hist, hist, 0, 255, Core.NORM_MINMAX);
            return hist;
        } catch (Exception ex){
            ex.printStackTrace();

        }

        return null;
    }

    public static void drawHistRects(Bitmap bitmap) {
        for(int i = 0; i<9; i++){
            int x = (7+3*(i%3))*(int) bitmap.getWidth()/20;
            int y = (6+4*(i/3))*(int) bitmap.getHeight()/20;
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setAntiAlias(true);
            paint.setColor(Color.GREEN);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawRect(x-2, y-2, x + 42, y +42, paint);
        }
    }

    public static MatOfPoint getHandContour(Mat frame){
        Mat dst  = new Mat(frame.size(), frame.channels());

        // Find the contour that corresponding to hand
        MatOfPoint contour;

//        Imgproc.drawContours(dst, Arrays.asList(contour), -1, new Scalar(0,255,0), 3);
//
//        Mat kernel = new Mat(5, 5, CvType.CV_32F);
//        Imgproc.dilate(dst, dst, kernel);
//        Imgproc.erode(dst, dst, kernel);
//
//        contour = FrameProcessor.getMaxContour(dst);
//        Imgproc.drawContours(frame, Arrays.asList(contour), -1, new Scalar(0,255,0), 3);

//        Mat kernel = new Mat(5, 5, CvType.CV_32F);
//        // Bug from here
//        Imgproc.dilate(frame, frame, kernel);
//        Imgproc.erode(frame, frame, kernel);
        contour = FrameProcessor.getMaxContour(frame);
        return contour;
    }

    // frameMat receive as RGB, return as RGB
    public static HandInfo processFrame(Mat orgMat, Mat hist) {
        Mat frameMat = orgMat.clone();
        Imgproc.cvtColor(frameMat, frameMat, Imgproc.COLOR_BGR2HSV);

        Core.flip(frameMat, frameMat, 0);

        Mat handMask = HandDetector.applyHistMask(frameMat, hist);
        MatOfPoint handContour = getHandContour(handMask);
        Pair<Point, Integer> palmInfo = null;
        List<Point> fingers = null;

        if (handContour != null) {
            MatOfPoint hull = extractHull(handContour);
            palmInfo = HandDetector.findPalm(handContour);
            fingers = HandDetector.findFingers(hull, palmInfo.first, palmInfo.second);

            plotPoints(handMask, fingers);
            HandDetector.drawPalm(handMask, palmInfo.first, palmInfo.second);
            Imgproc.drawContours(handMask, Arrays.asList(hull), 0, new Scalar(0,255,255), 2);
//            Imgproc.rectangle(handMask, palmCenter, new Point(palmCenter.x + 100, palmCenter.y + 10), new Scalar(0,255,255),-1);
        }

        Imgproc.cvtColor(handMask, handMask, Imgproc.COLOR_HSV2BGR);
        return new HandInfo(handMask, palmInfo, fingers);
    }

    public static Pair<Gesture, Point> getGesture(HandInfo handInfo){
        Gesture gesture = HandDetector.detectGesture(handInfo.palmInfo, handInfo.fingers);
        Point centroid = handInfo.palmInfo.first;
        return new Pair<>(gesture, centroid);
    }

    public static MatOfPoint extractHull(MatOfPoint contour) {
        MatOfInt hullIds = getHull(contour);

        int[] arrHullId = hullIds.toArray();
        Point[] arrContour = contour.toArray();
        Point[] arrPoints = new Point[arrHullId.length];
        for (int i = 0; i < arrPoints.length; i++) {
            arrPoints[i] = arrContour[arrHullId[i]];
        }
        MatOfPoint hull = new MatOfPoint();
        hull.fromArray(arrPoints);
        return hull;
    }

    public static void plotPoints(Mat frame, List<Point> points){
        for (Point point : points) {
            Imgproc.circle(frame, point, 10, new Scalar(120, 255, 255));
        }
    }


}


class HandInfo {
    public Mat handMask;
    public Pair<Point, Integer> palmInfo;
    public List<Point> fingers;

    public HandInfo(Mat handMask, Pair<Point, Integer> palmInfo, List<Point> fingers){
        this.handMask = handMask;
        this.palmInfo = palmInfo;
        this.fingers = fingers;
    }
}