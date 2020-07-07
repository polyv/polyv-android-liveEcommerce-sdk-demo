package com.easefun.polyv.livecommon.modules.player;

import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

import com.easefun.polyv.businesssdk.api.common.meidaControl.IPolyvMediaController;
import com.easefun.polyv.businesssdk.model.video.PolyvBitrateVO;
import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.businesssdk.model.video.PolyvLiveLinesVO;
import com.easefun.polyv.cloudclass.video.PolyvCloudClassVideoView;

import java.util.List;

/**
 * date: 2020-04-29
 * author: hwj
 * description:
 */
public class PLVEmptyMediaController implements IPolyvMediaController<PolyvCloudClassVideoView> {
    @Override
    public void release() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void setViewBitRate(String vid, int bitRate) {

    }

    @Override
    public PolyvCloudClassVideoView getMediaPlayer() {
        return null;
    }

    @Override
    public void onPrepared(PolyvCloudClassVideoView mp) {

    }

    @Override
    public void setMediaPlayer(PolyvCloudClassVideoView player) {

    }


    @Override
    public void onLongBuffering(String tip) {

    }

    @Override
    public void updateControllerWithCloseSubView() {

    }

    @Override
    public void changeToLandscape() {

    }

    @Override
    public void changeToPortrait() {

    }

    @Override
    public void initialConfig(ViewGroup container) {

    }

    @Override
    public void initialBitrate(PolyvBitrateVO bitrateVO) {
        //是否支持码率选择
        boolean isSupportBitrate = !bitrateVO.getDefinitions().isEmpty();

        //默认码率
        String defaultBitrate = bitrateVO.getDefaultDefinition();
        //码率列表
        List<PolyvDefinitionVO> bitrateList = bitrateVO.getDefinitions();

        // [0,bitrateList.size()]
//        getMediaPlayer().changeBitRate()
    }

    @Override
    public void initialLines(List<PolyvLiveLinesVO> lines) {
        // [0,lines.size()]
//        getMediaPlayer().changeLines();
    }

    @Override
    public void hide() {

    }

    @Override
    public boolean isShowing() {
        return false;
    }

    @Override
    public void setAnchorView(View view) {

    }

    @Override
    public void setEnabled(boolean enabled) {

    }

    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {

    }

    @Override
    public void show(int timeout) {

    }

    @Override
    public void show() {

    }

    @Override
    public void showOnce(View view) {

    }
}
