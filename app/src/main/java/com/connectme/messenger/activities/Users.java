package com.connectme.messenger.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Html;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.connectme.messenger.App;
import com.connectme.messenger.Database;
import com.connectme.messenger.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.connectme.messenger.model.User;
import com.connectme.messenger.viewholder.UserView;

/**
 * Created by Administrator on 17-05-2017.
 */

public class Users extends AppCompatActivity {
    private Context context = Users.this;
    DatabaseReference myRef;
    RecyclerView recyclerView;
    public FirebaseRecyclerAdapter<User, UserView> mFirebaseAdapter;
    LinearLayoutManager mLinearLayoutManager;
    FirebaseUser user;
    private App app;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        app = (App) getApplication();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(Html.fromHtml("Select Contact"));
        }
        user = FirebaseAuth.getInstance().getCurrentUser();
        myRef = Database.getUsersRef();
        myRef.keepSynced(true);
        recyclerView = (RecyclerView)findViewById(R.id.show_chat_recyclerView);
        mLinearLayoutManager = new LinearLayoutManager(Users.this);
        recyclerView.setLayoutManager(mLinearLayoutManager);
    }
    @Override
    public void onStart() {
        super.onStart();
        app.setContext(context);
        final ProgressBar pb = (ProgressBar)findViewById(R.id.myprogressBar);
        pb.setVisibility(View.VISIBLE);
        mFirebaseAdapter = new FirebaseRecyclerAdapter<User, UserView>(User.class, R.layout.users_single_item, UserView.class, myRef) {
            public void populateViewHolder(final UserView userView, User model, final int position) {
                pb.setVisibility(View.GONE);
                if (model.getName()!=null && model.getUserID()!=null && model.getPhone()!=null){
                    userView.setName(model.getName());
                    userView.setPhotoFromUrl(model.getPhotoUrl());
                    userView.setStatus(model.getStatus());
                    if((user.getUid().equals(model.getUserID())) || (!contactExists(Users.this,model.getPhone()) && !model.getPhone().equals("+917988428985"))) {
                        userView.Layout_hide();
                    }
                }else{
                    userView.Layout_hide();
                }
                userView.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        DatabaseReference ref = mFirebaseAdapter.getRef(position);
                        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                        intent.putExtra("id",ref.getKey());
                        startActivity(intent);
                    }
                });
            }
        };
        recyclerView.setAdapter(mFirebaseAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(app.hasContext(context))
            app.setContext(null);
    }

    public boolean contactExists(Context context, String number){
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
}
