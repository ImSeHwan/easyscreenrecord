package world.junseo.co.kr.easyscreenrecord.Layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import world.junseo.co.kr.easyscreenrecord.R;

public class RecordGroupLayout extends RelativeLayout implements View.OnTouchListener{

    public static final String LIST = "LIST";
    public static final String RECORD = "RECORD";
    public static final String CLOSE = "CLOSE";

    public interface OnRecordGroupLayoutInterface {
        void OnSelectControl(String controlName);
    }

    OnRecordGroupLayoutInterface mOnRecordGroupLayoutInterface;

    View mainView;
    ImageView iv_record_list, iv_record_start, iv_record_close;
    Context mContext;

    public RecordGroupLayout(Context context) {
        this(context, null);
    }

    public RecordGroupLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mainView = inflater.inflate(R.layout.recording_group_layout, this, true);

        iv_record_list = mainView.findViewById(R.id.iv_record_list);
        iv_record_list.setOnTouchListener(this);

        iv_record_start = mainView.findViewById(R.id.iv_record_start);
        iv_record_start.setOnTouchListener(this);

        iv_record_close = mainView.findViewById(R.id.iv_record_close);
        iv_record_close.setOnTouchListener(this);
    }

    public void setOnRecordGroupLayoutInterface(OnRecordGroupLayoutInterface onRecordGroupLayoutInterface) {
        mOnRecordGroupLayoutInterface = onRecordGroupLayoutInterface;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (view.getId()) {
            case R.id.iv_record_list:
                mOnRecordGroupLayoutInterface.OnSelectControl(LIST);
                break;
            case R.id.iv_record_start:
                mOnRecordGroupLayoutInterface.OnSelectControl(RECORD);
                break;
            case R.id.iv_record_close:
                mOnRecordGroupLayoutInterface.OnSelectControl(CLOSE);
                break;
        }
        return false;
    }
}
