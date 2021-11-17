package com.connectme.messenger.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.connectme.messenger.App;
import com.connectme.messenger.R;


public class DeveloperInfo extends AppCompatActivity {
    Context context = DeveloperInfo.this;
    private App app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_info);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        app = (App) getApplication();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==android.R.id.home) {
            finish();
        }
        return true;
    }
    public void fbbtnclick(View v){
        try {
            this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("fb://profile/100018154058196")));
        } catch (Exception e) {
            this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://www.facebook.com/profile.php?id=100018154058196")));
        }
    }

    public void emailbtnclick(View v){
        startActivity(new Intent("android.intent.action.SENDTO", Uri.fromParts("mailto", "iampjeetsingh@gmail.com", null)));
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
