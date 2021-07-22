package world.junseo.co.kr.easyscreenrecord.Fragment;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.victor.loading.rotate.RotateLoading;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import world.junseo.co.kr.easyscreenrecord.Adapter.RecordInfoAdapter;
import world.junseo.co.kr.easyscreenrecord.BuildConfig;
import world.junseo.co.kr.easyscreenrecord.Common.CommonFunc;
import world.junseo.co.kr.easyscreenrecord.Common.CommonInfo;
import world.junseo.co.kr.easyscreenrecord.Interface.RecyclerItemListener;
import world.junseo.co.kr.easyscreenrecord.JWApplication;
import world.junseo.co.kr.easyscreenrecord.Model.RecordInfo;
import world.junseo.co.kr.easyscreenrecord.R;
import world.junseo.co.kr.easyscreenrecord.Util.Log.JLog;

public class RecordListFragment extends BaseFragment implements RecyclerItemListener {

    // Admob
    private InterstitialAd mInterstitialAd;

    View mainView = null;

    RecyclerView rv_record_list;

    String TAG = "RecordListFragment";

    DataHandler mDataHandler;

    ImageView iv_up_arrow;
    ConstraintLayout cl_empty_infomation;
    Animation connectingAnimation;

    private ArrayList<RecordInfo> mRecordInfos = new ArrayList<>();
    private RecordInfoAdapter mRecordInfoAdapter;

    private RotateLoading rotateLoading;

    // 첫 실행을 체크하여 리스트가 한번은 갱신할 수 있도록 한다.
    private boolean m_isFirst = true;

    public void TransShowProgress() {
        try {
            if(!rotateLoading.isStart())
                rotateLoading.start();

        } catch (Exception e) {
            JLog.e(TAG, "rotateLoading=" + e);
        }

    }

    /**
     * 프로그래스 바가 활성 상태이면 종료한다.
     */
    protected void DismissProgress() {
        try {
            if(rotateLoading.isStart())
                rotateLoading.stop();

        } catch (Exception e) {
            JLog.e(TAG, "DismissProgress=" + e);
        }
    }

    public static RecordListFragment newInstance() {
        RecordListFragment fragment = new RecordListFragment();

        return fragment;
    }

    @Override
    public void showAdMob(int itemIdx, int resourceID) {
        mAdListenerEx.setItemIdx(itemIdx, resourceID);

        requestAdmob();
    }

    public class AdListenerEx extends AdListener {

        int m_nItemIdx = -1;
        int m_nResourceID = 0;

        public void setItemIdx(int itemIdx, int resourceID) {
            m_nItemIdx = itemIdx;
            m_nResourceID = resourceID;
        }
        public int getItemIdx() {
            return m_nItemIdx;
        }

        @Override
        public void onAdClosed() {
            super.onAdClosed();

            // 광고가 또 뜨지 않도록 초기화
            JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.RC, false);

            if(m_nItemIdx > -1) {
                // 광고시청 후 자동으로 터치 처리를 위한 코드
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        //rv_record_list.findViewHolderForAdapterPosition(m_nItemIdx).itemView.performClick();
                        if(m_nResourceID > 0) {
                            rv_record_list.findViewHolderForAdapterPosition(m_nItemIdx).itemView.findViewById(m_nResourceID).performClick();
                            m_nResourceID = 0;
                        }
                        m_nItemIdx = -1;
                    }
                });
            }
        }

        @Override
        public void onAdFailedToLoad(int i) {
            super.onAdFailedToLoad(i);

            JLog.d(TAG, "광고 로딩 실패 : " + i);

            // 실패해도 기능은 유지되도록한다.
            // 광고가 또 뜨지 않도록 초기화
            JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.RC, false);
            JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.RC_IS_FAIL, true);

            if(m_nItemIdx > -1) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        //rv_record_list.findViewHolderForAdapterPosition(m_nItemIdx).itemView.performClick();
                        if(m_nResourceID > 0) {
                            rv_record_list.findViewHolderForAdapterPosition(m_nItemIdx).itemView.findViewById(m_nResourceID).performClick();
                            m_nResourceID = 0;
                        }
                        m_nItemIdx = -1;
                    }
                });
            }
        }

        @Override
        public void onAdLoaded() {
            super.onAdLoaded();

            JLog.d(TAG, "광고 로딩 성공");

            JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.RC_IS_FAIL, false);
            mInterstitialAd.show();
        }
    }

    AdListenerEx mAdListenerEx = new AdListenerEx();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDataHandler = new DataHandler(this);
        mRecordInfoAdapter = new RecordInfoAdapter(getContext(), this);

        mInterstitialAd = new InterstitialAd(getContext());
        mInterstitialAd.setAdUnitId(getString(R.string.ad_unitId));

        mInterstitialAd.setAdListener(mAdListenerEx);


        /*mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                JLog.d(TAG, "onAdLoaded()");
                mInterstitialAd.show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                JLog.d(TAG, "onAdFailedToLoad() with error code: " + errorCode);
            }

            @Override
            public void onAdClosed() {
                JLog.d(TAG, "onAdClosed");
            }
        });*/
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        JWApplication.getAppContext().setRecorded(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mainView = inflater.inflate(R.layout.fragment_record_list, container, false);

        iv_up_arrow = mainView.findViewById(R.id.iv_up_arrow);
        cl_empty_infomation = mainView.findViewById(R.id.cl_empty_infomation);

        rv_record_list = mainView.findViewById(R.id.rv_record_list);
        rv_record_list.setNestedScrollingEnabled(false);
        rv_record_list.setHasFixedSize(true);
        //rv_record_list.setItemViewCacheSize(20);
        rv_record_list.setDrawingCacheEnabled(true);
        rv_record_list.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rv_record_list.setLayoutManager(linearLayoutManager);

        mRecordInfoAdapter.setRecordInfo(mRecordInfos);
        rv_record_list.setAdapter(mRecordInfoAdapter);
        //rv_record_list.getRecycledViewPool().setMaxRecycledViews(0, 10);

        rotateLoading = mainView.findViewById(R.id.rotateloading);

        return mainView;
    }

    public void requestAdmob() {
        // 레코딩이 이루어져야만 광고를 보여준다.
        AdRequest.Builder request = new AdRequest.Builder();
        /*if (BuildConfig.DEBUG) {
            JLog.d(TAG, "Admob DEBUG");
            //테스트 기기 등록
            request.addTestDevice("FCF1590CC148596E6882F65D28549DF8");
        }*/

        mInterstitialAd.loadAd(request.build());
    }

    @Override
    public void onResume() {
        super.onResume();

        // 레코딩이 완료된 적이 있다면 갱신하자
        if(JWApplication.getAppContext().isRecorded() || m_isFirst) {
            // 영상 리스트 로딩
            new getRecordList().execute();

            JWApplication.getAppContext().setRecorded(false);
            m_isFirst = false;
        }
    }

    @Override
    protected void handleMessageEx(Message msg) {
        switch (msg.what) {

        }
    }

    public class getRecordList extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            mRecordInfos.clear();

            TransShowProgress();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            loadRecordList();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            mRecordInfoAdapter.setRecordInfo(mRecordInfos);
            mRecordInfoAdapter.notifyDataSetChanged();

            boolean isFirst = JWApplication.getAppContext().getSimplePrefs().get(CommonInfo.PREF.FIRST_TIME, true);

            if(mRecordInfos.size() > 0 || !isFirst) {
                if(connectingAnimation != null && connectingAnimation.hasStarted())
                    connectingAnimation.cancel();

                rv_record_list.setVisibility(View.VISIBLE);
                cl_empty_infomation.setVisibility(View.GONE);
            } else {
                JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.FIRST_TIME, false);

                rv_record_list.setVisibility(View.GONE);
                cl_empty_infomation.setVisibility(View.VISIBLE);

                ObjectAnimator anim = ObjectAnimator.ofFloat(iv_up_arrow,"y",80, 0);
                anim.setInterpolator(new LinearInterpolator());
                anim.setDuration(1000); // duration 5 seconds
                anim.setRepeatMode(ValueAnimator.REVERSE);
                anim.setRepeatCount(ValueAnimator.INFINITE);
                anim.start();
            }

            DismissProgress();
        }
    }

    public void loadRecordList() {
        try {
            FilenameFilter fileFilter = new FilenameFilter() // 특정 확장자만 가져올 때 사용
            {
                public boolean accept(File dir, String name) {
                    return name.endsWith("mp4"); // 필터로 적용할 확장자
                }
            };

            File file = new File(CommonFunc.getRecordPath());
            File[] files = file.listFiles(fileFilter); // 필터 적용

            // 영상 파일 날짜순으로 정렬
            Arrays.sort(files, new Comparator<File>(){
                    public int compare(File f1, File f2)
                    {
                        return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
                    }
                }
            );

            for (int i = 0; i < files.length; i++) {
                getVODInfo(files[i]);
            }

        } catch (Exception e) {
            JLog.e(TAG, e.toString());
        }
    }

    private void getVODInfo(File file) {
        RecordInfo recordInfo = new RecordInfo();

        String dateFormat = "yyyy.MM.dd HH:mm:ss";
        String sFileDate = DateFormat.format(dateFormat, new Date(file.lastModified())).toString();

        MediaPlayer mp = null;
        Uri uri = Uri.fromFile(file);
        try {
            mp = MediaPlayer.create(getContext(), uri);
            //mp.release();
        } catch (Exception e) {
            JLog.e("MediaPlayer", "can NOT play: " + uri);
            if (file.exists())
                file.delete();

            mp.release();
            return;
        }

        if(mp == null)
        {
            JLog.e("MediaPlayer", "MediaPlayer is null");
            if (file.exists())
                file.delete();
            return;
        }

        try {
            recordInfo.setVideoPath(file.getAbsolutePath());
            recordInfo.setCreateDate(sFileDate);
            recordInfo.setFileSize(file.length());
            recordInfo.setHeight(mp.getVideoHeight());
            recordInfo.setWidth(mp.getVideoWidth());
            recordInfo.setPlayTime(mp.getDuration());

            String sImageThumbPath = file.getAbsolutePath().replace(".mp4", ".jpg");
            sImageThumbPath = sImageThumbPath.replace("/easyrecord/record", "/easyrecord/thumb");

            recordInfo.setThumbPath(sImageThumbPath);

            mRecordInfos.add(recordInfo);

            mp.release();
        } catch (Exception e) {
            // TODO: handle exception
            JLog.e(TAG, e.toString());
            if (file.exists())
                file.delete();

            mp.release();
            return;
        }

    }
}
