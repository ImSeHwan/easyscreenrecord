package world.junseo.co.kr.easyscreenrecord;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import world.junseo.co.kr.easyscreenrecord.Common.CommonFunc;
import world.junseo.co.kr.easyscreenrecord.Common.CommonInfo;

public class VideoPlayerActivity extends Activity {
    VideoView video_player_view;
    DisplayMetrics dm;
    MediaController media_Controller;

    int stopPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        Intent intent = getIntent();
        String sPath = intent.getStringExtra("MP4_PATH");
        int nWidth = intent.getIntExtra("MP4_WIDTH", 0);
        int nHeight = intent.getIntExtra("MP4_HEIGHT", 0);

        if (CommonFunc.IsEmpty(sPath)) {
            Toast.makeText(this, "경로가 잘못되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            getInit(sPath, nWidth, nHeight);
        }

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    public void getInit(String sPath, int nWidth, int nHeight) {
        video_player_view = (VideoView) findViewById(R.id.video_player_view);
        media_Controller = new MediaController(this);

        video_player_view.setMinimumWidth(nWidth);
        video_player_view.setMinimumHeight(nHeight);
        video_player_view.setMediaController(media_Controller);
        video_player_view.setVideoPath(sPath);
        video_player_view.start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        video_player_view.seekTo(stopPosition);
        video_player_view.start();

        CommonFunc.hideRecordButton(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(video_player_view.isPlaying()) {
            stopPosition = video_player_view.getCurrentPosition();

            video_player_view.pause();
        }

/*        boolean bIsStay = JWApplication.getAppContext().getSimplePrefs().get(CommonInfo.PREF.SERVICE_EXECUTE_ALWAYS, true);
        *//**
         * 앱을 벗어나면 플로팅을 실행해주자
         * 서비스 항상 실행일 경우만 실행한다.
         **//*
        if(bIsStay) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && Settings.canDrawOverlays(this)) {
                CommonFunc.showRecordButton(this);
            }
        }*/
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(video_player_view.isPlaying()) {
            video_player_view.stopPlayback();
        }
    }
}
