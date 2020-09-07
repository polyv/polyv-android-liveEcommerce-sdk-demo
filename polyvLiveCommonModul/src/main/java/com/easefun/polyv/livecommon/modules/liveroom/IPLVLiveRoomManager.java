package com.easefun.polyv.livecommon.modules.liveroom;

import com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.cloudclass.model.commodity.saas.PolyvCommodityVO;
import com.easefun.polyv.livecommon.net.IPLVNetRequestListener;

/**
 * 直播间业务管理器接口
 */
public interface IPLVLiveRoomManager {

    /**
     * 上报观看热度
     */
    void increasePageViewer(IPLVNetRequestListener<Integer> listener);

    /**
     * 取消上报热度接口的请求
     */
    void disposableIncreasePageViewer();

    /**
     * 获取直播详情数据
     */
    void getLiveDetail(IPLVNetRequestListener<PolyvLiveClassDetailVO> listener);

    /**
     * 取消直播详情接口的请求
     */
    void disposableGetLiveDetail();

    /**
     * 获取商品信息
     */
    void getCommodityInfo(IPLVNetRequestListener<PolyvCommodityVO> listener);

    /**
     * 获取商品信息
     */
    void getCommodityInfo(int rank, IPLVNetRequestListener<PolyvCommodityVO> listener);

    /**
     * 获取请求商品的rank
     */
    int getCommodityRank();

    /**
     * 取消商品信息接口的请求
     */
    void disposableGetCommodityInfo();

    /**
     * 销毁，取消所有的接口请求
     */
    void destroy();
}
