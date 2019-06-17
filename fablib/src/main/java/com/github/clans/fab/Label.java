package com.github.clans.fab;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.Animation;
import android.widget.TextView;

public class Label extends TextView {
    
    private static final Xfermode PORTER_DUFF_CLEAR = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    
    private int mshadowradius;
    private int mshadowxoffset;
    private int mshadowyoffset;
    private int mshadowcolor;
    private Drawable mbackgrounddrawable;
    private boolean mshowshadow = true;
    private int mrawwidth;
    private int mrawheight;
    private int mcolornormal;
    private int mcolorpressed;
    private int mcolorripple;
    private int mcornerradius;
    private FloatingActionButton mfab;
    private Animation mshowanimation;
    private Animation mhideanimation;
    private boolean musingstyle;
    private boolean mhandlevisibilitychanges = true;
    
    public Label(Context context) {
        super(context);
    }
    
    public Label(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public Label(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(calculateMeasuredWidth(), calculateMeasuredHeight());
    }
    
    private int calculateMeasuredWidth() {
        if (mrawwidth == 0) {
            mrawwidth = getMeasuredWidth();
        }
        return getMeasuredWidth() + calculateShadowWidth();
    }
    
    private int calculateMeasuredHeight() {
        if (mrawheight == 0) {
            mrawheight = getMeasuredHeight();
        }
        return getMeasuredHeight() + calculateShadowHeight();
    }
    
    int calculateShadowWidth() {
        return mshowshadow ? (mshadowradius + Math.abs(mshadowxoffset)) : 0;
    }
    
    int calculateShadowHeight() {
        return mshowshadow ? (mshadowradius + Math.abs(mshadowyoffset)) : 0;
    }
    
    void updateBackground() {
        LayerDrawable layerDrawable;
        if (mshowshadow) {
            layerDrawable = new LayerDrawable(new Drawable[] { new Shadow(), createFillDrawable() });
            
            int leftInset = mshadowradius + Math.abs(mshadowxoffset);
            int topInset = mshadowradius + Math.abs(mshadowyoffset);
            int rightInset = (mshadowradius + Math.abs(mshadowxoffset));
            int bottomInset = (mshadowradius + Math.abs(mshadowyoffset));
            
            layerDrawable.setLayerInset(1, leftInset, topInset, rightInset, bottomInset);
        } else {
            layerDrawable = new LayerDrawable(new Drawable[] { createFillDrawable() });
        }
        
        setBackgroundCompat(layerDrawable);
    }
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Drawable createFillDrawable() {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[] { android.R.attr.state_pressed }, createRectDrawable(mcolorpressed));
        drawable.addState(new int[] {}, createRectDrawable(mcolornormal));
        
        if (Util.hasLollipop()) {
            RippleDrawable ripple = new RippleDrawable(
                    new ColorStateList(new int[][] { {} }, new int[] {mcolorripple}), drawable, null);
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
            setClipToOutline(true);
            mbackgrounddrawable = ripple;
            return ripple;
        }
        
        mbackgrounddrawable = drawable;
        return drawable;
    }
    
    private Drawable createRectDrawable(int color) {
        RoundRectShape shape = new RoundRectShape(new float[] {mcornerradius, mcornerradius, mcornerradius,
            mcornerradius, mcornerradius, mcornerradius, mcornerradius, mcornerradius}, null, null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(shape);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }
    
    private void setShadow(FloatingActionButton fab) {
        mshadowcolor = fab.getShadowColor();
        mshadowradius = fab.getShadowRadius();
        mshadowxoffset = fab.getShadowXOffset();
        mshadowyoffset = fab.getShadowYOffset();
        mshowshadow = fab.hasShadow();
    }
    
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setBackgroundCompat(Drawable drawable) {
        if (Util.hasJellyBean()) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }
    
    private void playShowAnimation() {
        if (mshowanimation != null) {
            mhideanimation.cancel();
            startAnimation(mshowanimation);
        }
    }
    
    private void playHideAnimation() {
        if (mhideanimation != null) {
            mshowanimation.cancel();
            startAnimation(mhideanimation);
        }
    }
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void onActionDown() {
        if (musingstyle) {
            mbackgrounddrawable = getBackground();
        }
        
        if (mbackgrounddrawable instanceof StateListDrawable) {
            StateListDrawable drawable = (StateListDrawable) mbackgrounddrawable;
            drawable.setState(new int[] { android.R.attr.state_pressed });
        } else if (Util.hasLollipop() && mbackgrounddrawable instanceof RippleDrawable) {
            RippleDrawable ripple = (RippleDrawable) mbackgrounddrawable;
            ripple.setState(new int[] { android.R.attr.state_enabled, android.R.attr.state_pressed });
            ripple.setHotspot(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
            ripple.setVisible(true, true);
        }
        // setPressed(true);
    }
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void onActionUp() {
        if (musingstyle) {
            mbackgrounddrawable = getBackground();
        }
        
        if (mbackgrounddrawable instanceof StateListDrawable) {
            StateListDrawable drawable = (StateListDrawable) mbackgrounddrawable;
            drawable.setState(new int[] {});
        } else if (Util.hasLollipop() && mbackgrounddrawable instanceof RippleDrawable) {
            RippleDrawable ripple = (RippleDrawable) mbackgrounddrawable;
            ripple.setState(new int[] {});
            ripple.setHotspot(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
            ripple.setVisible(true, true);
        }
        // setPressed(false);
    }
    
    void setFab(FloatingActionButton fab) {
        mfab = fab;
        setShadow(fab);
    }
    
    void setShowShadow(boolean show) {
        mshowshadow = show;
    }
    
    void setCornerRadius(int cornerRadius) {
        mcornerradius = cornerRadius;
    }
    
    void setColors(int colorNormal, int colorPressed, int colorRipple) {
        mcolornormal = colorNormal;
        mcolorpressed = colorPressed;
        mcolorripple = colorRipple;
    }
    
    void show(boolean animate) {
        if (animate) {
            playShowAnimation();
        }
        setVisibility(VISIBLE);
    }
    
    void hide(boolean animate) {
        if (animate) {
            playHideAnimation();
        }
        setVisibility(INVISIBLE);
    }
    
    void setShowAnimation(Animation showAnimation) {
        mshowanimation = showAnimation;
    }
    
    void setHideAnimation(Animation hideAnimation) {
        mhideanimation = hideAnimation;
    }
    
    void setUsingStyle(boolean usingStyle) {
        musingstyle = usingStyle;
    }
    
    void setHandleVisibilityChanges(boolean handle) {
        mhandlevisibilitychanges = handle;
    }
    
    boolean isHandleVisibilityChanges() {
        return mhandlevisibilitychanges;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mfab == null || mfab.getOnClickListener() == null || !mfab.isEnabled()) {
            return super.onTouchEvent(event);
        }
        
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                onActionUp();
                mfab.onActionUp();
                break;
            
            case MotionEvent.ACTION_CANCEL:
                onActionUp();
                mfab.onActionUp();
                break;
            default:
                break;
        }
        
        detector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    
    GestureDetector detector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        
        @Override
        public boolean onDown(MotionEvent e) {
            onActionDown();
            if (mfab != null) {
                mfab.onActionDown();
            }
            return super.onDown(e);
        }
        
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onActionUp();
            if (mfab != null) {
                mfab.onActionUp();
            }
            return super.onSingleTapUp(e);
        }
    });
    
    private class Shadow extends Drawable {
        
        private Paint mpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint merase = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        private Shadow() {
            this.init();
        }
        
        private void init() {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
            mpaint.setStyle(Paint.Style.FILL);
            mpaint.setColor(mcolornormal);
            
            merase.setXfermode(PORTER_DUFF_CLEAR);
            
            if (!isInEditMode()) {
                mpaint.setShadowLayer(mshadowradius, mshadowxoffset, mshadowyoffset, mshadowcolor);
            }
        }
        
        @Override
        public void draw(Canvas canvas) {
            RectF shadowRect = new RectF(mshadowradius + Math.abs(mshadowxoffset),
                    mshadowradius + Math.abs(mshadowyoffset), mrawwidth, mrawheight);
            
            canvas.drawRoundRect(shadowRect, mcornerradius, mcornerradius, mpaint);
            canvas.drawRoundRect(shadowRect, mcornerradius, mcornerradius, merase);
        }
        
        @Override
        public void setAlpha(int alpha) {
            
        }
        
        @Override
        public void setColorFilter(ColorFilter cf) {
            
        }
        
        @Override
        public int getOpacity() {
            return 0;
        }
    }
}
