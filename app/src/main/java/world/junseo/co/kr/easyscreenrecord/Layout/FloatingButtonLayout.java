package world.junseo.co.kr.easyscreenrecord.Layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import world.junseo.co.kr.easyscreenrecord.Common.CommonFunc;
import world.junseo.co.kr.easyscreenrecord.Interface.FloatingListener;
import world.junseo.co.kr.easyscreenrecord.R;

public class FloatingButtonLayout extends RelativeLayout{

    FloatingListener mFloatingListener;

    //플로팅 종류
    private int m_nStatus = 0;
    public final static int RECORD = 0;

    private Context mContext;
    ImageView iv_background_img;

    public void setListener(FloatingListener floatingListener) {
        mFloatingListener = floatingListener;
    }

    public FloatingButtonLayout(Context context, int status, int resID) {
        this(context, status, resID, null);
        // TODO Auto-generated constructor stub
    }

    public FloatingButtonLayout(Context context, int status, int resID, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.floating_button_layout, this, // we are the parent
                true);

        mContext = context;

        m_nStatus = status;

        iv_background_img = findViewById(R.id.iv_background_img);
        //iv_background_img.setOnClickListener(this);

        if(CommonFunc.isResource(context, resID))
            iv_background_img.setImageResource(resID);
    }

/*    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_background_img:
                if(mFloatingListener != null)
                    mFloatingListener.touchImage();
                break;
        }
    }*/
}
