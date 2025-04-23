package world.junseo.co.kr.easyscreenrecord;

import android.app.Activity;
import android.app.Application;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import world.junseo.co.kr.easyscreenrecord.Common.CommonFunc;
import world.junseo.co.kr.easyscreenrecord.Common.CommonInfo;
import world.junseo.co.kr.easyscreenrecord.Prefs.SimplePrefs;

public class JWApplication extends Application {

    AppStatus mAppStatus;
    public AppStatus getAppStatus() {
        return mAppStatus;
    }

    private boolean m_bIsRecorded = false;
    public void setRecorded(boolean isRecorded) {
        m_bIsRecorded = isRecorded;
    }
    public boolean isRecorded() {
        return m_bIsRecorded;
    }

    public static MediaProjection mMediaProjection = null;

    public JWApplication() {
        appContext = this;
    }

    public enum RecordStatus { IDLE, RUN }

    private RecordStatus mRecordStatus = RecordStatus.IDLE;
    public void setRecordStatus(RecordStatus recordStatus) {
        mRecordStatus = recordStatus;
    }
    public RecordStatus getRecordStatus() {
        return mRecordStatus;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mSimplePrefs = new SimplePrefs(this, CommonInfo.PACKAGE_NAME);

        registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());

    }

    private static JWApplication appContext;
    public static JWApplication getAppContext() {
        return appContext;
    }

    private SimplePrefs mSimplePrefs = null;
    public SimplePrefs getSimplePrefs() { return mSimplePrefs; }

    public enum AppStatus {
        BACKGROUND, // app is background
        RETURNED_TO_FOREGROUND, // app returned to foreground(or first launch)
        FOREGROUND; // app is foreground
    }

    public class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        // running activity count
        private int running = 0;

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (++running == 1) {
// running activity is 1,
// app must be returned from background just now (or first launch)
                mAppStatus = AppStatus.RETURNED_TO_FOREGROUND;
                //Toast.makeText(getAppContext(), "RETURNED_TO_FOREGROUND" ,Toast.LENGTH_LONG).show();
            } else if (running > 1) {
// 2 or more running activities,
// should be foreground already.
                mAppStatus = AppStatus.FOREGROUND;
                //Toast.makeText(getAppContext(), "FOREGROUND" ,Toast.LENGTH_LONG).show();
            }

            if(mRecordStatus != RecordStatus.RUN) {
                CommonFunc.hideRecordButton(JWApplication.getAppContext());
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (--running == 0) {
// no active activity
// app goes to background
                mAppStatus = AppStatus.BACKGROUND;
                //Toast.makeText(getAppContext(), "BACKGROUND" ,Toast.LENGTH_LONG).show();
                boolean bIsStay = JWApplication.getAppContext().getSimplePrefs().get(CommonInfo.PREF.SERVICE_EXECUTE_ALWAYS, true);
                /**
                 * 앱을 벗어나면 플로팅을 실행해주자
                 * 서비스 항상 실행일 경우만 실행한다.
                 */
                if(bIsStay) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                            && Settings.canDrawOverlays(getAppContext())) {
                        CommonFunc.showRecordButton(JWApplication.getAppContext());
                    }
                } else {
                    if(JWApplication.getAppContext().getRecordStatus() != JWApplication.RecordStatus.RUN) {
                        CommonFunc.stopRecordService(JWApplication.getAppContext());
                    }
                }
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    }
}
