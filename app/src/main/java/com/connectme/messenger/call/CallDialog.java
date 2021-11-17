package com.connectme.messenger.call;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;
import com.connectme.messenger.Auth;
import com.connectme.messenger.Database;
import com.connectme.messenger.R;
import com.connectme.messenger.model.CallLog;
import com.connectme.messenger.model.User;
import java.util.List;

public class CallDialog extends Dialog {
    private TextView nametxt,phonetxt,statustxt;
    private Button answerbtn,hangupbtn;
    private ImageView playerimg;
    private Call call;
    private Context context;
    private Ringtone ringtone;
    private boolean incoming;

    public CallDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_call);
        setCancelable(false);
        this.context = context;
        Window window = getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.TOP;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        statustxt = findViewById(R.id.statustxt);
        nametxt = findViewById(R.id.nametxt);
        phonetxt = findViewById(R.id.phonetxt);
        answerbtn = findViewById(R.id.answerbtn);
        hangupbtn = findViewById(R.id.rejectbtn);
        playerimg = findViewById(R.id.playerimg);
        answerbtn.setOnClickListener(view -> onAnswer());
        hangupbtn.setOnClickListener(view -> onHangUp());
    }

    public void setRemoteUser(User user){
        nametxt.setText(user.getName());
        phonetxt.setText(user.getPhone());
        String photoUrl = user.getPhotoUrl();
        if(photoUrl!=null){
            Glide.with(playerimg.getContext())
                    .load(photoUrl)
                    .into(playerimg);
        }
    }

    public void setCall(Call call,boolean incoming){
        this.call = call;
        this.incoming = incoming;
        boolean videoCall = call.getDetails().isVideoOffered();
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(context,uri);
        this.call.addCallListener(callListener);
        if(incoming){
            ringtone.play();
            setCallStatus("Incoming Voice Call");
            if(videoCall)
                setCallStatus("Incoming Video Call");
        }else{
            answerbtn.setVisibility(View.INVISIBLE);
            hangupbtn.setText("Hang Up");
            setCallStatus("Outgoing Voice Call");
            if(videoCall)
                setCallStatus("Outgoing Video Call");
        }
    }


    private CallListener callListener = new CallListener() {
        @Override
        public void onCallProgressing(Call call) {

        }

        @Override
        public void onCallEstablished(Call call) {
            onAnswer();
        }

        @Override
        public void onCallEnded(Call call) {
            call.removeCallListener(callListener);
            Toast.makeText(context,"Call Ended",Toast.LENGTH_SHORT).show();
            CallManager.generateCallLog(call);
            ringtone.stop();
            dismiss();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
    };

    private void onAnswer(){
        ringtone.stop();
        hide();
        call.removeCallListener(callListener);
        call.answer();
        boolean videoCall = call.getDetails().isVideoOffered();
        if(videoCall){
            Intent intent = new Intent(context, VideoCallScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }else{
            Intent intent = new Intent(context, VoiceCallScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        dismiss();
    }

    private void onHangUp(){
        ringtone.stop();
        call.removeCallListener(callListener);
        call.hangup();
        dismiss();
    }

    private void setCallStatus(String status){
        statustxt.setText(status);
    }
}
