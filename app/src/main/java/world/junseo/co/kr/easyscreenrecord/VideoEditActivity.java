package world.junseo.co.kr.easyscreenrecord;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import world.junseo.co.kr.easyscreenrecord.Common.CommonFunc;
import world.junseo.co.kr.easyscreenrecord.Layout.JWVideoTrimmer;
import world.junseo.co.kr.easyscreenrecord.Util.Log.JLog;
import world.junseo.co.kr.easyscreenrecord.videoTrimmer.interfaces.OnJWVideoListener;
import world.junseo.co.kr.easyscreenrecord.videoTrimmer.interfaces.OnTrimVideoListener;

public class VideoEditActivity extends AppCompatActivity implements OnJWVideoListener, OnTrimVideoListener {

    private JWVideoTrimmer vt_layout;
    private ProgressDialog mProgressDialog;

    String TAG = "VideoEditActivity";

    private String makeFilePath() {
        try {
            long lCurrentDate = System.currentTimeMillis();
            Date date = new Date(lCurrentDate);
            SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
            String strCurDate = CurDateFormat.format(date);

            String sPath = getPackageName() + "_" + strCurDate;

            return CommonFunc.getRecordPath() + "/" + sPath + ".mp4";
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit);

        Intent extraIntent = getIntent();
        String path = "";
        int duration = 0;

        if (extraIntent != null) {
            path = extraIntent.getStringExtra("MP4_PATH");
            duration = extraIntent.getIntExtra("VIDEO_TOTAL_DURATION", 10);
        }

        //setting progressbar
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.trimming_progress));

        vt_layout = findViewById(R.id.vt_layout);
        if (vt_layout != null) {
            String editPath = makeFilePath();
            if(!CommonFunc.IsEmpty(editPath)) {
                vt_layout.setMaxDuration(duration);
                //vt_layout.setMinDuration(10);
                vt_layout.setDestinationPath(editPath);
                vt_layout.setVideoURI(Uri.parse(path));
                vt_layout.setVideoInformationVisibility(true);
                vt_layout.setOnJWVideoListener(this);
                vt_layout.setOnTrimVideoListener(this);
            }
        }
    }

    @Override
    public void onVideoPrepared() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Toast.makeText(TrimmerActivity.this, "onVideoPrepared", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onTrimStarted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.show();
            }
        });
    }

    @Override
    public void getResult(final Uri uri) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.cancel();

                JWApplication.getAppContext().setRecorded(true);

                String videoPath = uri.getPath();
                String sThumbImagePath = videoPath.replace(".mp4", ".jpg");
                sThumbImagePath = sThumbImagePath.replace("/easyrecord/record", "/easyrecord/thumb");

                // 썸네일을 만든후에 Image 파일명은 지우자
                if (!CommonFunc.IsEmpty(videoPath)) {
                    /*String sThumbImagePath = CommonFunc.getRecordThumbPath()
                            + "/" + m_sFileName + ".jpg";
                    String sMP4Path = CommonFunc.getRecordPath() + "/"
                            + m_sFileName + ".mp4";*/

                    JLog.d(TAG, "sThumbImagePath : " + sThumbImagePath
                            + " sMP4Path : " + videoPath);

                    try {

                        Bitmap thumbImage = ThumbnailUtils
                                .createVideoThumbnail(videoPath,
                                        MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
                        if (thumbImage != null) {
                            CommonFunc.SaveBitmapToFileCache(thumbImage,
                                    sThumbImagePath);
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                        JLog.d(TAG, e.toString());
                    }
                }
                /*Toast.makeText(VideoEditActivity.this, getString(R.string.video_saved_at, uri.getPath()), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setDataAndType(uri, "video/mp4");
                startActivity(intent);*/
                finish();
            }
        });
    }

    @Override
    public void cancelAction() {
        mProgressDialog.cancel();
        vt_layout.destroy();
        finish();
    }

    @Override
    public void onError(String message) {
        mProgressDialog.cancel();

        Toast.makeText(VideoEditActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
