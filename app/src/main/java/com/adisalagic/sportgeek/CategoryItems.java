package com.adisalagic.sportgeek;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.adisalagic.sportgeek.api.ApiRoles;
import com.adisalagic.sportgeek.api.Item;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

public class CategoryItems extends AppCompatActivity {

    View                 rootView;
    LinearLayout         layout;
    FragmentManager      manager;
    ArrayList<Item>      items;
    FloatingActionButton fab;


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }
    };

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
        name.setText(getIntent().getStringExtra("categoryName"));
        if (ApiHandler.getInstance().getRole() > ApiRoles.GUEST) {
            fab = findViewById(R.id.floatingActionButton);
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog dialog = getDialogAddItem(v);
                    dialog.show();
                }
            });
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (!ApiHandler.getInstance().isApiAlive()) {
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
        for (Item item : items) {
            ItemFragment itemFragment = new ItemFragment(item);
            transaction.add(layout.getId(), itemFragment);
        }
        transaction.commit();
    }

    public void refresh() {
        FragmentTransaction transaction = manager.beginTransaction();
        for (Fragment fragment : manager.getFragments()) {
            transaction.remove(fragment);
        }
        transaction.commit();
        try {
            items = ApiHandler.getInstance().getApi().getItems(getIntent().getIntExtra("categoryId", -1));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        transaction = manager.beginTransaction();
        for (Item item : items) {
            ItemFragment itemFragment = new ItemFragment(item);
            transaction.add(layout.getId(), itemFragment);
        }
        transaction.commit();
    }


    public static final int PICK_IMAGE = 1;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
//            Bitmap bitmap = data.getExtras().getParcelable("data");
            File file = new File(data.getData().getPath());
            String filename = file.getName();
            filename.isEmpty();
            tvfilename.setText(filename);
            tvfilename.setVisibility(View.VISIBLE);
        }
    }
    TextView tvfilename;
    private AlertDialog getDialogAddItem(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        EditText name = view.findViewById(R.id.name);
        EditText price = view.findViewById(R.id.price);
        EditText amount = view.findViewById(R.id.amount);
        tvfilename = view.findViewById(R.id.file_name);
        Button button = view.findViewById(R.id.button_add_image);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");

                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                    startActivityForResult(chooserIntent, PICK_IMAGE);
                }else {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                }
            }
        });
        builder.setView(view);
        return builder.create();
    }
}
