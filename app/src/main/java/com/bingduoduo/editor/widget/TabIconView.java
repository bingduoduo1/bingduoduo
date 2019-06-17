
package com.bingduoduo.editor.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;

import com.termux.R;

/**
 * TAB
 */
public class TabIconView extends HorizontalScrollView {
    
    private LinearLayout mlayout;
    private LayoutInflater minflater;
    
    public TabIconView(Context context) {
        super(context);
        init();
    }
    
    public TabIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public TabIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        this.setOverScrollMode(OVER_SCROLL_NEVER);
        this.setHorizontalScrollBarEnabled(false);
        
        minflater = LayoutInflater.from(getContext());
        
        LinearLayout.LayoutParams params;
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mlayout = new LinearLayout(getContext());
        mlayout.setPadding(1, 0, 1, 0);
        mlayout.setOrientation(LinearLayout.HORIZONTAL);
        addView(mlayout, params);
    }
    
    public void addTab(@DrawableRes int iconId, @IdRes int id, OnClickListener onClickListener) {
        ImageButton view = (ImageButton) minflater.inflate(R.layout.item_tab_icon, mlayout, false);
        view.setImageResource(iconId);
        view.setId(id);
        view.setOnClickListener(onClickListener);
        mlayout.addView(view, mlayout.getChildCount());
        // 滑到最右边
        this.postDelayed(() -> this.smoothScrollBy(1000, 0), 5);
    }
    
    public void removeTab() {
        int count = mlayout.getChildCount();
        // 移除最后一个
        mlayout.removeViewAt(count - 1);
    }
    
}
