package wang.unclecat.smarttoken.beans;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import wang.unclecat.smarttoken.utils.BeanHelper;

import java.util.List;

public class RespBaiduHuman {
    private String apiType;

    public void setApiType(String apiType) {
        this.apiType = apiType;
    }

    @SerializedName("log_id")
    @Expose
    private String logId;
    @SerializedName("labelmap")
    @Expose
    private String labelmap;
    @SerializedName("scoremap")
    @Expose
    private String scoremap;
    @SerializedName("foreground")
    @Expose
    private String foreground;
    @SerializedName("person_num")
    @Expose
    private int personNum;
    @SerializedName("person_info")
    @Expose
    private List<PersonInfo> personInfo = null;

    public String getBase64Img() {
        if (apiType != null) {
            if (apiType.contentEquals("scoremap")) {
                return getScoremap();
            } else if (apiType.contentEquals("labelmap")) {
                return getLabelmap();
            }
        }
        return getForeground();
    }

    public boolean isGray() {
        return apiType.contentEquals("scoremap");
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getLabelmap() {
        return labelmap;
    }

    public void setLabelmap(String labelmap) {
        this.labelmap = labelmap;
    }

    public String getScoremap() {
        return scoremap;
    }

    public void setScoremap(String scoremap) {
        this.scoremap = scoremap;
    }

    public String getForeground() {
        return foreground;
    }

    public void setForeground(String foreground) {
        this.foreground = foreground;
    }

    public int getPersonNum() {
        return personNum;
    }

    public void setPersonNum(int personNum) {
        this.personNum = personNum;
    }

    public List<PersonInfo> getPersonInfo() {
        return personInfo;
    }

    public void setPersonInfo(List<PersonInfo> personInfo) {
        this.personInfo = personInfo;
    }

    @Override
    public String toString() {
        int length = 0;
        if (foreground!=null) {
            length = foreground.length();
        }
        return "RespBaiduHuman{" +
                "logId='" + logId + '\'' +
                ", foreground='" + length + '\'' +
                ", personNum=" + personNum +
                ", personInfo=" + personInfo +
                '}';
    }

    public static class PersonInfo {

        @SerializedName("height")
        @Expose
        private float height;
        @SerializedName("width")
        @Expose
        private float width;
        @SerializedName("top")
        @Expose
        private float top;
        @SerializedName("score")
        @Expose
        private float score;
        @SerializedName("left")
        @Expose
        private float left;

        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }

        public float getWidth() {
            return width;
        }

        public void setWidth(float width) {
            this.width = width;
        }

        public float getTop() {
            return top;
        }

        public void setTop(float top) {
            this.top = top;
        }

        public float getScore() {
            return score;
        }

        public void setScore(float score) {
            this.score = score;
        }

        public float getLeft() {
            return left;
        }

        public void setLeft(float left) {
            this.left = left;
        }

        @Override
        public String toString() {
            return BeanHelper.toJson(this);
        }
    }
}
