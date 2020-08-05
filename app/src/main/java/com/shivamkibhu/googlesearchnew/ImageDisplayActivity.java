package com.shivamkibhu.googlesearchnew;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.shivamkibhu.googlesearchnew.ConstantCommonMethods.searchInTrie;
import static com.shivamkibhu.googlesearchnew.ConstantCommonMethods.sharedPreferences;
import static com.shivamkibhu.googlesearchnew.ConstantCommonMethods.showOptions;
import static com.shivamkibhu.googlesearchnew.ConstantCommonMethods.storeHistory;
import static com.shivamkibhu.googlesearchnew.MainActivity.validKeywordsSet;

public class ImageDisplayActivity extends AppCompatActivity {
    private FirebaseFirestore db_Reference;
    private CollectionReference collectionReference;
    private ImageAdapter imageAdapter;
    private RecyclerView imagelist;
    TextView next, pre;
    int min = 0;
    String queryWord = "";
    TextView totalImagesTv, curPageNum, totalPageNum;

    private AutoCompleteTextView searchQueryEditTv;
    private ImageView searchButton;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);

        init();

        retriveImages();

        searchQueryEditTv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                String query = searchQueryEditTv.getText().toString().toLowerCase().trim();
                if (query.length() == 0) {
                    String[] suggestions = showOptions();
                    showMenu(suggestions);
                } else {
                    String[] sug = searchInTrie(query);
                    showMenu(sug);
                }

                searchQueryEditTv.showDropDown();
                return false;
            }
        });

        searchQueryEditTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchQueryEditTv.getText().toString().trim().length() == 0) {
                    String[] suggestions = showOptions();
                    showMenu(suggestions);
                } else {
                    String[] sug = searchInTrie(s);
                    showMenu(sug);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        searchQueryEditTv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final String query = searchQueryEditTv.getText().toString().toLowerCase().trim();
                if (validKeywordsSet.contains(query)) {
                    // Hide keyboard
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), 0);

                    if (isConnected()) {
                        CollectionReference keywordsMap = db_Reference.collection("keywordsMap");
                        keywordsMap.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    List<DocumentSnapshot> snapshots = task.getResult().getDocuments();
                                    for (DocumentSnapshot snapshot : snapshots) {
                                        Map<String, String> map = (Map<String, String>) snapshot.get("map");
                                        if (map.containsKey(query)) {
                                            loadKeyword((String) map.get(query));
                                            break;
                                        }
                                    }
                                }
                            }
                        });
                    } else
                        Toast.makeText(ImageDisplayActivity.this, "Please check your internet connectivity!!", Toast.LENGTH_SHORT).show();

                } else
                    Toast.makeText(ImageDisplayActivity.this, "Try!! Another word", Toast.LENGTH_SHORT).show();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchQueryEditTv.getText().toString().isEmpty()) {
                    //TODO: need to verify query is present in database or not if exist then run below method
                    final String query = searchQueryEditTv.getText().toString().toLowerCase().trim();
                    if (validKeywordsSet.contains(query)) {
                        // Hide keyboard
                        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        in.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                        if (isConnected()) {
                            CollectionReference keywordsMap = db_Reference.collection("keywordsMap");
                            keywordsMap.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        List<DocumentSnapshot> snapshots = task.getResult().getDocuments();
                                        for (DocumentSnapshot snapshot : snapshots) {
                                            Map<String, String> map = (Map<String, String>) snapshot.get("map");
                                            if (map.containsKey(query)) {
                                                loadKeyword((String) map.get(query));
                                                break;
                                            }
                                        }
                                    }
                                }
                            });
                        } else
                            Toast.makeText(ImageDisplayActivity.this, "Please check your internet connectivity!!", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(ImageDisplayActivity.this, "Try!! Another word", Toast.LENGTH_SHORT).show();
                }
            }
        });
        searchQueryEditTv.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (!searchQueryEditTv.getText().toString().isEmpty() && actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //TODO: need to verify query is present in database or not if exist then run below method
                    final String query = searchQueryEditTv.getText().toString().toLowerCase().trim();
                    if (validKeywordsSet.contains(query)) {
                        // Hide keyboard
                        InputMethodManager inm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);

                        if (isConnected()) {
                            CollectionReference keywordsMap = db_Reference.collection("keywordsMap");
                            keywordsMap.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        List<DocumentSnapshot> snapshots = task.getResult().getDocuments();
                                        for (DocumentSnapshot snapshot : snapshots) {
                                            Map<String, String> map = (Map<String, String>) snapshot.get("map");
                                            if (map.containsKey(query)) {
                                                loadKeyword((String) map.get(query));
                                                break;
                                            }
                                        }
                                    }
                                }
                            });
                        } else
                            Toast.makeText(ImageDisplayActivity.this, "Please check internet connectivity!!", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(ImageDisplayActivity.this, "Try!! Another word", Toast.LENGTH_SHORT).show();

                }
                return false;
            }
        });


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                retriveImages();
            }
        });

        pre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                loadPre();
            }
        });
    }

    private void loadKeyword(String query) {
        queryWord = query;
        storeHistory(query);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("Min", 0);
        editor.apply();
        retriveImages();
    }

    private void showMenu(String[] suggestions) {
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, suggestions);
        searchQueryEditTv.setAdapter(adapter);
    }

    private void retriveImages() {
        final DocumentReference docRef = collectionReference.document(queryWord);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                List<String> urls = (List<String>) documentSnapshot.get("urls");
                totalImagesTv.setText("Search results- " + urls.size());
                int pages = urls.size() / 10;
                pages = pages + (urls.size() % 10 > 0 ? 1 : 0);
                totalPageNum.setText(pages + "");
                List<String> urllist = new ArrayList<>();
                int val = sharedPreferences.getInt("Min", 0);
                if (val == 0) {
                    pre.setVisibility(View.GONE);
                    curPageNum.setText("1");
                } else {
                    pre.setVisibility(View.VISIBLE);
                    int pageNum = Integer.parseInt(curPageNum.getText().toString()) + 1;
                    curPageNum.setText(pageNum + "");
                }

                if (Integer.parseInt(curPageNum.getText().toString()) < Integer.parseInt(totalPageNum.getText().toString()))
                    next.setVisibility(View.VISIBLE);
                else next.setVisibility(View.GONE);

//                if(urls.size() > val + 10) next.setVisibility(View.VISIBLE);
//                else next.setVisibility(View.GONE);
                for (int i = val; i < val + 10; i++) {
                    if (i < urls.size()) {
                        urllist.add(urls.get(i));
                        min = i;
                    } else {
                        break;
                    }
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("Min", min + 1);
                editor.apply();
                Collections.shuffle(urllist);
                imageAdapter = new ImageAdapter(ImageDisplayActivity.this, urllist);
                StaggeredGridLayoutManager staggeredGrid = new StaggeredGridLayoutManager(2, LinearLayout.VERTICAL);
                imagelist.setLayoutManager(staggeredGrid);
                imagelist.setAdapter(imageAdapter);

            }
        });
    }

    private void loadPre() {
        final DocumentReference docRef = collectionReference.document(queryWord);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                List<String> urls = (List<String>) documentSnapshot.get("urls");
                List<String> urllist = new ArrayList<>();
                int val = sharedPreferences.getInt("Min", 0) - 10;
                if (val <= 10) pre.setVisibility(View.GONE);
                else pre.setVisibility(View.VISIBLE);

                next.setVisibility(View.VISIBLE);

                int curPage = Integer.parseInt(curPageNum.getText().toString());
                curPageNum.setText((curPage - 1) + "");
                int num = (curPage - 2) * 10;
                for (int i = num; i <= num + 9; i++) {
                    if (i < urls.size()) {
                        urllist.add(urls.get(i));
                    } else {
                        break;
                    }
                }

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("Min", num + 10);
                editor.apply();

                Collections.shuffle(urllist);
                imageAdapter = new ImageAdapter(ImageDisplayActivity.this, urllist);
                StaggeredGridLayoutManager staggeredGrid = new StaggeredGridLayoutManager(2, LinearLayout.VERTICAL);
                imagelist.setLayoutManager(staggeredGrid);
                imagelist.setAdapter(imageAdapter);

            }
        });
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
            return true;

        return false;
    }

    private void init() {
        searchQueryEditTv = findViewById(R.id.search_edtText);
        searchButton = findViewById(R.id.searchIcon);
        totalImagesTv = findViewById(R.id.totalImageTv);
        next = findViewById(R.id.next);
        pre = findViewById(R.id.pre);
        imagelist = findViewById(R.id.imageDisplay_recycler);
        curPageNum = findViewById(R.id.curPageNum);
        totalPageNum = findViewById(R.id.totPageNum);

        db_Reference = FirebaseFirestore.getInstance();
        collectionReference = db_Reference.collection("AllImages");

        queryWord = getIntent().getExtras().get("searchQuery").toString();
        String searchedQuery = getIntent().getExtras().get("oriSearchQuery").toString();
        if (!searchedQuery.equals("")) searchQueryEditTv.setText(searchedQuery);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("Min", min);
        editor.apply();
    }
}
