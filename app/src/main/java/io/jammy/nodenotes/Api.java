package io.jammy.nodenotes;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface Api {

  @GET ("notes/")
  Call<List<Note>> listAllNotes();

  @FormUrlEncoded
  @POST ("notes/")
  Call<Note> createNote(@Field ("title") String title, @Field ("body") String body);

  @DELETE("notes/{id}")
  Call<String> removeNote(@Path ("id") String id);
}
