package world.junseo.co.kr.easyscreenrecord.Control;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import world.junseo.co.kr.easyscreenrecord.Common.CommonFunc;
import world.junseo.co.kr.easyscreenrecord.Common.CommonInfo;
import world.junseo.co.kr.easyscreenrecord.JWApplication;
import world.junseo.co.kr.easyscreenrecord.R;
import world.junseo.co.kr.easyscreenrecord.Service.RecordService;

public class RecordStatusLayout extends RelativeLayout {

	Context mContext;

	private long m_lOldTime = 0L;
	private TimerTask mTask;
	private Timer mTimer;
	final Handler handler = new Handler();

	int CAM_MAX_SIZE = 135;
	int CAM_MIN_SIZE = 57;

	TextView tv_timer, tv_facecam_timer;
	boolean m_bFaceCam = false;

	RelativeLayout rl_time_status_group, rl_facecam_group;
	LinearLayout ll_camera_group;

	LinearLayout ll_facecam_timer;

	CameraCircleView mCameraCircleView;
    final int FILTER_NONE = 0;
    final int FILTER_BLACK_WHITE = 1;
    final int FILTER_BLUR = 2;
    final int FILTER_SHARPEN = 3;
    final int FILTER_EDGE_DETECT = 4;
    final int FILTER_EMBOSS = 5;

	public RecordStatusLayout(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	private float getDefaultCamSize() {
		float fDPSize = 50 * (CAM_MAX_SIZE - CAM_MIN_SIZE) / 100 + CAM_MIN_SIZE;

		if(fDPSize > CAM_MAX_SIZE)
			fDPSize = CAM_MAX_SIZE;

		if(fDPSize < CAM_MIN_SIZE)
			fDPSize = CAM_MIN_SIZE;

		//float fPixelSize = CommFunction.convertDpToPixel(fDPSize, FaceCamDetailSettingActivity.this);
		return CommonFunc.dipToPixels(getContext(), fDPSize);
	}

	public RecordStatusLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
		
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.recording_status_layout, this, true);
		
		
		rl_time_status_group = (RelativeLayout)findViewById(R.id.rl_time_status_group);
		rl_facecam_group = (RelativeLayout)findViewById(R.id.rl_facecam_group);
		
		ll_camera_group  = (LinearLayout)findViewById(R.id.ll_camera_group);

		ll_facecam_timer  = findViewById(R.id.ll_facecam_timer);
		
		tv_timer = (TextView)findViewById(R.id.tv_timer);
		tv_facecam_timer = (TextView)findViewById(R.id.tv_facecam_timer);
	}
	
	public void startUI(boolean bIsFaceCam)
	{
		m_bFaceCam = bIsFaceCam;
		
		if(bIsFaceCam) {
			
			if(rl_facecam_group.getVisibility() == GONE)
			{
				rl_facecam_group.setVisibility(VISIBLE);
			}
			
			if(rl_time_status_group.getVisibility() == VISIBLE)
			{
				rl_time_status_group.setVisibility(GONE);
			}

			float fFaceCamSize = JWApplication.getAppContext().getSimplePrefs().get(CommonInfo.PREF.FACECAM_SETTING_SIZE, getDefaultCamSize());

			//if(fFaceCamSize == -1)
			//	fFaceCamSize = FACE_CAM_MIN;
			
			mCameraCircleView = new CameraCircleView(getContext(), FILTER_NONE, 90);
			
			LayoutParams params = (LayoutParams) ll_camera_group.getLayoutParams();
			params.width = (int)fFaceCamSize;
			params.height = (int)fFaceCamSize;
			
			ll_camera_group.setLayoutParams(params);
			
			// timer 위치 조정			
			LayoutParams paramsTimer = (LayoutParams) ll_facecam_timer.getLayoutParams();
			paramsTimer.setMargins(0, params.height - 60, 0, 0);

			ll_facecam_timer.setLayoutParams(paramsTimer);
			
			ll_camera_group.addView(mCameraCircleView);
			ll_camera_group.setVisibility(VISIBLE);
		} else {
			
			if(rl_facecam_group.getVisibility() == VISIBLE)
			{
				rl_facecam_group.setVisibility(GONE);
			}
			
			if(rl_time_status_group.getVisibility() == GONE)
			{
				rl_time_status_group.setVisibility(VISIBLE);
			}
		}
		
		startTimer();
	}

	public void startTimer() {
		m_lOldTime = SystemClock.uptimeMillis();

		mTask = new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						long lCurrentTime = SystemClock.uptimeMillis();
						long millis = lCurrentTime - m_lOldTime;

						// 한시간이 넘으면 레코딩을 중지한다.
						if(millis > (1000 * 60 * 59) + (1000 * 59))
						{
							((RecordService)mContext).stopRecordProcess(true);
							endTimer();
							return;
						}
						
						if(m_bFaceCam)
						{
							tv_facecam_timer.setText(CommonFunc.msToTime(millis));
						}
						else
						{
							tv_timer.setText(CommonFunc.msToTime(millis));
						}
					}
				});
			}
		};

		mTimer = new Timer();

		mTimer.schedule(mTask, 500, 500);
	}

	public void endTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		
		if(mTask != null)
		{
			mTask.cancel();
			mTask = null;
		}
		
		if ( mCameraCircleView != null) {
			mCameraCircleView = null;
		}
	}

}
