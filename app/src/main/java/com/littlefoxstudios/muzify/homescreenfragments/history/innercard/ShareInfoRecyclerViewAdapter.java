package com.littlefoxstudios.muzify.homescreenfragments.history.innercard;

import static com.littlefoxstudios.muzify.Utilities.getRandomDrawableIDForThumbnail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.littlefoxstudios.muzify.R;
import com.littlefoxstudios.muzify.Utilities;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShareInfoRecyclerViewAdapter extends RecyclerView.Adapter<ShareInfoRecyclerViewAdapter.MyViewHolder>{
    Context context;
    InnerCardObj.ShareModel shareModel;
    String currentUserEmail;

    public ShareInfoRecyclerViewAdapter(Context context, InnerCardObj.ShareModel shareModel, String currentUserEmail){
        this.context = context;
        if(shareModel == null){
            this.shareModel = new InnerCardObj.ShareModel();
        }else{
            this.shareModel = shareModel;
        }
        this.currentUserEmail = currentUserEmail;
    }

    @NonNull
    @Override
    public ShareInfoRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View views = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_inner_card_share_info_user_details,parent,false);
        return new ShareInfoRecyclerViewAdapter.MyViewHolder(views);
    }

    @Override
    public void onBindViewHolder(@NonNull ShareInfoRecyclerViewAdapter.MyViewHolder holder, int position) {
        if(Utilities.isEmpty(shareModel.sharedDetailsList)){
            return;
        }
        ArrayList<InnerCardObj.SharedDetails> sharedDetails = shareModel.sharedDetailsList;
        InnerCardObj.SharedDetails sd = sharedDetails.get(position);
        holder.shareInfoDestinationImage.setImageResource(sd.getDestinationService().getLogoDrawableID());
        holder.shareInfoUserName.setText(sd.getUserName());
        if(sd.getOwnerEmailID().equals(currentUserEmail)){
            holder.shareInfoEmail.setText(sd.getEmailID());
            holder.shareInfoDate.setText(sd.getSharedDate());
        }else{
            holder.shareInfoEmail.setText(sd.getSharedDate());
        }
        holder.shareInfoSNo.setText((position+1)+"");
        Picasso.get().load(sd.getProfilePicURL()).placeholder(R.drawable.person_image).into(holder.shareInfoProfilePic);
    }

    @Override
    public int getItemCount() {
        return (shareModel == null || shareModel.sharedDetailsList == null) ? 0 : shareModel.sharedDetailsList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView shareInfoDestinationImage;
        TextView shareInfoUserName;
        TextView shareInfoEmail;
        TextView shareInfoDate;
        TextView shareInfoSNo;
        ImageView shareInfoProfilePic;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            shareInfoDestinationImage = itemView.findViewById(R.id.shareInfoDestinationImage);
            shareInfoUserName = itemView.findViewById(R.id.shareInfoUserName);
            shareInfoEmail = itemView.findViewById(R.id.shareInfoEmail);
            shareInfoDate = itemView.findViewById(R.id.shareInfoDate);
            shareInfoSNo = itemView.findViewById(R.id.shareInfoSNo);
            shareInfoProfilePic = itemView.findViewById(R.id.shareInfoProfilePic);
        }
    }
}
