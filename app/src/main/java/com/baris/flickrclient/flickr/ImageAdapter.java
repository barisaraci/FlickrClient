package com.baris.flickrclient.flickr;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baris.flickrclient.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by baris on 20/04/2017.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Image> images;

    public ImageAdapter(Context context, ArrayList<Image> imageList) {
        this.context = context;
        this.images = new ArrayList<>(imageList);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Image image = images.get(position);
        holder.bind(image);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void addItem(int position, Image image) {
        images.add(position, image);
        notifyItemInserted(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvImage;
        private ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.image);
            tvImage = (TextView) itemView.findViewById(R.id.textview_image);
        }

        public void bind(final Image image) {
            tvImage.setText("PHOTO " + image.getNo());
            Picasso.with(context)
                    .load(image.getThumbnailURL())
                    .error(android.R.drawable.stat_notify_error)
                    .placeholder(R.drawable.loading_anim)
                    .into(imageView);

            /*imageLoader.get(image.getThumbnailURL(), ImageLoader.getImageListener(imageView, R.drawable.loading_anim, android.R.drawable.stat_notify_error));
            imageView.setImageUrl(image.getThumbnailURL(), imageLoader);*/

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ImageActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("image", image);
                    context.startActivity(intent);
                }
            });
        }
    }

}
