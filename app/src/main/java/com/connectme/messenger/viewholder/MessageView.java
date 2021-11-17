package com.connectme.messenger.viewholder;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.connectme.messenger.Auth;
import com.connectme.messenger.DIRECTORY;
import com.connectme.messenger.Database;
import com.connectme.messenger.MESSAGE;
import com.connectme.messenger.R;
import com.connectme.messenger.filetransfer.DownloadTask;
import com.connectme.messenger.filetransfer.FileDownloader;
import com.connectme.messenger.model.Message;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public  class MessageView extends RecyclerView.ViewHolder {
    private final TextView messageTextView, timeTextView;
    private final ImageView imageView, seenSign;
    private ProgressBar progressBar;
    public View view;
    private final LinearLayout.LayoutParams params;
    private LinearLayout chatLayout, messageRow,
            imageLayout,audioLayout,videoLayout,documentLayout,locationLayout,contactLayout;
    private  String MESSAGE_DIRECTORY,
            IMAGE_DIRECTORY,
            VOICE_DIRECTORY,
            AUDIO_DIRECTORY,
            VIDEO_DIRECTORY,
            DOCUMENT_DIRECTORY;

    public MessageView(final View itemView) {
        super(itemView);
        view = itemView;
        messageTextView = view.findViewById(R.id.chat_message);
        timeTextView = view.findViewById(R.id.time);
        seenSign = view.findViewById(R.id.seen_sign);
        imageView = view.findViewById(R.id.imageView);
        progressBar = view.findViewById(R.id.progressBar);

        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        messageRow = view.findViewById(R.id.message_row);
        chatLayout = view.findViewById(R.id.chat_layout);

        imageLayout = view.findViewById(R.id.imageLayout);
        audioLayout = view.findViewById(R.id.audioLayout);
        videoLayout = view.findViewById(R.id.videoLayout);
        documentLayout = view.findViewById(R.id.documentLayout);
        locationLayout = view.findViewById(R.id.locationLayout);
        contactLayout = view.findViewById(R.id.contactLayout);
    }

    private void setSender(String senderUserID, boolean seen, String messageId) {
        IMAGE_DIRECTORY= DIRECTORY.IMAGE;
        VOICE_DIRECTORY=DIRECTORY.VOICE;
        AUDIO_DIRECTORY=DIRECTORY.AUDIO;
        VIDEO_DIRECTORY=DIRECTORY.VIDEO;
        DOCUMENT_DIRECTORY=DIRECTORY.DOCUMENT;
        if(senderUserID!=null){
            if(Auth.getUid().equals(senderUserID)){
                params.setMargins(0,0,10,10);
                view.setLayoutParams(params);
                messageRow.setGravity(Gravity.END);
                chatLayout.setBackgroundResource(R.drawable.out_chat_bubble);
                IMAGE_DIRECTORY = IMAGE_DIRECTORY+"/sent";
                VOICE_DIRECTORY = VOICE_DIRECTORY+"/sent";
                AUDIO_DIRECTORY = AUDIO_DIRECTORY+"/sent";
                VIDEO_DIRECTORY = VIDEO_DIRECTORY+"/sent";
                DOCUMENT_DIRECTORY = DOCUMENT_DIRECTORY+"/sent";
                seenSign.setVisibility(View.VISIBLE);
                if(seen){
                    seenSign.setImageResource(R.drawable.ic_message_seen);
                }else{
                    seenSign.setImageResource(R.drawable.ic_message_sent);
                }
            }else{
                params.setMargins(10,0,0,10);
                view.setLayoutParams(params);
                messageRow.setGravity(Gravity.START);
                chatLayout.setBackgroundResource(R.drawable.in_chat_bubble);
                seenSign.setVisibility(View.GONE);
                Database.getChatRef(senderUserID, Auth.getUid()).child(messageId).child("seen").setValue(true);
                Database.getChatRef(Auth.getUid(),senderUserID).child(messageId).child("seen").setValue(true);
            }
        }else {
            chatLayout.setVisibility(View.GONE);
        }
    }

    private void setMessageType(int messageType){
        imageLayout.setVisibility(View.GONE);
        audioLayout.setVisibility(View.GONE);
        videoLayout.setVisibility(View.GONE);
        documentLayout.setVisibility(View.GONE);
        locationLayout.setVisibility(View.GONE);
        contactLayout.setVisibility(View.GONE);
        if(messageType== MESSAGE.IMAGE){
            MESSAGE_DIRECTORY = IMAGE_DIRECTORY;
            imageLayout.setVisibility(View.VISIBLE);

        }else if(messageType==MESSAGE.VOICE){
            MESSAGE_DIRECTORY = VOICE_DIRECTORY;
            audioLayout.setVisibility(View.VISIBLE);

        }else if(messageType==MESSAGE.AUDIO){
            MESSAGE_DIRECTORY = AUDIO_DIRECTORY;
            audioLayout.setVisibility(View.VISIBLE);

        }else if(messageType==MESSAGE.VIDEO){
            MESSAGE_DIRECTORY = VIDEO_DIRECTORY;
            videoLayout.setVisibility(View.VISIBLE);

        }else if(messageType==MESSAGE.DOCUMENT){
            MESSAGE_DIRECTORY = DOCUMENT_DIRECTORY;
            documentLayout.setVisibility(View.VISIBLE);

        }else if(messageType==MESSAGE.LOCATION){
            locationLayout.setVisibility(View.VISIBLE);

        }else if(messageType==MESSAGE.CONTACT){
            contactLayout.setVisibility(View.VISIBLE);
        }
    }

    public void setMessage(Message message) {
        messageTextView.setText(null);
        imageView.setImageDrawable(null);
        setSender(message.getSenderUserID(),message.isSeen(),message.getId());
        setMessageType(message.getType());
        int messageType = message.getType();
        long timeStamp = message.getTimeStamp();
        String messageText = message.getText();

        String url =  message.getUrl();
        String fileName = message.getFileName();
        timeTextView.setText(getTimeFromTimeStamp(timeStamp));
        if(message.getText()!=null)
            messageTextView.setText(messageText);
        if(messageType==MESSAGE.IMAGE){
            if(isFileDownloaded(fileName)){
                Glide.with(itemView.getContext())
                        .load(getFile(fileName))
                        .apply(RequestOptions.centerCropTransform().sizeMultiplier(0.5f))
                        .into(imageView);
            }else{
                downloadFile(url,fileName,imageView);
            }
        }else if(messageType==MESSAGE.VOICE){

        }else if(messageType==MESSAGE.AUDIO){

        }else if(messageType==MESSAGE.VIDEO){

        }else if(messageType==MESSAGE.DOCUMENT){

        }else if(messageType==MESSAGE.LOCATION){

        }else if(messageType==MESSAGE.CONTACT){

        }else if(messageType==MESSAGE.LINK){

        }
    }

    private boolean isFileDownloaded(String fileName){
        File file = new File(MESSAGE_DIRECTORY+"/"+fileName);
        if(file.exists()){
            return true;
        }
        return false;
    }

    private File getFile(String fileName){
        File file = new File(MESSAGE_DIRECTORY+"/"+fileName);
        return file;
    }

    private void downloadFile(String url,String fileName,ImageView imageView){
        progressBar.setVisibility(View.VISIBLE);
        StorageReference root = FirebaseStorage.getInstance().getReference();
        StorageReference reference = root.child(url.substring(1));
        File file = new File(MESSAGE_DIRECTORY+"/"+fileName);
        DownloadTask task = new DownloadTask(reference,file);
        task.setProgressBar(progressBar);
        task.setResultImageView(imageView);
        new FileDownloader().execute(task);
    }

    private String getTimeFromTimeStamp(long timeStamp){
        DateFormat dateFormat = new SimpleDateFormat("hh:mm");
        return dateFormat.format(timeStamp);
    }
}