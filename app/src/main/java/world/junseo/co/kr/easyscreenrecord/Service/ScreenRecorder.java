package world.junseo.co.kr.easyscreenrecord.Service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

import world.junseo.co.kr.easyscreenrecord.Common.CommonInfo;
import world.junseo.co.kr.easyscreenrecord.Interface.OnCaptureStateChangedListener;
import world.junseo.co.kr.easyscreenrecord.Util.Log.JLog;

@SuppressLint({ "NewApi" })
public class ScreenRecorder {

	private static final String TAG = "ScreenRecorder";

	public static final int CHANNEL_COUNT = 1;
	public static final int SAMPLE_RATE = 44100;
	public static final int BIT_RATE_AUDIO = 128000;
	public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
	public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	public static final int SAMPLES_PER_FRAME = 1024; // AAC
	public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;

	//private GLSurfaceTexture glSurfaceTexture;
	//private WaterMarkInfo mWaterMarkInfo = null;

	private static final String VIDEO_MIME_TYPE = "video/avc";

	/**
	 * 레코딩 되는 파일 크기
	 */
	private long m_lCurrentSize = 0;

	/**
	 * 레코딩 시작 시칸
	 */
	private long m_lStartTime = 0;
	/**
	 * 레코딩 시간
	 */
	private final long RECORD_DURING_TIME = 1000 * 60 * 59 + 1000 * 59; // 59분
																		// 59초
	/**
	 * 레코딩 MAX 파일 크기
	 */
	private final long RECORD_MAX_FILE_SIZE = 1 * 1024 * 1024 * 1024; // 1기가

	private boolean m_bIsRecording = false;
	private boolean m_bMIC;
	private int mWidth;
	private int mHeight;
	private int mBitRate;
	private int mFrameRate;
	private int mDpi;
	private String mDstPath;
	private MediaProjection mMediaProjection;

	AudioRecord mAudioRecorder;

	private MediaCodec.BufferInfo mVideoBufferInfo;
	private MediaCodec mVideoEncoder;

	private MediaCodec.BufferInfo mAudioBufferInfo;
	private MediaCodec mAudioEncoder;

	private int mVideoTrackIndex = -1;
	private int mAudioTrackIndex = -1;

	private MediaMuxer mMuxer;

	private Surface mInputSurface;

	private boolean bIsRecording = false; // 음성 레코딩
	private boolean mMuxerStarted = false;

	private boolean mSetVideoIndex = false;

	private OnCaptureStateChangedListener onCaptureStateChangedListener;

	private final Handler mDrainHandler = new Handler(Looper.getMainLooper());
	private Runnable mDrainEncoderRunnable = new Runnable() {
		@Override
		public void run() {
			drainEncoder();
		}
	};

	public ScreenRecorder(MediaProjection mp) {
		mMediaProjection = mp;
	}

	public void setRecordBase(Context context, boolean bMIC, int width,
			int height, int bitrate, int framerate, int dpi, String dstPath) {
		m_bMIC = bMIC;
		mWidth = width;
		mHeight = height;
		mBitRate = bitrate;
		mFrameRate = framerate;
		mDpi = dpi;
		mDstPath = dstPath;
	}

	public void startRecording() {
		JLog.d(TAG, "레코딩 시작");
		prepareVideoEncoder();

		if (m_bMIC) {
			JLog.d(TAG, "레코딩 시작 prepareAudioEncoder");
			prepareAudioEncoder();
			JLog.d(TAG, "레코딩 시작 prepareAudioRecorder");
			prepareAudioRecorder();
			bIsRecording = true;
		}

		try {
			mMuxer = new MediaMuxer(mDstPath,
					MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
		} catch (Exception e) {
			//throw new RuntimeException("MediaMuxer creation failed", ioe);
			JLog.e(TAG, e.toString());
		}

		if (mMediaProjection == null) {
			JLog.d(TAG, "mMediaProjection == null");
			return;
		}

		/*Surface TextureSurface = null;

		glSurfaceTexture = new GLSurfaceTexture(mInputSurface, mWidth, mHeight,
				0);

		mWaterMarkInfo = new WaterMarkInfo();

		if (mWaterMarkInfo.waterMarkFilePath == null
				|| mWaterMarkInfo.waterMarkFilePath == "") {
			mWaterMarkInfo.waterMarkFilePath = "com_watermark.png";
			// mWaterMarkInfo.waterMarkFilePath =
			// Uri.parse("android.resource://com.example.screenrecordforapi21later/"
			// + R.drawable.com_watermark).toString();
			// Log.d(TAG, "mWaterMarkInfo.waterMarkFilePath : " +
			// mWaterMarkInfo.waterMarkFilePath);
		}

		if ((mWaterMarkInfo != null) && (mWaterMarkInfo.available() == true)) {
			Point waterMarkPoint = WaterMarkDrawUtils.computeWaterMarkPosition(
					new Point(mWidth, mHeight), mWaterMarkInfo, 0);

			glSurfaceTexture.addDrawObject(mWaterMarkInfo.waterMarkFilePath,
					waterMarkPoint, new Point(mWidth, mHeight));
		}

		try {
			TextureSurface = glSurfaceTexture.createInputSurface(mFrameRate);// 프레임레이트
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mMediaProjection.createVirtualDisplay("Recording Display", mWidth,
				mHeight, mDpi, 0 *//* flags *//*, TextureSurface, null *//* callback *//*,
				null *//* handler *//*);*/

		mMediaProjection.createVirtualDisplay("Easy Recording Display", mWidth,
				mHeight, mDpi, 0 /* flags */, mInputSurface, null /* callback */,
				null /* handler */);

		m_bIsRecording = true;
		JLog.d(TAG, "mMediaRecorder.start() OK!!!");

		if (onCaptureStateChangedListener != null)
			onCaptureStateChangedListener.onCaptureStarted();

		m_lStartTime = System.currentTimeMillis();

		JLog.d(TAG, "m_nStartTime : " + m_lStartTime);

		// Start the encoders
		drainEncoder();
	}

	public void stopRecording() {
		JLog.d(TAG, "레코딩 중지");


		mDrainHandler.removeCallbacks(mDrainEncoderRunnable);

		bIsRecording = false;
		mSetVideoIndex = false;

		m_lCurrentSize = 0;
		m_lStartTime = 0;

		try {
			if (mMuxer != null) {
				if (mMuxerStarted) {
					mMuxer.stop();
				}
				mMuxer.release();
				mMuxer = null;
				mMuxerStarted = false;
			}
		} catch (Exception e) {
			// TODO: handle exception

			mMuxer = null;
			mMuxerStarted = false;

			JLog.e(TAG, e.toString());
		}

		try {
			if (mVideoEncoder != null) {
				mVideoEncoder.stop();
				mVideoEncoder.release();
				mVideoEncoder = null;
			}

			if (mAudioEncoder != null) {
				mAudioEncoder.stop();
				mAudioEncoder.release();
				mAudioEncoder = null;
			}
			if (mInputSurface != null) {
				mInputSurface.release();
				mInputSurface = null;
			}

			if (mMediaProjection != null) {
				mMediaProjection.stop();
				mMediaProjection = null;
			}
		} catch (Exception e) {
			JLog.e(TAG, e.toString());
		}


		mVideoBufferInfo = null;
		mAudioBufferInfo = null;
		mDrainEncoderRunnable = null;
		mVideoTrackIndex = -1;
		mAudioTrackIndex = -1;

		m_bIsRecording = false;
	}

	public boolean getRecordStatus() {
		return m_bIsRecording;
	}

	public void setOnCaptureStateChangedListener(
			OnCaptureStateChangedListener paramOnCaptureStateChangedListener) {
		onCaptureStateChangedListener = paramOnCaptureStateChangedListener;
	}

	private void prepareVideoEncoder() {
		mVideoBufferInfo = new MediaCodec.BufferInfo();
		MediaFormat format = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE,
				mWidth, mHeight);

		// Set some required properties. The media codec may fail if these
		// aren't defined.
		format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
				MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
		format.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate); // 6Mbps
		format.setInteger(MediaFormat.KEY_FRAME_RATE, mFrameRate);
		format.setInteger(MediaFormat.KEY_CAPTURE_RATE, mFrameRate);
		format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER,
				1000000 / mFrameRate);
		format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
		format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1); // 1 seconds
																// between
																// I-frames

		// Create a MediaCodec encoder and configure it. Get a Surface we can
		// use for recording into.
		try {
			mVideoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE);
			mVideoEncoder.configure(format, null, null,
					MediaCodec.CONFIGURE_FLAG_ENCODE);
			mInputSurface = mVideoEncoder.createInputSurface();
			mVideoEncoder.start();
		} catch (IOException e) {
			onCaptureStateChangedListener
					.onCaptureStopped(CommonInfo.RecordResultInfo.ERROR_STOP);
		}
	}

	/**
	 * 레코딩 제한 시간을 체크한다.
	 * 
	 * @return true : 레코딩 제한 시간을 넘었다. false : 레코딩 가능
	 */
	private boolean checkDuringTime() {
		boolean bRet = false;

		long lCurrentTime = System.currentTimeMillis();

		long lTimeGap = lCurrentTime - m_lStartTime;

		JLog.d(TAG, "lTimeGap : " + lTimeGap);

		if (lTimeGap > RECORD_DURING_TIME) {
			bRet = true;
		}

		return bRet;
	}

	/**
	 * 레코딩 파일의 최대 크기를 체크한다.
	 * 
	 * @return true 레코딩 최대 파일 크기를 벗어났다. false : 아직 파일 크기가 정상이다.
	 */
	private boolean checkRecordFileSize() {
		boolean bRet = false;

		if (m_lCurrentSize >= RECORD_MAX_FILE_SIZE) {
			bRet = true;
		}

		return bRet;
	}

	private boolean drainEncoder() {

		try {
			mDrainHandler.removeCallbacks(mDrainEncoderRunnable);

			if (checkDuringTime()) {
				onCaptureStateChangedListener
						.onCaptureStopped(CommonInfo.RecordResultInfo.ERROR_STOP);
			} else if (checkRecordFileSize()) {
				onCaptureStateChangedListener
						.onCaptureStopped(CommonInfo.RecordResultInfo.MAX_FILESIZE_REACHED_STOP);
			} else {
				if (m_bMIC) {
					processDequeue(mVideoEncoder, mVideoBufferInfo, mMuxer,
							false);

					if (mSetVideoIndex)
						processDequeue(mAudioEncoder, mAudioBufferInfo, mMuxer,
								true);
				} else {
					processDequeueOnlyVideo(mVideoEncoder, mVideoBufferInfo,
							mMuxer);
				}

				mDrainHandler.postDelayed(mDrainEncoderRunnable, 10);
			}
		} catch (Exception e) {
			// TODO: handle exception
			JLog.e(TAG, e.toString());
			onCaptureStateChangedListener
					.onCaptureStopped(CommonInfo.RecordResultInfo.ERROR_STOP);
		}

		return false;
	}

	private void processDequeueOnlyVideo(MediaCodec mMediaCodec,
			MediaCodec.BufferInfo mBufferInfo, MediaMuxer mMediaMuxer) {
		int bufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 0);

		if (bufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
			// nothing available yet
			JLog.d(TAG, "MediaCodec.INFO_TRY_AGAIN_LATER");
		} else if (bufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
			// should happen before receiving buffers, and should only happen
			// once
			mVideoTrackIndex = mMediaMuxer.addTrack(mMediaCodec
					.getOutputFormat());

			if (!mMuxerStarted && mVideoTrackIndex >= 0) {
				mMediaMuxer.start();
				mMuxerStarted = true;
			}

		} else if (bufferIndex < 0) {
			// not sure what's going on, ignore it

		} else {

			ByteBuffer encodedData = mMediaCodec.getOutputBuffer(bufferIndex);
			if (encodedData == null) {
				throw new RuntimeException("couldn't fetch buffer at index "
						+ bufferIndex);
			}

			if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
				mBufferInfo.size = 0;
			}

			if (mBufferInfo.size != 0) {
				if (mMuxerStarted) {
					encodedData.position(mBufferInfo.offset);
					encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
					mMediaMuxer.writeSampleData(mVideoTrackIndex, encodedData,
							mBufferInfo);

					// 레코딩 되는 byte 수를 더해준다.
					// 나중에 레코딩 파일 제한 체크에 사용된다.
					m_lCurrentSize += mBufferInfo.size;
					JLog.d(TAG, "m_lCurrentSize : " + m_lCurrentSize);

				} else {
					// muxer not started
				}
			}

			mMediaCodec.releaseOutputBuffer(bufferIndex, false);

			if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
				JLog.i(TAG,
						"(mVideoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0 out");
			}
		}
	}

	private boolean processDequeue(MediaCodec mMediaCodec,
			MediaCodec.BufferInfo mBufferInfo, MediaMuxer mMediaMuxer,
			boolean bIsAudio) {
		boolean bResult = true;
		int bufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 0);

		if (bufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
			// nothing available yet
			JLog.d(TAG, "MediaCodec.INFO_TRY_AGAIN_LATER bIsAudio : "
					+ bIsAudio);
			bResult = false;
		} else if (bufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
			// should happen before receiving buffers, and should only happen
			// once

			JLog.d(TAG, "MediaCodec.INFO_OUTPUT_FORMAT_CHANGED bIsAudio : "
					+ bIsAudio);

			if (!bIsAudio) {
				mVideoTrackIndex = mMediaMuxer.addTrack(mMediaCodec
						.getOutputFormat());
				mSetVideoIndex = true;
			} else
				mAudioTrackIndex = mMediaMuxer.addTrack(mMediaCodec
						.getOutputFormat());

			if (!mMuxerStarted && mAudioTrackIndex != -1) {
				mMediaMuxer.start();
				mMuxerStarted = true;
			}

		} else if (bufferIndex < 0) {
			// not sure what's going on, ignore it
			JLog.d(TAG, "bufferIndex < 0 bIsAudio : " + bIsAudio);
		} else {
			JLog.d(TAG, "mMediaCodec.getOutputBuffer : " + bIsAudio);

			ByteBuffer encodedData = mMediaCodec.getOutputBuffer(bufferIndex);
			if (encodedData == null) {
				throw new RuntimeException("couldn't fetch buffer at index "
						+ bufferIndex);
			}

			if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
				mBufferInfo.size = 0;
			}

			if (mBufferInfo.size != 0) {
				if (mMuxerStarted) {
					encodedData.position(mBufferInfo.offset);
					encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
					if (bIsAudio) {
						mMediaMuxer.writeSampleData(mAudioTrackIndex,
								encodedData, mBufferInfo);
					} else {
						mMediaMuxer.writeSampleData(mVideoTrackIndex,
								encodedData, mBufferInfo);
					}

					// 레코딩 되는 byte 수를 더해준다.
					// 나중에 레코딩 파일 제한 체크에 사용된다.
					m_lCurrentSize += mBufferInfo.size;
					JLog.d(TAG, "m_lCurrentSize : " + m_lCurrentSize);

				} else {
					// muxer not started
				}
			}

			mMediaCodec.releaseOutputBuffer(bufferIndex, false);

			if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
				JLog.i(TAG,
						"(mVideoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0 out");
				bResult = false;
			}
		}

		return bResult;
	}

	private void prepareAudioEncoder() {
		mAudioBufferInfo = new MediaCodec.BufferInfo();
		MediaFormat format = new MediaFormat();
		format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
		format.setInteger(MediaFormat.KEY_AAC_PROFILE,
				MediaCodecInfo.CodecProfileLevel.AACObjectLC);
		format.setInteger(MediaFormat.KEY_SAMPLE_RATE, SAMPLE_RATE);
		format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, CHANNEL_COUNT);
		format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE_AUDIO);

		try {
			mAudioEncoder = MediaCodec.createEncoderByType("audio/mp4a-latm");
			mAudioEncoder.configure(format, null, null,
					MediaCodec.CONFIGURE_FLAG_ENCODE);
			mAudioEncoder.start();

		} catch (Exception e) {
			// TODO: handle exception
			onCaptureStateChangedListener
					.onCaptureStopped(CommonInfo.RecordResultInfo.ERROR_STOP);

			JLog.e(TAG, e.toString());
		}
	}

	private void prepareAudioRecorder() {
		int iMinBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
				CHANNEL_CONFIG, AUDIO_FORMAT);

		int iBufferSize;

		bIsRecording = false;

		iBufferSize = SAMPLES_PER_FRAME * mFrameRate;

		// Ensure buffer is adequately sized for the AudioRecord
		// object to initialize
		if (iBufferSize < iMinBufferSize)
			iBufferSize = ((iMinBufferSize / SAMPLES_PER_FRAME) + 1)
					* SAMPLES_PER_FRAME * 2;

		// AudioRecord mAudioRecorder;
		mAudioRecorder = new AudioRecord(AUDIO_SOURCE, // source
				SAMPLE_RATE, // sample rate, hz
				CHANNEL_CONFIG, // channels
				AUDIO_FORMAT, // audio format
				iBufferSize); // buffer size (bytes)

		JLog.d(TAG, "startRecording");
		mAudioRecorder.startRecording();

		new Thread(new AudioRecorderTask(mAudioRecorder), "AudioRecorderTask")
				.start();
	}

	private class AudioRecorderTask implements Runnable {

		int iReadResult = 0;

		AudioRecord mAudioRecorder;
		ByteBuffer[] inputBuffers;
		ByteBuffer inputBuffer;


		public AudioRecorderTask(AudioRecord recorder) {
			this.mAudioRecorder = recorder;
		}

		@Override
		public void run() {
			JLog.d(TAG, "AudioRecorder started recording");
			long audioPresentationTimeNs;

			byte[] mTempBuffer = new byte[SAMPLES_PER_FRAME];

			while (bIsRecording) {
				// audioPresentationTimeNs = System.nanoTime();
				audioPresentationTimeNs = System.currentTimeMillis();

				iReadResult = mAudioRecorder.read(mTempBuffer, 0,
						SAMPLES_PER_FRAME);
				if (iReadResult == AudioRecord.ERROR_BAD_VALUE
						|| iReadResult == AudioRecord.ERROR_INVALID_OPERATION)
					JLog.e(TAG, "audio buffer read error");

				// send current frame data to encoder
				try {
					if (inputBuffers == null)
						inputBuffers = mAudioEncoder.getInputBuffers();

					int inputBufferIndex = mAudioEncoder.dequeueInputBuffer(-1);
					if (inputBufferIndex >= 0) {
						inputBuffer = inputBuffers[inputBufferIndex];
						inputBuffer.clear();
						inputBuffer.put(mTempBuffer);
						// recycleInputBuffer(mTempBuffer);

						JLog.d(TAG,
								"sending frame to audio encoder audioPresentationTimeNs : "
										+ audioPresentationTimeNs);
						mAudioEncoder.queueInputBuffer(inputBufferIndex, 0,
								mTempBuffer.length,
								audioPresentationTimeNs * 1000, 0);
					}
				} catch (Throwable t) {
					JLog.e(TAG, "sendFrameToAudioEncoder exception");
					t.printStackTrace();
				}
			}

			// finished recording -> send it to the encoder
			audioPresentationTimeNs = System.nanoTime();

			iReadResult = mAudioRecorder
					.read(mTempBuffer, 0, SAMPLES_PER_FRAME);
			if (iReadResult == AudioRecord.ERROR_BAD_VALUE
					|| iReadResult == AudioRecord.ERROR_INVALID_OPERATION)
				JLog.e(TAG, "audio buffer read error");

			// send current frame data to encoder
			try {
				int inputBufferIndex = mAudioEncoder.dequeueInputBuffer(-1);
				if (inputBufferIndex >= 0) {
					inputBuffer = inputBuffers[inputBufferIndex];
					inputBuffer.clear();
					inputBuffer.put(mTempBuffer);

					JLog.d(TAG, "sending EOS to audio encoder");
					mAudioEncoder.queueInputBuffer(inputBufferIndex, 0,
							mTempBuffer.length, audioPresentationTimeNs / 1000,
							MediaCodec.BUFFER_FLAG_END_OF_STREAM);
				}
			} catch (Throwable t) {
				JLog.e(TAG, "sendFrameToAudioEncoder exception");
				t.printStackTrace();
			}

			JLog.d(TAG, "++++++++++++AudioRecorderTask 가 끝났다.");

			if (mAudioRecorder != null) {
				mAudioRecorder.stop();
				mAudioRecorder.release();
				mAudioRecorder = null;
				JLog.i(TAG, "stopped");
			}
		}
	}
}
