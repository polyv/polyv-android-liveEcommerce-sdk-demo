package com.easefun.polyv.livecommon.modules.chatroom.view;

import android.support.annotation.NonNull;

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
import com.easefun.polyv.cloudclass.chat.event.commodity.PolyvProductMoveEvent;
import com.easefun.polyv.cloudclass.chat.event.commodity.PolyvProductRemoveEvent;
import com.easefun.polyv.cloudclass.chat.send.custom.PolyvCustomEvent;
import com.easefun.polyv.cloudclass.model.bulletin.PolyvBulletinVO;
import com.easefun.polyv.livecommon.modules.chatroom.PLVCustomGiftBean;
import com.easefun.polyv.livecommon.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;

import java.util.List;

/**
 * mvp-聊天室view层抽象类
 */
public abstract class PLVAbsChatroomView implements IPLVChatroomContract.IChatroomView {
    @Override
    public void setPresenter(@NonNull IPLVChatroomContract.IChatroomPresenter presenter) {

    }

    @Override
    public void handleLoginIng(boolean isReconnect) {

    }

    @Override
    public void handleLoginSuccess(boolean isReconnect) {

    }

    @Override
    public void handleLoginFailed(@NonNull Throwable throwable) {

    }

    @Override
    public void onSpeakEvent(@NonNull PolyvSpeakEvent speakEvent) {

    }

    @Override
    public int onSpeakTextSize() {
        return 0;
    }

    @Override
    public void onImgEvent(@NonNull PolyvChatImgEvent chatImgEvent) {

    }

    @Override
    public void onLikesEvent(@NonNull PolyvLikesEvent likesEvent) {

    }

    @Override
    public void onLoginEvent(@NonNull PolyvLoginEvent loginEvent) {

    }

    @Override
    public void onLogoutEvent(@NonNull PolyvLogoutEvent logoutEvent) {

    }

    @Override
    public void onBulletinEvent(@NonNull PolyvBulletinVO bulletinVO) {

    }

    @Override
    public void onRemoveBulletinEvent() {

    }

    @Override
    public void onProductControlEvent(@NonNull PolyvProductControlEvent productControlEvent) {

    }

    @Override
    public void onProductRemoveEvent(@NonNull PolyvProductRemoveEvent productRemoveEvent) {

    }

    @Override
    public void onProductMoveEvent(@NonNull PolyvProductMoveEvent productMoveEvent) {

    }

    @Override
    public void onCloseRoomEvent(@NonNull PolyvCloseRoomEvent closeRoomEvent) {

    }

    @Override
    public void onKickEvent(@NonNull PolyvKickEvent kickEvent, boolean isOwn) {

    }

    @Override
    public void onLoginRefuseEvent(@NonNull PolyvLoginRefuseEvent loginRefuseEvent) {

    }

    @Override
    public void onReloginEvent(@NonNull PolyvReloginEvent reloginEvent) {

    }

    @Override
    public void onLocalMessage(PolyvLocalMessage localMessage) {

    }

    @Override
    public void onCustomGiftEvent(@NonNull PolyvCustomEvent.UserBean userBean, @NonNull PLVCustomGiftBean customGiftBean) {

    }

    @Override
    public void onChatMessageDataList(List<PLVBaseViewData> chatMessageDataList) {

    }
}
