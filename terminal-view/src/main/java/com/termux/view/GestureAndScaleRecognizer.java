package com.termux.view;

import android.content.Context;
import android.view.GestureDetector;//GestureDetector:手势检测，用于辅助检测用户单击、滑动、长按、双击等行为
import android.view.MotionEvent; // 触摸事件
import android.view.ScaleGestureDetector;//专门用来检测两个手指在屏幕上做缩放的手势用的

/** A combination of {@link GestureDetector} and {@link ScaleGestureDetector}. */
final class GestureAndScaleRecognizer {
    
    public interface Listener {
        
        boolean onSingleTapUp(MotionEvent e);
        
        boolean onDoubleTap(MotionEvent e);
        
        boolean onScroll(MotionEvent e2, float dx, float dy);
        
        boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);
        
        boolean onScale(float focusX, float focusY, float scale);
        
        boolean onDown(float x, float y);
        
        boolean onUp(MotionEvent e);
        
        void onLongPress(MotionEvent e);
    }
    
    private final GestureDetector mgesturedetector;
    private final ScaleGestureDetector mscaledetector;
    final Listener mlistener;
    boolean isAfterLongPress;
    
    public GestureAndScaleRecognizer(Context context, Listener listener) {
        mlistener = listener;
        
        mgesturedetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
                return mlistener.onScroll(e2, dx, dy);
            }
            
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return mlistener.onFling(e1, e2, velocityX, velocityY);
            }
            
            @Override
            public boolean onDown(MotionEvent e) {
                return mlistener.onDown(e.getX(), e.getY());
            }
            
            @Override
            public void onLongPress(MotionEvent e) {
                mlistener.onLongPress(e);
                isAfterLongPress = true;
            }
        }, null, true /* ignoreMultitouch */);
        
        mgesturedetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return mlistener.onSingleTapUp(e);
            }
            
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return mlistener.onDoubleTap(e);
            }
            
            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return true;
            }
        });
        
        mscaledetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }
            
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                return mlistener.onScale(detector.getFocusX(), detector.getFocusY(), detector.getScaleFactor());
            }
        });
        mscaledetector.setQuickScaleEnabled(false);
    }
    
    public void onTouchEvent(MotionEvent event) {
        mgesturedetector.onTouchEvent(event);
        mscaledetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isAfterLongPress = false;
                break;
            case MotionEvent.ACTION_UP:
                if (!isAfterLongPress) {
                    // This behaviour is desired when in e.g. vim with mouse events, where we do not
                    // want to move the cursor when lifting finger after a long press.
                    mlistener.onUp(event);
                }
                break;
            default:
                break;
        }
    }
    
    public boolean isInProgress() {
        return mscaledetector.isInProgress();
    }
    
}
