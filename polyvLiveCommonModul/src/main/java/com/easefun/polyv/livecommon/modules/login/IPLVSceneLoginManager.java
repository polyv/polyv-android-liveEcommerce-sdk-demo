package com.easefun.polyv.livecommon.modules.login;

import com.easefun.polyv.livecommon.net.IPLVNetRequestListener;

/**
 * date: 2020-04-17
 * author: hwj
 * description: 场景登陆管理器
 */
public interface IPLVSceneLoginManager {

    /**
     * 登陆直播
     *
     * @param userId        直播账号userId
     * @param appSecret     直播账号appSecret
     * @param channelId     直播频道号
     * @param appId         直播账号appId
     * @param loginListener 监听器
     */
    void loginLive(String userId,
                   String appSecret,
                   String channelId,
                   String appId,
                   IPLVNetRequestListener<PLVLiveLoginResult> loginListener);

    /**
     * 登陆回放
     *
     * @param userId        直播账号userId
     * @param appSecret     直播账号appSecret
     * @param channelId     直播频道号
     * @param vid           回放视频vid
     * @param appId         直播账号appId
     * @param loginListener 监听器
     */
    void loginPlayback(String userId,
                       String appSecret,
                       String channelId,
                       String vid,
                       String appId,
                       IPLVNetRequestListener<PLVPlaybackLoginResult> loginListener);

    /**
     * 销毁
     */
    void destroy();
}
