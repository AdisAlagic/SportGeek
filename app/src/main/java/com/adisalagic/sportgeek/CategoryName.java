package com.adisalagic.sportgeek;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.adisalagic.sportgeek.api.Catalog;

import java.util.Objects;


public class CategoryName extends Fragment {

    private Catalog catalog;
    private View    rootView;

    public CategoryName(Catalog catalog) {
        this.catalog = catalog;
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_category_name, container, false);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CategoryItems.class);
                intent.putExtra("categoryId", catalog.getId());
                intent.putExtra("categoryName", catalog.getName());
                startActivity(intent);
            }
        });
        rootView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                if (catalog.getId() == 0) {
                    AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                            .setMessage("Вы не можете изменить этот основной раздел")
                            .create();
                    dialog.show();
                    return false;
                }
                AlertDialog alertDialog = new AlertDialog.Builder(v.getContext())
                        .setTitle("Что вы хотите сделать с \"" + catalog.getName() + "\"?")
                        .setPositiveButton("Редактировать", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO
                                final EditText name = new EditText(v.getContext());
                                final AlertDialog edit = new AlertDialog.Builder(v.getContext())
                                        .setView(name)
                                        .setTitle("Новое имя для " + catalog.getName())
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                try {
                                                    ApiHandler.getInstance().getApi().editCatalog(catalog.getId(),
                                                            name.getText().toString());
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                                Intent intent = new Intent("ACTION_REFRESH");
                                                intent.putExtra("refresh", true);
                                                LocalBroadcastManager.getInstance(v.getContext()).sendBroadcast(intent);
                                            }
                                        }).create();
                                edit.show();
                            }
                        })
                        .setNegativeButton("Удалить", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    ApiHandler.getInstance().getApi().deleteCategory(catalog);
                                    Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Успех", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent("ACTION_REFRESH");
                                    intent.putExtra("refresh", true);
                                    LocalBroadcastManager.getInstance(v.getContext()).sendBroadcast(intent);
                                } catch (Exception e) {
                                    Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).create();
                alertDialog.show();
                return false;
            }
        });
        setVars();
        return rootView;
    }


    public View getRootView() {
        return rootView;
    }

    private void setVars() {
        TextView view = rootView.findViewById(R.id.name);
        view.setText(catalog.getName());
    }

    public Catalog getCatalog() {
        return catalog;
    }
}