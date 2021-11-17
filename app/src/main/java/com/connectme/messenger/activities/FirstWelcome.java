package com.connectme.messenger.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.connectme.messenger.App;
import com.connectme.messenger.R;

public class FirstWelcome extends AppCompatActivity {
    Context context = FirstWelcome.this;
    private App app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(FirebaseAuth.getInstance().getCurrentUser()!= null){
            startActivity(new Intent(FirstWelcome.this, HomeActivity.class));
        }
        setContentView(R.layout.first_welcome);
        app = (App) getApplication();
    }
    public void agreeclick(View v){
        startActivity(new Intent(FirstWelcome.this, Register.class));
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
