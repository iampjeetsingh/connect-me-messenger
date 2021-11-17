package com.connectme.messenger.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.connectme.messenger.model.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    private Context context = SettingsActivity.this;
    private ListView optlist;
    private ImageView userimg;
    private TextView userNameTextView, userPhoneTextView;
    private LinearLayout userDetails;
    private DatabaseReference mref;
    private App app;

    String[] opttexts = {"Account","Invite a friend","Rate Us","About Developer"};
    int[] optimgs = {R.drawable.ic_account,R.drawable.ic_friends,
            R.drawable.ic_rate_us,R.drawable.ic_account};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        app = (App) getApplication();
        optlist = findViewById(R.id.opt_list);
        userDetails = findViewById(R.id.user_details_cont);
        userNameTextView = findViewById(R.id.user_name);
        userPhoneTextView = findViewById(R.id.user_phone);
        userimg = findViewById(R.id.user_image);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        userDetails.setOnClickListener(view -> startActivity(new Intent(SettingsActivity.this, MyProfile.class)));
        List<HashMap<String, String>> optList = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < 4; i++) {
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("text",opttexts[i]);
            hm.put("image", Integer.toString(optimgs[i]));
            optList.add(hm);
        }
        String[] from = {"image", "text"};
        int[] to = {R.id.opt_img, R.id.opt_txt};
        SimpleAdapter simpleAdapter = new SimpleAdapter(getBaseContext(), optList, R.layout.settings_single_item ,from, to);
        optlist.setAdapter(simpleAdapter);
        userNameTextView.setText(Auth.getDisplayName());
        if(Auth.getPhotoUrl()!=null){
            Glide.with(SettingsActivity.this)
                    .load(Auth.getPhotoUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(userimg);
        }
        mref = Database.getUserRef();
        mref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                userPhoneTextView.setText(user.getPhone());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        optlist.setOnItemClickListener((adapterView, view, i, l) -> {
            if(i==0){
                startActivity(new Intent(SettingsActivity.this, AccountSettings.class));
            }else if(i==1){
                try {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Connect Me - Messenger");
                    String str = "\nInstall this instant messaging app, Connect Me\n\n";
                    str = str + "https://play.google.com/store/apps/details?id="+getApplicationContext().getPackageName()+"\n\n";
                    intent.putExtra(Intent.EXTRA_TEXT, str);
                    startActivity(Intent.createChooser(intent, "choose one"));
                } catch(Exception e) {
                    e.printStackTrace();
                }
            } else if(i==2){
                Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
                }
            }else if(i==3){
                startActivity(new Intent(SettingsActivity.this, DeveloperInfo.class));
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
