package wang.unclecat.smarttoken.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class BaiduAuthManager {
    private static SharedPreferences mSP;

    public static void init(Context context) {
        mSP = context.getSharedPreferences("baidu_auth", Context.MODE_PRIVATE);
    }

    public static void putAccessToken(String token) {
        SharedPreferences.Editor editor = mSP.edit();
        editor.putString("access_token", token);
        editor.putLong("access_token_update_time", System.currentTimeMillis());
        editor.apply();
    }

    public static void commit() {
        SharedPreferences.Editor editor = mSP.edit();
        editor.commit();
    }

    public static String getAccessToken() {
        return mSP.getString("access_token", "");
    }

    public static boolean isAccessTokenOutOfDate() {
        long updateTime = mSP.getLong("access_token_update_time", 0);
        return (System.currentTimeMillis()-updateTime)/1000>=29*24*60*60;//29天access token过期
    }
}
