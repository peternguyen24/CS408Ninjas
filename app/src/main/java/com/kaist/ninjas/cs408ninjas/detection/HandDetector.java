package com.kaist.ninjas.cs408ninjas.detection;

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


    public static void applyHistMask(Mat src, Mat hist){
        Mat mask = new Mat();
        Imgproc.cvtColor(src, mask, Imgproc.COLOR_BGR2HSV);
        Imgproc.calcBackProject(Arrays.asList(mask), new MatOfInt(0,1), hist, mask, new MatOfFloat(0,180,0,256),1);

        Mat disc = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(11,11));
        Imgproc.filter2D(mask, mask, -1, disc);

        Imgproc.threshold(mask, mask, 100, 255, 0);
//        Imgproc.cvtColor(mask, mask, Imgproc.COLOR_GRAY2BGR);
//        Core.bitwise_and(src, mask, src);

        src.copyTo(src, mask);
    }

    public static MatOfPoint getHandContour(Mat frame, Mat handHist){
        Mat dst  = new Mat(frame.size(), frame.channels());
        applyHistMask(frame, handHist);
        MatOfPoint contour = FrameProcessor.getMaxContour(frame);
//        Imgproc.drawContours(dst, Arrays.asList(contour), -1, new Scalar(0,255,0), 3);
//
//        Mat kernel = new Mat(5, 5, CvType.CV_32F);
//        Imgproc.dilate(dst, dst, kernel);
//        Imgproc.erode(dst, dst, kernel);
//
//        contour = FrameProcessor.getMaxContour(dst);
        Imgproc.drawContours(frame, Arrays.asList(contour), -1, new Scalar(0,255,0), 3);

        Mat kernel = new Mat(5, 5, CvType.CV_32F);
        // Bug from here
        Imgproc.dilate(frame, frame, kernel);
        Imgproc.erode(frame, frame, kernel);

        contour = FrameProcessor.getMaxContour(frame);
        return contour;
    }

    public static Pair<Point, Double> findPalm(Mat frame, Mat handHist){
        MatOfPoint handContour = getHandContour(frame, handHist);
        Rect rect = Imgproc.boundingRect(handContour);
        int max_d = 0;
        Point center = null;

        int y_start = (int) Math.round(rect.y + 0.2 * rect.height);
        int y_end = (int) Math.round(rect.y + 0.8 * rect.height);
        int y_step = (int) Math.max(1, Math.round(0.6 / 100 * rect.height));
        int x_start = (int) Math.round(rect.x + 0.3 * rect.width);
        int x_end = (int) Math.round(rect.x + 0.7 * rect.width);
        int x_step = (int) Math.max(1, Math.round(0.4 / 100 * rect.width));

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

        return new Pair(center, max_d);
    }

    public static void drawPalmCentroid(Mat frame, Mat handHist){
        Mat frameCpy = new Mat();
        frame.copyTo(frameCpy);
        Pair<Point, Double> palm = findPalm(frameCpy, handHist);
        Point palmCenter = palm.first;
        double palmRad = palm.second;
        Imgproc.circle(frame, palmCenter, (int) palmRad, new Scalar(255,0,0), 2);
        Imgproc.circle(frame, palmCenter, 5, new Scalar(255,0,0), -1);
    }
}
