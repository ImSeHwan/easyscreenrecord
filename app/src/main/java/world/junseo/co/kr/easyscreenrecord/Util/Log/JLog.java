package world.junseo.co.kr.easyscreenrecord.Util.Log;

import android.util.Log;

import world.junseo.co.kr.easyscreenrecord.BuildConfig;

/**
 * 사용 로그
 */
public class JLog {

	public static void d(String tag, String msg) {
		if (BuildConfig.LOG) {
			Log.d(tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (BuildConfig.LOG) {
			Log.i(tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (BuildConfig.LOG) {
			Log.e(tag, msg);
		}
	}

	public static void v(String tag, String msg) {
		if (BuildConfig.LOG) {
			Log.v(tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (BuildConfig.LOG) {
			Log.w(tag, msg);
		}
	}
}
