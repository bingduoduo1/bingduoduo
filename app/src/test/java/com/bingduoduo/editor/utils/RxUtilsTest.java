package com.bingduoduo.editor.utils;

import org.junit.Test;

import rx.Observable;

import static org.junit.Assert.*;

public class RxUtilsTest {

    @Test
    public void applySchedulersIoAndMainThread() {
        RxUtils rxUtils = new RxUtils();
        rxUtils.applySchedulersIoAndMainThread();
        RxUtils.transformer = new Observable.Transformer() {
            @Override
            public Object call(Object o) {
                return null;
            }
        };
        rxUtils.applySchedulersIoAndMainThread();

    }
}
