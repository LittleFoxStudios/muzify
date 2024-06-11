package com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout;

import static com.littlefoxstudios.muzify.Utilities.getRandomDrawableIDForThumbnail;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.imageview.ShapeableImageView;
import com.littlefoxstudios.muzify.R;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.apis.API;
import com.littlefoxstudios.muzify.homescreenfragments.TransferFragment;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.MusicServices;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlaylistItemsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlaylistItemsFragment extends Fragment implements TransferPlaylistItemsRecyclerView.ItemClickListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TransferInfo transferInfo;
    private String sourceAccountEmail;
    private String playlistID;

    private RecyclerView recyclerView;
    private TransferPlaylistItemsRecyclerView playlistItemsRVAdapter;
    private Utilities.Alert alert;
    private ArrayList<PlaylistItem> playlistItems;
    private RequestQueue queue;

    private TextView playlistTitle;
    private ShapeableImageView playlistThumbnail;
    private Button continueButton;
    private Button backButton;

    private static final String CONTINUE = "CONTINUE";
    private static final String BLOCKED = "BLOCKED";

    public PlaylistItemsFragment() {
        // Required empty public constructor
    }

    public PlaylistItemsFragment(TransferInfo transferInfo, String playlistID){
        this.transferInfo = transferInfo;
        this.sourceAccountEmail = transferInfo.getSourceAccountEmailID();
        this.playlistID = playlistID;
    }


    public static PlaylistItemsFragment newInstance(String param1, String param2) {
        PlaylistItemsFragment fragment = new PlaylistItemsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlist_items, container, false);
        recyclerView = view.findViewById(R.id.transferFragmentPlaylistItemsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        playlistItems = new ArrayList<>();
        playlistItemsRVAdapter = new TransferPlaylistItemsRecyclerView(view.getContext(), playlistItems);
        playlistItemsRVAdapter.setClickListener(this);
        recyclerView.setAdapter(playlistItemsRVAdapter);
        alert = new Utilities.Alert(getActivity(), Utilities.Alert.LOADING);
        queue = Volley.newRequestQueue(view.getContext());
        initializeScreen(view);
        loadPlaylistItems();
        return view;
    }

    private void initializeScreen(View view)
    {
        playlistTitle = view.findViewById(R.id.transferFragmentPlaylistItemsTitle);
        playlistThumbnail = view.findViewById(R.id.transferFragmentPlaylistItemsThumbnail);
        continueButton = view.findViewById(R.id.transferFragmentPlaylistItemsContinueButton);
        backButton = view.findViewById(R.id.transferFragmentPlaylistItemsBackButton);
        backButton.setVisibility(View.INVISIBLE);
    }

    private void allowContinue()
    {
        continueButton.setText(CONTINUE);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(continueButton.getText().equals(CONTINUE)){
                    switchToTransferFragment();
                }else{
                    Utilities.Loggers.showShortToast(getContext(), "Please wait. Songs are still loading.");
                }
            }
        });
    }

    private void loadPlaylistItems()
    {
        Playlist selectedPlaylist = TransferDataCache.getSelectedPlaylist(playlistID, sourceAccountEmail, transferInfo.getSourceServiceCode(), transferInfo.getTransferDataCaches());
        if(selectedPlaylist == null){
            alert.stopDialog();
            Utilities.Loggers.showLongToast(getContext(), "Sorry. Internal error occurred while loading playlists!");
        }
        Picasso.get().load(selectedPlaylist.getPlaylistThumbnailUrl()).placeholder(getRandomDrawableIDForThumbnail()).into(playlistThumbnail);
        playlistTitle.setText(selectedPlaylist.getPlaylistTitle());
        playlistTitle.setSingleLine(true);
        playlistTitle.setSelected(true);
        playlistTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        alert.startLoading("Loading", "Please wait while we fetch your songs");
        playlistItems = TransferDataCache.getPlaylistItems(transferInfo.getSourceServiceCode(), sourceAccountEmail, transferInfo.getTransferDataCaches(), playlistID);
        if(playlistItems == null || playlistItems.size() == 0){
            API serviceAPI = MusicServices.apiFactory(transferInfo.getSourceServiceCode(), queue, getActivity(), transferInfo);
            try {
               serviceAPI.getAllPlaylistItems(alert, this, playlistID, serviceAPI.getAllPlaylistItemsInitialParams(), sourceAccountEmail);
            }catch (Exception e){
                alert.stopDialog();
                Utilities.Loggers.postInfoLog(Utilities.MusicService.getMusicServiceFromCode(transferInfo.getDestinationServiceCode()).getFormattedServiceName()+"_GET_PLAYLISTS", e.getMessage());
                Utilities.Loggers.showLongToast(getContext(), "Sorry. We are unable to get playlists");
            }
        }else{
            //playlist items already present - back button press
            alert.stopDialog();
            allowContinue();
            if(playlistItemsRVAdapter != null){
                playlistItemsRVAdapter.update(this.playlistItems);
            }else{
                playlistItemsRVAdapter = new TransferPlaylistItemsRecyclerView(getContext(), this.playlistItems);
                playlistItemsRVAdapter.setClickListener(this);
                recyclerView.setAdapter(playlistItemsRVAdapter);
            }
            playlistItemsRVAdapter.notifyDataSetChanged();
        }
    }

    public void updateUI(ArrayList<PlaylistItem> playlistItems, boolean downloadFinished){
        if(this.playlistItems != null && this.playlistItems.size() > 0){
            this.playlistItems.addAll(playlistItems);
        }else{
            this.playlistItems = playlistItems;
        }
        if(playlistItemsRVAdapter != null){
            playlistItemsRVAdapter.update(this.playlistItems);
        }else{
            playlistItemsRVAdapter = new TransferPlaylistItemsRecyclerView(getContext(), this.playlistItems);
            playlistItemsRVAdapter.setClickListener(this);
            recyclerView.setAdapter(playlistItemsRVAdapter);
        }
        playlistItemsRVAdapter.notifyDataSetChanged();
        ArrayList<TransferDataCache> cache = TransferDataCache.updateTransferDataCache(transferInfo.getSourceServiceCode(), sourceAccountEmail, this.playlistItems, transferInfo.getTransferDataCaches(), playlistID);
        transferInfo.setTransferDataCaches(cache);
        if(downloadFinished){
            allowContinue();
            alert.stopDialog();
        }
    }


    @Override
    public void onItemClick(View view, int position) {

    }

    private void switchToTransferFragment()
    {
        transferInfo.setSelectedPlaylistID(playlistID);
        Fragment nextFragment = new TransferFragment(transferInfo, TransferInfo.RECEIVER_SIDE);
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.frame_layout, nextFragment);
        fragmentTransaction.commit();
    }
}