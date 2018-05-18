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

public class HandDetector {


    public void applyHistMask(Mat src, Mat hist){
        Mat dst = new Mat();
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2HSV);
        Imgproc.calcBackProject(Arrays.asList(dst), new MatOfInt(0,1), hist, dst, new MatOfFloat(0,180,0,256),1);

        Mat disc = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(11,11));
        Imgproc.filter2D(dst, dst, -1, disc);

        Imgproc.threshold(dst, dst, 100, 255, 0);
        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_GRAY2BGR);
        Core.bitwise_and(src, dst, dst);

    }

    public MatOfPoint getHandContour(Mat frame){
        Mat dst  = new Mat(frame.size(), frame.channels());
        MatOfPoint contour = FrameProcessor.getMaxContour(frame);
        Imgproc.drawContours(dst, Arrays.asList(contour), -1, new Scalar(0,255,0), 3);

        Mat kernel = new Mat(5, 5, CvType.CV_32F);
        Imgproc.dilate(dst, dst, kernel);
        Imgproc.erode(dst, dst, kernel);

        contour = FrameProcessor.getMaxContour(dst);
        return contour;
    }

    public Pair<Point, Double> findPalm(Mat frame){
        MatOfPoint handContour = getHandContour(frame);
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

    public void drawPalmCentroid(Mat frame){
        Pair<Point, Double> palm = findPalm(frame);
        Point palmCenter = palm.first;
        double palmRad = palm.second;
        Imgproc.circle(frame, palmCenter, (int) palmRad, new Scalar(255,0,0), 2);
        Imgproc.circle(frame, palmCenter, 5, new Scalar(255,0,0), -1);
    }
}
