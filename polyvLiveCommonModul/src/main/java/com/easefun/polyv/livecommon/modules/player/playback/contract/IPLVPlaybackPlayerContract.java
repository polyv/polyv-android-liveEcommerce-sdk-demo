package com.easefun.polyv.livecommon.modules.player.playback.contract;

import android.support.annotation.NonNull;
import android.view.View;

import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.cloudclass.playback.video.PolyvPlaybackVideoView;
import com.easefun.polyv.livecommon.modules.player.playback.model.PLVPlaybackPlayerData;

/**
 * mvp-回放播放器契约接口
 */
public interface IPLVPlaybackPlayerContract {

    //mvp-直播播放器view层接口
    interface IPlaybackPlayerView {
        //设置presenter
        void setPresenter(@NonNull IPlaybackPlayerPresenter presenter);

        //获取主播放器view
        PolyvPlaybackVideoView getPlaybackVideoView();

        //获取播放器缓冲视图
        View getBufferingIndicator();

        //播放器准备完成回调
        void onPrepared();

        //播放失败回调
        void onPlayError(PolyvPlayError error, String tips);

        //缓冲开始
        void onBufferStart();

        //缓冲结束
        void onBufferEnd();
    }

    //mvp-回放播放器presenter层接口
    interface IPlaybackPlayerPresenter {
        //注册view
        void registerView(@NonNull IPlaybackPlayerView v);

        //解除注册的view
        void unregisterView();

        //初始化播放器配置
        void init();

        //开始播放
        void startPlay();

        //暂停播放
        void pause();

        //恢复播放
        void resume();

        //停止播放
        void stop();

        //获取视频总时长
        int getDuration();

        //跳转到指定的视频时间
        void seekTo(int duration);

        //根据progress占max的百分比，跳转到视频总时间的该百分比进度
        void seekTo(int progress, int max);

        //是否在播放中
        boolean isPlaying();

        //设置播放速度
        void setSpeed(float speed);

        //获取播放速度
        float getSpeed();

        //获取回放播放器数据
        @NonNull
        PLVPlaybackPlayerData getData();

        //销毁，包括销毁播放器、解除view
        void destroy();
    }
}
