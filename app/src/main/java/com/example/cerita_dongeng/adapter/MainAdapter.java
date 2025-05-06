package com.example.cerita_dongeng.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cerita_dongeng.R;
import com.example.cerita_dongeng.activities.DetailActivity;
import com.example.cerita_dongeng.model.ModelMain;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> implements Filterable {

    private Context context;

    private List<ModelMain> modelMainList;
    private List<ModelMain> modelMainFilterList;

    @Override
    public Filter getFilter() {
        return modelFilter;
    }

    private Filter modelFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ModelMain> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(modelMainFilterList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (ModelMain modelMainFilter : modelMainFilterList) {
                    if (modelMainFilter.getStrJudul().toLowerCase().contains(filterPattern)) {
                        filteredList.add(modelMainFilter);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            modelMainList.clear();
            modelMainList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public MainAdapter(Context context, List<ModelMain> modelMain) {
        this.context = context;
        this.modelMainList = modelMain;
        this.modelMainFilterList = new ArrayList<>(modelMain);
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_main, parent, false);
        return new MainViewHolder(view);
    }


    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        ModelMain item = modelMainList.get(position);

        holder.tvJudul.setText(item.getStrJudul());

        Picasso.get()
                .load(item.getImagePath())
                .placeholder(R.drawable.ic_icon_list)
                .error(R.drawable.ic_icon_list)
                .into(holder.imgLogo);


        //send data to detail activity
        holder.cvDongeng.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra(DetailActivity.DETAIL_DONGENG, modelMainList.get(position));
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return modelMainList.size();
    }

    public static class MainViewHolder extends RecyclerView.ViewHolder {

        CardView cvDongeng;
        TextView tvJudul;
        ImageView imgLogo;

        public MainViewHolder(View itemView) {
            super(itemView);
            cvDongeng = itemView.findViewById(R.id.cvDongeng);
            tvJudul = itemView.findViewById(R.id.tvJudul);
            imgLogo = itemView.findViewById(R.id.imgLogo);

        }
    }

}