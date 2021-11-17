package com.connectme.messenger.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.connectme.messenger.App;
import com.connectme.messenger.DIRECTORY;
import com.connectme.messenger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StartScreen extends AppCompatActivity {
    private String TAG = "StartScreen";
    private Context context = StartScreen.this;
    private List<String> listPermissionsNeeded;
    private App app;

    public static final int PERMISSIONS = 10;
    String[] permissions= new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        app = (App) getApplication();
        listPermissionsNeeded = new ArrayList<>();
    }

    @Override
    protected void onStart() {
        super.onStart();
        app.setContext(context);
        if(checkPermissions())
            start();
        else
            ActivityCompat.requestPermissions(StartScreen.this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    PERMISSIONS );

    }

    private void checkDirectory(){
        String[] array = {DIRECTORY.APP,DIRECTORY.IMAGE,DIRECTORY.VOICE,DIRECTORY.AUDIO,DIRECTORY.VIDEO,DIRECTORY.DOCUMENT};
        for(int i=0 ; i<array.length ; i++){
            File file = new File(array[i]);
            if(!file.exists()){
                file.mkdir();
                if(i!=0){
                    File sent = new File(array[i]+"/sent");
                    sent.mkdir();
                    File nomedia = new File(array[i]+"/sent/.nomedia");
                    try{
                        nomedia.createNewFile();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(app.hasContext(context))
            app.setContext(null);
    }

    private void start(){
        checkDirectory();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user==null){
            startActivity(new Intent(context, FirstWelcome.class));
        }else if(user.getDisplayName()==null){
            startActivity(new Intent(context, Username.class));
        }else{
            startActivity(new Intent(context, HomeActivity.class));
        }
    }

    private  boolean checkPermissions() {
        int result;
        List<String> permissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(context,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(p);
            }
        }
        if(!permissionsNeeded.isEmpty()) {
            listPermissionsNeeded = permissionsNeeded;
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(checkPermissions())
                        start();
                }
            }
        }
    }
}
