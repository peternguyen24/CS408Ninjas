package com.kaist.ninjas.cs408ninjas.detection;

import android.util.Log;

import org.opencv.core.Point;

public class MotionDetector {
    Point[] posBuffer1;
    int count;
    int currentIndex;

    // detect gesture
    // detect position
    // save init gesture
    // save position
    // not detect gesture
    // null init gesture
    // null position

    public MotionDetector(int bufferNum){
        posBuffer1 = new Point[bufferNum];
        count = bufferNum;
        currentIndex = 0;
    }

    public void saveToBuffer(Point centroid){
        currentIndex = (currentIndex + 1) % count;
        posBuffer1[currentIndex] = centroid;
        if (centroid != null){
            Log.i("GMOTION","palm centroid["+ currentIndex +"]: " + centroid.x + ", " + centroid.y);
        }
    }



    public Motion detectMotion(){
        for (int i = 0; i < posBuffer1.length; i++) {
            if(posBuffer1[i] == null){
                Log.i("GMOTION", "MOTION NULL");
                return null;
            }
        }

        Log.i("GMOTION", posBuffer1[0].toString() + " " + posBuffer1[1].toString() + " " + posBuffer1[2].toString() + " " + posBuffer1[3].toString());

        try {
            if(isSlideUp(posBuffer1)){
                return Motion.VolUp;
            }
            else if(isSlideDown(posBuffer1)){
                return Motion.VolDw;
            }
            else if(isSlideLeft(posBuffer1)){
                return Motion.Prev;
            }
            else if(isSlideRight(posBuffer1)){
                return Motion.Next;
            }
            else if(isStill(posBuffer1)){
                return Motion.Play;
            }
        } catch(Exception ex){
            Log.i("GMOTION", "ERROR: " );
        }
        Log.i("GMOTION", "NO MOTION");
        return null;
    }

    private boolean isStill(Point[] posBuffer){
        double current_x = posBuffer[currentIndex].x;
        double current_y = posBuffer[currentIndex].y;

        for (int i = 0; i < posBuffer.length; i++) {
            int index = (currentIndex - i + count) % count;
            double x = posBuffer[index].x;
            double y = posBuffer[index].y;

            if (y < current_y - 8 || y > current_y + 8) {
                return false;
            }
            if (x < current_x - 8 || x > current_x + 8) {
                return false;
            }

        }
        reset();
        return true;
    }

    private boolean isSlideDown(Point[] posBuffer) {
        double current_y = posBuffer[currentIndex].y;

        for (int i = 0; i < posBuffer.length-1; i++) {
            int index = (currentIndex - i + count) % count;
            int prev = (index - 1 + count) % count;
            double x = posBuffer[index].x;
            double y = posBuffer[index].y;
            double x_prev = posBuffer[prev].x;

            if (y < current_y - 50 || y > current_y + 50) {
                return false;
            }
            if (x <= x_prev + 5) {
                return false;
            }
        }
        reset();
        return true;
    }

    private boolean isSlideUp(Point[] posBuffer) {
        double current_y = posBuffer[currentIndex].y;

        for (int i = 0; i < posBuffer.length-1; i++) {
            int index = (currentIndex - i + count) % count;
            int prev = (index - 1 + count) % count;
            double x = posBuffer[index].x;
            double y = posBuffer[index].y;
            double x_prev = posBuffer[prev].x;
            if (y < current_y - 50 || y > current_y + 50) {
                return false;
            }
            if (x >= x_prev - 5) {
                return false;
            }
        }
        reset();
        return true;
    }


    private boolean isSlideRight(Point[] posBuffer) {
        double current_x = posBuffer[currentIndex].x;

        for (int i = 0; i < posBuffer.length-1; i++) {
            int index = (currentIndex - i + count) % count;
            int prev = (index - 1 + count) % count;
            double x = posBuffer[index].x;
            double y = posBuffer[index].y;
            double y_prev = posBuffer[prev].y;

            if (x < current_x - 50 || x > current_x + 50) {
                return false;
            }
            if (y >= y_prev - 5) {
                return false;
            }
        }
        reset();
        return true;
    }

    private boolean isSlideLeft(Point[] posBuffer) {
        double current_x = posBuffer[currentIndex].x;

        for (int i = 0; i < posBuffer.length-1; i++) {
            int index = (currentIndex - i + count) % count;
            int prev = (index - 1 + count) % count;
            double x = posBuffer[index].x;
            double y = posBuffer[index].y;
            double y_prev = posBuffer[prev].y;

            if (x < current_x - 50 || x > current_x + 50) {
                return false;
            }
            if (y <= y_prev + 5) {
                return false;
            }
        }
        reset();
        return true;
    }

    private void reset(){
        for (int i = 0; i < count; i++) {
            posBuffer1[i] = null;
        }
    }
}
