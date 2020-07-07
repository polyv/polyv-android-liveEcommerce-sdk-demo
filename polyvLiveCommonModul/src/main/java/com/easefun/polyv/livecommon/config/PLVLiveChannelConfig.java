package com.easefun.polyv.livecommon.config;

import android.os.Build;
import android.text.TextUtils;

import com.easefun.polyv.businesssdk.PolyvChatDomainManager;
import com.easefun.polyv.businesssdk.model.chat.PolyvChatDomain;
import com.easefun.polyv.businesssdk.vodplayer.PolyvVodSDKClient;
import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.config.PolyvLiveSDKClient;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.linkmic.PolyvLinkMicClient;

/**
 * 直播频道相关信息配置类
 */
public class PLVLiveChannelConfig implements Cloneable {
    /**
     * 保利威账号信息
     */
    private Account account;
    /**
     * 用户(观众)信息
     */
    private User user;
    //聊天室私有域名
    private PolyvChatDomain chatDomain;
    /**
     * 直播频道号，即推流的频道号
     */
    private String channelId;
    /**
     * 房间id，房间id一般和频道号一致，用于登录socket等场景
     */
    private String roomId;
    /**
     * 回放vid
     */
    private String vid;

    private static volatile PLVLiveChannelConfig singleton = null;

    private PLVLiveChannelConfig() {
        account = new Account();
        user = new User();
    }

    private static PLVLiveChannelConfig getInstance() {
        if (singleton == null) {
            synchronized (PLVLiveChannelConfig.class) {
                if (singleton == null) {
                    singleton = new PLVLiveChannelConfig();
                }
            }
        }
        return singleton;
    }

    /**
     * 配置保利威账号参数
     *
     * @param userId    直播账号userId
     * @param appId     直播账号appId
     * @param appSecret 直播账号appSecret
     */
    public static void setupAccount(String userId, String appId, String appSecret) {
        getInstance().account.userId = userId;
        getInstance().account.appId = appId;
        getInstance().account.appSecret = appSecret;
        //sdk参数配置
        PolyvLinkMicClient.getInstance().setAppIdSecret(appId, appSecret);
        PolyvLiveSDKClient.getInstance().setAppIdSecret(appId, appSecret);
        PolyvVodSDKClient.getInstance().initConfig(appId, appSecret);
    }

    /**
     * 配置用户参数
     *
     * @param viewerId   用户的userId，用于登录socket、发送日志
     * @param viewerName 用户昵称，用于登录socket、发送日志
     */
    public static void setupUser(String viewerId, String viewerName) {
        setupUser(viewerId, viewerName, PolyvChatManager.DEFAULT_AVATARURL);
    }

    /**
     * 配置用户参数
     *
     * @param viewerId     用户的userId，用于登录socket、发送日志
     * @param viewerName   用户昵称，用于登录socket、发送日志
     * @param viewerAvatar 用户的头像url，用于登录socket、发送日志
     */
    public static void setupUser(String viewerId, String viewerName, String viewerAvatar) {
        setupUser(viewerId, viewerName, viewerAvatar, PolyvChatManager.USERTYPE_STUDENT);
    }

    /**
     * 配置用户参数
     *
     * @param viewerId     用户的userId，用于登录socket、发送日志
     * @param viewerName   用户昵称，用于登录socket、发送日志
     * @param viewerAvatar 用户的头像url，用于登录socket、发送日志
     * @param viewerType   用户的类型，用于登录socket，需要为指定的类型，{@link PolyvChatManager#USERTYPE_STUDENT}， {@link PolyvChatManager#USERTYPE_SLICE}
     */
    public static void setupUser(String viewerId, String viewerName, String viewerAvatar, String viewerType) {
        getInstance().user.viewerId = TextUtils.isEmpty(viewerId) ? Build.SERIAL + "" : viewerId;
        getInstance().user.viewerName = TextUtils.isEmpty(viewerName) ? "观众" + Build.SERIAL : viewerName;
        getInstance().user.viewerAvatar = TextUtils.isEmpty(viewerAvatar) ? PolyvChatManager.DEFAULT_AVATARURL : viewerAvatar;
        getInstance().user.viewerType = TextUtils.isEmpty(viewerType) ? PolyvChatManager.USERTYPE_STUDENT : viewerType;
    }

    //设置聊天室私有域名
    public static void setupChatDomain(PolyvChatDomain chatDomain) {
        getInstance().chatDomain = chatDomain;
        PolyvChatDomainManager.getInstance().setChatDomain(chatDomain);
    }

    /**
     * 配置频道号
     */
    public static void setupChannelId(String channelId) {
        getInstance().channelId = channelId;
        getInstance().roomId = TextUtils.isEmpty(getInstance().roomId) ? channelId : getInstance().roomId;
    }

    /**
     * 配置房间号
     */
    public static void setupRoomId(String roomId) {
        getInstance().roomId = roomId;
    }

    /**
     * 配置vid
     */
    public static void setupVid(String vid) {
        getInstance().vid = vid;
    }

    //生成config对象
    public static PLVLiveChannelConfig generateConfig() {
        return (PLVLiveChannelConfig) getInstance().clone();
    }

    public Account getAccount() {
        return account;
    }

    public User getUser() {
        return user;
    }

    public PolyvChatDomain getChatDomain() {
        return chatDomain;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public String getVid() {
        return vid;
    }

    @Override
    protected Object clone() {
        PLVLiveChannelConfig channelConfig = null;
        try {
            channelConfig = (PLVLiveChannelConfig) super.clone();
            channelConfig.account = (Account) account.clone();
            channelConfig.user = (User) user.clone();
        } catch (CloneNotSupportedException e) {
            PolyvCommonLog.exception(e);
        }
        return channelConfig;
    }

    /**
     * 保利威直播账号信息
     */
    public static class Account implements Cloneable {
        /**
         * 直播账号userId
         */
        private String userId;
        /**
         * 直播账号appId
         */
        private String appId;
        /**
         * 直播账号appSecret
         */
        private String appSecret;

        public String getUserId() {
            return userId;
        }

        public String getAppId() {
            return appId;
        }

        public String getAppSecret() {
            return appSecret;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    /**
     * 用户(观众)信息
     */
    public static class User implements Cloneable {
        /**
         * 用户Id，用于登录socket、发送日志<br>
         * 注意{@link #viewerId}不能和{@link Account#userId}一致)
         */
        private String viewerId;
        /**
         * 用户昵称，用于登录socket、发送日志
         */
        private String viewerName;
        /**
         * 用户的头像url，用于登录socket、发送日志
         */
        private String viewerAvatar;
        /**
         * 用户的类型，用于登录socket
         */
        private String viewerType;

        public String getViewerId() {
            return viewerId;
        }

        public String getViewerName() {
            return viewerName;
        }

        public String getViewerAvatar() {
            return viewerAvatar;
        }

        public String getViewerType() {
            return viewerType;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }
}
