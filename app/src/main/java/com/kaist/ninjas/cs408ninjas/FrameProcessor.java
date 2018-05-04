package com.kaist.ninjas.cs408ninjas;

import android.content.Context;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.TimerTask;

public class FrameProcessor {

    public static String TAG = "FRAME PROCESSOR";
    public static void test(String inDir, String outDir){
        String inputFileName="image";
        String inputExtension = "jpg";
//        String inputDir = getCacheDir().getAbsolutePath();  // use the cache directory for i/o
//        String outputDir = getCacheDir().getAbsolutePath();
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
        Imgcodecs.imwrite(cannyFilename, im_canny);}

}
