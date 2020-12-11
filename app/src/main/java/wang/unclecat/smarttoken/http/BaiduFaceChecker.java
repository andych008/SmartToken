package wang.unclecat.smarttoken.http;

import android.text.TextUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.ByteString;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;
import wang.unclecat.smarttoken.AbsAccessTokenInterceptor;
import wang.unclecat.smarttoken.beans.RespBaiduHuman;
import wang.unclecat.smarttoken.beans.RespBaiduToken;
import wang.unclecat.smarttoken.http.api.BaiduFaceService;
import wang.unclecat.smarttoken.utils.BaiduAuthManager;
import wang.unclecat.smarttoken.utils.FileUtils;

/**
 * 百度人像抠图(试用版，请求时间一般在13秒左右)
 *
 * @author: 喵叔catuncle
 * @date:  2020/12/11 10:03
 */
public class BaiduFaceChecker {

    public static final String BASE_URL = "https://aip.baidubce.com/";

    /**
     * 单图上传(智能判断access token是否有效(被动获取有效token)，对业务使用者隐藏access token的获取)
     *
     * @param filePath
     */
    public static void uploadFile(final String filePath, @AbsAccessTokenInterceptor.Visibility int tokenType, final Observer<RespBaiduHuman> observer) {

        Timber.d("uploadFile() called with: filePath = [ %s ], tokenType = [ %s ]", filePath, tokenType);
//        String token = BaiduAuthManager.getToken();
        String token = "";//just for test !
        BaiduAccessAccessTokenInterceptor tokenInterceptor = new BaiduAccessAccessTokenInterceptor(tokenType);
        doUploadFile(token, filePath, tokenInterceptor).subscribe(observer);
    }

    /**
     * 单图上传（依次执行获取access token和抠图请求）
     * @param filePath
     */
    @Deprecated
    public static void uploadFile2(final String filePath, final Observer<RespBaiduHuman> observer) {

        token().subscribe(new Observer<RespBaiduToken>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                observer.onSubscribe(d);
            }

            @Override
            public void onNext(@NonNull RespBaiduToken respBaiduToken) {
                Timber.d("onNext() called with: respBaiduToken = [ %s ]", respBaiduToken);
                if (!TextUtils.isEmpty(respBaiduToken.getAccessToken())) {
                    BaiduAuthManager.putToken(respBaiduToken.getAccessToken());

                    doUploadFile(respBaiduToken.getAccessToken(), filePath, null).retry(2, new Predicate<Throwable>() {
                        @Override
                        public boolean test(@NonNull Throwable throwable) throws Exception {
                            Timber.d("test() called with: throwable = [ %s ]", throwable.getMessage());
                            return true;
                        }
                    }).subscribe(observer);

                } else {
                    throw new RuntimeException("服务异常");
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                observer.onError(e);
            }

            @Override
            public void onComplete() {

            }
        });

    }

    /**
     * 调用获取token开放服务
     */
    static BaiduFaceService createSuperVerifyAPI() {
        BaiduFaceService service = new Retrofit.Builder()
                .client(createOkHttpClient( null))
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(BaiduFaceService.class);
        return service;
    }

    /**
     * 调用普通开放服务
     */
    private static BaiduFaceService createNormalAPI(Interceptor interceptor) {
        Retrofit retrofit = new Retrofit.Builder()
                .client(createOkHttpClient(interceptor))
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(BaiduFaceService.class);
    }

    /**
     * 初始化okHttpClient
     */
    private static OkHttpClient createOkHttpClient(Interceptor interceptor) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.retryOnConnectionFailure(true)
                .addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
                .connectTimeout(15, TimeUnit.SECONDS)
                .callTimeout(20, TimeUnit.SECONDS);

        if (interceptor != null) {
            builder.addInterceptor(interceptor);
        }

        return builder.build();
    }


    private static Observable<RespBaiduToken> token() {

        return createSuperVerifyAPI().token("client_credentials", "3xnMekEFimbBNPwZ6Z5zYcob", "Q1sUqdNKUuYn22U96F1j1QrG4DoQYgIg")
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private static Observable<RespBaiduHuman> doUploadFile(String access_token, String filePath, Interceptor tokenInterceptor) {
        Buffer buffer = FileUtils.bitmapFileToBuffer(filePath);

//        byte[] bytes = buffer.readByteArray();
        ByteString byteString = buffer.readByteString();
        String base64 = byteString.base64();

        Observable<RespBaiduHuman> respHumanObservable = createNormalAPI(tokenInterceptor).humanBody(access_token, base64, "foreground")
                .subscribeOn(Schedulers.io())
                .map(new Function<RespBaiduHuman, RespBaiduHuman>() {
                    @Override
                    public RespBaiduHuman apply(@NonNull RespBaiduHuman respBaiduHuman) throws Exception {
                        if (respBaiduHuman.getPersonNum() <= 0) {
                            throw new RuntimeException("抠图失败");
                        }
                        return respBaiduHuman;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
        return respHumanObservable;
    }


}
