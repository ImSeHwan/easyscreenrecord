package world.junseo.co.kr.easyscreenrecord.Service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import world.junseo.co.kr.easyscreenrecord.Common.CommonFunc;
import world.junseo.co.kr.easyscreenrecord.Common.CommonInfo;
import world.junseo.co.kr.easyscreenrecord.Control.RecordStatusLayout;
import world.junseo.co.kr.easyscreenrecord.Interface.OnCaptureStateChangedListener;
import world.junseo.co.kr.easyscreenrecord.JWApplication;
import world.junseo.co.kr.easyscreenrecord.Layout.RecordGroupLayout;
import world.junseo.co.kr.easyscreenrecord.MainActivity;
import world.junseo.co.kr.easyscreenrecord.Model.RecordSet;
import world.junseo.co.kr.easyscreenrecord.Model.RecordSet.RecordSize;
import world.junseo.co.kr.easyscreenrecord.R;
import world.junseo.co.kr.easyscreenrecord.RecordingResultActivity;
import world.junseo.co.kr.easyscreenrecord.Util.Log.JLog;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class RecordService extends Service {

    String TAG = "RecordService";

    String m_FloatingTouchControl = "";

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

    public static final String ACTION_PAUSE = "ACTION_STOP_RECORD";

    public static final String ACTION_PLAY = "ACTION_PLAY_RECORD";

    public static final String ACTION_START_RECORD = "ACTION_START_RECORD";

    public static final String ACTION_STOP_RECORD = "ACTION_STOP_RECORD";

    class RecordInfo {
        public String sPackageName = "";
        public String sCurrentTime = "";
    }

    RecordInfo mRecordInfo = null;

    RecordStatusLayout mRecordStatusLayout;

    //private ScreenRecorder mScreenRecorder = null;
    private OnCaptureStateChangedListener onCaptureStateChangedListener;

    // 녹화영상의 전체 파일 경로
    private String m_sFileName = "";

    // 레코딩 상태값 정의
    static final int RECORD_START = 1;
    static final int RECORD_END = 2;

    // 레코딩 상태값
    private int gRecordingStatus = 0;

    // 녹화가능 유무
    //boolean m_bIsRecordPossible = false;

    int getRecordingStatus() {
        return gRecordingStatus;
    }

    private WindowManager.LayoutParams mParams; // layout params 객체. 뷰의 위치 및 크기를 지정하는 객체
    private WindowManager mWindowManager; // 윈도우 매니저

    private float MAXBUTTON_START_X, MAXBUTTON_START_Y; // 움직이기 위해 터치한 시작 점
    private int MAXBUTTON_PREV_X, MAXBUTTON_PREV_Y; // 움직이기 이전에 뷰가 위치한 점
    private float REC_START_X, REC_START_Y; // 움직이기 위해 터치한 시작 점
    private int TOP_Y = 0, BOTTOM_Y = 0; // y축 값은 상태바 또는 소프트웨어 키가 있으므로 값이 다르다.
    private int REC_PREV_X, REC_PREV_Y; // 움직이기 이전에 뷰가 위치한 점
    private int REC_MAX_X = -1, REC_MAX_Y = -1; // 뷰의 위치 최대 값

    // 녹화상태 좌표
    private float START_X, START_Y; // 움직이기 위해 터치한 시작 점
    private int PREV_X, PREV_Y; // 움직이기 이전에 뷰가 위치한 점
    private int MAX_X = -1, MAX_Y = -1; // 뷰의 위치 최대 값

    // Rec 플로팅의 절대좌표
    int[] mRecButtonlocation = null;

    //FloatingButtonLayout mRecButton;
    RecordGroupLayout mRecGroup;
    // 레코딩 플로팅에 더해질 플로팅 정보
    WindowManager.LayoutParams recParam;

    // 소프트 키보드 처리를 위한 변수선언
    private boolean m_bIsSoftKey = false;
    private int m_nSoftKeySize = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public RecordService() {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
// TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);

        JLog.d(TAG, "onConfigurationChanged");
        // 화면이 돌아갔으므로 MAX값을 다시 조정한다.
        setRecFlotingMaxPosition();

        if (mWindowManager == null) {
            JLog.d(TAG, "mWindowManager == null");
            return;
        }

        if(mRecGroup == null) {
            return;
        }

        // 기존 좌표값을 저장하자
        float y_ratio = ((float) recParam.y / (float) REC_MAX_Y);
        int nOldX = recParam.x;

        // 이전에 왼쪽이면 (오른쪽 X 좌표값은 항상 0이므로 계산할 필요 없다)
        if (nOldX != 0) {
            //recParam.x = REC_MAX_X - mRecButton.getWidth();
            recParam.x = REC_MAX_X - mRecGroup.getWidth();
        }

        recParam.y = (int) (REC_MAX_Y * y_ratio);

        //mWindowManager.updateViewLayout(mRecButton, recParam);
        mWindowManager.updateViewLayout(mRecGroup, recParam);
    }

    // 게임중 circle 을 클릭하여 녹화를 중지 하고자 할때.
    GestureDetector recordStatusGesture = null;

    @Override
    public void onCreate() {
        super.onCreate();

        Toast.makeText(this, "레코딩 서비스 시작", Toast.LENGTH_SHORT).show();

        JLog.d(TAG, "RecordService service onCreate().");
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        recordStatusGesture = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // TODO Auto-generated method stub

                if (getRecordingStatus() != RECORD_END) {
                    stopRecordProcess(true);
                    // 레코딩이 끝났으므로 메인리스트의 갱신이 필요하다.
                    JWApplication.getAppContext().setRecorded(true);

                    JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.RC, true);
                    Toast.makeText(RecordService.this, getString(R.string.recorded), Toast.LENGTH_LONG).show();
                }
                return true;
            }

        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(gRecordingStatus == RECORD_START) {
            stopRecordProcess(false);
        }

        Toast.makeText(this, "레코딩 서비스 종료", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        JLog.d(TAG, "RecordService service onStartCommand().");

        if(intent != null)
        {
            String action = intent.getAction();

            switch (action)
            {
                case ACTION_START_FOREGROUND_SERVICE:
                    startForegroundService();
                    //Toast.makeText(getApplicationContext(), "Foreground service is started.", Toast.LENGTH_LONG).show();
                    break;
                case ACTION_STOP_FOREGROUND_SERVICE:
                    stopForegroundService();
                    //Toast.makeText(getApplicationContext(), "Foreground service is stopped.", Toast.LENGTH_LONG).show();
                    break;
                case ACTION_PLAY:
                    showRecordButton();
                    break;
                case ACTION_PAUSE:
                    hideRecordButton();
                    break;
                case ACTION_START_RECORD:
                    recordProcess();
                    break;
/*                case ACTION_STOP_RECORD:
                    stopRecord();
                    break;*/
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * REC 플로팅의 좌표를 초기화한다.
     */
    private void setRecFlotingMaxPosition() {
        try {
            DisplayMetrics matrixReal = new DisplayMetrics();
            DisplayMetrics matrix = new DisplayMetrics();
            mWindowManager.getDefaultDisplay().getRealMetrics(matrixReal); // 화면
            // 정보를
            // 가져와서
            mWindowManager.getDefaultDisplay().getMetrics(matrix);

            // RbPreference mRbPreference = new RbPreference(this);

            if (matrix.widthPixels != matrixReal.widthPixels) {
                // 가로 모드일때만 체크하자
                if (matrix.widthPixels > matrix.heightPixels) {
                    // 둘간의 x좌표가 다르면 소프트 키카 있는거다
                    JLog.d(TAG, "가로모드이고 소프트키카 있다.");
                    m_bIsSoftKey = true;
                    m_nSoftKeySize = matrixReal.widthPixels
                            - matrix.widthPixels;
                }
            }

            REC_MAX_X = matrix.widthPixels; // x 최대값 설정
            REC_MAX_Y = matrix.heightPixels; // y 최대값 설정

            // 상태바를 제외한 TOP 영역을 구한다.
            // TOP_Y = mRbPreference.getValue(BaseInfo.START_Y_POSITION, 0);
            // BOTTOM_Y = mRbPreference.getValue(BaseInfo.END_Y_POSITION,
            // REC_MAX_Y);

            // Log.i(TAG, "TOP_Y : " + TOP_Y + " BOTTOM_Y : " + BOTTOM_Y);

            JLog.i(TAG, "REC_MAX_X : " + REC_MAX_X + " REC_MAX_Y : "
                    + REC_MAX_Y);
        } catch (Exception e) {
            // TODO: handle exception
            // Log.e(TAG, "setRecFlotingMaxPosition : " + e.toString());
        }
    }

    // 레코드 버튼을 제거한다.
    private void hideRecordButton() {
        if(mWindowManager == null) {
            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        }

        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
/*                if (mRecButton != null) {
                    mWindowManager.removeView(mRecButton);
                    mRecButton = null;
                }*/
                if (mRecGroup != null) {
                    mWindowManager.removeView(mRecGroup);
                    mRecGroup = null;
                }
            }
        });

        mRecButtonGestureListener = null;
    }

    private void showRecordButton() {

        try {
            //boolean foregroud = new ForegroundCheckTask().execute(RecordService.this).get();

//            if(JWApplication.getAppContext().getAppStatus() == JWApplication.AppStatus.BACKGROUND)
//                JLog.d("sehwan", "BACKGROUND");
//            else if(JWApplication.getAppContext().getAppStatus() == JWApplication.AppStatus.FOREGROUND) {
//                JLog.d("sehwan", "FOREGROUND");
//            } else if(JWApplication.getAppContext().getAppStatus() == JWApplication.AppStatus.RETURNED_TO_FOREGROUND) {
//                JLog.d("sehwan", "RETURNED_TO_FOREGROUND");
//            }

            if(JWApplication.getAppContext().getAppStatus() != JWApplication.AppStatus.BACKGROUND) {
                JLog.d("sehwan", "FOREGROUND");
                return;
            }

            if (gRecordingStatus == RECORD_START)
                return;

            JLog.d(TAG, "showRecordButton");
            m_bIsSoftKey = false;
            m_nSoftKeySize = 0;

            if (mWindowManager == null) {
                mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            }

            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (mRecGroup == null) {

                        mRecButtonGestureListener = new RecButtonGestureListener(RecordService.this);

                        mRecGroup = new RecordGroupLayout(RecordService.this);

                        mRecGroup.setOnRecordGroupLayoutInterface(new RecordGroupLayout.OnRecordGroupLayoutInterface() {
                            @Override
                            public void OnSelectControl(String controlName) {
                                m_FloatingTouchControl = controlName;
                            }
                        });
                        mRecGroup.setOnTouchListener(mRecButtonTouchEventListener);

                        recParam = new WindowManager.LayoutParams(
                                WindowManager.LayoutParams.WRAP_CONTENT,
                                WindowManager.LayoutParams.WRAP_CONTENT, 0, 0,
                                CommonFunc.getApplicationOverlayFlag(),
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                                PixelFormat.TRANSLUCENT);

                        recParam.gravity = Gravity.RIGHT | Gravity.BOTTOM;

                        mWindowManager.addView(mRecGroup, recParam);

                        setRecFlotingMaxPosition();
                    } else {
                        JLog.d(TAG, "이미 레코드 버튼이 존재한다.");
                    }
                }
            });
        } catch (Exception e) {
            JLog.e(TAG, e.toString());
        }

    }

    private void startForegroundService()
    {
        JLog.d(TAG, "Start foreground service.");

        // Create notification default intent.
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        String channelId = "foregroundRecordService";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_NONE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager nm =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(channel);
        }


        // Create notification builder.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setContentTitle(getString(R.string.record_service_info));
        builder.setContentText(getString(R.string.record_service_detail_info));

        // Make notification show big text.
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(getString(R.string.record_service_info));
        bigTextStyle.bigText(getString(R.string.record_service_detail_info));
        // Set big text style.
        builder.setStyle(bigTextStyle);

        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.ic_launcher);
        Bitmap largeIconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.baseline_videocam_black_24dp);
        builder.setLargeIcon(largeIconBitmap);
        // Make the notification max priority.
        builder.setPriority(Notification.PRIORITY_MAX);
        // Make head-up notification.
        builder.setFullScreenIntent(pendingIntent, true);

        // Add Play button intent in notification.
        Intent playIntent = new Intent(this, RecordService.class);
        playIntent.setAction(ACTION_PLAY);
        PendingIntent pendingPlayIntent = PendingIntent.getService(this, 0, playIntent, 0);
        NotificationCompat.Action playAction = new NotificationCompat.Action(android.R.drawable.ic_media_play, getString(R.string.start_record), pendingPlayIntent);
        builder.addAction(playAction);

        // Add Pause button intent in notification.
        Intent pauseIntent = new Intent(this, RecordService.class);
        pauseIntent.setAction(ACTION_PAUSE);
        PendingIntent pendingPrevIntent = PendingIntent.getService(this, 0, pauseIntent, 0);
        NotificationCompat.Action prevAction = new NotificationCompat.Action(android.R.drawable.ic_media_pause, getString(R.string.stop_record), pendingPrevIntent);
        builder.addAction(prevAction);

        // Build the notification.
        Notification notification = builder.build();

        // Start foreground service.
        startForeground(1, notification);

        //showRecordButton();
    }

    private void stopForegroundService()
    {
        JLog.d(TAG, "Stop foreground service.");

        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();

        hideRecordButton();
    }

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            //Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            readyRecord();
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {

        }
    };

    RecButtonTouchEventListener mRecButtonTouchEventListener = new RecButtonTouchEventListener();
    protected RecButtonGestureListener mRecButtonGestureListener;

    class RecButtonGestureListener extends GestureDetector.SimpleOnGestureListener {
        Context context;
        GestureDetector gDetector;

        public RecButtonGestureListener() {
            super();
        }

        public RecButtonGestureListener(Context context) {
            this(context, null);
        }

        public RecButtonGestureListener(Context context,
                                        GestureDetector gDetector) {

            if (gDetector == null)
                gDetector = new GestureDetector(context, this);

            this.context = context;
            this.gDetector = gDetector;
        }

        public GestureDetector getDetector() {
            return gDetector;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            if(!CommonFunc.IsEmpty(m_FloatingTouchControl)) {

                switch (m_FloatingTouchControl) {
                    case RecordGroupLayout.RECORD:
                        // TODO : 레코딩 실행하자
                        recordProcess();
                        break;
                    case RecordGroupLayout.LIST:
                        //Toast.makeText(RecordService.this, "리스트 터치", Toast.LENGTH_SHORT).show();

                        hideRecordButton();

                        Intent intent = new Intent(RecordService.this, MainActivity.class);
                        intent.putExtra(CommonInfo.MENU_ID, CommonInfo.MenuID.RECORD_LIST);
                        PendingIntent p = PendingIntent.getActivity(RecordService.this, 0, intent, 0);
                        try {
                            p.send();
                        } catch (Exception error) {
                            JLog.e(TAG, error.toString());
                        }
                        // 아래처럼 서비스에서 액티비티를 호출할 때 startActivity를 사용하면 지연실행이 될 경우가 있다.
                        // 위와 같이 PendingIntent를 통해 send 하자
                        /*Intent intent = new Intent(RecordService.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(CommonInfo.MENU_ID, CommonInfo.MenuID.RECORD_LIST);
                        startActivity(intent);*/
                        break;
                    case RecordGroupLayout.CLOSE:
                        hideRecordButton();
                        break;
                }
            }

            return true;
            //return super.onSingleTapUp(e);
        }

    }

    public void recordProcess() {
        ArrayList<String> permissions = new ArrayList<>(Arrays.asList(WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, CAMERA));

        if (CommonFunc.checkPermission(RecordService.this, permissions)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && Settings.canDrawOverlays(this)) {
                readyRecord();
            } else {
                Toast.makeText(this, getString(R.string.record_service_information2), Toast.LENGTH_SHORT).show();
            }
        } else {
            try {
                TedPermission.with(RecordService.this)
                        .setPermissionListener(permissionlistener)
                        .setDeniedMessage(getString(R.string.info_message1))
                        .setPermissions(RECORD_AUDIO, CAMERA, WRITE_EXTERNAL_STORAGE)
                        .check();
            } catch (Exception e2) {
                JLog.e(TAG, e2.toString());
            }
        }
    }

    class RecButtonTouchEventListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub

            JLog.d(TAG, "getID : " + v.getId());
            mRecButtonGestureListener.getDetector().onTouchEvent(event);

            if (mRecButtonlocation == null)
                mRecButtonlocation = new int[2];

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    JLog.d(TAG, "MotionEvent.ACTION_DOWN");

                    v.getLocationOnScreen(mRecButtonlocation);
                    JLog.d(TAG, "location[0] : " + mRecButtonlocation[0] + " location[1] : "
                            + mRecButtonlocation[1]);

                    if (m_bIsSoftKey == true) {
                        // JLog.d("sehwan", "소프트 키카 있는 디바이스");
                        // 최대값 + 소프트키 = 전체화면
                        // 따라서 레이아웃의 넓이를 빼면 x 절대좌표다.
                        // 레코딩 플로팅은 항상 좌우 끝에만 붙어 있으므로 만약 절대좌표값이 소프트 키 크기만큼
                        // 밀렸다면 그만큼 최대값도 늘려줘야한다. 그래야 플로팅이 펼쳐질때 이상동작을 하지 않는다.
                        if (mRecButtonlocation[0] == REC_MAX_X + m_nSoftKeySize
                                - v.getWidth()) {
                            JLog.d(TAG, "절대좌표가 0이 아니다");
                            REC_MAX_X = REC_MAX_X + m_nSoftKeySize;
                        }
                    }


                    REC_START_X = event.getRawX(); // 터치 시작 점
                    REC_START_Y = event.getRawY(); // 터치 시작 점

                    REC_PREV_X = recParam.x; // 뷰의 시작 점
                    REC_PREV_Y = recParam.y; // 뷰의 시작 점

                    break;

                case MotionEvent.ACTION_MOVE:
                    JLog.d(TAG, "MotionEvent.ACTION_MOVE");

                    int x = (int) (event.getRawX() - REC_START_X); // 이동한 거리
                    int y = (int) (event.getRawY() - REC_START_Y); // 이동한 거리

                    recParam.x = REC_PREV_X - x;
                    recParam.y = REC_PREV_Y - y;

                    mWindowManager.updateViewLayout(mRecGroup, recParam); // 뷰
                    break;

                case MotionEvent.ACTION_UP:

                    // 레이아웃 좌표는 최초 생성되는 좌표 기준이다. 꼭 명심하자
                    // Log.i(TAG, "MotionEvent.ACTION_UP m_bIsShowAllowed : " +
                    // m_bIsShowAllowed);
                    DisplayMetrics matrix = new DisplayMetrics();
                    mWindowManager.getDefaultDisplay().getMetrics(matrix);
                    // 오른쪽
                    if (matrix.widthPixels / 2 < event.getRawX()) {
                        // 오른쪽 상단
                        if (recParam.y <= -(REC_MAX_Y - mRecGroup.getHeight() - TOP_Y) ) {
                            recParam.y = -(REC_MAX_Y - mRecGroup.getHeight() - TOP_Y) ;
                        }
                        // 오른쪽 하단
                        else if (recParam.y >= (REC_MAX_Y - mRecGroup.getHeight() - TOP_Y) ) {
                            recParam.y = (REC_MAX_Y - mRecGroup.getHeight() - TOP_Y) ;
                        }

                        //recParam.x = 0;
                        mWindowManager.updateViewLayout(mRecGroup, recParam);
                    }
                    // 왼쪽
                    else {
                        // 왼쪽 상단
                        if (recParam.y <= -(REC_MAX_Y - mRecGroup.getHeight() - TOP_Y) ) {
                            recParam.y = -(REC_MAX_Y - mRecGroup.getHeight() - TOP_Y) ;
                        }
                        // 왼쪽 하단
                        else if (recParam.y > (REC_MAX_Y - mRecGroup.getHeight() - TOP_Y) ) {
                            recParam.y = (REC_MAX_Y - mRecGroup.getHeight() - TOP_Y) ;
                        }

                        // JLog.d(TAG, "REC_MAX_X : " + REC_MAX_X);

                        //recParam.x = REC_MAX_X - mRecButton.getWidth();
                        mWindowManager.updateViewLayout(mRecGroup, recParam);
                    }
/*                    // 오른쪽
                    if (matrix.widthPixels / 2 < event.getRawX()) {
                        // 오른쪽 상단
                        if (recParam.y <= -(REC_MAX_Y - mRecButton.getHeight() - TOP_Y) ) {
                            recParam.y = -(REC_MAX_Y - mRecButton.getHeight() - TOP_Y) ;
                        }
                        // 오른쪽 하단
                        else if (recParam.y >= (REC_MAX_Y - mRecButton.getHeight() - TOP_Y) ) {
                            recParam.y = (REC_MAX_Y - mRecButton.getHeight() - TOP_Y) ;
                        }

                        //recParam.x = 0;
                        mWindowManager.updateViewLayout(mRecButton, recParam);
                    }
                    // 왼쪽
                    else {
                        // 왼쪽 상단
                        if (recParam.y <= -(REC_MAX_Y - mRecButton.getHeight() - TOP_Y) ) {
                            recParam.y = -(REC_MAX_Y - mRecButton.getHeight() - TOP_Y) ;
                        }
                        // 왼쪽 하단
                        else if (recParam.y > (REC_MAX_Y - mRecButton.getHeight() - TOP_Y) ) {
                            recParam.y = (REC_MAX_Y - mRecButton.getHeight() - TOP_Y) ;
                        }

                        // JLog.d(TAG, "REC_MAX_X : " + REC_MAX_X);

                        //recParam.x = REC_MAX_X - mRecButton.getWidth();
                        mWindowManager.updateViewLayout(mRecButton, recParam);
                    }*/

                    break;
                default:
                    break;
            }
            return true;
        }
    }

    /** 레코딩 준비 */
    public void readyRecord() {
        try {
            if (SimpleScreenRecorder.getInstance().getMediaProjection() == null) {
                gRecordingStatus = 0;

                Intent intent = new Intent(this, RecordingResultActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            } else {
                startRecord();
            }
        } catch (Exception e) {
            JLog.e(TAG, e.toString());
        }
    }

    /**
     * 레코딩 시작
     */
    public void startRecord() {
        if(SimpleScreenRecorder.getInstance().getMediaProjection() == null)
            return;

        //Intent mainactivityBroadcastIntent = new Intent(CommonInfo.BroadcastID.APP_MOVE_BACKGROUD);
        //sendBroadcast(mainactivityBroadcastIntent);

        gRecordingStatus = RECORD_START;

        if (setRecordInfo()) {
            showRecordStatus();
            SimpleScreenRecorder.getInstance().startRecord();
            hideRecordButton();

            JWApplication.getAppContext().setRecordStatus(JWApplication.RecordStatus.RUN);

            //Intent mainactivityBroadcastIntent2 = new Intent(CommonInfo.BroadcastID.START_RECORD);
            //sendBroadcast(mainactivityBroadcastIntent2);
        } else {
            gRecordingStatus = 0;
        }
    }

    public void stopRecord() {
        SimpleScreenRecorder.getInstance().stopRecord();
        gRecordingStatus = RECORD_END;

        JWApplication.getAppContext().setRecordStatus(JWApplication.RecordStatus.IDLE);
        //Intent mainactivityBroadcastIntent = new Intent(CommonInfo.BroadcastID.STOP_RECORD);
        //sendBroadcast(mainactivityBroadcastIntent);
    }

    /**
     * 레코딩 관련 데이터 세팅
     *
     * @return
     */
    public boolean setRecordInfo() {
        boolean bRet = false;

        boolean bIsOrientation = false;
        if (Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bIsOrientation = true;
        }

        boolean bIsUseMIC = JWApplication.getAppContext().getSimplePrefs().get(CommonInfo.PREF.REC_VOICE_SETTING, true);

        int nQualityIndex = JWApplication.getAppContext().getSimplePrefs().get(CommonInfo.PREF.REC_QUALITY_SETTING, 1);

        long lCurrentDate = System.currentTimeMillis();
        Date date = new Date(lCurrentDate);
        SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
        String strCurDate = CurDateFormat.format(date);

        if (mRecordInfo == null)
            mRecordInfo = new RecordInfo();

        mRecordInfo.sCurrentTime = strCurDate;
        mRecordInfo.sPackageName = getPackageName();

        m_sFileName = getPackageName() + "_" + strCurDate;

        String sMediaPath = CommonFunc.getRecordPath() + "/" + m_sFileName + ".mp4";


        RecordSize mRecordSize = null;

        // 가로/세로 모드 구분
        DisplayMetrics matrix = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(matrix); // 화면 정보를 가져와서
        int nScreenDensity = matrix.densityDpi;

        mRecordSize = new RecordSize(Integer.toString(matrix.widthPixels),
                Integer.toString(matrix.heightPixels));

        bRet = SimpleScreenRecorder.getInstance().setRecordBase(this, bIsUseMIC,
                Integer.parseInt(mRecordSize.width),
                Integer.parseInt(mRecordSize.height),
                getAngle(),
                RecordSet.BITRATE_DEFAULT[nQualityIndex], 25, nScreenDensity,
                sMediaPath);

        return bRet;
    }

    // 화면이 돌아간 각도를 알아낸다.
    private int getAngle() {
        int nAngle = 0;

        Display display = mWindowManager.getDefaultDisplay();
        int screenOrientation = display.getRotation();

        switch (screenOrientation)
        {
            default:
            case CommonInfo.Angle.ORIENTATION_0: // Portrait
                nAngle = 0;
                break;
            case CommonInfo.Angle.ORIENTATION_90: // Landscape right
                nAngle = 90;
                break;
            case CommonInfo.Angle.ORIENTATION_270: // Landscape left
                nAngle = 270;
                break;
        }

        return nAngle;
    }

    /**
     * 녹화 시작 후 녹화 진행 상태를 보여준다.(일반/페이스캠)
     */
    public void showRecordStatus() {

        //HandlerThread t = new HandlerThread("Record");
        //t.start();

        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                JLog.d(TAG, "showRecordStatus");
                try {
                    // 최상위 윈도우에 넣기 위한 설정
                    if (mParams != null)
                        mParams = null;

                    mParams = new WindowManager.LayoutParams(
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            CommonFunc.getApplicationOverlayFlag(), // 항상 최 상위에 있게.
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT); // 투명
                    mParams.gravity = Gravity.LEFT | Gravity.BOTTOM; // 왼쪽 중앙

                    // 녹화시 보여주지 않고 싶으면 주석을 풀자. 대신 가속화 모드일 경우는 없어지지 않는다.
                    // mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

                    if(mWindowManager == null)
                        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

                    mRecordStatusLayout = new RecordStatusLayout(RecordService.this);
                    mRecordStatusLayout.setOnTouchListener(recordStatusTouchListener);

                    boolean bIsOn = JWApplication.getAppContext().getSimplePrefs().get(CommonInfo.PREF.REC_FACE_SETTING, true);

                    // bIsOn false : 일반, true : 페이스캠
                    if (bIsOn) {
                        mRecordStatusLayout.startUI(true);
                    } else {
                        mRecordStatusLayout.startUI(false);
                    }

                    mWindowManager.addView(mRecordStatusLayout, mParams);

                    JLog.d(TAG, "showRecordStatus end");
                } catch (Exception e) {
                    JLog.e(TAG, e.toString());
                }
            }
        });
    }

    /**
     * 녹화 중료 후 녹화 진행 상태 플로팅을 제거한다.(일반/페이스캠)
     */
    public void closeRecordStatus() {

        JLog.d(TAG, "closeRecordStatus");

        if (mRecordStatusLayout != null) {
            if (mWindowManager != null) {
                // HandlerThread t = new HandlerThread("Thread");
                // t.start();
                // new Handler(t.getLooper()).post(new Runnable() {
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        // X좌표를 갱신하기 위해서 MAX_X에 -1 대입
                        MAX_X = -1;

                        if (mRecordStatusLayout != null) {
                            // MLog.WriteLog(TAG, "녹화 상태 종료(일반/페이스캠)");
                            ((WindowManager) getSystemService(WINDOW_SERVICE))
                                    .removeView(mRecordStatusLayout);
                            mRecordStatusLayout = null;
                            // MLog.WriteLog(TAG, "녹화 상태 종료(일반/페이스캠) end");
                        }
                    }
                });
            }
        }
    }

    final RecordStatusTouchListener recordStatusTouchListener = new RecordStatusTouchListener();

    class RecordStatusTouchListener implements View.OnTouchListener {
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            if (mRecordStatusLayout == null)
                return true;

            recordStatusGesture.onTouchEvent(event);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: // 사용자 터치 다운이면
                    if (MAX_X == -1)
                        setStatusMaxPosition();
                    START_X = event.getRawX(); // 터치 시작 점
                    START_Y = event.getRawY(); // 터치 시작 점
                    PREV_X = mParams.x; // 뷰의 시작 점
                    PREV_Y = mParams.y; // 뷰의 시작 점

                    break;
                case MotionEvent.ACTION_MOVE:
                    int x = (int) (event.getRawX() - START_X); // 이동한 거리
                    int y = (int) (event.getRawY() - START_Y); // 이동한 거리

                    // 터치해서 이동한 만큼 이동 시킨다

                    mParams.x = PREV_X + x;
                    mParams.y = PREV_Y - y;

                    optimizePosition(); // 뷰의 위치 최적화
                    mWindowManager
                            .updateViewLayout(mRecordStatusLayout, mParams); // 뷰
                    // 업데이트
                    break;
            }

            return true;
        }
    }

    /**
     * 뷰의 위치가 화면 안에 있게 최대값을 설정한다
     */
    private void setStatusMaxPosition() {
        try {
            DisplayMetrics matrix = new DisplayMetrics();
            mWindowManager.getDefaultDisplay().getMetrics(matrix);

            MAX_X = matrix.widthPixels - mRecordStatusLayout.getWidth(); // x 최대값
            MAX_Y = matrix.heightPixels - mRecordStatusLayout.getHeight(); // y 최대값
        } catch (Exception e) {
            // TODO: handle exception
            JLog.e(TAG, "setStatusMaxPosition : " + e.toString());
        }

    }

    /**
     * 뷰의 위치가 화면 안에 있게 하기 위해서 검사하고 수정한다.
     */
    private void optimizePosition() {
        // 최대값 넘어가지 않게 설정
        if (mParams.x > MAX_X)
            mParams.x = MAX_X;
        if (mParams.y > MAX_Y)
            mParams.y = MAX_Y;
        if (mParams.x < 0)
            mParams.x = 0;
        if (mParams.y < 0)
            mParams.y = 0;
    }

    public void stopRecordProcess(boolean isShowRecordButton) {

        JLog.d(TAG, "stopRecordProcess");
        stopRecord();

        if (mRecordStatusLayout != null)
            mRecordStatusLayout.endTimer();

        // 썸네일을 만든후에 Image 파일명은 지우자
        if (!CommonFunc.IsEmpty(m_sFileName)) {
            String sThumbImagePath = CommonFunc.getRecordThumbPath()
                    + "/" + m_sFileName + ".jpg";
            String sMP4Path = CommonFunc.getRecordPath() + "/"
                    + m_sFileName + ".mp4";

            JLog.d(TAG, "sThumbImagePath : " + sThumbImagePath
                    + " sMP4Path : " + sMP4Path);

            try {

                Bitmap thumbImage = ThumbnailUtils
                        .createVideoThumbnail(sMP4Path,
                                MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
                if (thumbImage != null) {
                    CommonFunc.SaveBitmapToFileCache(thumbImage,
                            sThumbImagePath);
                }
            } catch (Exception e) {
                // TODO: handle exception
                JLog.d(TAG, e.toString());
            }

            JLog.d(TAG, "레코딩 중지 성공");
            m_sFileName = "";
        }

        closeRecordStatus();

        if(isShowRecordButton)
            showRecordButton();
    }

    class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
            final Context context = params[0].getApplicationContext();
            return isAppOnForeground(context);
        }

        private boolean isAppOnForeground(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                return false;
            }
            final String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }
    }
}
