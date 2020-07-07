package com.easefun.polyv.livecommon.config;

import android.app.Application;

import com.easefun.polyv.cloudclass.config.PolyvLiveSDKClient;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;

/**
 * sdk配置类
 */
public class PLVLiveSDKConfig {

    //初始化sdk配置
    public static void init(Parameter parameter) {
        PolyvCommonLog.setDebug(parameter.isOpenDebugLog);
        PolyvLiveSDKClient liveSDKClient = PolyvLiveSDKClient.getInstance();
        liveSDKClient.initContext(parameter.application);
        liveSDKClient.enableHttpDns(parameter.isEnableHttpDns);
    }

    public static class Parameter {
        private Application application;
        private boolean isOpenDebugLog = true;
        private boolean isEnableHttpDns = false;

        public Parameter(Application application) {
            this.application = application;
        }

        public Parameter isOpenDebugLog(boolean isOpenDebugLog) {
            this.isOpenDebugLog = isOpenDebugLog;
            return this;
        }

        public Parameter isEnableHttpDns(boolean isEnableHttpDns) {
            this.isEnableHttpDns = isEnableHttpDns;
            return this;
        }
    }
}
