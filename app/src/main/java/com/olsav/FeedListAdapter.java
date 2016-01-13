package com.olsav;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import twitter4j.Status;

public class FeedListAdapter extends ArrayAdapter{
    private final Activity context;
    private final List statuses;

    public FeedListAdapter(Activity context, List statuses) {
        super(context, R.layout.feed_item, statuses);

        this.context = context;
        this.statuses = statuses;
    }

    public View getView(int position,View view,ViewGroup parent) {
        Status status = (Status) statuses.get(position);

        LayoutInflater inflater=context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.feed_item, null, true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.feedItemTitle);
        TextView extratxt = (TextView) rowView.findViewById(R.id.feedItemContent);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.avatar);

        txtTitle.setText(status.getUser().getName());
        extratxt.setText(status.getText());
        imageView.setImageBitmap(
                getBitmapFromUrl(
                        status.getUser().getProfileImageURL()
                )
        );

        return rowView;
    }

    public Bitmap getBitmapFromUrl(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();

            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
