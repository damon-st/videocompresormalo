package com.damon.videocompress.utils;

import com.damon.videocompress.models.VideoModel;

public class MessageEvent {

    private EventType type;

    private VideoModel model;

    public MessageEvent(EventType type, VideoModel model) {
        this.type = type;
        this.model = model;
    }

    public EventType getType() {
        return type;
    }

    public VideoModel getModel() {
        return model;
    }
}
