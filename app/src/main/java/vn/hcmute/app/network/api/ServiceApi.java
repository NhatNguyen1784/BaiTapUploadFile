package vn.hcmute.app.network.api;

import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import vn.hcmute.app.constant.Const;
import vn.hcmute.app.model.ImageUpload;

public interface ServiceApi {
    Gson gson = new GsonBuilder().setDateFormat("yyyy MM dd HH:mm:ss").create();
    ServiceApi serviceApi = new Retrofit.Builder()
            .baseUrl("http://app.iotstar.vn:8081/appfoods/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ServiceApi.class);

    @Multipart
    @POST("upload.php")
    Call<List<ImageUpload>> upload(@Part(Const.MY_USERNAME)RequestBody username,
                                   @Part MultipartBody.Part avatar);

    @Multipart
    @POST("upload1.php")
    Call<Message> upload1(@Part(Const.MY_USERNAME)RequestBody username,
                         @Part MultipartBody.Part avatar);
}
