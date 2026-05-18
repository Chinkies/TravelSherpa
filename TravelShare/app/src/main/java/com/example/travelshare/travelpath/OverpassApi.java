package com.example.travelshare.travelpath;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OverpassApi {
    @GET("api/interpreter")
    Call<OverpassResponse> getLieux(@Query("data") String query);
}