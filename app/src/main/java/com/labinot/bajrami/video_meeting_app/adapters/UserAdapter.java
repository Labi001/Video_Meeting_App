package com.labinot.bajrami.video_meeting_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.labinot.bajrami.video_meeting_app.R;
import com.labinot.bajrami.video_meeting_app.listeners.UsersListeners;
import com.labinot.bajrami.video_meeting_app.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<User> users;
    private UsersListeners usersListeners;
    private List<User> selectedUsers;

    public UserAdapter(List<User> users,UsersListeners usersListeners) {
        this.users = users;
        this.usersListeners = usersListeners;
        selectedUsers = new ArrayList<>();
    }

    public List<User> SelectedUsers() {
        return selectedUsers;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_user,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {

        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtFirstChar,txtUserName,txtEmail;
        ImageView imgSelected;
        ImageView imageAudioMeeting,imageVideoMeeting;
        ConstraintLayout userContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtFirstChar = itemView.findViewById(R.id.textFirstChat);
            txtUserName = itemView.findViewById(R.id.txt_userName);
            txtEmail = itemView.findViewById(R.id.txt_email);
            imageAudioMeeting = itemView.findViewById(R.id.img_AudioMeeting);
            imageVideoMeeting = itemView.findViewById(R.id.img_videoMeeting);
            userContainer = itemView.findViewById(R.id.userContainer);
            imgSelected = itemView.findViewById(R.id.img_selected);
        }

        void setUserData(User user){

            txtFirstChar.setText(user.first_name.substring(0,1));
            txtUserName.setText(String.format("%s %s",user.first_name,user.last_name));
            txtEmail.setText(user.email);
            imageAudioMeeting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    usersListeners.initiateAudioMeeting(user);
                }
            });

            imageVideoMeeting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    usersListeners.initiateVideoMeeting(user);
                }
            });

            userContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if(imgSelected.getVisibility() != View.VISIBLE){
                        selectedUsers.add(user);
                        imgSelected.setVisibility(View.VISIBLE);
                        imageVideoMeeting.setVisibility(View.GONE);
                        imageAudioMeeting.setVisibility(View.GONE);
                        usersListeners.onMultipleUsersAction(true);

                    }
                    return true;
                }
            });

            userContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(imgSelected.getVisibility() == View.VISIBLE){

                        selectedUsers.remove(user);
                        imgSelected.setVisibility(View.GONE);
                        imageAudioMeeting.setVisibility(View.VISIBLE);
                        imageVideoMeeting.setVisibility(View.VISIBLE);

                        if(selectedUsers.size() == 0)
                            usersListeners.onMultipleUsersAction(false);

                    }else {

                        if(selectedUsers.size() > 0){

                            selectedUsers.add(user);
                            imgSelected.setVisibility(View.VISIBLE);
                            imageVideoMeeting.setVisibility(View.GONE);
                            imageAudioMeeting.setVisibility(View.GONE);
                        }

                    }

                }
            });

        }

    }
}
