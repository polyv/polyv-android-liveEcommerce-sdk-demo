package com.easefun.polyv.livecommon.utils.imageloader;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.widget.ImageView;


import com.easefun.polyv.livecommon.utils.imageloader.glide.PLVGlideImageLoadEngine;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * date: 2019/9/4 0004
 *
 * @author hwj
 * <p>
 * 图片加载器。默认使用Glide作为图片加载引擎{@link PLVGlideImageLoadEngine}，
 * 开发者如果用了其他的图片加载框架，
 * 可以通过实现{@link IPLVImageLoadEngine}接口，
 * 并在下方代码块中实例化，来快速替换图片加载库。
 */
public class PLVImageLoader {
    // <editor-fold defaultstate="collapsed" desc="单例">
    private static PLVImageLoader INSTANCE;

    private PLVImageLoader() {/**/}

    public static PLVImageLoader getInstance() {
        if (INSTANCE == null) {
            synchronized (PLVImageLoader.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PLVImageLoader();
                }
            }
        }
        return INSTANCE;
    }
    // </editor-fold>


    private IPLVImageLoadEngine loadEngine;

    {
        loadEngine = new PLVGlideImageLoadEngine();
    }

    /**
     * 加载图片
     */
    public void loadImage(String url, ImageView imageView) {
        loadEngine.loadImage(imageView.getContext(), url, imageView);
    }
    /**
     * 加载图片
     */
    public void loadImage(Context context, String url, ImageView imageView) {
        loadEngine.loadImage(context, url, imageView);
    }

    /**
     * 加载图片：带有进度监听。
     */
    public void loadImage(Context context, final String moduleTag, final Object urlTag, @DrawableRes int errorRes, @NonNull final PLVAbsProgressListener listener, final ImageView view) {
        loadEngine.loadImage(context, moduleTag, urlTag, errorRes, listener, view);
    }

    /**
     * 加载图片，不进行本地磁盘缓存
     */
    public void loadImageNoDiskCache(Context context, String url, @DrawableRes int placeHolder, @DrawableRes int error, ImageView imageView) {
        loadEngine.loadImageNoDiskCache(context, url, placeHolder, error, imageView);
    }

    /**
     * 加载图片，进行本地磁盘缓存
     */
    public void loadImage(Context context, String url, @DrawableRes int placeHolder, @DrawableRes int error, ImageView imageView) {
        loadEngine.loadImage(context, url, placeHolder, error, imageView);
    }

    public void loadImage(Context context, String url, @DrawableRes int placeHolder, @DrawableRes int error, ImageView imageView, int radius) {
        loadEngine.loadImage(context, url, placeHolder, error, imageView, radius);
    }

    /**
     * 将图片保存成文件
     */
    @WorkerThread
    public File saveImageAsFile(Context context, String url) throws ExecutionException, InterruptedException {
        return loadEngine.saveImageAsFile(context, url);
    }
}
