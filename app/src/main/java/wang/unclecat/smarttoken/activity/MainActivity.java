package wang.unclecat.smarttoken.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.knight.smarttoken.R;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import okio.ByteString;
import timber.log.Timber;
import wang.unclecat.smarttoken.beans.RespBaiduHuman;
import wang.unclecat.smarttoken.http.BaiduFaceChecker;
import wang.unclecat.smarttoken.utils.BaiduAuthManager;
import wang.unclecat.smarttoken.utils.BitmapUtils;
import wang.unclecat.smarttoken.utils.PhotoAlbumUtil;
import wang.unclecat.smarttoken.utils.ToastUtil;
import wang.unclecat.smarttoken.utils.permissionsUtil.Permissions;

import static wang.unclecat.smarttoken.SmartToken.TOKEN_IN_HEADER;
import static wang.unclecat.smarttoken.SmartToken.TOKEN_IN_QUERY;
import static wang.unclecat.smarttoken.SmartToken.TOKEN_IN_QUERY_AND_HEADER;

public class MainActivity extends FragmentActivity {

    private ImageView iv_photo;
    private ImageView iv_photo2;
    private CheckBox cb_query;
    private CheckBox cb_header;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBind();
        checkNeedPermissions();


        //log
        {
            FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                    .showThreadInfo(true)  // (Optional) Whether to show thread info or not. Default true
                    .methodCount(1)         // (Optional) How many method line to show. Default 2
                    .methodOffset(5)        // (Optional) Hides internal method calls up to offset. Default 5
                    .tag("SmartToken")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                    .build();

            Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected void log(int priority, String tag, String message, Throwable t) {
                    Logger.log(priority, tag, message, t);
                }
            });
        }

        BaiduAuthManager.init(this.getApplicationContext());
    }

    private int fillType = 1;
    private void initBind(){

        progressBar = findViewById(R.id.progressBar);
        cb_query = findViewById(R.id.cb_query);
        cb_header = findViewById(R.id.cb_header);
        iv_photo = findViewById(R.id.iv_photo);
        iv_photo2 = findViewById(R.id.iv_photo2);

        //调用系统相册
        findViewById(R.id.btn_system_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goSystemPhoto();
            }
        });

        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButton:
                        fillType = 1;
                        break;
                    case R.id.radioButton2:
                        fillType = 2;
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * 动态处理申请权限的结果
     * 用户点击同意或者拒绝后触发
     *
     * @param requestCode 请求码
     * @param permissions 权限
     * @param grantResults 结果码
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                //获取权限一一验证
                if (grantResults.length > 1) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        //全部授于可以进行往下操作
                    } else {
                        //拒绝就要强行跳转设置界面
                        Permissions.showPermissionsSettingDialog(this, permissions[0]);
                    }
                } else {
                    ToastUtil.showShortToast(this, "请重新尝试~");
                }
                break;
        }
    }

    /**
     * 跳转系统相册
     */
    private void goSystemPhoto(){
        Intent intent = new Intent();
        //设置Intent.ACTION_PICK
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,2);
    }


    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        String photoPath;

        if(requestCode == 2 && resultCode == RESULT_OK){
            //处理调用相册返回的路径
            photoPath = PhotoAlbumUtil.getRealPathFromUri(this,data.getData());
            Glide.with(this).load(photoPath).apply(RequestOptions.noTransformation()
                    .override(iv_photo.getWidth(),iv_photo.getHeight())
                    .error(R.drawable.default_person_icon))
                    .into(iv_photo);

            handlePicBaidu(photoPath);
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void handlePicBaidu(final String photoPath) {
        Timber.i("--------2. 开始抠图");
        final long startTime = System.currentTimeMillis();

        int tokenType = getTokenType();

        BaiduFaceChecker.uploadFile(photoPath, tokenType, fillType, new Observer<RespBaiduHuman>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                showProgress();
            }

            @Override
            public void onNext(@NonNull RespBaiduHuman resp) {
                Timber.i("--------3. 抠图完成, 用时%s秒", (System.currentTimeMillis()-startTime)/1000);

                Timber.d("onNext() called with: resp = [ %s ]", resp);

                ByteString byteString = ByteString.decodeBase64(resp.getBase64Img());

                byte[] bytes = byteString.toByteArray();

                Bitmap faceBitmap;
                if (resp.isGray()) {
                    // Load RGB data
                    Bitmap rgb = BitmapFactory.decodeFile(photoPath);

                    Timber.d("--------rgb");
                    // Load Alpha data
                    Bitmap alpha = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    Timber.d("--------alpha");
                    faceBitmap = BitmapUtils.composeAlpha(rgb, alpha);

                    Timber.d("--------composeAlpha");
                } else {
                    faceBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                }

                Timber.i("--------4. 生成bitmap");
                Glide.with(MainActivity.this).load(faceBitmap).apply(RequestOptions.noTransformation()
                        .override(iv_photo2.getWidth(),iv_photo2.getHeight())
                        .error(R.drawable.default_person_icon))
                        .into(iv_photo2);

            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.i("--------3. 抠图失败, 用时%s秒", (System.currentTimeMillis()-startTime)/1000);
                Timber.e(e);
                ToastUtil.showShortToast(MainActivity.this, e.getMessage());
                hideProgress();
            }

            @Override
            public void onComplete() {

                hideProgress();
            }
        });
    }

    private int getTokenType() {
        int tokenType;
        if (cb_query.isChecked()) {
            if (cb_header.isChecked()) {
                tokenType = TOKEN_IN_QUERY_AND_HEADER;
            } else {
                tokenType = TOKEN_IN_QUERY;
            }
        } else if (cb_header.isChecked()) {
            tokenType = TOKEN_IN_HEADER;
        } else {
            tokenType = -1;
        }
        return tokenType;
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }


    /**
     * 检测需要申请的权限
     *
     */
    private void checkNeedPermissions(){
        //6.0以上需要动态申请权限 动态权限校验 Android 6.0 的 oppo & vivo 手机时，始终返回 权限已被允许 但是当真正用到该权限时，却又弹出权限申请框。
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                //多个权限一起申请
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 1);
            }
        }
    }
}
