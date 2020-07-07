package com.easefun.polyv.livecommon.dataservice;

import android.support.annotation.NonNull;

/**
 * 业务数据协议
 */
public interface IPLVBusinessDataProtocol {

    /**
     * 设置直播间数据
     */
    void setLiveRoomData(@NonNull IPLVLiveRoomData liveRoomData);

    /**
     * 获取直播间数据
     */
    @NonNull
    IPLVLiveRoomData getLiveRoomData();
}
