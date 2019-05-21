package com.bingduoduo.editor.utils;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RxUtils {
    public static Observable.Transformer transformer;


    @SuppressWarnings("unchecked")
    public static <T> Observable.Transformer<T, T> applySchedulersIoAndMainThread() {
        return (Observable.Transformer<T, T>) getScheduler();
    }

    @SuppressWarnings("unchecked")
    private static <T> Observable.Transformer<T, T> getScheduler() {
        if (transformer == null) {
            Observable.Transformer<T, T> ttTransformer = observable -> observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
            transformer = ttTransformer;
            return ttTransformer;
        } else {
            return transformer;
        }
    }
}
