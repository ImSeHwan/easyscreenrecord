package world.junseo.co.kr.easyscreenrecord.Model;

public class RecordSet {
	
	public static final String RECORD_QUALITY = "quality";
	public static final String RECORD_RESOLUTION = "resolution";
	public static final String RECORD_FRAME_RATE = "framerate";
	public static final String RECORD_AUDIO_RECORDING = "audio_support";
	public static final String RECORD_SPEED_MODE = "speed_mode";
	public static final String RECORD_GESTURE_SUPPORT = "gesture_support";
	
	public static final String RECORD_FILE_PATH = "filepath";
	public static final String RECORD_FILE_NAME = "filename";
	
	public static class RecordSize {
		public String width;
		public String height;
		
		public RecordSize(String width, String height) {
			this.width = width;
			this.height = height;
		}
	}
	
	public static final int[] BITRATE_DEFAULT = {
			10485760, 3145728
		/* 원본
		10485760,
		5242880,
		1048576,
		512000
		 */
	};
	
	public static final int[] FRAMERATE_DEFAULT = {
		5,
		10,
		15,
		20,
		0
	};
	
	public static final int[] FRAMERATE_LOLLIPOP_MORE_THAN = {
		5,
		10,
		15,
		20,
	};
	
	/**
	 * Video 기본 배율로 75%
	 */
	public static final float VIDEO_DEFAULT_RATIO = .75f;
	
	public static final int BANCHMARK_FPS_AUTO = 0;
	public static final int BANCHMARK_FPS_NO_LIMITS = -1;
}