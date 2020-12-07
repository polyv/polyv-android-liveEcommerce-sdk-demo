package com.easefun.polyv.livecommon.modules.chatroom.model;

import android.arch.lifecycle.LiveData;
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
    //点赞数
    private MutableLiveData<Long> likesCountData = new MutableLiveData<>();
    //观看热度数
    private MutableLiveData<Long> viewerCountData = new MutableLiveData<>();
    //在线人数
    private MutableLiveData<Integer> onlineCountData = new MutableLiveData<>();

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

    public LiveData<Long> getLikesCountData() {
        return likesCountData;
    }

    public void postLikesCountData(long likesCount) {
        likesCountData.postValue(likesCount);
    }

    public LiveData<Long> getViewerCountData() {
        return viewerCountData;
    }

    public void postViewerCountData(long viewerCount) {
        viewerCountData.postValue(viewerCount);
    }

    public LiveData<Integer> getOnlineCountData() {
        return onlineCountData;
    }

    public void postOnlineCountData(int onlineCount) {
        onlineCountData.postValue(onlineCount);
    }
}
