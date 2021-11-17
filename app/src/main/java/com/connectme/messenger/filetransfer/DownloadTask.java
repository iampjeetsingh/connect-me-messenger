package com.connectme.messenger.filetransfer;

import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.firebase.storage.StorageReference;

import java.io.File;

public class DownloadTask {
    private StorageReference reference;
    private File file;
    private ProgressBar progressBar;
    private ImageView resultImageView;

    public DownloadTask(StorageReference reference, File file) {
        this.reference = reference;
        this.file = file;
    }

    public StorageReference getReference() {
        return reference;
    }

    public void setReference(StorageReference reference) {
        this.reference = reference;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public ImageView getResultImageView() {
        return resultImageView;
    }

    public void setResultImageView(ImageView resultImageView) {
        this.resultImageView = resultImageView;
    }
}
