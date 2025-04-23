/*
 * MIT License
 *
 * Copyright (c) 2016 Knowledge, education for life.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package world.junseo.co.kr.easyscreenrecord.Layout;


import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import world.junseo.co.kr.easyscreenrecord.JWApplication;
import world.junseo.co.kr.easyscreenrecord.R;
import world.junseo.co.kr.easyscreenrecord.Util.Log.JLog;
import world.junseo.co.kr.easyscreenrecord.videoTrimmer.interfaces.OnJWVideoListener;
import world.junseo.co.kr.easyscreenrecord.videoTrimmer.interfaces.OnProgressVideoListener;
import world.junseo.co.kr.easyscreenrecord.videoTrimmer.interfaces.OnRangeSeekBarListener;
import world.junseo.co.kr.easyscreenrecord.videoTrimmer.interfaces.OnTrimVideoListener;
import world.junseo.co.kr.easyscreenrecord.videoTrimmer.utils.BackgroundExecutor;
import world.junseo.co.kr.easyscreenrecord.videoTrimmer.utils.TrimVideoUtils;
import world.junseo.co.kr.easyscreenrecord.videoTrimmer.utils.UiThreadExecutor;
import world.junseo.co.kr.easyscreenrecord.videoTrimmer.view.ProgressBarView;
import world.junseo.co.kr.easyscreenrecord.videoTrimmer.view.RangeSeekBarView;
import world.junseo.co.kr.easyscreenrecord.videoTrimmer.view.Thumb;
import world.junseo.co.kr.easyscreenrecord.videoTrimmer.view.TimeLineView;

public class JWVideoTrimmer extends FrameLayout implements SurfaceHolder.Callback{

    private static final String TAG = "JWVideoTrimmer";
    private static final int MIN_TIME_FRAME = 1000;
    private static final int SHOW_PROGRESS = 2;

    private MediaPlayer mMediaPlayer;

    private SeekBar mHolderTopView;
    private RangeSeekBarView mRangeSeekBarView;
    private RelativeLayout mLinearVideo;
    private View mTimeInfoContainer;
    private VideoView mVideoView;
    private ImageView mPlayView;
    private TextView mTextSize;
    private TextView mTextTimeFrame;
    private TextView mTextTime;
    private TimeLineView mTimeLineView;

    private ProgressBarView mVideoProgressIndicator;
    private Uri mSrc;
    private String mFinalPath;

    private int mMaxDuration;
    private List<OnProgressVideoListener> mListeners;

    private OnTrimVideoListener mOnTrimVideoListener;
    private OnJWVideoListener mOnJWVideoListener;

    private int mDuration = 0;
    private int mTimeVideo = 0;
    private int mStartPosition = 0;
    private int mEndPosition = 0;

    private long mOriginSizeFile;
    private boolean mResetSeekBar = true;
    private final MessageHandler mMessageHandler = new MessageHandler(this);

    public JWVideoTrimmer(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JWVideoTrimmer(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_time_line, this, true);

        mHolderTopView = ((SeekBar) findViewById(R.id.handlerTop));
        mVideoProgressIndicator = ((ProgressBarView) findViewById(R.id.timeVideoView));
        mRangeSeekBarView = ((RangeSeekBarView) findViewById(R.id.timeLineBar));
        mLinearVideo = ((RelativeLayout) findViewById(R.id.layout_surface_view));
        mVideoView = ((VideoView) findViewById(R.id.video_loader));
        mPlayView = ((ImageView) findViewById(R.id.icon_video_play));
        mTimeInfoContainer = findViewById(R.id.timeText);
        mTextSize = ((TextView) findViewById(R.id.textSize));
        mTextTimeFrame = ((TextView) findViewById(R.id.textTimeSelection));
        mTextTime = ((TextView) findViewById(R.id.textTime));
        mTimeLineView = ((TimeLineView) findViewById(R.id.timeLineView));

        setUpListeners();
        setUpMargins();
    }

    private void setUpListeners() {
        mListeners = new ArrayList<>();
        mListeners.add(new OnProgressVideoListener() {
            @Override
            public void updateProgress(int time, int max, float scale) {
                updateVideoProgress(time);
            }
        });
        mListeners.add(mVideoProgressIndicator);

        findViewById(R.id.btCancel)
                .setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onCancelClicked();
                            }
                        }
                );

        findViewById(R.id.btSave)
                .setOnClickListener(
                        new OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD_MR1)
                            @Override
                            public void onClick(View view) {
                                onSaveClicked();
                            }
                        }
                );

        final GestureDetector gestureDetector = new
                GestureDetector(getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        onClickVideoPlayPause();
                        return true;
                    }
                }
        );

        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                if (mOnTrimVideoListener != null)
                    mOnTrimVideoListener.onError("Something went wrong reason : " + what);
                return false;
            }
        });

        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, @NonNull MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        mRangeSeekBarView.addOnRangeSeekBarListener(mVideoProgressIndicator);
        mRangeSeekBarView.addOnRangeSeekBarListener(new OnRangeSeekBarListener() {
            @Override
            public void onCreate(RangeSeekBarView rangeSeekBarView, int index, float value) {
                //JLog.d(TAG, "RangeSeekBarListener onCreate Index : " + index + " value : " + value + "\n");
            }

            @Override
            public void onSeek(RangeSeekBarView rangeSeekBarView, int index, float value) {
                //JLog.d(TAG, "RangeSeekBarListener onSeek Index : " + index + " value : " + value + "\n");
                onSeekThumbs(index, value);
            }

            @Override
            public void onSeekStart(RangeSeekBarView rangeSeekBarView, int index, float value) {
                //JLog.d(TAG, "RangeSeekBarListener onSeekStart Index : " + index + " value : " + value + "\n");
            }

            @Override
            public void onSeekStop(RangeSeekBarView rangeSeekBarView, int index, float value) {
                //JLog.d(TAG, "RangeSeekBarListener onSeekStop Index : " + index + " value : " + value + "\n");
                onStopSeekThumbs();
            }
        });

        mHolderTopView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //JLog.d(TAG, "OnSeekBarChangeListener onProgressChanged progress : " + progress + "\n");
                onPlayerIndicatorSeekChanged(progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //JLog.d(TAG, "OnSeekBarChangeListener onStartTrackingTouch");
                onPlayerIndicatorSeekStart();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //JLog.d(TAG, "OnSeekBarChangeListener onStopTrackingTouch");
                onPlayerIndicatorSeekStop(seekBar);
            }
        });

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //JLog.d(TAG, "비디오 재생 준비 완료");
                mMediaPlayer = mp;

                onVideoPrepared(mp);
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //JLog.d(TAG, "비디오 재생 완료");
                onVideoCompleted();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void setUpMargins() {
        int marge = mRangeSeekBarView.getThumbs().get(0).getWidthBitmap();
        int widthSeek = mHolderTopView.getThumb().getMinimumWidth() / 2;

        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mHolderTopView.getLayoutParams();
        //JLog.d(TAG, "꼭대기 뷰 마진 left + " + (marge - widthSeek) + " right : " + (marge - widthSeek) + "\n");
        lp.setMargins(marge - widthSeek, 0, marge - widthSeek, 0);
        mHolderTopView.setLayoutParams(lp);

        lp = (ConstraintLayout.LayoutParams) mTimeLineView.getLayoutParams();
        //JLog.d(TAG, "사진 뷰 마진 left + " + (marge) + " right : " + (marge) + "\n");
        lp.setMargins(marge, 0, marge, 0);
        mTimeLineView.setLayoutParams(lp);

        lp = (ConstraintLayout.LayoutParams) mVideoProgressIndicator.getLayoutParams();
        //JLog.d(TAG, "프로그래스 선 마진 left + " + (marge) + " right : " + (marge) + "\n");
        lp.setMargins(marge, 0, marge, 0);
        mVideoProgressIndicator.setLayoutParams(lp);
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD_MR1)
    private void onSaveClicked() {
        if (mStartPosition <= 0 && mEndPosition >= mDuration) {
            if (mOnTrimVideoListener != null)
                mOnTrimVideoListener.getResult(mSrc);
        } else {
            mPlayView.setVisibility(View.VISIBLE);
            mVideoView.pause();

            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(getContext(), mSrc);
            long METADATA_KEY_DURATION = Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

            final File file = new File(mSrc.getPath());

            if (mTimeVideo < MIN_TIME_FRAME) {

                if ((METADATA_KEY_DURATION - mEndPosition) > (MIN_TIME_FRAME - mTimeVideo)) {
                    mEndPosition += (MIN_TIME_FRAME - mTimeVideo);
                } else if (mStartPosition > (MIN_TIME_FRAME - mTimeVideo)) {
                    mStartPosition -= (MIN_TIME_FRAME - mTimeVideo);
                }
            }

            //notify that video trimming started
            if (mOnTrimVideoListener != null)
                mOnTrimVideoListener.onTrimStarted();

            BackgroundExecutor.execute(
                    new BackgroundExecutor.Task("", 0L, "") {
                        @Override
                        public void execute() {
                            try {
                                TrimVideoUtils.startTrim(file, getDestinationPath(), mStartPosition, mEndPosition, mOnTrimVideoListener);
                            } catch (final Throwable e) {
                                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                            }
                        }
                    }
            );
        }
    }

    private void onClickVideoPlayPause() {
        if (mVideoView.isPlaying()) {
            mPlayView.setVisibility(View.VISIBLE);
            mMessageHandler.removeMessages(SHOW_PROGRESS);
            mVideoView.pause();
        } else {
            mPlayView.setVisibility(View.GONE);

            if (mResetSeekBar) {
                mResetSeekBar = false;
                mVideoView.seekTo(mStartPosition);

                JLog.d(TAG, "화면이 터치됐다 mStartPosition : " + mStartPosition + "\n");
                JLog.d(TAG, "화면이 터치됐다 mVideoView.getCurrentPosition : " + mVideoView.getCurrentPosition() + "\n");
            }
            mVideoView.start();

            mMessageHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    }

    private void onCancelClicked() {
        mVideoView.stopPlayback();
        if (mOnTrimVideoListener != null) {
            mOnTrimVideoListener.cancelAction();
        }
    }

    private String getDestinationPath() {
        if (mFinalPath == null) {
            //File folder = Environment.getExternalStorageDirectory();
            File folder = JWApplication.getAppContext().getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES);
            mFinalPath = folder.getPath() + File.separator;
            ////JLog.d(TAG, "Using default path " + mFinalPath);
        }
        return mFinalPath;
    }

    private void onPlayerIndicatorSeekChanged(int progress, boolean fromUser) {

        int duration = (int) ((mDuration * progress) / 1000L);

        if (fromUser) {
            if (duration < mStartPosition) {
                setProgressBarPosition(mStartPosition);
                duration = mStartPosition;
            } else if (duration > mEndPosition) {
                setProgressBarPosition(mEndPosition);
                duration = mEndPosition;
            }
            setTimeVideo(duration);
        }
    }

    private void onPlayerIndicatorSeekStart() {
        mMessageHandler.removeMessages(SHOW_PROGRESS);
        mVideoView.pause();
        mPlayView.setVisibility(View.VISIBLE);

        notifyProgressUpdate(false);
    }

    private void onPlayerIndicatorSeekStop(@NonNull SeekBar seekBar) {
        //JLog.d(TAG, "onPlayerIndicatorSeekStop");
        mMessageHandler.removeMessages(SHOW_PROGRESS);
        mVideoView.pause();
        mPlayView.setVisibility(View.VISIBLE);

        int duration = (int) ((mDuration * seekBar.getProgress()) / 1000L);
        mVideoView.seekTo(duration);
        setTimeVideo(duration);
        notifyProgressUpdate(false);
    }

    private void onVideoPrepared(@NonNull MediaPlayer mp) {
        // Adjust the size of the video
        // so it fits on the screen
        int videoWidth = mp.getVideoWidth();
        int videoHeight = mp.getVideoHeight();
        float videoProportion = (float) videoWidth / (float) videoHeight;
        int screenWidth = mLinearVideo.getWidth();
        int screenHeight = mLinearVideo.getHeight();
        float screenProportion = (float) screenWidth / (float) screenHeight;
        ViewGroup.LayoutParams lp = mVideoView.getLayoutParams();

        if (videoProportion > screenProportion) {
            lp.width = screenWidth;
            lp.height = (int) ((float) screenWidth / videoProportion);
        } else {
            lp.width = (int) (videoProportion * (float) screenHeight);
            lp.height = screenHeight;
        }
        mVideoView.setLayoutParams(lp);

        mPlayView.setVisibility(View.VISIBLE);

        mDuration = mVideoView.getDuration();
        setSeekBarPosition();

        setTimeFrames();
        setTimeVideo(0);

        if (mOnJWVideoListener != null) {
            mOnJWVideoListener.onVideoPrepared();
        }
    }

    private void setSeekBarPosition() {

        if (mDuration >= mMaxDuration) {
            mStartPosition = mDuration / 2 - mMaxDuration / 2;
            mEndPosition = mDuration / 2 + mMaxDuration / 2;

            mRangeSeekBarView.setThumbValue(0, (mStartPosition * 100) / mDuration);
            mRangeSeekBarView.setThumbValue(1, (mEndPosition * 100) / mDuration);

        } else {
            mStartPosition = 0;
            mEndPosition = mDuration;
        }

        setProgressBarPosition(mStartPosition);
        mVideoView.seekTo(mStartPosition);

        mTimeVideo = mDuration;
        mRangeSeekBarView.initMaxWidth();
    }

    private void setTimeFrames() {
        mTextTimeFrame.setText(String.format("%s ~ %s", TrimVideoUtils.stringForTime(mStartPosition), TrimVideoUtils.stringForTime(mEndPosition)));
    }

    private void setTimeVideo(int position) {
        mTextTime.setText(String.format("%s", TrimVideoUtils.stringForTime(position)));
    }

    private void onSeekThumbs(int index, float value) {
        switch (index) {
            case Thumb.LEFT: {
                mStartPosition = (int) ((mDuration * value) / 100L);
                mVideoView.seekTo(mStartPosition);
                break;
            }
            case Thumb.RIGHT: {
                mEndPosition = (int) ((mDuration * value) / 100L);
                break;
            }
        }
        setProgressBarPosition(mStartPosition);

        setTimeFrames();
        mTimeVideo = mEndPosition - mStartPosition;
    }

    private void onStopSeekThumbs() {
        mMessageHandler.removeMessages(SHOW_PROGRESS);
        mVideoView.pause();
        mPlayView.setVisibility(View.VISIBLE);
    }

    private void onVideoCompleted() {
        JLog.d(TAG, "onVideoCompleted mStartPosition : " + mStartPosition + "\n");
        mVideoView.seekTo(mStartPosition);
    }

    private void notifyProgressUpdate(boolean all) {
        if (mDuration == 0) return;

        int position = mVideoView.getCurrentPosition();

        if (all) {
            JLog.d(TAG, "(notifyProgressUpdate all) position : " + position + " mDuration : " + mDuration + "\n");

            for (OnProgressVideoListener item : mListeners) {
                item.updateProgress(position, mDuration, ((position * 100) / mDuration));
            }
        } else {
            mListeners.get(1).updateProgress(position, mDuration, ((position * 100) / mDuration));
        }
    }

    private void updateVideoProgress(int time) {
        if (mVideoView == null) {
            return;
        }

        if (time >= mEndPosition) {
            mMessageHandler.removeMessages(SHOW_PROGRESS);
            mVideoView.pause();
            mPlayView.setVisibility(View.VISIBLE);
            mResetSeekBar = true;
            return;
        }

        if (mHolderTopView != null) {
            // use long to avoid overflow
            setProgressBarPosition(time);
        }
        setTimeVideo(time);
    }

    private void setProgressBarPosition(int position) {
        if (mDuration > 0) {
            long pos = 1000L * position / mDuration;
            mHolderTopView.setProgress((int) pos);
        }
    }

    /**
     * Set video information visibility.
     * For now this is for debugging
     *
     * @param visible whether or not the videoInformation will be visible
     */
    public void setVideoInformationVisibility(boolean visible) {
        mTimeInfoContainer.setVisibility(visible ? VISIBLE : GONE);
    }

    /**
     * Listener for events such as trimming operation success and cancel
     *
     * @param onTrimVideoListener interface for events
     */
    @SuppressWarnings("unused")
    public void setOnTrimVideoListener(OnTrimVideoListener onTrimVideoListener) {
        mOnTrimVideoListener = onTrimVideoListener;
    }

    /**
     * Listener for some {@link VideoView} events
     *
     * @param onJWVideoListener interface for events
     */
    @SuppressWarnings("unused")
    public void setOnJWVideoListener(OnJWVideoListener onJWVideoListener) {
        mOnJWVideoListener = onJWVideoListener;
    }

    /**
     * Sets the path where the trimmed video will be saved
     * Ex: /storage/emulated/0/MyAppFolder/
     *
     * @param finalPath the full path
     */
    @SuppressWarnings("unused")
    public void setDestinationPath(final String finalPath) {
        mFinalPath = finalPath;
        ////JLog.d(TAG, "Setting custom path " + mFinalPath);
    }

    /**
     * Cancel all current operations
     */
    public void destroy() {
        BackgroundExecutor.cancelAll("", true);
        UiThreadExecutor.cancelAll("");
    }

    /**
     * Set the maximum duration of the trimmed video.
     * The trimmer interface wont allow the user to set duration longer than maxDuration
     *
     * @param maxDuration the maximum duration of the trimmed video in seconds
     */
    @SuppressWarnings("unused")
    public void setMaxDuration(int maxDuration) {
        // mMaxDuration = maxDuration * 1000;
        mMaxDuration = maxDuration;
    }

    /**
     * Sets the uri of the video to be trimmer
     *
     * @param videoURI Uri of the video
     */
    @SuppressWarnings("unused")
    public void setVideoURI(final Uri videoURI) {
        mSrc = videoURI;

        if (mOriginSizeFile == 0) {
            File file = new File(mSrc.getPath());

            mOriginSizeFile = file.length();
            long fileSizeInKB = mOriginSizeFile / 1024;

            if (fileSizeInKB > 1000) {
                long fileSizeInMB = fileSizeInKB / 1024;
                mTextSize.setText(String.format("%s %s", fileSizeInMB, getContext().getString(R.string.megabyte)));
            } else {
                mTextSize.setText(String.format("%s %s", fileSizeInKB, getContext().getString(R.string.kilobyte)));
            }
        }

        mVideoView.setVideoURI(mSrc);
        mVideoView.requestFocus();

        mTimeLineView.setVideo(mSrc);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    private static class MessageHandler extends Handler {

        @NonNull
        private final WeakReference<JWVideoTrimmer> mView;

        MessageHandler(JWVideoTrimmer view) {
            mView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            JWVideoTrimmer view = mView.get();
            if (view == null || view.mVideoView == null) {
                return;
            }

            view.notifyProgressUpdate(true);
            if (view.mVideoView.isPlaying()) {
                sendEmptyMessageDelayed(0, 10);
            }
        }
    }
}
