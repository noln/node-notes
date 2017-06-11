package io.jammy.nodenotes;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

  private List<Note> mDataset = new ArrayList<>();

  public void setDataset(List<Note> mDataset) {
    this.mDataset = mDataset;
  }

  public NotesAdapter(List<Note> mDataset) {
    this.mDataset = mDataset;
  }

  // Create new views (invoked by the layout manager)
  @Override
  public NotesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    // create a new view
    ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.my_text_view, parent, false);

    // set the view's size, margins, paddings and layout parameters
    ViewHolder vh = new ViewHolder(v);
    return vh;
  }

  // Replace the contents of a view (invoked by the layout manager)
  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    // - get element from your dataset at this position
    // - replace the contents of the view with that element
    holder.textTitle.setText(mDataset.get(position).getTitle());
    holder.textBody.setText(mDataset.get(position).getText());
  }

  // Return the size of your dataset (invoked by the layout manager)
  @Override
  public int getItemCount() {
    return mDataset.size();
  }

  // Provide a reference to the views for each data item
  // Complex data items may need more than one view per item, and
  // you provide access to all the views for a data item in a view holder
  public static class ViewHolder extends RecyclerView.ViewHolder {

    // each data item is just a string in this case
    public TextView textTitle, textBody;

    public ViewHolder(ConstraintLayout v) {
      super(v);
      textTitle = (TextView) v.findViewById(R.id.title);
      textBody = (TextView) v.findViewById(R.id.body);
    }
  }
}

