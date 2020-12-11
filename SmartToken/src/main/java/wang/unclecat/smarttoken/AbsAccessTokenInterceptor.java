package wang.unclecat.smarttoken;

import android.support.annotation.IntDef;
import android.util.Log;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * 拦截器实现access token自动获取
 *
 * @author: 喵叔catuncle
 * @date:  2020/12/11 10:12
 */
public abstract class AbsAccessTokenInterceptor implements Interceptor {

    public static final int TOKEN_IN_QUERY = 1;//token 在query
    public static final int TOKEN_IN_HEADER = 2;//token 在header
    public static final int TOKEN_IN_QUERY_AND_HEADER = 3;//token 在query和header同时存在


    @IntDef({TOKEN_IN_QUERY, TOKEN_IN_HEADER, TOKEN_IN_QUERY_AND_HEADER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Visibility {}


    @Visibility
    private final int tokenType;

    public AbsAccessTokenInterceptor(@Visibility int tokenType) {
        this.tokenType = tokenType;
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

    /**
     * 更新AccessToken到新request里
     *
     * @param request 原request
     * @param token access token
     * @return 新request
     */
    private Request updateToken(Request request, String token) {
        //重新构造request
        if (tokenType == TOKEN_IN_QUERY) {
            HttpUrl updatedUrl = updateUrl(request, token);
            Request.Builder builder = request.newBuilder().url(updatedUrl);

            return builder.build();
        } else if (tokenType == TOKEN_IN_HEADER) {
            Request.Builder builder = request.newBuilder()
                    .header(tokenKey(), token);

            return builder.build();
        } else if (tokenType == TOKEN_IN_QUERY_AND_HEADER) {
            HttpUrl updatedUrl = updateUrl(request, token);
            Request.Builder builder = request.newBuilder()
                    .url(updatedUrl)
                    .header(tokenKey(), token);

            return builder.build();
        } else {
            return request;
        }
    }

    private HttpUrl updateUrl(Request request, String token) {
        HttpUrl url = request.url();
        return url.newBuilder()
                .setQueryParameter(tokenKey(), token)
                .build();
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

    /**
     * access token在query或header里对应的key
     */
    protected abstract String tokenKey();

    /**
     * 同步获取access token
     */
    protected abstract String getAccessTokenFromServer() throws IOException;
}