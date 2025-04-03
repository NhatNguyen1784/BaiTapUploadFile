package vn.hcmute.app.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;


    private static Retrofit getRetrofit(){
        Gson gson = new GsonBuilder().setDateFormat("yyyy MM dd HH:mm:ss").create();
        if(retrofit==null){
            retrofit =new Retrofit.Builder()
                    .baseUrl("http://app.iotstar.vn/appfoods/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}
