package world.junseo.co.kr.easyscreenrecord.Fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import world.junseo.co.kr.easyscreenrecord.Common.CommonFunc;
import world.junseo.co.kr.easyscreenrecord.Common.CommonInfo;
import world.junseo.co.kr.easyscreenrecord.Common.CounterHandler;
import world.junseo.co.kr.easyscreenrecord.Dialog.RecQualitySelectDialog;
import world.junseo.co.kr.easyscreenrecord.JWApplication;
import world.junseo.co.kr.easyscreenrecord.R;
import world.junseo.co.kr.easyscreenrecord.Util.Log.JLog;

public class SettingsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener,
    CounterHandler.CounterListener{
    @Override
    public void onIncrement(View view, long number) {
        setPreviewCamSize((int)number);
    }

    @Override
    public void onDecrement(View view, long number) {
        setPreviewCamSize((int)number);
    }

    View mainView = null;

    int CAM_MAX_SIZE = 135;
    int CAM_MIN_SIZE = 57;

    String TAG = "SettingsFragment";

    // 영상 품질 선택 다이얼로그
    RecQualitySelectDialog mRecQualitySelectDialog = null;

    ImageView iv_preview_image;
    SwitchCompat sc_service_always_execute_swich;
    SwitchCompat sc_face_cam_swich;
    SwitchCompat sc_voice_rec_swich;

    RadioGroup rg_rec_quality_selector;
    RadioButton rb_record_quality_low, rb_record_quality_general;

    AdView adViewBanner;

    ImageView iv_up_size, iv_down_size;

    //SeekBar sb_cam_size;

    final int MIN_BUTTON_VALUE = 0;
    final int MAX_BUTTON_VALUE = 100;

    int nQualityIndex = 0; // 0 : 저화질, 1 : 고화질

    //float fFaceCamSize = 0;
    int nProgressPercent = 0;

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    AdListener mAdListener = new AdListener() {
        @Override
        public void onAdClosed() {
            super.onAdClosed();

            JLog.d(TAG, "onAdClosed");
        }

        @Override
        public void onAdFailedToLoad(int i) {
            super.onAdFailedToLoad(i);

            JLog.d(TAG, "onAdFailedToLoad : " + i);
        }

        @Override
        public void onAdLeftApplication() {
            super.onAdLeftApplication();

            JLog.d(TAG, "onAdLeftApplication");
        }

        @Override
        public void onAdOpened() {
            super.onAdOpened();
            JLog.d(TAG, "onAdOpened");
        }

        @Override
        public void onAdLoaded() {
            super.onAdLoaded();
            JLog.d(TAG, "onAdLoaded");
        }

        @Override
        public void onAdClicked() {
            super.onAdClicked();
        }

        @Override
        public void onAdImpression() {
            super.onAdImpression();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mainView = inflater.inflate(R.layout.fragment_settings, container, false);

        adViewBanner = mainView.findViewById(R.id.adViewBanner);
        adViewBanner.setAdListener(mAdListener);

        AdRequest adRequest = new AdRequest.Builder().build();
        adViewBanner.loadAd(adRequest);
        /*adViewBanner.setAdListener(mAdListener);

        AdRequest.Builder request = new AdRequest.Builder();
        if (BuildConfig.DEBUG) {
            JLog.d(TAG, "Admob DEBUG");
            //테스트 기기 등록
            request.addTestDevice("FCF1590CC148596E6882F65D28549DF8");
        }

        adViewBanner.loadAd(request.build());*/

        iv_up_size = mainView.findViewById(R.id.iv_up_size);
        iv_down_size = mainView.findViewById(R.id.iv_down_size);

        iv_preview_image = mainView.findViewById(R.id.iv_preview_image);
        iv_preview_image.setImageResource(R.drawable.setting_facecam_preview_all);

        //fFaceCamSize = JWApplication.getAppContext().getSimplePrefs().get(CommonInfo.PREF.FACECAM_SETTING_SIZE, 0f);
        nProgressPercent = JWApplication.getAppContext().getSimplePrefs().get(CommonInfo.PREF.FACECAM_SETTING_PERCENT, 50);
        //sb_cam_size.setProgress(nProgressPercent);

        new CounterHandler.Builder()
                .startNumber(nProgressPercent)
                .incrementalView(iv_up_size)
                .decrementalView(iv_down_size)
                .minRange(MIN_BUTTON_VALUE) // cant go any less than -50
                .maxRange(MAX_BUTTON_VALUE) // cant go any further than 50
                .isCycle(false) // 49,50,-50,-49 and so on
                .counterDelay(20) // speed of counter
                .counterStep(2)  // steps e.g. 0,2,4,6...
                .listener(this) // to listen counter results and show them in app
                .build();

        rg_rec_quality_selector = mainView.findViewById(R.id.rg_rec_quality_selector);
        rg_rec_quality_selector.setOnCheckedChangeListener(this);

        rb_record_quality_general = mainView.findViewById(R.id.rb_record_quality_general);
        rb_record_quality_low = mainView.findViewById(R.id.rb_record_quality_low);

        sc_service_always_execute_swich = mainView.findViewById(R.id.sc_service_always_execute_swich);
        sc_service_always_execute_swich.setOnCheckedChangeListener(this);

        sc_face_cam_swich = mainView.findViewById(R.id.sc_face_cam_swich);
        sc_face_cam_swich.setOnCheckedChangeListener(this);

        sc_voice_rec_swich = mainView.findViewById(R.id.sc_voice_rec_swich);
        sc_voice_rec_swich.setOnCheckedChangeListener(this);

        //sb_cam_size = mainView.findViewById(R.id.sb_cam_size);
//        sb_cam_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//
//            int progressChanged = 0;
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                // TODO Auto-generated method stub
//                JLog.i("FaceCamDetailSettingActivity", "onStopTrackingTouch");
//                setFaceCamSize(seekBar.getProgress());
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//                // TODO Auto-generated method stub
//
//            }
//
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress,
//                                          boolean fromUser) {
//                // TODO Auto-generated method stub
//                setPreviewSize(progress);
//                progressChanged = progress;
//            }
//        });

        nQualityIndex = JWApplication.getAppContext().getSimplePrefs().get(CommonInfo.PREF.REC_QUALITY_SETTING, 0); // 디폴트는 저화질
        //고화질
        if(nQualityIndex == 0)
        {
            //tv_quality_selector_contents.setText(getString(R.string.record_quality_general));
            rb_record_quality_general.setChecked(true);
        }
        //저화질
        else {
            //tv_quality_selector_contents.setText(getString(R.string.record_quality_low));
            rb_record_quality_low.setChecked(true);
        }


        boolean bIsOn = JWApplication.getAppContext().getSimplePrefs().get(CommonInfo.PREF.REC_FACE_SETTING, true);
        sc_face_cam_swich.setChecked(bIsOn);

        boolean bIsStay = JWApplication.getAppContext().getSimplePrefs().get(CommonInfo.PREF.SERVICE_EXECUTE_ALWAYS, true);
        sc_service_always_execute_swich.setChecked(bIsStay);

        boolean bIsUseMIC = JWApplication.getAppContext().getSimplePrefs().get(CommonInfo.PREF.REC_VOICE_SETTING, true);
        sc_voice_rec_swich.setChecked(bIsUseMIC);

        return mainView;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        int viewID = compoundButton.getId();

        if(viewID == R.id.sc_service_always_execute_swich) {
            JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.SERVICE_EXECUTE_ALWAYS, isChecked);
        } else if(viewID == R.id.sc_face_cam_swich) {
            JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.REC_FACE_SETTING, isChecked);
        }  else if(viewID == R.id.sc_voice_rec_swich) {
            JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.REC_VOICE_SETTING, isChecked);
        }
        /*switch (compoundButton.getId()) {
            case R.id.sc_service_always_execute_swich:
                JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.SERVICE_EXECUTE_ALWAYS, isChecked);
                break;
            case R.id.sc_face_cam_swich:
                JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.REC_FACE_SETTING, isChecked);
                break;
            case R.id.sc_voice_rec_swich:
                JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.REC_VOICE_SETTING, isChecked);
                break;
        }*/
    }

    private void setPreviewCamSize(int nProgress) {
        float fDPSize = nProgress * (CAM_MAX_SIZE - CAM_MIN_SIZE) / 100 + CAM_MIN_SIZE;

        if(fDPSize > CAM_MAX_SIZE)
            fDPSize = CAM_MAX_SIZE;

        if(fDPSize < CAM_MIN_SIZE)
            fDPSize = CAM_MIN_SIZE;

        //float fPixelSize = CommFunction.convertDpToPixel(fDPSize, FaceCamDetailSettingActivity.this);
        float fPixelSize = CommonFunc.dipToPixels(getContext(), fDPSize);

        JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.FACECAM_SETTING_SIZE, fPixelSize);
        JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.FACECAM_SETTING_PERCENT, nProgress);

        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) iv_preview_image.getLayoutParams();

        params.width = (int) fPixelSize;
        params.height = (int) fPixelSize;

        iv_preview_image.setLayoutParams(params);
    }

    private void setFaceCamSize(int nProgress)
    {
        float fDPSize = nProgress * (CAM_MAX_SIZE - CAM_MIN_SIZE) / 100 + CAM_MIN_SIZE;

        if(fDPSize > CAM_MAX_SIZE)
            fDPSize = CAM_MAX_SIZE;

        if(fDPSize < CAM_MIN_SIZE)
            fDPSize = CAM_MIN_SIZE;

        float fPixelSize = CommonFunc.convertDpToPixel(fDPSize, getContext());

        JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.FACECAM_SETTING_SIZE, fPixelSize);
        JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.FACECAM_SETTING_PERCENT, nProgress);
    }

    private void setPreviewSize(int nNewProgress)
    {
        JLog.d("FaceCamDetailSettingActivity", "nNewProgress : " + nNewProgress);

        float fDPSize = nNewProgress * (CAM_MAX_SIZE - CAM_MIN_SIZE) / 100 + CAM_MIN_SIZE;

        if(fDPSize > CAM_MAX_SIZE)
            fDPSize = CAM_MAX_SIZE;

        if(fDPSize < CAM_MIN_SIZE)
            fDPSize = CAM_MIN_SIZE;

        //float fPixelSize = CommFunction.convertDpToPixel(fDPSize, FaceCamDetailSettingActivity.this);
        float fPixelSize = CommonFunc.dipToPixels(getContext(), fDPSize);
        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) iv_preview_image.getLayoutParams();

        params.width = (int) fPixelSize;
        params.height = (int) fPixelSize;

        iv_preview_image.setLayoutParams(params);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        if(i == R.id.rb_record_quality_general) {
            JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.REC_QUALITY_SETTING, 0);
        } else if(i == R.id.rb_record_quality_low) {
            JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.REC_QUALITY_SETTING, 1);
        }
        /*switch (i) {
            case R.id.rb_record_quality_general:
                JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.REC_QUALITY_SETTING, 0);
                break;
            case R.id.rb_record_quality_low:
                JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.REC_QUALITY_SETTING, 1);
                break;
        }*/
    }
}
