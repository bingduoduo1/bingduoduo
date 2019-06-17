package com.bingduoduo.editor.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;

/**
 * Created by saurabhmathur on 18/01/18.
 */

// 可拖动的fab 保留在这里,之后再想想怎么用
// 需求: 想要一个可以拖动的语音按钮
// 问题: 但是语音的监听是长按, 拖动的实现也是长按拖动

public class DraggableFloatingActionButton extends FloatingActionButton implements View.OnTouchListener {
    
    private static final float CLICK_DRAG_TOLERANCE = 10; // Often, there will be a slight, unintentional, drag when the

    private float downRawX;
    private float downRawY;
    private float dx;
    private float dy;
    
    public DraggableFloatingActionButton(Context context) {
        super(context);
        init();
    }
    
    public DraggableFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public DraggableFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        setOnTouchListener(this);
    }
    
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        
        int action = motionEvent.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            
            downRawX = motionEvent.getRawX();
            downRawY = motionEvent.getRawY();
            dx = view.getX() - downRawX;
            dy = view.getY() - downRawY;
            
            return true; // Consumed
            
        } else if (action == MotionEvent.ACTION_MOVE) {
            
            int viewWidth;
            viewWidth = view.getWidth();
            int viewHeight;
            viewHeight = view.getHeight();
            
            View viewParent = (View) view.getParent();
            int parentWidth = viewParent.getWidth();
            int parentHeight;
            parentHeight = viewParent.getHeight();
            
            float newX = motionEvent.getRawX() + dx;
            newX = Math.max(0, newX); // Don't allow the FAB past the left hand side of the parent
            newX = Math.min(parentWidth - viewWidth, newX); // Don't allow the FAB past the right hand side of the

            float newY = motionEvent.getRawY() + dy;
            newY = Math.max(0, newY); // Don't allow the FAB past the top of the parent
            newY = Math.min(parentHeight - viewHeight, newY); // Don't allow the FAB past the bottom of the parent
            
            view.animate().x(newX).y(newY).setDuration(0).start();
            
            return true; // Consumed
            
        } else if (action == MotionEvent.ACTION_UP) {
            
            float upRawX = motionEvent.getRawX();
            float upRawY = motionEvent.getRawY();
            
            float upDX = upRawX - downRawX;
            float upDY = upRawY - downRawY;
            
            if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY) < CLICK_DRAG_TOLERANCE) { // A click
                return performClick();
            } else { // A drag
                return true; // Consumed
            }
            
        } else {
            return super.onTouchEvent(motionEvent);
        }
        
    }
}
