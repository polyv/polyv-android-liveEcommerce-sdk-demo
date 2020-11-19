package com.easefun.polyv.livecommon.modules.chatroom.presenter;

import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

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
import com.easefun.polyv.cloudclass.chat.event.PolyvRemoveContentEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvRemoveHistoryEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvSpeakEvent;
import com.easefun.polyv.cloudclass.chat.event.commodity.PolyvProductControlEvent;
import com.easefun.polyv.cloudclass.chat.event.commodity.PolyvProductEvent;
import com.easefun.polyv.cloudclass.chat.event.commodity.PolyvProductMenuSwitchEvent;
import com.easefun.polyv.cloudclass.chat.event.commodity.PolyvProductMoveEvent;
import com.easefun.polyv.cloudclass.chat.event.commodity.PolyvProductRemoveEvent;
import com.easefun.polyv.cloudclass.chat.history.PolyvChatImgHistory;
import com.easefun.polyv.cloudclass.chat.history.PolyvHistoryConstant;
import com.easefun.polyv.cloudclass.chat.history.PolyvSpeakHistory;
import com.easefun.polyv.cloudclass.chat.send.custom.PolyvBaseCustomEvent;
import com.easefun.polyv.cloudclass.chat.send.custom.PolyvCustomEvent;
import com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.cloudclass.model.bulletin.PolyvBulletinVO;
import com.easefun.polyv.cloudclass.net.PolyvApiManager;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBaseTransformer;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;
import com.easefun.polyv.foundationsdk.utils.PolyvGsonUtil;
import com.easefun.polyv.livecommon.config.PLVLiveChannelConfig;
import com.easefun.polyv.livecommon.data.IPLVLiveRoomData;
import com.easefun.polyv.livecommon.modules.chatroom.PLVChatroomMessage;
import com.easefun.polyv.livecommon.modules.chatroom.PLVCustomGiftBean;
import com.easefun.polyv.livecommon.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.modules.chatroom.model.PLVChatroomData;
import com.easefun.polyv.livecommon.modules.liveroom.PLVLiveRoomManager;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.utils.PLVReflectionUtils;
import com.easefun.polyv.livecommon.utils.span.PLVTextFaceLoader;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.Utils;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.socket.client.Ack;
import okhttp3.ResponseBody;

/**
 * mvp-聊天室presenter层实现
 */
public class PLVChatroomPresenter implements IPLVChatroomContract.IChatroomPresenter {
    private static final String TAG = "PLVChatroomPresenter";
    //默认获取的历史记录条数
    public static final int GET_CHAT_HISTORY_COUNT = 20;
    //聊天信息处理间隔
    private static final int CHAT_MESSAGE_TIMESPAN = 500;
    //直播间数据
    private IPLVLiveRoomData liveRoomData;
    //聊天室数据
    private PLVChatroomData chatroomData;
    private WeakReference<IPLVChatroomContract.IChatroomView> vWeakReference;
    private Disposable messageDisposable;

    //是否允许分房间功能
    private boolean allowChildRoom;
    //是否成功拿到分房间号
    private boolean isGetChildRoomId;
    //是否有请求历史记录的事件
    //分房间需要等聊天室登录成功，拿到分房间的频道号后才能去获取历史记录
    private boolean hasRequestHistoryEvent;

    //点赞数
    private long likesCount;
    //观看热度数
    private long viewerCount;
    //在线人数
    private int onlineCount;

    //直播详情数据观察者
    private Observer<PolyvLiveClassDetailVO> classDetailVOObserver;

    //获取的历史记录条数
    private int getChatHistoryCount = GET_CHAT_HISTORY_COUNT;
    //获取历史记录成功的次数
    private int getChatHistoryTime;
    //是否没有更多历史记录
    private boolean isNoMoreChatHistory;
    //获取历史记录的disposable
    private Disposable chatHistoryDisposable;

    public PLVChatroomPresenter(@NonNull IPLVLiveRoomData liveRoomData) {
        this.liveRoomData = liveRoomData;
        chatroomData = new PLVChatroomData();
        subscribeChatroomMessage();
        observeLiveRoomData();
    }

    // <editor-fold defaultstate="collapsed" desc="presenter方法">
    @Override
    public void registerView(@NonNull IPLVChatroomContract.IChatroomView v) {
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
        this.allowChildRoom = allow;
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
    public int sendTextMessage(final PolyvLocalMessage textMessage) {
        int sendValue = PolyvChatManager.getInstance().sendChatMessage(textMessage, true, new Ack() {
            @Override
            public void call(Object... args) {
                PolyvCommonLog.d(TAG, "chatroom sendTextMessage call: " + Arrays.toString(args));
                if (args == null || args.length == 0 || args[0] == null) {
                    return;
                }
                //信息发送成功后，保存信息id
                textMessage.setId(String.valueOf(args[0]));
                chatroomData.postLocalMessage(textMessage);
                if (getView() != null) {
                    getView().onLocalMessage(textMessage);
                }

            }
        });
        if (sendValue == PolyvLocalMessage.SENDVALUE_BANIP) {
            chatroomData.postLocalMessage(textMessage);
            if (getView() != null) {
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
        likesCount++;
        chatroomData.postLikesCountData(likesCount);
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

    @Override
    public void setGetChatHistoryCount(int getChatHistoryCount) {
        this.getChatHistoryCount = getChatHistoryCount;
    }

    @Override
    public void requestChatHistory() {
        if (allowChildRoom) {
            if (!isGetChildRoomId) {
                hasRequestHistoryEvent = true;
                return;
            }
        }
        hasRequestHistoryEvent = false;
        isNoMoreChatHistory = false;
        if (chatHistoryDisposable != null) {
            chatHistoryDisposable.dispose();
        }
        int start = getChatHistoryTime * getChatHistoryCount;
        int end = (getChatHistoryTime + 1) * getChatHistoryCount;
        String loginRoomId = PolyvChatManager.getInstance().getLoginRoomId();//实际登录聊天室的房间id
        chatHistoryDisposable = PolyvApiManager.getPolyvApichatApi().getChatHistory(loginRoomId, start, end, 1)
                .map(new Function<ResponseBody, JSONArray>() {
                    @Override
                    public JSONArray apply(ResponseBody responseBody) throws Exception {
                        return new JSONArray(responseBody.string());
                    }
                })
                .compose(new PolyvRxBaseTransformer<JSONArray, JSONArray>())
                .map(new Function<JSONArray, JSONArray>() {
                    @Override
                    public JSONArray apply(JSONArray jsonArray) throws Exception {
                        if (jsonArray.length() <= getChatHistoryCount) {
                            isNoMoreChatHistory = true;
                        }
                        return jsonArray;
                    }
                })
                .observeOn(Schedulers.io())
                .map(new Function<JSONArray, List<PLVBaseViewData>>() {
                    @Override
                    public List<PLVBaseViewData> apply(JSONArray jsonArray) throws Exception {
                        //把带表情的信息解析保存下来
                        int textSize = ConvertUtils.dp2px(12);
                        if (getView() != null) {
                            textSize = (getView().onSpeakTextSize() <= 0) ? textSize : getView().onSpeakTextSize();
                        }
                        return acceptChatHistory(jsonArray, textSize);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<PLVBaseViewData>>() {
                    @Override
                    public void accept(final List<PLVBaseViewData> dataList) throws Exception {
                        getChatHistoryTime++;
                        if (getView() != null) {
                            getView().onHistoryDataList(dataList, getChatHistoryTime, isNoMoreChatHistory);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(final Throwable throwable) throws Exception {
                        PolyvCommonLog.exception(throwable);
                        if (getView() != null) {
                            getView().onHistoryRequestFailed(PLVLiveRoomManager.getErrorMessage(throwable), throwable);
                        }
                    }
                });
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

    @Override
    public void destroy() {
        getChatHistoryTime = 0;
        isNoMoreChatHistory = false;
        isGetChildRoomId = false;
        hasRequestHistoryEvent = false;
        unregisterView();
        if (messageDisposable != null) {
            messageDisposable.dispose();
        }
        if (chatHistoryDisposable != null) {
            chatHistoryDisposable.dispose();
        }
        liveRoomData.getClassDetailVO().removeObserver(classDetailVOObserver);
        PolyvChatManager.getInstance().destroy();//销毁，会移除实例及所有的监听器
        PLVReflectionUtils.cleanFields(this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="获取view、config">
    private IPLVChatroomContract.IChatroomView getView() {
        return vWeakReference != null ? vWeakReference.get() : null;
    }

    private PLVLiveChannelConfig getConfig() {
        return liveRoomData.getConfig();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="历史信息处理">
    private List<PLVBaseViewData> acceptChatHistory(JSONArray jsonArray, int speakTextSizes) {
        if (speakTextSizes == 0) {
            speakTextSizes = ConvertUtils.dp2px(12);
        }
        List<PLVBaseViewData> tempChatItems = new ArrayList<>();
        for (int i = 0; i < (jsonArray.length() <= getChatHistoryCount ? jsonArray.length() : jsonArray.length() - 1); i++) {
            JSONObject jsonObject = jsonArray.optJSONObject(i);
            if (jsonObject != null) {
                String msgType = jsonObject.optString("msgType");
                if (!TextUtils.isEmpty(msgType)) {
                    if (PolyvHistoryConstant.MSGTYPE_CUSTOMMESSAGE.equals(msgType)) {
                        //custom message
                    }
                    continue;
                }
                String messageSource = jsonObject.optString("msgSource");
                if (!TextUtils.isEmpty(messageSource)) {
                    //收/发红包/图片信息，这里仅取图片信息
                    if (PolyvHistoryConstant.MSGSOURCE_CHATIMG.equals(messageSource)) {
                        int itemType = PLVChatMessageItemType.ITEMTYPE_IMG;
                        PolyvChatImgHistory chatImgHistory = PolyvGsonUtil.fromJson(PolyvChatImgHistory.class, jsonObject.toString());
                        //如果是当前用户，则使用当前用户的昵称
                        if (PolyvChatManager.getInstance().userId.equals(chatImgHistory.getUser().getUserId())) {
                            chatImgHistory.getUser().setNick(PolyvChatManager.getInstance().nickName);
                            itemType = PLVChatMessageItemType.ITEMTYPE_IMG;
                        }
                        PLVBaseViewData itemData = new PLVBaseViewData<>(chatImgHistory, itemType);
                        tempChatItems.add(0, itemData);
                    }
                    continue;
                }
                JSONObject jsonObject_user = jsonObject.optJSONObject("user");
                if (jsonObject_user != null) {
                    String uid = jsonObject_user.optString("uid");
                    if (PolyvHistoryConstant.UID_REWARD.equals(uid) || PolyvHistoryConstant.UID_CUSTOMMSG.equals(uid)) {
                        //打赏/自定义信息，这里过滤掉
                        continue;
                    }
                    JSONObject jsonObject_content = jsonObject.optJSONObject("content");
                    if (jsonObject_content != null) {
                        //content不为字符串的信息，这里过滤掉
                        continue;
                    }
                    int itemType = PLVChatMessageItemType.ITEMTYPE_SPEAK;
                    PolyvSpeakHistory speakHistory = PolyvGsonUtil.fromJson(PolyvSpeakHistory.class, jsonObject.toString());
                    //如果是当前用户，则使用当前用户的昵称
                    if (PolyvChatManager.getInstance().userId.equals(speakHistory.getUser().getUserId())) {
                        speakHistory.getUser().setNick(PolyvChatManager.getInstance().nickName);
                        itemType = PLVChatMessageItemType.ITEMTYPE_SPEAK;
                    }
                    //把带表情的信息解析保存下来
                    speakHistory.setObjects(PLVTextFaceLoader.messageToSpan(speakHistory.getContent(), speakTextSizes, Utils.getApp()));
                    PLVBaseViewData itemData = new PLVBaseViewData<>(speakHistory, itemType);
                    tempChatItems.add(0, itemData);
                }
            }
        }
        return tempChatItems;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 观察直播间的数据">
    private void observeLiveRoomData() {
        //观察直播间的直播详情数据
        liveRoomData.getClassDetailVO().observeForever(classDetailVOObserver = new Observer<PolyvLiveClassDetailVO>() {
            @Override
            public void onChanged(@Nullable PolyvLiveClassDetailVO classDetailVO) {
                liveRoomData.getClassDetailVO().removeObserver(this);
                if (classDetailVO == null || classDetailVO.getData() == null) {
                    return;
                }
                String hasFormatLikes = classDetailVO.getData().getLikes();
                likesCount = (long) (hasFormatLikes.endsWith("w")
                        ? Double.valueOf(hasFormatLikes.substring(0, hasFormatLikes.length() - 1)) * 10000
                        : Double.valueOf(hasFormatLikes));
                viewerCount = classDetailVO.getData().getPageView();
                chatroomData.postLikesCountData(likesCount);
                chatroomData.postViewerCountData(viewerCount);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室事件订阅及处理">
    private void acceptConnectStatusChange(int status, @Nullable Throwable t) {
        switch (status) {
            case PolyvConnectStatusListener.STATUS_DISCONNECT:
                if (t != null) {
                    if (getView() != null) {
                        getView().handleLoginFailed(t);
                    }
                }//t为null时为sdk内部的断开重连或者退出，无需处理
                break;
            case PolyvConnectStatusListener.STATUS_LOGINING:
                if (getView() != null) {
                    getView().handleLoginIng(false);
                }
                break;
            case PolyvConnectStatusListener.STATUS_LOGINSUCCESS:
                isGetChildRoomId = true;
                if (hasRequestHistoryEvent) {
                    requestChatHistory();
                }
                if (getView() != null) {
                    getView().handleLoginSuccess(false);
                }
                break;
            case PolyvConnectStatusListener.STATUS_RECONNECTING:
                if (getView() != null) {
                    getView().handleLoginIng(true);
                }
                break;
            case PolyvConnectStatusListener.STATUS_RECONNECTSUCCESS:
                if (getView() != null) {
                    getView().handleLoginSuccess(true);
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
                                if (getView() != null) {
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
                            if (getView() != null) {
                                textSize = (getView().onSpeakTextSize() <= 0) ? textSize : getView().onSpeakTextSize();
                            }
                            speakEvent.setObjects(PLVTextFaceLoader.messageToSpan(speakEvent.getValues().get(0), textSize, Utils.getApp()));
                            chatMessage = speakEvent;
                            itemType = PLVChatMessageItemType.ITEMTYPE_SPEAK;
                            if (getView() != null) {
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
                                if (getView() != null) {
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
                                if (getView() != null) {
                                    getView().onLikesEvent(likesEvent);
                                }
                                likesCount = likesCount + likesEvent.getCount();
                                chatroomData.postLikesCountData(likesCount);
                            }
                        }
                        break;
                    //用户登录信息
                    case PolyvChatManager.EVENT_LOGIN:
                        PolyvLoginEvent loginEvent = PolyvEventHelper.getEventObject(PolyvLoginEvent.class, message, event);
                        if (loginEvent != null) {
                            if (getView() != null) {
                                getView().onLoginEvent(loginEvent);
                            }
                            //如果不是自己的socket登录事件，则观看热度+1
                            if (!PolyvChatManager.getInstance().userId.equals(loginEvent.getUser().getUserId())) {
                                viewerCount++;
                                chatroomData.postViewerCountData(viewerCount);
                            }
                            onlineCount = loginEvent.getOnlineUserNumber();
                            chatroomData.postOnlineCountData(onlineCount);
                        }
                        break;
                    //用户登出信息
                    case PolyvChatManager.EVENT_LOGOUT:
                        PolyvLogoutEvent logoutEvent = PolyvEventHelper.getEventObject(PolyvLogoutEvent.class, message, event);
                        if (logoutEvent != null) {
                            if (getView() != null) {
                                getView().onLogoutEvent(logoutEvent);
                            }
                            onlineCount = logoutEvent.getOnlineUserNumber();
                            chatroomData.postOnlineCountData(onlineCount);
                        }
                        break;
                    //发布公告事件
                    case PolyvSocketEvent.BULLETIN_SHOW:
                        PolyvBulletinVO bulletinVO = PolyvGsonUtil.fromJson(PolyvBulletinVO.class, message);
                        if (bulletinVO != null) {
                            chatroomData.postBulletinVO(bulletinVO);
                            if (getView() != null) {
                                getView().onBulletinEvent(bulletinVO);
                            }
                        }
                        break;
                    //删除公告事件
                    case PolyvSocketEvent.BULLETIN_REMOVE:
                        chatroomData.postBulletinVO(null);
                        if (getView() != null) {
                            getView().onRemoveBulletinEvent();
                        }
                        break;
                    //商品操作事件
                    case PolyvChatManager.EVENT_PRODUCT_MESSAGE:
                        PolyvProductEvent productEvent = PolyvEventHelper.gson.fromJson(message, PolyvProductEvent.class);
                        if (productEvent != null) {
                            if (productEvent.isProductControlEvent()) {
                                PolyvProductControlEvent productControlEvent = PolyvEventHelper.gson.fromJson(message, PolyvProductControlEvent.class);
                                if (productControlEvent != null) {
                                    if (getView() != null) {
                                        getView().onProductControlEvent(productControlEvent);
                                    }
                                }
                            } else if (productEvent.isProductRemoveEvent()) {
                                PolyvProductRemoveEvent productRemoveEvent = PolyvEventHelper.gson.fromJson(message, PolyvProductRemoveEvent.class);
                                if (productRemoveEvent != null) {
                                    if (getView() != null) {
                                        getView().onProductRemoveEvent(productRemoveEvent);
                                    }
                                }
                            } else if (productEvent.isProductMoveEvent()) {
                                PolyvProductMoveEvent productMoveEvent = PolyvEventHelper.gson.fromJson(message, PolyvProductMoveEvent.class);
                                if (productMoveEvent != null) {
                                    if (getView() != null) {
                                        getView().onProductMoveEvent(productMoveEvent);
                                    }
                                }
                            } else if (productEvent.isProductMenuSwitchEvent()) {
                                PolyvProductMenuSwitchEvent productMenuSwitchEvent = PolyvEventHelper.gson.fromJson(message, PolyvProductMenuSwitchEvent.class);
                                if (productMenuSwitchEvent != null) {
                                    if (getView() != null) {
                                        getView().onProductMenuSwitchEvent(productMenuSwitchEvent);
                                    }
                                }
                            }
                        }
                        break;
                    //聊天室房间开启/关闭事件
                    case PolyvChatManager.EVENT_CLOSEROOM:
                        PolyvCloseRoomEvent closeRoomEvent = PolyvEventHelper.getEventObject(PolyvCloseRoomEvent.class, message, event);
                        if (closeRoomEvent != null) {
                            if (getView() != null) {
                                getView().onCloseRoomEvent(closeRoomEvent);
                            }
                        }
                        break;
                    //用户被踢事件
                    case PolyvChatManager.EVENT_KICK:
                        PolyvKickEvent kickEvent = PolyvEventHelper.getEventObject(PolyvKickEvent.class, message, event);
                        if (kickEvent != null) {
                            boolean isOwn = PolyvChatManager.getInstance().userId.equals(kickEvent.getUser().getUserId());
                            if (getView() != null) {
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
                            if (getView() != null) {
                                getView().onLoginRefuseEvent(loginRefuseEvent);
                            }
                        }
                        break;
                    //用户重新登录事件
                    case PolyvChatManager.EVENT_RELOGIN:
                        PolyvReloginEvent reloginEvent = PolyvEventHelper.getEventObject(PolyvReloginEvent.class, message, event);
                        if (reloginEvent != null) {
                            if (PolyvChatManager.getInstance().userId.equals(reloginEvent.getUser().getUserId())) {
                                if (getView() != null) {
                                    getView().onReloginEvent(reloginEvent);
                                }
                            }
                        }
                        break;
                    //管理员删除某条聊天信息事件
                    case PolyvChatManager.EVENT_REMOVE_CONTENT:
                        final PolyvRemoveContentEvent removeContentEvent = PolyvEventHelper.getEventObject(PolyvRemoveContentEvent.class, message, event);
                        if (removeContentEvent != null) {
                            if (getView() != null) {
                                getView().onRemoveMessageEvent(removeContentEvent.getId(), false);
                            }
                        }
                        break;
                    //管理员清空所有聊天信息事件
                    case PolyvChatManager.EVENT_REMOVE_HISTORY:
                        final PolyvRemoveHistoryEvent removeHistoryEvent = PolyvEventHelper.getEventObject(PolyvRemoveHistoryEvent.class, message, event);
                        if (removeHistoryEvent != null) {
                            if (getView() != null) {
                                getView().onRemoveMessageEvent(null, true);
                            }
                        }
                }
                if (chatMessage != null) {
                    chatMessageDataList.add(new PLVBaseViewData<>(chatMessage, itemType));
                }
            }
        }
        if (chatMessageDataList.size() > 0) {
            if (getView() != null) {
                getView().onChatMessageDataList(chatMessageDataList);
            }
        }
    }
    // </editor-fold>
}
