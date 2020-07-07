package com.easefun.polyv.livecommon.dataservice;

import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.config.PLVLiveChannelConfig;

/**
 * 业务数据协议实现
 */
public abstract class PLVAbsBusinessDataImpl implements IPLVBusinessDataProtocol {
    protected IPLVLiveRoomData liveRoomData;

    @Override
    public void setLiveRoomData(@NonNull IPLVLiveRoomData liveRoomData) {
        this.liveRoomData = liveRoomData;
    }

    @NonNull
    @Override
    public IPLVLiveRoomData getLiveRoomData() {
        return liveRoomData;
    }

    @NonNull
    protected PLVLiveChannelConfig getConfig() {
        return getLiveRoomData().getConfig();
    }
}
