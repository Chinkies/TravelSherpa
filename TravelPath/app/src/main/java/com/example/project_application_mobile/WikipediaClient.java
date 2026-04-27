package com.example.project_application_mobile;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WikipediaClient {
    private static WikipediaApi api;

    public static WikipediaApi getApi() {
        if (api == null) {
            api = new Retrofit.Builder()
                    .baseUrl("https://fr.wikipedia.org/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(WikipediaApi.class);
        }
        return api;
    }
}