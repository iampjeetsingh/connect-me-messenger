package com.connectme.messenger;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class AttachDialog extends Dialog {
    private ResponseListener responseListener;
    public AttachDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_attach);
        Window window = getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ImageView galleryImg,audioImg,videoImg,documentImg,locationImg,contactImg;
        galleryImg = findViewById(R.id.galleryimg);
        audioImg = findViewById(R.id.audioimg);
        videoImg = findViewById(R.id.videoimg);
        documentImg = findViewById(R.id.documentimg);
        locationImg = findViewById(R.id.locationimg);
        contactImg = findViewById(R.id.contactimg);
        galleryImg.setOnClickListener(v->attachImage());
        audioImg.setOnClickListener(v->attachAudio());
        videoImg.setOnClickListener(v->attachVideo());
        documentImg.setOnClickListener(v->attachDocument());
        locationImg.setOnClickListener(v->attachLocation());
        contactImg.setOnClickListener(v->attachContact());
    }

    private void attachImage(){
        hide();
        if(responseListener!=null)
            responseListener.onAttachImageClick();
        responseListener=null;
        dismiss();
    }
    private void attachAudio(){
        hide();
        if(responseListener!=null)
            responseListener.onAttachAudioClick();
        responseListener=null;
        dismiss();
    }
    private void attachVideo(){
        hide();
        if(responseListener!=null)
            responseListener.onAttachVideoClick();
        responseListener=null;
        dismiss();
    }
    private void attachDocument(){
        hide();
        if(responseListener!=null)
            responseListener.onAttachDocumentClick();
        responseListener=null;
        dismiss();
    }
    private void attachLocation(){
        hide();
        if(responseListener!=null)
            responseListener.onAttachLocationClick();
        responseListener=null;
        dismiss();
    }
    private void attachContact(){
        hide();
        if(responseListener!=null)
            responseListener.onAttachContactClick();
        responseListener=null;
        dismiss();
    }
    public interface ResponseListener{
        void onAttachImageClick();
        void onAttachAudioClick();
        void onAttachVideoClick();
        void onAttachDocumentClick();
        void onAttachLocationClick();
        void onAttachContactClick();
    }
    public void setResponseListener(ResponseListener responseListener) {
        this.responseListener = responseListener;
    }
}
