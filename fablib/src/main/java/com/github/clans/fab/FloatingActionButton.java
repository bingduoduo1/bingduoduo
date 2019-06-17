package com.github.clans.fab;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

public class FloatingActionButton extends ImageButton {
    
    public static final int SIZE_NORMAL = 0;
    public static final int SIZE_MINI = 1;
    
    int mfabsize;
    boolean mshowshadow;
    int mshadowcolor;
    int mshadowradius = Util.dpToPx(getContext(), 4f);
    int mshadowxoffset = Util.dpToPx(getContext(), 1f);
    int mshadowyoffset = Util.dpToPx(getContext(), 3f);
    
    private static final Xfermode PORTER_DUFF_CLEAR = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private static final long PAUSE_GROWING_TIME = 200;
    private static final double BAR_SPIN_CYCLE_TIME = 500;
    private static final int BAR_MAX_LENGTH = 270;
    
    private int mcolornormal;
    private int mcolorpressed;
    private int mcolordisabled;
    private int mcolorripple;
    private Drawable micon;
    private int miconsize = Util.dpToPx(getContext(), 24f);
    private Animation mshowanimation;
    private Animation mhideanimation;
    private String mlabeltext;
    private OnClickListener mclicklistener;
    private Drawable mbackgrounddrawable;
    private boolean musingelevation;
    private boolean musingelevationcompat;
    
    // Progress
    private boolean mprogressbarenabled;
    private int mprogresswidth = Util.dpToPx(getContext(), 6f);
    private int mprogresscolor;
    private int mprogressbackgroundcolor;
    private boolean mshouldupdatebuttonposition;
    private float moriginalx = -1;
    private float moriginaly = -1;
    private boolean mbuttonpositionsaved;
    private RectF mprogresscirclebounds = new RectF();
    private Paint mbackgroundpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mprogresspaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean mprogressindeterminate;
    private long mlasttimeanimated;
    private float mspinspeed = 195.0f; // The amount of degrees per second
    private long mpausedtimewithoutgrowing = 0;
    private double mtimestartgrowing;
    private boolean mbargrowingfromfront = true;
    private int mbarlength = 16;
    private float mbarextralength;
    private float mcurrentprogress;
    private float mtargetprogress;
    private int mprogress;
    private boolean manimateprogress;
    private boolean mshouldprogressindeterminate;
    private boolean mshouldsetprogress;
    private int mprogressmax = 100;
    private boolean mshowprogressbackground;
    
    public FloatingActionButton(Context context) {
        this(context, null);
    }
    
    public FloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }
    
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton, defStyleAttr, 0);
        mcolornormal = attr.getColor(R.styleable.FloatingActionButton_fab_colorNormal, 0xFFDA4336);
        mcolorpressed = attr.getColor(R.styleable.FloatingActionButton_fab_colorPressed, 0xFFE75043);
        mcolordisabled = attr.getColor(R.styleable.FloatingActionButton_fab_colorDisabled, 0xFFAAAAAA);
        mcolorripple = attr.getColor(R.styleable.FloatingActionButton_fab_colorRipple, 0x99FFFFFF);
        mshowshadow = attr.getBoolean(R.styleable.FloatingActionButton_fab_showShadow, true);
        mshadowcolor = attr.getColor(R.styleable.FloatingActionButton_fab_shadowColor, 0x66000000);
        mshadowradius = attr.getDimensionPixelSize(R.styleable.FloatingActionButton_fab_shadowRadius, mshadowradius);
        mshadowxoffset = attr.getDimensionPixelSize(R.styleable.FloatingActionButton_fab_shadowXOffset, mshadowxoffset);
        mshadowyoffset = attr.getDimensionPixelSize(R.styleable.FloatingActionButton_fab_shadowYOffset, mshadowyoffset);
        mfabsize = attr.getInt(R.styleable.FloatingActionButton_fab_size, SIZE_NORMAL);
        mlabeltext = attr.getString(R.styleable.FloatingActionButton_fab_label);
        mshouldprogressindeterminate =
                attr.getBoolean(R.styleable.FloatingActionButton_fab_progress_indeterminate, false);
        mprogresscolor = attr.getColor(R.styleable.FloatingActionButton_fab_progress_color, 0xFF009688);
        mprogressbackgroundcolor =
                attr.getColor(R.styleable.FloatingActionButton_fab_progress_backgroundColor, 0x4D000000);
        mprogressmax = attr.getInt(R.styleable.FloatingActionButton_fab_progress_max, mprogressmax);
        mshowprogressbackground = attr.getBoolean(R.styleable.FloatingActionButton_fab_progress_showBackground, true);
        
        if (attr.hasValue(R.styleable.FloatingActionButton_fab_progress)) {
            mprogress = attr.getInt(R.styleable.FloatingActionButton_fab_progress, 0);
            mshouldsetprogress = true;
        }
        
        if (attr.hasValue(R.styleable.FloatingActionButton_fab_elevationCompat)) {
            float elevation = attr.getDimensionPixelOffset(R.styleable.FloatingActionButton_fab_elevationCompat, 0);
            if (isInEditMode()) {
                setElevation(elevation);
            } else {
                setElevationCompat(elevation);
            }
        }
        
        initShowAnimation(attr);
        initHideAnimation(attr);
        attr.recycle();
        
        if (isInEditMode()) {
            if (mshouldprogressindeterminate) {
                setIndeterminate(true);
            } else if (mshouldsetprogress) {
                saveButtonOriginalPosition();
                setProgress(mprogress, false);
            }
        }
        
        setClickable(true);
    }
    
    private void initShowAnimation(TypedArray attr) {
        int resourceId = attr.getResourceId(R.styleable.FloatingActionButton_fab_showAnimation, R.anim.fab_scale_up);
        mshowanimation = AnimationUtils.loadAnimation(getContext(), resourceId);
    }
    
    private void initHideAnimation(TypedArray attr) {
        int resourceId = attr.getResourceId(R.styleable.FloatingActionButton_fab_hideAnimation, R.anim.fab_scale_down);
        mhideanimation = AnimationUtils.loadAnimation(getContext(), resourceId);
    }
    
    private int getCircleSize() {
        return getResources()
                .getDimensionPixelSize(mfabsize == SIZE_NORMAL ? R.dimen.fab_size_normal : R.dimen.fab_size_mini);
    }
    
    private int calculateMeasuredWidth() {
        int width = getCircleSize() + calculateShadowWidth();
        if (mprogressbarenabled) {
            width += mprogresswidth * 2;
        }
        return width;
    }
    
    private int calculateMeasuredHeight() {
        int height = getCircleSize() + calculateShadowHeight();
        if (mprogressbarenabled) {
            height += mprogresswidth * 2;
        }
        return height;
    }
    
    int calculateShadowWidth() {
        return hasShadow() ? getShadowX() * 2 : 0;
    }
    
    int calculateShadowHeight() {
        return hasShadow() ? getShadowY() * 2 : 0;
    }
    
    private int getShadowX() {
        return mshadowradius + Math.abs(mshadowxoffset);
    }
    
    private int getShadowY() {
        return mshadowradius + Math.abs(mshadowyoffset);
    }
    
    private float calculateCenterX() {
        return (float) (getMeasuredWidth() / 2);
    }
    
    private float calculateCenterY() {
        return (float) (getMeasuredHeight() / 2);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(calculateMeasuredWidth(), calculateMeasuredHeight());
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (mprogressbarenabled) {
            if (mshowprogressbackground) {
                canvas.drawArc(mprogresscirclebounds, 360, 360, false, mbackgroundpaint);
            }
            
            boolean shouldInvalidate = false;
            
            if (mprogressindeterminate) {
                shouldInvalidate = true;
                
                long deltaTime = SystemClock.uptimeMillis() - mlasttimeanimated;
                float deltaNormalized = deltaTime * mspinspeed / 1000.0f;
                
                updateProgressLength(deltaTime);
                
                mcurrentprogress += deltaNormalized;
                if (mcurrentprogress > 360f) {
                    mcurrentprogress -= 360f;
                }
                
                mlasttimeanimated = SystemClock.uptimeMillis();
                float from = mcurrentprogress - 90;
                float to = mbarlength + mbarextralength;
                
                if (isInEditMode()) {
                    from = 0;
                    to = 135;
                }
                
                canvas.drawArc(mprogresscirclebounds, from, to, false, mprogresspaint);
            } else {
                if (mcurrentprogress != mtargetprogress) {
                    shouldInvalidate = true;
                    float deltaTime = (float) (SystemClock.uptimeMillis() - mlasttimeanimated) / 1000;
                    float deltaNormalized = deltaTime * mspinspeed;
                    
                    if (mcurrentprogress > mtargetprogress) {
                        mcurrentprogress = Math.max(mcurrentprogress - deltaNormalized, mtargetprogress);
                    } else {
                        mcurrentprogress = Math.min(mcurrentprogress + deltaNormalized, mtargetprogress);
                    }
                    mlasttimeanimated = SystemClock.uptimeMillis();
                }
                
                canvas.drawArc(mprogresscirclebounds, -90, mcurrentprogress, false, mprogresspaint);
            }
            
            if (shouldInvalidate) {
                invalidate();
            }
        }
    }
    
    private void updateProgressLength(long deltaTimeInMillis) {
        if (mpausedtimewithoutgrowing >= PAUSE_GROWING_TIME) {
            mtimestartgrowing += deltaTimeInMillis;
            
            if (mtimestartgrowing > BAR_SPIN_CYCLE_TIME) {
                mtimestartgrowing -= BAR_SPIN_CYCLE_TIME;
                mpausedtimewithoutgrowing = 0;
                mbargrowingfromfront = !mbargrowingfromfront;
            }
            
            float distance = (float) Math.cos((mtimestartgrowing / BAR_SPIN_CYCLE_TIME + 1) * Math.PI) / 2 + 0.5f;
            float length = BAR_MAX_LENGTH - mbarlength;
            
            if (mbargrowingfromfront) {
                mbarextralength = distance * length;
            } else {
                float newLength = length * (1 - distance);
                mcurrentprogress += (mbarextralength - newLength);
                mbarextralength = newLength;
            }
        } else {
            mpausedtimewithoutgrowing += deltaTimeInMillis;
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        saveButtonOriginalPosition();
        
        if (mshouldprogressindeterminate) {
            setIndeterminate(true);
            mshouldprogressindeterminate = false;
        } else if (mshouldsetprogress) {
            setProgress(mprogress, manimateprogress);
            mshouldsetprogress = false;
        } else if (mshouldupdatebuttonposition) {
            updateButtonPosition();
            mshouldupdatebuttonposition = false;
        }
        super.onSizeChanged(w, h, oldw, oldh);
        
        setupProgressBounds();
        setupProgressBarPaints();
        updateBackground();
    }
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if (params instanceof ViewGroup.MarginLayoutParams && musingelevationcompat) {
            ((ViewGroup.MarginLayoutParams) params).leftMargin += getShadowX();
            ((ViewGroup.MarginLayoutParams) params).topMargin += getShadowY();
            ((ViewGroup.MarginLayoutParams) params).rightMargin += getShadowX();
            ((ViewGroup.MarginLayoutParams) params).bottomMargin += getShadowY();
        }
        super.setLayoutParams(params);
    }
    
    void updateBackground() {
        LayerDrawable layerDrawable;
        if (hasShadow()) {
            layerDrawable = new LayerDrawable(new Drawable[] { new Shadow(), createFillDrawable(), getIconDrawable() });
        } else {
            layerDrawable = new LayerDrawable(new Drawable[] { createFillDrawable(), getIconDrawable() });
        }
        
        int iconSize = -1;
        if (getIconDrawable() != null) {
            iconSize = Math.max(getIconDrawable().getIntrinsicWidth(), getIconDrawable().getIntrinsicHeight());
        }
        int iconOffset = (getCircleSize() - (iconSize > 0 ? iconSize : miconsize)) / 2;
        int circleInsetHorizontal = hasShadow() ? mshadowradius + Math.abs(mshadowxoffset) : 0;
        int circleInsetVertical = hasShadow() ? mshadowradius + Math.abs(mshadowyoffset) : 0;
        
        if (mprogressbarenabled) {
            circleInsetHorizontal += mprogresswidth;
            circleInsetVertical += mprogresswidth;
        }
        
        /*
         * layerDrawable.setLayerInset( mshowshadow ? 1 : 0, circleInsetHorizontal, circleInsetVertical,
         * circleInsetHorizontal, circleInsetVertical );
         */
        layerDrawable.setLayerInset(hasShadow() ? 2 : 1, circleInsetHorizontal + iconOffset,
                circleInsetVertical + iconOffset, circleInsetHorizontal + iconOffset, circleInsetVertical + iconOffset);
        
        setBackgroundCompat(layerDrawable);
    }
    
    protected Drawable getIconDrawable() {
        if (micon != null) {
            return micon;
        } else {
            return new ColorDrawable(Color.TRANSPARENT);
        }
    }
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Drawable createFillDrawable() {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[] { -android.R.attr.state_enabled }, createCircleDrawable(mcolordisabled));
        drawable.addState(new int[] { android.R.attr.state_pressed }, createCircleDrawable(mcolorpressed));
        drawable.addState(new int[] {}, createCircleDrawable(mcolornormal));
        
        if (Util.hasLollipop()) {
            RippleDrawable ripple = new RippleDrawable(
                    new ColorStateList(new int[][] { {} }, new int[] { mcolorripple }), drawable, null);
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
    
    private Drawable createCircleDrawable(int color) {
        CircleDrawable shapeDrawable = new CircleDrawable(new OvalShape());
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }
    
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setBackgroundCompat(Drawable drawable) {
        if (Util.hasJellyBean()) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }
    
    private void saveButtonOriginalPosition() {
        if (!mbuttonpositionsaved) {
            if (moriginalx == -1) {
                moriginalx = getX();
            }
            
            if (moriginaly == -1) {
                moriginaly = getY();
            }
            
            mbuttonpositionsaved = true;
        }
    }
    
    private void updateButtonPosition() {
        float x;
        float y;
        if (mprogressbarenabled) {
            x = moriginalx > getX() ? getX() + mprogresswidth : getX() - mprogresswidth;
            y = moriginaly > getY() ? getY() + mprogresswidth : getY() - mprogresswidth;
        } else {
            x = moriginalx;
            y = moriginaly;
        }
        setX(x);
        setY(y);
    }
    
    private void setupProgressBarPaints() {
        mbackgroundpaint.setColor(mprogressbackgroundcolor);
        mbackgroundpaint.setStyle(Paint.Style.STROKE);
        mbackgroundpaint.setStrokeWidth(mprogresswidth);
        
        mprogresspaint.setColor(mprogresscolor);
        mprogresspaint.setStyle(Paint.Style.STROKE);
        mprogresspaint.setStrokeWidth(mprogresswidth);
    }
    
    private void setupProgressBounds() {
        int circleInsetHorizontal = hasShadow() ? getShadowX() : 0;
        int circleInsetVertical = hasShadow() ? getShadowY() : 0;
        mprogresscirclebounds =
                new RectF(circleInsetHorizontal + mprogresswidth / 2, circleInsetVertical + mprogresswidth / 2,
                        calculateMeasuredWidth() - circleInsetHorizontal - mprogresswidth / 2,
                        calculateMeasuredHeight() - circleInsetVertical - mprogresswidth / 2);
    }
    
    Animation getShowAnimation() {
        return mshowanimation;
    }
    
    Animation getHideAnimation() {
        return mhideanimation;
    }
    
    void playShowAnimation() {
        mhideanimation.cancel();
        startAnimation(mshowanimation);
    }
    
    void playHideAnimation() {
        mshowanimation.cancel();
        startAnimation(mhideanimation);
    }
    
    OnClickListener getOnClickListener() {
        return mclicklistener;
    }
    
    Label getLabelView() {
        return (Label) getTag(R.id.fab_label);
    }
    
    void setColors(int colorNormal, int colorPressed, int colorRipple) {
        mcolornormal = colorNormal;
        mcolorpressed = colorPressed;
        mcolorripple = colorRipple;
    }
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void onActionDown() {
        if (mbackgrounddrawable instanceof StateListDrawable) {
            StateListDrawable drawable = (StateListDrawable) mbackgrounddrawable;
            drawable.setState(new int[] { android.R.attr.state_enabled, android.R.attr.state_pressed });
        } else if (Util.hasLollipop()) {
            RippleDrawable ripple = (RippleDrawable) mbackgrounddrawable;
            ripple.setState(new int[] { android.R.attr.state_enabled, android.R.attr.state_pressed });
            ripple.setHotspot(calculateCenterX(), calculateCenterY());
            ripple.setVisible(true, true);
        }
    }
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void onActionUp() {
        if (mbackgrounddrawable instanceof StateListDrawable) {
            StateListDrawable drawable = (StateListDrawable) mbackgrounddrawable;
            drawable.setState(new int[] { android.R.attr.state_enabled });
        } else if (Util.hasLollipop()) {
            RippleDrawable ripple = (RippleDrawable) mbackgrounddrawable;
            ripple.setState(new int[] { android.R.attr.state_enabled });
            ripple.setHotspot(calculateCenterX(), calculateCenterY());
            ripple.setVisible(true, true);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mclicklistener != null && isEnabled()) {
            Label label = (Label) getTag(R.id.fab_label);
            if (label == null)
            {
                return super.onTouchEvent(event);
            }
            
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_UP:
                    if (label != null) {
                        label.onActionUp();
                    }
                    onActionUp();
                    break;
                
                case MotionEvent.ACTION_CANCEL:
                    if (label != null) {
                        label.onActionUp();
                    }
                    onActionUp();
                    break;
                default:
                    break;
            }
            mgesturedetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }
    
    GestureDetector mgesturedetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        
        @Override
        public boolean onDown(MotionEvent e) {
            Label label = (Label) getTag(R.id.fab_label);
            if (label != null) {
                label.onActionDown();
            }
            onActionDown();
            return super.onDown(e);
        }
        
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Label label = (Label) getTag(R.id.fab_label);
            if (label != null) {
                label.onActionUp();
            }
            onActionUp();
            return super.onSingleTapUp(e);
        }
    });
    
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        
        ProgressSavedState ss = new ProgressSavedState(superState);
        
        ss.mcurrentprogress = this.mcurrentprogress;
        ss.mtargetprogress = this.mtargetprogress;
        ss.mspinspeed = this.mspinspeed;
        ss.mprogresswidth = this.mprogresswidth;
        ss.mprogresscolor = this.mprogresscolor;
        ss.mprogressbackgroundcolor = this.mprogressbackgroundcolor;
        ss.mshouldprogressindeterminate = this.mprogressindeterminate;
        ss.mshouldsetprogress = this.mprogressbarenabled && mprogress > 0 && !this.mprogressindeterminate;
        ss.mprogress = this.mprogress;
        ss.manimateprogress = this.manimateprogress;
        ss.mshowprogressbackground = this.mshowprogressbackground;
        
        return ss;
    }
    
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof ProgressSavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        
        ProgressSavedState ss = (ProgressSavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        
        this.mcurrentprogress = ss.mcurrentprogress;
        this.mtargetprogress = ss.mtargetprogress;
        this.mspinspeed = ss.mspinspeed;
        this.mprogresswidth = ss.mprogresswidth;
        this.mprogresscolor = ss.mprogresscolor;
        this.mprogressbackgroundcolor = ss.mprogressbackgroundcolor;
        this.mshouldprogressindeterminate = ss.mshouldprogressindeterminate;
        this.mshouldsetprogress = ss.mshouldsetprogress;
        this.mprogress = ss.mprogress;
        this.manimateprogress = ss.manimateprogress;
        this.mshowprogressbackground = ss.mshowprogressbackground;
        
        this.mlasttimeanimated = SystemClock.uptimeMillis();
    }
    
    private class CircleDrawable extends ShapeDrawable {
        
        private int circleInsetHorizontal;
        private int circleInsetVertical;
        
        private CircleDrawable() {
        }
        
        private CircleDrawable(Shape s) {
            super(s);
            circleInsetHorizontal = hasShadow() ? mshadowradius + Math.abs(mshadowxoffset) : 0;
            circleInsetVertical = hasShadow() ? mshadowradius + Math.abs(mshadowyoffset) : 0;
            
            if (mprogressbarenabled) {
                circleInsetHorizontal += mprogresswidth;
                circleInsetVertical += mprogresswidth;
            }
        }
        
        @Override
        public void draw(Canvas canvas) {
            setBounds(circleInsetHorizontal, circleInsetVertical, calculateMeasuredWidth() - circleInsetHorizontal,
                    calculateMeasuredHeight() - circleInsetVertical);
            super.draw(canvas);
        }
    }
    
    private class Shadow extends Drawable {
        
        private Paint mpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint merase = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float mradius;
        
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
            
            mradius = getCircleSize() / 2;
            
            if (mprogressbarenabled && mshowprogressbackground) {
                mradius += mprogresswidth;
            }
        }
        
        @Override
        public void draw(Canvas canvas) {
            canvas.drawCircle(calculateCenterX(), calculateCenterY(), mradius, mpaint);
            canvas.drawCircle(calculateCenterX(), calculateCenterY(), mradius, merase);
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
    
    static class ProgressSavedState extends BaseSavedState {
        
        float mcurrentprogress;
        float mtargetprogress;
        float mspinspeed;
        int mprogress;
        int mprogresswidth;
        int mprogresscolor;
        int mprogressbackgroundcolor;
        boolean mprogressbarenabled;
        boolean mprogressbarvisibilitychanged;
        boolean mprogressindeterminate;
        boolean mshouldprogressindeterminate;
        boolean mshouldsetprogress;
        boolean manimateprogress;
        boolean mshowprogressbackground;
        
        ProgressSavedState(Parcelable superState) {
            super(superState);
        }
        
        private ProgressSavedState(Parcel in) {
            super(in);
            this.mcurrentprogress = in.readFloat();
            this.mtargetprogress = in.readFloat();
            this.mprogressbarenabled = in.readInt() != 0;
            this.mspinspeed = in.readFloat();
            this.mprogress = in.readInt();
            this.mprogresswidth = in.readInt();
            this.mprogresscolor = in.readInt();
            this.mprogressbackgroundcolor = in.readInt();
            this.mprogressbarvisibilitychanged = in.readInt() != 0;
            this.mprogressindeterminate = in.readInt() != 0;
            this.mshouldprogressindeterminate = in.readInt() != 0;
            this.mshouldsetprogress = in.readInt() != 0;
            this.manimateprogress = in.readInt() != 0;
            this.mshowprogressbackground = in.readInt() != 0;
        }
        
        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(this.mcurrentprogress);
            out.writeFloat(this.mtargetprogress);
            out.writeInt((mprogressbarenabled ? 1 : 0));
            out.writeFloat(this.mspinspeed);
            out.writeInt(this.mprogress);
            out.writeInt(this.mprogresswidth);
            out.writeInt(this.mprogresscolor);
            out.writeInt(this.mprogressbackgroundcolor);
            out.writeInt(this.mprogressbarvisibilitychanged ? 1 : 0);
            out.writeInt(this.mprogressindeterminate ? 1 : 0);
            out.writeInt(this.mshouldprogressindeterminate ? 1 : 0);
            out.writeInt(this.mshouldsetprogress ? 1 : 0);
            out.writeInt(this.manimateprogress ? 1 : 0);
            out.writeInt(this.mshowprogressbackground ? 1 : 0);
        }
        
        public static final Parcelable.Creator<ProgressSavedState> CREATOR =
            new Parcelable.Creator<ProgressSavedState>() {
                public ProgressSavedState createFromParcel(Parcel in) {
                    return new ProgressSavedState(in);
                }

                public ProgressSavedState[] newArray(int size) {
                    return new ProgressSavedState[size];
                }
            };
    }
    
    /* ===== API methods ===== */
    
    @Override
    public void setImageDrawable(Drawable drawable) {
        if (micon != drawable) {
            micon = drawable;
            updateBackground();
        }
    }
    
    @Override
    public void setImageResource(int resId) {
        Drawable drawable = getResources().getDrawable(resId);
        if (micon != drawable) {
            micon = drawable;
            updateBackground();
        }
    }
    
    @Override
    public void setOnClickListener(final OnClickListener l) {
        super.setOnClickListener(l);
        mclicklistener = l;
        View label = (View) getTag(R.id.fab_label);
        if (label != null) {
            label.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mclicklistener != null) {
                        mclicklistener.onClick(FloatingActionButton.this);
                    }
                }
            });
        }
    }
    
    /**
     * Sets the size of the <b>FloatingActionButton</b> and invalidates its layout.
     *
     * @param size size of the <b>FloatingActionButton</b>. Accepted values: SIZE_NORMAL, SIZE_MINI.
     */
    public void setButtonSize(int size) {
        if (size != SIZE_NORMAL && size != SIZE_MINI) {
            throw new IllegalArgumentException("Use @FabSize constants only!");
        }
        
        if (mfabsize != size) {
            mfabsize = size;
            updateBackground();
        }
    }
    
    public int getButtonSize() {
        return mfabsize;
    }
    
    public void setColorNormal(int color) {
        if (mcolornormal != color) {
            mcolornormal = color;
            updateBackground();
        }
    }
    
    public void setColorNormalResId(int colorResId) {
        setColorNormal(getResources().getColor(colorResId));
    }
    
    public int getColorNormal() {
        return mcolornormal;
    }
    
    public void setColorPressed(int color) {
        if (color != mcolorpressed) {
            mcolorpressed = color;
            updateBackground();
        }
    }
    
    public void setColorPressedResId(int colorResId) {
        setColorPressed(getResources().getColor(colorResId));
    }
    
    public int getColorPressed() {
        return mcolorpressed;
    }
    
    public void setColorRipple(int color) {
        if (color != mcolorripple) {
            mcolorripple = color;
            updateBackground();
        }
    }
    
    public void setColorRippleResId(int colorResId) {
        setColorRipple(getResources().getColor(colorResId));
    }
    
    public int getColorRipple() {
        return mcolorripple;
    }
    
    public void setColorDisabled(int color) {
        if (color != mcolordisabled) {
            mcolordisabled = color;
            updateBackground();
        }
    }
    
    public void setColorDisabledResId(int colorResId) {
        setColorDisabled(getResources().getColor(colorResId));
    }
    
    public int getColorDisabled() {
        return mcolordisabled;
    }
    
    public void setShowShadow(boolean show) {
        if (mshowshadow != show) {
            mshowshadow = show;
            updateBackground();
        }
    }
    
    public boolean hasShadow() {
        return !musingelevation && mshowshadow;
    }
    
    /**
     * Sets the shadow radius of the <b>FloatingActionButton</b> and invalidates its layout.
     *
     * @param dimenResId the resource identifier of the dimension
     */
    public void setShadowRadius(int dimenResId) {
        int shadowRadius = getResources().getDimensionPixelSize(dimenResId);
        if (mshadowradius != shadowRadius) {
            mshadowradius = shadowRadius;
            requestLayout();
            updateBackground();
        }
    }
    
    /**
     * Sets the shadow radius of the <b>FloatingActionButton</b> and invalidates its layout.
     * <p>
     * Must be specified in density-independent (dp) pixels, which are then converted into actual
     * pixels (px).
     *
     * @param shadowRadiusDp shadow radius specified in density-independent (dp) pixels
     */
    public void setShadowRadius(float shadowRadiusDp) {
        mshadowradius = Util.dpToPx(getContext(), shadowRadiusDp);
        requestLayout();
        updateBackground();
    }
    
    public int getShadowRadius() {
        return mshadowradius;
    }
    
    /**
     * Sets the shadow x offset of the <b>FloatingActionButton</b> and invalidates its layout.
     *
     * @param dimenResId the resource identifier of the dimension
     */
    public void setShadowXOffset(int dimenResId) {
        int shadowXOffset = getResources().getDimensionPixelSize(dimenResId);
        if (mshadowxoffset != shadowXOffset) {
            mshadowxoffset = shadowXOffset;
            requestLayout();
            updateBackground();
        }
    }
    
    /**
     * Sets the shadow x offset of the <b>FloatingActionButton</b> and invalidates its layout.
     * <p>
     * Must be specified in density-independent (dp) pixels, which are then converted into actual
     * pixels (px).
     *
     * @param shadowXOffsetDp shadow radius specified in density-independent (dp) pixels
     */
    public void setShadowXOffset(float shadowXOffsetDp) {
        mshadowxoffset = Util.dpToPx(getContext(), shadowXOffsetDp);
        requestLayout();
        updateBackground();
    }
    
    public int getShadowXOffset() {
        return mshadowxoffset;
    }
    
    /**
     * Sets the shadow y offset of the <b>FloatingActionButton</b> and invalidates its layout.
     *
     * @param dimenResId the resource identifier of the dimension
     */
    public void setShadowYOffset(int dimenResId) {
        int shadowYOffset = getResources().getDimensionPixelSize(dimenResId);
        if (mshadowyoffset != shadowYOffset) {
            mshadowyoffset = shadowYOffset;
            requestLayout();
            updateBackground();
        }
    }
    
    /**
     * Sets the shadow y offset of the <b>FloatingActionButton</b> and invalidates its layout.
     * <p>
     * Must be specified in density-independent (dp) pixels, which are then converted into actual
     * pixels (px).
     *
     * @param shadowYOffsetDp shadow radius specified in density-independent (dp) pixels
     */
    public void setShadowYOffset(float shadowYOffsetDp) {
        mshadowyoffset = Util.dpToPx(getContext(), shadowYOffsetDp);
        requestLayout();
        updateBackground();
    }
    
    public int getShadowYOffset() {
        return mshadowyoffset;
    }
    
    public void setShadowColorResource(int colorResId) {
        int shadowColor = getResources().getColor(colorResId);
        if (mshadowcolor != shadowColor) {
            mshadowcolor = shadowColor;
            updateBackground();
        }
    }
    
    public void setShadowColor(int color) {
        if (mshadowcolor != color) {
            mshadowcolor = color;
            updateBackground();
        }
    }
    
    public int getShadowColor() {
        return mshadowcolor;
    }
    
    /**
     * Checks whether <b>FloatingActionButton</b> is hidden
     *
     * @return true if <b>FloatingActionButton</b> is hidden, false otherwise
     */
    public boolean isHidden() {
        return getVisibility() == INVISIBLE;
    }
    
    /**
     * Makes the <b>FloatingActionButton</b> to appear and sets its visibility to {@link #VISIBLE}
     *
     * @param animate if true - plays "show animation"
     */
    public void show(boolean animate) {
        if (isHidden()) {
            if (animate) {
                playShowAnimation();
            }
            super.setVisibility(VISIBLE);
        }
    }
    
    /**
     * Makes the <b>FloatingActionButton</b> to disappear and sets its visibility to {@link #INVISIBLE}
     *
     * @param animate if true - plays "hide animation"
     */
    public void hide(boolean animate) {
        if (!isHidden()) {
            if (animate) {
                playHideAnimation();
            }
            super.setVisibility(INVISIBLE);
        }
    }
    
    public void toggle(boolean animate) {
        if (isHidden()) {
            show(animate);
        } else {
            hide(animate);
        }
    }
    
    public void setLabelText(String text) {
        mlabeltext = text;
        TextView labelView = getLabelView();
        if (labelView != null) {
            labelView.setText(text);
        }
    }
    
    public String getLabelText() {
        return mlabeltext;
    }
    
    public void setShowAnimation(Animation showAnimation) {
        mshowanimation = showAnimation;
    }
    
    public void setHideAnimation(Animation hideAnimation) {
        mhideanimation = hideAnimation;
    }
    
    public void setLabelVisibility(int visibility) {
        Label labelView = getLabelView();
        if (labelView != null) {
            labelView.setVisibility(visibility);
            labelView.setHandleVisibilityChanges(visibility == VISIBLE);
        }
    }
    
    public int getLabelVisibility() {
        TextView labelView = getLabelView();
        if (labelView != null) {
            return labelView.getVisibility();
        }
        
        return -1;
    }
    
    @Override
    public void setElevation(float elevation) {
        if (Util.hasLollipop() && elevation > 0) {
            super.setElevation(elevation);
            if (!isInEditMode()) {
                musingelevation = true;
                mshowshadow = false;
            }
            updateBackground();
        }
    }
    
    /**
     * Sets the shadow color and radius to mimic the native elevation.
     *
     * <p><b>API 21+</b>: Sets the native elevation of this view, in pixels. Updates margins to
     * make the view hold its position in layout across different platform versions.</p>
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setElevationCompat(float elevation) {
        mshadowcolor = 0x26000000;
        mshadowradius = Math.round(elevation / 2);
        mshadowxoffset = 0;
        mshadowyoffset = Math.round(mfabsize == SIZE_NORMAL ? elevation : elevation / 2);
        
        if (Util.hasLollipop()) {
            super.setElevation(elevation);
            musingelevationcompat = true;
            mshowshadow = false;
            updateBackground();
            
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (layoutParams != null) {
                setLayoutParams(layoutParams);
            }
        } else {
            mshowshadow = true;
            updateBackground();
        }
    }
    
    /**
     * <p>Change the indeterminate mode for the progress bar. In indeterminate
     * mode, the progress is ignored and the progress bar shows an infinite
     * animation instead.</p>
     *
     * @param indeterminate true to enable the indeterminate mode
     */
    public synchronized void setIndeterminate(boolean indeterminate) {
        if (!indeterminate) {
            mcurrentprogress = 0.0f;
        }
        
        mprogressbarenabled = indeterminate;
        mshouldupdatebuttonposition = true;
        mprogressindeterminate = indeterminate;
        mlasttimeanimated = SystemClock.uptimeMillis();
        setupProgressBounds();
        // saveButtonOriginalPosition();
        updateBackground();
    }
    
    public synchronized void setMax(int max) {
        mprogressmax = max;
    }
    
    public synchronized int getMax() {
        return mprogressmax;
    }
    
    public synchronized void setProgress(int progress, boolean animate) {
        if (mprogressindeterminate)
        {
            return;
        }
        
        mprogress = progress;
        manimateprogress = animate;
        
        if (!mbuttonpositionsaved) {
            mshouldsetprogress = true;
            return;
        }
        
        mprogressbarenabled = true;
        mshouldupdatebuttonposition = true;
        setupProgressBounds();
        saveButtonOriginalPosition();
        updateBackground();
        
        if (progress < 0) {
            progress = 0;
        } else if (progress > mprogressmax) {
            progress = mprogressmax;
        }
        
        if (progress == mtargetprogress) {
            return;
        }
        
        mtargetprogress = mprogressmax > 0 ? (progress / (float) mprogressmax) * 360 : 0;
        mlasttimeanimated = SystemClock.uptimeMillis();
        
        if (!animate) {
            mcurrentprogress = mtargetprogress;
        }
        
        invalidate();
    }
    
    public synchronized int getProgress() {
        return mprogressindeterminate ? 0 : mprogress;
    }
    
    public synchronized void hideProgress() {
        mprogressbarenabled = false;
        mshouldupdatebuttonposition = true;
        updateBackground();
    }
    
    public synchronized void setShowProgressBackground(boolean show) {
        mshowprogressbackground = show;
    }
    
    public synchronized boolean isProgressBackgroundShown() {
        return mshowprogressbackground;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Label label = (Label) getTag(R.id.fab_label);
        if (label != null) {
            label.setEnabled(enabled);
        }
    }
    
    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        Label label = (Label) getTag(R.id.fab_label);
        if (label != null) {
            label.setVisibility(visibility);
        }
    }
    
    /**
     * <b>This will clear all AnimationListeners.</b>
     */
    public void hideButtonInMenu(boolean animate) {
        if (!isHidden() && getVisibility() != GONE) {
            hide(animate);
            
            Label label = getLabelView();
            if (label != null) {
                label.hide(animate);
            }
            
            getHideAnimation().setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                
                @Override
                public void onAnimationEnd(Animation animation) {
                    setVisibility(GONE);
                    getHideAnimation().setAnimationListener(null);
                }
                
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
    }
    
    public void showButtonInMenu(boolean animate) {
        if (getVisibility() == VISIBLE)
        {
            return;
        }
        
        setVisibility(INVISIBLE);
        show(animate);
        Label label = getLabelView();
        if (label != null) {
            label.show(animate);
        }
    }
    
    /**
     * Set the label's background colors
     */
    public void setLabelColors(int colorNormal, int colorPressed, int colorRipple) {
        Label label = getLabelView();
        
        int left = label.getPaddingLeft();
        int top = label.getPaddingTop();
        int right = label.getPaddingRight();
        int bottom = label.getPaddingBottom();
        
        label.setColors(colorNormal, colorPressed, colorRipple);
        label.updateBackground();
        label.setPadding(left, top, right, bottom);
    }
    
    public void setLabelTextColor(int color) {
        getLabelView().setTextColor(color);
    }
    
    public void setLabelTextColor(ColorStateList colors) {
        getLabelView().setTextColor(colors);
    }
}
