package com.easefun.polyv.liveecommerce.modules.player;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.easefun.polyv.businesssdk.api.auxiliary.PolyvAuxiliaryVideoview;
import com.easefun.polyv.businesssdk.api.common.meidaControl.IPolyvMediaController;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.cloudclass.video.PolyvCloudClassVideoView;
import com.easefun.polyv.cloudclass.video.api.IPolyvCloudClassAudioModeView;
import com.easefun.polyv.livecommon.modules.player.PLVEmptyMediaController;
import com.easefun.polyv.livecommon.modules.player.live.contract.IPLVLivePlayerContract;
import com.easefun.polyv.livecommon.modules.player.live.view.PLVAbsLivePlayerView;
import com.easefun.polyv.livecommon.utils.PLVToastUtils;
import com.easefun.polyv.livecommon.utils.PLVVideoSizeUtils;
import com.easefun.polyv.liveecommerce.R;

/**
 * date: 2020-04-29
 * author: hwj
 * description:
 */
public class PLVECLiveVideoLayout extends FrameLayout {
    // <editor-fold defaultstate="collapsed" desc="成员变量">
    //直播播放器横屏视频、音频模式的播放器区域位置
    private Rect videoViewRect;
    //播放器view
    private PolyvCloudClassVideoView videoView;
    //暖场播放器view
    private PolyvAuxiliaryVideoview subVideoView;
    //音频模式view
    private IPolyvCloudClassAudioModeView audioModeView;
    //控制器
    private IPolyvMediaController mediaController;
    //播放器缓冲显示的view
    private View loadingView;
    //播放器没有直播流显示的view
    private View nostreamView;
    //播放器presenter
    private IPLVLivePlayerContract.IPLVLivePlayerPresenter livePlayerPresenter;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">
    public PLVECLiveVideoLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVECLiveVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECLiveVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_live_player_layout, this, true);
        videoView = findViewById(R.id.plvec_live_video_view);
        subVideoView = findViewById(R.id.sub_video_view);
        audioModeView = findViewById(R.id.audio_mode_ly);
        loadingView = findViewById(R.id.loading_pb);
        nostreamView = findViewById(R.id.nostream_ly);
        mediaController = new PLVEmptyMediaController();

        videoView.setSubVideoView(subVideoView);
        videoView.setAudioModeView(audioModeView);
        videoView.setPlayerBufferingIndicator(loadingView);
        videoView.setNoStreamIndicator(nostreamView);
        videoView.setMediaController(mediaController);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器view层事件处理">
    private PLVAbsLivePlayerView livePlayerView = new PLVAbsLivePlayerView() {
        @Override
        public PolyvCloudClassVideoView getCloudClassVideoView() {
            return videoView;
        }

        @Override
        public PolyvAuxiliaryVideoview getSubVideoView() {
            return subVideoView;
        }

        @Override
        public View getBufferingIndicator() {
            return loadingView;
        }

        @Override
        public IPolyvCloudClassAudioModeView getAudioModeView() {
            return audioModeView;
        }

        @Override
        public View getNoStreamIndicator() {
            return nostreamView;
        }

        @Override
        public IPolyvMediaController getMediaController() {
            return mediaController;
        }

        @Override
        public void onSubVideoViewPlay(boolean isFirst) {
            super.onSubVideoViewPlay(isFirst);
            PLVVideoSizeUtils.fitVideoRatioAndRect(subVideoView, videoView.getParent(), videoViewRect);//传主播放器viewParent
        }

        @Override
        public void onPlayError(PolyvPlayError error, String tips) {
            super.onPlayError(error, tips);
            PLVToastUtils.showLong(tips);
            PLVVideoSizeUtils.fitVideoRect(false, videoView.getParent(), videoViewRect);
        }

        @Override
        public void onNoLiveAtPresent() {
            super.onNoLiveAtPresent();
            PLVVideoSizeUtils.fitVideoRect(false, videoView.getParent(), videoViewRect);
            PLVToastUtils.showShort("暂无直播");
        }

        @Override
        public void onLiveEnd() {
            super.onLiveEnd();
            PLVToastUtils.showShort("直播结束");
        }

        @Override
        public void onPrepared(int mediaPlayMode) {
            super.onPrepared(mediaPlayMode);
            if (mediaPlayMode == PolyvMediaPlayMode.MODE_VIDEO) {
                PLVVideoSizeUtils.fitVideoRatioAndRect(videoView, videoView.getParent(), videoViewRect);
            } else if (mediaPlayMode == PolyvMediaPlayMode.MODE_AUDIO) {
                PLVVideoSizeUtils.fitVideoRect(false, videoView.getParent(), videoViewRect);
            }
        }

        @Override
        public void onRouteChanged(int routePos) {
            super.onRouteChanged(routePos);
        }

        @Override
        public void setPresenter(IPLVLivePlayerContract.IPLVLivePlayerPresenter presenter) {
            super.setPresenter(presenter);
            livePlayerPresenter = presenter;
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="获取播放器view层实例">
    public IPLVLivePlayerContract.IPLVLivePlayerView getLivePlayerView() {
        return livePlayerView;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置播放器区域位置">
    public void setVideoViewRect(Rect videoViewRect) {
        this.videoViewRect = videoViewRect;
    }
    // </editor-fold>
}
