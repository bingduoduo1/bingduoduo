
package com.bingduoduo.editor.base;

import android.os.Bundle;
import androidx.annotation.LayoutRes;

public interface BaseViewInterface {
    
    /**
     * Activitiy的布局,必须重写
     *
     * @return 布局资源
     */
    @LayoutRes
    int getLayoutId();
    
    /**
     * onCreate之后调用,可以用来初始化view
     */
    void onCreateAfter(Bundle savedInstanceState);
    
    /**
     * 界面渲染完毕，可在这里进行初始化工作，建议在这里启动线程进行初始化工作
     * 数据获取等操作
     */
    void initData();
    
}
