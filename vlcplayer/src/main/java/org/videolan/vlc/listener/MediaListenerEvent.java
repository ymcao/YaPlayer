package org.videolan.vlc.listener;

public interface MediaListenerEvent {

    void eventBuffing(float buffing, boolean show);

    void eventPlayInit(boolean openClose);

    void eventStop(boolean isPlayError);

    void eventError(int error, boolean show);

    void eventPlay(boolean isPlaying);
}
