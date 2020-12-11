package wang.unclecat.smarttoken.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class BaiduAuthManager {
    private static SharedPreferences mSP;

    public static void init(Context context) {
        mSP = context.getSharedPreferences("baidu_auth", Context.MODE_PRIVATE);
    }

    public static void putToken(String token) {
        SharedPreferences.Editor editor = mSP.edit();
        editor.putString("token", token);
        editor.apply();
    }

    public static void commit() {
        SharedPreferences.Editor editor = mSP.edit();
        editor.commit();
    }

    public static String getToken() {
        return mSP.getString("token", "");
    }
}
