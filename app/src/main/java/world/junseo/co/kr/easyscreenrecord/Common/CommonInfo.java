package world.junseo.co.kr.easyscreenrecord.Common;

public class CommonInfo {

    public static String PACKAGE_NAME = "world.junseo.co.kr.easyscreenrecord";
    public static String MENU_ID = "MENU_ID";

    public static class PREF {
        // 레코딩 관련
        public static String REC_FACE_SETTING = "REC_FACE_SETTING";
        public static String REC_VOICE_SETTING = "REC_VOICE_SETTING";
        public static String REC_QUALITY_SETTING = "REC_QUALITY_SETTING";
        public static String FACECAM_SETTING_SIZE = "FACECAM_SETTING_SIZE";
        public static String FACECAM_SETTING_PERCENT = "FACECAM_SETTING_PERCENT";
        public static String SERVICE_EXECUTE_ALWAYS = "SERVICE_EXECUTE_ALWAYS";
        public static String RC = "RC"; //레코딩 카운트(이걸로 전면광고를 실행여부를 판단한다)
        public static String RC_IS_FAIL = "RC_IS_FAIL"; // 광고 노출 실패 유무
        public static String FIRST_TIME = "FIRST_TIME"; //레코딩 카운트(이걸로 전면광고를 실행여부를 판단한다)
        public static String CHECK_WIFI = "CHECK_WIFI";
    }

    public static class ADMOB_ID {
        public static String APP_ID = "ca-app-pub-7096183387874826~4810974488";
    }

    public static class START_ACTIVITY_RESULT_CODE {
        public static final int CHECK_DRAW_OVERLAY = 10001; //checkDrawOverlay
    }

    public static class REC_MOVE_RECT {
        public final static int CONTROL_LOCATION_RIGHT = 1;
        public final static int CONTROL_LOCATION_LEFT = 2;
        public final static int CONTROL_LOCATION_LEFT_UP_CONNER = 3;
        public final static int CONTROL_LOCATION_LEFT_DOWN_CONNER = 4;
        public final static int CONTROL_LOCATION_RIGHT_UP_CONNER = 5;
        public final static int CONTROL_LOCATION_RIGHT_DOWN_CONNER = 6;
    }

    // 레코드 상태 정보
    public class RecordResultInfo{
        public final static int NORMAL_STOP = 1001;
        public final static int ERROR_STOP = 1002;
        public final static int MAX_FILESIZE_REACHED_STOP = 1003;
    }

    public class Angle {
        public static final int ORIENTATION_0 = 0;
        public static final int ORIENTATION_90 = 3;
        public static final int ORIENTATION_270 = 1;
    }

    public class MenuID {
        public static final int RECORD_LIST = 0;
        public static final int RECORD_SETTINGS = 1;
    }

    public class BroadcastID {
        //public static final String START_RECORD = "world.junseo.co.kr.easyscreenrecord.START_RECORD";
        //public static final String STOP_RECORD = "world.junseo.co.kr.easyscreenrecord.STOP_RECORD";
        //public static final String APP_MOVE_BACKGROUD = "world.junseo.co.kr.easyscreenrecord.APP_MOVE_BACKGROUD";
    }

    public class MessageCode {
        public static final int RECORD_LIST = 10001;
    }
}
