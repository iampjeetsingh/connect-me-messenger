package com.connectme.messenger.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.connectme.messenger.App;
import com.connectme.messenger.Auth;
import com.connectme.messenger.Database;
import com.connectme.messenger.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.connectme.messenger.model.User;

public class MyProfile extends AppCompatActivity {
    private Context context = MyProfile.this;
    TextView nametxt,statustxt,phonetxt;
    ImageView pic;
    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        app = (App) getApplication();
        nametxt = findViewById(R.id.nametxt);
        statustxt = findViewById(R.id.statustxt);
        phonetxt = findViewById(R.id.mobiletxt);
        pic = findViewById(R.id.profilepic);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        nametxt.setText(Auth.getDisplayName());
        phonetxt.setText(Auth.getPhone());
        if (Auth.getPhotoUrl()!=null) {
            Glide.with(pic.getContext())
                    .load(Auth.getPhotoUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(pic);
        }
        Database.getUserRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                statustxt.setText(user.getStatus());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==android.R.id.home) {
            finish();
        }
        return true;
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
