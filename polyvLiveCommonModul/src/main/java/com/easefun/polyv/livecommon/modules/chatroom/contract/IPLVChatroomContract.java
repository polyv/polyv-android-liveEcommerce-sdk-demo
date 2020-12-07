package com.easefun.polyv.livecommon.modules.chatroom.contract;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;

import com.easefun.polyv.cloudclass.chat.PolyvLocalMessage;
import com.easefun.polyv.cloudclass.chat.event.PolyvChatImgEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvCloseRoomEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvKickEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvLikesEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvLoginEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvLoginRefuseEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvLogoutEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvReloginEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvSpeakEvent;
import com.easefun.polyv.cloudclass.chat.event.commodity.PolyvProductControlEvent;
import com.easefun.polyv.cloudclass.chat.event.commodity.PolyvProductMenuSwitchEvent;
import com.easefun.polyv.cloudclass.chat.event.commodity.PolyvProductMoveEvent;
import com.easefun.polyv.cloudclass.chat.event.commodity.PolyvProductRemoveEvent;
import com.easefun.polyv.cloudclass.chat.send.custom.PolyvBaseCustomEvent;
import com.easefun.polyv.cloudclass.chat.send.custom.PolyvCustomEvent;
import com.easefun.polyv.cloudclass.model.bulletin.PolyvBulletinVO;
import com.easefun.polyv.livecommon.modules.chatroom.PLVCustomGiftBean;
import com.easefun.polyv.livecommon.modules.chatroom.model.PLVChatroomData;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;

import java.util.List;

/**
 * mvp-聊天室契约接口
 */
public interface IPLVChatroomContract {

    //mvp-聊天室view层接口，登录状态回调信息的线程皆为主线程，event信息回调的线程皆为子线程
    interface IChatroomView {
        //设置presenter
        void setPresenter(@NonNull IChatroomPresenter presenter);

        //登录/重连中
        void handleLoginIng(boolean isReconnect);

        //登录/重连成功
        void handleLoginSuccess(boolean isReconnect);

        //登录失败
        void handleLoginFailed(@NonNull Throwable throwable);

        //文本发言事件
        void onSpeakEvent(@NonNull PolyvSpeakEvent speakEvent);

        //文本发言显示的文字大小，用来计算表情图片的大小(emoSize=SpeakTextSize*1.5)
        int onSpeakTextSize();

        //图片事件
        void onImgEvent(@NonNull PolyvChatImgEvent chatImgEvent);

        //点赞事件
        void onLikesEvent(@NonNull PolyvLikesEvent likesEvent);

        //用户登录事件
        void onLoginEvent(@NonNull PolyvLoginEvent loginEvent);

        //用户退出事件
        void onLogoutEvent(@NonNull PolyvLogoutEvent logoutEvent);

        //发送公告事件
        void onBulletinEvent(@NonNull PolyvBulletinVO bulletinVO);

        //移除公告事件
        void onRemoveBulletinEvent();

        //商品上架/新增/编辑/推送事件
        void onProductControlEvent(@NonNull PolyvProductControlEvent productControlEvent);

        //商品下架/删除事件
        void onProductRemoveEvent(@NonNull PolyvProductRemoveEvent productRemoveEvent);

        //商品上移/下移事件
        void onProductMoveEvent(@NonNull PolyvProductMoveEvent productMoveEvent);

        //商品库开关事件
        void onProductMenuSwitchEvent(@NonNull PolyvProductMenuSwitchEvent productMenuSwitchEvent);

        //房间开启/关闭事件
        void onCloseRoomEvent(@NonNull PolyvCloseRoomEvent closeRoomEvent);

        //移除信息事件，移除单条信息的id，如果是移除所有信息，那么必定为null，true：移除所有信息，false：移除单条信息
        void onRemoveMessageEvent(@Nullable String id, boolean isRemoveAll);

        //用户被踢事件，isOwn：是否是自己
        void onKickEvent(@NonNull PolyvKickEvent kickEvent, boolean isOwn);

        //自己由于被踢后的登录被拒事件
        void onLoginRefuseEvent(@NonNull PolyvLoginRefuseEvent loginRefuseEvent);

        //自己的重新登录事件
        void onReloginEvent(@NonNull PolyvReloginEvent reloginEvent);

        //自定义事件中的送礼事件
        void onCustomGiftEvent(@NonNull PolyvCustomEvent.UserBean userBean, @NonNull PLVCustomGiftBean customGiftBean);

        //自己本地发送的聊天信息
        void onLocalMessage(PolyvLocalMessage localMessage);

        //需要添加到列表的文本发言、图片信息
        void onChatMessageDataList(@Size(min = 1) List<PLVBaseViewData> chatMessageDataList);

        //历史记录数据
        void onHistoryDataList(@Size(min = 0) List<PLVBaseViewData> chatMessageDataList, int requestSuccessTime, boolean isNoMoreHistory);

        //历史记录请求失败回调
        void onHistoryRequestFailed(String errorMsg, Throwable t);
    }

    //mvp-聊天室presenter层接口
    interface IChatroomPresenter {
        /**
         * 注册view
         */
        void registerView(@NonNull IChatroomView v);

        /**
         * 解除注册的view
         */
        void unregisterView();

        /**
         * 初始化聊天室配置，该方法内部会设置聊天室的状态、信息监听器
         */
        void init();

        /**
         * 是否允许使用子房间功能，默认为false。为false时不管后台是否开启子房间功能，都不使用子房间功能。直播带货场景需开启
         */
        void setAllowChildRoom(boolean allow);

        /**
         * 登录
         */
        void login();

        /**
         * 发送文本信息
         *
         * @param textMessage 要发送的信息，不能为空
         */
        int sendTextMessage(PolyvLocalMessage textMessage);

        /**
         * 发送点赞信息
         */
        void sendLikeMessage();

        /**
         * 发送自定义信息
         *
         * @param baseCustomEvent 自定义信息事件
         */
        <DataBean> void sendCustomMsg(PolyvBaseCustomEvent<DataBean> baseCustomEvent);

        /**
         * 发送自定义信息，示例为发送自定义送礼信息
         *
         * @param customGiftBean 自定义信息实例
         * @param tip            信息提示文案
         */
        PolyvCustomEvent<PLVCustomGiftBean> sendCustomGiftMessage(PLVCustomGiftBean customGiftBean, String tip);

        /**
         * 设置每次获取历史记录的条数
         */
        void setGetChatHistoryCount(int count);

        /**
         * 请求聊天历史记录
         */
        void requestChatHistory();

        /**
         * 获取聊天室的数据
         */
        @NonNull
        PLVChatroomData getData();

        /**
         * 断开连接，断开后可以再次登录
         */
        void disconnect();

        /**
         * 销毁，包括销毁聊天室操作、解除view操作
         */
        void destroy();
    }
}
