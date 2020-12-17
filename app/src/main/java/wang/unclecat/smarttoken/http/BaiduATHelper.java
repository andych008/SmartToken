package wang.unclecat.smarttoken.http;

import android.text.TextUtils;

import java.io.IOException;

import retrofit2.Call;
import timber.log.Timber;
import wang.unclecat.smarttoken.beans.RespBaiduToken;
import wang.unclecat.smarttoken.http.api.BaiduFaceService;
import wang.unclecat.smarttoken.utils.BaiduAuthManager;

public class BaiduATHelper {

    public static final String TOKEN_KEY = "access_token";

    /**
     * 同步获取Token
     */
    public static String getAccessTokenFromServer() throws IOException {
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
                    BaiduAuthManager.putAccessToken(accessToken);
                }
                return accessToken;
            }
        }

        throw new IOException("getTokenFromServer failed");
    }
}
