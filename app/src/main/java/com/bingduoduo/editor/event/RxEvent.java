

package com.bingduoduo.editor.event;

public class RxEvent {
    /**
     * Activity关闭事件
     * The constant TYPE_FINISH.
     */
    public static final int TYPE_FINISH = 1;
    //刷新预览数据
    public static final int TYPE_REFRESH_DATA = 2;
    public static final int TYPE_REFRESH_NOTIFY = 3;
    //刷新文件夹
    public static final int TYPE_REFRESH_FOLDER = 4;

    public int type;
    public Object[] o = new Object[0];

    public RxEvent(int type, Object... obj) {
        this.type = type;
        if (obj != null) {
            this.o = obj;
        }
    }

    public boolean isType(int type) {
        return this.type == type;
    }

    public boolean isTypeAndData(int type) {
        return isType(type) && o.length > 0;
    }


}
