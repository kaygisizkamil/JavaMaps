package com.kygsz.javamaps.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kygsz.javamaps.databinding.RecycleRowBinding;
import com.kygsz.javamaps.model.Place;
import com.kygsz.javamaps.view.MainActivity;
import com.kygsz.javamaps.view.MapsActivity;

import java.util.List;
import java.util.Map;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceHolder> {
    List<Place> places;
    public PlaceAdapter(List<Place> places){
        this.places=places;
    }
    @NonNull
    @Override
    public PlaceAdapter.PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecycleRowBinding rowBinding=RecycleRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new PlaceHolder(rowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceAdapter.PlaceHolder holder, int position) {
        //no data is sending in here ,it is just to bind textview to inside our recycleView
         holder.rowBinding.recyclerViewAdressTextView.setText(places.get(position).name);
         holder.itemView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent=new Intent(holder.itemView.getContext(), MapsActivity.class);
                 //after clicking recycleview element send the old value to control state inside Maps.activity
                 intent.putExtra("info","old");
                 intent.putExtra("place",places.get(position));
                 holder.itemView.getContext().startActivity(intent);
             }
         });
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public class PlaceHolder extends RecyclerView.ViewHolder {
        RecycleRowBinding rowBinding;
        public PlaceHolder(RecycleRowBinding rowBinding) {
            super(rowBinding.getRoot());
            this.rowBinding=rowBinding;
            // we no longer working with items  super(itemView);
        }
    }
}
