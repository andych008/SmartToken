package wang.unclecat.smarttoken;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 拦截器实现access token自动获取(主动判断是否过期)
 *
 * @author: 喵叔catuncle
 * @date: 2020/12/16 17:32
 */
public abstract class ActiveSmartToken extends SmartToken implements Interceptor {

    public ActiveSmartToken(@Type int tokenType) {
        super(tokenType);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        request.url().toString();
        Log.d("SmartToken", "url(处理前) = " + request.url());
        Log.d("SmartToken", "headers(处理前) = " + request.headers());

        String localToken = getAccessTokenFromLocal();
        String token = isOutOfDate(localToken) ? getAccessTokenFromServer() : localToken;
        Request updatedRequest = updateToken(request, token);
        Log.d("SmartToken", "url(处理后) = " + updatedRequest.url());
        Log.d("SmartToken", "headers(处理后) = " + updatedRequest.headers());

        return chain.proceed(updatedRequest);
    }

    /**
     * 判断access token是否已过期
     *
     * @param localToken 本地的token(可能为空)
     * @return true:过期, false:未过期
     */
    protected abstract boolean isOutOfDate(String localToken);

    /**
     * 从本地同步获取access token
     */
    protected abstract String getAccessTokenFromLocal() throws IOException;
}
