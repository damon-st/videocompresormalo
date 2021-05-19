package com.damon.videocompress.models;

import android.net.Uri;

public class ModelVideo {

    long id;
    Uri data;
    String title, duration,duracion_video,resolucion;

    public ModelVideo(long id, Uri data, String title, String duration, String duracion_video, String resolucion) {
        this.id = id;
        this.data = data;
        this.title = title;
        this.duration = duration;
        this.duracion_video = duracion_video;
        this.resolucion = resolucion;
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

    public String getDuracion_video() {
        return duracion_video;
    }

    public void setDuracion_video(String duracion_video) {
        this.duracion_video = duracion_video;
    }

    public String getResolucion() {
        return resolucion;
    }

    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }
}
