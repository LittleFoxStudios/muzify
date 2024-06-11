package com.littlefoxstudios.muzify.homescreenfragments.history.innercard;

import static com.littlefoxstudios.muzify.Utilities.getRandomDrawableIDForThumbnail;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.littlefoxstudios.muzify.MuzifySharedMemory;
import com.littlefoxstudios.muzify.R;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.datastorage.CloudStorage;
import com.littlefoxstudios.muzify.datastorage.LocalStorage;
import com.littlefoxstudios.muzify.datastorage.MuzifyRoomDatabase;
import com.littlefoxstudios.muzify.datastorage.MuzifyViewModel;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.MusicServices;
import com.littlefoxstudios.muzify.internet.InternetTesterInterface;
import com.littlefoxstudios.muzify.internet.InternetTestingViewModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class InnerCardActivity extends AppCompatActivity implements InternetTesterInterface, CardInterface {

    private static InnerCardObj innerCard;
    private String userEmailID;

    ImageView sourceImage, destinationImage;
    ImageView imageOne, imageTwo, imageThree, imageFour;

    RecyclerView innerCardPlaylistItemsRecyclerView;
    RecyclerView detailsBlockRecyclerView;

    LiveData<List<LocalStorage.Album>> albumLD;
    List<LocalStorage.Album> albumTemp;

    ShareInfoDialog dialog;

    Animation fade;
    Animation fadeOut;

    MuzifyViewModel muzifyViewModel;
    MuzifySharedMemory sharedMemory;
    LiveData<List<LocalStorage.Card>> cardsLD;
    LocalStorage.Card cardData;

    InternetTestingViewModel internetTestingViewModel;
    Utilities.Alert alert;

    ConstraintLayout cardLoadingLayout;
    AppBarLayout cardDataLayout;

    private boolean refreshingSharedUserDetails = false;

    View detailsBlockShine;
    ShimmerFrameLayout playlistItemsShimmerLayout;
    ShimmerFrameLayout detailsBlockShimmerLayout;
    RelativeLayout detailsBlockFirstHalf;

    long cardNumber;

    int previousBlockNumber = -1;
    boolean shareInfoDownloaded = false;
    boolean showingPlaylistItems = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_inner_card_layout);
        //getSupportActionBar().hide();
        cardNumber = getIntent().getLongExtra("card_number", -1);
        userEmailID = getIntent().getStringExtra("emailID");

        innerCardPlaylistItemsRecyclerView = findViewById(R.id.HistoryInnerCardRecyclerViewLY);
        detailsBlockRecyclerView = findViewById(R.id.historyInnerCardDetailsBlockRecyclerView);

        playlistItemsShimmerLayout = findViewById(R.id.historyInnerCardPlaylistItemsShimmerLayout);
        detailsBlockShimmerLayout = findViewById(R.id.innerCardDetailsBlockTopHalfShimmer);
        detailsBlockFirstHalf = findViewById(R.id.innerCardDetailsBlockTopHalfOriginal);

        sharedMemory = new MuzifySharedMemory(this);

        showShimmer();
        initializeViews();
        //showLoading();
        initialize();
    }

    private void stopShimmer() {
        stopDetailsBlockShimmer();
        stopPlaylistItemsShimmer();
    }

    private void stopDetailsBlockShimmer() {
        detailsBlockShimmerLayout.setVisibility(View.GONE);
        detailsBlockShimmerLayout.stopShimmer();
        detailsBlockFirstHalf.setVisibility(View.VISIBLE);
    }

    private void showShimmer() {
        showDetailsBlockShimmer();
        showPlaylistItemsShimmer();
    }

    private void showDetailsBlockShimmer() {
        detailsBlockShimmerLayout.setVisibility(View.VISIBLE);
        detailsBlockShimmerLayout.startShimmer();
    }

    public InternetTestingViewModel initializeInternetTestingViewModel(){
        return new InternetTestingViewModel(getApplication());
    }

    private void showPlaylistItemsShimmer()
    {
        playlistItemsShimmerLayout.setVisibility(View.VISIBLE);
        playlistItemsShimmerLayout.startShimmer();
    }

    private void stopPlaylistItemsShimmer(){
        playlistItemsShimmerLayout.setVisibility(View.GONE);
        playlistItemsShimmerLayout.stopShimmer();
    }


    private void showLoading()

    {
        cardLoadingLayout.setVisibility(View.VISIBLE);
        cardDataLayout.setVisibility(View.GONE);
    }

    public void stopLoading()
    {
        cardLoadingLayout.setVisibility(View.GONE);
        cardDataLayout.setVisibility(View.VISIBLE);
    }

    private void initializeViews()
    {
        fade = AnimationUtils.loadAnimation(this, R.anim.fade);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        detailsBlockShine = findViewById(R.id.innerCardDisplayBlockShineEffect);

        imageOne = findViewById(R.id.innerCardThumbnailImage1);
        imageTwo = findViewById(R.id.innerCardThumbnailImage2);
        imageThree = findViewById(R.id.innerCardThumbnailImage3);
        imageFour = findViewById(R.id.innerCardThumbnailImage4);

        sourceImage = findViewById(R.id.innerCardSourceImage);
        destinationImage = findViewById(R.id.innerCardDestinationImage);

        cardLoadingLayout = (ConstraintLayout) findViewById(R.id.innerCardLoading);
        cardDataLayout = (AppBarLayout) findViewById(R.id.innerCardDataLayout);

        muzifyViewModel = new MuzifyViewModel(getApplication());

        internetTestingViewModel = initializeInternetTestingViewModel();
        alert = new Utilities.Alert(this, Utilities.Alert.NO_INTERNET);
        observeInternetConnection(internetTestingViewModel, this, alert);

    }

    private void initialize()
    {
       try {
           /*
           Parcelable parcelable = getIntent().getParcelableExtra("innerCardObj");
           innerCard = Parcels.unwrap(parcelable);
           //set source and destination image
           Utilities.MusicService sourceService = innerCard.getSource();
           Utilities.MusicService destinationService = innerCard.getDestination();
           sourceImage.setImageResource(sourceService.getLogoDrawableID());
           destinationImage.setImageResource(destinationService.getLogoDrawableID());
            */

           /*
           //share button
           //TODO implement share button
           share.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   if(innerCard.getTotalItemsInPlaylist() == innerCard.getTotalItemsFailed()){
                       Utilities.Loggers.showLongToast(getApplicationContext(), "Sorry, You cannot share a failed playlist");
                       return;
                   }
                   openShareInfoDialog(innerCard);
               }
           });
            */


           cardsLD = muzifyViewModel.getCardDataForSpecificUser(userEmailID);
           cardsLD.observe(this, new Observer<List<LocalStorage.Card>>() {
               @Override
               public void onChanged(List<LocalStorage.Card> cards) {
                   cardsLD.removeObserver(this);
                   for(LocalStorage.Card card : cards){
                       if(card.cardNumber == cardNumber){
                           cardData = card;
                           innerCard = InnerCardObj.getInnerCard(card);
                           /*
                           dialog.cardData = card;
                           dialog.muzifyViewModel = muzifyViewModel;
                           List<LocalStorage.ShareInfo> shareInfos = new ArrayList<>();
                           if(innerCard.getShareModel() != null){
                               shareInfos = innerCard.getShareModel().getShareInfos(card.getShareCode());
                           }
                           dialog.showCodeIfPresent(shareInfos);
                            */
                           break;
                       }
                   }

                   sourceImage.setImageResource(innerCard.getSource().getLogoDrawableID());
                   destinationImage.setImageResource(innerCard.getDestination().getLogoDrawableID());

                   DetailsBlockRecyclerViewAdapter detailsBlockRecyclerViewAdapter = new DetailsBlockRecyclerViewAdapter(innerCard, InnerCardActivity.this);
                   detailsBlockRecyclerView.setAdapter(detailsBlockRecyclerViewAdapter);
                   LinearLayoutManager detailsBlockLinearLayoutManager = new LinearLayoutManager(InnerCardActivity.this,LinearLayoutManager.HORIZONTAL, false);
                   detailsBlockRecyclerView.setLayoutManager(detailsBlockLinearLayoutManager);
                   PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
                   pagerSnapHelper.attachToRecyclerView(detailsBlockRecyclerView);


                   detailsBlockRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                       @Override
                       public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                           super.onScrollStateChanged(recyclerView, newState);
                           if(newState == RecyclerView.SCROLL_STATE_IDLE && detailsBlockLinearLayoutManager.findFirstVisibleItemPosition()
                                   == DetailsBlockRecyclerViewAdapter.BLOCK_5_POSITION) {

                               InnerCardActivity.this.innerCardPlaylistItemsRecyclerView.setAdapter(new CardRecyclerViewAdapter());
                               InnerCardActivity.this.innerCardPlaylistItemsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                               showPlaylistItemsShimmer();
                               LiveData<List<LocalStorage.ShareInfo>> shareInfo = muzifyViewModel.getShareInfoForSpecificCard(cardData.getShareCode());
                               shareInfo.observe(InnerCardActivity.this, new Observer<List<LocalStorage.ShareInfo>>() {
                                   @Override
                                   public void onChanged(List<LocalStorage.ShareInfo> shareInfos) {
                                       shareInfo.removeObserver(this);
                                       innerCard.setShareCount(shareInfos.size());

                                       if(shareInfoDownloaded){
                                           handleRefreshShareCallbackV2(shareInfos);
                                       }else{
                                           CloudStorage.refreshSharedUserDetailsV2(cardData.getShareCode(), muzifyViewModel, shareInfos, InnerCardActivity.this);
                                       }
                                   }
                               });

                           }else{
                               stopShimmer();
                               if(previousBlockNumber != DetailsBlockRecyclerViewAdapter.BLOCK_5_POSITION && showingPlaylistItems){
                                   return;
                               }
                               showingPlaylistItems = true;
                               InnerCardActivity.this.innerCardPlaylistItemsRecyclerView.setAdapter(new CardRecyclerViewAdapter(getApplicationContext(), innerCard,  albumTemp, InnerCardActivity.this));
                               InnerCardActivity.this.innerCardPlaylistItemsRecyclerView.getAdapter().notifyDataSetChanged();
                           }
                           previousBlockNumber = detailsBlockLinearLayoutManager.findFirstVisibleItemPosition();
                       }
                   });




                   Animation anim = AnimationUtils.loadAnimation(InnerCardActivity.this, R.anim.left_to_right_double_shine);
                   anim.setDuration(1000);
                   detailsBlockShine.startAnimation(anim);
                   anim.setAnimationListener(new Animation.AnimationListener() {
                       @Override
                       public void onAnimationStart(Animation animation) {

                       }

                       @Override
                       public void onAnimationEnd(Animation animation) {
                           new Handler().postDelayed(new Runnable() {
                               @Override
                               public void run() {
                                   detailsBlockShine.startAnimation(anim);
                               }
                           }, 3000);
                       }

                       @Override
                       public void onAnimationRepeat(Animation animation) {

                       }
                   });
                   MuzifyRoomDatabase instance = MuzifyRoomDatabase.getInstance(getApplicationContext());
                   //dialog = new ShareInfoDialog(InnerCardActivity.this, muzifyViewModel);
                   albumLD = instance.albumDAO().getAlbumsForSpecificCard(cardNumber);
                   albumLD.observe(InnerCardActivity.this, new Observer<List<LocalStorage.Album>>() {
                       @Override
                       public void onChanged(List<LocalStorage.Album> albums) {
                           albumLD.removeObserver(this);
                           //4 thumbnails
                           if(imageOne == null){
                               initializeViews();
                           }
                           albumTemp = Album.sortAlbums(albums);
                           ArrayList<String> thumbnailUrls = innerCard.getAlbumCoverForThumbnail(albums);
                           Picasso.get().load(thumbnailUrls.get(0)).placeholder(getRandomDrawableIDForThumbnail()).into(imageOne);
                           Picasso.get().load(thumbnailUrls.get(1)).placeholder(getRandomDrawableIDForThumbnail()).into(imageTwo);
                           Picasso.get().load(thumbnailUrls.get(2)).placeholder(getRandomDrawableIDForThumbnail()).into(imageThree);
                           Picasso.get().load(thumbnailUrls.get(3)).placeholder(getRandomDrawableIDForThumbnail()).into(imageFour);
                           innerCardPlaylistItemsRecyclerView.setAdapter(new CardRecyclerViewAdapter(getApplicationContext(), innerCard,  albumTemp, InnerCardActivity.this));
                           innerCardPlaylistItemsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                           if(!refreshingSharedUserDetails){
                               //stopLoading();
                               stopShimmer();
                           }
                       }
                   });
               }
           });



       }catch (Exception e) {
            Log.e("HISTORY", "History_InnerCard_Initialization | "+e.getMessage());
           Toast.makeText(this, "Some error occurred", Toast.LENGTH_LONG).show();
           finishActivity(-1);
       }
    }


    private void updateDisplayAndShareInfosRecyclerView(List<LocalStorage.ShareInfo> shareInfos)
    {
        stopShimmer();
        showingPlaylistItems = false;
        detailsBlockRecyclerView.setAdapter(new DetailsBlockRecyclerViewAdapter(innerCard, InnerCardActivity.this));
        detailsBlockRecyclerView.getAdapter().notifyDataSetChanged();
        detailsBlockRecyclerView.scrollToPosition(DetailsBlockRecyclerViewAdapter.BLOCK_5_POSITION);
        innerCardPlaylistItemsRecyclerView.setAdapter(new CardRecyclerViewAdapter(shareInfos, InnerCardActivity.this, sharedMemory.getDefaultAccount()));
        innerCardPlaylistItemsRecyclerView.getAdapter().notifyDataSetChanged();
    }




    @Override
    public void handleExternalLinkClick(int position) {
        if(albumTemp == null){
            Utilities.Loggers.showShortToast(getApplicationContext(), "Unable to open external link");
            return;
        }
        LocalStorage.Album album = albumTemp.get(position);
        String songID = (album.isSourceSelected()) ? album.sourceSongID : album.getDestinationSongID();
        int serviceCode = (album.isSourceSelected()) ? innerCard.originalSource.getCode() : innerCard.destination.getCode();
        ContentLinkInterface musicService = MusicServices.getAppropriateServiceForContentLinking(InnerCardActivity.this, serviceCode);
        if(musicService != null){
            if(album.getIsSongFailed()){
                Utilities.Loggers.showLongToast(InnerCardActivity.this, "Cannot open destination link for failed items");
                return;
            }
            musicService.openPlaylistItemInApp(songID);
        }
    }

    @Override
    public void handleMusicServiceToggleClick(int position, boolean isSourceSelected) {
        LocalStorage.Album album = albumTemp.get(position);
        album.isSourceSelected = isSourceSelected;
        albumTemp.remove(position);
        albumTemp.add(position, album);
    }

    @Override
    public int getServiceThemeColor(boolean isSource) {
        return isSource ? innerCard.originalSource.getThemeColor(InnerCardActivity.this) : innerCard.destination.getThemeColor(InnerCardActivity.this);
    }

    @Override
    public int getSecondaryColor() {
        return getColor(R.color.grey);
    }

    public void handleNewCodeCreationRequest(DetailsBlockRecyclerViewAdapter.MyViewHolder holder) {
        CloudStorage.addShareV2(sharedMemory.getDefaultAccount(), muzifyViewModel, cardNumber, System.currentTimeMillis(), this, holder);
    }

    //method copy is in DetailsBlockRecyclerViewAdapter
    public void handleAddShareCallbackV2(String shareCode, DetailsBlockRecyclerViewAdapter.MyViewHolder holder) {
        cardData.setShareCode(shareCode);

        holder.block1.setVisibility(View.GONE);
        holder.block2.setVisibility(View.GONE);
        holder.block4.setVisibility(View.GONE);
        holder.block5.setVisibility(View.VISIBLE);

        holder.block5_firstPhase.setVisibility(View.GONE);
        holder.block5_secondPhase.setVisibility(View.GONE);
        holder.block5_thirdPhase.setVisibility(View.VISIBLE);

        holder.block5_thirdPhase_shareCode.setText(shareCode);
        holder.block5_thirdPhase_copyCode_btn.setColorFilter(getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        holder.block5_thirdPhase_copyCode_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uri = "https://www.muzify.littlefoxstudios.com/share?code="+shareCode;
                Utilities.setClipboard(InnerCardActivity.this, uri);
                Utilities.Loggers.showLongToast(InnerCardActivity.this, "Link copied to clipboard!");
            }
        });
        holder.shareCount.setText(innerCard.getShareCount()+"");
    }

    public void handleAddShareCallbackV2FromCloud(String shareCode, DetailsBlockRecyclerViewAdapter.MyViewHolder holder) {
        handleAddShareCallbackV2(shareCode, holder);
        detailsBlockRecyclerView.setAdapter(new DetailsBlockRecyclerViewAdapter(innerCard, InnerCardActivity.this));
        detailsBlockRecyclerView.getAdapter().notifyDataSetChanged();
        detailsBlockRecyclerView.scrollToPosition(DetailsBlockRecyclerViewAdapter.BLOCK_5_POSITION);
    }

    public void handleRefreshShareCallbackV2(List<LocalStorage.ShareInfo> updatedShareInfo) {
        shareInfoDownloaded = true;
        innerCard.shareCount = updatedShareInfo.size();
        //showPlaylistItemsShimmer();
        updateDisplayAndShareInfosRecyclerView(updatedShareInfo);
        //stopPlaylistItemsShimmer();
    }



    //DEPRECATED ON V1 - Using V2 Share
    public class ShareInfoDialog
    {
        private Activity activity;
        private AlertDialog alertDialog;
        private View dialogView;

        TextView shareCodeText;
        TextView shareCodeWaitText;
        RecyclerView shareInfoRecyclerView;


        LocalStorage.Card cardData;
        MuzifyViewModel muzifyViewModel;

        public ShareInfoDialog(Activity activity, MuzifyViewModel muzifyViewModel){
            this.activity = activity;
            LayoutInflater inflater = activity.getLayoutInflater();
            dialogView = inflater.inflate(R.layout.history_inner_card_share_info_dialog_box, null);
            shareCodeText = dialogView.findViewById(R.id.shareCodeText);
            shareCodeWaitText = dialogView.findViewById(R.id.shareCodeWaitText);
            shareInfoRecyclerView = dialogView.findViewById(R.id.ShareInfoRecyclerView);
            this.muzifyViewModel = muzifyViewModel;

            shareCodeWaitText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(cardData.getShareCode() != null && !cardData.getShareCode().equals("")){
                        showCode(cardData.getShareCode());
                    }
                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogTheme);
            if(dialogView.getParent()!=null)
                ((ViewGroup)dialogView.getParent()).removeView(dialogView);
            builder.setView(dialogView);
            builder.setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    stopDialog();
                }
            });
            shareInfoRecyclerView.setAdapter(new ShareInfoRecyclerViewAdapter(activity.getApplicationContext(), new InnerCardObj.ShareModel(), userEmailID));
            shareInfoRecyclerView.setLayoutManager(new LinearLayoutManager(activity.getApplicationContext()));
            alertDialog = builder.create();
        }

        public void showCodeIfPresent(List<LocalStorage.ShareInfo> shareInfos)
        {
            if(cardData.getShareCode() != null && !cardData.getShareCode().equals("")){
                showCode(cardData.getShareCode());
                if(cardData.isSharedUserDataSyncAvailable()){
                    cardData.updateSharedUserDataSync();
                    refreshingSharedUserDetails = true;
                    CloudStorage.refreshSharedUserDetails(cardData.getShareCode(), muzifyViewModel, activity, shareInfos, (InnerCardActivity) activity);
                    try{
                        muzifyViewModel.update(cardData);
                    }catch (Exception e){
                        Utilities.Loggers.postInfoLog("INNER_CARD", "unable to update shared user data");
                    }
                }
            }
        }

        public void stopDialog(){
            alertDialog.dismiss();
        }


       public void showCode(String code)
       {
           shareCodeText.setVisibility(View.VISIBLE);
           Utilities.fadeIn(code, shareCodeText, 0, getApplicationContext());
           shareCodeText.setText(code);
           shareCodeWaitText.setVisibility(View.GONE);
       }

        private void showWait(InnerCardObj.ShareModel sm){
            shareCodeText.setVisibility(View.GONE);
            shareCodeWaitText.setVisibility(View.VISIBLE);
            shareCodeWaitText.setText("Generating Share Code...");
            shareCodeWaitText.setTextColor(activity.getResources().getColor(R.color.nav_btn_selected));
            CloudStorage.addShare(userEmailID, muzifyViewModel, cardData.getCardNumber(), System.currentTimeMillis(), sm, this) ;
            alertDialog.show();
        }

        public void show(InnerCardObj.ShareModel sm)
        {
            showCode(sm.shareCode);
            shareInfoRecyclerView.setAdapter(new ShareInfoRecyclerViewAdapter(activity.getApplicationContext(), sm, userEmailID));
            shareInfoRecyclerView.setLayoutManager(new LinearLayoutManager(activity.getApplicationContext()));
            shareInfoRecyclerView.getAdapter().notifyDataSetChanged();
            alertDialog.show();
        }
    }

    private void openShareInfoDialog(InnerCardObj innerCard)
    {
        try{
            if(innerCard.getShareModel() == null || innerCard.getShareModel().shareCode == null){
                if(innerCard.getShareModel() == null){
                    innerCard.setShareModel(new InnerCardObj.ShareModel());
                }
                dialog.showWait(innerCard.shareModel);
            }else{
                dialog.show(innerCard.shareModel);
            }
        }catch (Exception e){
            Utilities.Loggers.postErrorLog(e.getMessage());
        }
    }


}