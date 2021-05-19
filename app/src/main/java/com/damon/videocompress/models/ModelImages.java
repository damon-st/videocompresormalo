package com.damon.videocompress.models;

import android.net.Uri;

public class ModelImages {

    long id;
    Uri data;
    String title, duration,display;

    public ModelImages(long id, Uri data, String title, String duration, String display) {
        this.id = id;
        this.data = data;
        this.title = title;
        this.duration = duration;
        this.display = display;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Uri getData() {
        return data;
    }

    public void setData(Uri data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }
}
