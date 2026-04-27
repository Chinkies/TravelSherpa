package com.example.project_application_mobile;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface WikipediaApi {
    @Headers("User-Agent: TravelPathApp/1.0 (thomasbarascud556@gmail)")
    @GET("api/rest_v1/page/summary/{title}")
    Call<WikipediaResponse> getSummary(@Path("title") String title);
}