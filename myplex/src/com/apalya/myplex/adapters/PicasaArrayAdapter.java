
package com.apalya.myplex.adapters;

import java.util.List;
import java.util.Random;

import com.android.volley.toolbox.ImageLoader;
import com.apalya.myplex.R;
import com.apalya.myplex.utils.PicasaEntry;
import com.apalya.myplex.views.FadeInNetworkImageView;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;


public class PicasaArrayAdapter extends ArrayAdapter<PicasaEntry> {
    private ImageLoader mImageLoader;
    
    public PicasaArrayAdapter(Context context, 
                              int textViewResourceId, 
                              List<PicasaEntry> objects,
                              ImageLoader imageLoader
                              ) {
        super(context, textViewResourceId, objects);
        mImageLoader = imageLoader;
    }

    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.list_row, null);
        }
        
        ViewHolder holder = (ViewHolder) v.getTag(R.id.id_holder);       
        
        if (holder == null) {
            holder = new ViewHolder(v);
            v.setTag(R.id.id_holder, holder);
        }        
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(128), rnd.nextInt(64)); 
        
        holder.image.setBackgroundColor(color);
        
        PicasaEntry entry = getItem(position);
        if (entry.getThumbnailUrl() != null) {
            holder.image.setImageUrl(entry.getThumbnailUrl(), mImageLoader);
           
        } else {
            holder.image.setImageResource(R.drawable.iconrate);
        	
        }
        
        holder.title.setText(entry.getTitle());
        
        return v;
    }
    
    
    private class ViewHolder {
    	FadeInNetworkImageView image;
        TextView title; 
        ProgressBar mProgressBar;
        TextView percentage;
        
        public ViewHolder(View v) {
            image = (FadeInNetworkImageView) v.findViewById(R.id.list_image);
            title = (TextView) v.findViewById(R.id.title);
            mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
            percentage = (TextView) v.findViewById(R.id.percentage);
            
            v.setTag(this);
        }
    }
}
