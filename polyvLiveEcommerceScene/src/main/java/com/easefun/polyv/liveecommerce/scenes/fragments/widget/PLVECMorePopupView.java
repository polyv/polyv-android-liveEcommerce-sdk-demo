package com.easefun.polyv.liveecommerce.scenes.fragments.widget;

import android.graphics.drawable.ColorDrawable;
import android.support.annotation.LayoutRes;
import android.util.Pair;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.liveecommerce.R;

import java.util.List;

/**
 * 更多-弹出view
 */
public class PLVECMorePopupView {
    // <editor-fold defaultstate="collapsed" desc="成员变量">
    //直播更多布局
    private PopupWindow liveMorePopupWindow;
    private ImageView playModeIv;
    private TextView playModeTv;
    private ImageView changeRouteIv;
    private ImageView changeDefinitionIv;
    //直播切换线路布局
    private PopupWindow routeChangePopupWindow;
    private ViewGroup changeRouteLy;
    //直播切换清晰度布局
    private PopupWindow definitionPopupWindow;
    private ViewGroup changeDefinitionLy;
    //监听器
    private OnLiveMoreClickListener liveMoreClickListener;

    //回放更多布局
    private PopupWindow playbackMorePopupWindow;
    //回放切换倍速布局
    private ViewGroup changeSpeedLy;
    //监听器
    private OnPlaybackMoreClickListener playbackMoreClickListener;

    //播放状态view的显示状态
    private int playStatusViewVisibility = View.GONE;
    //播放模式view的显示状态
    private int playModeViewVisibility = View.GONE;
    //是否有清晰度信息
    private boolean isHasDefinitionVO;
    //是否有多线路信息
    private boolean isHasRouteInfo;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="直播更多布局">
    public void showLiveMoreLayout(final View v, boolean isCurrentVideoMode, final OnLiveMoreClickListener clickListener) {
        this.liveMoreClickListener = clickListener;
        if (liveMorePopupWindow == null) {
            liveMorePopupWindow = new PopupWindow(v.getContext());
            View view = initPopupWindow(v, R.layout.plvec_live_more_layout, liveMorePopupWindow);

            PLVBlurUtils.initBlurView((PLVBlurView) view.findViewById(R.id.blur_ly));
            playModeIv = view.findViewById(R.id.play_mode_iv);
            playModeTv = view.findViewById(R.id.play_mode_tv);
            changeRouteIv = view.findViewById(R.id.change_route_iv);
            changeDefinitionIv = view.findViewById(R.id.change_definition_iv);
            ((ViewGroup) playModeIv.getParent()).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null) {
                        boolean result = clickListener.onPlayModeClick(playModeIv);
                        if (result) {
                            playModeIv.setSelected(!playModeIv.isSelected());
                            playModeTv.setText(!playModeIv.isSelected() ? "音频模式" : "视频模式");
                            if (playModeIv.isSelected()) {
                                ((ViewGroup) changeDefinitionIv.getParent()).setVisibility(View.GONE);
                            } else if (isHasDefinitionVO) {
                                ((ViewGroup) changeDefinitionIv.getParent()).setVisibility(View.VISIBLE);
                            }
                            hide();
                        }
                    }
                }
            });
            ((ViewGroup) changeRouteIv.getParent()).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null) {
                        int[] route = clickListener.onShowRouteClick(changeRouteIv);
                        showRouteChangeLayout(v, route);
                    }
                }
            });
            ((ViewGroup) changeDefinitionIv.getParent()).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null) {
                        Pair<List<PolyvDefinitionVO>, Integer> listIntegerPair = clickListener.onShowDefinitionClick(changeDefinitionIv);
                        showDefinitionChangeLayout(v, listIntegerPair);
                    }
                }
            });
            if (playStatusViewVisibility == View.VISIBLE) {
                ((ViewGroup) playModeIv.getParent()).setVisibility(playStatusViewVisibility);
                if (isHasRouteInfo) {
                    ((ViewGroup) changeRouteIv.getParent()).setVisibility(playStatusViewVisibility);
                }
                if (playModeViewVisibility == View.VISIBLE && isHasDefinitionVO) {
                    ((ViewGroup) changeDefinitionIv.getParent()).setVisibility(playStatusViewVisibility);
                }
            }
        }
        playModeIv.setSelected(!isCurrentVideoMode);
        playModeTv.setText(!playModeIv.isSelected() ? "音频模式" : "视频模式");
        liveMorePopupWindow.showAtLocation(v, Gravity.NO_GRAVITY, 0, 0);
    }

    public void showRouteChangeLayout(View v, int[] route) {
        if (routeChangePopupWindow == null) {
            routeChangePopupWindow = new PopupWindow(v.getContext());
            View view = initPopupWindow(v, R.layout.plvec_live_more_route_change_layout, routeChangePopupWindow);

            PLVBlurUtils.initBlurView((PLVBlurView) view.findViewById(R.id.blur_ly));
            changeRouteLy = view.findViewById(R.id.change_route_ly);
        }
        updateRouteView(route);
        routeChangePopupWindow.showAtLocation(v, Gravity.NO_GRAVITY, 0, 0);

    }

    public void updateRouteView(final int[] route) {
        isHasRouteInfo = route[0] > 1;
        if (changeRouteIv != null) {
            if (!isHasRouteInfo) {
                ((ViewGroup) changeRouteIv.getParent()).setVisibility(View.GONE);
            } else if (playStatusViewVisibility == View.VISIBLE) {
                ((ViewGroup) changeRouteIv.getParent()).setVisibility(View.VISIBLE);
            }
        }
        if (changeRouteLy != null) {
            if (!isHasRouteInfo) {
                changeRouteLy.setVisibility(View.GONE);
                return;
            } else {
                changeRouteLy.setVisibility(View.VISIBLE);
            }
            for (int i = 0; i < changeRouteLy.getChildCount(); i++) {
                View view = changeRouteLy.getChildAt(i);
                final int finalI = i;
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateRouteView(new int[]{route[0], finalI});
                        if (liveMoreClickListener != null) {
                            liveMoreClickListener.onRouteChangeClick(v, finalI);
                        }
                        hide();
                    }
                });
                view.setSelected(false);
                if (i <= route[0] - 1) {
                    view.setVisibility(View.VISIBLE);
                    if (i == route[1]) {
                        view.setSelected(true);
                    }
                } else {
                    view.setVisibility(View.GONE);
                }
            }
        }
    }

    public void updateDefinitionView(final Pair<List<PolyvDefinitionVO>, Integer> listIntegerPair) {
        isHasDefinitionVO = listIntegerPair.first != null;
        if (changeDefinitionIv != null) {
            if (!isHasDefinitionVO) {
                ((ViewGroup) changeDefinitionIv.getParent()).setVisibility(View.GONE);
            } else if (playModeViewVisibility == View.VISIBLE) {
                ((ViewGroup) changeDefinitionIv.getParent()).setVisibility(View.VISIBLE);
            }
        }
        if (changeDefinitionLy != null) {
            if (!isHasDefinitionVO) {
                changeDefinitionLy.setVisibility(View.GONE);
                return;
            } else {
                changeDefinitionLy.setVisibility(View.VISIBLE);
            }
            for (int i = 0; i < changeDefinitionLy.getChildCount(); i++) {
                View view = changeDefinitionLy.getChildAt(i);
                final int finalI = i;
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateDefinitionView(listIntegerPair);
                        if (liveMoreClickListener != null) {
                            liveMoreClickListener.onDefinitionChangeClick(v, finalI);
                        }
                        hide();
                    }
                });
                view.setSelected(false);
                if (i <= listIntegerPair.first.size() - 1) {
                    if (view instanceof TextView) {
                        ((TextView) view).setText(listIntegerPair.first.get(i).definition);
                    }
                    view.setVisibility(View.VISIBLE);
                    if (i == listIntegerPair.second) {
                        view.setSelected(true);
                    }
                } else {
                    view.setVisibility(View.GONE);
                }
            }
        }
    }

    public void showDefinitionChangeLayout(View v, Pair<List<PolyvDefinitionVO>, Integer> listIntegerPair) {
        if (definitionPopupWindow == null) {
            definitionPopupWindow = new PopupWindow(v.getContext());
            View view = initPopupWindow(v, R.layout.plvec_live_more_definition_change_layout, definitionPopupWindow);

            PLVBlurUtils.initBlurView((PLVBlurView) view.findViewById(R.id.blur_ly));
            changeDefinitionLy = view.findViewById(R.id.change_definition_ly);
        }
        updateDefinitionView(listIntegerPair);
        definitionPopupWindow.showAtLocation(v, Gravity.NO_GRAVITY, 0, 0);
    }

    //暖场/无直播时隐藏切换音视频模式、切换线路相关的按钮
    public void updatePlayStateView(int visibility) {
        this.playStatusViewVisibility = visibility;
        if (liveMorePopupWindow != null) {
            ((ViewGroup) playModeIv.getParent()).setVisibility(visibility);
            if (visibility != View.VISIBLE) {//根据视频是否支持线路切换来显示
                ((ViewGroup) changeRouteIv.getParent()).setVisibility(visibility);
            }
            if (visibility != View.VISIBLE) {//根据视频是否支持多码率切换来显示
                ((ViewGroup) changeDefinitionIv.getParent()).setVisibility(visibility);
            }
        }
    }

    //音频模式时隐藏切换清晰度的按钮
    public void updatePlayModeView(int visibility) {
        this.playModeViewVisibility = visibility;
        if (liveMorePopupWindow != null) {
            if (visibility != View.VISIBLE) {//根据视频是否支持多码率切换来显示
                ((ViewGroup) changeDefinitionIv.getParent()).setVisibility(visibility);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="回放更多布局">
    public void showPlaybackMoreLayout(View v, float currentSpeed, OnPlaybackMoreClickListener clickListener) {
        this.playbackMoreClickListener = clickListener;
        if (playbackMorePopupWindow == null) {
            playbackMorePopupWindow = new PopupWindow(v.getContext());
            View view = initPopupWindow(v, R.layout.plvec_playback_more_speed_change_layout, playbackMorePopupWindow);

            PLVBlurUtils.initBlurView((PLVBlurView) view.findViewById(R.id.blur_ly));
            changeSpeedLy = view.findViewById(R.id.change_speed_ly);
        }
        updateSpeedView(currentSpeed);
        playbackMorePopupWindow.showAtLocation(v, Gravity.NO_GRAVITY, 0, 0);
    }

    public void updateSpeedView(float currentSpeed) {
        if (changeSpeedLy != null) {
            for (int i = 0; i < changeSpeedLy.getChildCount(); i++) {
                View view = changeSpeedLy.getChildAt(i);
                final int finalI = i;
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateSpeedView(speedArray.get(finalI));
                        if (playbackMoreClickListener != null) {
                            playbackMoreClickListener.onChangeSpeedClick(v, speedArray.get(finalI));
                        }
                        hide();
                    }
                });
                view.setSelected(false);
                if (speedArray.valueAt(i).equals(currentSpeed)) {
                    view.setSelected(true);
                }
            }
        }
    }

    private SparseArray<Float> speedArray = new SparseArray<Float>() {
        {
            put(0, 0.5f);
            put(1, 1f);
            put(2, 1.25f);
            put(3, 1.5f);
            put(4, 2.0f);
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="隐藏">
    public void hide() {
        if (liveMorePopupWindow != null) {
            liveMorePopupWindow.dismiss();
        }
        if (routeChangePopupWindow != null) {
            routeChangePopupWindow.dismiss();
        }
        if (definitionPopupWindow != null) {
            definitionPopupWindow.dismiss();
        }
        if (playbackMorePopupWindow != null) {
            playbackMorePopupWindow.dismiss();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化popupWindow配置">
    private View initPopupWindow(View v, @LayoutRes int resource, final PopupWindow popupWindow) {
        View root = LayoutInflater.from(v.getContext()).inflate(resource, null, false);
        popupWindow.setContentView(root);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
        View closeBt = root.findViewById(R.id.close_iv);
        if (closeBt != null) {
            closeBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide();
                }
            });
        }
        return root;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="监听器">
    public interface OnLiveMoreClickListener {
        //播放模式是否切换成功
        boolean onPlayModeClick(View view);

        //[线路总数，当前线路]
        int[] onShowRouteClick(View view);

        //切换线路
        void onRouteChangeClick(View view, int routePos);

        //[清晰度信息，清晰度索引]
        Pair<List<PolyvDefinitionVO>, Integer> onShowDefinitionClick(View view);

        //切换清晰度
        void onDefinitionChangeClick(View view, int definitionPos);
    }

    public interface OnPlaybackMoreClickListener {
        void onChangeSpeedClick(View view, float speed);
    }
    // </editor-fold>
}
