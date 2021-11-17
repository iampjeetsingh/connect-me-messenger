package com.connectme.messenger.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.connectme.messenger.App;
import com.connectme.messenger.Database;
import com.connectme.messenger.R;

public class AccountSettings extends AppCompatActivity {
    FirebaseUser user;
    Context context=AccountSettings.this;
    private App app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        user = FirebaseAuth.getInstance().getCurrentUser();
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
    public void deleteAccount(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(AccountSettings.this);
        builder.setCancelable(false);
        builder.setMessage("Are you sure you want to delete your account");
        builder.setPositiveButton("Yes", (dialog, which) -> {

        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {

        });
        builder.create().show();
    }
    public void clearChats(final View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(AccountSettings.this);
        builder.setCancelable(false);
        builder.setMessage("Are you sure that you want to delete all of chats");
        builder.setPositiveButton("Yes", (dialog, which) -> Database.getChatsRef().setValue(null)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Snackbar.make(v,"All Chats Cleared",Snackbar.LENGTH_SHORT).show();
                    }else{
                        Snackbar.make(v,"Something went wrong", Snackbar.LENGTH_SHORT).show();
                    }
                }));
        builder.setNegativeButton("Cancel", (dialog, which) -> {
        });
        builder.create().show();
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
