package com.easefun.polyv.liveecommerce.scenes.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.foundationsdk.utils.PolyvTimeUtils;
import com.easefun.polyv.livecommon.modules.player.playback.model.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.modules.player.playback.model.PLVPlaybackPlayerData;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECMorePopupView;
import com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECWatchInfoView;

/**
 * 回放首页：播放控制、进度条、更多
 */
public class PLVECPalybackHomeFragment extends PLVBaseFragment implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="成员变量">
    //观看信息布局
    private PLVECWatchInfoView watchInfoLy;
    //播放控制
    private ImageView playControlIv;
    private TextView playTimeTv;
    private SeekBar playProgressSb;
    private TextView totalTimeTv;
    private boolean isPlaySbDragging;
    //更多
    private ImageView moreIv;
    private PLVECMorePopupView morePopupView;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期方法">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvec_playback_page_home_fragment, null);
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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        watchInfoLy = findViewById(R.id.watch_info_ly);
        playControlIv = findViewById(R.id.play_control_iv);
        playControlIv.setOnClickListener(this);
        playTimeTv = findViewById(R.id.play_time_tv);
        playProgressSb = findViewById(R.id.play_progress_sb);
        playProgressSb.setOnSeekBarChangeListener(playProgressChangeListener);
        totalTimeTv = findViewById(R.id.total_time_tv);
        moreIv = findViewById(R.id.more_iv);
        moreIv.setOnClickListener(this);

        morePopupView = new PLVECMorePopupView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="观看信息 - 数据处理">
    public void setClassDetailVO(PolyvLiveClassDetailVO liveClassDetailVO) {
        if (liveClassDetailVO == null) {
            return;
        }
        PolyvLiveClassDetailVO.DataBean dataBean = liveClassDetailVO.getData();
        watchInfoLy.updateWatchInfo(dataBean.getCoverImage(), dataBean.getPublisher(), dataBean.getPageView() + 1);
        watchInfoLy.setVisibility(View.VISIBLE);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - 状态变化处理、播放信息变化处理">
    public void setPlaybackPlayState(PLVPlaybackPlayerData.PlayerState state) {
        if (state == PLVPlaybackPlayerData.PlayerState.Prepared) {
            if (onViewActionListener != null) {
                totalTimeTv.setText(PolyvTimeUtils.generateTime(onViewActionListener.onGetDurationAction()));
            }
        }
    }

    public void setPlaybackPlayInfo(PLVPlayInfoVO playInfoVO) {
        if (playInfoVO == null) {
            return;
        }
        int position = playInfoVO.getPosition();
        int totalTime = playInfoVO.getTotalTime();
        int bufPercent = playInfoVO.getBufPercent();
        boolean isPlaying = playInfoVO.isPlaying();
        //在拖动进度条的时候，这里不更新
        if (!isPlaySbDragging) {
            playTimeTv.setText(PolyvTimeUtils.generateTime(position));
            if (totalTime > 0) {
                playProgressSb.setProgress((int) ((long) playProgressSb.getMax() * position / totalTime));
            } else {
                playProgressSb.setProgress(0);
            }
        }
        playProgressSb.setSecondaryProgress(playProgressSb.getMax() * bufPercent / 100);
        if (isPlaying) {
            playControlIv.setSelected(true);
        } else {
            playControlIv.setSelected(false);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - 进度条拖动事件处理">
    private SeekBar.OnSeekBarChangeListener playProgressChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) {
                return;
            }
            isPlaySbDragging = true;
            if (onViewActionListener != null) {
                int seekPosition = (int) ((long) onViewActionListener.onGetDurationAction() * progress / seekBar.getMax());
                playTimeTv.setText(PolyvTimeUtils.generateTime(seekPosition));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            seekBar.setSelected(true);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            seekBar.setSelected(false);
            isPlaySbDragging = false;
            if (onViewActionListener != null) {
                onViewActionListener.onSeekToAction(seekBar.getProgress(), seekBar.getMax());
            }
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.play_control_iv) {
            if (onViewActionListener != null) {
                v.setSelected(onViewActionListener.onPauseOrResumeClick(v));
            }
        } else if (id == R.id.more_iv) {
            float currentSpeed = onViewActionListener == null ? 1 : onViewActionListener.onGetSpeedAction();
            morePopupView.showPlaybackMoreLayout(v, currentSpeed, new PLVECMorePopupView.OnPlaybackMoreClickListener() {
                @Override
                public void onChangeSpeedClick(View view, float speed) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onChangeSpeedClick(view, speed);
                    }
                }
            });
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="view事件监听器">
    private OnViewActionListener onViewActionListener;

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public interface OnViewActionListener {
        //暂停/播放，true: doResume, false: doPause
        boolean onPauseOrResumeClick(View view);

        //切换倍速
        void onChangeSpeedClick(View view, float speed);

        //跳转播放
        void onSeekToAction(int progress, int max);

        //获取总视频时长
        int onGetDurationAction();

        //获取倍速
        float onGetSpeedAction();

        void onViewCreated();
    }
    // </editor-fold>
}
