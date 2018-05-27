package com.kaist.ninjas.cs408ninjas.detection;

import android.util.Log;
import android.util.Pair;

import com.kaist.ninjas.cs408ninjas.FrameProcessor;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f ;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandDetector {
    public static Mat applyHistMask(Mat frameMat, Mat hist){
            Mat hsvFrame = frameMat.clone();
            Mat dst = new Mat();
            List<Mat> frames = new ArrayList<Mat>();
            frames.add(hsvFrame);
            Imgproc.calcBackProject(frames, new MatOfInt(0, 1), hist, dst, new MatOfFloat(0, 180, 0, 256), 1);

            Mat disc = Imgproc.getStructuringElement(Imgproc.MORPH_OPEN, new Size(11, 11));
            Imgproc.filter2D(dst, dst, -1, disc);

            Mat dst2 = new Mat();

            Imgproc.threshold(dst, dst, 70, 255, Imgproc.THRESH_BINARY);
            Core.merge(Arrays.asList(dst, dst, dst), dst2);

            Imgproc.GaussianBlur(dst2, dst2, new Size(3,3),0);

            Core.bitwise_and(hsvFrame, dst2, hsvFrame);
            return hsvFrame;
    }


    public static Pair<Point, Integer> findPalm(MatOfPoint handContour){
        Rect rect = Imgproc.boundingRect(handContour);
        int max_d = 0;
        Point center = null;

        int y_start = (int) Math.round(rect.y + 0.2 * rect.height);
        int y_end = (int) Math.round(rect.y + 0.8 * rect.height);
        int y_step = (int) Math.max(1, Math.round(0.6 / 40 * rect.height));
        int x_start = (int) Math.round(rect.x + 0.3 * rect.width);
        int x_end = (int) Math.round(rect.x + 0.7 * rect.width);
        int x_step = (int) Math.max(1, Math.round(0.4 / 40 * rect.width));

        // initialize src
        MatOfPoint2f handContour2f = new MatOfPoint2f();
        handContour.convertTo(handContour2f, CvType.CV_32F);

        for (int y = y_start; y <= y_end; y += y_step){
            for (int x = x_start; x <= x_end; x += x_step) {
                Point p = new Point(x,y);
                double dist = Imgproc.pointPolygonTest(handContour2f, p, true);
                if (dist > max_d) {
                    max_d = (int) dist;
                    center = p;
                }
            }
        }
        return new Pair<>(center, max_d);
    }

    public static List<Point> findFingers(MatOfPoint hull, Point palmCenter, int palmRadius) {
        double finger_thresh_l = 2.0;
        double finger_thresh_u = 3.8;
        List<Point> fingers = new ArrayList<>();
        List<Point> hullPoints = new ArrayList<>();
        int dist;

        Log.i("PALM RADIUS", ""+palmRadius);
//        int clusterMaxRange = Math.max(palmRadius / )

        Point[] arrHull = hull.toArray();
        for (int i =0 ; i < arrHull.length; i++) {
            dist = (int)(Math.pow(arrHull[i].x - arrHull[(i+1)%arrHull.length].x, 2)+
                         Math.pow(arrHull[i].y - arrHull[(i+1)%arrHull.length].y, 2));
            if (dist > 400) {
                hullPoints.add(arrHull[i]);
            }
        }

        for (int i =0 ; i < hullPoints.size(); i++) {
            dist = (int)(Math.pow(hullPoints.get(i).x - palmCenter.x, 2)+
                    Math.pow(hullPoints.get(i).y - palmCenter.y, 2));

            // omit some condition
            // dist > finger_thresh_l * palmRadius && dist < finger_thresh_u *palmRadius &&

            if (hullPoints.get(i).x < palmCenter.x + palmRadius ) {
                fingers.add(hullPoints.get(i));
            }
        }

        return fingers;

    }

    public static void drawPalm(Mat frame, Point center, int radius){
        Mat frameCpy = new Mat();
        frame.copyTo(frameCpy);
        Imgproc.circle(frame, center, radius, new Scalar(255,0,0), 2);
        Imgproc.circle(frame, center, 3, new Scalar(255,0,0), -1);
    }

    // from trained model
    public static ArrayList<Pair<Gesture, Point>> receiveGesture(Mat frame){
        // detect gesture and its location here!
        ArrayList<Pair<Gesture,Point>> detected = new ArrayList<>();
        Gesture detectedGes1 = Gesture.Act;
        Gesture detectedGes2 = Gesture.V;
        Point detectedLoc1 = new Point();
        Point detectedLoc2 = new Point();
        if(detectedLoc1 != null){
            detected.add(new Pair(detectedGes1, detectedLoc1));
        }
        if(detectedLoc2 != null){
            detected.add(new Pair(detectedGes2, detectedLoc2));
        }


        return detected;
    }

    // from opencv
    public static Gesture detectGesture(Pair<Point, Integer> palmInfo,  List<Point> fingers){
        Point palmCenter = palmInfo.first;
        int palmRadius = palmInfo.second;
        Gesture res = null;

        // detect palm
        if(fingers.size() == 5){
            boolean detectPalm = true;
            for (Point finger: fingers){
                double dist = Math.sqrt(Math.pow(finger.x - palmCenter.x, 2)+ Math.pow(finger.y - palmCenter.y, 2));
                if(dist < palmRadius){
                    detectPalm = false;
                }
            }
            if(detectPalm){
                res = Gesture.Palm;
            }
        }

        // detect fist
        if (fingers.size() == 0) {
            res = Gesture.Fist;
        }

        // detect call, act, v
        if (fingers.size() == 2){
            Point finger1 = fingers.get(0);
            Point finger2 = fingers.get(1);
            double xDist = finger1.x - finger2.x;
            if (xDist > 1.7 * palmRadius){
                res = Gesture.Call;
            }
            if (xDist > 0.3 * palmRadius && xDist < 0.7 * palmRadius){
                res = Gesture.V;
            }
            if (xDist > palmRadius && xDist < 1.3 * palmRadius){
                res = Gesture.Act;
            }
        }

        if (fingers.size() == 1){
            Point finger = fingers.get(0);
            double yDist = finger.y - palmCenter.y;
            if (yDist > palmRadius){
                res = Gesture.Point;
            }
            if (yDist < palmRadius){
                res = Gesture.Thumb;
            }
        }
        return res;
    }
}
