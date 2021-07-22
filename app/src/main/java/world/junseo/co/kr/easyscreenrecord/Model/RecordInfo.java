package world.junseo.co.kr.easyscreenrecord.Model;

import java.io.Serializable;

public class RecordInfo implements Serializable {
    String VideoPath;
    String CreateDate;
    long FileSize;
    int PlayTime;
    String ThumbPath;

    int width;
    int height;

    public String getVideoPath() {
        return VideoPath;
    }

    public void setVideoPath(String videoPath) {
        VideoPath = videoPath;
    }

    public String getCreateDate() {
        return CreateDate;
    }

    public void setCreateDate(String createDate) {
        CreateDate = createDate;
    }

    public long getFileSize() {
        return FileSize;
    }

    public void setFileSize(long fileSize) {
        FileSize = fileSize;
    }

    public int getPlayTime() {
        return PlayTime;
    }

    public void setPlayTime(int playTime) {
        PlayTime = playTime;
    }

    public String getThumbPath() {
        return ThumbPath;
    }

    public void setThumbPath(String thumbBitmap) {
        ThumbPath = thumbBitmap;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
