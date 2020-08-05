package com.shivamkibhu.googlesearchnew;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.ArrayAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ConstantCommonMethods {
    public static final String SUGGESTIONS = "suggestions";
    public static final String TRIE = "trie";
    public static final String MINCOUNT = "minCount";

    public static SharedPreferences sharedPreferences;


    public static String[] showOptions() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(SUGGESTIONS, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        List<String> sugg = gson.fromJson(json, type);

        if (sugg == null)
            sugg = new ArrayList<>();
        String[] suggestions = new String[sugg.size()];
        for (int i = 0; i < sugg.size(); i++)
            suggestions[i] = sugg.get(i);

        return suggestions;
    }

    // search Suggestion from trie
    public static String[] searchInTrie(CharSequence s) {
        List<String> words = new ArrayList<>();
        Gson gson = new Gson();
        String json = ConstantCommonMethods.sharedPreferences.getString(TRIE, null);
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
        if (words.size() >= 4) return;
        if (sugg.children.containsKey("*")) {
            words.add(sugg.completeWord);
        }
        for (String s : sugg.children.keySet())
            searchFurtherKey(sugg.children.get(s), words);

    }

    // Store searched histiory
    public static void storeHistory(String query) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //Put the value to sharedPreference
        int preCount = sharedPreferences.getInt(query, 0);
        editor.putInt(query, preCount + 1);
        editor.apply();

        //Read suggestion array
        Gson gson = new Gson();
        String json = sharedPreferences.getString(SUGGESTIONS, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        List<String> sugg = gson.fromJson(json, type);

        if (sugg == null) sugg = new ArrayList<>();

        if (!sugg.contains(query)) {
            if (sugg.size() >= 4) {
                if (sharedPreferences.getInt(MINCOUNT, Integer.MIN_VALUE) < preCount + 1) {
                    // Update suggestions array
                    for (String s : sugg) {
                        if (sharedPreferences.getInt(s, -1) == sharedPreferences.getInt(MINCOUNT, -2)) {
                            sugg.remove(sugg.indexOf(s));
                            sugg.add(query);
                            break;
                        }
                    }

                }
            } else sugg.add(query);

            String arrayToString = gson.toJson(sugg);
            editor.putString(SUGGESTIONS, arrayToString);
        }
        int min = Integer.MAX_VALUE;
        for (String option : sugg)
            min = Math.min(min, sharedPreferences.getInt(option, Integer.MAX_VALUE));

        editor.putInt(MINCOUNT, min);
        editor.apply();
    }
}
