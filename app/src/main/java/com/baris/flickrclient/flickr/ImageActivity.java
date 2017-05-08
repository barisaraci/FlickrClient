package com.baris.flickrclient.flickr;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.baris.flickrclient.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

public class ImageActivity extends AppCompatActivity {

    private Context context;
    private Image image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        context = this;

        init();
    }

    private void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        image = getIntent().getExtras().getParcelable("image");

        PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
        Picasso.with(context)
                .load(image.getImageURL())
                .error(android.R.drawable.stat_notify_error)
                .placeholder(R.drawable.loading_anim_hd)
                .into(photoView);

        TextView tvTitle = (TextView) findViewById(R.id.text_view_title);
        tvTitle.setText("PHOTO " + image.getNo() + "\n" + image.getTitle());
    }
}
