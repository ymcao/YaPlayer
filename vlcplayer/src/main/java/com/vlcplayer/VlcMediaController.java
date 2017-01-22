package com.vlcplayer;

import android.view.View;

import com.log.Logger;

import org.videolan.vlc.listener.FullScreenListener;
import org.videolan.vlc.listener.MediaListenerEvent;
import org.videolan.vlc.listener.MediaPlayerControl;

/**
 * Author：caoyamin
 * Time: 2017/1/22
 */

public class VlcMediaController implements VlcControllerView.MediaPlayerControl, MediaListenerEvent {
    private MediaPlayerControl mediaPlayer;
    private VlcControllerView controller;
    private FullScreenListener fullScreenListener;

    public VlcMediaController(final VlcControllerView controller,
                              final VlcVideoView mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        this.controller = controller;

        controller.setControllerView(mediaPlayer);
        controller.setMediaPlayerListener(this);

        mediaPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!controller.isShowing())
                    controller.show();
                else
                    controller.hide();
            }
        });
    }


    public void setFullScreenListener(FullScreenListener listener) {
        this.fullScreenListener = listener;
    }


    @Override
    public void start() {
        mediaPlayer.start();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public int getDuration() {
        return (int) mediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return (int) mediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        mediaPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return mediaPlayer.getBufferPercentage();
    }

    @Override
    public boolean canPause() {
        return mediaPlayer.isPrepare();
    }


    @Override
    public void toggleFullScreen(boolean isFullscreen) {

        if (isFullscreen) {
            fullScreenListener.Fullscreen(isFullscreen);
        } else {
            fullScreenListener.Fullscreen(isFullscreen);
        }
    }

    @Override
    public void eventBuffing(float buffing, boolean show) {
        if (show)
            Logger.w("buffing=" + buffing);
        else
            Logger.w("播放中");
    }

    @Override
    public void eventPlayInit(boolean openClose) {
        Logger.w("加载中");
    }

    @Override
    public void eventStop(boolean isPlayError) {
        Logger.w("Stop" + (isPlayError ? "  播放已停止   有错误" : ""));
    }

    @Override
    public void eventError(int error, boolean show) {
        Logger.w("地址 出错了 error=" + error);
    }

    @Override
    public void eventPlay(boolean isPlaying) {
        if (isPlaying)
            controller.show();
    }

}