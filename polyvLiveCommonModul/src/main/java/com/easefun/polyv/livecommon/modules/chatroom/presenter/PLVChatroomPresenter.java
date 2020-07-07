package com.easefun.polyv.livecommon.modules.chatroom.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.easefun.polyv.cloudclass.PolyvSocketEvent;
import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.PolyvConnectStatusListener;
import com.easefun.polyv.cloudclass.chat.PolyvLocalMessage;
import com.easefun.polyv.cloudclass.chat.PolyvNewMessageListener2;
import com.easefun.polyv.cloudclass.chat.event.PolyvChatImgEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvCloseRoomEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvEventHelper;
import com.easefun.polyv.cloudclass.chat.event.PolyvKickEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvLikesEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvLoginEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvLoginRefuseEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvLogoutEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvReloginEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvSpeakEvent;
import com.easefun.polyv.cloudclass.chat.send.custom.PolyvBaseCustomEvent;
import com.easefun.polyv.cloudclass.chat.send.custom.PolyvCustomEvent;
import com.easefun.polyv.cloudclass.model.bulletin.PolyvBulletinVO;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;
import com.easefun.polyv.foundationsdk.utils.PolyvGsonUtil;
import com.easefun.polyv.livecommon.modules.chatroom.PLVChatroomMessage;
import com.easefun.polyv.livecommon.modules.chatroom.PLVCustomGiftBean;
import com.easefun.polyv.livecommon.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.modules.chatroom.model.PLVChatroomData;
import com.easefun.polyv.livecommon.contract.PLVAbsViewPresenter;
import com.easefun.polyv.livecommon.dataservice.IPLVLiveRoomData;
import com.easefun.polyv.livecommon.utils.PLVReflectionUtils;
import com.easefun.polyv.livecommon.utils.span.PLVTextFaceLoader;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.Utils;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * mvp-聊天室presenter层实现
 */
public class PLVChatroomPresenter extends PLVAbsViewPresenter<IPLVChatroomContract.IChatroomView> implements IPLVChatroomContract.IChatroomPresenter {
    private static final String TAG = "PLVChatroomPresenter";
    private static final int CHAT_MESSAGE_TIMESPAN = 500;
    private Disposable messageDisposable;

    private PLVChatroomData chatroomData;

    public PLVChatroomPresenter(@NonNull IPLVLiveRoomData liveRoomData) {
        this.liveRoomData = liveRoomData;
        chatroomData = new PLVChatroomData();
        subscribeChatroomMessage();
    }

    // <editor-fold defaultstate="collapsed" desc="presenter方法">
    @Override
    protected void setPresenterToView() {
        if (isAlive()) {
            getView().setPresenter(this);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (messageDisposable != null) {
            messageDisposable.dispose();
        }
        PolyvChatManager.getInstance().destroy();//销毁，会移除实例及所有的监听器
        PLVReflectionUtils.cleanFields(this);
    }

    @Override
    public void init() {
        //如果调用过destroy方法，那么需要重新初始化监听器
        //设置聊天室(socket)监听器
        PolyvChatManager.getInstance().addConnectStatusListener(new PolyvConnectStatusListener() {
            @Override
            public void onConnectStatusChange(int status, @Nullable Throwable t) {
                if (getConfig().getChannelId().equals(PolyvChatManager.getInstance().roomId)) {//config中的频道id和聊天室单例的roomId一致时才做信息处理
                    PolyvCommonLog.d(TAG, "chatroom connectStatus: " + status + ", throwable: " + t);
                    acceptConnectStatusChange(status, t);
                }
            }
        });
        PolyvChatManager.getInstance().addNewMessageListener(new PolyvNewMessageListener2() {
            @Override
            public void onNewMessage(String message, String event, String socketListen) {
                if (getConfig().getChannelId().equals(PolyvChatManager.getInstance().roomId)) {
                    PolyvCommonLog.d(TAG, "chatroom receiveMessage: " + message + ", event: " + event + ", socketListen: " + socketListen);
                    PolyvRxBus.get().post(new PLVChatroomMessage(message, event, socketListen));
                }
            }

            @Override
            public void onDestroy() {
            }
        });
    }

    @Override
    public void setAllowChildRoom(boolean allow) {
        //如果调用过destroy方法，那么需要重新设置
        PolyvChatManager.getInstance().setAllowChildRoom(allow);
    }

    /**
     * 登录
     */
    @Override
    public void login() {
        disconnect();
        login(getConfig().getAccount().getUserId(), getConfig().getUser().getViewerId(), getConfig().getChannelId(),
                getConfig().getUser().getViewerName(), getConfig().getUser().getViewerAvatar(), getConfig().getUser().getViewerType());
    }

    /**
     * 登录，登录参数均不能为空
     *
     * @param accountId  直播账号id
     * @param viewerId   聊天室用户id
     * @param roomId     房间号
     * @param viewerName 聊天室用户昵称
     * @param avatarUrl  头像地址，需为url格式
     * @param userType   聊天室用户类型
     */
    private void login(String accountId, String viewerId, String roomId, String viewerName, String avatarUrl, String userType) {
        PolyvCommonLog.d(TAG, "chatroom login: " + accountId + ", " + viewerId + ", " + roomId + ", " + viewerName + ", " + avatarUrl);
        PolyvChatManager.getInstance().setAccountId(accountId);
        PolyvChatManager.getInstance().userType = userType;
        PolyvChatManager.getInstance().login(viewerId, roomId, viewerName, avatarUrl);
    }

    /**
     * 发送文本信息
     *
     * @param textMessage 要发送的信息，不能为空
     */
    @Override
    public int sendTextMessage(PolyvLocalMessage textMessage) {
        int sendValue = PolyvChatManager.getInstance().sendChatMessage(textMessage);
        if (sendValue > 0 || sendValue == PolyvLocalMessage.SENDVALUE_BANIP) {
            chatroomData.postLocalMessage(textMessage);
            if (isAlive()) {
                getView().onLocalMessage(textMessage);
            }
        }
        PolyvCommonLog.d(TAG, "chatroom sendTextMessage: " + textMessage.getSpeakMessage() + ", sendValue: " + sendValue);
        return sendValue;
    }

    /**
     * 发送点赞信息
     */
    @Override
    public void sendLikeMessage() {
        PolyvCommonLog.d(TAG, "chatroom sendLikeMessage: " + liveRoomData.getSessionId());
        PolyvChatManager.getInstance().sendLikes(liveRoomData.getSessionId());
    }

    /**
     * 发送自定义信息
     *
     * @param baseCustomEvent 自定义信息事件
     */
    @Override
    public <DataBean> void sendCustomMsg(PolyvBaseCustomEvent<DataBean> baseCustomEvent) {
        PolyvCommonLog.d(TAG, "chatroom sendCustomMsg: " + baseCustomEvent);
        PolyvChatManager.getInstance().sendCustomMsg(baseCustomEvent);
    }

    /**
     * 发送自定义信息，示例为发送自定义送礼信息
     *
     * @param customGiftBean 自定义信息实例
     * @param tip            信息提示文案
     */
    @Override
    public PolyvCustomEvent<PLVCustomGiftBean> sendCustomGiftMessage(PLVCustomGiftBean customGiftBean, String tip) {
        PolyvCustomEvent<PLVCustomGiftBean> customEvent = new PolyvCustomEvent<>(PLVCustomGiftBean.EVENT/*自定义信息事件名*/, customGiftBean);
        customEvent.setTip(tip);
        customEvent.setEmitMode(PolyvBaseCustomEvent.EMITMODE_ALL);//设置广播方式，EMITMODE_ALL为广播给包括自己的所有用户，EMITMODE_OTHERS为广播给不包括自己的所有用户
        customEvent.setVersion(PolyvCustomEvent.VERSION_1);//设置信息的版本号，对该版本号的信息才进行处理
        PolyvCommonLog.d(TAG, "chatroom sendCustomGiftMessage: " + customEvent);
        PolyvChatManager.getInstance().sendCustomMsg(customEvent);
        return customEvent;
    }

    @NonNull
    @Override
    public PLVChatroomData getData() {
        return chatroomData;
    }

    /**
     * 断开连接，断开后可以再次登录
     */
    @Override
    public void disconnect() {
        PolyvCommonLog.d(TAG, "chatroom disconnect");
        PolyvChatManager.getInstance().disconnect();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室事件订阅及处理">
    private void acceptConnectStatusChange(int status, @Nullable Throwable t) {
        switch (status) {
            case PolyvConnectStatusListener.STATUS_DISCONNECT:
                if (t != null) {
                    if (isAlive()) {
                        getView().handleLoginFailed(t);
                    }
                }//t为null时为sdk内部的断开重连或者退出，无需处理
                break;
            case PolyvConnectStatusListener.STATUS_LOGINING:
                if (isAlive()) {
                    getView().handleLoginIng(false);
                }
                break;
            case PolyvConnectStatusListener.STATUS_LOGINSUCCESS:
                if (isAlive()) {
                    getView().handleLoginSuccess(false);
                }
                break;
            case PolyvConnectStatusListener.STATUS_RECONNECTING:
                if (isAlive()) {
                    getView().handleLoginIng(true);
                }
                break;
            case PolyvConnectStatusListener.STATUS_RECONNECTSUCCESS:
                if (isAlive()) {
                    getView().handleLoginIng(true);
                }
                break;
        }
    }

    private void subscribeChatroomMessage() {
        messageDisposable = PolyvRxBus.get().toObservable(PLVChatroomMessage.class)
                .buffer(CHAT_MESSAGE_TIMESPAN, TimeUnit.MILLISECONDS)//500ms更新一次数据，避免聊天信息刷得太频繁
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<List<PLVChatroomMessage>, List<PLVChatroomMessage>>() {
                    @Override
                    public List<PLVChatroomMessage> apply(List<PLVChatroomMessage> chatroomMessages) throws Exception {
                        //需要在主线程调用PolyvChatManager.getInstance()
                        return getConfig().getChannelId().equals(PolyvChatManager.getInstance().roomId) ? chatroomMessages : null;
                    }
                })
                .observeOn(Schedulers.computation())
                .subscribe(new Consumer<List<PLVChatroomMessage>>() {
                    @Override
                    public void accept(List<PLVChatroomMessage> chatroomMessages) throws Exception {
                        acceptNewMessage(chatroomMessages);//在子线程解析数据
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PolyvCommonLog.exception(throwable);
                    }
                });
    }

    private void acceptNewMessage(List<PLVChatroomMessage> chatroomMessages) {
        if (chatroomMessages == null || chatroomMessages.size() == 0) {
            return;
        }
        List<PLVBaseViewData> chatMessageDataList = new ArrayList<>();
        for (PLVChatroomMessage chatroomMessage : chatroomMessages) {
            String message = chatroomMessage.message;
            String event = chatroomMessage.event;
            String socketListen = chatroomMessage.socketListen;

            Object chatMessage = null;
            int itemType = PLVChatMessageItemType.ITEMTYPE_UNDEFINED;
            if (PolyvChatManager.SE_CUSTOMMESSAGE.equals(socketListen)) {
                //自定义信息
                switch (event) {
                    //自定义送礼信息解析示例
                    case PLVCustomGiftBean.EVENT:
                        Type giftType = new TypeToken<PolyvCustomEvent<PLVCustomGiftBean>>() {
                        }.getType();
                        PolyvCustomEvent<PLVCustomGiftBean> customGiftEvent = PolyvEventHelper.gson.fromJson(message, giftType);
                        if (customGiftEvent != null && PolyvCustomEvent.VERSION_1 == customGiftEvent.getVersion()
                                && customGiftEvent.getData() != null && customGiftEvent.getUser() != null) {
                            if (PolyvChatManager.getInstance().userId.equals(customGiftEvent.getUser().getUserId())) {
                                //自己的送礼信息
                            } else {
                                //其他用户的送礼信息
                                if (isAlive()) {
                                    getView().onCustomGiftEvent(customGiftEvent.getUser(), customGiftEvent.getData());
                                }
                            }
                        }
                        break;
                }
            } else if (PolyvChatManager.SE_MESSAGE.equals(socketListen)) {
                //非自定义信息
                switch (event) {
                    //文本类型发言
                    case PolyvChatManager.EVENT_SPEAK:
                        PolyvSpeakEvent speakEvent = PolyvEventHelper.getEventObject(PolyvSpeakEvent.class, message, event);
                        if (speakEvent != null) {
                            //把带表情的信息解析保存下来
                            int textSize = ConvertUtils.dp2px(12);
                            if (isAlive()) {
                                textSize = (getView().onSpeakTextSize() <= 0) ? textSize : getView().onSpeakTextSize();
                            }
                            speakEvent.setObjects(PLVTextFaceLoader.messageToSpan(speakEvent.getValues().get(0), textSize, Utils.getApp()));
                            chatMessage = speakEvent;
                            itemType = PLVChatMessageItemType.ITEMTYPE_SPEAK;
                            if (isAlive()) {
                                getView().onSpeakEvent(speakEvent);
                            }
                        }
                        break;
                    //图片类型发言
                    case PolyvChatManager.EVENT_CHAT_IMG:
                        PolyvChatImgEvent chatImgEvent = PolyvEventHelper.getEventObject(PolyvChatImgEvent.class, message, event);
                        if (chatImgEvent != null) {
                            if (!PolyvChatManager.getInstance().userId.equals(chatImgEvent.getUser().getUserId())) {
                                chatMessage = chatImgEvent;
                                itemType = PLVChatMessageItemType.ITEMTYPE_IMG;
                                if (isAlive()) {
                                    getView().onImgEvent(chatImgEvent);
                                }
                            }
                        }
                        break;
                    //点赞事件
                    case PolyvChatManager.EVENT_LIKES:
                        PolyvLikesEvent likesEvent = PolyvEventHelper.getEventObject(PolyvLikesEvent.class, message, event);
                        if (likesEvent != null) {
                            if (!PolyvChatManager.getInstance().userId.equals(likesEvent.getUserId())) {
                                if (isAlive()) {
                                    getView().onLikesEvent(likesEvent);
                                }
                            }
                        }
                        break;
                    //用户登录信息
                    case PolyvChatManager.EVENT_LOGIN:
                        PolyvLoginEvent loginEvent = PolyvEventHelper.getEventObject(PolyvLoginEvent.class, message, event);
                        if (loginEvent != null) {
                            if (isAlive()) {
                                getView().onLoginEvent(loginEvent);
                            }
                        }
                        break;
                    //用户登出信息
                    case PolyvChatManager.EVENT_LOGOUT:
                        PolyvLogoutEvent logoutEvent = PolyvEventHelper.getEventObject(PolyvLogoutEvent.class, message, event);
                        if (logoutEvent != null) {
                            if (isAlive()) {
                                getView().onLogoutEvent(logoutEvent);
                            }
                        }
                        break;
                    //发布公告事件
                    case PolyvSocketEvent.BULLETIN_SHOW:
                        PolyvBulletinVO bulletinVO = PolyvGsonUtil.fromJson(PolyvBulletinVO.class, message);
                        if (bulletinVO != null) {
                            chatroomData.postBulletinVO(bulletinVO);
                            if (isAlive()) {
                                getView().onBulletinEvent(bulletinVO);
                            }
                        }
                        break;
                    //删除公告事件
                    case PolyvSocketEvent.BULLETIN_REMOVE:
                        chatroomData.postBulletinVO(null);
                        if (isAlive()) {
                            getView().onRemoveBulletinEvent();
                        }
                        break;
                    //聊天室房间开启/关闭事件
                    case PolyvChatManager.EVENT_CLOSEROOM:
                        PolyvCloseRoomEvent closeRoomEvent = PolyvEventHelper.getEventObject(PolyvCloseRoomEvent.class, message, event);
                        if (closeRoomEvent != null) {
                            if (isAlive()) {
                                getView().onCloseRoomEvent(closeRoomEvent);
                            }
                        }
                        break;
                    //用户被踢事件
                    case PolyvChatManager.EVENT_KICK:
                        PolyvKickEvent kickEvent = PolyvEventHelper.getEventObject(PolyvKickEvent.class, message, event);
                        if (kickEvent != null) {
                            boolean isOwn = PolyvChatManager.getInstance().userId.equals(kickEvent.getUser().getUserId());
                            if (isAlive()) {
                                getView().onKickEvent(kickEvent, isOwn);
                            }
                        }
                        break;
                    //用户登录被拒事件，被踢后，再次登录聊天室会回调(用户被踢后不能再正常登录聊天室，可以在管理员后台取消踢出后恢复)
                    case PolyvChatManager.EVENT_LOGIN_REFUSE:
                        PolyvLoginRefuseEvent loginRefuseEvent = PolyvEventHelper.getEventObject(PolyvLoginRefuseEvent.class, message, event);
                        if (loginRefuseEvent != null) {
                            //收到该事件处理后需要退出登录，否则sdk内部每次重连都会触发该回调
                            disconnect();
                            if (isAlive()) {
                                getView().onLoginRefuseEvent(loginRefuseEvent);
                            }
                        }
                        break;
                    //用户重新登录事件
                    case PolyvChatManager.EVENT_RELOGIN:
                        PolyvReloginEvent reloginEvent = PolyvEventHelper.getEventObject(PolyvReloginEvent.class, message, event);
                        if (reloginEvent != null) {
                            if (PolyvChatManager.getInstance().userId.equals(reloginEvent.getUser().getUserId())) {
                                if (isAlive()) {
                                    getView().onReloginEvent(reloginEvent);
                                }
                            }
                        }
                        break;
                }
                if (chatMessage != null) {
                    chatMessageDataList.add(new PLVBaseViewData<>(chatMessage, itemType));
                }
            }
        }
        if (chatMessageDataList.size() > 0) {
            if (isAlive()) {
                getView().onChatMessageDataList(chatMessageDataList);
            }
        }
    }
    // </editor-fold>
}
