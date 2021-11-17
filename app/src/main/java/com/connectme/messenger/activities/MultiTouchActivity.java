package com.connectme.messenger.activities;

/**
 * Created by Paramjeet Singh on 9-7-17.
 */

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.connectme.messenger.App;
import com.connectme.messenger.Auth;
import com.connectme.messenger.DIRECTORY;
import com.connectme.messenger.R;
import com.connectme.messenger.TouchImageView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.connectme.messenger.model.Message;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class MultiTouchActivity extends AppCompatActivity {
    private Context context = MultiTouchActivity.this;
    String url;
    private ActionBar actionBar;
    private App app;
    private TouchImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_touch);
        imageView = findViewById(R.id.myimage);
        imageView.setMaxZoom(4f);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        url = getIntent().getStringExtra("url");
        app = (App) getApplication();
        loadImage();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==android.R.id.home) {
            finish();
        }
        return true;
    }

    public boolean isImageDownloaded(String fileName,boolean sent){
        File image = new File(DIRECTORY.IMAGE+"/"+fileName);
        File imageSent = new File(DIRECTORY.IMAGE+"/sent/"+fileName);
        if(image.exists() || (imageSent.exists() && sent)){
            return true;
        }
        return false;
    }
    private  File getImage(String fileName,boolean sent){
        if(sent)
            return new File(DIRECTORY.IMAGE+"/sent/"+fileName);
        return new File(DIRECTORY.IMAGE+"/"+fileName);
    }

    private void loadImage(){
        StorageReference reference = FirebaseStorage.getInstance().getReference().child(url.substring(1));
        String path = reference.getPath();
        boolean profilePic = path.contains("Profile_Photos");
        boolean chat = path.contains("Chat_Images");
        if(chat){
            String friend = getIntent().getStringExtra("friend");
            String messageID = getIntent().getStringExtra("message");
            Message message = app.getChatsSnapshot().child(friend).child(messageID).getValue(Message.class);
            boolean sent=false;
            if(message.getSenderUserID().equals(Auth.getUid())) {
                actionBar.setTitle("You");
                sent=true;
            }else
                actionBar.setTitle(message.getSenderName());
            actionBar.setSubtitle(getTimeFromTimeStamp(message.getTimeStamp()));
            if(isImageDownloaded(message.getFileName(),sent)){
                Glide.with(imageView.getContext())
                        .load(getImage(message.getFileName(),sent))
                        .apply(RequestOptions.fitCenterTransform())
                        .into(imageView);
            }
        }
    }

    private String getTimeFromTimeStamp(long timeStamp){
        DateFormat dateFormat = new SimpleDateFormat("hh:mm");
        return dateFormat.format(timeStamp);
    }

    @Override
    protected void onStart() {
        super.onStart();
        app.setContext(context);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(app.hasContext(context))
            app.setContext(null);
    }

}
