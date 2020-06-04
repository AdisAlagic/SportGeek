package com.adisalagic.sportgeek;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.adisalagic.sportgeek.api.Item;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

public class ItemView extends AppCompatActivity {

    ImageView image;
    TextView price;
    TextView amount;
    TextView name;
    Item data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_view);
        image = findViewById(R.id.pic);
        price = findViewById(R.id.price);
        amount = findViewById(R.id.amount);
        name = findViewById(R.id.name);
        data = ApiHandler.getInstance().itemFromBundle(Objects.requireNonNull(getIntent().getBundleExtra("item")));
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        final ProgressBar loader   = findViewById(R.id.loader);
        final Drawable[]  drawable = new Drawable[1];
        final Drawable noIn = this.getDrawable(R.drawable.ic_baseline_wifi_off_24);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                drawable[0] = LoadImageFromWebOperations(data.getImageUrl());
                image.post(new Runnable() {
                    @Override
                    public void run() {
                        loader.setVisibility(View.GONE);
                        image.setImageDrawable(drawable[0] == null ? noIn : drawable[0]);
                    }
                });
            }
        });
        price.setText(String.valueOf(data.getPrice()));
        amount.setText(String.valueOf(data.getAmount()));
        name.setText(data.getName());
    }

    public static Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            return Drawable.createFromStream(is, null);
        } catch (Exception e) {
            return null;
        }
    }
}