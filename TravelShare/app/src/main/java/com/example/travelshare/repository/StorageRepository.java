package com.example.travelshare.repository;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Url;

public class StorageRepository {

    private static final String IMGBB_API_KEY = "0329c485dc681ec3109b9f6771c55af6";
    private final ImgBBApi imgBBApi;
    private final DeleteApi deleteApi;

    public interface DeleteApi {
        @GET
        Call<ResponseBody> deleteImage(@Url String deleteUrl);
    }

    public StorageRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.imgbb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        imgBBApi = retrofit.create(ImgBBApi.class);
        deleteApi = retrofit.create(DeleteApi.class);
    }

    public void uploadImage(Context context, Uri imageUri, String fileName, FireStoreCallBack<ImgBBResponse.Data> callback) {
        if (IMGBB_API_KEY.equals("VOTRE_CLE_API_ICI")) {
            callback.onFailure("Clé API ImgBB non configurée dans StorageRepository");
            return;
        }

        File file = getFileFromUri(context, imageUri);
        if (file == null) {
            callback.onFailure("Impossible de récupérer le fichier de l'image");
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse(context.getContentResolver().getType(imageUri)), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        imgBBApi.uploadImage(IMGBB_API_KEY, body).enqueue(new Callback<ImgBBResponse>() {
            @Override
            public void onResponse(Call<ImgBBResponse> call, Response<ImgBBResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onFailure("Erreur ImgBB : " + response.message());
                }
                if (file.getParent().equals(context.getCacheDir().getPath())) {
                    file.delete();
                }
            }

            @Override
            public void onFailure(Call<ImgBBResponse> call, Throwable t) {
                callback.onFailure("Erreur réseau : " + t.getMessage());
            }
        });
    }

    public void deleteImage(String deleteUrl) {
        if (deleteUrl == null || deleteUrl.isEmpty()) return;
        
        deleteApi.deleteImage(deleteUrl).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // Suppression effectuée (ImgBB renvoie souvent du HTML ici, on ignore le contenu)
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Erreur silencieuse pour la suppression d'image
            }
        });
    }

    private File getFileFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = new File(context.getCacheDir(), "upload_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}