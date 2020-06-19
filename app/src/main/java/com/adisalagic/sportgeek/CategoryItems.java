package com.adisalagic.sportgeek;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.adisalagic.sportgeek.api.ApiRoles;
import com.adisalagic.sportgeek.api.Item;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
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
                    showDialog(v);
                }
            });
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("ACTION_REFRESH"));
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
    File file;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
//            Bitmap bitmap = data.getExtras().getParcelable("data");
            try {
                file = FileUtil.from(this, data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
            String filename = file.getName();
            tvfilename.setText(filename);
            tvfilename.setVisibility(View.VISIBLE);
        }
    }

    TextView tvfilename;

    private void showDialog(View v) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        final View                view    = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        final EditText      name    = view.findViewById(R.id.name);
        final EditText      price   = view.findViewById(R.id.price);
        final EditText      amount  = view.findViewById(R.id.amount);
        tvfilename = view.findViewById(R.id.file_name);
        Button button = view.findViewById(R.id.button_add_image);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");

                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                    startActivityForResult(chooserIntent, PICK_IMAGE);
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
                }
            }
        });
        final Button      send   = view.findViewById(R.id.send);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        Activity activity = (Activity) v.getContext();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        });
                        int cId = getIntent().getIntExtra("categoryId", -1);
                        try {
                            int idOfNewItem = ApiHandler.getInstance().getApi().addItem(cId,
                                    name.getText().toString(),
                                    Integer.parseInt(amount.getText().toString()),
                                    Double.parseDouble(price.getText().toString()));
                            if (idOfNewItem != -1) {
                                if (file != null) {
                                    if (!ApiHandler.getInstance().getApi().uploadImageToItem(cId, idOfNewItem, file)){
                                        Toast.makeText(getApplicationContext(), "Ошибка при загрузке файла!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refresh();
                            }
                        });
                    }
                });
            }
        });

    }

    public String getPath(Context context, Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":")+1);
        cursor.close();

        cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,null
                , MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }
}
