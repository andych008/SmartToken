package wang.unclecat.smarttoken.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import okio.Buffer;
import timber.log.Timber;


public class FileUtils {

    public static final long MB = 1048576L;


    public static Buffer fileToBuffer(String filePath) {

        InputStream fis = null;

        try {
            fis = new FileInputStream(filePath);

            Buffer buffer = new okio.Buffer();
            buffer.readFrom(fis);

            return buffer;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static Buffer bitmapFileToBuffer(String filePath) {
        final int maxSize = 2;
        InputStream fis = null;

        try {
            fis = new FileInputStream(filePath);
            int byteSize = fis.available();

            float size = byteSize / (float) MB;
            Bitmap bitmap = BitmapFactory.decodeStream(fis);

            Timber.d("bitmapFileToBuffer() called with: 压缩前 size = [ %s ]", size);

            if (size > maxSize) {
                // 当图片大于maxSize兆时，进行尺寸压缩(做个压缩处理，转化Base64字符串会快一些)
                bitmap = getScaleBitmap(bitmap);
            }

            if (bitmap != null) {

                try {
                    Buffer buffer = new okio.Buffer();
                    if (size > maxSize) {
                        // 当图片大于maxSize兆时，进行质量压缩
                        float multiple = size / maxSize;
                        bitmap.compress(Bitmap.CompressFormat.JPEG, (int)(100 / multiple), buffer.outputStream());
                    } else {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, buffer.outputStream());
                    }
                    Timber.d("bitmapFileToBuffer() called with: 压缩后 size = [ %s ]", buffer.size()/1024f/1024f);

                    return buffer;

                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static Bitmap getScaleBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        //将大于1080px的图片转化为1080px的大小，这里可以根据业务做修改
        final int minSize = 1080;
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        Timber.d("getScaleBitmap() called with: 宽高(%s, %s)", bitmapWidth, bitmapHeight);
        int scaleWidth;
        int scaleHeight;
        int tempSize = Math.min(bitmapWidth, bitmapHeight);
        float scaleFactor = (float) minSize / (float) tempSize;
        //判断最大的长或宽
        if (tempSize == bitmapWidth) {
            if (tempSize > minSize) {
                scaleWidth = minSize;
                scaleHeight = (int) (bitmapHeight * scaleFactor);
            } else {
                return bitmap;
            }
        } else {
            if (tempSize > minSize) {
                scaleHeight = minSize;
                scaleWidth = (int) (bitmapWidth * scaleFactor);
            } else {
                return bitmap;
            }
        }
        Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, true);

        return scaleBitmap;
    }

}
