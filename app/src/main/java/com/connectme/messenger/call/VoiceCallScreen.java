package com.connectme.messenger.call;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
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
import com.sinch.android.rtc.AudioController;
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

public class VoiceCallScreen extends AppCompatActivity {
    private String TAG = "VoiceCallScreen";
    private Context context = VoiceCallScreen.this;
    private Call call;
    private AudioController audioController;
    private boolean speakerPhoneOn=false,callMuted=false;
    private DatabaseReference userRef;
    private User friend;
    private TextView nametxt,phonetxt,timetxt;
    private ImageView imageView;
    private Thread thread;
    private int callTime = 0;
    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);
        nametxt = findViewById(R.id.nametxt);
        phonetxt = findViewById(R.id.phonetxt);
        timetxt = findViewById(R.id.timetxt);
        imageView = findViewById(R.id.imageView);

        app = (App) getApplication();
        audioController = app.sinchClient.getAudioController();
        call = app.call;
        call.addCallListener(callListener);
        userRef = Database.getUserRef(call.getRemoteUserId());
        thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!thread.isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(() -> {
                            callTime = call.getDetails().getDuration();
                            int minute = (callTime/60);
                            int csec = callTime-(minute*60);
                            String mm = ""+minute;
                            String ss = ""+csec;
                            if(mm.length()==1)
                                mm = "0"+mm;
                            if(ss.length()==1)
                                ss = "0"+ss;
                            timetxt.setText(mm+":"+ss);
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
    }

    public void toggleSpeakerPhone(View v){
        ImageView imageView = (ImageView)v;
        if(speakerPhoneOn){
            speakerPhoneOn=false;
            audioController.disableSpeaker();
            imageView.setImageResource(R.drawable.ic_speaker);
        }else{
            speakerPhoneOn=true;
            audioController.enableSpeaker();
            imageView.setImageResource(R.drawable.ic_speaker_off);
        }
    }

    public void endCall(View v){
        call.hangup();
    }

    public void muteCall(View v){
        ImageView imageView = (ImageView)v;
        if(callMuted){
            callMuted=false;
            audioController.unmute();
            imageView.setImageResource(R.drawable.ic_mic_off);
        }else{
            callMuted=true;
            audioController.mute();
            imageView.setImageResource(R.drawable.ic_mic);
        }
    }

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
    private CallListener callListener = new CallListener() {
        @Override
        public void onCallProgressing(Call call) {

        }

        @Override
        public void onCallEstablished(Call call) {

        }

        @Override
        public void onCallEnded(Call call) {
            thread.interrupt();
            Toast.makeText(context,"Call Ended",Toast.LENGTH_SHORT).show();
            CallManager.generateCallLog(call);
            finish();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        app.setContext(context);
        userRef.addValueEventListener(profileListener);
        thread.start();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(app.hasContext(context))
            app.setContext(null);
        thread.interrupt();
        userRef.removeEventListener(profileListener);
    }
}
