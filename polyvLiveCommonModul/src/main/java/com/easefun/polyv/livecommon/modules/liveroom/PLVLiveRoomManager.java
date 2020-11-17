package com.easefun.polyv.livecommon.modules.liveroom;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import com.easefun.polyv.cloudclass.chat.PolyvChatApiRequestHelper;
import com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.cloudclass.model.PolyvLiveStatusVO;
import com.easefun.polyv.cloudclass.model.commodity.saas.PolyvCommodityVO;
import com.easefun.polyv.cloudclass.net.PolyvApiManager;
import com.easefun.polyv.foundationsdk.net.PolyvResponseBean;
import com.easefun.polyv.foundationsdk.net.PolyvResponseExcutor;
import com.easefun.polyv.foundationsdk.net.PolyvrResponseCallback;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBaseRetryFunction;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBaseTransformer;
import com.easefun.polyv.foundationsdk.sign.PolyvSignCreator;
import com.easefun.polyv.livecommon.config.PLVLiveChannelConfig;
import com.easefun.polyv.livecommon.data.IPLVLiveRoomData;
import com.easefun.polyv.livecommon.data.PLVLiveRoomData;
import com.easefun.polyv.livecommon.net.IPLVNetRequestListener;
import com.easefun.polyv.livecommon.utils.PLVReflectionUtils;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import retrofit2.HttpException;

/**
 * 直播间业务管理器，主要用于获取直播api相关的数据
 */
public class PLVLiveRoomManager implements IPLVLiveRoomManager {
    // <editor-fold defaultstate="collapsed" desc="成员变量">
    public static final int GET_COMMODITY_COUNT = 20;
    //直播间数据
    private IPLVLiveRoomData liveRoomData;
    //请求商品的rank
    private int commodityRank;
    //接口请求disposable
    private Disposable increasePageViewerDisposable;
    private Disposable getLiveDetailDisposable;
    private Disposable getCommodityInfoDisposable;
    private Disposable getLiveStatusDisposable;

    //观看热度是否已经请求成功
    private boolean isRequestedPageViewer;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="公共静态方法">
    public static String getErrorMessage(Throwable t) {
        String errorMessage = t.getMessage();
        if (t instanceof HttpException) {
            try {
                errorMessage = ((HttpException) t).response().errorBody().string();
            } catch (Exception e) {
            }
        }
        return errorMessage;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">
    public PLVLiveRoomManager(@NonNull IPLVLiveRoomData liveRoomData) {
        this.liveRoomData = liveRoomData;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="观看热度 - 请求、取消">
    @Override
    public void increasePageViewer(final IPLVNetRequestListener<Integer> listener) {
        disposableIncreasePageViewer();
        String appId = getConfig().getAccount().getAppId();
        String appSecret = getConfig().getAccount().getAppSecret();
        String channelId = getConfig().getChannelId();
        int times = 1;
        long ts = System.currentTimeMillis();
        Map<String, String> paramMap = new ArrayMap<>();
        paramMap.put("channelId", channelId);
        paramMap.put("appId", appId);
        paramMap.put("timestamp", String.valueOf(ts));
        paramMap.put("times", String.valueOf(times));
        String sign = PolyvSignCreator.createSign(appSecret, paramMap);
        increasePageViewerDisposable = PolyvResponseExcutor.excuteDataBean(
                PolyvApiManager.getPolyvLiveStatusApi().increasePageViewer(Integer.valueOf(channelId), appId, ts, sign, times),
                Integer.class, new PolyvrResponseCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer integer) {
                        isRequestedPageViewer = true;
                        if (listener != null) {
                            listener.onSuccess(integer);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        super.onError(throwable);
                        if (listener != null) {
                            listener.onFailed(getErrorMessage(throwable), throwable);
                        }
                    }

                    @Override
                    public void onFailure(PolyvResponseBean<Integer> polyvResponseBean) {
                        super.onFailure(polyvResponseBean);
                        if (listener != null) {
                            String errorMsg = responseBean.toString();
                            listener.onFailed(errorMsg, new Throwable(errorMsg));
                        }
                    }
                });
    }

    @Override
    public void disposableIncreasePageViewer() {
        if (increasePageViewerDisposable != null) {
            increasePageViewerDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="直播详情 - 请求、取消">
    @Override
    public void getLiveDetail(final IPLVNetRequestListener<PolyvLiveClassDetailVO> listener) {
        disposableGetLiveDetail();
        String channelId = getConfig().getChannelId();
        getLiveDetailDisposable = PolyvChatApiRequestHelper.getInstance().requestLiveClassDetailApi(channelId)
                .retryWhen(new PolyvRxBaseRetryFunction(3, 3000))
                .subscribe(new Consumer<PolyvLiveClassDetailVO>() {
                    @Override
                    public void accept(PolyvLiveClassDetailVO liveClassDetailVO) throws Exception {
                        if (!isRequestedPageViewer && liveClassDetailVO.getData() != null) {
                            //如果观看热度还没请求成功，则加上自己
                            liveClassDetailVO.getData().setPageView(liveClassDetailVO.getData().getPageView() + 1);
                        }
                        liveRoomData.postClassDetailVO(liveClassDetailVO);
                        if (listener != null) {
                            listener.onSuccess(liveClassDetailVO);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if (listener != null) {
                            listener.onFailed(getErrorMessage(throwable), throwable);
                        }
                    }
                });
    }

    @Override
    public void disposableGetLiveDetail() {
        if (getLiveDetailDisposable != null) {
            getLiveDetailDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="商品信息 - 请求、取消">
    @Override
    public void getCommodityInfo(IPLVNetRequestListener<PolyvCommodityVO> listener) {
        getCommodityInfo(-1, listener);
    }

    @Override
    public void getCommodityInfo(int rank, final IPLVNetRequestListener<PolyvCommodityVO> listener) {
        this.commodityRank = rank;
        disposableGetCommodityInfo();
        String channelId = getConfig().getChannelId();
        String appId = getConfig().getAccount().getAppId();
        String appSecret = getConfig().getAccount().getAppSecret();
        long timestamp = System.currentTimeMillis();
        int count = GET_COMMODITY_COUNT;
        Map<String, String> map = new ArrayMap<>();
        map.put("appId", appId);
        map.put("timestamp", timestamp + "");
        map.put("channelId", channelId);
        map.put("count", count + "");
        if (rank > -1) {
            map.put("rank", rank + "");
        }
        String sign = PolyvSignCreator.createSign(appSecret, map);
        Observable<PolyvCommodityVO> commodityVOObservable;
        if (rank > -1) {
            commodityVOObservable = PolyvApiManager.getPolyvLiveStatusApi().getProductList(channelId, appId, timestamp, count, rank, sign);
        } else {
            commodityVOObservable = PolyvApiManager.getPolyvLiveStatusApi().getProductList(channelId, appId, timestamp, count, sign);
        }
        getCommodityInfoDisposable = commodityVOObservable.retryWhen(new PolyvRxBaseRetryFunction(3, 3000))
                .compose(new PolyvRxBaseTransformer<PolyvCommodityVO, PolyvCommodityVO>())
                .subscribe(new Consumer<PolyvCommodityVO>() {
                    @Override
                    public void accept(PolyvCommodityVO polyvCommodityVO) throws Exception {
                        liveRoomData.postCommodityVO(polyvCommodityVO);
                        if (listener != null) {
                            listener.onSuccess(polyvCommodityVO);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if (listener != null) {
                            listener.onFailed(getErrorMessage(throwable), throwable);
                        }
                    }
                });
    }

    @Override
    public int getCommodityRank() {
        return commodityRank;
    }

    @Override
    public void disposableGetCommodityInfo() {
        if (getCommodityInfoDisposable != null) {
            getCommodityInfoDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="直播状态 - 请求、取消">
    @Override
    public void getLiveStatus(final IPLVNetRequestListener<PLVLiveRoomData.LiveStatus> listener) {
        disposableGetLiveStatus();
        String channelId = getConfig().getChannelId();
        getLiveStatusDisposable = PolyvResponseExcutor.excuteUndefinData(PolyvApiManager.getPolyvLiveStatusApi().getLiveStatusJson2(channelId),
                new PolyvrResponseCallback<PolyvLiveStatusVO>() {
                    @Override
                    public void onSuccess(PolyvLiveStatusVO statusVO) {
                        if (statusVO != null && statusVO.getCode() == PolyvResponseExcutor.CODE_SUCCESS) {
                            PLVLiveRoomData.LiveStatus liveStatus = null;
                            String var = statusVO.getData().split(",")[0];
                            if (PLVLiveRoomData.LiveStatus.LIVE.getValue().equals(var)) {
                                liveStatus = PLVLiveRoomData.LiveStatus.LIVE;
                            } else if (PLVLiveRoomData.LiveStatus.STOP.getValue().equals(var)) {
                                liveStatus = PLVLiveRoomData.LiveStatus.STOP;
                            } else if (PLVLiveRoomData.LiveStatus.END.getValue().equals(var)) {
                                liveStatus = PLVLiveRoomData.LiveStatus.END;
                            }
                            liveRoomData.postLiveStatusData(liveStatus);
                            if (listener != null) {
                                listener.onSuccess(liveStatus);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        super.onError(throwable);
                        if (listener != null) {
                            listener.onFailed(getErrorMessage(throwable), throwable);
                        }
                    }

                    @Override
                    public void onFailure(PolyvResponseBean<PolyvLiveStatusVO> polyvResponseBean) {
                        super.onFailure(polyvResponseBean);
                        if (listener != null) {
                            String errorMsg = responseBean.toString();
                            listener.onFailed(errorMsg, new Throwable(errorMsg));
                        }
                    }
                });
    }

    @Override
    public void disposableGetLiveStatus() {
        if (getLiveStatusDisposable != null) {
            getLiveStatusDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="销毁">
    @Override
    public void destroy() {
        disposableIncreasePageViewer();
        disposableGetLiveDetail();
        disposableGetCommodityInfo();
        disposableGetLiveStatus();
        PLVReflectionUtils.cleanFields(this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="获取config">
    private PLVLiveChannelConfig getConfig() {
        return liveRoomData.getConfig();
    }
    // </editor-fold>
}
