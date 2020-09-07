package com.easefun.polyv.livecommon.modules.player.playback.prsenter;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.businesssdk.api.common.player.listener.IPolyvVideoViewListenerEvent;
import com.easefun.polyv.businesssdk.model.video.PolyvBaseVideoParams;
import com.easefun.polyv.businesssdk.model.video.PolyvPlaybackVideoParams;
import com.easefun.polyv.cloudclass.playback.video.PolyvPlaybackListType;
import com.easefun.polyv.cloudclass.playback.video.PolyvPlaybackVideoView;
import com.easefun.polyv.foundationsdk.config.PolyvPlayOption;
import com.easefun.polyv.livecommon.config.PLVLiveChannelConfig;
import com.easefun.polyv.livecommon.data.IPLVLiveRoomData;
import com.easefun.polyv.livecommon.modules.player.playback.contract.IPLVPlaybackPlayerContract;
import com.easefun.polyv.livecommon.modules.player.playback.model.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.modules.player.playback.model.PLVPlaybackPlayerData;
import com.easefun.polyv.livecommon.utils.PLVReflectionUtils;

import java.lang.ref.WeakReference;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * mvp-回放播放器presenter层实现
 */
public class PLVPlaybackPlayerPresenter implements IPLVPlaybackPlayerContract.IPlaybackPlayerPresenter {
    private static final int WHAT_PLAY_PROGRESS = 1;
    private IPLVLiveRoomData liveRoomData;
    private PLVPlaybackPlayerData playbackPlayerData;
    private WeakReference<IPLVPlaybackPlayerContract.IPlaybackPlayerView> vWeakReference;

    private PolyvPlaybackVideoView videoView;

    public PLVPlaybackPlayerPresenter(@NonNull IPLVLiveRoomData liveRoomData) {
        this.liveRoomData = liveRoomData;
        playbackPlayerData = new PLVPlaybackPlayerData();
    }

    // <editor-fold defaultstate="collapsed" desc="presenter方法">
    @Override
    public void registerView(@NonNull IPLVPlaybackPlayerContract.IPlaybackPlayerView v) {
        this.vWeakReference = new WeakReference<>(v);
        v.setPresenter(this);
    }

    @Override
    public void unregisterView() {
        if (vWeakReference != null) {
            vWeakReference.clear();
            vWeakReference = null;
        }
    }

    @Override
    public void init() {
        if (getView() == null) {
            return;
        }
        //init data
        videoView = getView().getPlaybackVideoView();
        initVideoViewListener();
    }

    @Override
    public void destroy() {
        unregisterView();
        if (videoView != null) {
            videoView.destroy();
        }
        stopPlayProgressTimer();
        PLVReflectionUtils.cleanFields(this);
    }

    @Override
    public void startPlay() {
        PolyvPlaybackVideoParams playbackVideoParams = new PolyvPlaybackVideoParams(
                getConfig().getVid(),
                getConfig().getChannelId(),
                getConfig().getAccount().getUserId(),
                getConfig().getUser().getViewerId()
        );
        playbackVideoParams.buildOptions(PolyvBaseVideoParams.MARQUEE, true)
                .buildOptions(PolyvBaseVideoParams.PARAMS2, getConfig().getUser().getViewerName())
                .buildOptions(PolyvPlaybackVideoParams.ENABLE_ACCURATE_SEEK, true)
                .buildOptions(PolyvPlaybackVideoParams.VIDEO_LISTTYPE, PolyvPlaybackListType.PLAYBACK);
        if (videoView != null) {
            videoView.playByMode(playbackVideoParams, PolyvPlayOption.PLAYMODE_VOD);
        }
        startPlayProgressTimer();
    }

    @Override
    public void pause() {
        if (videoView != null) {
            videoView.pause();
        }
    }

    @Override
    public void resume() {
        if (videoView != null) {
            videoView.start();
        }
    }

    @Override
    public void stop() {
        if (videoView != null) {
            videoView.stopPlay();
        }
    }

    @Override
    public int getDuration() {
        if (videoView != null) {
            return videoView.getDuration();
        }
        return 0;
    }

    @Override
    public void seekTo(int duration) {
        if (videoView != null) {
            videoView.seekTo(duration);
        }
    }

    @Override
    public void seekTo(int progress, int max) {
        if (videoView != null && videoView.isInPlaybackStateEx()) {
            int seekPosition = (int) ((long) videoView.getDuration() * progress / max);
            if (!videoView.isCompletedState()) {
                videoView.seekTo(seekPosition);
            } else if (seekPosition < videoView.getDuration()) {
                videoView.seekTo(seekPosition);
                videoView.start();
            }
        }
    }

    @Override
    public boolean isPlaying() {
        if (videoView != null) {
            return videoView.isPlaying();
        }
        return false;
    }

    @Override
    public void setSpeed(float speed) {
        if (videoView != null) {
            videoView.setSpeed(speed);
        }
    }

    @Override
    public float getSpeed() {
        if (videoView != null) {
            return videoView.getSpeed();
        }
        return 0;
    }

    @NonNull
    @Override
    public PLVPlaybackPlayerData getData() {
        return playbackPlayerData;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="获取view、config">
    private IPLVPlaybackPlayerContract.IPlaybackPlayerView getView() {
        return vWeakReference != null ? vWeakReference.get() : null;
    }

    private PLVLiveChannelConfig getConfig() {
        return liveRoomData.getConfig();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化videoView的监听器配置">
    private void initVideoViewListener() {
        if (videoView != null) {
            videoView.setOnPreparedListener(new IPolyvVideoViewListenerEvent.OnPreparedListener() {
                @Override
                public void onPrepared() {
                    playbackPlayerData.postPrepared();
                    if (getView() != null) {
                        getView().onPrepared();
                    }
                }

                @Override
                public void onPreparing() {
                }
            });
            videoView.setOnErrorListener(new IPolyvVideoViewListenerEvent.OnErrorListener() {
                @Override
                public void onError(int what, int extra) {
                }

                @Override
                public void onError(PolyvPlayError error) {
                    String tips = error.playStage == PolyvPlayError.PLAY_STAGE_HEADAD ? "片头广告"
                            : error.playStage == PolyvPlayError.PLAY_STAGE_TAILAD ? "片尾广告"
                            : error.playStage == PolyvPlayError.PLAY_STAGE_TEASER ? "暖场视频"
                            : error.isMainStage() ? "主视频" : "";
                    tips += "播放异常\n" + error.errorDescribe + "(" + error.errorCode + "-" + error.playStage + ")\n" + error.playPath;
                    if (getView() != null) {
                        getView().onPlayError(error, tips);
                    }
                }
            });
            videoView.setOnInfoListener(new IPolyvVideoViewListenerEvent.OnInfoListener() {
                @Override
                public void onInfo(int what, int extra) {
                    if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
                        if (getView() != null) {
                            getView().onBufferStart();
                        }
                    } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
                        if (getView() != null) {
                            getView().onBufferEnd();
                        }
                    }
                }
            });
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="定时获取播放信息任务">
    private Handler selfHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_PLAY_PROGRESS) {
                startPlayProgressTimer();
            }
        }
    };

    private void startPlayProgressTimer() {
        stopPlayProgressTimer();
        if (videoView != null) {
            // 单位：毫秒
            int position = videoView.getCurrentPosition();
            int totalTime = videoView.getDuration() / 1000 * 1000;
            if (videoView.isCompletedState() || position > totalTime) {
                position = totalTime;
            }
            int bufPercent = videoView.getBufferPercentage();
            playbackPlayerData.postPlayInfoVO(
                    new PLVPlayInfoVO.Builder()
                            .position(position).totalTime(totalTime)
                            .bufPercent(bufPercent).isPlaying(videoView.isPlaying())
                            .build()
            );
            selfHandler.sendEmptyMessageDelayed(WHAT_PLAY_PROGRESS, 1000 - (position % 1000));
        } else {
            selfHandler.sendEmptyMessageDelayed(WHAT_PLAY_PROGRESS, 1000);
        }
    }

    private void stopPlayProgressTimer() {
        selfHandler.removeMessages(WHAT_PLAY_PROGRESS);
    }
    // </editor-fold>
}
