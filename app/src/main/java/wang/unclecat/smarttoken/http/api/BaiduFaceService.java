package wang.unclecat.smarttoken.http.api;


import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import wang.unclecat.smarttoken.beans.RespBaiduHuman;
import wang.unclecat.smarttoken.beans.RespBaiduToken;

//https://cloud.baidu.com/doc/BODY/s/Fk3cpyxua
public interface BaiduFaceService {

    @GET("oauth/2.0/token")
    Observable<RespBaiduToken> token(@Query("grant_type") String grant_type, @Query("client_id") String client_id, @Query("client_secret") String client_secret);

    @GET("oauth/2.0/token")
    Call<RespBaiduToken> autoGetToken(@Query("grant_type") String grant_type, @Query("client_id") String client_id, @Query("client_secret") String client_secret);

    @FormUrlEncoded
    @POST("rest/2.0/image-classify/v1/body_seg")
    Observable<RespBaiduHuman> humanBody(@Query("access_token") String access_token, @Field("image") String image, @Field("type") String type);

}