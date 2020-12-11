package wang.unclecat.smarttoken.beans;

/**
 * https://cloud.baidu.com/doc/BODY/s/mk4qt2wpt
 *
 * 100	Invalid parameter	无效的access_token参数，token拉取失败，可以参考“Access Token获取”重新获取
 * 110	Access token invalid or no longer valid	access_token无效，token有效期为30天，注意需要定期更换，也可以每次请求都拉取新token
 * 111	Access token expired	access token过期，token有效期为30天，注意需要定期更换，也可以每次请求都拉取新token
 */
public class RespBaiduError {
    private int error_code;
    private String error_msg;

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public String getError_msg() {
        return error_msg;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
    }

    public static boolean isAccessTokenError(RespBaiduError error) {
        int code = error.getError_code();
        return code == 100 || code == 110 || code == 111;
    }
}
