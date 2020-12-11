package wang.unclecat.smarttoken.http;

import android.text.TextUtils;

import java.io.IOException;

import retrofit2.Call;
import timber.log.Timber;
import wang.unclecat.smarttoken.AbsAccessTokenInterceptor;
import wang.unclecat.smarttoken.beans.RespBaiduError;
import wang.unclecat.smarttoken.beans.RespBaiduToken;
import wang.unclecat.smarttoken.http.api.BaiduFaceService;
import wang.unclecat.smarttoken.utils.BaiduAuthManager;
import wang.unclecat.smarttoken.utils.BeanHelper;

/**
 * Baidu access token自动获取
 *
 * @author: 喵叔catuncle
 * @date:  2020/12/11 10:03
 */
public class BaiduAccessAccessTokenInterceptor extends AbsAccessTokenInterceptor {

    //这个构造方法只是为了demo演示
    public BaiduAccessAccessTokenInterceptor(@Visibility int tokenType) {
        super(tokenType);
    }

    //实际中用无参构造方法
    public BaiduAccessAccessTokenInterceptor() {
        super(TOKEN_IN_HEADER);
    }

    protected String tokenKey() {
       return "access_token";
    }

    protected boolean checkTokenError(String respBody) {
        if (respBody.length() <= 512) {
            Timber.d("checkTokenError() called with: respBody = [ %s ]", respBody);
            RespBaiduError respAppVerify = BeanHelper.getGSON().fromJson(respBody, RespBaiduError.class);

            return RespBaiduError.isAccessTokenError(respAppVerify);
        }
        return false;
    }

    /**
     * 同步获取Token
     */
    protected String getAccessTokenFromServer() throws IOException {
        //同步获取Token
        BaiduFaceService service = BaiduFaceChecker.createSuperVerifyAPI();//注意：刷新Token不能再拦截，否则就会陷入无限循环
        Call<RespBaiduToken> call = service.autoGetToken("client_credentials", "1B43KE7Hf9S591dyAuGDaVeT",
                "jaGVrO0s4owFL4NUnQxc7zn4kTf6Zyyq");

        retrofit2.Response<RespBaiduToken> execute = call.execute();

        if (execute.isSuccessful()) {
            RespBaiduToken body = execute.body();
            Timber.d("autoGetToken() result : [ %s ]", body);

            if (body != null) {
                String accessToken = body.getAccessToken();
                if (!TextUtils.isEmpty(accessToken)) {
                    //保存Token
                    BaiduAuthManager.putToken(accessToken);
                }
                return accessToken;
            }
        }

        throw new IOException("getTokenFromServer failed");
    }
}