package org.videolan.vlc.listener;

public interface MediaPlayerControl {

    boolean isPrepare();

    boolean canControl();

    void startPlay(String path);

    void start();

    void pause();

    long getDuration();

    long getCurrentPosition();

    void seekTo(long pos);

    boolean isPlaying();

    void setMirror(boolean mirror);

    boolean getMirror();

    /**
     * 第二级缓冲
     *
     * @return
     */
    int getBufferPercentage();

    /**
     * 速度  0.25--4.0
     */
    boolean setPlaybackSpeedMedia(float speed);

    float getPlaybackSpeed();

    /**
     * 是否循环
     */
    void setLoop(boolean isLoop);

    boolean isLoop();
}
