package com.connectme.messenger.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sinch.android.rtc.calling.Call;
import com.connectme.messenger.App;
import com.connectme.messenger.AttachDialog;
import com.connectme.messenger.Auth;
import com.connectme.messenger.DIRECTORY;
import com.connectme.messenger.Database;
import com.connectme.messenger.MESSAGE;
import com.connectme.messenger.R;
import com.connectme.messenger.call.CallDialog;
import com.connectme.messenger.filetransfer.FileUploader;
import com.connectme.messenger.model.Message;
import com.connectme.messenger.model.User;
import com.connectme.messenger.viewholder.MessageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class ChatActivity extends AppCompatActivity {
    Context context = ChatActivity.this;
    private String TAG = "ChatActivity";
    public RecyclerView recyclerView;
    private DatabaseReference myChatRef,frndChatRef,frndProfileRef;
    private FirebaseRecyclerAdapter<Message, MessageView> mFirebaseAdapter;
    public LinearLayoutManager mLinearLayoutManager;
    static String Sender_Name;
    static String User_ID;
    static String User_Name;
    static String Frnd_ID;
    ImageView attach_icon,send_icon,recordButton,no_data_available_image;
    EditText message_area;
    TextView no_chat;
    private ProgressDialog progressDialog;
    ProgressBar progressBar;
    FirebaseUser user;
    ImageView frndimg,onlinedot;
    TextView frndnametxt,frndstatustxt;
    App app;
    Call call;
    User friend;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        app =(App) getApplication();
        user = FirebaseAuth.getInstance().getCurrentUser();
        User_ID = user.getUid();
        if(user.getDisplayName()!=null)
            User_Name = user.getDisplayName();
        Frnd_ID = getIntent().getStringExtra("id");
        frndimg = findViewById(R.id.frnd_image);
        onlinedot = findViewById(R.id.online_dot);
        frndnametxt = findViewById(R.id.frnd_name);
        frndstatustxt = findViewById(R.id.frnd_status);
        recordButton = findViewById(R.id.recordButton);
        //recordButton.setOnTouchListener(holdListener);
        myChatRef = Database.getChatRef(User_ID,Frnd_ID);
        myChatRef.keepSynced(true);
        frndChatRef = Database.getChatRef(Frnd_ID,User_ID);
        frndChatRef.keepSynced(true);
        Sender_Name = getIntent().getStringExtra("name");
        recyclerView = findViewById(R.id.fragment_chat_recycler_view);
        attach_icon = findViewById(R.id.attachButton);
        send_icon = findViewById(R.id.sendButton);
        no_data_available_image = findViewById(R.id.no_data_available_image);
        message_area = findViewById(R.id.messageArea);
        progressDialog = new ProgressDialog(this);
        progressBar = findViewById(R.id.progressBar3);
        no_chat = findViewById(R.id.no_chat_text);
        mLinearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        mLinearLayoutManager.setStackFromEnd(true);
        ((EditText)findViewById(R.id.messageArea)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(((EditText)findViewById(R.id.messageArea)).getText().toString().length()>1){
                    Database.getUserRef().child("typing").setValue(true);
                }else {
                    Database.getUserRef().child("typing").setValue(false);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }});
        frndProfileRef = Database.getUserRef(Frnd_ID);
        frndProfileRef.keepSynced(true);
    }

    private void setUpEventListeners(){
        frndProfileRef.addValueEventListener(frndProfileListener);
    }
    private void removeEventListeners(){
        frndProfileRef.removeEventListener(frndProfileListener);
    }
    private ValueEventListener frndProfileListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            friend = dataSnapshot.getValue(User.class);
            frndnametxt.setText(friend.getName());
            if(friend.getPhotoUrl()!=null){
                Glide.with(ChatActivity.this)
                        .asDrawable()
                        .load(friend.getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(frndimg);
            }
            if(friend.isOnline()){
                onlinedot.setVisibility(View.VISIBLE);
                if(friend.getTalkingTo()!=null){
                    if(friend.getTalkingTo().equals(User_ID)){
                        if(friend.isTyping()) {
                            frndstatustxt.setText("Typing...");
                        }else{
                            frndstatustxt.setText("Connected");
                        }
                    }else{
                        frndstatustxt.setText("Online");
                    }
                }else{
                    frndstatustxt.setText("Online");
                }
            }else{
                onlinedot.setVisibility(View.INVISIBLE);
                frndstatustxt.setText(getLastSeenText(friend.getLastSeen()));
            }
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };
    private AttachDialog.ResponseListener responseListener = new AttachDialog.ResponseListener() {
        @Override
        public void onAttachImageClick() {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.putExtra("return-data", true);
            startActivityForResult(intent, MESSAGE.IMAGE);
        }
        @Override
        public void onAttachAudioClick() {
            Intent intent = new Intent();
            //startActivityForResult(intent,MESSAGE.AUDIO);
        }
        @Override
        public void onAttachVideoClick() {
            Intent intent = new Intent();
            //startActivityForResult(intent,MESSAGE.VIDEO);
        }
        @Override
        public void onAttachDocumentClick() {
            Intent intent = new Intent();
            //startActivityForResult(intent,MESSAGE.DOCUMENT);
        }
        @Override
        public void onAttachLocationClick() {
            Intent intent = new Intent();
            //startActivityForResult(intent,MESSAGE.LOCATION);
        }
        @Override
        public void onAttachContactClick() {
            Intent intent = new Intent();
            //startActivityForResult(intent,MESSAGE.CONTACT);
        }
    };

    //Handling User Clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==android.R.id.home) {
            finish();
        }
        return true;
    }
    public void sendClick(View v){
        String messageText = message_area.getText().toString().trim();
        if(messageText.length()!=0){
            Message message = new Message();
            message.setType(MESSAGE.TEXT);
            message.setText(messageText);
            message.setSenderName(Auth.getDisplayName());
            message.setSenderUserID(Auth.getUid());
            message.setSeen(false);
            String key = myChatRef.push().getKey();
            message.setId(key);
            myChatRef.child(key).setValue(message);
            frndChatRef.child(key).setValue(message);
            myChatRef.child(key).child("timeStamp").setValue(ServerValue.TIMESTAMP);
            frndChatRef.child(key).child("timeStamp").setValue(ServerValue.TIMESTAMP);
            message_area.setText("");
            if(recyclerView.getAdapter().getItemCount()>1)
                recyclerView.postDelayed(() -> recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount()-1)
                    ,500);
        }
    }
    public void photoClick(View v){
        if(friend.getPhotoUrl()!=null){
            String timestamp = new SimpleDateFormat("MMddHHmm").format(Calendar.getInstance().getTime());
            Intent intent = (new Intent(ChatActivity.this, MultiTouchActivity.class));
            intent.putExtra("url",friend.getPhotoUrl());
            intent.putExtra("id", friend.getName()+"_"+timestamp);
            intent.putExtra("profilepic",true);
            intent.putExtra("name",friend.getName());
            startActivity(intent);
        }
    }
    public void attachClick(View v){
        AttachDialog dialog = new AttachDialog(context);
        dialog.setResponseListener(responseListener);
        dialog.show();
    }
    public void callClick(View v){
        call = app.callClient.callUser(Frnd_ID);
        app.call = call;
        User user = new User();
        user.setName(friend.getName());
        user.setPhotoUrl(friend.getPhotoUrl());
        user.setPhone(friend.getPhone());
        CallDialog dialog = new CallDialog(context);
        dialog.setRemoteUser(user);
        dialog.setCall(call,false);
        dialog.show();
    }
    public void videoCallClick(View v){
        call = app.callClient.callUserVideo(Frnd_ID);
        app.call = call;
        User user = new User();
        user.setName(friend.getName());
        user.setPhotoUrl(friend.getPhotoUrl());
        user.setPhone(friend.getPhone());
        CallDialog dialog = new CallDialog(context);
        dialog.setRemoteUser(user);
        dialog.setCall(call,false);
        dialog.show();
    }
    public void dotMenuClick(View v){
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.chat_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return false;
            }
        });
        popup.show();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private MediaRecorder recorder;
    private File audioFile;
    private View.OnTouchListener holdListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            ImageView imageView = (ImageView)view;
            if(motionEvent.getAction() == android.view.MotionEvent.ACTION_DOWN ) {
                //On Hold
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                String timestamp = format.format(Calendar.getInstance().getTime());
                audioFile = new File(DIRECTORY.AUDIO+"/"+timestamp+".3gpp");
                recorder.setOutputFile(audioFile.getPath());
                try {
                    recorder.prepare();
                    recorder.start();
                    imageView.setBackground(getResources().getDrawable(R.drawable.circle_green));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }else if(motionEvent.getAction() == android.view.MotionEvent.ACTION_UP){
                //On Released
                try {
                    recorder.stop();
                    imageView.setBackground(getResources().getDrawable(R.drawable.circle_blue));
                    recorder.release();

                    StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                            .child("Audios").child(Auth.getUid()).child(audioFile.getName());
                    Message message = new Message();
                    message.setSenderName(Auth.getDisplayName());
                    message.setSenderUserID(Auth.getUid());
                    message.setSeen(false);
                    message.setType(MESSAGE.AUDIO);
                    message.setFileName(audioFile.getName());
                    message.setUrl(storageReference.getPath());
                    String key = myChatRef.push().getKey();
                    message.setId(key);
                    if (key != null) {
                        myChatRef.child(key).setValue(message);
                        frndChatRef.child(key).setValue(message);
                        myChatRef.child(key).child("timeStamp").setValue(ServerValue.TIMESTAMP);
                        frndChatRef.child(key).child("timeStamp").setValue(ServerValue.TIMESTAMP);
                        new FileUploader().equals(new com.connectme.messenger.filetransfer.UploadTask(storageReference,audioFile));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return true;
            }
            return false;
        }
    };

    private String getLastSeenText(long lastSeen){
        DateFormat year = new SimpleDateFormat("yyyy");
        DateFormat month = new SimpleDateFormat("MM");
        DateFormat day = new SimpleDateFormat("dd");
        DateFormat hour = new SimpleDateFormat("hh");
        DateFormat minutes = new SimpleDateFormat("mm");
        Date now = Calendar.getInstance().getTime();
        int yearDiff = Integer.parseInt(year.format(now))-Integer.parseInt(year.format(lastSeen));
        int monthDiff = Integer.parseInt(month.format(now))-Integer.parseInt(month.format(lastSeen));
        int dayDiff = Integer.parseInt(day.format(now))-Integer.parseInt(day.format(lastSeen));
        int hourDiff = Integer.parseInt(hour.format(now))-Integer.parseInt(hour.format(lastSeen));
        int minutesDiff = Integer.parseInt(minutes.format(now))-Integer.parseInt(minutes.format(lastSeen));
        if(yearDiff>0){
            if(yearDiff==1)
                return "1 year ago";
            else
                return yearDiff+" years ago";
        }else if(monthDiff>0){
            if(monthDiff==1)
                return "1 month ago";
            else
                return monthDiff+" months ago";
        }else if(dayDiff>0){
            if(dayDiff==1)
                return "1 day ago";
            else
                return dayDiff+" days ago";
        }else if(hourDiff>0){
            if(hourDiff==1)
                return "1 hour ago";
            else
                return hourDiff+" hours ago";
        }else if(minutesDiff>0){
            if(minutesDiff==1)
                return "1 minute ago";
            else
                return minutesDiff+" minutes ago";
        }else
            return "0 minute ago";
    }

    @Override
    public void onStart() {
        super.onStart();
        app.setContext(context);
        setUpEventListeners();
        Database.getUserRef().child("typing").setValue(false);
        Database.getUserRef().child("talkingTo").setValue(Frnd_ID);
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Message, MessageView>(Message.class, R.layout.chat_single_item, MessageView.class, myChatRef) {
            public void populateViewHolder(final MessageView messageView, Message message, final int position) {
                messageView.setMessage(message);
                messageView.view.setOnClickListener(v -> {
                    if(message.getType()==MESSAGE.IMAGE){
                        Intent intent = (new Intent(ChatActivity.this,MultiTouchActivity.class));
                        intent.putExtra("url",message.getUrl());
                        DatabaseReference reference = mFirebaseAdapter.getRef(position);
                        Log.e(TAG,""+reference);
                        intent.putExtra("friend",reference.getParent().getKey());
                        intent.putExtra("message",reference.getKey());
                        startActivity(intent);
                    }
                });
            }

            @Override
            protected void onCancelled(DatabaseError error) {
                super.onCancelled(error);
                Log.e(TAG,"Error "+error.getMessage());
            }
        };
        recyclerView.setAdapter(mFirebaseAdapter);
        if(app.userTalkedWith(Frnd_ID)) {
            progressBar.setVisibility(ProgressBar.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            no_data_available_image.setVisibility(View.GONE);
            no_chat.setVisibility(View.GONE);
            recyclerView.postDelayed(() -> recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount()-1), 500);
            recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                if (bottom < oldBottom) {
                    recyclerView.postDelayed(() -> recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount()-1), 100);
                }
            });
        } else {
            progressBar.setVisibility(ProgressBar.GONE);
            recyclerView.setVisibility(View.GONE);
            no_data_available_image.setVisibility(View.VISIBLE);
            no_chat.setVisibility(View.VISIBLE);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(app.hasContext(context))
            app.setContext(null);
        removeEventListeners();
        Database.getUserRef().child("typing").setValue(null);
        Database.getUserRef().child("talkingTo").setValue(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == RESULT_OK){
            if (requestCode == MESSAGE.IMAGE) {
                final Uri selectedImage = intent.getData();
                File source = new File(selectedImage+"");
                @SuppressLint("SimpleDateFormat") String timeStamp =
                        new SimpleDateFormat("yyyyMMddHHmm").format(Calendar.getInstance().getTime());
                File file = backupFile(source,DIRECTORY.IMAGE+"/sent",timeStamp);
                StorageReference filePath =
                        FirebaseStorage.getInstance().getReference().child("Chat_Images").child(User_ID).child(timeStamp);
                progressDialog.setMessage("Uploading...");
                progressDialog.show();
                filePath.putFile(selectedImage)
                        .addOnSuccessListener(taskSnapshot -> {
                            progressDialog.dismiss();
                            Message message = new Message();
                            message.setSenderName(Auth.getDisplayName());
                            message.setSenderUserID(Auth.getUid());
                            message.setType(MESSAGE.IMAGE);
                            message.setUrl(filePath.getPath());
                            message.setFileName(filePath.getName());
                            message.setSeen(false);
                            String messageID = myChatRef.push().getKey();
                            message.setId(messageID);
                            myChatRef.child(messageID).setValue(message);
                            frndChatRef.child(messageID).setValue(message);
                            myChatRef.child(messageID).child("timeStamp").setValue(ServerValue.TIMESTAMP);
                            frndChatRef.child(messageID).child("timeStamp").setValue(ServerValue.TIMESTAMP);
                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(context,"Something Went Wrong",Toast.LENGTH_SHORT).show();
                            Log.e(TAG,e.getMessage());
                        });
            }
            else if(requestCode==MESSAGE.AUDIO){

            }
            else if(requestCode==MESSAGE.VIDEO){

            }
            else if(requestCode==MESSAGE.DOCUMENT){

            }
            else if(requestCode==MESSAGE.LOCATION){

            }
            else if(requestCode==MESSAGE.CONTACT){

            }
        }
    }

    private File backupFile(File source,String directory,String name) {
        File destination = new File(directory+"/"+name);
        try {
            copyFile(source.getParentFile().getPath(),source.getPath(),destination.getParentFile().getPath());
            return destination;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private void copyFile(String inputPath, String inputFile, String outputPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists()){
                dir.mkdirs();
            }
            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;
        }  catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }
}