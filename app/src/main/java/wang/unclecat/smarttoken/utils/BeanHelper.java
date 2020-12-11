package wang.unclecat.smarttoken.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * java bean工具类(序列化等)
 *
 * @author: 喵叔catuncle
 * @date:  2020/12/11 10:03
 */
public class BeanHelper {

    private static final Gson GSON = createGson(true);

    public static String toJson(final Object object) {
        return GSON.toJson(object);
    }


    public static Gson getGSON() {
        return GSON;
    }

    private static Gson createGson(final boolean serializeNulls) {
        final GsonBuilder builder = new GsonBuilder();
        if (serializeNulls) builder.serializeNulls();
        return builder.create();
    }
}
