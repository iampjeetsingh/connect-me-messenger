package com.connectme.messenger.call;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallDirection;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;
import com.sinch.android.rtc.video.VideoScalingType;
import com.connectme.messenger.App;
import com.connectme.messenger.Auth;
import com.connectme.messenger.Database;
import com.connectme.messenger.R;
import com.connectme.messenger.model.CallLog;

import java.util.List;

public class VideoCallScreen extends AppCompatActivity {
    private Context context = VideoCallScreen.this;
    private App app;
    private SinchClient sinchClient;
    private Call call;
    private FrameLayout inputView,outputView;
    private LinearLayout linearLayout1,linearLayout2;
    private TextView timetxt;
    private boolean muted=false;
    private Thread thread;
    private int callTime = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        inputView = findViewById(R.id.videoInput);
        outputView = findViewById(R.id.videoOutput);
        timetxt = findViewById(R.id.timetxt);
        linearLayout1 = findViewById(R.id.linearLayout1);
        linearLayout2 = findViewById(R.id.linearLayout2);

        app = (App) getApplication();
        sinchClient = app.sinchClient;
        sinchClient.getAudioController().enableSpeaker();
        call = app.call;
        call.addCallListener(videoCallListener);
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

    public void endCall(View v){
        call.hangup();
    }

    public void switchCamera(View v){
        VideoController videoController = sinchClient.getVideoController();
        videoController.toggleCaptureDevicePosition();
    }

    public void muteCall(View v){
        AudioController audioController = sinchClient.getAudioController();
        ImageView imageView = (ImageView) v;
        if(muted) {
            muted=false;
            audioController.unmute();
            imageView.setImageResource(R.drawable.ic_mic_off);
        }else{
            muted=true;
            audioController.mute();
            imageView.setImageResource(R.drawable.ic_mic);
        }
    }

    public void toggleControls(View v){
        if(linearLayout1.getVisibility()==View.VISIBLE) {
            linearLayout1.setVisibility(View.GONE);
            linearLayout2.setVisibility(View.GONE);
            hideStatusBar();
        }else{
            linearLayout1.setVisibility(View.VISIBLE);
            linearLayout2.setVisibility(View.VISIBLE);
            showStatusBar();
        }
    }

    VideoCallListener videoCallListener = new VideoCallListener() {
        @Override
        public void onVideoTrackAdded(Call call) {
            VideoController vc = sinchClient.getVideoController();
            vc.setResizeBehaviour(VideoScalingType.ASPECT_FILL);
            View myPreview = vc.getLocalView();
            View remoteView = vc.getRemoteView();
            inputView.addView(myPreview);
            outputView.addView(remoteView);
        }

        @Override
        public void onVideoTrackPaused(Call call) {

        }

        @Override
        public void onVideoTrackResumed(Call call) {

        }

        @Override
        public void onCallProgressing(Call call) {

        }

        @Override
        public void onCallEstablished(Call call) {

        }

        @Override
        public void onCallEnded(Call call) {
            Toast.makeText(context,"Call Ended",Toast.LENGTH_SHORT).show();
            CallManager.generateCallLog(call);
            finish();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
    };

    private void hideStatusBar(){
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void showStatusBar(){
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        decorView.setSystemUiVisibility(uiOptions);
    }


    @Override
    protected void onStart() {
        super.onStart();
        app.setContext(context);
        thread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(app.hasContext(context))
            app.setContext(null);
        thread.interrupt();
    }
}
