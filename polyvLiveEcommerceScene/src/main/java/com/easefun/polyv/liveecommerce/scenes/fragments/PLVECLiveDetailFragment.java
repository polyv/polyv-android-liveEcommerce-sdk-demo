package com.easefun.polyv.liveecommerce.scenes.fragments;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.cloudclass.model.bulletin.PolyvBulletinVO;
import com.easefun.polyv.livecommon.ui.widget.webview.PLVSafeWebView;
import com.easefun.polyv.livecommon.ui.widget.webview.PLVWebViewHelper;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 直播详情页： 公告、直播介绍
 */
public class PLVECLiveDetailFragment extends PLVBaseFragment {
    // <editor-fold defaultstate="collapsed" desc="成员变量">
    //公告布局
    private ViewGroup bulletinBlurLy;
    private TextView bulletinMsgTv;
    //直播介绍布局
    private ViewGroup introBlurLy;
    private PLVSafeWebView introWebView;
    private TextView introEmtTv;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期方法">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvec_live_page_detail_fragment, null);
        initView();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (onViewActionListener != null) {
            onViewActionListener.onViewCreated();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (introWebView != null) {
            introWebView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (introWebView != null) {
            introWebView.onResume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (introWebView != null) {
            if (introWebView.getParent() != null) {
                ((ViewGroup) introWebView.getParent()).removeView(introWebView);
            }
            introWebView.stopLoading();
            introWebView.clearMatches();
            introWebView.clearHistory();
            introWebView.clearSslPreferences();
            introWebView.clearCache(true);
            introWebView.loadUrl("about:blank");
            introWebView.removeAllViews();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                introWebView.removeJavascriptInterface("AndroidNative");
            }
            introWebView.destroy();
        }
        introWebView = null;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        bulletinBlurLy = findViewById(R.id.bulletin_ly);
        bulletinMsgTv = findViewById(R.id.bulletin_msg_tv);
        introBlurLy = findViewById(R.id.intro_ly);
        introEmtTv = findViewById(R.id.intro_emt_tv);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="公告 - 数据处理，显示、隐藏">
    public void setBulletinVO(PolyvBulletinVO bulletinVO) {
        if (bulletinVO != null) {
            showBulletin(bulletinVO);
        } else {
            hideBulletin();
        }
    }

    private void showBulletin(PolyvBulletinVO bulletinVO) {
        bulletinMsgTv.setText(Html.fromHtml(bulletinVO.getContent()));
        bulletinMsgTv.setMovementMethod(LinkMovementMethod.getInstance());
        bulletinBlurLy.setVisibility(View.VISIBLE);
    }

    private void hideBulletin() {
        bulletinBlurLy.setVisibility(View.GONE);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="直播介绍 - 数据处理，webView加载">
    public void setClassDetailVO(PolyvLiveClassDetailVO liveClassDetailVO) {
        if (liveClassDetailVO == null) {
            return;
        }
        for (PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean : liveClassDetailVO.getData().getChannelMenus()) {
            if (PolyvLiveClassDetailVO.MENUTYPE_DESC.equals(channelMenusBean.getMenuType())) {
                acceptIntroMsg(channelMenusBean.getContent());
                break;
            }
        }
    }

    private void acceptIntroMsg(String content) {
        if (TextUtils.isEmpty(content)) {
            introEmtTv.setVisibility(View.VISIBLE);
            if (introWebView != null && introWebView.getParent() != null) {
                ((ViewGroup) introWebView.getParent()).removeView(introWebView);
            }
            return;
        }
        String style = "style=\" width:100%;\"";
        String breakStyle = "word-break:break-all;";
        String colorStyle = "color:#000000;";
        content = content.replaceAll("img src=\"//", "img src=\\\"https://");
        content = content.replace("<img ", "<img " + style + " ");
        content = content.replaceAll("<p>", "<p style=\"" + breakStyle + colorStyle + "\">");
        content = content.replaceAll("<div>", "<div style=\"" + breakStyle + colorStyle + "\">");
        content = content.replaceAll("<table>", "<table border='1' rules=all style=\"" + colorStyle + "\">");
        content = content.replaceAll("<td>", "<td width=\"36\">");
        content = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "        <meta charset=\"UTF-8\">\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "        <meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">\n" +
                "        <title>Document</title>\n" +
                "</head>\n" +
                "<body>" +
                content + "</body>\n" +
                "</html>";
        loadWebView(content);
    }

    private void loadWebView(String content) {
        if (introWebView == null) {
            introWebView = new PLVSafeWebView(getContext());
            introWebView.clearFocus();
            introWebView.setFocusable(false);
            introWebView.setFocusableInTouchMode(false);
            introWebView.setBackgroundColor(Color.TRANSPARENT);
            introWebView.setHorizontalScrollBarEnabled(false);
            introWebView.setVerticalScrollBarEnabled(false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -1);
            lp.topMargin = ConvertUtils.dp2px(16);
            introWebView.setLayoutParams(lp);
            introBlurLy.addView(introWebView);
            PLVWebViewHelper.initWebView(getContext(), introWebView);
            introWebView.loadData(content, "text/html; charset=UTF-8", null);
        } else {
            if (introWebView.getParent() != null) {
                ((ViewGroup) introWebView.getParent()).removeView(introWebView);
            }
            introBlurLy.addView(introWebView);
            introWebView.loadData(content, "text/html; charset=UTF-8", null);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="view事件监听器">
    private OnViewActionListener onViewActionListener;

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public interface OnViewActionListener {
        void onViewCreated();
    }
    // </editor-fold>
}
