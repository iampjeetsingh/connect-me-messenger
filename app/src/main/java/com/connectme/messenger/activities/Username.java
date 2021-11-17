package com.connectme.messenger.activities;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.connectme.messenger.App;
import com.connectme.messenger.Auth;
import com.connectme.messenger.Database;
import com.connectme.messenger.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Username extends AppCompatActivity {
    private Context context = Username.this;
    private String TAG = "Username";
    private int GALLERY_INTENT = 2;
    private EditText nametxt;
    private ImageView profilepic;

    private StorageReference reference;
    private Bitmap bitmap;
    private Uri photoUri;
    private String name;
    private  ProgressDialog progressDialog;
    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);
        app = (App) getApplication();
        nametxt = findViewById(R.id.nametxt);
        profilepic = findViewById(R.id.userimage);
        if(Auth.isNull()){
            startActivity(new Intent(Username.this, Register.class));
            return;
        }
        nametxt.setText(Auth.getDisplayName());
        if(Auth.getPhotoUrl()!=null){
            Glide.with(profilepic.getContext())
                    .load(Auth.getPhotoUrl())
                    .thumbnail(0.5f)
                    .into(profilepic);
        }
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        reference = FirebaseStorage.getInstance().getReference().child("Profile_Photos").child(Auth.getUid());
    }
    public void photoclick(View v){
        callGalary();
    }
    public void updatename(final View v){
        name = nametxt.getText().toString();
        if(name.equals("")){
            name = Auth.getDisplayName();
        }
        if(bitmap==null){
            photoUri = Auth.getUser().getPhotoUrl();
            progressDialog.setMessage("Updating Profile...");
            progressDialog.show();
            updateData();
        }else{
            progressDialog.setMessage("Uploading Photo...");
            progressDialog.show();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bytes = baos.toByteArray();
            final UploadTask uploadTask = reference.putBytes(bytes);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return reference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        photoUri = downloadUri;
                        Toast.makeText(context,"Photo Uploaded",Toast.LENGTH_SHORT).show();
                        progressDialog.setMessage("Updating Profile...");
                        updateData();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(context,"Something went wrong",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void updateData(){
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(photoUri)
                .setDisplayName(name)
                .build();
        Auth.getUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Map<String,Object> map = new HashMap<>();
                    map.put("phone",Auth.getPhone());
                    map.put("userID",Auth.getUid());
                    map.put("name",name);
                    String photoUrl = null;
                    if(photoUri!=null)
                        photoUrl = photoUri.toString();
                    map.put("photoUrl",photoUrl);
                    map.put("status","Hi! I am Connected");
                    Database.getUserRef().updateChildren(map);
                    progressDialog.dismiss();
                    Toast.makeText(context,"Profile Updated",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(context, HomeActivity.class));
                }else{
                    progressDialog.dismiss();
                    Toast.makeText(context,"Something went wrong",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void callGalary() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        try {
            intent.putExtra("return-data", true);
            startActivityForResult(intent, GALLERY_INTENT);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG,""+e.getMessage());
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                bitmap = extras.getParcelable("data");
                profilepic.setImageBitmap(bitmap);
            }
        }
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
