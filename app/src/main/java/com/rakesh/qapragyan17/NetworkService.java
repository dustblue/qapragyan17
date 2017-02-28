package com.rakesh.qapragyan17;

import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

interface NetworkService {
    @FormUrlEncoded
    @POST("auth/app")
    Observable<Response> authenticate(@Field("user_email") String username, @Field("user_pass") String password);

    @FormUrlEncoded
    @POST("QA/response")
    Observable<Response> sendFeedback(@Body Feedback feedback);

    @FormUrlEncoded
    @POST("auth/team/pin")
    Observable<Response> authPin(@Field("team_name") String teamname, @Field("team_pin") String pin);
}

