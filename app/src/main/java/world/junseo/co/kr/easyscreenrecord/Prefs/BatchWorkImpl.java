package world.junseo.co.kr.easyscreenrecord.Prefs;

/**
 * Created by ish_test on 2016-06-27.
 */
public class BatchWorkImpl implements BatchWork{

    private final SimplePrefs mPref;

    BatchWorkImpl(SimplePrefs pref) {
        this.mPref = pref;
    }

    @Override
    public BatchWork set(String key, String value) {
        mPref.setBatch(key, value);
        return this;
    }

    @Override
    public BatchWork set(String key, int value) {
        mPref.setBatch(key, value);
        return this;
    }

    @Override
    public BatchWork set(String key, long value) {
        mPref.setBatch(key, value);
        return this;
    }

    @Override
    public BatchWork set(String key, float value) {
        mPref.setBatch(key, value);
        return this;
    }

    @Override
    public BatchWork set(String key, boolean value) {
        mPref.setBatch(key, value);
        return this;
    }

    @Override
    public void apply() {
        mPref.apply();
    }
}
