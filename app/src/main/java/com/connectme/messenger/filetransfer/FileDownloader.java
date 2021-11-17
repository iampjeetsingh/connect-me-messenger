package com.connectme.messenger.filetransfer;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.storage.StorageReference;
import java.io.File;

public class FileDownloader extends AsyncTask<DownloadTask,Integer,Boolean> {
    private String TAG = "FileDownloader";
    private boolean completed;

    @Override
    protected Boolean doInBackground(DownloadTask... downloadDetails) {
        completed = false;
        try {
            DownloadTask details = downloadDetails[0];
            StorageReference storageReference = details.getReference();
            ProgressBar progressBar = details.getProgressBar();
            ImageView resultImageView = details.getResultImageView();
            File file = details.getFile();
            storageReference.getFile(file)
                    .addOnSuccessListener(taskSnapshot -> {
                        completed=true;
                        progressBar.setVisibility(View.GONE);
                        if(resultImageView!=null){
                            Glide.with(resultImageView.getContext())
                                    .load(file)
                                    .apply(RequestOptions.centerCropTransform())
                                    .thumbnail(0.25f)
                                    .into(resultImageView);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG,"File not downloaded "+e.getMessage());
                        e.printStackTrace();
                        progressBar.setVisibility(View.GONE);
                        completed=false;
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        String divide = ""+(taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                        int percent = Integer.parseInt(divide);
                        progressBar.setProgress(percent);
                    });
        }catch(Exception e){
            completed=false;
            Log.e(TAG,"File not downloaded "+e.getMessage());
        }
        return completed;
    }
}
