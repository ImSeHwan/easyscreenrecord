package world.junseo.co.kr.easyscreenrecord;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;

import world.junseo.co.kr.easyscreenrecord.Common.CommonFunc;
import world.junseo.co.kr.easyscreenrecord.Service.SimpleScreenRecorder;
import world.junseo.co.kr.easyscreenrecord.Util.Log.JLog;

public class RecordingResultActivity extends Activity {

	private static final int REQUEST_CODE = 1;
	private MediaProjectionManager mMediaProjectionManager;
	String TAG = "RecordingWithAPI21LaterActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_result);
		
		mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
		
        Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		//moveTaskToBack(true);
		if(resultCode == RESULT_OK)
		{
			if(requestCode == REQUEST_CODE)
			{
				//SimpleScreenRecorder.getInstance().setMediaProjection(mMediaProjectionManager.getMediaProjection(resultCode, data));
				//JWApplication.mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);

				MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
				if (mediaProjection == null) {
					JLog.e(TAG, "media projection is null");
				}
				else
				{
					SimpleScreenRecorder.getInstance().setMediaProjection(mediaProjection);
					CommonFunc.startRecord(this);
				}
			}
		}
		else
		{
			JLog.d(TAG, "get MediaProjectionManager fail");
		}	
		
		finish();
	}
}
