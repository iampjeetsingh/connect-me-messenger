package com.connectme.messenger;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Auth {
    public static FirebaseUser getUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }
    public static String getDisplayName(){
        return getUser().getDisplayName();
    }
    public static String getPhotoUrl(){
        Uri uri = getUser().getPhotoUrl();
        if(uri==null)
            return null;
        else
            return getUser().getPhotoUrl().toString();
    }
    public static String getPhone(){
        return getUser().getPhoneNumber();
    }
    public static String getUid(){
        return getUser().getUid();
    }
    public static boolean isNull(){
        return getUser()==null;
    }
}
