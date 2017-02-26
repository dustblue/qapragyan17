package com.rakesh.qapragyan17;


import java.util.Map;

import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

public interface NetworkService {
    @FormUrlEncoded
    @POST("/")
    Observable<Response> authenticate(@Field("user_roll") String username, @Field("user_pass") String password);

    @FormUrlEncoded
    @POST("QA/response")
    Observable<Response> sendFeedback(@FieldMap Map<String, String> parameters);
}

