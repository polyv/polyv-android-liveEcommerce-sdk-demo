package com.easefun.polyv.livecommon.modules.chatroom;

/**
 * 聊天室回调信息封装类
 */
public class PLVChatroomMessage {
    public String socketListen;
    public String event;
    public String message;

    public PLVChatroomMessage(String message, String event, String socketListen) {
        this.message = message;
        this.event = event;
        this.socketListen = socketListen;
    }
}
