package world.junseo.co.kr.easyscreenrecord.Fragment;

import android.os.Handler;
import android.os.Message;
import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;

public abstract class BaseFragment extends Fragment {
    String TAG = "BaseFragment";

    /*private RotateLoading rotateLoading;

    public void TransShowProgress() {
        try {
            if(!rotateLoading.isStart())
                rotateLoading.start();

        } catch (Exception e) {
            JLog.e(TAG, "rotateLoading=" + e);
        }

    }

    *//**
     * 프로그래스 바가 활성 상태이면 종료한다.
     *//*
    protected void DismissProgress() {
        try {
            if(rotateLoading.isStart())
                rotateLoading.stop();

        } catch (Exception e) {
            JLog.e(TAG, "DismissProgress=" + e);
        }
    }*/

    @Override
    public void onStop() {
        super.onStop();

        // 뷰가 종료될 때 프로그래스바가 있으면 제거하자
    }

    /**
     * 메모리 누수를 피하기 위해 약한 참조를 걸어둔다.
     */
    protected static class DataHandler extends Handler {
        private final WeakReference<BaseFragment> mFragment;

        DataHandler(BaseFragment baseFragment) {
            mFragment = new WeakReference<BaseFragment>(baseFragment);
        }
        @Override
        public void handleMessage(Message msg) {
            BaseFragment mBaseFragment = mFragment.get();

            if(mBaseFragment != null)
                mBaseFragment.handleMessageEx(msg);
        }
    }

    abstract protected void handleMessageEx(Message msg);
}
