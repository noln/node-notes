package io.jammy.nodenotes;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hudomju.swipe.OnItemClickListener;
import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.SwipeableItemClickListener;
import com.hudomju.swipe.adapter.RecyclerViewAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

  Api service;
  private RecyclerView mRecyclerView;
  private NotesAdapter mAdapter;
  private RecyclerView.LayoutManager mLayoutManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        final View dialogView = getLayoutInflater().inflate(R.layout.input_dialog, null);
        AlertDialog alertDialog = new AlertDialog.Builder(view.getContext()).create();
        alertDialog.setTitle("Create a Note");
        alertDialog.setIcon(R.drawable.ic_add_24dp);
        alertDialog.setCancelable(false);

        final EditText titleEditText = dialogView.findViewById(R.id.title_input);
        final EditText bodyEditText = dialogView.findViewById(R.id.body_input);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {

            service.createNote(titleEditText.getText().toString(), bodyEditText.getText().toString())
                .enqueue(new Callback<Note>() {

                  @Override
                  public void onResponse(Call<Note> call, Response<Note> response) {
                    try {
                      Timber.d(String.format(Locale.getDefault(), "Note Created [%s : %s : %s]", response.body().getId(), response.body().getTitle(), response.body().getText()));
                      mAdapter.addItem(response.body());
                    }
                    catch (Exception e) {
                      Timber.d("Note created, but with some empty fields");
                    }
                  }

                  @Override
                  public void onFailure(Call<Note> call, Throwable t) {
                    Timber.d(String.format(Locale.getDefault(), "onFailure :: %s", t.getMessage()));
                  }
                });
          }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        });

        alertDialog.setView(dialogView);
        alertDialog.show();
      }

    });

    // Set up the recyclerview
    mRecyclerView = findViewById(R.id.my_recycler_view);

    // use this setting to improve performance if you know that changes
    // in content do not change the layout size of the RecyclerView
    mRecyclerView.setHasFixedSize(true);

    // use a linear layout manager
    mLayoutManager = new LinearLayoutManager(this);
    mRecyclerView.setLayoutManager(mLayoutManager);

    // Set up adapter
    mAdapter = new NotesAdapter(new ArrayList<Note>());
    mRecyclerView.setAdapter(mAdapter);

    // Swipe to dismiss
    final SwipeToDismissTouchListener<RecyclerViewAdapter> touchListener =
        new SwipeToDismissTouchListener<>(
            new RecyclerViewAdapter(mRecyclerView),
            new SwipeToDismissTouchListener.DismissCallbacks<RecyclerViewAdapter>() {
              @Override
              public boolean canDismiss(int position) {

                Timber.d("SwipeToDismiss --> Can dismiss");

                return true;
              }

              @Override
              public void onPendingDismiss(RecyclerViewAdapter recyclerView, int position) {
                Timber.d("Pending dismiss...");
              }

              @Override
              public void onDismiss(RecyclerViewAdapter view, int position) {

                Timber.d("SwipeToDismiss --> On dismiss");

                service.removeNote(mAdapter.removeItem(position).getId()).enqueue(new Callback<String>() {
                  @Override
                  public void onResponse(Call<String> call, Response<String> response) {
                    Timber.d("Note Deleted : " + response.body());
                  }

                  @Override
                  public void onFailure(Call<String> call, Throwable t) {
                    Timber.e("Failed to delete note... [" + call.toString() + "]");
                  }
                });
              }
            });

    // Dismiss the item automatically after 3 seconds
    touchListener.setDismissDelay(3000);

    final Context context = this;

    mRecyclerView.setOnTouchListener(touchListener);
    mRecyclerView.setOnScrollListener((RecyclerView.OnScrollListener) touchListener.makeScrollListener());
    mRecyclerView.addOnItemTouchListener(new SwipeableItemClickListener(this,
        new OnItemClickListener() {
          @Override
          public void onItemClick(View view, int position) {
            if (view.getId() == R.id.txt_delete) {
              touchListener.processPendingDismisses();
            }
            else if (view.getId() == R.id.txt_undo) {
              touchListener.undoPendingDismiss();
            }
            else { // R.id.txt_data
              Toast.makeText(context, "Position " + position, LENGTH_SHORT).show();
            }
          }
        }));

    // Set up logging interceptor for debug
    HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
    interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

    // Load all notes
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("http://192.168.0.18:8000")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    service = retrofit.create(Api.class);

    Call<List<Note>> repos = service.listAllNotes();

    repos.enqueue(new Callback<List<Note>>() {
      @Override
      public void onResponse(Call<List<Note>> call, Response<List<Note>> response) {

        Timber.i("onResponse::%s", response.toString());

        for (Note note : response.body()) {
          Timber.v("::> Note [%s : %s : %s]", note.getId(), note.getTitle(), note.getText());

          mAdapter.addItem(note);
        }
      }

      @Override
      public void onFailure(Call<List<Note>> call, Throwable t) {
        Timber.e("> Error [%s]", t.getMessage());
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
