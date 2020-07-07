package com.easefun.polyv.liveecommerce.modules.player.widget;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.easefun.polyv.cloudclass.video.api.IPolyvCloudClassAudioModeView;
import com.easefun.polyv.liveecommerce.R;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 音频模式view
 */
public class PLVECLiveAudioModeView extends FrameLayout implements IPolyvCloudClassAudioModeView {
    private AnimationDrawable animationDrawable;
    private Disposable animationDisposable;
    private ImageView audioModeIv;
    private static final int ANIMATION_TOTAL_DURATION = 1000;

    public PLVECLiveAudioModeView(@NonNull Context context) {
        this(context, null);
    }

    public PLVECLiveAudioModeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECLiveAudioModeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_live_player_audio_mode_layout, this);
        setVisibility(View.GONE);
        audioModeIv = findViewById(R.id.audio_mode_iv);
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimation();
        super.onDetachedFromWindow();
    }

    private void startAnimation() {
        if (animationDrawable == null) {
            animationDrawable = new AnimationDrawable();
            animationDrawable.setOneShot(false);
        }
        if (animationDisposable == null) {
            animationDisposable = Observable.just(1).observeOn(Schedulers.io())
                    .doOnNext(new Consumer<Integer>() {
                        @Override
                        public void accept(Integer integer) throws Exception {
                            int drawableCount = 3;
                            for (int i = 1; i <= drawableCount; i++) {
                                String firstDrawableName = getResources().getResourceName(R.drawable.plvec_audio_effect_1);
                                String drawableName = firstDrawableName.substring(0, firstDrawableName.length() - 1) + i;
                                int drawableId = getResources().getIdentifier(drawableName, getResources().getResourceTypeName(R.drawable.plvec_audio_effect_1), getContext().getPackageName());
                                animationDrawable.addFrame(getResources().getDrawable(drawableId), ANIMATION_TOTAL_DURATION / drawableCount);
                            }
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Integer>() {
                        @Override
                        public void accept(Integer integer) throws Exception {
                            audioModeIv.setImageDrawable(animationDrawable);
                            animationDrawable.start();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                        }
                    });
        }
    }

    private void stopAnimation() {
        if (animationDisposable != null) {
            animationDisposable.dispose();
            animationDisposable = null;
        }
        if (animationDrawable != null) {
            animationDrawable.stop();
            audioModeIv.setImageDrawable(null);
            animationDrawable = null;
        }
    }

    @Override
    public void onShow() {
        setVisibility(View.VISIBLE);
        startAnimation();
    }

    @Override
    public void onHide() {
        setVisibility(View.GONE);
        stopAnimation();
    }

    @Override
    public View getRoot() {
        return this;
    }
}
