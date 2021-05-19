package com.damon.videocompress.interfaces;

import com.damon.videocompress.view.MarkerView;

public interface MarkerListener {
    public void markerTouchStart(MarkerView marker, float pos);
    public void markerTouchMove(MarkerView marker, float pos);
    public void markerTouchEnd(MarkerView marker);
    public void markerFocus(MarkerView marker);
    public void markerLeft(MarkerView marker, int velocity);
    public void markerRight(MarkerView marker, int velocity);
    public void markerEnter(MarkerView marker);
    public void markerKeyUp();
    public void markerDraw();
}
