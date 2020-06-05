package com.adisalagic.sportgeek;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.adisalagic.sportgeek.api.Api;
import com.adisalagic.sportgeek.api.ApiBuilder;
import com.adisalagic.sportgeek.api.AuthResult;
import com.adisalagic.sportgeek.api.Item;

public class ApiHandler {

    private static volatile ApiHandler mInstance;
    private                 AuthResult result;
    private                 int        role  = 0;
    private                 String     token = "";
    private                 Api        api;

    private ApiHandler() {
        api = new ApiBuilder()
                .setUrl("192.168.31.38")
                .setPort(1337)
                .usePort(true)
                .build();
    }


    public void recreateApi(SharedPreferences sharedPreferences){
        String url = sharedPreferences.getString("url", "192.168.31.38");
        int port = Integer.parseInt(sharedPreferences.getString("port", "-1"));
        ApiBuilder apiBuilder = new ApiBuilder();
        apiBuilder.setUrl(url);
        boolean usingPort = port == -1;
        if (!usingPort){
            apiBuilder.setPort(port);
            apiBuilder.usePort(true);
        }else {
            apiBuilder.usePort(false);
        }
        api = apiBuilder.build();
    }


    public void setResult(AuthResult result) {
        this.result = result;
    }

    public AuthResult getResult() {
        return result;
    }

    public Api getApi() {
        return api;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getRole() {
        return role;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public static ApiHandler getInstance() {
        if (mInstance == null) {
            synchronized (ApiHandler.class) {
                if (mInstance == null) {
                    mInstance = new ApiHandler();
                }
            }
        }
        return mInstance;
    }

    public Bundle itemToBundle(Item item) {
        Bundle bundle = new Bundle();
        bundle.putInt("amount", item.getAmount());
        bundle.putInt("catalogId", item.getCatalogId());
        bundle.putInt("id", item.getId());
        bundle.putString("image", item.getImageUrl());
        bundle.putString("name", item.getName());
        bundle.putDouble("price", item.getPrice());
        return bundle;
    }

    public Item itemFromBundle(Bundle bundle) {
        Item item = new Item();
        item.setAmount(bundle.getInt("amount"));
        item.setCatalogId(bundle.getInt("catalogId"));
        item.setId(bundle.getInt("id"));
        item.setImageUrl(bundle.getString("image"));
        item.setPrice(bundle.getDouble("price"));
        item.setName(bundle.getString("name"));
        return item;
    }

    public boolean isApiAlive() {
        try {
            return api.sendLiveSignal();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }
}
