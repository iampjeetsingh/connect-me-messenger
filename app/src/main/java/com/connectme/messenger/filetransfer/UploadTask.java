package com.connectme.messenger.filetransfer;

import com.google.firebase.storage.StorageReference;

import java.io.File;

public class UploadTask {
    private StorageReference reference;
    private File file;

    public UploadTask() {
    }

    public UploadTask(StorageReference reference, File file) {
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
}
