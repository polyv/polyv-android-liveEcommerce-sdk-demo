package com.easefun.polyv.livedemo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.easefun.polyv.livecommon.modules.login.IPLVSceneLoginManager;
import com.easefun.polyv.livecommon.modules.login.PLVLiveLoginResult;
import com.easefun.polyv.livecommon.modules.login.PLVPlaybackLoginResult;
import com.easefun.polyv.livecommon.modules.login.PLVSceneLoginManager;
import com.easefun.polyv.livecommon.net.IPLVNetRequestListener;
import com.easefun.polyv.livecommon.ui.window.PLVBaseActivity;
import com.easefun.polyv.livecommon.utils.PLVToastUtils;
import com.easefun.polyv.liveecommerce.scenes.PLVECLiveActivity;
import com.easefun.polyv.liveecommerce.scenes.PLVECPlaybackActivity;

/**
 * date: 2020-04-29
 * author: hwj
 * descriptione
 */
public class PLVLoginActivity extends PLVBaseActivity {
    private IPLVSceneLoginManager loginManager;

    private Button enterLiveBt;
    private Button enterPlaybackBt;
    private EditText userIdEt;
    private EditText appIdEt;
    private EditText appSecretEt;
    private EditText channelIdEt;
    private EditText vidEt;
    private ProgressDialog loginProgressDialog;

    private String defaultUserId = "a98c9950bd";
    private String defaultAppId = "ezl37aj4dj";
    private String defaultAppSecret = "22fa4e608b72428a83bf3a16681cc05a";
    private String defaultChannelId = "309004";
    private String defaultVid = "a98c9950bd76141650dba3112995fcec_a";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plv_login_activity);

        initView();

        loginManager = new PLVSceneLoginManager();

        enterLiveBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginProgressDialog.show();
                loginManager.loginLive(getUserId(), getAppSecret(), getChannelId(), getAppId(), new IPLVNetRequestListener<PLVLiveLoginResult>() {
                    @Override
                    public void onSuccess(PLVLiveLoginResult polyvLiveLoginResult) {
                        loginProgressDialog.dismiss();
                        if (!polyvLiveLoginResult.isNormal()) {
                            PLVToastUtils.showShort("该场景不支持三分屏类型");
                            return;
                        }
                        PLVECLiveActivity.launchLive(PLVLoginActivity.this, getChannelId(), getViewerId(), getViewerName());
                    }

                    @Override
                    public void onFailed(String msg, Throwable throwable) {
                        loginProgressDialog.dismiss();
                        PLVToastUtils.showShort(msg);
                        throwable.printStackTrace();
                    }
                });
            }
        });

        enterPlaybackBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginProgressDialog.show();
                loginManager.loginPlayback(getUserId(), getAppSecret(), getChannelId(), getVid(), getAppId(), new IPLVNetRequestListener<PLVPlaybackLoginResult>() {
                    @Override
                    public void onSuccess(PLVPlaybackLoginResult polyvPlaybackLoginResult) {
                        loginProgressDialog.dismiss();
                        if (!polyvPlaybackLoginResult.isNormal()) {
                            PLVToastUtils.showShort("该场景不支持三分屏类型");
                            return;
                        }
                        PLVECPlaybackActivity.launchPlayback(PLVLoginActivity.this, getChannelId(), getVid(), getViewerId(), getViewerName());
                    }

                    @Override
                    public void onFailed(String msg, Throwable throwable) {
                        loginProgressDialog.dismiss();
                        PLVToastUtils.showShort(msg);
                        throwable.printStackTrace();
                    }
                });
            }
        });
    }

    private void initView() {
        userIdEt = findViewById(R.id.userid_et);
        appIdEt = findViewById(R.id.appid_et);
        appSecretEt = findViewById(R.id.appsecret_et);
        channelIdEt = findViewById(R.id.channelid_et);
        vidEt = findViewById(R.id.vid_et);

        enterLiveBt = findViewById(R.id.enter_live_bt);
        enterPlaybackBt = findViewById(R.id.enter_playback_bt);

        loginProgressDialog = new ProgressDialog(this);
        loginProgressDialog.setMessage("正在登录中，请稍等...");
        loginProgressDialog.setCanceledOnTouchOutside(false);
        loginProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                loginManager.destroy();
            }
        });
    }

    private String getUserId() {
        String userId = TextUtils.isEmpty(userId = userIdEt.getText().toString()) ? defaultUserId : userId;
        return userId;
    }

    private String getAppId() {
        String appId = TextUtils.isEmpty(appId = appIdEt.getText().toString()) ? defaultAppId : appId;
        return appId;
    }

    private String getAppSecret() {
        String appSecret = TextUtils.isEmpty(appSecret = appSecretEt.getText().toString()) ? defaultAppSecret : appSecret;
        return appSecret;
    }

    private String getChannelId() {
        String channelId = TextUtils.isEmpty(channelId = channelIdEt.getText().toString()) ? defaultChannelId : channelId;
        return channelId;
    }

    private String getVid() {
        String vid = TextUtils.isEmpty(vid = vidEt.getText().toString()) ? defaultVid : vid;
        return vid;
    }

    private String getViewerId() {
        //todo 在这里替换为你的用户ID
        return Build.SERIAL + "";
    }

    private String getViewerName() {
        //todo 在这里替换为你的用户昵称
        return "观众" + Build.SERIAL;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loginManager.destroy();
    }
}
