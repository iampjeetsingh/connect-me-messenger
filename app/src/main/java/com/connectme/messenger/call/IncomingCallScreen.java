package com.connectme.messenger.call;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallDirection;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;
import com.connectme.messenger.App;
import com.connectme.messenger.Auth;
import com.connectme.messenger.Database;
import com.connectme.messenger.R;
import com.connectme.messenger.model.CallLog;
import com.connectme.messenger.model.User;

import java.util.List;

public class IncomingCallScreen extends AppCompatActivity {
    private String TAG = "IncomingCallScreen";
    private Context context = IncomingCallScreen.this;
    private Call call;
    private TextView nametxt,phonetxt, calltypetxt;
    private ImageView imageView;
    private DatabaseReference userRef;
    private User friend;
    private App app;
    private Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call_screen);
        nametxt = findViewById(R.id.nametxt);
        phonetxt = findViewById(R.id.phonetxt);
        calltypetxt = findViewById(R.id.calltypetxt);
        imageView = findViewById(R.id.imageView);

        app = (App) getApplication();
        call = app.call;
        userRef = Database.getUserRef(call.getRemoteUserId());
        if(call.getDetails().isVideoOffered()){
            calltypetxt.setText("Incoming Video Call");
        }else{
            calltypetxt.setText("Incoming Voice Call");
        }
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(context,uri);
    }

    public void pickCall(View v){
        call.answer();
        if(call.getDetails().isVideoOffered()){
            Intent intent = new Intent(context, VideoCallScreen.class);
            context.startActivity(intent);
        }else{
            Intent intent = new Intent(context, VoiceCallScreen.class);
            context.startActivity(intent);
        }
        finish();
    }

    public void endCall(View v){
        call.hangup();
    }

    private CallListener callListener = new CallListener() {
        @Override
        public void onCallProgressing(Call call) {

        }

        @Override
        public void onCallEstablished(Call call) {

        }

        @Override
        public void onCallEnded(Call call) {
            ringtone.stop();
            call.removeCallListener(callListener);
            CallManager.generateCallLog(call);
            app.call = null;
            Toast.makeText(context,"Call Ended",Toast.LENGTH_SHORT).show();
            finish();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
    };



    private ValueEventListener profileListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            friend = dataSnapshot.getValue(User.class);
            if(friend!=null){
                nametxt.setText(friend.getName());
                phonetxt.setText(friend.getPhone());
                if(friend.getPhotoUrl()!=null){
                    Glide.with(imageView.getContext())
                            .load(friend.getPhotoUrl())
                            .into(imageView);
                }
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG,"Listener was cancelled");
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        app.setContext(context);
        ringtone.play();
        call.addCallListener(callListener);
        userRef.addValueEventListener(profileListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(app.hasContext(context))
            app.setContext(null);
        ringtone.stop();
        call.removeCallListener(callListener);
        userRef.removeEventListener(profileListener);
    }
}
