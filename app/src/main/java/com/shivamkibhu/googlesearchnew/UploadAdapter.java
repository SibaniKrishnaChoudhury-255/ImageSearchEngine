package com.shivamkibhu.googlesearchnew;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import static com.shivamkibhu.googlesearchnew.UploadData.enteredKeywordHashSet;

public class UploadAdapter extends RecyclerView.Adapter<UploadAdapter.UploadViewHolder> {
    Context context;
    List<String> keywords;

    public UploadAdapter(Context context, List<String> keywords) {
        this.context = context;
        this.keywords = keywords;
    }

    @NonNull
    @Override
    public UploadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.keyword_itemdesign, parent, false);
        return new UploadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UploadViewHolder holder, final int position) {
        holder.enteredKeyword.setText(keywords.get(position));
        holder.setListener(position);
    }

    @Override
    public int getItemCount() {
        if (keywords == null) {
            keywords = new ArrayList<>();
        }
        return keywords.size();
    }

    public class UploadViewHolder extends RecyclerView.ViewHolder {
        TextView enteredKeyword;
        ImageView removeKeyword;

        public UploadViewHolder(@NonNull View itemView) {
            super(itemView);

            enteredKeyword = itemView.findViewById(R.id.enteredKeyword);
            removeKeyword = itemView.findViewById(R.id.remove);
        }

        public void setListener(final int position) {
            removeKeyword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    enteredKeywordHashSet.remove(keywords.get(position));
                    keywords.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, keywords.size());
                }
            });
        }
    }
}
