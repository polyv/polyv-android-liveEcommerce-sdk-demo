package com.easefun.polyv.livecommon.modules.chatroom;

import android.text.SpannableStringBuilder;

/**
 * 自定义礼物事件，用于聊天室列表显示礼物信息
 */
public class PLVCustomGiftEvent {
    public SpannableStringBuilder span;

    public PLVCustomGiftEvent(SpannableStringBuilder span) {
        this.span = span;
    }
}
