package wang.unclecat.smarttoken.utils;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;


import com.ypx.imagepicker.helper.launcher.PLauncher;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;

/**
 * PLauncher的RxJava接口适配
 *
 * @author: 喵叔catuncle
 * @date: 2021/1/22 18:45
 */
public class RxPLauncher {
    private PLauncher pLauncher;

    public static RxPLauncher init(Fragment fragment) {
        return new RxPLauncher(fragment.getActivity());
    }

    public static RxPLauncher init(FragmentActivity activity) {
        return new RxPLauncher(activity);
    }

    public static RxPLauncher init(Activity activity) {
        return new RxPLauncher(activity);
    }

    public Observable<Result> startForResult(final Intent intent) {
        return Observable.create(new ObservableOnSubscribe<Result>() {

            @Override
            public void subscribe(@NonNull final ObservableEmitter<Result> emitter) {
                pLauncher.startActivityForResult(intent, new PLauncher.Callback() {

                    @Override
                    public void onActivityResult(int resultCode, Intent data) {
                        emitter.onNext(new Result(resultCode, data));
                        emitter.onComplete();
                    }
                });
            }
        });
    }

    private RxPLauncher(FragmentActivity activity) {
        pLauncher = PLauncher.init(activity);
    }

    private RxPLauncher(Activity activity) {
        pLauncher = PLauncher.init(activity);
    }


    public static class Result {
        private final int resultCode;
        private final Intent data;

        public Result(int resultCode, Intent data) {
            this.resultCode = resultCode;
            this.data = data;
        }

        public int getResultCode() {
            return resultCode;
        }

        public Intent getData() {
            return data;
        }
    }

}
