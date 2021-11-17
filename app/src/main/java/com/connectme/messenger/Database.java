package com.connectme.messenger;

import com.google.firebase.database.DatabaseReference;

public class Database {
    public static DatabaseReference getRootRef(){
        return App.getDatabase().getReference();
    }
    public static DatabaseReference getConnectedRef(){
        return App.getDatabase().getReference(".info/connected");
    }
    public static DatabaseReference getUserRef(){
        return getRootRef().child("Users").child(Auth.getUid());
    }
    public static DatabaseReference getUsersRef(){
        return getRootRef().child("Users");
    }
    public static DatabaseReference getUserRef(String uid){
        return getRootRef().child("Users").child(uid);
    }
    public static DatabaseReference getChatsRef(){
        return getRootRef().child("Chats").child(Auth.getUid());
    }
    public static DatabaseReference getChatsRef(String uid){
        return getRootRef().child("Chats").child(uid);
    }
    public static DatabaseReference getChatRef(String friendId){
        return getRootRef().child("Chats").child(Auth.getUid()).child(friendId);
    }
    public static DatabaseReference getChatRef(String uid,String friendId){
        return getRootRef().child("Chats").child(uid).child(friendId);
    }

    public static DatabaseReference getCallsRef(){
        return getRootRef().child("Calls").child(Auth.getUid());
    }
    public static DatabaseReference getCallsRef(String uid){
        return getRootRef().child("Calls").child(uid);
    }


    public static Connection getConnection(){
        return new Connection();
    }
}
