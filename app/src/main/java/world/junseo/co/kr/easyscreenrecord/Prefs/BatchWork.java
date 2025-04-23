package world.junseo.co.kr.easyscreenrecord.Prefs;

/**
 * An interface to chain calls to {@link SimplePrefs} set()
 * without applying to {@link android.content.SharedPreferences.Editor}.
 * After you are done with setting values do not forget to call {@link #apply()}
 *
 * Created by Dimitry Ivanov on 08.05.2015.
 */
public interface BatchWork {
    BatchWork set(String key, String value);
    BatchWork set(String key, int value);
    BatchWork set(String key, long value);
    BatchWork set(String key, float value);
    BatchWork set(String key, boolean value);

    void apply();
}
