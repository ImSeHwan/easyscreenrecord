package world.junseo.co.kr.easyscreenrecord.Common;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

public abstract class ThreadSafeHandler<T> extends Handler {
    private WeakReference<T> mReference;

    public ThreadSafeHandler(T reference) {
        mReference = new WeakReference<T>(reference);
    }

    @Override
    public void handleMessage(Message msg) {
        T reference = null;

        if (mReference != null) {
            reference = mReference.get();
        } else {
            reference = null;
        }

        if (reference == null) {
            return;
        }
        handleMessage(reference, msg);
    }

    protected abstract void handleMessage(T reference, Message msg);
}
