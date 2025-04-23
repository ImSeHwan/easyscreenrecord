package world.junseo.co.kr.easyscreenrecord;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
//import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import world.junseo.co.kr.easyscreenrecord.Common.CommonFunc;
import world.junseo.co.kr.easyscreenrecord.Common.CommonInfo;
import world.junseo.co.kr.easyscreenrecord.Fragment.RecordListFragment;
import world.junseo.co.kr.easyscreenrecord.Fragment.SettingsFragment;
import world.junseo.co.kr.easyscreenrecord.Util.Log.JLog;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class MainActivity extends AppCompatActivity{

    FrameLayout fl_container;
    //private ImageView iv_record_start;
    BottomNavigationView navView;

    String TAG = "MainActivity";

    int CURRENT_MENU_ID = -1;

    //int RECORD_STATUS = 0;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int viewId = item.getItemId();

            if(viewId == R.id.item_saved_list) {
                changeMenu(CommonInfo.MenuID.RECORD_LIST);
                return true;
            } else if(viewId == R.id.item_settings) {
                changeMenu(CommonInfo.MenuID.RECORD_SETTINGS);
//              return true;
            }
//            switch (item.getItemId()) {
//                case R.id.item_saved_list:
//                    changeMenu(CommonInfo.MenuID.RECORD_LIST);
//                    return true;
//                case R.id.item_settings:
//                    changeMenu(CommonInfo.MenuID.RECORD_SETTINGS);
//                    return true;
//            }
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_record, menu);

        try {
            //Animation startAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink_animation);
            View recordLayout = menu.findItem(R.id.item_screen_record).getActionView();
            //recordLayout.startAnimation(startAnimation);

            recordLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                            && Settings.canDrawOverlays(MainActivity.this)) {
                        if (JWApplication.getAppContext().getRecordStatus() != JWApplication.RecordStatus.RUN) {
                            JWApplication.getAppContext().setRecordStatus(JWApplication.RecordStatus.RUN);
                            MainActivity.this.moveTaskToBack(true);
                            CommonFunc.startRecord(MainActivity.this);
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.info_message7), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        //Toast.makeText(MainActivity.this, getString(R.string.record_service_information3), Toast.LENGTH_LONG).show();
                        CommonFunc.checkOverlay(MainActivity.this);
                    }
                }
            });
        } catch (Exception e) {
            JLog.e(TAG, e.toString());
        }

        return true;
    }

/*    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_screen_record:
                if(JWApplication.getAppContext().getRecordStatus() != JWApplication.RecordStatus.RUN) {
                JWApplication.getAppContext().setRecordStatus(JWApplication.RecordStatus.RUN);
                moveTaskToBack(true);
                CommonFunc.startRecord(this);
                } else {
                    Toast.makeText(this, getString(R.string.info_message7) ,Toast.LENGTH_LONG).show();
                }
                return true ;
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this);

        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fl_container = findViewById(R.id.fl_container);

        //iv_record_start = findViewById(R.id.iv_record_start);
        //iv_record_start.setOnClickListener(this);

        //registerMainActivityReceiver();

        int nMenuIndex = 0;
        Intent intent = getIntent();

        if(intent != null) {
            nMenuIndex = intent.getIntExtra(CommonInfo.MENU_ID, 0);
        }

        // 저장공간 권한을 체크한다.
        writeStorageCheck(nMenuIndex);
    }

    private void changeMenu(int idx) {
        if(CURRENT_MENU_ID != idx) {
            switch (idx) {
                case CommonInfo.MenuID.RECORD_LIST:
                    replaceFragment(new RecordListFragment());
                    break;
                case CommonInfo.MenuID.RECORD_SETTINGS:
                    replaceFragment(new SettingsFragment());
                    break;
            }

            CURRENT_MENU_ID = idx;
        }
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fl_container, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commitAllowingStateLoss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //unregisterMainActivityReceiver();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CommonInfo.START_ACTIVITY_RESULT_CODE.CHECK_DRAW_OVERLAY:
                if( !Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, getString(R.string.record_service_information3), Toast.LENGTH_LONG).show();
                } else {
                    CommonFunc.startRecordService(this);
                }
                break;
        }
    }

    public class PermissionListenerEx implements PermissionListener {

        int m_nMenuID = 0;

        public PermissionListenerEx(int menuID) {
            m_nMenuID = menuID;
        }

        @Override
        public void onPermissionGranted() {
            //오버레이 기능을 체크하자
            if(CommonFunc.checkOverlay(MainActivity.this)) {
                CommonFunc.startRecordService(MainActivity.this);
            }

            changeMenu(m_nMenuID);
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            Toast.makeText(MainActivity.this, getString(R.string.info_message4), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    PermissionListenerEx mPermissionListenerEx;

    /*PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {

        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            Toast.makeText(MainActivity.this, getString(R.string.info_message4), Toast.LENGTH_LONG).show();
            finish();
        }
    };*/

    public void writeStorageCheck(int menuID) {
        ArrayList<String> permissions = new ArrayList<>(Arrays.asList(WRITE_EXTERNAL_STORAGE));

        if (!CommonFunc.checkPermission(this, permissions)) {
            try {
                mPermissionListenerEx = new PermissionListenerEx(menuID);

                TedPermission.create()
                        .setPermissionListener(mPermissionListenerEx)
                        .setDeniedMessage(getString(R.string.info_message3))
                        .setPermissions(WRITE_EXTERNAL_STORAGE)
                        .check();
//                TedPermission.with(this)
//                        .setPermissionListener(mPermissionListenerEx)
//                        .setDeniedMessage(getString(R.string.info_message3))
//                        .setPermissions(WRITE_EXTERNAL_STORAGE)
//                        .check();
            } catch (Exception e2) {
                JLog.e(TAG, e2.toString());
            }
        } else {
            //오버레이 기능을 체크하자
            if(CommonFunc.checkOverlay(MainActivity.this)) {
                CommonFunc.startRecordService(MainActivity.this);
            }

            changeMenu(menuID);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(intent != null) {

            switch (intent.getIntExtra(CommonInfo.MENU_ID, 0)) {
                case CommonInfo.MenuID.RECORD_LIST:
                    navView.setSelectedItemId(R.id.item_saved_list);
                    break;
                case CommonInfo.MenuID.RECORD_SETTINGS:
                    navView.setSelectedItemId(R.id.item_settings);
                    break;
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * 레코딩 버튼을 숨긴다.
         * 앱 내 에서는 해당 플로팅이 필요없다.
         */
        CommonFunc.hideRecordButton(this);
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        boolean bIsStay = JWApplication.getAppContext().getSimplePrefs().get(CommonInfo.PREF.SERVICE_EXECUTE_ALWAYS, false);
//        /**
//         * 앱을 벗어나면 플로팅을 실행해주자
//         * 서비스 항상 실행일 경우만 실행한다.
//         */
//        if(bIsStay) {
//            CommonFunc.showRecordButton(this);
//        }
//    }

//    @Override
//    public void onBackPressed() {
//        boolean bIsStay = JWApplication.getAppContext().getSimplePrefs().get(CommonInfo.PREF.SERVICE_EXECUTE_ALWAYS, false);
//
//        if(!bIsStay && RECORD_STATUS != 1) {
//            CommonFunc.stopRecordService(this);
//            super.onBackPressed();
//        } else {
//            moveTaskToBack(true);
//        }
//    }

//    private BroadcastReceiver mainAcitivtyBroadcastReceiver = new BroadcastReceiver() {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            String action = intent.getAction();
//
//            if (!CommonFunc.IsEmpty(action)) {
//                switch (action) {
//                    case CommonInfo.BroadcastID.START_RECORD:
//                        RECORD_STATUS = 1;
//                        break;
//                    case CommonInfo.BroadcastID.STOP_RECORD:
//                        RECORD_STATUS = 2;
//                        break;
//                    case CommonInfo.BroadcastID.APP_MOVE_BACKGROUD:
//                        JLog.d(TAG, "백그라운드로 이동");
//                        moveTaskToBack(true);
//                        break;
//                }
//            }
//        }
//    };

//    private void registerMainActivityReceiver() {
//        IntentFilter intentFilter = new IntentFilter(CommonInfo.BroadcastID.START_RECORD);
//        intentFilter.addAction(CommonInfo.BroadcastID.STOP_RECORD);
//        intentFilter.addAction(CommonInfo.BroadcastID.APP_MOVE_BACKGROUD);
//        registerReceiver(mainAcitivtyBroadcastReceiver, intentFilter);
//    }
//
//    private void unregisterMainActivityReceiver() {
//        unregisterReceiver(mainAcitivtyBroadcastReceiver);
//    }

/*    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_record_start:
                if(JWApplication.getAppContext().getRecordStatus() != JWApplication.RecordStatus.RUN) {
                    JWApplication.getAppContext().setRecordStatus(JWApplication.RecordStatus.RUN);
                    moveTaskToBack(true);
                    CommonFunc.startRecord(this);
                } else {
                    Toast.makeText(this, getString(R.string.info_message7) ,Toast.LENGTH_LONG).show();
                }
                break;
        }
    }*/
}
