package com.connectme.messenger.activities;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.connectme.messenger.App;
import com.connectme.messenger.R;
import com.google.firebase.auth.FirebaseAuth;

public class MessageDetails extends AppCompatActivity {
    private TextView bytxt,fromtxt,totxt,messagetxt,timetxt,urltxt;
    private String by,to,from,message,time,date;
    private Context context = MessageDetails.this;
    private App app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_details);
        app = (App) getApplication();
        bytxt = findViewById(R.id.by);
        fromtxt = findViewById(R.id.from);
        totxt = findViewById(R.id.to);
        messagetxt = findViewById(R.id.message);
        timetxt = findViewById(R.id.time);
        by = getIntent().getExtras().getString("by");
        from = getIntent().getExtras().getString("from");
        to = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        message = getIntent().getExtras().getString("message");
        time = getIntent().getExtras().getString("time");
        date = getIntent().getExtras().getString("date");
        bytxt.setText("Sent by :- "+by);
        fromtxt.setText("Sent from :- "+from);
        totxt.setText("Sent to :- "+to);
        messagetxt.setText("Message :- "+message);
        timetxt.setText("Sent at :- "+time+" on "+date);
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
