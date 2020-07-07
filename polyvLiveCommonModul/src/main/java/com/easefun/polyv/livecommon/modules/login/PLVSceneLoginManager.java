package com.easefun.polyv.livecommon.modules.login;

import com.easefun.polyv.businesssdk.model.chat.PolyvChatDomain;
import com.easefun.polyv.businesssdk.model.video.PolyvPlayBackVO;
import com.easefun.polyv.businesssdk.service.PolyvLoginManager;
import com.easefun.polyv.cloudclass.model.PolyvLiveStatusVO;
import com.easefun.polyv.cloudclass.net.PolyvApiManager;
import com.easefun.polyv.foundationsdk.net.PolyvResponseBean;
import com.easefun.polyv.foundationsdk.net.PolyvResponseExcutor;
import com.easefun.polyv.foundationsdk.net.PolyvrResponseCallback;
import com.easefun.polyv.livecommon.config.PLVLiveChannelConfig;
import com.easefun.polyv.livecommon.net.IPLVNetRequestListener;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;

/**
 * date: 2020-04-17
 * author: hwj
 * description:
 */
public class PLVSceneLoginManager implements IPLVSceneLoginManager {
    // <editor-fold defaultstate="collapsed" desc="成员变量">
    //登录直播
    private Disposable loginLiveDisposable;
    private Disposable getLiveStatusDisposable;

    //登录回放
    private Disposable loginPlaybackDisposable;
    private Disposable getPlaybackStatusDisposable;

    //验证直播/回放参数接口的disposable
    private Disposable verifyDisposable;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="登陆直播">
    @Override
    public void loginLive(final String userId, final String appSecret, final String channelId, final String appId,
                          final IPLVNetRequestListener<PLVLiveLoginResult> loginListener) {
        dispose(loginLiveDisposable);
        loginLiveDisposable = Observable.zip(
                verify(userId, appSecret, channelId, "", appId),
                requestLiveStatus(channelId),
                new BiFunction<PolyvChatDomain, PolyvLiveStatusVO, PLVLiveLoginResult>() {
                    @Override
                    public PLVLiveLoginResult apply(PolyvChatDomain polyvChatDomain, PolyvLiveStatusVO polyvLiveStatusVO) throws Exception {
                        //处理verifyLive的结果，登录成功后设置账号信息
                        PLVLiveChannelConfig.setupAccount(userId, appId, appSecret);
                        //聊天室私有域名设置
                        PLVLiveChannelConfig.setupChatDomain(polyvChatDomain);

                        //处理requestLiveStatus的结果
                        String data = polyvLiveStatusVO.getData();
                        String[] dataArr = data.split(",");
                        //是否有ppt。要将改结果回调上去
                        final boolean isAlone = "alone".equals(dataArr[1]);

                        return new PLVLiveLoginResult(isAlone);
                    }
                }
        ).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<PLVLiveLoginResult>() {
            @Override
            public void accept(PLVLiveLoginResult result) throws Exception {
                if (loginListener != null) {
                    //回调结果
                    loginListener.onSuccess(result);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (loginListener != null) {
                    loginListener.onFailed(throwable.getMessage(), throwable);
                }
            }
        });
    }

    //获取直播状态的接口请求
    private Observable<PolyvLiveStatusVO> requestLiveStatus(final String channelId) {
        dispose(getLiveStatusDisposable);
        return Observable.create(new ObservableOnSubscribe<PolyvLiveStatusVO>() {
            @Override
            public void subscribe(final ObservableEmitter<PolyvLiveStatusVO> emitter) throws Exception {
                getLiveStatusDisposable = PolyvResponseExcutor.excuteUndefinData(PolyvApiManager.getPolyvLiveStatusApi().geLiveStatusJson(channelId)
                        , new PolyvResponseCallbackAdapter<>(emitter));
            }
        }).doOnDispose(new Action() {
            @Override
            public void run() throws Exception {
                dispose(getLiveStatusDisposable);
            }
        });

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="登录回放">
    @Override
    public void loginPlayback(final String userId, final String appSecret, final String channelId, String vid,
                              final String appId, final IPLVNetRequestListener<PLVPlaybackLoginResult> loginListener) {
        dispose(loginPlaybackDisposable);
        loginPlaybackDisposable = Observable.zip(
                verify(userId, "", channelId, vid, appId),
                requestPlaybackStatus(vid),
                new BiFunction<PolyvChatDomain, PolyvPlayBackVO, PLVPlaybackLoginResult>() {
                    @Override
                    public PLVPlaybackLoginResult apply(PolyvChatDomain polyvChatDomain, PolyvPlayBackVO polyvPlayBackVO) throws Exception {
                        //登录成功后设置账号信息
                        PLVLiveChannelConfig.setupAccount(userId, appId, appSecret);
                        boolean isNormal = polyvPlayBackVO.getLiveType() == 0;
                        return new PLVPlaybackLoginResult(isNormal);
                    }
                }
        ).subscribe(new Consumer<PLVPlaybackLoginResult>() {
            @Override
            public void accept(PLVPlaybackLoginResult result) throws Exception {
                if (loginListener != null) {
                    loginListener.onSuccess(result);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (loginListener != null) {
                    loginListener.onFailed(throwable.getMessage(), throwable);
                }
            }
        });
    }

    //获取回放状态的接口请求
    private Observable<PolyvPlayBackVO> requestPlaybackStatus(final String vid) {
        dispose(getPlaybackStatusDisposable);
        return Observable.create(new ObservableOnSubscribe<PolyvPlayBackVO>() {
            @Override
            public void subscribe(final ObservableEmitter<PolyvPlayBackVO> emitter) throws Exception {
                getPlaybackStatusDisposable = PolyvLoginManager.getPlayBackType(vid, new PolyvResponseCallbackAdapter<>(emitter));
            }
        }).doOnDispose(new Action() {
            @Override
            public void run() throws Exception {
                dispose(getPlaybackStatusDisposable);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="验证直播/回放参数的接口请求">
    private Observable<PolyvChatDomain> verify(final String userId, final String appSecret, final String channelId, final String vid, final String appId) {
        dispose(verifyDisposable);
        return Observable.create(new ObservableOnSubscribe<PolyvChatDomain>() {
            @Override
            public void subscribe(final ObservableEmitter<PolyvChatDomain> emitter) throws Exception {
                verifyDisposable = PolyvLoginManager.checkLoginToken(userId, appSecret, appId, channelId, vid,
                        new PolyvResponseCallbackAdapter<>(emitter));
            }
        }).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                dispose(verifyDisposable);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="dispose方法">
    private void dispose(Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="销毁">
    @Override
    public void destroy() {
        dispose(loginLiveDisposable);
        dispose(loginPlaybackDisposable);
    }
    // </editor-fold>

    /**
     * Polyv请求回调转成RxJava Observable的适配器
     *
     * @param <Bean> 携带的数据
     */
    private static class PolyvResponseCallbackAdapter<Bean> extends PolyvrResponseCallback<Bean> {
        private ObservableEmitter<Bean> emitter;

        PolyvResponseCallbackAdapter(ObservableEmitter<Bean> emitter) {
            this.emitter = emitter;
        }

        @Override
        public void onSuccess(Bean bean) {
            emitter.onNext(bean);
        }

        @Override
        public void onFailure(PolyvResponseBean<Bean> responseBean) {
            super.onFailure(responseBean);
            emitter.onError(new Throwable(responseBean.toString()));
        }

        @Override
        public void onError(Throwable e) {
            super.onError(e);
            emitter.onError(e);
        }
    }
}
