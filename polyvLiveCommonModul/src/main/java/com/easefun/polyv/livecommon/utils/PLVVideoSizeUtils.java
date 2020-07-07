package com.easefun.polyv.livecommon.utils;

import android.graphics.Rect;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.easefun.polyv.businesssdk.api.common.player.PolyvBaseVideoView;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayerScreenRatio;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PLVVideoSizeUtils {

    public static void fitVideoRatioAndRect(PolyvBaseVideoView baseVideoView, ViewParent viewParent, Rect rect) {
        int ratio = fitVideoRatio(baseVideoView);
        fitVideoRect(ratio == PolyvPlayerScreenRatio.AR_ASPECT_FILL_PARENT, viewParent, rect);
    }

    //适配播放器的位置大小
    public static void fitVideoRect(boolean isFill, ViewParent viewParent, Rect rect) {
        ViewGroup videoViewParent = (ViewGroup) viewParent;
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) videoViewParent.getLayoutParams();
        if (!isFill && rect != null) {
            if (rect.bottom > rect.top) {
                lp.height = rect.bottom - rect.top;
            }
            if (rect.right > rect.left) {
                lp.width = rect.right - rect.left;
            }
            lp.topMargin = rect.top;
            lp.leftMargin = rect.left;
        } else {
            lp.height = -1;
            lp.width = -1;
            lp.topMargin = 0;
            lp.leftMargin = 0;
        }
        videoViewParent.requestLayout();
        videoViewParent.invalidate();
    }

    //适配播放器的视频比例，如果宽>高，那么使用等比缩放，否则使用等比填充父窗
    public static int fitVideoRatio(PolyvBaseVideoView baseVideoView) {
        int ratio = -1;
        if (baseVideoView != null) {
            int[] videoSize = getVideoWH(baseVideoView);
            if (videoSize[0] >= videoSize[1]) {
                ratio = PolyvPlayerScreenRatio.AR_ASPECT_FIT_PARENT;
                baseVideoView.setAspectRatio(ratio);
            } else {
                ratio = PolyvPlayerScreenRatio.AR_ASPECT_FILL_PARENT;
                baseVideoView.setAspectRatio(ratio);
            }
        }
        return ratio;
    }

    public static int[] getVideoWH(PolyvBaseVideoView baseVideoView) {
        if (baseVideoView != null) {
            IjkMediaPlayer mediaPlayer = baseVideoView.getIjkMediaPlayer();
            if (mediaPlayer != null) {
                return new int[]{mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight()};
            }
        }
        return new int[]{0, 0};
    }
}
