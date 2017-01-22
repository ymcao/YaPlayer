package org.videolan.vlc.listener;

public interface VideoSizeChange {
    void onVideoSizeChanged(int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen);
}
