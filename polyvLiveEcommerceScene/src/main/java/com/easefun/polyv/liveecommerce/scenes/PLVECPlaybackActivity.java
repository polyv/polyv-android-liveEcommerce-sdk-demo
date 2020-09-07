package com.easefun.polyv.liveecommerce.scenes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.FrameLayout;

import com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livecommon.config.PLVLiveChannelConfig;
import com.easefun.polyv.livecommon.data.IPLVLiveRoomData;
import com.easefun.polyv.livecommon.data.PLVLiveRoomData;
import com.easefun.polyv.livecommon.modules.liveroom.IPLVLiveRoomManager;
import com.easefun.polyv.livecommon.modules.liveroom.PLVLiveRoomManager;
import com.easefun.polyv.livecommon.modules.player.playback.contract.IPLVPlaybackPlayerContract;
import com.easefun.polyv.livecommon.modules.player.playback.model.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.modules.player.playback.model.PLVPlaybackPlayerData;
import com.easefun.polyv.livecommon.modules.player.playback.prsenter.PLVPlaybackPlayerPresenter;
import com.easefun.polyv.livecommon.ui.window.PLVBaseActivity;
import com.easefun.polyv.livecommon.utils.PLVViewInitUtils;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.player.PLVECPlaybackVideoLayout;
import com.easefun.polyv.liveecommerce.scenes.fragments.PLVECEmptyFragment;
import com.easefun.polyv.liveecommerce.scenes.fragments.PLVECPalybackHomeFragment;

/**
 * date: 2020-04-29
 * author: hwj
 * description:直播带货场景-回放页面
 */
public class PLVECPlaybackActivity extends PLVBaseActivity {

    // <editor-fold defaultstate="collapsed" desc="成员变量">
    // 参数 - 定义进入页面所需参数
    private static final String EXTRA_CHANNEL_ID = "channelId";//频道号
    private static final String EXTRA_VID = "vid";//视频Id
    private static final String EXTRA_VIEWER_ID = "viewerId";//观看者Id
    private static final String EXTRA_VIEWER_NAME = "viewerName";//观看者昵称

    // 布局 - 回放页面 - 底层的播放器layout
    private PLVECPlaybackVideoLayout playbackVideoLayout;

    // 布局 - 直播页面 - 上层的 viewpager 布局，包含的两个fragment；默认显示第一个的 playbackHomeFragment
    private ViewPager viewPager;
    private PLVECPalybackHomeFragment playbackHomeFragment;
    private PLVECEmptyFragment emptyFragment;

    /**
     * MVP模式 - 说明
     * V 是页面UI，代码公开在 polyvLiveEcommerceSence module 中，可按需修改；
     * P 是底层处理，代码封装在 polyvLiveComoonModule 中，一般不需要修改；
     */
    // MVP模式 - 播放器
    private IPLVPlaybackPlayerContract.IPlaybackPlayerPresenter playbackPlayerPresenter; // 播放器MVP模式中 的 P
    private IPLVPlaybackPlayerContract.IPlaybackPlayerView playbackPlayerView; // 播放器MVP模式中 的 V

    // 直播间业务管理器
    private IPLVLiveRoomManager liveRoomManager;
    // 直播间数据，每个业务初始化所需的参数
    private IPLVLiveRoomData liveRoomData;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="启动Activity">
    public static void launchPlayback(Activity activity, String channelId, String vid, String viewerId, String viewerName) {
        Intent intent = new Intent(activity, PLVECPlaybackActivity.class);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(EXTRA_VID, vid);
        intent.putExtra(EXTRA_VIEWER_ID, viewerId);
        intent.putExtra(EXTRA_VIEWER_NAME, viewerName);
        activity.startActivity(intent);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期方法">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plvec_playback_page_activity);
        initParams();
        initView();
        initPlaybackPlayerMVP();
        initRoomManager();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playbackPlayerPresenter != null) {
            playbackPlayerPresenter.destroy();
        }
        if (liveRoomManager != null) {
            liveRoomManager.destroy();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化页面参数">
    private void initParams() {
        Intent intent = getIntent();
        String channelId = intent.getStringExtra(EXTRA_CHANNEL_ID);
        String vid = intent.getStringExtra(EXTRA_VID);
        String viewerId = intent.getStringExtra(EXTRA_VIEWER_ID);
        String viewerName = intent.getStringExtra(EXTRA_VIEWER_NAME);
        PLVLiveChannelConfig.setupUser(viewerId, viewerName);
        PLVLiveChannelConfig.setupChannelId(channelId);
        PLVLiveChannelConfig.setupVid(vid);
        // 配置好直播参数后，生成直播间数据实例
        liveRoomData = new PLVLiveRoomData(PLVLiveChannelConfig.generateConfig());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化页面ui">
    private void initView() {
        // 页面关闭按钮
        findViewById(R.id.close_page_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 页面底层播放器
        playbackVideoLayout = new PLVECPlaybackVideoLayout(this);
        FrameLayout videoContainer = findViewById(R.id.plvec_fl_video_container);
        videoContainer.addView(playbackVideoLayout, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        // 页面上层ViewPage，包含两个Fragment - playbackHomeFragment、emptyFragment
        viewPager = findViewById(R.id.watch_info_vp);
        playbackHomeFragment = new PLVECPalybackHomeFragment();
        emptyFragment = new PLVECEmptyFragment();
        PLVViewInitUtils.initViewPager(
                getSupportFragmentManager(),
                viewPager,
                0,
                playbackHomeFragment,
                emptyFragment
        );

        // /设置playbackHomeFragment的view事件监听器
        playbackHomeFragment.setOnViewActionListener(new PLVECPalybackHomeFragment.OnViewActionListener() {
            @Override
            public boolean onPauseOrResumeClick(View view) {
                if (playbackPlayerPresenter.isPlaying()) {
                    playbackPlayerPresenter.pause();
                    return false;
                } else {
                    playbackPlayerPresenter.resume();
                    return true;
                }
            }

            @Override
            public void onChangeSpeedClick(View view, float speed) {
                playbackPlayerPresenter.setSpeed(speed);
            }

            @Override
            public void onSeekToAction(int progress, int max) {
                playbackPlayerPresenter.seekTo(progress, max);
            }

            @Override
            public int onGetDurationAction() {
                return playbackPlayerPresenter.getDuration();
            }

            @Override
            public float onGetSpeedAction() {
                return playbackPlayerPresenter.getSpeed();
            }

            @Override
            public void onViewCreated() {
                initDataObserverToPlaybackHomeFragment();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - MVP模式 初始化">
    private void initPlaybackPlayerMVP() {
        // 获取 V
        playbackPlayerView = playbackVideoLayout.getPlaybackPlayerView();

        // 获取 P
        playbackPlayerPresenter = new PLVPlaybackPlayerPresenter(liveRoomData);

        // 把 P 和 V 绑定
        playbackPlayerPresenter.registerView(playbackPlayerView);

        // 进行 P 的初始化操作
        playbackPlayerPresenter.init();
        playbackPlayerPresenter.startPlay();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="直播业务管理器 - 初始化，上报观看热度、获取直播频道信息">
    private void initRoomManager() {
        // 生成直播业务管理器实例
        liveRoomManager = new PLVLiveRoomManager(liveRoomData);

        // 上报观看热度
        liveRoomManager.increasePageViewer(null);

        // 获取直播详情数据
        liveRoomManager.getLiveDetail(null);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置数据监听">
    private void initDataObserverToPlaybackHomeFragment() {
        //当前页面 监听 直播间数据对象中的直播详情数据变化
        liveRoomData.getClassDetailVO().observe(this, new Observer<PolyvLiveClassDetailVO>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(@Nullable PolyvLiveClassDetailVO liveClassDetailVO) {
                playbackHomeFragment.setClassDetailVO(liveClassDetailVO);
            }
        });

        //当前页面 监听 播放器数据对象中的播放状态变化
        playbackPlayerPresenter.getData().getPlayerState().observe(this, new Observer<PLVPlaybackPlayerData.PlayerState>() {
            @Override
            public void onChanged(@Nullable PLVPlaybackPlayerData.PlayerState state) {
                playbackHomeFragment.setPlaybackPlayState(state);
            }
        });

        //当前页面 监听 播放器数据对象中的播放信息变化
        playbackPlayerPresenter.getData().getPlayInfoVO().observe(this, new Observer<PLVPlayInfoVO>() {
            @Override
            public void onChanged(@Nullable PLVPlayInfoVO playInfoVO) {
                playbackHomeFragment.setPlaybackPlayInfo(playInfoVO);
            }
        });
    }
    // </editor-fold>
}
