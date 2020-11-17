package com.easefun.polyv.livecommon.data;

import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.cloudclass.model.commodity.saas.PolyvCommodityVO;
import com.easefun.polyv.livecommon.config.PLVLiveChannelConfig;

/**
 * 直播间数据的接口，存放直播api获取的数据以及各个业务间公用的数据，因此需要每个业务模块间持有同个直播间数据对象
 */
public interface IPLVLiveRoomData {

    /**
     * 获取直播频道参数信息
     */
    @NonNull
    PLVLiveChannelConfig getConfig();

    /**
     * 设置直播频道参数信息
     */
    void setConfig(@NonNull PLVLiveChannelConfig config);

    /**
     * 获取直播详情数据LiveData
     */
    MutableLiveData<PolyvLiveClassDetailVO> getClassDetailVO();

    /**
     * 提交直播详情数据
     */
    void postClassDetailVO(PolyvLiveClassDetailVO liveClassDetailVO);

    /**
     * 获取直播商品数据LiveData
     */
    MutableLiveData<PolyvCommodityVO> getCommodityVO();

    /**
     * 提交直播商品数据
     */
    void postCommodityVO(PolyvCommodityVO commodityVal);

    /**
     * 获取直播状态LiveData
     */
    MutableLiveData<PLVLiveRoomData.LiveStatus> getLiveStatusData();

    /**
     * 提交直播状态数据
     */
    void postLiveStatusData(PLVLiveRoomData.LiveStatus liveStatus);

    /**
     * 设置sessionId
     */
    void setSessionId(String sessionId);

    /**
     * 获取sessionId
     */
    String getSessionId();
}
