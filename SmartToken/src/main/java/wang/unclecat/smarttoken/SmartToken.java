package wang.unclecat.smarttoken;

import android.support.annotation.IntDef;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * SmartToken
 *
 * @author: 喵叔catuncle
 * @date: 2020/12/17 10:39
 */
public abstract class SmartToken {
    /**
     * token 在query
     */
    public static final int TOKEN_IN_QUERY = 1;
    /**
     * token 在header
     */
    public static final int TOKEN_IN_HEADER = 2;
    /**
     * token 在query和header同时存在
     */
    public static final int TOKEN_IN_QUERY_AND_HEADER = 3;


    @IntDef({TOKEN_IN_QUERY, TOKEN_IN_HEADER, TOKEN_IN_QUERY_AND_HEADER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }


    @Type
    private final int tokenType;

    public SmartToken(@Type int tokenType) {
        this.tokenType = tokenType;
    }

    /**
     * 更新AccessToken到新request里
     *
     * @param request 原request
     * @param token   access token
     * @return 新request
     */
    protected Request updateToken(Request request, String token) {
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

    protected HttpUrl updateUrl(Request request, String token) {
        HttpUrl url = request.url();
        return url.newBuilder()
                .setQueryParameter(tokenKey(), token)
                .build();
    }

    /**
     * access token在query或header里对应的key
     */
    protected abstract String tokenKey();

    /**
     * 从服务端同步获取access token
     */
    protected abstract String getAccessTokenFromServer() throws IOException;
}