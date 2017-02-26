package com.rakesh.qapragyan17;


import java.util.Map;

import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

interface NetworkService {
    @FormUrlEncoded
    @POST("admin/login")
    Observable<Response> authenticate(@Field("admin_roll") String username, @Field("admin_pass") String password);

    @FormUrlEncoded
    @POST("QA/response")
    Observable<Response> sendFeedback(@FieldMap Map<String, String> parameters);
}

