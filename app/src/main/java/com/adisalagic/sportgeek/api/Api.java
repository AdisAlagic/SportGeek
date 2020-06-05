package com.adisalagic.sportgeek.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Api {
    private String url, version, token;
    private       int          port;
    private       OkHttpClient client;
    private       Gson         gson;
    private final MediaType    JSON = MediaType.get("application/json; charset=utf-8");

    Api(String url, int port, String version, String token, boolean usePort) {
        this.port = port;
        this.token = token;
        this.url = "http://" + url;
        this.version = version;
        client = new OkHttpClient();
        if (usePort) {
            this.url += ":" + port;
        }
        this.url += "/api";
        gson = new GsonBuilder().create();
    }


    public ArrayList<Catalog> getCatalogs() throws InterruptedException {
        final ArrayList<Catalog> catalogs       = new ArrayList<>();
        final Request            request        = new Request.Builder().url(url + "/" + version + "/catalog").build();
        Call                     call           = client.newCall(request);
        final CountDownLatch     countDownLatch = new CountDownLatch(1);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                catalogs.clear();
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Catalog[] catalogs1 = gson.fromJson(response.body().string(), Catalog[].class);
                catalogs.addAll(Arrays.asList(catalogs1));
                countDownLatch.countDown();
            }
        });

        countDownLatch.await();
        return catalogs;
    }

    public AuthResult getAuth(String login, String password) throws InterruptedException, IOException {
        final AuthResult[] result      = {new AuthResult()};
        RequestBody        requestBody = RequestBody.create(null, new byte[]{});
        Request request = new Request.Builder().url(url + "/" + version + "/auth?password=" + password +
                "&login=" + login).post(requestBody).build();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Call                 call           = client.newCall(request);
        final IOException[]  exception      = new IOException[1];
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                exception[0] = e;
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() == 401) {
                    result[0].setError("Unauthorized");
                    countDownLatch.countDown();
                    return;
                }
                result[0] = gson.fromJson(response.body().string(), AuthResult.class);
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
        if (exception[0] != null) {
            throw exception[0];
        }
        token = result[0].getToken();
        return result[0];
    }

    public ArrayList<Item> getItems(int categoryId) throws InterruptedException {
        final ArrayList<Item> items          = new ArrayList<>();
        Request               request        = new Request.Builder().url(url + "/" + version + "/catalog/" + categoryId + "/").build();
        final CountDownLatch  countDownLatch = new CountDownLatch(1);
        Call                  call           = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                items.clear();
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                items.addAll(Arrays.asList(gson.fromJson(response.body().string(), Item[].class)));
                countDownLatch.countDown();
            }
        });

        countDownLatch.await();
        return items;
    }

    public String addNewCategory(String name) throws InterruptedException {
        RequestBody        requestBody = RequestBody.create(null, new byte[]{});
        Request request = new Request.Builder()
                .url(url + "/" + version + "/catalog?name=" + name + "&token=" + token)
                .post(requestBody)
                .build();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final StringBuilder  error          = new StringBuilder();
        Call                 call           = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                error.append("Something broke");
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() == 200){
                    countDownLatch.countDown();
                }
                if (response.code() == 401){
                    error.append("Token check failed");
                }

            }
        });
        countDownLatch.await();
        return error.toString();
    }

    public void deleteCategory(Catalog catalog) throws Exception {
        final Request request = new Request.Builder()
                .url(url + "/" + version + "/catalog?id=" + catalog.getId() + "&token=" + token)
                .delete()
                .build();
        Call                 call           = client.newCall(request);
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                countDownLatch.countDown();
                if (response.code() == 401){
                    throw new IOException("Token check failed");
                }
            }
        });
        countDownLatch.await();
    }


    public boolean sendLiveSignal() throws InterruptedException {
        Request             request       = new Request.Builder().url(url).build();
        final AtomicBoolean atomicBoolean = new AtomicBoolean();
        Call                 call           = client.newCall(request);
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                atomicBoolean.set(false);
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                atomicBoolean.set(true);
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
        return atomicBoolean.get();
    }

    public boolean editCatalog(int id, String newName) throws InterruptedException {
        RequestBody body = RequestBody.create(null, new byte[]{});
        Request request = new Request.Builder().url(url + "/" + version + "/catalog/" + id + "?name=" + newName +
                "&token=" + token)
                .put(body)
                .build();
        Call                 call           = client.newCall(request);
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final AtomicBoolean  atomicBoolean  = new AtomicBoolean();
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                countDownLatch.countDown();
                atomicBoolean.set(false);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                countDownLatch.countDown();
                atomicBoolean.set(true);
            }
        });

        countDownLatch.await();
        return atomicBoolean.get();
    }

    public boolean deleteItem(int catalogId, int id) throws InterruptedException {
        Request request = new Request.Builder().url(url + "/" + version + "/catalog/" + catalogId + "/" + id + "?token=" + token)
                .delete()
                .build();
        Call                 call           = client.newCall(request);
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final AtomicBoolean  atomicBoolean  = new AtomicBoolean();
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                atomicBoolean.set(false);
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                atomicBoolean.set(response.code() != 401);
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
        return atomicBoolean.get();
    }
}
