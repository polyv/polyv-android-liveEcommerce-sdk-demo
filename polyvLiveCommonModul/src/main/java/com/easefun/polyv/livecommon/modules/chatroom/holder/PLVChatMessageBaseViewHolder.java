package com.easefun.polyv.livecommon.modules.chatroom.holder;

import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.PolyvLocalMessage;
import com.easefun.polyv.cloudclass.chat.event.PolyvChatImgEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvSpeakEvent;
import com.easefun.polyv.cloudclass.chat.history.PolyvChatImgHistory;
import com.easefun.polyv.cloudclass.chat.history.PolyvSpeakHistory;
import com.easefun.polyv.livecommon.modules.chatroom.PLVCustomGiftEvent;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.adapter.PLVBaseAdapter;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 聊天信息基础viewHolder
 */
public class PLVChatMessageBaseViewHolder<Data extends PLVBaseViewData, Adapter extends PLVBaseAdapter> extends PLVBaseViewHolder<Data, Adapter> {
    protected Object chatMessage;
    protected String userType;
    protected String nickName;
    protected String userId;
    protected String actor;
    protected CharSequence speakMsg;
    protected String chatImgUrl;//vh self cache
    protected int chatImgWidth, chatImgHeight;

    public PLVChatMessageBaseViewHolder(View itemView, Adapter adapter) {
        super(itemView, adapter);
    }

    private void resetParams() {
        userType = null;
        nickName = null;
        speakMsg = null;
        userId = null;
        actor = null;
        chatImgUrl = null;
        chatImgWidth = 0;
        chatImgHeight = 0;
    }

    @Override
    public void processData(Data data, int position) {
        super.processData(data, position);
        chatMessage = data.getData();
        resetParams();
        if (chatMessage instanceof PolyvSpeakEvent) {//接收的发言事件信息
            PolyvSpeakEvent.UserBean userBean = ((PolyvSpeakEvent) chatMessage).getUser();
            if (userBean != null) {
                userType = userBean.getUserType();
                nickName = userBean.getNick();
                userId = userBean.getUserId();
                actor = userBean.getActor();
            }
            speakMsg = (CharSequence) ((PolyvSpeakEvent) chatMessage).getObjects()[0];
        } else if (chatMessage instanceof PolyvLocalMessage) {//本地的发言事件信息
            userType = PolyvChatManager.getInstance().userType;
            nickName = PolyvChatManager.getInstance().nickName;
            userId = PolyvChatManager.getInstance().userId;
            speakMsg = (CharSequence) ((PolyvLocalMessage) chatMessage).getObjects()[0];
        } else if (chatMessage instanceof PolyvChatImgEvent) {//接收的图片事件信息
            PolyvChatImgEvent chatImgEvent = (PolyvChatImgEvent) chatMessage;
            PolyvChatImgEvent.UserBean userBean = chatImgEvent.getUser();
            if (userBean != null) {
                userType = userBean.getUserType();
                nickName = userBean.getNick();
                userId = userBean.getUserId();
                actor = userBean.getActor();
            }
            if (chatImgEvent.getValues() != null && chatImgEvent.getValues().size() > 0) {
                PolyvChatImgEvent.ValuesBean valuesBean = chatImgEvent.getValues().get(0);
                if (valuesBean != null) {
                    chatImgUrl = valuesBean.getUploadImgUrl();
                    if (valuesBean.getSize() != null) {
                        chatImgWidth = (int) valuesBean.getSize().getWidth();
                        chatImgHeight = (int) valuesBean.getSize().getHeight();
                    }
                }
            }
        } else if (chatMessage instanceof PolyvSpeakHistory) {//历史发言事件
            PolyvSpeakHistory speakHistory = (PolyvSpeakHistory) chatMessage;
            PolyvSpeakHistory.UserBean userBean = speakHistory.getUser();
            if (userBean != null) {
                userType = userBean.getUserType();
                nickName = userBean.getNick();
                userId = userBean.getUserId();
                actor = userBean.getActor();
            }
            speakMsg = (CharSequence) speakHistory.getObjects()[0];
        } else if (chatMessage instanceof PolyvChatImgHistory) {//历史图片事件
            PolyvChatImgHistory chatImgHistory = (PolyvChatImgHistory) chatMessage;
            PolyvChatImgHistory.UserBean userBean = chatImgHistory.getUser();
            if (userBean != null) {
                userType = userBean.getUserType();
                nickName = userBean.getNick();
                userId = userBean.getUserId();
                actor = userBean.getActor();
            }
            PolyvChatImgHistory.ContentBean contentBean = chatImgHistory.getContent();
            if (contentBean != null) {
                chatImgUrl = contentBean.getUploadImgUrl();
                if (contentBean.getSize() != null) {
                    chatImgWidth = (int) contentBean.getSize().getWidth();
                    chatImgHeight = (int) contentBean.getSize().getHeight();
                }
            }
        } else if (chatMessage instanceof PLVCustomGiftEvent) {
            speakMsg = ((PLVCustomGiftEvent) chatMessage).span;
        }
    }

    protected void fitChatImgWH(int width, int height, View view, int maxLengthDp ,int minLengthDp) {
        int maxLength = ConvertUtils.dp2px(maxLengthDp);
        int minLength = ConvertUtils.dp2px(minLengthDp);
        //计算显示的图片大小
        float percentage = width * 1f / height;
        if (percentage == 1) {//方图
            if (width < minLength) {
                width = height = minLength;
            } else if (width > maxLength) {
                width = height = maxLength;
            }
        } else if (percentage < 1) {//竖图
            height = maxLength;
            width = (int) Math.max(minLength, height * percentage);
        } else {//横图
            width = maxLength;
            height = (int) Math.max(minLength, width / percentage);
        }
        ViewGroup.LayoutParams vlp = view.getLayoutParams();
        vlp.width = width;
        vlp.height = height;
        view.setLayoutParams(vlp);
    }
}
