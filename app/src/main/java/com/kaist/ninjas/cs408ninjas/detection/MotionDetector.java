package com.kaist.ninjas.cs408ninjas.detection;

import org.opencv.core.Point;

public class MotionDetector {
    Gesture initGesture1;
    Gesture initGesture2;
    Point[] posBuffer1;
    Point[] posBuffer2;
    int maxIndex;
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
        maxIndex = bufferNum-1;
        currentIndex1 = 0;
        currentIndex2 = 0;
    }

    public void saveToBuffer(Point centroid){
        posBuffer1[currentIndex1] = centroid;
        currentIndex1 = (currentIndex1 + 1) % maxIndex;
    }

    public void saveToBuffer(Gesture gesture, Point centroid) {
        if(initGesture1 != null && initGesture1 != gesture){
            reset();
        }
        initGesture1 = gesture;
        posBuffer1[currentIndex1] = centroid;
        currentIndex1 = (currentIndex1 + 1) % maxIndex;
    }

    public void saveToBuffer(Gesture gesture1, Gesture gesture2, Point centroid1, Point centroid2) {
        if((initGesture1 != null && initGesture1 != gesture1)||(initGesture2 != null && initGesture2 != gesture2)){
            reset();
        }
        initGesture1 = gesture1;
        initGesture2 = gesture2;
        posBuffer1[currentIndex1] = centroid1;
        posBuffer2[currentIndex2] = centroid2;
        currentIndex1 = (currentIndex1 + 1) % maxIndex;
        currentIndex2 = (currentIndex2 + 1) % maxIndex;
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
                return null;
            }
        }
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
        return null;
    }

    private boolean isStill(Point[] posBuffer){
        boolean detected = false;
        double current_x = posBuffer[currentIndex1].x;
        double current_y = posBuffer[currentIndex1].y;

        for (int i = 0; i < posBuffer.length; i++) {
            int index = (currentIndex1 - i) % maxIndex;
            int prev = (index - 1 + maxIndex) % maxIndex;
            double x = posBuffer[index].x;
            double y = posBuffer[index].y;
            double x_prev = posBuffer[prev].x;
            double y_prev = posBuffer[prev].y;

            if (y < current_y - 100 || y > current_y + 100) {
                return false;
            }
            if (x < current_x - 100 || x > current_x + 100) {
                return false;
            } else {
                detected = true;
            }

        }
        reset();
        return detected;
    }

    private boolean isSlideRight(Point[] posBuffer) {
        boolean detected = false;
        double current_x = posBuffer[currentIndex1].x;
        double current_y = posBuffer[currentIndex1].y;

        for (int i = 0; i < posBuffer.length; i++) {
            int index = (currentIndex1 - i) % maxIndex;
            int prev = (index - 1 + maxIndex) % maxIndex;
            double x = posBuffer[index].x;
            double y = posBuffer[index].y;
            double x_prev = posBuffer[prev].x;
            double y_prev = posBuffer[prev].y;

            if (y < current_y - 100 || y > current_y + 100) {
                return false;
            }
            if (x <= x_prev + 50) {
                return false;
            } else {
                detected = true;
            }

        }
        reset();
        return detected;
    }

    private boolean isSlideLeft(Point[] posBuffer) {
        boolean detected = false;
        double current_x = posBuffer[currentIndex1].x;
        double current_y = posBuffer[currentIndex1].y;

        for (int i = 0; i < posBuffer.length; i++) {
            int index = (currentIndex1 - i) % maxIndex;
            int prev = (index - 1 + maxIndex) % maxIndex;
            double x = posBuffer[index].x;
            double y = posBuffer[index].y;
            double x_prev = posBuffer[prev].x;
            double y_prev = posBuffer[prev].y;

            if (y < current_y - 100 || y > current_y + 100) {
                return false;
            }
            if (x >= x_prev - 50) {
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
            int index = (currentIndex1 - i) % maxIndex;
            int prev = (index - 1 + maxIndex) % maxIndex;
            double x = posBuffer[index].x;
            double y = posBuffer[index].y;
            double x_prev = posBuffer[prev].x;
            double y_prev = posBuffer[prev].y;

            if (x < current_x - 100 || x > current_x + 100) {
                return false;
            }
            if (y >= y_prev - 50) {
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
            int index = (currentIndex1 - i) % maxIndex;
            int prev = (index - 1 + maxIndex) % maxIndex;
            double x = posBuffer[index].x;
            double y = posBuffer[index].y;
            double x_prev = posBuffer[prev].x;
            double y_prev = posBuffer[prev].y;

            if (x < current_x - 100 || x > current_x + 100) {
                return false;
            }
            if (y <= y_prev + 50) {
                return false;
            } else {
                detected = true;
            }
        }
        reset();
        return detected;
    }

    private void reset(){
        for (int i = 0; i < maxIndex; i++) {
            posBuffer1[i] = null;
            posBuffer2[i] = null;
        }
        initGesture1=null;
        initGesture2=null;

    }
}
