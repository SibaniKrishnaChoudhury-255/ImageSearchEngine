package com.shivamkibhu.googlesearchnew;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class UploadData extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteTextView;
    private Button addKey, uploadBtn, goMain;
    private RecyclerView wordListRecycler;
    private EditText urlText;
    private UploadAdapter adapter;
    private List<String> enteredKeywordList;
    public static HashSet<String> enteredKeywordHashSet;

    private TextView preUrl;

    private HashMap<String, String> validKeywords = new HashMap<>();

    private static final String SHAREDPREF = "upload";
    private static final String KEYWORDLENGTH = "lengthOfTrie";
    private static final String TRIE = "Trie";

    private FirebaseFirestore db_Reference;
    private CollectionReference allImageRef;
    private CollectionReference mapRef;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_data);

        init();

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (autoCompleteTextView.getText().toString().trim().length() != 0) {
                    String[] sug = searchInTrie(s.toString().toLowerCase());
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(UploadData.this, android.R.layout.simple_list_item_1, sug);
                    autoCompleteTextView.setAdapter(adapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        setUpRecycler();

        addKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredWord = autoCompleteTextView.getText().toString().trim().toLowerCase();
                if (!enteredWord.equals("")) {
                    if (validKeywords.containsKey(enteredWord)) {
                        if (!enteredKeywordHashSet.contains(validKeywords.get(enteredWord))) {
                            enteredKeywordHashSet.add(validKeywords.get(enteredWord));
                            enteredKeywordList.add(validKeywords.get(enteredWord));
                            adapter.notifyItemInserted(enteredKeywordList.size() - 1);
                        }
                    } else {
                        enteredKeywordHashSet.add(enteredWord);
                        enteredKeywordList.add(enteredWord);
                        adapter.notifyItemInserted(enteredKeywordList.size() - 1);
                    }
                }
                autoCompleteTextView.setText("");
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String url = urlText.getText().toString().trim();
                for (String placeToAdd : enteredKeywordHashSet) {
                    final DocumentReference allDocRef = allImageRef.document(placeToAdd);
                    if (validKeywords.containsKey(placeToAdd)) {
                        allDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    List<String> urls = (List<String>) documentSnapshot.get("urls");
                                    if (!url.isEmpty() && !urls.contains(url)) {
                                        urls.add(url);
                                        Map<String, List<String>> map = new HashMap<>();
                                        map.put("urls", urls);

                                        allDocRef.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(UploadData.this, "Added Successfully...", Toast.LENGTH_SHORT).show();
                                                    urlText.setText("");
                                                    preUrl.setText(url);
                                                }
                                                else
                                                    Toast.makeText(UploadData.this, "......Error.........", Toast.LENGTH_LONG).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(UploadData.this, "......Error...." + e.toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    } else {
                        List<String> urls = new ArrayList<>();
                        urls.add(url);
                        Map<String, List<String>> map = new HashMap<>();
                        map.put("urls", urls);

                        allDocRef.set(map);

                        DocumentReference keyMapRef = mapRef.document(placeToAdd);
                        HashMap<String, String> childMap = new HashMap<>();
                        childMap.put(placeToAdd, placeToAdd);

                        Map<String, Map<String, String>> allMap = new HashMap<>();
                        allMap.put("map", childMap);

                        validKeywords.put(placeToAdd, placeToAdd);
                        keyMapRef.set(allMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(UploadData.this, "Added Successfully...", Toast.LENGTH_SHORT).show();
                                    urlText.setText("");
                                    preUrl.setText(url);
                                }
                                else
                                    Toast.makeText(UploadData.this, "......Error.........", Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UploadData.this, "......Error...." + e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });

        goMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UploadData.this, MainActivity.class));
            }
        });
    }

    private void setUpRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        wordListRecycler.setLayoutManager(layoutManager);

        adapter = new UploadAdapter(this, enteredKeywordList);
        wordListRecycler.setAdapter(adapter);
    }

    private String[] searchInTrie(CharSequence s) {
        List<String> words = new ArrayList<>();
        Gson gson = new Gson();
        String json = sharedPreferences.getString(TRIE, null);
        Type type = new TypeToken<Trie>() {
        }.getType();
        Trie sugg = gson.fromJson(json, type);
        if (sugg != null) {
            boolean rightKeyword = true;
            for (Character c : s.toString().toCharArray()) {
                if (sugg.children.containsKey(c + ""))
                    sugg = sugg.children.get(c + "");
                else rightKeyword = false;
            }
            if (rightKeyword) searchFurtherKey(sugg, words);
        }

        String[] sug = new String[words.size()];
        for (int i = 0; i < words.size(); i++)
            sug[i] = words.get(i);

        return sug;


    }

    private static void searchFurtherKey(Trie sugg, List<String> words) {
        if (words.size() >= 7) return;
        if (sugg.children.containsKey("*")) {
            words.add(sugg.completeWord);
        }
        for (String s : sugg.children.keySet())
            searchFurtherKey(sugg.children.get(s), words);

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

    private void init() {
        autoCompleteTextView = findViewById(R.id.uploadAutoComp);
        addKey = findViewById(R.id.upload_addKeyBtn);
        uploadBtn = findViewById(R.id.uploadBtn);
        wordListRecycler = findViewById(R.id.upload_wordList);
        urlText = findViewById(R.id.imgUrl);
        goMain = findViewById(R.id.goMain);

        preUrl = findViewById(R.id.preUrl);

        enteredKeywordHashSet = new HashSet<>();
        enteredKeywordList = new ArrayList<>();

        sharedPreferences = getSharedPreferences(SHAREDPREF, MODE_PRIVATE);

        db_Reference = FirebaseFirestore.getInstance();
        allImageRef = db_Reference.collection("AllImages");
        mapRef = db_Reference.collection("keywordsMap");
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
                        final Trie finalSugg = new Trie("a");
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
                            validKeywords.put(keyword, map.get(keyword));
                        }
                    }
                }
            }
        });

//        CollectionReference ref = db_Reference.collection("own");
//        DocumentReference docRef = ref.document("ownDoc");
//
//        List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));
//        Map<String, HashMap<String, String>> map = new HashMap<>();
//        HashMap<String, String> child = new HashMap<>();
//        child.put("dhoni", "dhonu");
//        map.put("urls", child);
//        docRef.set(map);
    }
}
