package wang.unclecat.smarttoken.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

/**
 * Bitmap工具
 *
 * @author: 喵叔catuncle
 * @date:  2021/1/22 19:54
 */
public class BitmapUtils {

    private static final ColorMatrix sRedToAlphaMatrix = new ColorMatrix(new float[]{
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            1, 0, 0, 0, 0});

    private static final ColorMatrixColorFilter sRedToAlphaFilter = new ColorMatrixColorFilter(sRedToAlphaMatrix);

    /**
     * 合并灰度图
     *
     * @param rgb
     * @param alpha 灰度图保存着alpha通道信息
     * @return
     */
    public static Bitmap composeAlpha(Bitmap rgb, Bitmap alpha) {
        Bitmap target = Bitmap.createBitmap(rgb.getWidth(), rgb.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(target);
        c.setDensity(Bitmap.DENSITY_NONE);

        c.drawBitmap(rgb, 0, 0, null);

        final Paint grayToAlpha = new Paint();
        grayToAlpha.setColorFilter(sRedToAlphaFilter);
        grayToAlpha.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        Rect src = new Rect(0, 0, alpha.getWidth(), alpha.getHeight());
        Rect dst = new Rect(0, 0, rgb.getWidth(), rgb.getHeight());
        c.drawBitmap(alpha, src, dst, grayToAlpha);

        return target;
    }

}
