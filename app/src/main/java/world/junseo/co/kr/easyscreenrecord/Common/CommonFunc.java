package world.junseo.co.kr.easyscreenrecord.Common;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

import world.junseo.co.kr.easyscreenrecord.JWApplication;
import world.junseo.co.kr.easyscreenrecord.R;
import world.junseo.co.kr.easyscreenrecord.Service.RecordService;
import world.junseo.co.kr.easyscreenrecord.Util.Log.JLog;

public class CommonFunc {

    static String TAG = "CommonFunc";
    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    /**
     * DP를 픽셀로 컨버팅 한다.
     * @param context
     * @param dipValue
     * @return
     */
    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue,
                metrics);
    }

    // 서비스 관련

    /**
     * 포그라운드 서비스 시작
     * @param context
     */
    public static void startRecordService(Context context) {
        Intent intent = new Intent(context, RecordService.class);
        intent.setAction(RecordService.ACTION_START_FOREGROUND_SERVICE);
        context.startService(intent);
    }

    /**
     * 포그라운드 서비스 중지
     * @param context
     */
    public static void stopRecordService(Context context) {
        Intent intent = new Intent(context, RecordService.class);
        intent.setAction(RecordService.ACTION_STOP_FOREGROUND_SERVICE);
        context.startService(intent);
    }

    /**
     * 레코딩 버튼 보이기
     * @param context
     */
    public static void showRecordButton(Context context) {
        Intent intent = new Intent(context, RecordService.class);
        intent.setAction(RecordService.ACTION_PLAY);
        context.startService(intent);
    }

    /**
     * 레코딩 버튼 숨기기
     * @param context
     */
    public static void hideRecordButton(Context context) {
        Intent intent = new Intent(context, RecordService.class);
        intent.setAction(RecordService.ACTION_PAUSE);
        context.startService(intent);
    }

    /**
     * 스크린 레코드 시작
     * @param context
     */
    public static void startRecord(Context context) {
        Intent intent = new Intent(context, RecordService.class);
        intent.setAction(RecordService.ACTION_START_RECORD);
        context.startService(intent);
    }

    /**
     * 스크린 레코드 중지
     * @param context
     */
    public static void stopRecord(Context context) {
        Intent intent = new Intent(context, RecordService.class);
        intent.setAction(RecordService.ACTION_STOP_RECORD);
        context.startService(intent);
    }

    /**
     * 리소스 체크(로딩 가능한 리소스 ID인지 체크한다.
     * @param context
     * @param resId
     * @return
     */
    public static boolean isResource(Context context, int resId){
        if (context != null){
            try {
                return context.getResources().getResourceName(resId) != null;
            } catch (Resources.NotFoundException ignore) {
            }
        }
        return false;
    }

    /**
     * oreo 이상부터 다른 앱 위에 띄우는 플래그 값이 변경되었다
     * @return
     */
    public static int getApplicationOverlayFlag() {
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        return LAYOUT_FLAG;
    }

    public static boolean checkOverlay(@NonNull final Activity activity) {
        boolean bRet = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(activity)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(activity.getString(R.string.notice));
            builder.setMessage(R.string.record_service_information2);
            builder.setPositiveButton(activity.getString(R.string.confirm),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
                            activity.startActivityForResult(intent, CommonInfo.START_ACTIVITY_RESULT_CODE.CHECK_DRAW_OVERLAY);
                        }
                    });

            builder.setNegativeButton(activity.getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(activity, activity.getString(R.string.record_service_information3), Toast.LENGTH_LONG).show();
                        }
                    });

            builder.show();

        } else {
            bRet = true;
        }

        return bRet;
    }

    // 값이 없으면 true/ 있다면 false
    public static boolean IsEmpty(String sData) {
        boolean bRet = true;

        if (sData != null && sData.length() > 0)
            bRet = false;

        return bRet;
    }

    public static String getRecordPath() {
        //File file = Environment.getExternalStorageDirectory();
        File file = JWApplication.getAppContext().getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES);

        String dir = file.getAbsolutePath() + "/easyrecord/record";

        file = new File(dir);

        if (!file.exists()) {
            file.mkdirs();
        }

        return dir;
    }

    public static void callShardVideo(final Context context, final String path) {
        new Handler().post(new Runnable() {

            @Override
            public void run() {

                File file = new File(path);
                Uri uri = CommonFunc.getVideoContentUriFromFilePath(context, file);
                if (!CommonFunc.IsEmpty(uri.toString())) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, context.getString(R.string.video_file_sharing));
                    intent.setType("video/mp4");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    try {
                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.video_file_sharing_select_app)));
                    } catch (android.content.ActivityNotFoundException ex) {
                        JLog.e(TAG, ex.toString());
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.info_message6), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public static boolean checkWifiOnAndConnected(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            if( wifiInfo.getNetworkId() == -1 ){
                return false; // Not connected to an access point
            }
            return true; // Connected to an access point
        }
        else {
            return false; // Wi-Fi adapter is OFF
        }
    }

    public static String getRecordThumbPath() {
        //File file = Environment.getExternalStorageDirectory();
        File file = JWApplication.getAppContext().getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES);

        String dir = file.getAbsolutePath() + String.format(Locale.KOREA, "/easyrecord/thumb");

        file = new File(dir);

        if (!file.exists()) {
            file.mkdirs();
        }

        return dir;
    }

    // 편접된 Temp 폴더내의 모든 동영상 파일들을 삭제한다.
    public static void delVODTempFiles()
    {
        JLog.d(TAG,"Temp 폴더내의 모든 파일을 지운다..");
        //File file = Environment.getExternalStorageDirectory();
        File file = JWApplication.getAppContext().getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES);
        String dir = file.getAbsolutePath() + String.format(Locale.KOREA, "/easyrecord/TEMP");
        file = new File(dir);

        if (file.isDirectory()) {
            String[] children = file.list();
            for (int i = 0; i < children.length; i++) {
                new File(file, children[i]).delete();
            }
        }
    }

    /**
     * 섬네일 이미지 파일 삭제
     * @param filePath
     * @return
     */
    public static boolean imageFileDelete(String filePath){
        //filePath : 파일경로 및 파일명이 포함된 경로입니다.
        try {
            //확장자를 변경한다.
            String sImagePath = filePath.replace("mp4", "jpg");
            //경로를 변경한다.
            sImagePath = sImagePath.replace("/easyrecord/record", "/easyrecord/thumb");

            File file = new File(sImagePath);
            // 파일이 존재 하는지 체크
            if(file.exists()) {
                file.delete();
                return true;  // 파일 삭제 성공여부를 리턴값으로 반환해줄 수 도 있습니다.
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 파일 삭제
     * @param filePath
     * @return
     */
    public static boolean fileDelete(String filePath){
        //filePath : 파일경로 및 파일명이 포함된 경로입니다.
        try {
            File file = new File(filePath);
            // 파일이 존재 하는지 체크
            if(file.exists()) {
                file.delete();
                return true;  // 파일 삭제 성공여부를 리턴값으로 반환해줄 수 도 있습니다.
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 편집시 사용할 임시폴더 경로
     * @return
     */
    public static String getVODTempPath() {
        // File file = Environment
        // .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        //File file = Environment.getExternalStorageDirectory();
        File file = JWApplication.getAppContext().getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES);

        String dir = file.getAbsolutePath() + "/easyrecord/TEMP";

        file = new File(dir);

        if (!file.exists()) {
            file.mkdirs();
        }

        return dir;
    }

    public static String msToTime(long millis) {
        String returnvalue = "";
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) ((millis / (1000 * 60)) % 60);

        int hour = (int)((millis / (1000 * 60*60)) % 24);

        if(hour > 0)
            returnvalue = String.format(Locale.KOREA, "%01d:%02d:%02d", hour,minutes, seconds);
        else
            returnvalue = String.format(Locale.KOREA, "%02d:%02d", minutes, seconds);

        return returnvalue;
    }

    /**
     * 공유할 파일의 Uri를 가져온다.
     * @param context
     * @param fileToShare
     * @return
     */
    public static Uri getVideoContentUriFromFilePath(Context context, File fileToShare) {
        String filePath = fileToShare.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Video.Media._ID },
                MediaStore.Video.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            return Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (fileToShare.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Video.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    public static boolean checkPermission(Context context, @NonNull ArrayList<String> permissions) {
        boolean bRet = true;

        for(String permission:permissions) {
            if(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                bRet = false;
                break;
            }
        }

        return bRet;
    }

    /*
     * bitmap을 파일로 만든다.
     */
    public static void SaveBitmapToFileCache(Bitmap bitmap, String strFilePath) {

        if(bitmap == null)
        {
            JLog.e(TAG, "SaveBitmapToFileCache bitmap == null");
            return;
        }
        File fileCacheItem = new File(strFilePath);
        OutputStream out = null;

        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
