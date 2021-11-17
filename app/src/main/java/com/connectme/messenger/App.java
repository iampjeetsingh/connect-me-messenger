package com.connectme.messenger;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.connectme.messenger.call.CallDialog;
import com.connectme.messenger.call.IncomingCallScreen;
import com.connectme.messenger.call.VideoCallScreen;
import com.connectme.messenger.filetransfer.FileDownloader;
import com.connectme.messenger.model.Message;
import com.connectme.messenger.model.User;
import java.util.Iterator;

public class App extends Application {
    private String TAG = "App";
    private FirebaseAuth.AuthStateListener authStateListener;
    private static FirebaseDatabase firebaseDatabase;
    public SinchClient sinchClient;
    public CallClient callClient;
    public Call call;
    private Context context;
    private Connection connection;

    public DataSnapshot getChatsSnapshot() {
        return chatsSnapshot;
    }

    protected DataSnapshot chatsSnapshot;
    private MessageListener messageListener;
    @Override
    public void onCreate() {
        super.onCreate();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.setPersistenceEnabled(true);
        if(Auth.isNull()){
            authStateListener = firebaseAuth -> {
                if(!Auth.isNull()){
                    FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
                    onStart();
                }
            };
            FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
        }else{
            onStart();
        }
    }
    public void onStart(){
        startSinch();
        listenConnection();
        listenChats();
    }

    private void listenConnection(){
        connection = Database.getConnection();
        connection.addStateListener(new Connection.StateListener() {
            @Override
            public void onConnected() {
                DatabaseReference mRef = Database.getUserRef();
                mRef.child("online").setValue(true);
                mRef.child("online").onDisconnect().setValue(false);
                mRef.child("lastSeen").onDisconnect().setValue(ServerValue.TIMESTAMP);
            }

            @Override
            public void onDisconnected() {

            }
        });
    }
    private void listenChats(){
        DatabaseReference chatsRef = Database.getChatsRef();
        chatsRef.keepSynced(true);
        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatsSnapshot=dataSnapshot;
                if(messageListener!=null)
                    messageListener.onNewMessage();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("App","Listener was cancelled "+databaseError);
            }
        });
    }
    private void startSinch(){
        String API_KEY = com.connectme.messenger.BuildConfig.sinch_api_key;
        String API_SECRET = com.connectme.messenger.BuildConfig.sinch_api_secret;
        String ENVIRONMENT_HOST = com.connectme.messenger.BuildConfig.environment_host;
        sinchClient = Sinch.getSinchClientBuilder().context(getApplicationContext())
                .applicationKey(API_KEY)
                .applicationSecret(API_SECRET)
                .environmentHost(ENVIRONMENT_HOST)
                .userId(Auth.getUid())
                .build();
        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.addSinchClientListener(new SinchClientListener() {
            public void onClientStarted(SinchClient client) { }
            public void onClientStopped(SinchClient client) { }
            public void onClientFailed(SinchClient client, SinchError error) {
                Log.e(TAG,"sinch onClientFailed");
            }
            public void onRegistrationCredentialsRequired(SinchClient client, ClientRegistration registrationCallback) { }
            public void onLogMessage(int level, String area, String message) {
                Log.println(level,area,message);
            }
        });
        callClient = sinchClient.getCallClient();
        callClient.addCallClientListener((callClient, call) -> {
            App.this.call = call;
            String remoteId = call.getRemoteUserId();
            Database.getUserRef(remoteId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user==null)
                        return;
                    if(context!=null){
                        CallDialog dialog = new CallDialog(context);
                        dialog.setRemoteUser(user);
                        dialog.setCall(call,true);
                        dialog.show();
                    }else{
                        Intent intent = new Intent(getApplicationContext(), IncomingCallScreen.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        });
        sinchClient.start();
    }

    public interface MessageListener{
        void onNewMessage();
    }
    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }
    public void removeMessageListener(){
        this.messageListener=null;
    }

    public boolean userTalkedWith(String uid){
        if(chatsSnapshot==null)
            return false;
        return chatsSnapshot.hasChild(uid);
    }
    public int getUnreadCountFrom(String uid){
        int unreadCount = 0;
        if(chatsSnapshot!=null){
            for (DataSnapshot dataSnapshot : chatsSnapshot.child(uid).getChildren()) {
                Message message = dataSnapshot.getValue(Message.class);
                if (!message.isSeen() && !message.getSenderUserID().equals(Auth.getUid())) {
                    unreadCount = unreadCount + 1;
                }
            }
        }
        return unreadCount;
    }
    public boolean contactExists(Context context,String number){
        Uri lookupuri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.DISPLAY_NAME};
        try (Cursor cur = context.getContentResolver().query(lookupuri, mPhoneNumberProjection, null, null, null)) {
            if (cur.moveToFirst())
                return true;
        }
        return false;
    }

    public static FirebaseDatabase getDatabase(){
        return firebaseDatabase;
    }
    public void setContext(Context context) {
        this.context = context;
    }
    public Context getContext() {
        return this.context;
    }
    public boolean hasContext(Context context) {
        return this.context == context;
    }
}