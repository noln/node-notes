package io.jammy.nodenotes;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Note {

  @SerializedName ("_id")
  @Expose
  private String id;

  @SerializedName ("text")
  @Expose
  private String text;

  @SerializedName ("title")
  @Expose

  private String title;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
