package com.rakesh.qapragyan17;


import com.google.gson.annotations.SerializedName;

import java.util.List;

class Feedback {

    @SerializedName("event_name")
    String event_name;

    @SerializedName("user_id")
    int user_id;

    @SerializedName("user_token")
    String user_token;

    @SerializedName("data")
    List<Data> data;
}
