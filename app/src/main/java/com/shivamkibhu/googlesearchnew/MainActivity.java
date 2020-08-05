package com.shivamkibhu.googlesearchnew;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.shivamkibhu.googlesearchnew.ConstantCommonMethods.TRIE;
import static com.shivamkibhu.googlesearchnew.ConstantCommonMethods.searchInTrie;
import static com.shivamkibhu.googlesearchnew.ConstantCommonMethods.sharedPreferences;
import static com.shivamkibhu.googlesearchnew.ConstantCommonMethods.showOptions;
import static com.shivamkibhu.googlesearchnew.ConstantCommonMethods.storeHistory;

public class MainActivity extends AppCompatActivity {

    private AutoCompleteTextView searchImageText;
    private ImageView searchButton;
//    private Button goUpload;
    public static HashSet<String> validKeywordsSet = new HashSet<>();

    private static final String SHAREDPREF = "google";
    private static final String KEYWORDLENGTH = "keywordLength";

    private FirebaseFirestore db_Reference;
    private CollectionReference collectionReference;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        searchImageText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                String query = searchImageText.getText().toString().toLowerCase().trim();
                if (query.length() == 0) {
                    String[] suggestions = showOptions();
                    showMenu(suggestions);
                } else {
                    String[] sug = searchInTrie(query);
                    showMenu(sug);
                }

                searchImageText.showDropDown();
                return false;
            }
        });

        searchImageText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchImageText.getText().toString().trim().length() == 0) {
                    String[] suggestions = showOptions();
                    showMenu(suggestions);
                } else {
                    String[] sug = searchInTrie(s.toString().toLowerCase());
                    showMenu(sug);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        searchImageText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final String query = searchImageText.getText().toString().toLowerCase().trim();
                if (validKeywordsSet.contains(query)) {
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
                                            Intent moveToNext = new Intent(MainActivity.this, ImageDisplayActivity.class);
                                            moveToNext.putExtra("searchQuery", (String) map.get(query));
                                            moveToNext.putExtra("oriSearchQuery", query);
                                            startActivity(moveToNext);

                                            storeHistory(query);
                                            break;
                                        }
                                    }
                                }
                            }
                        });

                    } else
                        Toast.makeText(MainActivity.this, "Please check internet connectivity!!", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(MainActivity.this, "Try!! Another word", Toast.LENGTH_SHORT).show();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchImageText.getText().toString().isEmpty()) {
                    //TODO: need to verify query is present in database or not
                    final String query = searchImageText.getText().toString().toLowerCase().trim();
                    if (validKeywordsSet.contains(query)) {
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
                                                Intent moveToNext = new Intent(MainActivity.this, ImageDisplayActivity.class);
                                                moveToNext.putExtra("searchQuery", (String) map.get(query));
                                                moveToNext.putExtra("oriSearchQuery", query);
                                                startActivity(moveToNext);

                                                storeHistory(query);
                                                break;
                                            }
                                        }
                                    }
                                }
                            });
                        } else
                            Toast.makeText(MainActivity.this, "Please check internet connectivity!!", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(MainActivity.this, "Try!! Another word", Toast.LENGTH_SHORT).show();
                }
            }
        });
        searchImageText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (!searchImageText.getText().toString().isEmpty() && actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //TODO: need to verify query is present in database or not
                    final String query = searchImageText.getText().toString().toLowerCase().trim();
                    if (validKeywordsSet.contains(query)) {
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
                                                Intent moveToNext = new Intent(MainActivity.this, ImageDisplayActivity.class);
                                                moveToNext.putExtra("searchQuery", (String) map.get(query));
                                                moveToNext.putExtra("oriSearchQuery", query);
                                                startActivity(moveToNext);

                                                storeHistory(query);
                                                break;
                                            }
                                        }
                                    }
                                }
                            });
                        } else
                            Toast.makeText(MainActivity.this, "Please check internet connection!!", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(MainActivity.this, "Try!! Another word", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

//        goUpload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, UploadData.class));
//            }
//        });
    }

    private void showMenu(String[] suggestions) {
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, suggestions);
        searchImageText.setAdapter(adapter);
    }

    private void createTrie(String keyword, Trie root) {
        Trie node = root;
        for (char c : keyword.toCharArray()) {
            if (!node.children.containsKey(c + ""))
                node.children.put(c + "", new Trie(""));

            node = node.children.get(c + "");
        }
        node.children.put("*", new Trie(""));
        node.completeWord = keyword;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson1 = new Gson();
        String arrayToString = gson1.toJson(root);
        editor.putString(TRIE, arrayToString);
        editor.apply();
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
            return true;

        return false;
    }

    private void init() {
        searchImageText = findViewById(R.id.search_edtText);
        searchButton = findViewById(R.id.searchIcon);
        sharedPreferences = getSharedPreferences(SHAREDPREF, MODE_PRIVATE);

//        goUpload = findViewById(R.id.goUpload);

        db_Reference = FirebaseFirestore.getInstance();
        collectionReference = db_Reference.collection("keywords");

    }

    @Override
    protected void onStart() {
        super.onStart();

        CollectionReference keywordsMap = db_Reference.collection("keywordsMap");
        keywordsMap.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> snapshots = task.getResult().getDocuments();
                    int size = 0;
                    for (DocumentSnapshot snapshot : snapshots) {
                        Map<String, String> map = (Map<String, String>) snapshot.get("map");
                        size += map.size();
                    }
                    int curKeywordLength = sharedPreferences.getInt(KEYWORDLENGTH, 0);
                    if (curKeywordLength != size) {
                        final Trie finalSugg = new Trie("aa");
                        for (DocumentSnapshot snapshot : snapshots) {
                            Map<String, String> map = (Map<String, String>) snapshot.get("map");
                            for (String keyword : map.keySet()) {
                                createTrie(keyword.toLowerCase(), finalSugg);
                            }
                        }
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(KEYWORDLENGTH, size);
                        editor.apply();
                    }

                    for (DocumentSnapshot snapshot : snapshots) {
                        Map<String, String> map = (Map<String, String>) snapshot.get("map");
                        for (String keyword : map.keySet()) {
                            validKeywordsSet.add(keyword);
                        }
                    }
                }
            }
        });
    }
}