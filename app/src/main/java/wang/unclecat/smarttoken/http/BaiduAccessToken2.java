package wang.unclecat.smarttoken.http;

import java.io.IOException;

import timber.log.Timber;
import wang.unclecat.smarttoken.PassiveSmartToken;
import wang.unclecat.smarttoken.beans.RespBaiduError;
import wang.unclecat.smarttoken.utils.BeanHelper;

/**
 * Baidu access token自动获取(被动方式)
 *
 * @author: 喵叔catuncle
 * @date:  2020/12/11 10:03
 */
public class BaiduAccessToken2 extends PassiveSmartToken {

    //这个构造方法只是为了demo演示
    @Deprecated
    public BaiduAccessToken2(@Type int tokenType) {
        super(tokenType);
    }

    //实际中用无参构造方法
    public BaiduAccessToken2() {
        super(TOKEN_IN_QUERY);
    }

    @Override
    protected String tokenKey() {
       return BaiduATHelper.TOKEN_KEY;
    }

    @Override
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
    @Override
    protected String getAccessTokenFromServer() throws IOException {
        return BaiduATHelper.getAccessTokenFromServer();
    }
}