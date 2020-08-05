package com.shivamkibhu.googlesearchnew;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewholder> {
    public Context contextVar;
    List<String> url;


    public ImageAdapter(Context contextVar, List<String> url) {
        this.contextVar = contextVar;
        this.url = url;
    }

    @NonNull
    @Override
    public ImageViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.imageitem_design, parent, false);
        return new ImageViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewholder holder, final int position) {
        RequestOptions requestOptions = new RequestOptions();

        Random random = new Random();
        int ran = random.nextInt(5) + 1;
        switch (ran) {
            case 1:
                requestOptions.placeholder(R.drawable.background_1);
                break;
            case 2:
                requestOptions.placeholder(R.drawable.background_2);
                break;
            case 3:
                requestOptions.placeholder(R.drawable.background_3);
                break;
            case 4:
                requestOptions.placeholder(R.drawable.background_4);
                break;
            case 5:
                requestOptions.placeholder(R.drawable.background_5);
                break;
            case 6:
                requestOptions.placeholder(R.drawable.background_6);
                break;

            default:
                requestOptions.placeholder(R.drawable.background_2);
        }

        Glide.with(contextVar).load(url.get(position)).apply(requestOptions).into(holder.imageItem);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullImageIntent = new Intent(contextVar, FullImageDisplay.class);
                fullImageIntent.putExtra("url", url.get(position));
                contextVar.startActivity(fullImageIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return url.size();
    }

    public static class ImageViewholder extends RecyclerView.ViewHolder {
        ImageView imageItem;

        public ImageViewholder(@NonNull View itemView) {
            super(itemView);
            imageItem = itemView.findViewById(R.id.imageItems);
        }

    }
}
