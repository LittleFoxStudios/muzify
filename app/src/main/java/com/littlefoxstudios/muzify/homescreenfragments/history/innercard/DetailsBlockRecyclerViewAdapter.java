package com.littlefoxstudios.muzify.homescreenfragments.history.innercard;

import static com.littlefoxstudios.muzify.Utilities.getRandomDrawableIDForThumbnail;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.littlefoxstudios.muzify.R;
import com.littlefoxstudios.muzify.Utilities;
import com.squareup.picasso.Picasso;

import java.util.logging.Logger;

public class DetailsBlockRecyclerViewAdapter extends RecyclerView.Adapter<DetailsBlockRecyclerViewAdapter.MyViewHolder> {

    private Context context;
    private InnerCardObj innerCardObj;
    InnerCardActivity innerCardActivity;

    public static final int BLOCK_5_POSITION = 4;

    DetailsBlockRecyclerViewAdapter(InnerCardObj innerCardObj, InnerCardActivity innerCardActivity){
        this.context = innerCardActivity;
        this.innerCardObj = innerCardObj;
        this.innerCardActivity = innerCardActivity;
    }

    @NonNull
    @Override
    public DetailsBlockRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View views = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_inner_card_details_block_recycler_view,parent,false);
        return new DetailsBlockRecyclerViewAdapter.MyViewHolder(views);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailsBlockRecyclerViewAdapter.MyViewHolder holder, int position) {
        Utilities.Loggers.postInfoLog("TEST", "Position : "+position);
        switch (position)
        {
            case 0 : showBlock1(holder); break;
            case 1 : showBlock2(holder, "SOURCE ACCOUNT"); break;
            case 2 : showBlock2(holder, "DESTINATION ACCOUNT"); break;
            case 3 : showBlock4(holder); break;
            case 4 : showBlock5(holder); break;
            default: break;
        }
    }


    private void hideAllBlocks(MyViewHolder holder){
        holder.block1.setVisibility(View.GONE);
        holder.block2.setVisibility(View.GONE);
        holder.block4.setVisibility(View.GONE);
        holder.block5.setVisibility(View.GONE);
    }

    private void showBlock4(MyViewHolder holder) {
        hideAllBlocks(holder);
        holder.block4.setVisibility(View.VISIBLE);

        holder.totalCount.setText(innerCardObj.getTotalItemsInPlaylist()+"");
        holder.failedCount.setText(innerCardObj.getTotalItemsFailed()+"");
        int percent = Integer.parseInt(String.valueOf(Math.round(
                (innerCardObj.getTotalItemsInPlaylist() - innerCardObj.getTotalItemsFailed()) * 100.0/innerCardObj.getTotalItemsInPlaylist())));
        if(percent > 75){
            holder.successPercentage.setTextColor(context.getColor(R.color.green));
        }else if(percent > 50){
            holder.successPercentage.setTextColor(context.getColor(R.color.yellow));
        }else{
            holder.successPercentage.setTextColor(context.getColor(R.color.red));
        }
        holder.successPercentage.setText(percent+"%");
    }

    private void showBlock2(MyViewHolder holder, String type) {
        hideAllBlocks(holder);
        holder.block2.setVisibility(View.VISIBLE);

        String name,email,profilePicUrl;
        int logoID;

        if("SOURCE ACCOUNT".equals(type)){
            name = innerCardObj.getSourceAccountName();
            email = innerCardObj.getSourceAccountEmail();
            profilePicUrl = innerCardObj.getSourceAccountProfilePicture();
            logoID = innerCardObj.getOriginalSourceCode().getLogoDrawableID();
        }else{
            name = innerCardObj.getDestinationAccountName();
            email = innerCardObj.getDestinationAccountEmail();
            profilePicUrl = innerCardObj.getDestinationAccountProfilePicture();
            logoID = innerCardObj.destination.getLogoDrawableID();
        }
        holder.transferType.setText(type);
        holder.sourceAccountEmail.setText(email);
        holder.sourceAccountName.setText(name);
        holder.sourceServiceImage.setImageResource(logoID);
        profilePicUrl = (profilePicUrl == null || profilePicUrl.equals("")) ? "null" : profilePicUrl;
        Picasso.get().load(profilePicUrl).placeholder(R.drawable.person_image).into(holder.profilePicture);
    }

    private void showBlock1(DetailsBlockRecyclerViewAdapter.MyViewHolder holder){
        hideAllBlocks(holder);
        holder.block1.setVisibility(View.VISIBLE);
        holder.playlistTitle.setText(innerCardObj.getPlayListTitle());
        holder.playlistTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        holder.playlistTitle.setSelected(true);
    }

    public void showBlock5(MyViewHolder holder){
        hideAllBlocks(holder);
        holder.block5.setVisibility(View.VISIBLE);
        holder.block5_firstPhase_Plus_img.setColorFilter(context.getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        if(innerCardActivity.cardData.getShareCode() != null && !innerCardActivity.cardData.getShareCode().equals("")){
            block5ThirdPhase(holder, false);
            return;
        }
        block5firstPhase(holder);
    }

    private void block5firstPhase(MyViewHolder holder) {
        hideAllBlocks(holder);
        holder.block5.setVisibility(View.VISIBLE);
        holder.block5_firstPhase.setVisibility(View.VISIBLE);
        holder.block5_firstPhase_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                block5ThirdPhase(holder, true);
            }
        });
    }

    private void block5ThirdPhase(MyViewHolder holder, boolean newShare) {
        hideAllBlocks(holder);
        holder.block5.setVisibility(View.VISIBLE);
        holder.block5_firstPhase.setVisibility(View.GONE);
        holder.block5_secondPhase.setVisibility(View.VISIBLE);
        if(newShare){
            innerCardActivity.handleNewCodeCreationRequest(holder);
            return;
        }
        block5ThirdPhaseDataUpdate(innerCardActivity.cardData.shareCode, holder);
    }

    //method copy is in InnerCardActivity
    public void block5ThirdPhaseDataUpdate(String shareCode, MyViewHolder holder)
    {
        innerCardActivity.cardData.setShareCode(shareCode);
        innerCardActivity.handleAddShareCallbackV2(shareCode, holder);
    }



    @Override
    public int getItemCount() {
        return InnerCardObj.TOTAL_DISPLAY_BLOCKS_SUPPORTED;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ConstraintLayout block1;
        ConstraintLayout block2;
        ConstraintLayout block4;
        ConstraintLayout block5;

        TextView playlistTitle;

        ShapeableImageView profilePicture;
        TextView sourceAccountName;
        TextView sourceAccountEmail;
        ImageView sourceServiceImage;
        TextView transferType;

        TextView failedCount;
        TextView totalCount;
        TextView successPercentage;


        ConstraintLayout block5_firstPhase;
        ConstraintLayout block5_secondPhase;
        ConstraintLayout block5_thirdPhase;
        ConstraintLayout block5_firstPhase_btn;
        ImageView block5_firstPhase_Plus_img;
        TextView block5_thirdPhase_shareCode;
        ImageView block5_thirdPhase_copyCode_btn;
        TextView shareCount;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            block1 = itemView.findViewById(R.id.icdb1);
            block2 = itemView.findViewById(R.id.icdb2);
            block4 = itemView.findViewById(R.id.icdb3);
            block5 = itemView.findViewById(R.id.icdb4);

            //block 1
            playlistTitle = itemView.findViewById(R.id.icdb1_playlistTitle);

            //block 2&3
            profilePicture = itemView.findViewById(R.id.icdb2_profilePicture);
            sourceAccountName = itemView.findViewById(R.id.icdb2_accountName);
            sourceAccountEmail = itemView.findViewById(R.id.icdb2_accountEmail);
            sourceServiceImage = itemView.findViewById(R.id.icdb2_musicServiceImage);
            transferType = itemView.findViewById(R.id.icdb2_transferType);

            //block 4
            totalCount = itemView.findViewById(R.id.icdb3_totalItems);
            failedCount = itemView.findViewById(R.id.icdb3_failedItems);
            successPercentage = itemView.findViewById(R.id.icdb3_percentage);

            //block 5
            block5_firstPhase = itemView.findViewById(R.id.icdb4_newBlock);
            block5_firstPhase_Plus_img = itemView.findViewById(R.id.icdb4_newBlock_plusSign);
            block5_secondPhase = itemView.findViewById(R.id.icdb4_generatingBlock);
            block5_thirdPhase = itemView.findViewById(R.id.icdb4_share);
            block5_firstPhase_btn = itemView.findViewById(R.id.icdb4_newBlock_generateButton);
            block5_thirdPhase_shareCode = itemView.findViewById(R.id.icdb4_share_codeHolder);
            block5_thirdPhase_copyCode_btn = itemView.findViewById(R.id.icdb4_share_copyButton);
            shareCount = itemView.findViewById(R.id.icdb4_share_countHolder);
        }
    }

}
