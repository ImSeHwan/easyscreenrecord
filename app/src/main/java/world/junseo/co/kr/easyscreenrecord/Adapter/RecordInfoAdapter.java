package world.junseo.co.kr.easyscreenrecord.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Locale;

import world.junseo.co.kr.easyscreenrecord.Common.CommonFunc;
import world.junseo.co.kr.easyscreenrecord.Common.CommonInfo;
import world.junseo.co.kr.easyscreenrecord.Interface.RecyclerItemListener;
import world.junseo.co.kr.easyscreenrecord.JWApplication;
import world.junseo.co.kr.easyscreenrecord.Model.RecordInfo;
import world.junseo.co.kr.easyscreenrecord.R;
import world.junseo.co.kr.easyscreenrecord.Util.Log.JLog;
import world.junseo.co.kr.easyscreenrecord.VideoEditActivity;
import world.junseo.co.kr.easyscreenrecord.VideoPlayerActivity;

public class RecordInfoAdapter extends RecyclerView.Adapter<RecordInfoAdapter.ViewHolder>{

    Context mContext;
    ArrayList<RecordInfo> m_RecordInfos;

    Vibrator vibrator;

    String TAG = "RecordInfoAdapter";

    RecyclerItemListener mRecyclerItemListener;

    public RecordInfoAdapter(Context context, RecyclerItemListener recyclerItemListener) {
        mContext = context;
        mRecyclerItemListener = recyclerItemListener;

        vibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void setRecordInfo(ArrayList<RecordInfo> recordInfos) {
        m_RecordInfos = recordInfos;
    }

    private int  getMediaDuration(Uri uriOfFile)  {
        MediaPlayer mp = MediaPlayer.create(mContext,uriOfFile);
        int duration = mp.getDuration();
        return  duration;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.record_list_adapter, viewGroup, false);

        final ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final RecordInfo recordInfo = m_RecordInfos.get(i);

        Glide.with(mContext)
                .load(recordInfo.getThumbPath())
                .into(viewHolder.iv_thumb_img);

        viewHolder.tv_playtime.setText(CommonFunc.msToTime(recordInfo.getPlayTime()));
        viewHolder.tv_make_date.setText(recordInfo.getCreateDate());
        viewHolder.tv_filesize.setText(String.format(Locale.KOREA, "%.2fMB", (float)recordInfo.getFileSize()/1024/1024));

    }

    private void displayCheckMobileNetworkDialog(final RecordInfo recordInfo) {

        AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
        LayoutInflater adbInflater = LayoutInflater.from(mContext);
        View eulaLayout = adbInflater.inflate(R.layout.custom_layout, null);

        final CheckBox cb_my_checkbox = eulaLayout.findViewById(R.id.cb_my_checkbox);
        adb.setView(eulaLayout);
        adb.setTitle(mContext.getString(R.string.info_message9));
        adb.setMessage(mContext.getString(R.string.info_message10));

        adb.setPositiveButton(mContext.getString(R.string.confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {


                // Do what you want to do on "OK" action
                CommonFunc.callShardVideo(mContext, recordInfo.getVideoPath());
                JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.CHECK_WIFI, cb_my_checkbox.isChecked());
                return;
            }
        });

        adb.setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {


                // Do what you want to do on "CANCEL" action
                return;
            }
        }).show();
    }

    @Override
    public int getItemCount() {
        return m_RecordInfos == null ? 0 : m_RecordInfos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tv_make_date, tv_playtime, tv_filesize;

        ImageView iv_thumb_img;

        ImageView iv_btn_edit, iv_btn_shared, iv_btn_delete;


        public ViewHolder(View itemView) {
            super(itemView);

            iv_thumb_img = itemView.findViewById(R.id.iv_thumb_img);
            iv_thumb_img.setOnClickListener(this);

            tv_make_date = itemView.findViewById(R.id.tv_make_date);
            tv_playtime = itemView.findViewById(R.id.tv_playtime);
            tv_filesize = itemView.findViewById(R.id.tv_filesize);

            iv_btn_edit = itemView.findViewById(R.id.iv_btn_edit);
            iv_btn_edit.setOnClickListener(this);

            iv_btn_shared = itemView.findViewById(R.id.iv_btn_shared);
            iv_btn_shared.setOnClickListener(this);

            iv_btn_delete = itemView.findViewById(R.id.iv_btn_delete);
            iv_btn_delete.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            vibrator.vibrate(10);

            boolean isAd = JWApplication.getAppContext().getSimplePrefs().get(CommonInfo.PREF.RC, false);

            if(isAd) {
                // 실행할 광고가 있다면 실행하자. 광고가 닫히면 이후 작업을 할 수 있도록 선택된 위치와 리소스 ID를 알려준다.
                mRecyclerItemListener.showAdMob(getAdapterPosition(), view.getId());
            } else {
                boolean isFailAdView = JWApplication.getAppContext().getSimplePrefs().get(CommonInfo.PREF.RC_IS_FAIL, false);

                switch (view.getId()) {
                    case R.id.iv_thumb_img:
                        if (m_RecordInfos != null) {

                            RecordInfo recordInfo = m_RecordInfos.get(getAdapterPosition());

                            try {
                                Intent intent = new Intent(mContext, VideoPlayerActivity.class);
                                intent.putExtra("MP4_PATH", recordInfo.getVideoPath());
                                intent.putExtra("MP4_WIDTH", recordInfo.getWidth());
                                intent.putExtra("MP4_HEIGHT", recordInfo.getHeight());

                                mContext.startActivity(intent);
                            } catch (Exception e) {
                                // TODO: handle exception
                                JLog.e(TAG, e.toString());
                            }
                        }
                        break;
                    case R.id.iv_btn_edit:

                        if (m_RecordInfos != null) {
                            RecordInfo recordInfo = m_RecordInfos.get(getAdapterPosition());
                            if (recordInfo != null) {
                                Intent intent = new Intent(mContext, VideoEditActivity.class);
                                //String filePath = recordInfo.getVideoPath();
                                intent.putExtra("MP4_PATH", recordInfo.getVideoPath());
                                intent.putExtra("VIDEO_TOTAL_DURATION", recordInfo.getPlayTime());
                                mContext.startActivity(intent);
                            }
                        }
                        break;

                    case R.id.iv_btn_shared:

                        if (CommonFunc.checkWifiOnAndConnected(mContext)) {
                            if (m_RecordInfos != null) {
                                final RecordInfo recordInfo = m_RecordInfos.get(getAdapterPosition());

                                CommonFunc.callShardVideo(mContext, recordInfo.getVideoPath());
                            }
                        } else {
                            boolean isWifiChecked = JWApplication.getAppContext().getSimplePrefs().get(CommonInfo.PREF.CHECK_WIFI, false);

                            final RecordInfo recordInfo = m_RecordInfos.get(getAdapterPosition());

                            if (isWifiChecked) {
                                if (m_RecordInfos != null) {

                                    CommonFunc.callShardVideo(mContext, recordInfo.getVideoPath());
                                }
                            } else {
                                displayCheckMobileNetworkDialog(recordInfo);
                            }
                        }

                        break;

                    case R.id.iv_btn_delete:

                        if (m_RecordInfos != null) {


                            final RecordInfo recordInfo = m_RecordInfos.get(getAdapterPosition());

                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setTitle(mContext.getString(R.string.video_delete));
                            builder.setMessage(R.string.info_message5);
                            builder.setPositiveButton(mContext.getString(R.string.confirm),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            removeAt(getAdapterPosition(), recordInfo.getVideoPath());

                                            dialog.dismiss();
                                        }
                                    });

                            builder.setNegativeButton(mContext.getString(R.string.cancel),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });

                            builder.show();

                        }
                        break;
                }
            }

            boolean isFailAdView = JWApplication.getAppContext().getSimplePrefs().get(CommonInfo.PREF.RC_IS_FAIL, false);
            if(isFailAdView)
                JWApplication.getAppContext().getSimplePrefs().set(CommonInfo.PREF.RC, true);
        }
    }

    public void removeAt(int position, String path) {
        m_RecordInfos.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, m_RecordInfos.size());

        CommonFunc.fileDelete(path);
        CommonFunc.imageFileDelete(path);
    }
}
