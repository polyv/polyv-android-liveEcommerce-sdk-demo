package com.easefun.polyv.livecommon.modules.player.live.contract;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.easefun.polyv.businesssdk.api.auxiliary.PolyvAuxiliaryVideoview;
import com.easefun.polyv.businesssdk.api.common.meidaControl.IPolyvMediaController;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.cloudclass.video.PolyvCloudClassVideoView;
import com.easefun.polyv.cloudclass.video.api.IPolyvCloudClassAudioModeView;
import com.easefun.polyv.livecommon.modules.player.live.model.PLVLivePlayerData;

import java.util.List;

/**
 * mvp-直播播放器契约接口
 */
public interface IPLVLivePlayerContract {

    //mvp-直播播放器view层接口
    interface ILivePlayerView {
        //设置presenter
        void setPresenter(@NonNull ILivePlayerPresenter presenter);

        //获取主播放器view
        PolyvCloudClassVideoView getCloudClassVideoView();

        //获取暖场播放器view
        PolyvAuxiliaryVideoview getSubVideoView();

        //获取播放器缓冲视图
        View getBufferingIndicator();

        //获取音频模式显示的视图
        IPolyvCloudClassAudioModeView getAudioModeView();

        //获取暂无直播显示的视图
        View getNoStreamIndicator();

        //获取播放控制器
        IPolyvMediaController getMediaController();

        //子播放器开始播放回调
        void onSubVideoViewPlay(boolean isFirst);

        //主播放器播放失败回调
        void onPlayError(PolyvPlayError error, String tips);

        //暂无直播回调
        void onNoLiveAtPresent();

        //直播结束回调
        void onLiveEnd();

        //准备完成回调
        void onPrepared(@PolyvMediaPlayMode.Mode int mediaPlayMode);

        //线路切换回调
        void onRouteChanged(int routePos);
    }

    //mvp-直播播放器presenter层接口
    interface ILivePlayerPresenter {
        //注册view
        void registerView(@NonNull ILivePlayerView v);

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

        //获取可以切换的线路数
        int getRouteCount();

        //获取当前线路可以切换的码率(清晰度)信息
        @Nullable
        List<PolyvDefinitionVO> getBitrateVO();

        //获取当前线路索引
        int getRoutePos();

        //获取当前码率(清晰度)索引
        int getBitratePos();

        //获取播放模式
        int getMediaPlayMode();

        //改变播放模式
        void changeMediaPlayMode(@PolyvMediaPlayMode.Mode int mediaPlayMode);

        //切换线路
        void changeRoute(int routePos);

        //切换码率
        void changeBitRate(int bitRate);

        //获取直播播放器数据
        @NonNull
        PLVLivePlayerData getData();

        //销毁，包括销毁播放器、解除view
        void destroy();
    }
}
