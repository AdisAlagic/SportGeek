package com.adisalagic.sportgeek;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.adisalagic.sportgeek.api.Item;

public class ItemFragment extends Fragment {
    View     rootView;
    Item     item;
    TextView name;


    ItemFragment(Item item) {
        this.item = item;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_category_name, null);
        name = rootView.findViewById(R.id.name);
        name.setText(item.getName());
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(rootView.getContext(), ItemView.class);
                intent.putExtra("item", ApiHandler.getInstance().itemToBundle(item));
                startActivity(intent);
            }
        });
        return rootView;
    }

    public Item getItem() {
        return item;
    }


}
