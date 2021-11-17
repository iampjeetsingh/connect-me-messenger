package com.connectme.messenger;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class Connection {
    private StateListener stateListener;
    private DatabaseReference connectedRef;

    public Connection() {
        connectedRef = Database.getConnectedRef();
    }

    public void addStateListener(StateListener stateListener){
        this.stateListener=stateListener;
        connectedRef.addValueEventListener(valueEventListener);
    }
    public interface StateListener{
        void onConnected();
        void onDisconnected();
    }
    public void removeStateListener(){
        this.stateListener=null;
        connectedRef.removeEventListener(valueEventListener);
    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            boolean connected = dataSnapshot.getValue(Boolean.class);
            if(connected){
                if(stateListener!=null)
                    stateListener.onConnected();
                else
                    connectedRef.removeEventListener(valueEventListener);
            }else{
                if(stateListener!=null)
                    stateListener.onDisconnected();
                else
                    connectedRef.removeEventListener(valueEventListener);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e("Connection","Listener was cancelled "+databaseError);
        }
    };
}
