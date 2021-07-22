package world.junseo.co.kr.easyscreenrecord.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import world.junseo.co.kr.easyscreenrecord.Common.CommonInfo;
import world.junseo.co.kr.easyscreenrecord.JWApplication;
import world.junseo.co.kr.easyscreenrecord.Model.RecordSet.RecordSize;
import world.junseo.co.kr.easyscreenrecord.R;

public class RecQualitySelectDialog extends Dialog implements View.OnClickListener{

	public interface OnResultListener {
		void resultQuality(int quality);
	}

	OnResultListener mOnResultListener;
	
	RelativeLayout lv_quality_normal, lv_quality_high;
	ImageView iv_close;
	
	// nFrameSetting는 프리퍼런스에 설정값을 넣어준다.
	int nFrameSetting = 0;
	// nResolutionSetting는 프리퍼런스에 배열 인덱스를 넣어준다.
	int nResolutionSetting = 0;
	// nQuality는 RecordSet의 BITRATE_DEFAULT를 사용(인덱스)
	int nQuality = 0;
	
	SharedPreferences myPrefs;
	Context mMainActivityContext = null;
	Fragment mRecSettingFragment = null;

	private ArrayList<RecordSize> screenSize = new ArrayList<RecordSize>();
	
	public RecQualitySelectDialog(Context context, OnResultListener onResultListener) {
		super(context);
		
		mMainActivityContext = context;

		mOnResultListener = onResultListener;
		
		// TODO Auto-generated constructor stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		setContentView(R.layout.dialog_rec_quality_setting_popup);
		
		lv_quality_normal = (RelativeLayout)findViewById(R.id.lv_quality_normal);
		lv_quality_normal.setOnClickListener(this);
		
		lv_quality_high = (RelativeLayout)findViewById(R.id.lv_quality_high);
		lv_quality_high.setOnClickListener(this);
		
		iv_close = (ImageView)findViewById(R.id.iv_close);
		iv_close.setOnClickListener(this);
	}

	// 퀄리티는 일반 : 1 고화질 0
	// recordset 클래스 참고
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.lv_quality_normal:
			JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.REC_QUALITY_SETTING, 1);
			if(mOnResultListener != null) {
				mOnResultListener.resultQuality(1); // 일반
			}
			dismiss();
			break;
		case R.id.lv_quality_high:
			JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.REC_QUALITY_SETTING, 0);
			if(mOnResultListener != null) {
				mOnResultListener.resultQuality(0); // 일반
			}
			dismiss();
			break;
		case R.id.iv_close:
			dismiss();
			break;
		default:
			break;
		}
	}

}
