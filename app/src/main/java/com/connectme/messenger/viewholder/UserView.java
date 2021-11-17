package com.connectme.messenger.viewholder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.connectme.messenger.R;

public class UserView extends RecyclerView.ViewHolder {
    private TextView nametxt, statustxt,unreadcounttxt;
    private ImageView profileimg, onlineimg;
    private LinearLayout layout;
    LinearLayout.LayoutParams params;

    public UserView(final View itemView) {
        super(itemView);
        nametxt = itemView.findViewById(R.id.frnd_name);
        unreadcounttxt = itemView.findViewById(R.id.unreadcounttxt);
        statustxt = itemView.findViewById(R.id.frnd_status);
        profileimg = itemView.findViewById(R.id.frnd_image);
        onlineimg = itemView.findViewById(R.id.online_dot);
        layout = itemView.findViewById(R.id.show_chat_single_item_layout);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void setName(String title) {
        nametxt.setText(title);
    }
    public void Layout_hide() {
        params.height = 0;
        layout.setLayoutParams(params);

    }

    public void setStatus(String status){
        statustxt.setText(status);
    }

    public void setPhotoFromUrl(String photoUrl) {
        if (!(photoUrl==null)) {
            Glide.with(itemView.getContext())
                    .load(photoUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profileimg);
        }else{
            profileimg.setImageResource(R.drawable.ic_account_circle);
        }
    }

    public void setOnlineStatus(boolean online) {
        if(online){
            onlineimg.setVisibility(View.VISIBLE);
        }else{
            onlineimg.setVisibility(View.INVISIBLE);
        }
    }

    public void setUnreadCount(int unreadCount){
        if(unreadCount==0)
            unreadcounttxt.setVisibility(View.GONE);
        else{
            unreadcounttxt.setVisibility(View.VISIBLE);
            unreadcounttxt.setText(""+unreadCount);
        }
    }
}