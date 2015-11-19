/*
 * Copyright 2013 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.yishun.onemoment.app.ui.view.shoot.video;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PointF;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGLContext;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import co.yishun.onemoment.app.function.Callback;
import co.yishun.onemoment.app.ui.view.shoot.gles.FullFrameRect;

import static co.yishun.onemoment.app.ui.view.shoot.filter.FilterManager.FilterType;
import static co.yishun.onemoment.app.ui.view.shoot.filter.FilterManager.getCameraFilter;

/**
 * Encode a movie from frames rendered from an external texture image.
 * <p>
 * The object wraps an encoder running on a dedicated thread.  The various control messages
 * may be sent from arbitrary threads (typically the app UI thread).  The encoder thread
 * manages both sides of the encoder (feeding and draining); the only external input is
 * the GL texture.
 * <p>
 * The design is complicated slightly by the need to create an EGL context that shares state
 * with a view that gets restarted if (say) the device orientation changes.  When the view
 * in question is a GLSurfaceView, we don't have full control over the EGL context creation
 * on that side, so we have to bend a bit backwards here.
 * <p>
 * To use:
 * <ul>
 * <li>create TextureMovieEncoder object
 * <li>create an EncoderConfig
 * <li>call TextureMovieEncoder#startRecording() with the config
 * <li>call TextureMovieEncoder#setTextureId() with the texture object that receives frames
 * <li>for each frame, after latching it with SurfaceTexture#updateTexImage(),
 * call TextureMovieEncoder#frameAvailable().
 * </ul>
 * <p>
 * TODO: tweak the API (esp. textureId) so it's less awkward for simple use cases.
 */
@TargetApi(18)
public class TextureMovieEncoder implements Runnable {
    protected static final int TIMEOUT_USEC = 10000;    // 10[msec]
    private static final String TAG = "TextureMovieEncoder";

    private static final int MSG_START_RECORDING = 0;
    private static final int MSG_STOP_RECORDING = 1;
    private static final int MSG_SCALE_MVP_MATRIX = 2;
    private static final int MSG_FRAME_AVAILABLE = 3;
    private static final int MSG_SET_TEXTURE_ID = 4;
    private static final int MSG_UPDATE_SHARED_CONTEXT = 6;
    private static final int MSG_UPDATE_FILTER = 7;
    private static final int MSG_QUIT = 8;
    private static final String MIME_TYPE = "video/avc";    // H.264 Advanced Video Coding
    private static final int FRAME_RATE = 30;               // 30fps
    private static final int IFRAME_INTERVAL = 5;           // 5 seconds between I-frames
    private volatile static TextureMovieEncoder sInstance;
    private final Object mReadyFence = new Object();      // guards ready/running
    private WindowSurface mInputWindowSurface;
    private EglCore mEglCore;
    private FullFrameRect mFullScreen;
    private int mTextureId;
    //    private VideoEncoderCore mVideoEncoder;
    private FilterType mCurrentFilterType;
    private volatile EncoderHandler mHandler;
    private boolean mReady;
    private boolean mRunning;
    private Context mContext;
    private EncoderConfig mConfig;
    private Surface mInputSurface;
//    private MediaMuxer mMuxer;
    private WeakReference<MediaMuxerWrapper> mWeakMuxer;
    private MediaCodec mMediaCodec;
    private MediaCodec.BufferInfo mBufferInfo;
    private int mTrackIndex;
    private boolean mMuxerStarted;

    public TextureMovieEncoder(Context applicationContext, EncoderConfig config, MediaMuxerWrapper mediaMuxerWrapper) {
        mContext = applicationContext;
        mConfig = config;
        mWeakMuxer = new WeakReference<>(mediaMuxerWrapper);
    }

    void prepare() throws IOException {
        mBufferInfo = new MediaCodec.BufferInfo();

        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mConfig.mWidth, mConfig.mHeight);

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, mConfig.mBitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
        Log.d(TAG, "format: " + format);

        mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = mMediaCodec.createInputSurface();
        mMediaCodec.start();

//        mMuxer = new MediaMuxer(mConfig.mOutputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        mTrackIndex = -1;
        mMuxerStarted = false;
    }

    /**
     * Tells the video recorder to start recording.  (Call from non-encoder thread.)
     * <p>
     * Creates a new thread, which will create an encoder using the provided configuration.
     * <p>
     * Returns after the recorder thread has started and is ready to accept Messages.  The
     * encoder may not yet be fully configured.
     */
    public void startRecording() {
        Log.d(TAG, "Encoder: startRecording()");
        synchronized (mReadyFence) {
            if (mRunning) {
                Log.w(TAG, "Encoder thread already running");
                return;
            }
            mRunning = true;
            new Thread(this, "TextureMovieEncoder").start();
            while (!mReady) {
                try {
                    mReadyFence.wait();
                } catch (InterruptedException ie) {
                    // ignore
                }
            }
        }

        mHandler.sendMessage(mHandler.obtainMessage(MSG_START_RECORDING));
    }

    public void scaleMVPMatrix(float x, float y) {
        PointF pointF = new PointF(x, y);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SCALE_MVP_MATRIX, pointF));
    }

    /**
     * Tells the video recorder to stop recording.  (Call from non-encoder thread.)
     * <p>
     * Returns immediately; the encoder/muxer may not yet be finished creating the movie.
     * <p>
     * TODO: have the encoder thread invoke a callback on the UI thread just before it shuts down
     * so we can provide reasonable status UI (and let the caller know that movie encoding
     * has completed).
     */
    public void stopRecording(Callback stopListener) {
        Log.i(TAG, "stopRecording: " + stopListener);
        Message message = mHandler.obtainMessage(MSG_STOP_RECORDING);
        message.obj = stopListener;
        mHandler.sendMessage(message);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_QUIT));
        // We don't know when these will actually finish (or even start).  We don't want to
        // delay the UI thread though, so we return immediately.
    }

    /**
     * Returns true if recording has been started.
     */
    public boolean isRecording() {
        synchronized (mReadyFence) {
            return mRunning;
        }
    }

    /**
     * Tells the video recorder to refresh its EGL surface.  (Call from non-encoder thread.)
     */
    public void updateSharedContext(EGLContext sharedContext) {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_SHARED_CONTEXT, sharedContext));
    }

    public void initFilter(FilterType filterType) {
        mCurrentFilterType = filterType;
    }

    public void updateFilter(FilterType filterType) {
        synchronized (mReadyFence) {
            if (!mReady) {
                return;
            }
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_FILTER, filterType));
    }

    /**
     * Tells the video recorder that a new frame is available.  (Call from non-encoder thread.)
     * <p>
     * This function sends a message and returns immediately.  This isn't sufficient -- we
     * don't want the caller to latch a new frame until we're done with this one -- but we
     * can get away with it so long as the input frame rate is reasonable and the encoder
     * thread doesn't stall.
     * <p>
     * TODO: either block here until the texture has been rendered onto the encoder surface,
     * or have a separate "block if still busy" method that the caller can execute immediately
     * before it calls updateTexImage().  The latter is preferred because we don't want to
     * stall the caller while this thread does work.
     */
    public void frameAvailable(float[] texMatrix, long timestamp) {
        synchronized (mReadyFence) {
            if (!mReady) {
                return;
            }
        }
        if (timestamp == 0) {
            // Seeing this after device is toggled off/on with power button.  The
            // first frame back has a zero timestamp.
            // MPEG4Writer thinks this is cause to abort() in native code, so it's very
            // important that we just ignore the frame.
            Log.w(TAG, "HEY: got SurfaceTexture with timestamp of zero");
            return;
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_FRAME_AVAILABLE, (int) (timestamp >> 32),
                (int) timestamp, texMatrix));
    }

    /**
     * Tells the video recorder what texture name to use.  This is the external texture that
     * we're receiving camera previews in.  (Call from non-encoder thread.)
     * <p>
     * TODO: do something less clumsy
     */
    public void setTextureId(int id) {
        synchronized (mReadyFence) {
            if (!mReady) {
                return;
            }
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_TEXTURE_ID, id, 0, null));
    }

    /**
     * Encoder thread entry point.  Establishes Looper/Handler and waits for messages.
     * <p>
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        // Establish a Looper for this thread, and define a Handler for it.
        Looper.prepare();
        synchronized (mReadyFence) {
            mHandler = new EncoderHandler(this);
            mReady = true;
            mReadyFence.notify();
        }
        Looper.loop();

        Log.d(TAG, "Encoder thread exiting");
        synchronized (mReadyFence) {
            mReady = mRunning = false;
            mHandler = null;
        }
    }

    /**
     * Starts recording.
     */
    private void handleStartRecording() {
        Log.d(TAG, "handleStartRecording " + mConfig);

        mEglCore = new EglCore(mConfig.mEglContext, EglCore.FLAG_RECORDABLE);
        mInputWindowSurface = new WindowSurface(mEglCore, mInputSurface, true);
        mInputWindowSurface.makeCurrent();

        mFullScreen = new FullFrameRect(getCameraFilter(mCurrentFilterType, mContext));
    }

    /**
     * Handles notification of an available frame.
     * <p>
     * The texture is rendered onto the encoder's input surface, along with a moving
     * box (just because we can).
     * <p>
     *
     * @param transform      The texture transform, from SurfaceTexture.
     * @param timestampNanos The frame's timestamp, from SurfaceTexture.
     */
    private void handleFrameAvailable(float[] transform, long timestampNanos) {
        //if (VERBOSE) Log.d(TAG, "handleFrameAvailable tr=" + transform);
        drainEncoder(false);
        mFullScreen.drawFrame(mTextureId, transform);
        mInputWindowSurface.setPresentationTime(timestampNanos);
        mInputWindowSurface.swapBuffers();
    }

    /**
     * Handles a request to stop encoding.
     */
    private void handleStopRecording(@Nullable Callback callback) {
        Log.d(TAG, "handleStopRecording");
        drainEncoder(true);
        if (callback != null) {
            callback.call();
        }
        releaseEncoder();
    }

    private void handleSaleMVPMatrix(PointF pointF) {
        mFullScreen.scaleMVPMatrix(pointF.x, pointF.y);
    }

    /**
     * Sets the texture name that SurfaceTexture will use when frames are received.
     */
    private void handleSetTexture(int id) {
        //Log.d(TAG, "handleSetTexture " + id);
        mTextureId = id;
    }

    /**
     * Tears down the EGL surface and context we've been using to feed the MediaCodec input
     * surface, and replaces it with a new one that shares with the new context.
     * <p>
     * This is useful if the old context we were sharing with went away (maybe a GLSurfaceView
     * that got torn down) and we need to hook up with the new one.
     */
    private void handleUpdateSharedContext(EGLContext newSharedContext) {
        Log.d(TAG, "handleUpdatedSharedContext " + newSharedContext);

        // Release the EGLSurface and EGLContext.
        mInputWindowSurface.releaseEglSurface();
        mFullScreen.release(false);
        mEglCore.release();

        // Create a new EGLContext and recreate the window surface.
        mEglCore = new EglCore(newSharedContext, EglCore.FLAG_RECORDABLE);
        mInputWindowSurface.recreate(mEglCore);
        mInputWindowSurface.makeCurrent();

        // Create new programs and such for the new context.
        mFullScreen = new FullFrameRect(getCameraFilter(mCurrentFilterType, mContext));
    }

    private void handleUpdateFilter(FilterType filterType) {
        if (mFullScreen != null && filterType != mCurrentFilterType) {
            mFullScreen.changeProgram(getCameraFilter(filterType, mContext));
            mCurrentFilterType = filterType;
        }
    }

    private void releaseEncoder() {
        Log.d(TAG, "releasing encoder objects");
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
//        if (mMuxer != null) {
//            // TODO: stop() throws an exception if you haven't fed it any data.  Keep track
//            //       of frames submitted, and don't call stop() if we haven't written anything.
//            mMuxer.stop();
//            mMuxer.release();
//            mMuxer = null;
//        }
        if (mMuxerStarted) {
            final MediaMuxerWrapper muxer = mWeakMuxer.get();
            if (muxer != null) {
                Log.d(TAG, "not null try to release");
                try {
                    muxer.stop();
                } catch (final Exception e) {
                    Log.e(TAG, "failed stopping muxer", e);
                }
            }
        }
        if (mInputWindowSurface != null) {
            mInputWindowSurface.release();
            mInputWindowSurface = null;
        }
        if (mFullScreen != null) {
            mFullScreen.release(false);
            mFullScreen = null;
        }
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }
    }

    public void drainEncoder(boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;
        Log.d(TAG, "drainEncoder(" + endOfStream + ")");
        final MediaMuxerWrapper muxer = mWeakMuxer.get();
        if (endOfStream) {
            Log.d(TAG, "sending EOS to encoder");
            mMediaCodec.signalEndOfInputStream();
        }

        ByteBuffer[] encoderOutputBuffers = mMediaCodec.getOutputBuffers();
        LOOP:
        while (true) {
            int encoderStatus = mMediaCodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    break;      // out of while
                } else {
                    Log.d(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = mMediaCodec.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = mMediaCodec.getOutputFormat();
                Log.d(TAG, "encoder output format changed: " + newFormat);

                // now that we have the Magic Goodies, start the muxer

//                mTrackIndex = mMuxer.addTrack(newFormat);
//                mMuxer.start();
                mTrackIndex = muxer.addTrack(newFormat);
                if (!muxer.start()) {
                    // we should wait until muxer is ready
                    synchronized (muxer) {
                        while (!muxer.isStarted())
                            try {
                                muxer.wait(100);
                            } catch (final InterruptedException e) {
                                break LOOP;
                            }
                    }
                }
                mMuxerStarted = true;
            } else if (encoderStatus < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
                // let's ignore it
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }

                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);

//                    mMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
                    mWeakMuxer.get().writeSampleData(mTrackIndex, encodedData, mBufferInfo);
                    Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer, ts=" +
                            mBufferInfo.presentationTimeUs);
                }

                mMediaCodec.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.w(TAG, "reached end of stream unexpectedly");
                    } else {
                        Log.d(TAG, "end of stream reached");
                    }
                    break;      // out of while
                }
            }
        }
    }

//    protected void drain(boolean endOfStream) {
//        if (mMediaCodec == null) return;
//        ByteBuffer[] encoderOutputBuffers = mMediaCodec.getOutputBuffers();
//        int encoderStatus, count = 0;
//        if (mMuxer == null) {
////        	throw new NullPointerException("muxer is unexpectedly null");
//            Log.w(TAG, "muxer is unexpectedly null");
//            return;
//        }
//        LOOP:
//        while (true) {
//            // get encoded data with maximum timeout duration of TIMEOUT_USEC(=10[msec])
//            encoderStatus = mMediaCodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
//            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
//                // wait 5 counts(=TIMEOUT_USEC x 5 = 50msec) until data/EOS come
//                if (!endOfStream) {
//                    if (++count > 5)
//                        break LOOP;        // out of while
//                }
//            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//                Log.v(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
//                // this shoud not come when encoding
//                encoderOutputBuffers = mMediaCodec.getOutputBuffers();
//            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                Log.v(TAG, "INFO_OUTPUT_FORMAT_CHANGED");
//                // this status indicate the output format of codec is changed
//                // this should come only once before actual encoded data
//                // but this status never come on Android4.3 or less
//                // and in that case, you should treat when MediaCodec.BUFFER_FLAG_CODEC_CONFIG come.
//                if (mMuxerStarted) {    // second time request is error
//                    throw new RuntimeException("format changed twice");
//                }
//                // get output format from codec and pass them to muxer
//                // getOutputFormat should be called after INFO_OUTPUT_FORMAT_CHANGED otherwise crash.
//                final MediaFormat format = mMediaCodec.getOutputFormat(); // API >= 16
//                mTrackIndex = mMuxer.addTrack(format);
//                mMuxer.start();
//                mMuxerStarted = true;
//            } else if (encoderStatus < 0) {
//                // unexpected status
//                Log.w(TAG, "drain:unexpected result from encoder#dequeueOutputBuffer: " + encoderStatus);
//            } else {
//                final ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
//                if (encodedData == null) {
//                    // this never should come...may be a MediaCodec internal error
//                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
//                }
//                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
//                    // You shoud set output format to muxer here when you target Android4.3 or less
//                    // but MediaCodec#getOutputFormat can not call here(because INFO_OUTPUT_FORMAT_CHANGED don't come yet)
//                    // therefor we should expand and prepare output format from buffer data.
//                    // This sample is for API>=18(>=Android 4.3), just ignore this flag here
//                    Log.d(TAG, "drain:BUFFER_FLAG_CODEC_CONFIG");
//                    mBufferInfo.size = 0;
//                }
//
//                if (mBufferInfo.size != 0) {
//                    // encoded data is ready, clear waiting counter
//                    count = 0;
//                    if (!mMuxerStarted) {
//                        // muxer is not ready...this will prrograming failure.
//                        throw new RuntimeException("drain:muxer hasn't started");
//                    }
//                    // write encoded data to muxer(need to adjust presentationTimeUs.
////                    mBufferInfo.presentationTimeUs = getPTSUs();
//                    mMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
////                    prevOutputPTSUs = mBufferInfo.presentationTimeUs;
//                    Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer, ts=" +
//                            mBufferInfo.presentationTimeUs);
//                }
//                // return buffer to encoder
//                mMediaCodec.releaseOutputBuffer(encoderStatus, false);
//                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                    // when EOS come.
//                    break;      // out of while
//                }
//            }
//        }
//    }


    /**
     * Handles encoder state change requests.  The handler is created on the encoder thread.
     */
    private static class EncoderHandler extends Handler {
        private WeakReference<TextureMovieEncoder> mWeakEncoder;

        public EncoderHandler(TextureMovieEncoder encoder) {
            mWeakEncoder = new WeakReference<>(encoder);
        }

        @Override  // runs on encoder thread
        public void handleMessage(Message inputMessage) {
            int what = inputMessage.what;
            Object obj = inputMessage.obj;

            TextureMovieEncoder encoder = mWeakEncoder.get();
            if (encoder == null) {
                Log.w(TAG, "EncoderHandler.handleMessage: encoder is null");
                return;
            }

            switch (what) {
                case MSG_START_RECORDING:
                    Log.i(TAG, "MSG_START_RECORDING");
                    encoder.handleStartRecording();
                    break;
                case MSG_STOP_RECORDING:
                    Log.i(TAG, "MSG_STOP_RECORDING");
                    encoder.handleStopRecording((co.yishun.onemoment.app.function.Callback) obj);
                    break;

                case MSG_SCALE_MVP_MATRIX:
                    Log.i(TAG, "MSG_SCALE_MVP_MATRIX");
                    encoder.handleSaleMVPMatrix((PointF) obj);
                    break;

                case MSG_FRAME_AVAILABLE:
                    Log.i(TAG, "MSG_FRAME_AVAILABLE");
                    long timestamp =
                            (((long) inputMessage.arg1) << 32) | (((long) inputMessage.arg2)
                                    & 0xffffffffL);
                    encoder.handleFrameAvailable((float[]) obj, timestamp);
                    break;
                case MSG_SET_TEXTURE_ID:
                    Log.i(TAG, "MSG_SET_TEXTURE_ID");
                    encoder.handleSetTexture(inputMessage.arg1);
                    break;
                case MSG_UPDATE_SHARED_CONTEXT:
                    Log.i(TAG, "MSG_UPDATE_SHARED_CONTEXT");
                    encoder.handleUpdateSharedContext((EGLContext) inputMessage.obj);
                    break;

                case MSG_UPDATE_FILTER:
                    Log.i(TAG, "MSG_UPDATE_FILTER");
                    encoder.handleUpdateFilter((FilterType) inputMessage.obj);
                    break;

                case MSG_QUIT:
                    Log.i(TAG, "MSG_QUIT");
                    Looper looper = Looper.myLooper();
                    if (looper != null) {
                        looper.quit();
                    }
                    break;
                default:
                    throw new RuntimeException("Unhandled msg what=" + what);
            }
        }
    }

}
