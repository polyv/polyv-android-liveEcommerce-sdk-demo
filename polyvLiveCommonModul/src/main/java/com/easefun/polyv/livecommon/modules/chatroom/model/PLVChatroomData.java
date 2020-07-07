package com.easefun.polyv.livecommon.modules.chatroom.model;

import android.arch.lifecycle.MutableLiveData;

import com.easefun.polyv.cloudclass.chat.PolyvLocalMessage;
import com.easefun.polyv.cloudclass.model.bulletin.PolyvBulletinVO;

/**
 * 聊天室业务数据
 */
public class PLVChatroomData {
    //本地发言信息
    private MutableLiveData<PolyvLocalMessage> localMessage = new MutableLiveData<>();
    //公告信息
    private MutableLiveData<PolyvBulletinVO> bulletinVO = new MutableLiveData<>();

    public MutableLiveData<PolyvLocalMessage> getLocalMessage() {
        return localMessage;
    }

    public void postLocalMessage(PolyvLocalMessage message) {
        localMessage.postValue(message);
    }

    public MutableLiveData<PolyvBulletinVO> getBulletinVO() {
        return bulletinVO;
    }

    //bulletin为null时为隐藏公告
    public void postBulletinVO(PolyvBulletinVO bulletin) {
        bulletinVO.postValue(bulletin);
    }
}
