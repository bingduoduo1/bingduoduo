package com.github.clans.fab;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class FloatingActionMenu extends ViewGroup {
    
    private static final int ANIMATION_DURATION = 300;
    private static final float CLOSED_PLUS_ROTATION = 0f;
    private static final float OPENED_PLUS_ROTATION_LEFT = -90f - 45f;
    private static final float OPENED_PLUS_ROTATION_RIGHT = 90f + 45f;
    
    private static final int OPEN_UP = 0;
    private static final int OPEN_DOWN = 1;
    
    private static final int LABELS_POSITION_LEFT = 0;
    private static final int LABELS_POSITION_RIGHT = 1;
    
    private AnimatorSet mopenanimatorset = new AnimatorSet();
    private AnimatorSet mcloseanimatorset = new AnimatorSet();
    private AnimatorSet micontoggleset;
    
    private int mbuttonspacing = Util.dpToPx(getContext(), 0f);
    private FloatingActionButton mmenubutton;
    private int mmaxbuttonwidth;
    private int mlabelsmargin = Util.dpToPx(getContext(), 0f);
    private int mlabelsverticaloffset = Util.dpToPx(getContext(), 0f);
    private int mbuttonscount;
    private boolean mmenuopened;
    private boolean mismenuopening;
    private Handler muihandler = new Handler();
    private int mlabelsshowanimation;
    private int mlabelshideanimation;
    private int mlabelspaddingtop = Util.dpToPx(getContext(), 4f);
    private int mlabelspaddingright = Util.dpToPx(getContext(), 8f);
    private int mlabelspaddingbottom = Util.dpToPx(getContext(), 4f);
    private int mlabelspaddingleft = Util.dpToPx(getContext(), 8f);
    private ColorStateList mlabelstextcolor;
    private float mlabelstextsize;
    private int mlabelscornerradius = Util.dpToPx(getContext(), 3f);
    private boolean mlabelsshowshadow;
    private int mlabelscolornormal;
    private int mlabelscolorpressed;
    private int mlabelscolorripple;
    private boolean mmenushowshadow;
    private int mmenushadowcolor;
    private float mmenushadowradius = 4f;
    private float mmenushadowxoffset = 1f;
    private float mmenushadowyoffset = 3f;
    private int mmenucolornormal;
    private int mmenucolorpressed;
    private int mmenucolorripple;
    private Drawable micon;
    private int manimationdelayperitem;
    private Interpolator mopeninterpolator;
    private Interpolator mcloseinterpolator;
    private boolean misanimated = true;
    private boolean mlabelssingleline;
    private int mlabelsellipsize;
    private int mlabelsmaxlines;
    private int mmenufabsize;
    private int mlabelsstyle;
    private Typeface mcustomtypefacefromfont;
    private boolean miconanimated = true;
    private ImageView mimagetoggle;
    private Animation mmenubuttonshowanimation;
    private Animation mmenubuttonhideanimation;
    private Animation mimagetoggleshowanimation;
    private Animation mimagetogglehideanimation;
    private boolean mismenubuttonanimationrunning;
    private boolean missetclosedontouchoutside;
    private int mopendirection;
    private OnMenuToggleListener mtogglelistener;
    
    private ValueAnimator mshowbackgroundanimator;
    private ValueAnimator mhidebackgroundanimator;
    private int mbackgroundcolor;
    
    private int mlabelsposition;
    private Context mlabelscontext;
    private String mmenulabeltext;
    private boolean musingmenulabel;
    
    // onTouchListener onTouch
    private float downRawX;
    private float downRawY;
    private float dx;
    private float dy;
    private static final float CLICK_DRAG_TOLERANCE = 10; // Often, there will be a slight, unintentional, drag when the

    public interface OnMenuToggleListener {
        void onMenuToggle(boolean opened);
    }
    
    public FloatingActionMenu(Context context) {
        this(context, null);
    }
    
    public FloatingActionMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public FloatingActionMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    // tmp
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (missetclosedontouchoutside) {
            boolean handled = false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "onTouchEvent: +++++++++++++++++++++++");
                    handled = isOpened();
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "onTouchEvent: =======================");
                    close(misanimated);
                    handled = true;
                    break;
                default:
                    break;
            }
            
            return handled;
        }
        
        return super.onTouchEvent(event);
    }
    
    // tmp
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // onTouchListener
        
        boolean handled = false;
        Log.d(TAG, "onTouch: in On Touch");
        int action = motionEvent.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            Log.d(TAG, "onTouch: action down @@@@@@@@");
            
            downRawX = motionEvent.getRawX();
            downRawY = motionEvent.getRawY();
            dx = view.getX() - downRawX;
            dy = view.getY() - downRawY;
            // handled = isOpened();
            return true; // Consumed
            
        } else if (action == MotionEvent.ACTION_MOVE) {
            Log.d(TAG, "onTouch: action move @@@@@@@@");
            
            int viewWidth;
            viewWidth = view.getWidth();
            int viewHeight;
            viewHeight = view.getHeight();
            
            View viewParent;
            viewParent = (View) view.getParent();
            int parentWidth;
            parentWidth = viewParent.getWidth();
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
            
            // close(misanimated);
            // handled = true;
            if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY) < CLICK_DRAG_TOLERANCE) { // A click
                return performClick();
            } else { // A drag
                return true; // Consumed
            }
            
        } else {
            return super.onTouchEvent(motionEvent);
        }
        
    }
    
    private void init(Context context, AttributeSet attrs) {
        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionMenu, 0, 0);
        mbuttonspacing = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_buttonSpacing, mbuttonspacing);
        mlabelsmargin = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_labels_margin, mlabelsmargin);
        mlabelsposition = attr.getInt(R.styleable.FloatingActionMenu_menu_labels_position, LABELS_POSITION_LEFT);
        mlabelsshowanimation = attr.getResourceId(R.styleable.FloatingActionMenu_menu_labels_showAnimation,
                mlabelsposition == LABELS_POSITION_LEFT ? R.anim.fab_slide_in_from_right
                        : R.anim.fab_slide_in_from_left);
        mlabelshideanimation = attr.getResourceId(R.styleable.FloatingActionMenu_menu_labels_hideAnimation,
                mlabelsposition == LABELS_POSITION_LEFT ? R.anim.fab_slide_out_to_right : R.anim.fab_slide_out_to_left);
        mlabelspaddingtop =
                attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_labels_paddingTop, mlabelspaddingtop);
        mlabelspaddingright = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_labels_paddingRight,
            mlabelspaddingright);
        mlabelspaddingbottom = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_labels_paddingBottom,
            mlabelspaddingbottom);
        mlabelspaddingleft =
                attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_labels_paddingLeft, mlabelspaddingleft);
        mlabelstextcolor = attr.getColorStateList(R.styleable.FloatingActionMenu_menu_labels_textColor);
        // set default value if null same as for textview
        if (mlabelstextcolor == null) {
            mlabelstextcolor = ColorStateList.valueOf(Color.WHITE);
        }
        mlabelstextsize = attr.getDimension(R.styleable.FloatingActionMenu_menu_labels_textSize,
                getResources().getDimension(R.dimen.labels_text_size));
        mlabelscornerradius = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_labels_cornerRadius,
            mlabelscornerradius);
        mlabelsshowshadow = attr.getBoolean(R.styleable.FloatingActionMenu_menu_labels_showShadow, true);
        mlabelscolornormal = attr.getColor(R.styleable.FloatingActionMenu_menu_labels_colorNormal, 0xFF333333);
        mlabelscolorpressed = attr.getColor(R.styleable.FloatingActionMenu_menu_labels_colorPressed, 0xFF444444);
        mlabelscolorripple = attr.getColor(R.styleable.FloatingActionMenu_menu_labels_colorRipple, 0x66FFFFFF);
        mmenushowshadow = attr.getBoolean(R.styleable.FloatingActionMenu_menu_showShadow, true);
        mmenushadowcolor = attr.getColor(R.styleable.FloatingActionMenu_menu_shadowColor, 0x66000000);
        mmenushadowradius = attr.getDimension(R.styleable.FloatingActionMenu_menu_shadowRadius, mmenushadowradius);
        mmenushadowxoffset = attr.getDimension(R.styleable.FloatingActionMenu_menu_shadowXOffset, mmenushadowxoffset);
        mmenushadowyoffset = attr.getDimension(R.styleable.FloatingActionMenu_menu_shadowYOffset, mmenushadowyoffset);
        mmenucolornormal = attr.getColor(R.styleable.FloatingActionMenu_menu_colorNormal, 0xFFDA4336);
        mmenucolorpressed = attr.getColor(R.styleable.FloatingActionMenu_menu_colorPressed, 0xFFE75043);
        mmenucolorripple = attr.getColor(R.styleable.FloatingActionMenu_menu_colorRipple, 0x99FFFFFF);
        manimationdelayperitem = attr.getInt(R.styleable.FloatingActionMenu_menu_animationDelayPerItem, 50);
        // micon = attr.getDrawable(R.styleable.FloatingActionMenu_menu_icon);
        micon = getResources().getDrawable(R.drawable.ic_ice_128);
        if (micon == null) {
            micon = getResources().getDrawable(R.drawable.ic_ice_128);
        }
        mlabelssingleline = attr.getBoolean(R.styleable.FloatingActionMenu_menu_labels_singleLine, false);
        mlabelsellipsize = attr.getInt(R.styleable.FloatingActionMenu_menu_labels_ellipsize, 0);
        mlabelsmaxlines = attr.getInt(R.styleable.FloatingActionMenu_menu_labels_maxLines, -1);
        mmenufabsize = attr.getInt(R.styleable.FloatingActionMenu_menu_fab_size, FloatingActionButton.SIZE_NORMAL);
        mlabelsstyle = attr.getResourceId(R.styleable.FloatingActionMenu_menu_labels_style, 0);
        String customFont = attr.getString(R.styleable.FloatingActionMenu_menu_labels_customFont);
        try {
            if (!TextUtils.isEmpty(customFont)) {
                mcustomtypefacefromfont = Typeface.createFromAsset(getContext().getAssets(), customFont);
            }
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Unable to load specified custom font: " + customFont, ex);
        }
        mopendirection = attr.getInt(R.styleable.FloatingActionMenu_menu_openDirection, OPEN_UP);
        mbackgroundcolor = attr.getColor(R.styleable.FloatingActionMenu_menu_backgroundColor, Color.TRANSPARENT);
        
        if (attr.hasValue(R.styleable.FloatingActionMenu_menu_fab_label)) {
            musingmenulabel = true;
            mmenulabeltext = attr.getString(R.styleable.FloatingActionMenu_menu_fab_label);
        }
        
        if (attr.hasValue(R.styleable.FloatingActionMenu_menu_labels_padding)) {
            int padding = attr.getDimensionPixelSize(R.styleable.FloatingActionMenu_menu_labels_padding, 0);
            initPadding(padding);
        }
        
        mopeninterpolator = new OvershootInterpolator();
        mcloseinterpolator = new AnticipateInterpolator();
        mlabelscontext = new ContextThemeWrapper(getContext(), mlabelsstyle);
        
        initBackgroundDimAnimation();
        createMenuButton();
        initMenuButtonAnimations(attr);
        
        attr.recycle();
        // mmenubutton.setOnTouchListener(this); 还是要小心这个东西
    }
    
    private void initMenuButtonAnimations(TypedArray attr) {
        int showResId = attr.getResourceId(R.styleable.FloatingActionMenu_menu_fab_show_animation, R.anim.fab_scale_up);
        setMenuButtonShowAnimation(AnimationUtils.loadAnimation(getContext(), showResId));
        mimagetoggleshowanimation = AnimationUtils.loadAnimation(getContext(), showResId);
        
        int hideResId =
                attr.getResourceId(R.styleable.FloatingActionMenu_menu_fab_hide_animation, R.anim.fab_scale_down);
        setMenuButtonHideAnimation(AnimationUtils.loadAnimation(getContext(), hideResId));
        mimagetogglehideanimation = AnimationUtils.loadAnimation(getContext(), hideResId);
    }
    
    private void initBackgroundDimAnimation() {
        final int maxAlpha = Color.alpha(mbackgroundcolor);
        final int red = Color.red(mbackgroundcolor);
        final int green = Color.green(mbackgroundcolor);
        final int blue = Color.blue(mbackgroundcolor);
        
        mshowbackgroundanimator = ValueAnimator.ofInt(0, maxAlpha);
        mshowbackgroundanimator.setDuration(ANIMATION_DURATION);
        mshowbackgroundanimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer alpha = (Integer) animation.getAnimatedValue();
                setBackgroundColor(Color.argb(alpha, red, green, blue));
            }
        });
        
        mhidebackgroundanimator = ValueAnimator.ofInt(maxAlpha, 0);
        mhidebackgroundanimator.setDuration(ANIMATION_DURATION);
        mhidebackgroundanimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer alpha = (Integer) animation.getAnimatedValue();
                setBackgroundColor(Color.argb(alpha, red, green, blue));
            }
        });
    }
    
    private boolean isBackgroundEnabled() {
        return mbackgroundcolor != Color.TRANSPARENT;
    }
    
    private void initPadding(int padding) {
        mlabelspaddingtop = padding;
        mlabelspaddingright = padding;
        mlabelspaddingbottom = padding;
        mlabelspaddingleft = padding;
    }
    
    private void createMenuButton() {
        mmenubutton = new FloatingActionButton(getContext());
        
        mmenubutton.mshowshadow = mmenushowshadow;
        if (mmenushowshadow) {
            mmenubutton.mshadowradius = Util.dpToPx(getContext(), mmenushadowradius);
            mmenubutton.mshadowxoffset = Util.dpToPx(getContext(), mmenushadowxoffset);
            mmenubutton.mshadowyoffset = Util.dpToPx(getContext(), mmenushadowyoffset);
        }
        mmenubutton.setColors(mmenucolornormal, mmenucolorpressed, mmenucolorripple);
        mmenubutton.mshadowcolor = mmenushadowcolor;
        mmenubutton.mfabsize = mmenufabsize;
        mmenubutton.updateBackground();
        mmenubutton.setLabelText(mmenulabeltext);
        
        mimagetoggle = new ImageView(getContext());
        mimagetoggle.setImageDrawable(micon);
        
        addView(mmenubutton, super.generateDefaultLayoutParams());
        addView(mimagetoggle);
        
        createDefaultIconAnimation();
    }
    
    private void createDefaultIconAnimation() {
        float collapseAngle;
        float expandAngle;
        if (mopendirection == OPEN_UP) {
            collapseAngle =
                    mlabelsposition == LABELS_POSITION_LEFT ? OPENED_PLUS_ROTATION_LEFT : OPENED_PLUS_ROTATION_RIGHT;
            expandAngle =
                    mlabelsposition == LABELS_POSITION_LEFT ? OPENED_PLUS_ROTATION_LEFT : OPENED_PLUS_ROTATION_RIGHT;
        } else {
            collapseAngle =
                    mlabelsposition == LABELS_POSITION_LEFT ? OPENED_PLUS_ROTATION_RIGHT : OPENED_PLUS_ROTATION_LEFT;
            expandAngle =
                    mlabelsposition == LABELS_POSITION_LEFT ? OPENED_PLUS_ROTATION_RIGHT : OPENED_PLUS_ROTATION_LEFT;
        }
        
        ObjectAnimator collapseAnimator =
                ObjectAnimator.ofFloat(mimagetoggle, "rotation", collapseAngle, CLOSED_PLUS_ROTATION);
        
        ObjectAnimator expandAnimator =
                ObjectAnimator.ofFloat(mimagetoggle, "rotation", CLOSED_PLUS_ROTATION, expandAngle);
        
        mopenanimatorset.play(expandAnimator);
        mcloseanimatorset.play(collapseAnimator);
        
        mopenanimatorset.setInterpolator(mopeninterpolator);
        mcloseanimatorset.setInterpolator(mcloseinterpolator);
        
        mopenanimatorset.setDuration(ANIMATION_DURATION);
        mcloseanimatorset.setDuration(ANIMATION_DURATION);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        width = 0;
        int height = 0;
        mmaxbuttonwidth = 0;
        int maxLabelWidth = 0;
        
        measureChildWithMargins(mimagetoggle, widthMeasureSpec, 0, heightMeasureSpec, 0);
        
        for (int i = 0; i < mbuttonscount; i++) {
            View child = getChildAt(i);
            
            if (child.getVisibility() == GONE || child == mimagetoggle)
            {
                continue;
            }
            
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            mmaxbuttonwidth = Math.max(mmaxbuttonwidth, child.getMeasuredWidth());
        }
        
        for (int i = 0; i < mbuttonscount; i++) {
            int usedWidth = 0;
            View child = getChildAt(i);
            
            if (child.getVisibility() == GONE || child == mimagetoggle)
            {
                continue;
            }
            
            usedWidth += child.getMeasuredWidth();
            height += child.getMeasuredHeight();
            
            Label label = (Label) child.getTag(R.id.fab_label);
            if (label != null) {
                int labelOffset = (mmaxbuttonwidth - child.getMeasuredWidth()) / (musingmenulabel ? 1 : 2);
                int labelUsedWidth =
                        child.getMeasuredWidth() + label.calculateShadowWidth() + mlabelsmargin + labelOffset;
                measureChildWithMargins(label, widthMeasureSpec, labelUsedWidth, heightMeasureSpec, 0);
                usedWidth += label.getMeasuredWidth();
                maxLabelWidth = Math.max(maxLabelWidth, usedWidth + labelOffset);
            }
        }
        
        width = Math.max(mmaxbuttonwidth, maxLabelWidth + mlabelsmargin) + getPaddingLeft() + getPaddingRight();
        
        height += mbuttonspacing * (mbuttonscount - 1) + getPaddingTop() + getPaddingBottom();
        height = adjustForOvershoot(height);
        
        if (getLayoutParams().width == LayoutParams.MATCH_PARENT) {
            width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        }
        
        if (getLayoutParams().height == LayoutParams.MATCH_PARENT) {
            height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        }
        
        setMeasuredDimension(width, height);
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int buttonsHorizontalCenter =
                mlabelsposition == LABELS_POSITION_LEFT ? r - l - mmaxbuttonwidth / 2 - getPaddingRight()
                        : mmaxbuttonwidth / 2 + getPaddingLeft();
        boolean openUp = mopendirection == OPEN_UP;
        
        int menuButtonTop = openUp ? b - t - mmenubutton.getMeasuredHeight() - getPaddingBottom() : getPaddingTop();
        int menuButtonLeft = buttonsHorizontalCenter - mmenubutton.getMeasuredWidth() / 2;
        
        mmenubutton.layout(menuButtonLeft, menuButtonTop, menuButtonLeft + mmenubutton.getMeasuredWidth(),
                menuButtonTop + mmenubutton.getMeasuredHeight());
        
        int imageLeft = buttonsHorizontalCenter - mimagetoggle.getMeasuredWidth() / 2;
        int imageTop = menuButtonTop + mmenubutton.getMeasuredHeight() / 2 - mimagetoggle.getMeasuredHeight() / 2;
        
        mimagetoggle.layout(imageLeft, imageTop, imageLeft + mimagetoggle.getMeasuredWidth(),
                imageTop + mimagetoggle.getMeasuredHeight());
        
        int nextY = openUp ? menuButtonTop + mmenubutton.getMeasuredHeight() + mbuttonspacing : menuButtonTop;
        
        for (int i = mbuttonscount - 1; i >= 0; i--) {
            View child = getChildAt(i);
            
            if (child == mimagetoggle)
            {
                continue;
            }
            
            FloatingActionButton fab = (FloatingActionButton) child;
            
            if (fab.getVisibility() == GONE)
            {
                continue;
            }
            
            int childX = buttonsHorizontalCenter - fab.getMeasuredWidth() / 2;
            int childY = openUp ? nextY - fab.getMeasuredHeight() - mbuttonspacing : nextY;
            
            if (fab != mmenubutton) {
                fab.layout(childX, childY, childX + fab.getMeasuredWidth(), childY + fab.getMeasuredHeight());
                
                if (!mismenuopening) {
                    fab.hide(false);
                }
            }
            
            View label = (View) fab.getTag(R.id.fab_label);
            if (label != null) {
                int labelsOffset = (musingmenulabel ? mmaxbuttonwidth / 2 : fab.getMeasuredWidth() / 2) + mlabelsmargin;
                int labelXNearButton = mlabelsposition == LABELS_POSITION_LEFT ? buttonsHorizontalCenter - labelsOffset
                        : buttonsHorizontalCenter + labelsOffset;
                
                int labelXAwayFromButton =
                        mlabelsposition == LABELS_POSITION_LEFT ? labelXNearButton - label.getMeasuredWidth()
                                : labelXNearButton + label.getMeasuredWidth();
                
                int labelLeft = mlabelsposition == LABELS_POSITION_LEFT ? labelXAwayFromButton : labelXNearButton;
                
                int labelRight = mlabelsposition == LABELS_POSITION_LEFT ? labelXNearButton : labelXAwayFromButton;
                
                int labelTop =
                        childY - mlabelsverticaloffset + (fab.getMeasuredHeight() - label.getMeasuredHeight()) / 2;
                
                label.layout(labelLeft, labelTop, labelRight, labelTop + label.getMeasuredHeight());
                
                if (!mismenuopening) {
                    label.setVisibility(INVISIBLE);
                }
            }
            
            nextY = openUp ? childY - mbuttonspacing : childY + child.getMeasuredHeight() + mbuttonspacing;
        }
    }
    
    private int adjustForOvershoot(int dimension) {
        return (int) (dimension * 0.03 + dimension);
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        bringChildToFront(mmenubutton);
        bringChildToFront(mimagetoggle);
        mbuttonscount = getChildCount();
        createLabels();
    }
    
    private void createLabels() {
        for (int i = 0; i < mbuttonscount; i++) {
            
            if (getChildAt(i) == mimagetoggle)
            {
                continue;
            }
            
            final FloatingActionButton fab = (FloatingActionButton) getChildAt(i);
            
            if (fab.getTag(R.id.fab_label) != null)
            {
                continue;
            }
            
            addLabel(fab);
            
            if (fab == mmenubutton) {
                mmenubutton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggle(misanimated);
                    }
                });
            }
        }
    }
    
    private void addLabel(FloatingActionButton fab) {
        String text = fab.getLabelText();
        
        if (TextUtils.isEmpty(text))
        {
            return;
        }
        
        final Label label = new Label(mlabelscontext);
        label.setClickable(true);
        label.setFab(fab);
        label.setShowAnimation(AnimationUtils.loadAnimation(getContext(), mlabelsshowanimation));
        label.setHideAnimation(AnimationUtils.loadAnimation(getContext(), mlabelshideanimation));
        
        if (mlabelsstyle > 0) {
            label.setTextAppearance(getContext(), mlabelsstyle);
            label.setShowShadow(false);
            label.setUsingStyle(true);
        } else {
            label.setColors(mlabelscolornormal, mlabelscolorpressed, mlabelscolorripple);
            label.setShowShadow(mlabelsshowshadow);
            label.setCornerRadius(mlabelscornerradius);
            if (mlabelsellipsize > 0) {
                setLabelEllipsize(label);
            }
            label.setMaxLines(mlabelsmaxlines);
            label.updateBackground();
            
            label.setTextSize(TypedValue.COMPLEX_UNIT_PX, mlabelstextsize);
            label.setTextColor(mlabelstextcolor);
            
            int left = mlabelspaddingleft;
            int top = mlabelspaddingtop;
            if (mlabelsshowshadow) {
                left += fab.getShadowRadius() + Math.abs(fab.getShadowXOffset());
                top += fab.getShadowRadius() + Math.abs(fab.getShadowYOffset());
            }
            
            label.setPadding(left, top, mlabelspaddingleft, mlabelspaddingtop);
            
            if (mlabelsmaxlines < 0 || mlabelssingleline) {
                label.setSingleLine(mlabelssingleline);
            }
        }
        
        if (mcustomtypefacefromfont != null) {
            label.setTypeface(mcustomtypefacefromfont);
        }
        label.setText(text);
        label.setOnClickListener(fab.getOnClickListener());
        
        addView(label);
        fab.setTag(R.id.fab_label, label);
    }
    
    private void setLabelEllipsize(Label label) {
        switch (mlabelsellipsize) {
            case 1:
                label.setEllipsize(TextUtils.TruncateAt.START);
                break;
            case 2:
                label.setEllipsize(TextUtils.TruncateAt.MIDDLE);
                break;
            case 3:
                label.setEllipsize(TextUtils.TruncateAt.END);
                break;
            case 4:
                label.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                break;
            default:
                break;
        }
    }
    
    @Override
    public MarginLayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }
    
    @Override
    protected MarginLayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }
    
    @Override
    protected MarginLayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(MarginLayoutParams.WRAP_CONTENT, MarginLayoutParams.WRAP_CONTENT);
    }
    
    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }
    
    private void hideMenuButtonWithImage(boolean animate) {
        if (!isMenuButtonHidden()) {
            mmenubutton.hide(animate);
            if (animate) {
                mimagetoggle.startAnimation(mimagetogglehideanimation);
            }
            mimagetoggle.setVisibility(INVISIBLE);
            mismenubuttonanimationrunning = false;
        }
    }
    
    private void showMenuButtonWithImage(boolean animate) {
        if (isMenuButtonHidden()) {
            mmenubutton.show(animate);
            if (animate) {
                mimagetoggle.startAnimation(mimagetoggleshowanimation);
            }
            mimagetoggle.setVisibility(VISIBLE);
        }
    }
    
    /* ===== API methods ===== */
    
    public boolean isOpened() {
        return mmenuopened;
    }
    
    public void toggle(boolean animate) {
        if (isOpened()) {
            close(animate);
        } else {
            open(animate);
        }
    }
    
    public void open(final boolean animate) {
        if (!isOpened()) {
            if (isBackgroundEnabled()) {
                mshowbackgroundanimator.start();
            }
            
            if (miconanimated) {
                if (micontoggleset != null) {
                    micontoggleset.start();
                } else {
                    mcloseanimatorset.cancel();
                    mopenanimatorset.start();
                }
            }
            
            int delay = 0;
            int counter = 0;
            mismenuopening = true;
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View child = getChildAt(i);
                if (child instanceof FloatingActionButton && child.getVisibility() != GONE) {
                    counter++;
                    
                    final FloatingActionButton fab = (FloatingActionButton) child;
                    muihandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isOpened())
                            {
                                return;
                            }
                            
                            if (fab != mmenubutton) {
                                fab.show(animate);
                            }
                            
                            Label label = (Label) fab.getTag(R.id.fab_label);
                            if (label != null && label.isHandleVisibilityChanges()) {
                                label.show(animate);
                            }
                        }
                    }, delay);
                    delay += manimationdelayperitem;
                }
            }
            
            muihandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mmenuopened = true;
                    
                    if (mtogglelistener != null) {
                        mtogglelistener.onMenuToggle(true);
                    }
                }
            }, ++counter * manimationdelayperitem);
        }
    }
    
    public void close(final boolean animate) {
        if (isOpened()) {
            if (isBackgroundEnabled()) {
                mhidebackgroundanimator.start();
            }
            
            if (miconanimated) {
                if (micontoggleset != null) {
                    micontoggleset.start();
                } else {
                    mcloseanimatorset.start();
                    mopenanimatorset.cancel();
                }
            }
            
            int delay = 0;
            int counter = 0;
            mismenuopening = false;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child instanceof FloatingActionButton && child.getVisibility() != GONE) {
                    counter++;
                    
                    final FloatingActionButton fab = (FloatingActionButton) child;
                    muihandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!isOpened())
                            {
                                return;
                            }
                            
                            if (fab != mmenubutton) {
                                fab.hide(animate);
                            }
                            
                            Label label = (Label) fab.getTag(R.id.fab_label);
                            if (label != null && label.isHandleVisibilityChanges()) {
                                label.hide(animate);
                            }
                        }
                    }, delay);
                    delay += manimationdelayperitem;
                }
            }
            
            muihandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mmenuopened = false;
                    
                    if (mtogglelistener != null) {
                        mtogglelistener.onMenuToggle(false);
                    }
                }
            }, ++counter * manimationdelayperitem);
        }
    }
    
    /**
     * Sets the {@link android.view.animation.Interpolator} for <b>FloatingActionButton's</b> icon animation.
     *
     * @param interpolator the Interpolator to be used in animation
     */
    public void setIconAnimationInterpolator(Interpolator interpolator) {
        mopenanimatorset.setInterpolator(interpolator);
        mcloseanimatorset.setInterpolator(interpolator);
    }
    
    public void setIconAnimationOpenInterpolator(Interpolator openInterpolator) {
        mopenanimatorset.setInterpolator(openInterpolator);
    }
    
    public void setIconAnimationCloseInterpolator(Interpolator closeInterpolator) {
        mcloseanimatorset.setInterpolator(closeInterpolator);
    }
    
    /**
     * Sets whether open and close actions should be animated
     *
     * @param animated if <b>false</b> - menu items will appear/disappear instantly without any animation
     */
    public void setAnimated(boolean animated) {
        misanimated = animated;
        mopenanimatorset.setDuration(animated ? ANIMATION_DURATION : 0);
        mcloseanimatorset.setDuration(animated ? ANIMATION_DURATION : 0);
    }
    
    public boolean isAnimated() {
        return misanimated;
    }
    
    public void setAnimationDelayPerItem(int animationDelayPerItem) {
        manimationdelayperitem = animationDelayPerItem;
    }
    
    public int getAnimationDelayPerItem() {
        return manimationdelayperitem;
    }
    
    public void setOnMenuToggleListener(OnMenuToggleListener listener) {
        mtogglelistener = listener;
    }
    
    public void setIconAnimated(boolean animated) {
        miconanimated = animated;
    }
    
    public boolean isIconAnimated() {
        return miconanimated;
    }
    
    public ImageView getMenuIconView() {
        return mimagetoggle;
    }
    
    public void setIconToggleAnimatorSet(AnimatorSet toggleAnimatorSet) {
        micontoggleset = toggleAnimatorSet;
    }
    
    public AnimatorSet getIconToggleAnimatorSet() {
        return micontoggleset;
    }
    
    public void setMenuButtonShowAnimation(Animation showAnimation) {
        mmenubuttonshowanimation = showAnimation;
        mmenubutton.setShowAnimation(showAnimation);
    }
    
    public void setMenuButtonHideAnimation(Animation hideAnimation) {
        mmenubuttonhideanimation = hideAnimation;
        mmenubutton.setHideAnimation(hideAnimation);
    }
    
    public boolean isMenuHidden() {
        return getVisibility() == INVISIBLE;
    }
    
    public boolean isMenuButtonHidden() {
        return mmenubutton.isHidden();
    }
    
    /**
     * Makes the whole {@link #FloatingActionMenu} to appear and sets its visibility to {@link #VISIBLE}
     *
     * @param animate if true - plays "show animation"
     */
    public void showMenu(boolean animate) {
        if (isMenuHidden()) {
            if (animate) {
                startAnimation(mmenubuttonshowanimation);
            }
            setVisibility(VISIBLE);
        }
    }
    
    /**
     * Makes the {@link #FloatingActionMenu} to disappear and sets its visibility to {@link #INVISIBLE}
     *
     * @param animate if true - plays "hide animation"
     */
    public void hideMenu(final boolean animate) {
        if (!isMenuHidden() && !mismenubuttonanimationrunning) {
            mismenubuttonanimationrunning = true;
            if (isOpened()) {
                close(animate);
                muihandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (animate) {
                            startAnimation(mmenubuttonhideanimation);
                        }
                        setVisibility(INVISIBLE);
                        mismenubuttonanimationrunning = false;
                    }
                }, manimationdelayperitem * mbuttonscount);
            } else {
                if (animate) {
                    startAnimation(mmenubuttonhideanimation);
                }
                setVisibility(INVISIBLE);
                mismenubuttonanimationrunning = false;
            }
        }
    }
    
    public void toggleMenu(boolean animate) {
        if (isMenuHidden()) {
            showMenu(animate);
        } else {
            hideMenu(animate);
        }
    }
    
    /**
     * Makes the {@link FloatingActionButton} to appear inside the {@link #FloatingActionMenu} and
     * sets its visibility to {@link #VISIBLE}
     *
     * @param animate if true - plays "show animation"
     */
    public void showMenuButton(boolean animate) {
        if (isMenuButtonHidden()) {
            showMenuButtonWithImage(animate);
        }
    }
    
    /**
     * Makes the {@link FloatingActionButton} to disappear inside the {@link #FloatingActionMenu} and
     * sets its visibility to {@link #INVISIBLE}
     *
     * @param animate if true - plays "hide animation"
     */
    public void hideMenuButton(final boolean animate) {
        if (!isMenuButtonHidden() && !mismenubuttonanimationrunning) {
            mismenubuttonanimationrunning = true;
            if (isOpened()) {
                close(animate);
                muihandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideMenuButtonWithImage(animate);
                    }
                }, manimationdelayperitem * mbuttonscount);
            } else {
                hideMenuButtonWithImage(animate);
            }
        }
    }
    
    public void toggleMenuButton(boolean animate) {
        if (isMenuButtonHidden()) {
            showMenuButton(animate);
        } else {
            hideMenuButton(animate);
        }
    }
    
    public void setClosedOnTouchOutside(boolean close) {
        missetclosedontouchoutside = close;
    }
    
    public void setMenuButtonColorNormal(int color) {
        mmenucolornormal = color;
        mmenubutton.setColorNormal(color);
    }
    
    public void setMenuButtonColorNormalResId(int colorResId) {
        mmenucolornormal = getResources().getColor(colorResId);
        mmenubutton.setColorNormalResId(colorResId);
    }
    
    public int getMenuButtonColorNormal() {
        return mmenucolornormal;
    }
    
    public void setMenuButtonColorPressed(int color) {
        mmenucolorpressed = color;
        mmenubutton.setColorPressed(color);
    }
    
    public void setMenuButtonColorPressedResId(int colorResId) {
        mmenucolorpressed = getResources().getColor(colorResId);
        mmenubutton.setColorPressedResId(colorResId);
    }
    
    public int getMenuButtonColorPressed() {
        return mmenucolorpressed;
    }
    
    public void setMenuButtonColorRipple(int color) {
        mmenucolorripple = color;
        mmenubutton.setColorRipple(color);
    }
    
    public void setMenuButtonColorRippleResId(int colorResId) {
        mmenucolorripple = getResources().getColor(colorResId);
        mmenubutton.setColorRippleResId(colorResId);
    }
    
    public int getMenuButtonColorRipple() {
        return mmenucolorripple;
    }
    
    public void addMenuButton(FloatingActionButton fab) {
        addView(fab, mbuttonscount - 2);
        mbuttonscount++;
        addLabel(fab);
    }
    
    public void addMenuButton(FloatingActionButton fab, int index) {
        int size = mbuttonscount - 2;
        if (index < 0) {
            index = 0;
        } else if (index > size) {
            index = size;
        }
        
        addView(fab, index);
        mbuttonscount++;
        addLabel(fab);
    }
    
    public void removeMenuButton(FloatingActionButton fab) {
        removeView(fab.getLabelView());
        removeView(fab);
        mbuttonscount--;
    }
    
    public void removeAllMenuButtons() {
        close(true);
        
        List<FloatingActionButton> viewsToRemove = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (v != mmenubutton && v != mimagetoggle && v instanceof FloatingActionButton) {
                viewsToRemove.add((FloatingActionButton) v);
            }
        }
        for (FloatingActionButton v : viewsToRemove) {
            removeMenuButton(v);
        }
    }
    
    public void setMenuButtonLabelText(String text) {
        mmenubutton.setLabelText(text);
    }
    
    public String getMenuButtonLabelText() {
        return mmenulabeltext;
    }
    
    public void setOnMenuButtonClickListener(OnClickListener clickListener) {
        mmenubutton.setOnClickListener(clickListener);
    }
    
    public void setOnMenuButtonLongClickListener(OnLongClickListener longClickListener) {
        mmenubutton.setOnLongClickListener(longClickListener);
    }
}
