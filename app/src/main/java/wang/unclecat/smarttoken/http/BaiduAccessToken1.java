package wang.unclecat.smarttoken.http;

import android.text.TextUtils;

import java.io.IOException;

import wang.unclecat.smarttoken.ActiveSmartToken;
import wang.unclecat.smarttoken.utils.BaiduAuthManager;

/**
 * Baidu access token自动获取(主动方式)
 *
 * @author: 喵叔catuncle
 * @date: 2020/12/11 10:03
 */
public class BaiduAccessToken1 extends ActiveSmartToken {

    //这个构造方法只是为了demo演示
    @Deprecated
    public BaiduAccessToken1(@Type int tokenType) {
        super(tokenType);
    }

    //实际中用无参构造方法
    public BaiduAccessToken1() {
        super(TOKEN_IN_QUERY);
    }

    @Override
    protected String tokenKey() {
        return BaiduATHelper.TOKEN_KEY;
    }

    /**
     * 同步获取Token
     */
    @Override
    protected String getAccessTokenFromServer() throws IOException {
        return BaiduATHelper.getAccessTokenFromServer();
    }

    @Override
    protected boolean isOutOfDate(String localToken) {
//        return TextUtils.isEmpty(localToken) && BaiduAuthManager.isAccessTokenOutOfDate();

        //一直返回true只是为了demo演示，验证token是否被成功添加
        return true;
    }

    @Override
    protected String getAccessTokenFromLocal() throws IOException {
//        String token = BaiduAuthManager.getAccessToken();
//        return token;

        //一直返回null只是为了demo演示，验证token是否被成功添加
        return null;
    }
}