package com.adisalagic.sportgeek;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.adisalagic.sportgeek.api.Item;

import java.util.ArrayList;

public class CategoryItems extends AppCompatActivity {

    View rootView;
    LinearLayout layout;
    FragmentManager manager;
    ArrayList<Item> items;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = getLayoutInflater().inflate(R.layout.activity_main, null);
        setContentView(R.layout.activity_main);
        try {
            items = ApiHandler.getInstance().getApi().getItems(getIntent().getIntExtra("categoryId", -1));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        manager = getSupportFragmentManager();
        TextView name = findViewById(R.id.text_category);
        String textName = getIntent().getStringExtra("categoryName");
        name.setText(getIntent().getStringExtra("categoryName"));
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (!ApiHandler.getInstance().isApiAlive()){
                    layout.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(layout.getContext(), Login.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        layout = findViewById(R.id.categories);
        FragmentTransaction transaction = manager.beginTransaction();
        for (Item item : items){
            ItemFragment itemFragment = new ItemFragment(item);
            transaction.add(layout.getId(), itemFragment);
        }
        transaction.commit();
    }
}
