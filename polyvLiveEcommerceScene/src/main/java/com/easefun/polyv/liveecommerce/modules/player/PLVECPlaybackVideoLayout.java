package com.easefun.polyv.liveecommerce.modules.player;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.cloudclass.playback.video.PolyvPlaybackVideoView;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.livecommon.modules.player.playback.contract.IPLVPlaybackPlayerContract;
import com.easefun.polyv.livecommon.modules.player.playback.view.PLVAbsPlaybackPlayerView;
import com.easefun.polyv.livecommon.utils.PLVVideoSizeUtils;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ToastUtils;

/**
 * date: 2020-04-30
 * author: hwj
 * description:
 */
public class PLVECPlaybackVideoLayout extends FrameLayout {
    // <editor-fold defaultstate="collapsed" desc="成员变量">
    private static final String TAG = "PLVECPlaybackVideoItem";
    //播放器view
    private PolyvPlaybackVideoView videoView;
    //播放器presenter
    private IPLVPlaybackPlayerContract.IPLVPlaybackPlayerPresenter playbackPlayerPresenter;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">
    public PLVECPlaybackVideoLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVECPlaybackVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECPlaybackVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_playback_player_layout, this, true);
        videoView = findViewById(R.id.plvec_playback_video_item);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器view事件处理">
    private PLVAbsPlaybackPlayerView playbackPlayerView = new PLVAbsPlaybackPlayerView() {
        @Override
        public PolyvPlaybackVideoView getPlaybackVideoView() {
            return videoView;
        }

        @Override
        public View getBufferingIndicator() {
            return super.getBufferingIndicator();
        }

        @Override
        public void onPrepared() {
            super.onPrepared();
            PLVVideoSizeUtils.fitVideoRatio(videoView);
        }

        @Override
        public void onPlayError(PolyvPlayError error, String tips) {
            super.onPlayError(error, tips);
            ToastUtils.showLong(tips);
        }

        @Override
        public void onBufferStart() {
            super.onBufferStart();
            PolyvCommonLog.i(TAG, "开始缓冲");
        }

        @Override
        public void onBufferEnd() {
            super.onBufferEnd();
            PolyvCommonLog.i(TAG, "缓冲结束");
        }

        @Override
        public void setPresenter(IPLVPlaybackPlayerContract.IPLVPlaybackPlayerPresenter presenter) {
            super.setPresenter(presenter);
            playbackPlayerPresenter = presenter;
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="获取播放器view层实例">
    public IPLVPlaybackPlayerContract.IPLVPlaybackPlayerView getPlaybackPlayerView() {
        return playbackPlayerView;
    }
    // </editor-fold>
}
