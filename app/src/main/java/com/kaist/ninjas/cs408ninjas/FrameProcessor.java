package com.kaist.ninjas.cs408ninjas;

import android.content.Context;
import android.media.Image;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

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

    public static void resize(Mat src, Mat dst){
        float ratio = src.width()/src.height();
        Size size = new Size(400, 400/ratio);
        Imgproc.resize(src, dst, size);
    }

    public static void flip(Mat src, Mat dst){
        flip(src, dst);
    }

    public static MatOfPoint getMaxContour(Mat src){
        Mat dst = new Mat(src.size(), CvType.CV_8UC3);
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(dst, dst, 0, 255, 0);

        // Get contours
        Mat _ = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(dst, contours, _,Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        _.release();

        int maxIndex = 0;
        double maxArea = 0;
        for(int i=0;i<contours.size();i++){
            MatOfPoint cnt = contours.get(i);
            double area = Imgproc.contourArea(cnt);
            if(area>maxArea){
                maxIndex = i;
                maxArea = area;
            }
        }
        return contours.get(maxIndex);
    }

    public static MatOfInt getHull(MatOfPoint contour){
        MatOfInt hull = new MatOfInt();
        Imgproc.convexHull(contour, hull);
        return hull;
    }

    public static Mat imgBytesToMat(byte[] imgData, Mat dst, int width, int height) {
//        Mat mat = new Mat(width, height, CvType.CV_8UC3);
        dst.put(0, 0, imgData);
        return dst;
    }
}
