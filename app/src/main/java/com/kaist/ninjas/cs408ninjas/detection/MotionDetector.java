package com.kaist.ninjas.cs408ninjas.detection;

import android.util.Log;

import org.opencv.core.Point;

public class MotionDetector {
    Gesture initGesture1;
    Gesture initGesture2;
    Point[] posBuffer1;
    Point[] posBuffer2;
    int count;
    int currentIndex1;
    int currentIndex2;

    // detect gesture
    // detect position
    // save init gesture
    // save position
    // not detect gesture
    // null init gesture
    // null position

    public MotionDetector(int bufferNum){
        initGesture1 = null;
        initGesture2 = null;
        posBuffer1 = new Point[bufferNum];
        posBuffer2 = new Point[bufferNum];
        count = bufferNum;
        currentIndex1 = 0;
        currentIndex2 = 0;
    }

    public void saveToBuffer(Point centroid){
        if (centroid != null){
            Log.i("MOTION","palm centroid["+ currentIndex1 +"]: " + centroid.x + ", " + centroid.y);
        }
        posBuffer1[currentIndex1] = centroid;
        currentIndex1 = (currentIndex1 + 1) % count;
    }

    public void saveToBuffer(Gesture gesture, Point centroid) {
        if(initGesture1 != null && initGesture1 != gesture){
            reset();
        }
        initGesture1 = gesture;
        posBuffer1[currentIndex1] = centroid;
        currentIndex1 = (currentIndex1 + 1) % count;
    }

//    public Motion detectMotion(){
//        Point currentP1 = posBuffer1[currentIndex2];
//        Point currentP2 = posBuffer2[currentIndex2];
//        if(currentP1 != null && currentP2 != null){
//            if (initGesture1 != Gesture.V || initGesture2 != Gesture.V){
//                return null;
//            }
//            // detect zooming gesture
//        }
//
//        if (isSlideUp(posBuffer1)){
//            switch(initGesture1){
//                case Fist:
//                    return Motion.BrightUp;
//                case V:
//                    return Motion.VolUp;
//            }
//        }
//
//        else if (isSlideDown(posBuffer1)){
//            switch(initGesture1){
//                case Fist:
//                    return Motion.BrightUp;
//                case V:
//                    return Motion.VolUp;
//            }
//        }
//
//        else if (isSlideLeft(posBuffer1)){
//            switch(initGesture1){
//                case Palm:
//                    return Motion.Prev;
//                case Thumb:
//                    return Motion.Backw;
//            }
//        }
//        else if (isSlideRight(posBuffer1)){
//            switch(initGesture1){
//                case Palm:
//                    return Motion.Next;
//                case Thumb:
//                    return Motion.Forw;
//            }
//        }
//        else if (isStill(posBuffer1)){
//            switch(initGesture1){
//                case Call:
//                    return Motion.RecvCall;
//            }
//        }
//
//        return null;
//    }

    public Motion detectMotion(){
        for (int i = 0; i < posBuffer1.length; i++) {
            if(posBuffer1[i] == null){
                Log.i("MOTION", "MOTION NULL");
                return null;
            }
        }

        try {
            if(isSlideUp(posBuffer1)){
                return Motion.VolUp;
            }
            else if(isSlideDown(posBuffer1)){
                return Motion.VolDw;
            }
            else if(isSlideLeft(posBuffer1)){
                return Motion.Backw;
            }
            else if(isSlideRight(posBuffer1)){
                return Motion.Next;
            }
            else if(isStill(posBuffer1)){
                return Motion.Play;
            }
        } catch(Exception ex){
            Log.i("MOTION", "ERROR: " );
        }
        Log.i("MOTION", "NO MOTION");
        return null;
    }

    private boolean isStill(Point[] posBuffer){
        boolean detected = false;
        double current_x = posBuffer[currentIndex1].x;
        double current_y = posBuffer[currentIndex1].y;

        for (int i = 0; i < posBuffer.length; i++) {
            int index = (currentIndex1 - i + count) % count;
            double x = posBuffer[index].x;
            double y = posBuffer[index].y;

            if (y < current_y - 20 || y > current_y + 20) {
                return false;
            }
            if (x < current_x - 20 || x > current_x + 20) {
                return false;
            }

        }
        reset();
        return true;
    }

    private boolean isSlideRight(Point[] posBuffer) {
        boolean detected = false;
        double current_y = posBuffer[currentIndex1].y;

        for (int i = 0; i < posBuffer.length; i++) {
            int index = (currentIndex1 - i + count) % count;
            int prev = (index - 1 + count) % count;
            double x = posBuffer[index].x;
            double y = posBuffer[index].y;
            double x_prev = posBuffer[prev].x;

            if (y < current_y - 200 || y > current_y + 200) {
                return false;
            }
            if (x <= x_prev + 5) {
                return false;
            }
        }
        reset();
        return true;
    }

    private boolean isSlideLeft(Point[] posBuffer) {
        boolean detected = false;
        double current_y = posBuffer[currentIndex1].y;

        for (int i = 0; i < posBuffer.length; i++) {
            int index = (currentIndex1 - i + count) % count;
            int prev = (index - 1 + count) % count;
            double x = posBuffer[index].x;
            double y = posBuffer[index].y;
            double x_prev = posBuffer[prev].x;
            double y_prev = posBuffer[prev].y;

            if (y < current_y - 200 || y > current_y + 200) {
                return false;
            }
            if (x >= x_prev - 5) {
                return false;
            } else {
                detected = true;
            }
        }
        reset();
        return detected;
    }


    private boolean isSlideUp(Point[] posBuffer) {
        boolean detected = false;
        double current_x = posBuffer[currentIndex1].x;
        double current_y = posBuffer[currentIndex1].y;

        for (int i = 0; i < posBuffer.length; i++) {
            int index = (currentIndex1 - i + count) % count;
            int prev = (index - 1 + count) % count;
            double x = posBuffer[index].x;
            double y = posBuffer[index].y;
            double x_prev = posBuffer[prev].x;
            double y_prev = posBuffer[prev].y;

            if (x < current_x - 200 || x > current_x + 200) {
                return false;
            }
            if (y >= y_prev - 5) {
                return false;
            } else {
                detected = true;
            }
        }
        reset();
        return detected;
    }

    private boolean isSlideDown(Point[] posBuffer) {
        boolean detected = false;
        double current_x = posBuffer[currentIndex1].x;
        double current_y = posBuffer[currentIndex1].y;

        for (int i = 0; i < posBuffer.length; i++) {
            int index = (currentIndex1 - i + count) % count;
            int prev = (index - 1 + count) % count;
            double x = posBuffer[index].x;
            double y = posBuffer[index].y;
            double x_prev = posBuffer[prev].x;
            double y_prev = posBuffer[prev].y;

            if (x < current_x - 200 || x > current_x + 200) {
                return false;
            }
            if (y <= y_prev + 5) {
                return false;
            } else {
                detected = true;
            }
        }
        reset();
        return detected;
    }

    private void reset(){
        for (int i = 0; i < count; i++) {
            posBuffer1[i] = null;
            posBuffer2[i] = null;
        }
        initGesture1=null;
        initGesture2=null;

    }
}
