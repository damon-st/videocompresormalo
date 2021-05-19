package com.damon.videocompress.utils.videoeditor;

import android.media.MediaCodec;
import android.os.Build;

import java.nio.ByteBuffer;

class MediaCodecBufferCompatWrapper {
    private final MediaCodec mediaCodec;
    private final ByteBuffer[] inputBuffers;
    private final ByteBuffer[] putputBuffers;

    MediaCodecBufferCompatWrapper(MediaCodec mediaCodec) {
        this.mediaCodec = mediaCodec;

        if (Build.VERSION.SDK_INT < 21) {
            inputBuffers = mediaCodec.getInputBuffers();
            putputBuffers = mediaCodec.getOutputBuffers();
        } else {
            inputBuffers = putputBuffers = null;
        }
    }

    ByteBuffer getInputBuffer(final int index) {
        if (Build.VERSION.SDK_INT >= 21) {
            return mediaCodec.getInputBuffer(index);
        }
        return inputBuffers[index];
    }

    ByteBuffer getOutputBuffer(final int index) {
        if (Build.VERSION.SDK_INT >= 21) {
            return mediaCodec.getOutputBuffer(index);
        }
        return putputBuffers[index];
    }

}