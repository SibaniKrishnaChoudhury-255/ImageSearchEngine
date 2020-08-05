package com.shivamkibhu.googlesearchnew;

import java.util.HashMap;

public class Trie {
    String alphabet;
    HashMap<String, Trie> children;
    String completeWord;

    public Trie(String alphabet){
        this.alphabet = alphabet;
        children = new HashMap<>();
    }
}
