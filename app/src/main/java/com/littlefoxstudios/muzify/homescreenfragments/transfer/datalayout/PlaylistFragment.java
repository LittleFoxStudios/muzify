package com.littlefoxstudios.muzify.homescreenfragments.transfer.datalayout;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.littlefoxstudios.muzify.MuzifyConfigs;
import com.littlefoxstudios.muzify.OnBackPressedInterface;
import com.littlefoxstudios.muzify.R;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.accounts.Spotify;
import com.littlefoxstudios.muzify.apis.API;
import com.littlefoxstudios.muzify.apis.SpotifyAPI;
import com.littlefoxstudios.muzify.apis.YoutubeMusicAPI;
import com.littlefoxstudios.muzify.homescreenfragments.TransferFragment;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.MusicServices;
import com.littlefoxstudios.muzify.homescreenfragments.transfer.TransferMusicServiceRecyclerView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlaylistFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlaylistFragment extends Fragment implements TransferPlaylistRecyclerView.ItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private TransferInfo transferInfo;
    private String sourceAccountEmail;

    RecyclerView recyclerView;
    TransferPlaylistRecyclerView playlistRVAdapter;
    ArrayList<Playlist> playlists;

    Utilities.Alert alert;
    RequestQueue queue;

    public PlaylistFragment() {
        // Required empty public constructor
    }

    public PlaylistFragment(TransferInfo transferInfo){
        this.transferInfo = transferInfo;
        this.sourceAccountEmail = transferInfo.getSourceAccountEmailID();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlaylistFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlaylistFragment newInstance(String param1, String param2) {
        PlaylistFragment fragment = new PlaylistFragment();
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
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        recyclerView = view.findViewById(R.id.transferFragmentPlaylistRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        playlists = new ArrayList<>();
        playlistRVAdapter = new TransferPlaylistRecyclerView(view.getContext(), playlists);
        playlistRVAdapter.setClickListener(this);
        recyclerView.setAdapter(playlistRVAdapter);
        alert = new Utilities.Alert(getActivity(), Utilities.Alert.LOADING);
        queue = Volley.newRequestQueue(view.getContext());
        loadPlaylists();
        return view;
    }

    public String getCurrentEmailID(){
        return sourceAccountEmail;
    }

    @Override
    public void onItemClick(View view, int position) {
        Playlist p = playlistRVAdapter.getItem(position);
        if(!verifyPlaylistSize(p)){
          if(p.getPlaylistItemsSize() == 0){
             Utilities.Loggers.showLongToast(getContext(), "This playlist is empty");
          }else{
              Utilities.Loggers.showLongToast(getContext(), "Sorry this playlist cannot be transferred. Maximum song count is "+ MuzifyConfigs.MAXIMUM_SONGS_SUPPORTED_FOR_TRANSFER);
          }
            return;
        }

        String playListID = playlistRVAdapter.getItem(position).getPlaylistID();
        transferInfo.setSourcePlaylistURL(playListID);
        Fragment nextFragment = new PlaylistItemsFragment(transferInfo, playListID);
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.frame_layout, nextFragment);
        fragmentTransaction.commit();
    }

    private boolean verifyPlaylistSize(Playlist p){
        return p.getPlaylistID().equals(YoutubeMusicAPI.YOUR_LIKES_PLAYLIST) ||
                p.getPlaylistID().equals(SpotifyAPI.YOUR_LIKES_PLAYLIST)  ||
                (p.getPlaylistItemsSize() > 0 && p.getPlaylistItemsSize() <= MuzifyConfigs.MAXIMUM_SONGS_SUPPORTED_FOR_TRANSFER);
    }


    private void loadPlaylists()
    {
        alert.startLoading("Loading", "Please wait. Fetching your playlists");
        playlists = TransferDataCache.getPlaylists(transferInfo.getSourceServiceCode(), sourceAccountEmail, transferInfo.getTransferDataCaches(), true);
        if(playlists == null || playlists.size() == 0){
             try
             {
                 API serviceApi = MusicServices.apiFactory(transferInfo.getSourceServiceCode(), queue, getActivity(), transferInfo);
                 serviceApi.getAllPlaylists(alert, this, serviceApi.getAllPlaylistInitialParams());
             }
             catch (Exception e)
             {
                 alert.stopDialog();
                 Utilities.Loggers.postInfoLog("LOAD_PLAYLISTS", e.getMessage());
                 Utilities.Loggers.showLongToast(getContext(), "Sorry. We are unable to get playlists");
             }
        }else{
           //playlist already present - back button pressed
            alert.stopDialog();
            if(playlistRVAdapter != null){
                playlistRVAdapter.update(this.playlists);
            }else{
                playlistRVAdapter = new TransferPlaylistRecyclerView(getContext(), this.playlists);
                playlistRVAdapter.setClickListener(this);
                recyclerView.setAdapter(playlistRVAdapter);
            }
            playlistRVAdapter.notifyDataSetChanged();
        }
    }

    public void updateUI(ArrayList<Playlist> playlists, boolean downloadFinished)
    {
        if(this.playlists != null && this.playlists.size() > 0){
            this.playlists.addAll(playlists);
        }else{
            //LIKED SONGS PLAYLIST
            if(transferInfo.getSourceServiceCode() == Utilities.MusicService.YOUTUBE_MUSIC.getCode()){
                Playlist like = new Playlist("LM", "Your Likes", "https://www.gstatic.com/youtube/media/ytm/images/pbg/liked-music-@576.png");
                playlists.add(0, like);
            }else if(transferInfo.getSourceServiceCode() == Utilities.MusicService.SPOTIFY.getCode()){
                Playlist like = new Playlist(SpotifyAPI.YOUR_LIKES_PLAYLIST, "Liked Songs", "https://t.scdn.co/images/3099b3803ad9496896c43f22fe9be8c4.png");
                playlists.add(0, like);
            }
            this.playlists = playlists;
        }
        if(playlistRVAdapter != null){
            playlistRVAdapter.update(this.playlists);
        }else{
            playlistRVAdapter = new TransferPlaylistRecyclerView(getContext(), this.playlists);
            playlistRVAdapter.setClickListener(this);
            recyclerView.setAdapter(playlistRVAdapter);
        }
        playlistRVAdapter.notifyDataSetChanged();
        ArrayList<TransferDataCache> cache = TransferDataCache.updateTransferDataCache(transferInfo.getSourceServiceCode(), sourceAccountEmail, this.playlists, transferInfo.getTransferDataCaches());
        transferInfo.setTransferDataCaches(cache);
        if(downloadFinished){
            alert.stopDialog();
        }
    }

}