package com.yplayer.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.vlcplayer.VlcControllerView;
import com.vlcplayer.VlcMediaController;
import com.vlcplayer.VlcVideoView;
import com.yaplayer.R;

import org.videolan.vlc.listener.FullScreenListener;


public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener {

    VlcVideoView videoView;
    VlcMediaController controller;
    String path = "http://baobab.wdjcdn.com/1456317490140jiyiyuetai_x264.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VlcControllerView controllerView= (VlcControllerView) findViewById(R.id.controllerView);
        videoView = (VlcVideoView) findViewById(R.id.videoView);
        controller=new VlcMediaController(controllerView,videoView);
        videoView.setMediaListenerEvent(controller);
        videoView.startPlay(path);
        controller.setFullScreenListener(new FullScreenListener() {
            @Override
            public void Fullscreen(boolean fullscreen) {
                if (fullscreen) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                } else {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        videoView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.onDestory();
    }

    @Override
    public void onClick(View v) {

    }
}
