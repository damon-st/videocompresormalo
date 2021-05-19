package com.damon.videocompress.interfaces;

public interface WaveformListener {
    public void waveformTouchStart(float x);
    public void waveformTouchMove(float x);
    public void waveformTouchEnd();
    public void waveformFling(float x);
    public void waveformDraw();
    public void waveformZoomIn();
    public void waveformZoomOut();
}
