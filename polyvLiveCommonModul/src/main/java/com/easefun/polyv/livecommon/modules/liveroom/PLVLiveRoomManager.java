package com.easefun.polyv.livecommon.modules.liveroom;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import com.easefun.polyv.cloudclass.chat.PolyvChatApiRequestHelper;
import com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.cloudclass.model.commodity.PolyvCommodityVO;
import com.easefun.polyv.cloudclass.net.PolyvApiManager;
import com.easefun.polyv.foundationsdk.net.PolyvResponseBean;
import com.easefun.polyv.foundationsdk.net.PolyvResponseExcutor;
import com.easefun.polyv.foundationsdk.net.PolyvrResponseCallback;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBaseRetryFunction;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBaseTransformer;
import com.easefun.polyv.foundationsdk.sign.PolyvSignCreator;
import com.easefun.polyv.livecommon.dataservice.IPLVLiveRoomData;
import com.easefun.polyv.livecommon.dataservice.PLVAbsBusinessDataImpl;
import com.easefun.polyv.livecommon.net.IPLVNetRequestListener;
import com.easefun.polyv.livecommon.utils.PLVReflectionUtils;

import java.util.Map;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 直播间业务管理器，主要用于获取直播api相关的数据
 */
public class PLVLiveRoomManager extends PLVAbsBusinessDataImpl implements IPLVLiveRoomManager {
    private Disposable increasePageViewerDisposable;
    private Disposable getLiveDetailDisposable;
    private Disposable getCommodityInfoDisposable;

    public PLVLiveRoomManager(@NonNull IPLVLiveRoomData liveRoomData) {
        this.liveRoomData = liveRoomData;
    }

    /**
     * 上报观看热度
     */
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
                        if (listener != null) {
                            listener.onSuccess(integer);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        super.onError(throwable);
                        if (listener != null) {
                            listener.onFailed(throwable.getMessage(), throwable);
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

    /**
     * 取消上报热度接口的请求
     */
    @Override
    public void disposableIncreasePageViewer() {
        if (increasePageViewerDisposable != null) {
            increasePageViewerDisposable.dispose();
        }
    }

    /**
     * 获取直播详情数据
     */
    @Override
    public void getLiveDetail(final IPLVNetRequestListener<PolyvLiveClassDetailVO> listener) {
        disposableGetLiveDetail();
        String channelId = getConfig().getChannelId();
        getLiveDetailDisposable = PolyvChatApiRequestHelper.getInstance().requestLiveClassDetailApi(channelId)
                .retryWhen(new PolyvRxBaseRetryFunction(3, 3000))
                .subscribe(new Consumer<PolyvLiveClassDetailVO>() {
                    @Override
                    public void accept(PolyvLiveClassDetailVO liveClassDetailVO) throws Exception {
                        liveRoomData.postClassDetailVO(liveClassDetailVO);
                        if (listener != null) {
                            listener.onSuccess(liveClassDetailVO);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if (listener != null) {
                            listener.onFailed(throwable.getMessage(), throwable);
                        }
                    }
                });
    }

    /**
     * 取消直播详情接口的请求
     */
    @Override
    public void disposableGetLiveDetail() {
        if (getLiveDetailDisposable != null) {
            getLiveDetailDisposable.dispose();
        }
    }

    @Override
    public void getCommodityInfo(final IPLVNetRequestListener<PolyvCommodityVO> listener) {
        disposableGetCommodityInfo();
        String channelId = getConfig().getChannelId();
        String appId = getConfig().getAccount().getAppId();
        String appSecret = getConfig().getAccount().getAppSecret();
        long timestamp = System.currentTimeMillis();
        int page = 1;
        final int limit = 200;
        Map<String, String> map = new ArrayMap<>();
        map.put("appId", appId);
        map.put("timestamp", timestamp + "");
        map.put("channelId", channelId);
        map.put("page", page + "");
        map.put("limit", limit + "");
        String sign = PolyvSignCreator.createSign(appSecret, map);
        getCommodityInfoDisposable = PolyvApiManager.getPolyvLiveStatusApi().getCommodityInfo(Integer.valueOf(channelId), appId, timestamp, page, limit, sign)
                .retryWhen(new PolyvRxBaseRetryFunction(3, 3000))
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
                            listener.onFailed(throwable.getMessage(), throwable);
                        }
                    }
                });
    }

    @Override
    public void disposableGetCommodityInfo() {
        if (getCommodityInfoDisposable != null) {
            getCommodityInfoDisposable.dispose();
        }
    }

    @Override
    public void destroy() {
        disposableIncreasePageViewer();
        disposableGetLiveDetail();
        PLVReflectionUtils.cleanFields(this);
    }
}
