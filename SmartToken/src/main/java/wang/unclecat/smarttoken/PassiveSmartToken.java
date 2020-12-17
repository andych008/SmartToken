package wang.unclecat.smarttoken;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * 拦截器实现access token自动获取(通过判断结果错误码，被动获取)
 *
 * @author: 喵叔catuncle
 * @date: 2020/12/11 10:12
 */
public abstract class PassiveSmartToken extends SmartToken implements Interceptor {

    public PassiveSmartToken(@Type int tokenType) {
        super(tokenType);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response originResponse = chain.proceed(request);
        if (checkTokenError(originResponse)) {
            //同获取AccessToken
            String token = getAccessTokenFromServer();

            //更新AccessToken
            Request updatedRequest = updateToken(request, token);

            return chain.proceed(updatedRequest);
        } else {
            return originResponse;
        }
    }

    private boolean checkTokenError(Response response) {
        ResponseBody responseBody = response.body();
        BufferedSource source = responseBody.source();

        try {
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.buffer().clone();
            String s = buffer.readUtf8();
            return checkTokenError(s);
        } catch (Exception e) {
            Log.e("SmartToken", e.getMessage(), e);
        }

        return false;
    }

    /**
     * 解析http resp body，判断是否是“access token无效”错误
     *
     * @param respBody http 返回实体字符串
     * @return true: 是, false: 否
     */
    protected abstract boolean checkTokenError(String respBody);

}