package com.littlefoxstudios.muzify.homescreenfragments;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Parcelable;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.littlefoxstudios.muzify.Constants;
import com.littlefoxstudios.muzify.MuzifySharedMemory;
import com.littlefoxstudios.muzify.R;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.datastorage.LocalStorage;
import com.littlefoxstudios.muzify.datastorage.MuzifyViewModel;
import com.littlefoxstudios.muzify.homescreenfragments.history.innercard.InnerCardActivity;
import com.littlefoxstudios.muzify.homescreenfragments.history.innercard.InnerCardObj;
import com.littlefoxstudios.muzify.homescreenfragments.history.outercard.CardInterface;
import com.littlefoxstudios.muzify.homescreenfragments.history.outercard.CardRecyclerViewAdapter;
import com.littlefoxstudios.muzify.homescreenfragments.history.outercard.OuterCard;
import com.littlefoxstudios.muzify.internet.InternetTesterInterface;
import com.littlefoxstudios.muzify.internet.InternetTestingViewModel;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment implements CardInterface, InternetTesterInterface {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    boolean filterNotFixed = true;

    ImageView searchIcon;
    EditText searchText;
    ImageView searchClear;

    CheckBox showOnlyFailedItemsFilterCheck;
    CardRecyclerViewAdapter adapter;
    RecyclerView recyclerView;
    Filter selectiveFilter;
    Spinner sourceFilterSpinner;
    Spinner destinationFilterSpinner;
    ImageView clearFilter;
    ImageView openFilter;
    ImageView closeFilter;

    LinearLayout filterBlock;


    ArrayAdapter spinnerAdapter;
    Animation fade;
    Animation fadeOut;

    ImageView deleteCards;
    boolean deleteMode = false;

    TextView selectedItemsCount;
    CheckBox selectedItemsDeleteToggle;
    TextView selectedItemsDeleteConfirm;
    boolean confirmDelete = false;
    LinearLayout confirmDeleteBlock;
    DeleteMode dMode;

    boolean allowDelete = true;
    boolean allowFilter = true;

    private static final String LOG_TAG = "HISTORY";

    LiveData<List<LocalStorage.Card>> allCardsLD;
    MuzifyViewModel muzifyViewModel;

    ArrayList<OuterCard> outerCardsMain;
    ArrayList<OuterCard> outerCardsBackup;

    LinearLayout dataLayout;
    RelativeLayout noDataLayout;
    RelativeLayout loadingDataLayout;

    InnerCardObj ic;

    MuzifySharedMemory muzifySharedMemory;

    InternetTestingViewModel internetTest;
    Utilities.Alert alert;

    ShimmerFrameLayout outerCardsShimmer;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HistoryFragment() {
        // Required empty public constructor
    }

    public HistoryFragment(MuzifyViewModel muzifyViewModel){
        this.muzifyViewModel = muzifyViewModel;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
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


    private void updateOuterCardLayoutForDeletion()
    {
       updateOuterCardLayoutForDeletion(true);
    }

    private void updateOuterCardLayoutForDeletion(boolean deleteMode)
    {
        CardRecyclerViewAdapter adapter = new CardRecyclerViewAdapter(getContext(), outerCardsMain, this, deleteMode);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        if(muzifyViewModel == null){
            muzifyViewModel = new MuzifyViewModel(getActivity().getApplication());
        }

        String emailID = getActivity().getIntent().getStringExtra("emailID");
        allCardsLD = muzifyViewModel.getCardDataForSpecificUser(emailID);
        outerCardsShimmer = rootView.findViewById(R.id.outerCardShimmer);
        recyclerView = rootView.findViewById(R.id.historyOuterRecyclerView);
        initialize(rootView);

        runBackgroundOperation();

        adapter = new CardRecyclerViewAdapter(rootView.getContext(), outerCardsMain, this);
        recyclerView.setAdapter(adapter);


        allCardsLD.observe(getViewLifecycleOwner(), new Observer<List<LocalStorage.Card>>() {
            @Override
            public void onChanged(List<LocalStorage.Card> cards) {
                refreshScreen(new OuterCard().getOuterCards(cards));
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        fade = AnimationUtils.loadAnimation(rootView.getContext(), R.anim.fade);
        fadeOut = AnimationUtils.loadAnimation(rootView.getContext(), R.anim.fade_out);

        //TODO - Fix Filter and remove access block (Boolean - filterNotFixed)
        openFilter.setImageResource(R.drawable.filter_icon_blocked);

        return rootView;
    }


    void refreshScreen(ArrayList<OuterCard> t)
    {
        if(t != null && t.size() != 0) {
            stopShimmer();
            outerCardsMain = new ArrayList<>(t);
            outerCardsBackup = new ArrayList<>(t);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter.setOuterCards(new ArrayList<>(t));
            muzifySharedMemory.setIsHistoryAvailable(true);
            //showData();
        }else{
            if(muzifySharedMemory.getIsHistoryAvailable()){
                //showLoading();
                startShimmer();
            }else{
                stopShimmer(false);
                showNoData();
            }
        }
    }

    private void runBackgroundOperation()
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                   showData();
                   startShimmer();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private void startShimmer()
    {
        recyclerView.setVisibility(View.GONE);
        outerCardsShimmer.setVisibility(View.VISIBLE);
        outerCardsShimmer.startShimmer();
    }

    private void stopShimmer()
    {
        stopShimmer(true);
    }

    private void stopShimmer(boolean delay)
    {
        if(delay){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    outerCardsShimmer.setVisibility(View.GONE);
                    outerCardsShimmer.stopShimmer();
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }, 1500);
        }else{
            outerCardsShimmer.setVisibility(View.GONE);
            outerCardsShimmer.stopShimmer();
            recyclerView.setVisibility(View.VISIBLE);
        }
    }


    private void showData(){
        loadingDataLayout.setVisibility(View.GONE);
        noDataLayout.setVisibility(View.GONE);
        dataLayout.setVisibility(View.VISIBLE);
    }

    private void showNoData(){
        loadingDataLayout.setVisibility(View.GONE);
        noDataLayout.setVisibility(View.VISIBLE);
        dataLayout.setVisibility(View.GONE);
    }

    private void showLoading(){
        loadingDataLayout.setVisibility(View.VISIBLE);
        noDataLayout.setVisibility(View.GONE);
        dataLayout.setVisibility(View.GONE);
    }

    @Override
    public InternetTestingViewModel initializeInternetTestingViewModel(){
        return new ViewModelProvider(this).get(InternetTestingViewModel.class);
    }

    void initialize(View rootView)
    {
        selectiveFilter = new Filter();
        dMode = new DeleteMode();
        InputMethodManager imm = (InputMethodManager) rootView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        searchIcon = rootView.findViewById(R.id.OuterCardSearch);
        searchText = rootView.findViewById(R.id.OuterCardPlaylistTitleFilter);
        searchClear = rootView.findViewById(R.id.OuterCardClearSearch);
        clearFilter = rootView.findViewById(R.id.OuterCardClearFilter);
        openFilter = rootView.findViewById(R.id.OuterCardFilter);
        closeFilter = rootView.findViewById(R.id.OuterCardCloseFilter);
        showOnlyFailedItemsFilterCheck = rootView.findViewById(R.id.OuterCardFailedPlaylistOnlyCheckFilter);
        sourceFilterSpinner = rootView.findViewById(R.id.OuterCardSourceFilterSpinner);
        destinationFilterSpinner = rootView.findViewById(R.id.OuterCardDestinationFilterSpinner);

        spinnerAdapter = new ArrayAdapter(rootView.getContext(), android.R.layout.simple_spinner_item, selectiveFilter.getAllFormattedServiceNames());
        spinnerAdapter.setDropDownViewResource(R.layout.history_outer_card_filter_spinner_layout);
        sourceFilterSpinner.setAdapter(spinnerAdapter);
        destinationFilterSpinner.setAdapter(spinnerAdapter);

        sourceFilterSpinner.setSelection(0);
        destinationFilterSpinner.setSelection(0);

        filterBlock = rootView.findViewById(R.id.OuterCardFilterBlockLayout);
        fade = AnimationUtils.loadAnimation(getContext(), R.anim.fade);
        fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        filterBlock.setAlpha(0);
        filterBlock.setVisibility(View.GONE);

        deleteCards = rootView.findViewById(R.id.OuterCardDelete);
        selectedItemsCount = rootView.findViewById(R.id.OuterCardTotalItemSelectedDisplay);
        selectedItemsDeleteToggle = rootView.findViewById(R.id.OuterCardSelectiveFilterToggler);
        selectedItemsDeleteConfirm = rootView.findViewById(R.id.OuterCardSelectiveFilterConfirm);
        confirmDeleteBlock = rootView.findViewById(R.id.OuterCardConfirmDeleteBlock);

        dataLayout = rootView.findViewById(R.id.history_data_available);
        noDataLayout = rootView.findViewById(R.id.history_data_not_available);
        loadingDataLayout = rootView.findViewById(R.id.history_data_loading);

        muzifySharedMemory = new MuzifySharedMemory(getActivity());

        internetTest = initializeInternetTestingViewModel();
        alert = new Utilities.Alert(getActivity(), Utilities.Alert.NO_INTERNET);
        observeInternetConnection(internetTest, this, alert);

        deleteCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleDelete();
            }
        });


        sourceFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if(parent.getChildAt(0) == null){
                   return;
                }
                ((TextView) parent.getChildAt(0)).setTextSize(20);
                ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.white));
                boolean noneSelected = false;
                refillOuterCards();
                if(!selectiveFilter.getAllFormattedServiceNames().get(pos).equals(selectiveFilter.NONE)){
                    String selectedService = selectiveFilter.getAllFormattedServiceNames().get(pos);
                    selectiveFilter.setSource(selectedService);
                    addClearFilterIconColor();
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.nav_btn_selected));
                    if(selectiveFilter.getDestination() != null && selectiveFilter.getDestination().getFormattedServiceName().equals(selectedService)){
                        selectiveFilter.clearDestinationFilter();
                        refreshSpinnerAdapter();
                        destinationFilterSpinner.setSelection(0);
                    }
                }else{
                    selectiveFilter.clearSourceFilter();
                    refreshSpinnerAdapter();
                    ((TextView) parent.getChildAt(0)).setText(selectiveFilter.NONE);
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.white));
                    sourceFilterSpinner.setSelection(0);
                    noneSelected = true;
                }

                selectiveFilter.initializeFilterSelection();
                if(noneSelected){
                    if(selectiveFilter.getDestination() == null){
                        searchIcon.setImageResource(R.drawable.search_icon);
                        return;
                    }
                }

                searchIcon.setImageResource(R.drawable.search_blue_icon);

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        destinationFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if(parent.getChildAt(0) == null){
                   return;
                }
                ((TextView) parent.getChildAt(0)).setTextSize(20);
                boolean noneSelected = false;
                refillOuterCards();
                ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.white));
                if(!selectiveFilter.getAllFormattedServiceNames().get(pos).equals(selectiveFilter.NONE)){
                    String selectedService = selectiveFilter.getAllFormattedServiceNames().get(pos);
                    selectiveFilter.setDestination(selectedService);
                    addClearFilterIconColor();
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.nav_btn_selected));
                    if(selectiveFilter.getSource() != null && selectiveFilter.getSource().getFormattedServiceName().equals(selectedService)){
                        selectiveFilter.clearSourceFilter();
                        refreshSpinnerAdapter();
                        sourceFilterSpinner.setSelection(0);
                    }

                }else{
                   selectiveFilter.clearDestinationFilter();
                    refreshSpinnerAdapter();
                    ((TextView) parent.getChildAt(0)).setText(selectiveFilter.NONE);
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.white));
                    destinationFilterSpinner.setSelection(0);
                    noneSelected = true;
                }

                selectiveFilter.initializeFilterSelection();
                if(noneSelected){
                    if(selectiveFilter.getSource() == null){
                        searchIcon.setImageResource(R.drawable.search_icon);
                        return;
                    }
                }

                searchIcon.setImageResource(R.drawable.search_blue_icon);

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        selectedItemsDeleteToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedItemsDeleteToggle.isChecked()){
                    confirmDelete = true;
                    selectedItemsDeleteConfirm.setText("CONFIRM");
                }else{
                    confirmDelete = false;
                    selectedItemsDeleteConfirm.setText("CANCEL");
                }
            }
        });

        selectedItemsDeleteConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getSelectedCardCount() == 0 && confirmDelete)
                {
                    Utilities.Loggers.showLongToast(getContext(), "Select a playlist to delete!");
                    return;
                }
                if(confirmDelete) {
                    doSelectiveDeletion();
                }
                wrapDeleteSelection();
            }
        });

        showOnlyFailedItemsFilterCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(showOnlyFailedItemsFilterCheck.isChecked()){
                    selectiveFilter.onlyFailed = true;
                    showOnlyFailedItemsFilterCheck.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.nav_btn_selected)));
                    searchIcon.setImageResource(R.drawable.search_blue_icon);
                }else{
                    selectiveFilter.onlyFailed = false;
                    showOnlyFailedItemsFilterCheck.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    if(!selectiveFilter.isFilterUsed()){
                        searchIcon.setImageResource(R.drawable.search_icon);
                    }
                }
                addClearFilterIconColor();
            }
        });


        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = searchText.getText().toString();
                if(text.length() == 0 && (!selectiveFilter.isFilterUsed())){
                    Utilities.vibrate(rootView.getContext(), 1);
                    searchText.setFocusableInTouchMode(true);
                    searchText.requestFocus();
                    imm.showSoftInput(searchText, InputMethodManager.SHOW_FORCED);
                    return;
                }

                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                doFilter(text, rootView);
                searchIcon.setImageResource(R.drawable.search_icon);
            }
        });

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    doFilter(searchText.getText().toString(), rootView);
                    return true;
                }
                return false;
            }
        });

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() == 0){
                    searchIcon.setImageResource(R.drawable.search_icon);
                    removeFilter(rootView);
                    searchClear.setVisibility(View.GONE);
                }else{
                    searchIcon.setImageResource(R.drawable.search_blue_icon);
                    searchClear.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        searchClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchText.setText("");
                searchIcon.setImageResource(R.drawable.search_icon);
                removeFilter(rootView);
                searchClear.setVisibility(View.GONE);
                wrapDeleteSelection();
            }
        });

        clearFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearFilter();
                removeFilter(rootView);
                String text = searchText.getText().toString();
                if(text.length() > 0){
                    doFilter(text, rootView);
                }
            }
        });

        openFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*
                TODO : Filter Fix
                 */
                if(filterNotFixed){
                    Utilities.Loggers.showLongToast(getContext(), "Filter is currently disabled in this version!");
                    return;
                }

                if(!allowFilter){
                    Toast.makeText(getContext(), "Please close the delete mode first!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if((openFilter.getVisibility()) == (View.INVISIBLE)){
                    return;
                }
                filterBlock.setAlpha(1);
                filterBlock.startAnimation(fade);
                filterBlock.setVisibility(View.VISIBLE);
                openFilter.setVisibility(View.INVISIBLE);

                //block access to delete mode
                allowDelete = false;
                deleteCards.setImageResource(R.drawable.delete_icon_blocked);
            }
        });

        closeFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterBlock.startAnimation(fadeOut);
                filterBlock.setAlpha(0);
                filterBlock.setVisibility(View.GONE);
                openFilter.setVisibility(View.VISIBLE);

                //permit access to delete mode
                allowDelete = true;
                deleteCards.setImageResource(R.drawable.delete_icon_empty);
            }
        });
    }

    private ArrayList<LocalStorage.Card> getRoomCardData(ArrayList<OuterCard> outerCards){
        ArrayList<LocalStorage.Card> cards = new ArrayList<>();
        List<LocalStorage.Card> list = allCardsLD.getValue();
        for(OuterCard oc : outerCards){
            for(LocalStorage.Card c : list){
                if(c.getCardNumber() == oc.getCardNumber()){
                    cards.add(c);
                    break;
                }
            }
        }
        return cards;
    }

    private void handleDelete()
    {
        if(!allowDelete){
            Toast.makeText(getContext(), "Please close the filter mode first!", Toast.LENGTH_SHORT).show();
            return;
        }
        dMode.performOperation();
    }


    private void doSelectiveDeletion()
    {
        ArrayList<OuterCard> selectedCards = new ArrayList<>();
        for(OuterCard card : outerCardsMain){
            if(card.isCardSelected()){
                selectedCards.add(card);
            }
        }
        ArrayList<LocalStorage.Card> cards = getRoomCardData(selectedCards);
        muzifyViewModel.deleteSelectedCards(cards);

        if(outerCardsMain.size() == selectedCards.size()){
            muzifySharedMemory.setIsHistoryAvailable(false);
        }
    }

    private void refreshSpinnerAdapter()
    {
        spinnerAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, selectiveFilter.getAllFormattedServiceNames());
        spinnerAdapter.setDropDownViewResource(R.layout.history_outer_card_filter_spinner_layout);
    }

    private void resetOnlyFailedFilterCheck()
    {
        if(showOnlyFailedItemsFilterCheck.isChecked()){
            showOnlyFailedItemsFilterCheck.toggle();
        }
        showOnlyFailedItemsFilterCheck.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
    }

    private void clearFilter()
    {
        selectiveFilter.resetFilter();
        refreshSpinnerAdapter();
        sourceFilterSpinner.setAdapter(spinnerAdapter);
        destinationFilterSpinner.setAdapter(spinnerAdapter);
        sourceFilterSpinner.setSelection(0);
        destinationFilterSpinner.setSelection(0);

        resetOnlyFailedFilterCheck();
        searchIcon.setImageResource(R.drawable.search_icon);
        addClearFilterIconColor();
        clearFilter.setImageResource(R.drawable.filter_off_icon);
    }

    private void addClearFilterIconColor()
    {
        if(selectiveFilter.isFilterUsed()){
            clearFilter.setImageResource(R.drawable.filter_remove_red_icon);
        }else{
            clearFilter.setImageResource(R.drawable.filter_off_icon);
        }
        selectiveFilter.setFilterIconImage();
    }

    private void doFilter(String text, View rootView)
    {
        ArrayList<OuterCard> filteredOuterCards = new ArrayList<>();

        if(text.length() == 0){
            filteredOuterCards = new ArrayList<>(outerCardsMain);
        }else{
            for(OuterCard card : outerCardsMain)
            {
                if(card.getPlaylistTitle().toLowerCase().trim().contains(text.toLowerCase().trim())){
                    filteredOuterCards.add(card);
                }
            }
        }

        if(selectiveFilter.isFilterUsed()){
            filteredOuterCards = selectiveFilter.doAdditionalFilteringProcess(filteredOuterCards);
        }

        if(filteredOuterCards.size() == 0) {
            Toast.makeText(getContext(), "No titles found!", Toast.LENGTH_SHORT).show();
        }else{
            outerCardsMain = filteredOuterCards;
        }


        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        adapter.setOuterCards(filteredOuterCards);
        adapter.notifyDataSetChanged();
    }


    private void refillOuterCards(){
        if(outerCardsBackup == null){
            outerCardsBackup = new ArrayList<>();
        }
        outerCardsMain = new ArrayList<>(outerCardsBackup);
    }

    private void removeFilter(View rootView)
    {
        refillOuterCards();
        adapter = new CardRecyclerViewAdapter(rootView.getContext(), outerCardsMain, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(Integer position) {
        Intent intent = new Intent(getActivity(), InnerCardActivity.class);
        long cardNumber = allCardsLD.getValue().get(position).getCardNumber();
        String shareCode = allCardsLD.getValue().get(position).getShareCode();

        intent.putExtra("share_code", shareCode);
        intent.putExtra("card_number", cardNumber);
        intent.putExtra("emailID", getActivity().getIntent().getStringExtra("emailID"));

        startActivity(intent);
        ((Activity) getActivity()).overridePendingTransition(0, 0);
        /*
        ic = new InnerCardObj().getInnerCard(allCardsLD.getValue().get(position));

        LiveData<List<LocalStorage.ShareInfo>> shareInfo = muzifyViewModel.getShareInfoForSpecificCard(shareCode);
        shareInfo.observe(this, new Observer<List<LocalStorage.ShareInfo>>() {
            @Override
            public void onChanged(List<LocalStorage.ShareInfo> shareInfos) {
                shareInfo.removeObserver(this);
                if(position == null){
                    return;
                }
                ic = new InnerCardObj().updateInnerCard(allCardsLD.getValue().get(position), ic, shareInfos);
                //pass intent - innerCardObj
                Parcelable icP = Parcels.wrap(ic);
                intent.putExtra("innerCardObj", icP);
                intent.putExtra("card_number", cardNumber);
                intent.putExtra("emailID", getActivity().getIntent().getStringExtra("emailID"));
                startActivity(intent);
                ((Activity) getActivity()).overridePendingTransition(0, 0);
            }
        });

         */
    }

    private int getSelectionCount()
    {
        return Integer.parseInt(selectedItemsCount.getText().toString());
    }

    private void setSelectionCount(int count)
    {
        selectedItemsCount.setText(count+"");
    }

    @Override
    public void itemSelected(int position){
        //selecting cards for deletion
        int currentSelectionCount = getSelectionCount();
       if(!outerCardsMain.get(position).isCardSelected()){
           outerCardsMain.get(position).selectCard();
            currentSelectionCount++;
            if(currentSelectionCount == outerCardsMain.size()){
                dMode.setCurrentMode(DeleteMode.UNSELECT_ALL);
            }
       }else{
           outerCardsMain.get(position).removeSelection();
           currentSelectionCount--;
           dMode.setCurrentMode(DeleteMode.SELECT_ALL);
       }
       setSelectionCount(currentSelectionCount);
    }

    @Override
    public boolean isDeleteModeSelected() {
        return deleteMode;
    }

    @Override
    public void handleLongClick(int position) {
        if(!deleteMode){
            handleDelete();
        }
        itemSelected(position);
    }

    public class Filter{

        private boolean onlyFailed = false;
        private final String NONE = "-Select-";
        ArrayList<Utilities.MusicService> availableServices = new ArrayList<>();

        private Utilities.MusicService source;
        private Utilities.MusicService destination;

        Filter()
        {
           initializeAvailableServices();
        }

        public void initializeAvailableServices()
        {
            availableServices = Utilities.MusicService.getAllService();
        }

        public ArrayList<String> getAllFormattedServiceNames()
        {
            ArrayList<String> services = new ArrayList<>();
            services.add(NONE);
            for(Utilities.MusicService ms : availableServices){
                services.add(ms.getFormattedServiceName());
            }
            return services;
        }

        private void initializeFilterSelection()
        {
            if(sourceFilterSpinner.getChildAt(0) != null){
                setSource(((TextView) sourceFilterSpinner.getChildAt(0)).getText().toString());
            }
            if(destinationFilterSpinner.getChildAt(0) != null){
                setDestination(((TextView) destinationFilterSpinner.getChildAt(0)).getText().toString());
            }
        }

        boolean isFilterUsed()
        {
            initializeFilterSelection();
            return !(source == null && destination == null && !onlyFailed);
        }

        void setFilterIconImage()
        {
            if(isFilterUsed()){
                openFilter.setImageResource(R.drawable.filter_selected_icon);
            }else{
                openFilter.setImageResource(R.drawable.filter_on_icon);
            }
        }

        public void resetFilter()
        {
            clearSourceFilter();
            clearDestinationFilter();
            clearOnlyFailedCheck();
            initializeAvailableServices();
            setFilterIconImage();

        }

        public void clearSourceFilter()
        {
            source = null;
        }

        public void clearDestinationFilter()
        {
            destination = null;
        }

        public void clearOnlyFailedCheck()
        {
            onlyFailed = false;
        }

        public String getFilteredSourceName()
        {
            return (source == null) ? NONE : source.getFormattedServiceName();
        }

        public String getFilteredDestinationName() {
            return (destination == null) ? NONE : destination.getFormattedServiceName();
        }

        public Utilities.MusicService getSource()
        {
            return source;
        }

        public Utilities.MusicService getDestination()
        {
            return destination;
        }

        public void setSource(String sourceFormattedName)
        {
          source = (sourceFormattedName.equals(NONE)) ? null : Utilities.MusicService.getMusicServiceFromFormattedName(sourceFormattedName);
        }

        public void setDestination(String destinationFormattedName)
        {
            destination = (destinationFormattedName.equals(NONE)) ? null : Utilities.MusicService.getMusicServiceFromFormattedName(destinationFormattedName);
        }

        public boolean isOnlyFailedFilterSelected()
        {
            return onlyFailed;
        }

        private ArrayList<OuterCard> doAdditionalFilteringProcess(ArrayList<OuterCard> outerCards)
        {
            boolean sourceSelected = !(source == null);
            boolean destinationSelected = !(destination == null);
            if(!sourceSelected && !destinationSelected && !isOnlyFailedFilterSelected()){
                Utilities.Loggers.postInfoLog(LOG_TAG,"Source and Destination and only failed filter not set");
                return outerCards;
            }

            boolean onlyFilterFailedPlaylistSelected = (isOnlyFailedFilterSelected() && !sourceSelected && !destinationSelected);

            ArrayList<OuterCard> filteredCards = new ArrayList<>();

            for(OuterCard card : outerCards){
                boolean filterSuccess = false;
                if(isOnlyFailedFilterSelected()){
                    filterSuccess = !card.isPlaylistTransferSuccessful();
                    if(onlyFilterFailedPlaylistSelected){
                        if(filterSuccess){
                            filteredCards.add(card);
                        }
                        continue;
                    }
                }

                boolean sourceCheck = sourceSelected && card.getSourceServiceCode() == source.getCode();
                boolean destinationCheck = destinationSelected && card.getDestinationServiceCode() == destination.getCode();
                if(sourceSelected && destinationSelected){
                    if(sourceCheck && destinationCheck){
                        if(isOnlyFailedFilterSelected() && !filterSuccess){
                            continue;
                        }
                        filterSuccess = true;
                    }else{
                        continue;
                    }
                }else if(sourceSelected && sourceCheck){
                   filterSuccess = true;
                }else if(destinationSelected && destinationCheck){
                    filterSuccess = true;
                }


                if(filterSuccess){
                    filteredCards.add(card);
                }
            }

            return filteredCards;
        }
    }

    private void wrapDeleteSelection()
    {
        confirmDeleteBlock.setVisibility(View.GONE);
        deleteMode = false;
        confirmDelete = false;
        setSelectionCount(0);
        selectedItemsDeleteConfirm.setText("CANCEL");
        dMode.deselectAllCards();
        if(selectedItemsDeleteToggle.isChecked()){
            selectedItemsDeleteToggle.toggle();
        }
        dMode.setCurrentMode(DeleteMode.DELETE);
        updateOuterCardLayoutForDeletion(false);

        //allow filter process
        allowFilter = true;
        openFilter.setImageResource(R.drawable.filter_on_icon);
    }

    private int getSelectedCardCount(){
        int count = 0;
        for(OuterCard card : outerCardsMain){
            if(card.isCardSelected()){
                count++;
            }
        }
        return count;
    }

    class DeleteMode
    {
        public static final int DELETE = 1;
        public static final int SELECT_ALL = 2;
        public static final int UNSELECT_ALL = 3;

        int currentMode = DELETE;

        public void setCurrentMode(int mode)
        {
            switch (mode)
            {
                case DELETE: {
                    deleteCards.setImageResource(R.drawable.delete_icon_empty);
                    currentMode = DELETE;
                } break;

                case SELECT_ALL:{
                     deleteCards.setImageResource(R.drawable.select_all);
                     currentMode = SELECT_ALL;
                }break;

                case UNSELECT_ALL:{
                        deleteCards.setImageResource(R.drawable.select_all_selected);
                        currentMode = UNSELECT_ALL;
                }break;

            }
        }

        private void selectAllCards()
        {
            for(OuterCard card : outerCardsMain)
            {
                card.selectCard();
            }
        }

        private void deselectAllCards()
        {
            for(OuterCard card : outerCardsMain)
            {
                card.removeSelection();
            }
        }



        private void updateItemSelectionCount()
        {
           setSelectionCount(getSelectedCardCount());
        }

        public void performOperation()
        {
            switch(currentMode)
            {
                case DELETE:{
                    deleteMode = true;
                    updateOuterCardLayoutForDeletion();
                    setCurrentMode(SELECT_ALL);
                    confirmDeleteBlock.setVisibility(View.VISIBLE);
                    selectedItemsDeleteConfirm.setText("CANCEL");

                    //block filter process
                    allowFilter = false;
                    openFilter.setImageResource(R.drawable.filter_icon_blocked);
                }break;
                case SELECT_ALL:{
                    deleteMode = true;
                    setCurrentMode(UNSELECT_ALL);
                    selectAllCards();
                    updateOuterCardLayoutForDeletion();
                }break;
                case UNSELECT_ALL:{
                    deleteMode = true;
                    setCurrentMode(SELECT_ALL);
                    deselectAllCards();
                    updateOuterCardLayoutForDeletion();
                }
            }

            updateItemSelectionCount();
        }

    }

}