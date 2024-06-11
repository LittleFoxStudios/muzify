package com.littlefoxstudios.muzify.homescreenfragments.history.outercard;

import static android.view.View.GONE;
import static com.littlefoxstudios.muzify.Utilities.getRandomDrawableIDForThumbnail;

import android.content.Context;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.littlefoxstudios.muzify.R;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.homescreenfragments.HistoryFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CardRecyclerViewAdapter extends RecyclerView.Adapter<CardRecyclerViewAdapter.MyViewHolder> implements Filterable {

    private final CardInterface cardInterface;
    Context context;
    ArrayList<OuterCard> outerCards = new ArrayList<>();
    boolean deleteMode;

    public CardRecyclerViewAdapter(Context context, ArrayList<OuterCard> outerCards, CardInterface cardInterface, boolean deleteMode){
        this.context = context;
        this.outerCards = outerCards;
        this.cardInterface = cardInterface;
        this.deleteMode = deleteMode;
    }

    public CardRecyclerViewAdapter(Context context, ArrayList<OuterCard> outerCards, CardInterface cardInterface){
        this.context = context;
        this.outerCards = outerCards;
        this.cardInterface = cardInterface;
        this.deleteMode = false;
    }



    @NonNull
    @Override
    public CardRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.history_outer_card_recycler_view_v2, parent, false);
        return new CardRecyclerViewAdapter.MyViewHolder(view, cardInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull CardRecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.playListTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        holder.playListTitle.setSelected(true);
        holder.playListTitle.setText(outerCards.get(position).getPlaylistTitle());
        holder.createdDate.setText(outerCards.get(position).getCreatedDate());

        int selectedColor = getContext().getColor(R.color.nav_btn_selected);
        int disabledColor = getContext().getColor(R.color.grey);
        if(outerCards.get(position).isUploadFlagEnabled()){
            holder.uploadFlag.setColorFilter(selectedColor, PorterDuff.Mode.SRC_ATOP);
        }else{
            holder.uploadFlag.setColorFilter(disabledColor, PorterDuff.Mode.SRC_ATOP);
        }
        if(outerCards.get(position).isDownloadFlagEnabled()){
            holder.downloadFlag.setColorFilter(selectedColor, PorterDuff.Mode.SRC_ATOP);
        }else{
            holder.downloadFlag.setColorFilter(disabledColor, PorterDuff.Mode.SRC_ATOP);
        }
        if(outerCards.get(position).isShareFlagEnabled()){
            holder.shareFlag.setColorFilter(selectedColor, PorterDuff.Mode.SRC_ATOP);
        }else{
            holder.shareFlag.setColorFilter(disabledColor, PorterDuff.Mode.SRC_ATOP);
        }

        if(deleteMode) {
            holder.deleteCardCheckbox.setVisibility(View.VISIBLE);
            holder.subDetailsBox.setVisibility(GONE);
            holder.selectionBox.setVisibility(View.VISIBLE);
            if(outerCards.get(position).isCardSelected()){
                if(!holder.deleteCardCheckbox.isChecked()){
                    holder.deleteCardCheckbox.toggle();
                }
            }else{
                if(holder.deleteCardCheckbox.isChecked()){
                    holder.deleteCardCheckbox.toggle();
                }
            }
        }else {
            holder.deleteCardCheckbox.setVisibility(GONE);
            holder.subDetailsBox.setVisibility(View.VISIBLE);
            holder.selectionBox.setVisibility(GONE);
        }

        holder.subDetailsDestinationImage.setImageResource(outerCards.get(position).getTransferMediumImage());
        holder.selectionDestinationImage.setImageResource(outerCards.get(position).getTransferMediumImage());

        holder.resultImage.setImageResource(outerCards.get(position).getResponseImage());
        if(!outerCards.get(position).isPlaylistTransferSuccessful()){
            holder.failedItemCount.setText(String.valueOf(outerCards.get(position).getFailedItemsCount()));
        }else{
            holder.failedItemCount.setText("");
        }

        //4 thumbnail images
        ArrayList<String> thumbnailUrls = outerCards.get(position).getPlaylistImageUrls();
        if(outerCards.get(position).getFirstImageID() != null){
            holder.imageOne.setImageResource(outerCards.get(position).getFirstImageID());
        }else{
            Picasso.get().load(thumbnailUrls.get(0)).placeholder(getRandomDrawableIDForThumbnail()).into(holder.imageOne);
        }

        if(outerCards.get(position).getSecondImageID() != null){
            holder.imageTwo.setImageResource(outerCards.get(position).getSecondImageID());
        }else{
            Picasso.get().load(thumbnailUrls.get(1)).placeholder(getRandomDrawableIDForThumbnail()).into(holder.imageTwo);
        }

        if(outerCards.get(position).getThirdImageID() != null){
            holder.imageThree.setImageResource(outerCards.get(position).getThirdImageID());
        }else{
            Picasso.get().load(thumbnailUrls.get(2)).placeholder(getRandomDrawableIDForThumbnail()).into(holder.imageThree);
        }

        if(outerCards.get(position).getFourthImageID() != null){
            holder.imageFour.setImageResource(outerCards.get(position).getFourthImageID());
        }else{
            Picasso.get().load(thumbnailUrls.get(3)).placeholder(getRandomDrawableIDForThumbnail()).into(holder.imageFour);
        }

    }

    @Override
    public int getItemCount() {
        if(outerCards == null){
            outerCards = new ArrayList<>();
        }
        return outerCards.size();
    }

    @Override
    public Filter getFilter() {
        return filterCards;
    }

    private Filter filterCards = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<OuterCard> outerCardsList = new ArrayList<>();
            if (charSequence == null || charSequence.length() == 0) {
                outerCardsList.addAll(outerCards);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (OuterCard outerCard : outerCards) {
                    if (outerCard.getPlaylistTitle().toLowerCase().contains(filterPattern)) {
                        outerCardsList.add(outerCard);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = outerCardsList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            outerCards.clear();
            outerCards.addAll((ArrayList)filterResults.values);
            notifyDataSetChanged();
        }
    };

    public Context getContext()
    {
        return this.context;
    }

    public CardInterface getCardInterface()
    {
        return this.cardInterface;
    }

    public void setOuterCards(ArrayList<OuterCard> outerCards)
    {
        if(this.outerCards == null){
            this.outerCards = new ArrayList<>();
        }
        this.outerCards.clear();
        this.outerCards.addAll(outerCards);
        notifyDataSetChanged();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView imageOne;
        ImageView imageTwo;
        ImageView imageThree;
        ImageView imageFour;

        //ImageView transferMedium;
        ImageView resultImage;
        ImageView subDetailsDestinationImage;
        ImageView selectionDestinationImage;

        TextView playListTitle;
        TextView createdDate;
        TextView failedItemCount;

        ImageView uploadFlag;
        ImageView downloadFlag;
        ImageView shareFlag;

        CheckBox deleteCardCheckbox;

        LinearLayout selectionBox;
        LinearLayout subDetailsBox;


        public MyViewHolder(@NonNull View itemView, CardInterface cardInterface) {
            super(itemView);
            imageOne = itemView.findViewById(R.id.historyOuterCardImageOne);
            imageTwo = itemView.findViewById(R.id.historyOuterCardImageTwo);
            imageThree = itemView.findViewById(R.id.historyOuterCardImageThree);
            imageFour = itemView.findViewById(R.id.historyOuterCardImageFour);

            //transferMedium = itemView.findViewById(R.id.historyOuterCardTransferImageHolder);
            resultImage = itemView.findViewById(R.id.historyOuterCardImageResponseHolder);
            subDetailsDestinationImage = itemView.findViewById(R.id.historyOuterCardSubDetailsDestinationImage);
            selectionDestinationImage = itemView.findViewById(R.id.historyOuterCardSelectionBoxDestinationImage);

            playListTitle = itemView.findViewById(R.id.historyOuterCardPlaylistTitle);
            createdDate = itemView.findViewById(R.id.historyOuterCardDateHolder);
            failedItemCount = itemView.findViewById(R.id.historyOuterCardNumberResponseHolder);

            uploadFlag = itemView.findViewById(R.id.historyOuterCardUploadFlagImage);
            downloadFlag = itemView.findViewById(R.id.historyOuterCardDownloadFlagImage);
            shareFlag = itemView.findViewById(R.id.historyOuterCardShareFlagImage);

            selectionBox = itemView.findViewById(R.id.historyOuterCardSelectionBox);
            subDetailsBox = itemView.findViewById(R.id.historyOuterCardSubDetailsBox);


            deleteCardCheckbox = itemView.findViewById(R.id.OuterCardDeleteSelector);

            deleteCardCheckbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(cardInterface != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            cardInterface.itemSelected(position);
                        }
                    }
                }
            });



            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(cardInterface != null){
                        int position = getAdapterPosition();

                        //bypassing card open for card select in delete mode
                        if(cardInterface.isDeleteModeSelected()){
                            deleteCardCheckbox.toggle();
                            if(position != RecyclerView.NO_POSITION){
                                cardInterface.itemSelected(position);
                            }
                            return;
                        }

                        if(position != RecyclerView.NO_POSITION){
                            cardInterface.onItemClick(position);
                        }
                    }
                }
            });


            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(cardInterface == null){
                        return true;
                    }
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        deleteCardCheckbox.toggle();
                        cardInterface.handleLongClick(position);
                    }
                    return true;
                }
            });

        }
    }
}
