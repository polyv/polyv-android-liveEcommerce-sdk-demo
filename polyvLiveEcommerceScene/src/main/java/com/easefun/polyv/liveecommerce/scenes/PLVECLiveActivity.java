package com.easefun.polyv.liveecommerce.scenes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.View;
import android.widget.FrameLayout;

import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.cloudclass.model.bulletin.PolyvBulletinVO;
import com.easefun.polyv.cloudclass.model.commodity.saas.PolyvCommodityVO;
import com.easefun.polyv.livecommon.config.PLVLiveChannelConfig;
import com.easefun.polyv.livecommon.data.IPLVLiveRoomData;
import com.easefun.polyv.livecommon.data.PLVLiveRoomData;
import com.easefun.polyv.livecommon.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.modules.chatroom.presenter.PLVChatroomPresenter;
import com.easefun.polyv.livecommon.modules.liveroom.IPLVLiveRoomManager;
import com.easefun.polyv.livecommon.modules.liveroom.PLVLiveRoomManager;
import com.easefun.polyv.livecommon.modules.player.live.contract.IPLVLivePlayerContract;
import com.easefun.polyv.livecommon.modules.player.live.model.PLVLivePlayerData;
import com.easefun.polyv.livecommon.modules.player.live.presenter.PLVLivePlayerPresenter;
import com.easefun.polyv.livecommon.ui.window.PLVBaseActivity;
import com.easefun.polyv.livecommon.utils.PLVViewInitUtils;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.player.PLVECLiveVideoLayout;
import com.easefun.polyv.liveecommerce.scenes.fragments.PLVECEmptyFragment;
import com.easefun.polyv.liveecommerce.scenes.fragments.PLVECLiveDetailFragment;
import com.easefun.polyv.liveecommerce.scenes.fragments.PLVECLiveHomeFragment;

import java.util.List;

/**
 * date: 2020-04-29
 * author: hwj
 * description:直播带货场景-直播页面
 */
public class PLVECLiveActivity extends PLVBaseActivity {

    // <editor-fold defaultstate="collapsed" desc="成员变量">
    // 参数 - 定义进入页面所需参数
    private static final String EXTRA_CHANNEL_ID = "channelId";   // 频道号
    private static final String EXTRA_VIEWER_ID = "viewerId";   // 观看者Id
    private static final String EXTRA_VIEWER_NAME = "viewerName";   // 观看者昵称

    // 布局 - 直播页面 - 底层的播放器layout
    private PLVECLiveVideoLayout liveVideoLayout;

    // 布局 - 直播页面 - 上层的 viewpager 布局，包含的三个fragment；默认显示中间的 liveHomeFragment
    private ViewPager viewPager;
    private PLVECLiveDetailFragment liveDetailFragment; // 位于左边的 直播间详情信息页 fragment
    private PLVECLiveHomeFragment liveHomeFragment; // 位于中间的 直播间主页 fragment
    private PLVECEmptyFragment emptyFragment; // 位于右边的 空白页 fragment （该fragment用于清空左右信息，只看底层的视频）

    /**
     * MVP模式 - 说明
     *     V 是页面UI，代码公开在 polyvLiveEcommerceSence module 中，可按需修改；
     *     P 是底层处理，代码封装在 polyvLiveComoonModule 中，一般不需要修改；
     */
    // MVP模式 - 播放器
    private IPLVLivePlayerContract.ILivePlayerPresenter livePlayerPresenter;  // 播放器MVP模式中 的 P
    private IPLVLivePlayerContract.ILivePlayerView livePlayerView; // 播放器MVP模式中 的 V

    // MVP模式 - 聊天室
    private IPLVChatroomContract.IChatroomPresenter chatroomPresenter;   // 聊天室MVP模式中 的P
    private IPLVChatroomContract.IChatroomView chatroomView;   // 聊天室MVP模式中 的V

    // 直播间业务管理器
    private IPLVLiveRoomManager liveRoomManager;
    // 直播间数据，每个业务初始化所需的参数
    private IPLVLiveRoomData liveRoomData;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="启动Activity">
    public static void launchLive(Activity activity, String channelId, String viewerId, String viewerName) {
        Intent intent = new Intent(activity, PLVECLiveActivity.class);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(EXTRA_VIEWER_ID, viewerId);
        intent.putExtra(EXTRA_VIEWER_NAME, viewerName);
        activity.startActivity(intent);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期方法">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plvec_live_page_activity);
        initParams();
        initView();
        initLivePlayerMVP();
        initChatroomMVP();
        initRoomManager();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (liveRoomManager != null) {
            liveRoomManager.destroy();
        }
        if (livePlayerPresenter != null) {
            livePlayerPresenter.destroy();
        }
        if (chatroomPresenter != null) {
            chatroomPresenter.destroy();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化页面参数">
    private void initParams() {
        Intent intent = getIntent();
        String channelId = intent.getStringExtra(EXTRA_CHANNEL_ID);
        String viewerId = intent.getStringExtra(EXTRA_VIEWER_ID);
        String viewerName = intent.getStringExtra(EXTRA_VIEWER_NAME);
        PLVLiveChannelConfig.setupUser(viewerId, viewerName);
        PLVLiveChannelConfig.setupChannelId(channelId);
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
        liveVideoLayout = new PLVECLiveVideoLayout(this);
        FrameLayout videoContainer = findViewById(R.id.plvec_fl_video_container);
        videoContainer.addView(liveVideoLayout, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        // 页面上层ViewPage，包含三个Fragment - liveDetailFragment、liveHomeFragment、emptyFragment
        viewPager = findViewById(R.id.watch_info_vp);
        liveDetailFragment = new PLVECLiveDetailFragment();
        liveHomeFragment = new PLVECLiveHomeFragment();
        emptyFragment = new PLVECEmptyFragment();
        PLVViewInitUtils.initViewPager(
                getSupportFragmentManager(),
                viewPager,
                1,
                liveDetailFragment,
                liveHomeFragment,
                emptyFragment
        );

        //设置liveHomeFragment的view事件监听器
        liveHomeFragment.setOnViewActionListener(new PLVECLiveHomeFragment.OnViewActionListener() {
            @Override
            public void onChangeMediaPlayModeClick(View view, int mediaPlayMode) {
                livePlayerPresenter.changeMediaPlayMode(mediaPlayMode);
            }

            @Override
            public void onChangeRouteClick(View view, int routePos) {
                livePlayerPresenter.changeRoute(routePos);
            }

            @Override
            public Pair<List<PolyvDefinitionVO>, Integer> onShowDefinitionClick(View view) {
                return new Pair<>(livePlayerPresenter.getBitrateVO(), livePlayerPresenter.getBitratePos());
            }

            @Override
            public void onDefinitionChangeClick(View view, int definitionPos) {
                livePlayerPresenter.changeBitRate(definitionPos);
            }

            @Override
            public int onGetMediaPlayModeAction() {
                return livePlayerPresenter.getMediaPlayMode();
            }

            @Override
            public int onGetRouteCountAction() {
                return livePlayerPresenter.getRouteCount();
            }

            @Override
            public int onGetRoutePosAction() {
                return livePlayerPresenter.getRoutePos();
            }

            @Override
            public int onGetDefinitionAction() {
                return livePlayerPresenter.getBitratePos();
            }

            @Override
            public void onSetVideoViewRectAction(Rect videoViewRect) {
                liveVideoLayout.setVideoViewRect(videoViewRect);
            }

            @Override
            public void onGetCommodityVOAction(final int rank) {
                liveRoomManager.getCommodityInfo(rank, null);
            }

            @Override
            public void onViewCreated() {
                initDataObserverToLiveHomeFragment();
            }
        });
        //设置liveDetailFragment的view事件监听器
        liveDetailFragment.setOnViewActionListener(new PLVECLiveDetailFragment.OnViewActionListener() {
            @Override
            public void onViewCreated() {
                initDataObserverToLiveDetailFragment();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - MVP模式 初始化">
    private void initLivePlayerMVP() {
        // 获得 V
        livePlayerView = liveVideoLayout.getLivePlayerView();

        // 获得 P
        livePlayerPresenter = new PLVLivePlayerPresenter(liveRoomData);

        // 把 P 和 V 绑定
        livePlayerPresenter.registerView(livePlayerView);

        // 进行 P 的初始化操作
        livePlayerPresenter.init();
        livePlayerPresenter.startPlay();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - MVP模式 初始化">
    private void initChatroomMVP() {
        // 获得 聊天室MVP模式 中的 V
        chatroomView = liveHomeFragment.getChatroomView();

        // 获得 聊天室MVP模式 中的 P
        chatroomPresenter = new PLVChatroomPresenter(liveRoomData);

        // 把 聊天室MVP模式 中的 P 和 V 绑定
        chatroomPresenter.registerView(chatroomView);
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
    private void initDataObserverToLiveDetailFragment() {
        //当前页面 监听 直播间数据对象中的直播详情数据变化
        liveRoomData.getClassDetailVO().observe(this, new Observer<PolyvLiveClassDetailVO>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(@Nullable PolyvLiveClassDetailVO liveClassDetailVO) {
                liveDetailFragment.setClassDetailVO(liveClassDetailVO);
            }
        });
        //当前页面 监听 聊天室数据对象中的公告数据变化
        chatroomPresenter.getData().getBulletinVO().observe(this, new Observer<PolyvBulletinVO>() {
            @Override
            public void onChanged(@Nullable PolyvBulletinVO bulletinVO) {
                liveDetailFragment.setBulletinVO(bulletinVO);
            }
        });
    }

    private void initDataObserverToLiveHomeFragment() {
        //当前页面 监听 直播间数据对象中的直播详情数据变化
        liveRoomData.getClassDetailVO().observe(this, new Observer<PolyvLiveClassDetailVO>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(@Nullable PolyvLiveClassDetailVO liveClassDetailVO) {
                liveHomeFragment.setClassDetailVO(liveClassDetailVO);
            }
        });

        //当前页面 监听 直播间数据对象中的直播商品数据变化
        liveRoomData.getCommodityVO().observe(this, new Observer<PolyvCommodityVO>() {
            @Override
            public void onChanged(@Nullable PolyvCommodityVO polyvCommodityVO) {
                liveHomeFragment.setCommodityVO(liveRoomManager.getCommodityRank(), polyvCommodityVO);
            }
        });

        //当前页面 监听 播放器数据对象中的播放状态变化
        livePlayerPresenter.getData().getPlayerState().observe(this, new Observer<PLVLivePlayerData.PlayerState>() {
            @Override
            public void onChanged(@Nullable PLVLivePlayerData.PlayerState state) {
                liveHomeFragment.setPlayerState(state);
            }
        });
    }
    // </editor-fold>
}
