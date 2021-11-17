package com.connectme.messenger.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.connectme.messenger.App;
import com.connectme.messenger.Database;
import com.connectme.messenger.R;
import com.connectme.messenger.activities.Users;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.connectme.messenger.activities.ChatActivity;
import com.connectme.messenger.viewholder.UserView;
import com.connectme.messenger.model.User;

public class HomeChats extends Fragment {
    private DatabaseReference myRef;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<User, UserView> mFirebaseAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private App app;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_chats,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        app = (App) getActivity().getApplication();
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setBackgroundDrawable(getResources().getDrawable(R.drawable.floating));
        fab.setOnClickListener(view1 -> startActivity(new Intent(getContext(), Users.class)));
        myRef = Database.getUsersRef();
        myRef.keepSynced(true);
        recyclerView = view.findViewById(R.id.show_chat_recyclerView);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLinearLayoutManager);
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshData();
        app.setMessageListener(this::refreshData);
    }

    @Override
    public void onStop() {
        super.onStop();
        app.removeMessageListener();
    }

    private void refreshData(){
        mFirebaseAdapter = new FirebaseRecyclerAdapter<User, UserView>(User.class, R.layout.users_single_item, UserView.class, myRef) {
            public void populateViewHolder(final UserView userView, User model, final int position) {
                if(model.getUserID()==null){
                    userView.Layout_hide();
                }else{
                    userView.setName(model.getName());
                    userView.setPhotoFromUrl(model.getPhotoUrl());
                    userView.setStatus(model.getStatus());
                    userView.setOnlineStatus(model.isOnline());
                    if(!app.userTalkedWith(model.getUserID())) {
                        userView.Layout_hide();
                    }else{
                        userView.setUnreadCount(app.getUnreadCountFrom(model.getUserID()));
                    }
                }
                userView.itemView.setOnClickListener(v -> {
                    DatabaseReference ref = mFirebaseAdapter.getRef(position);
                    Intent intent = new Intent(getContext(), ChatActivity.class);
                    intent.putExtra("id",ref.getKey());
                    startActivity(intent);
                });
            }
        };
        recyclerView.setAdapter(mFirebaseAdapter);
    }

}


