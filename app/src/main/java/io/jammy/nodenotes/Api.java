package io.jammy.nodenotes;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface Api {

  @GET ("notes/")
  Call<List<Note>> listAllNotes();
}
