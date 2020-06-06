package com.adisalagic.sportgeek;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adisalagic.sportgeek.api.ApiRoles;
import com.adisalagic.sportgeek.api.Catalog;
import com.adisalagic.sportgeek.api.Item;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import okhttp3.internal.concurrent.Task;

public class MainActivity extends AppCompatActivity {

    FragmentManager      manager;
    LinearLayout         layout;
    ArrayList<Catalog>   catalogs;
    FloatingActionButton actionButton;
    TextView             title;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("refresh", false)) {
                clearData();
                fillData();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProgressBar bar = new ProgressBar(getApplicationContext());
        bar.setVisibility(View.VISIBLE);
        setContentView(R.layout.activity_main);
        bar.setVisibility(View.GONE);
        actionButton = findViewById(R.id.floatingActionButton);
        title = findViewById(R.id.text_category);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearData();
                fillData(); //DEBUG ONLY
            }
        });
        fillData();
        final Activity context = this;
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("ACTION_REFRESH"));

    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (ApiHandler.getInstance().getRole() > ApiRoles.GUEST) {
            actionButton.setVisibility(View.VISIBLE);
            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final EditText editText = new EditText(v.getContext());
                    editText.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                            .setView(R.layout.dialog_add_category)
                            .setTitle("Добавить")
                            .setView(editText)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (editText.getText().toString().equals("")) {
                                        Toast.makeText(getApplicationContext(), "Название не указано!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    AsyncTask.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            String s = "";
                                            try {
                                                s = ApiHandler.getInstance().getApi().addNewCategory(editText.getText().toString());
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            if (s.isEmpty()) {
                                                Activity ac = (Activity) v.getContext();
                                                ac.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        clearData();
                                                        fillData();
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
                                }
                            })
                            .create();
                    dialog.show();
                }
            });
        }

    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(getApplicationContext(), "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public void fillData() {
        try {
            catalogs = ApiHandler.getInstance().getApi().getCatalogs();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        layout = findViewById(R.id.categories);
        manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (catalogs != null) {
            for (Catalog catalog : catalogs) {
                final CategoryName name = new CategoryName(catalog);
                transaction.add(layout.getId(), name);
            }
        }
        transaction.commit();
    }

    public void clearData() {
        List<Fragment>      fragments   = manager.getFragments();
        FragmentTransaction transaction = manager.beginTransaction();
        for (Fragment fragment : fragments) {
            transaction.remove(fragment);
        }
        transaction.commit();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (!ApiHandler.getInstance().isApiAlive()) {
                    title.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(title.getContext(), Login.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }
        });
    }

    // TODO: 07.06.2020 Create basket
}