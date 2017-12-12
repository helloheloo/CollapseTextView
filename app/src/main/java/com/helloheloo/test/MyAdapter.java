package com.helloheloo.test;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.helloheloo.collapsetextview.CollapseTextView;


/**
 * Created by yangming on 2017/11/30.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ItemViewHolder>{
    public Context mContext;

    public MyAdapter(Context context){
        mContext = context;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemViewHolder holder = new ItemViewHolder(new CollapseTextView(mContext));
        return holder;
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.cTv.setOriginalText(mContext.getResources().getString(R.string.original_text));
        holder.cTv.setExpandText(mContext.getResources().getString(R.string.expand_text));
    }

    @Override
    public int getItemCount() {
        return 10;
    }

    public final class ItemViewHolder extends RecyclerView.ViewHolder{
        CollapseTextView cTv;
        public ItemViewHolder(View itemView) {
            super(itemView);
            cTv = (CollapseTextView) itemView;
        }
    }
}
