package com.adisalagic.sportgeek;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adisalagic.sportgeek.api.AuthResult;

public class Login extends AppCompatActivity {

    enum DisconnectReason {
        EXCEPTION,
        INCORRECT_DATA,
        OK
    }

    View        rootView;
    View        opback;
    ProgressBar bar;
    Button      button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = getLayoutInflater().inflate(R.layout.activity_login, null);
        setContentView(rootView);
        opback = findViewById(R.id.opback);
        bar = findViewById(R.id.progress);
        button = findViewById(R.id.submit);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        final Pair<String, String> LoP       = getLoginAndPassword();
        CheckBox                   remeberMe = findViewById(R.id.remember);
        EditText             login     = findViewById(R.id.login);
        EditText             password  = findViewById(R.id.password);
        remeberMe.setChecked(true);
        if (LoP.first != null && LoP.second != null) {
            login.setText(LoP.first);
            password.setText(LoP.second);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    final DisconnectReason reason = auth(LoP.first, LoP.second);
                    bar.post(new Runnable() {
                        @Override
                        public void run() {
                            switch (reason) {
                                case EXCEPTION:
                                    Toast.makeText(getApplicationContext(), "Нет подключения", Toast.LENGTH_SHORT).show();
                                    break;
                                case INCORRECT_DATA:
                                    Toast.makeText(getApplicationContext(), "Похоже, данные устарели", Toast.LENGTH_SHORT).show();
                                    break;
                                case OK:
                                    Intent intent = new Intent(rootView.getContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                    break;
                            }
                            opback.setVisibility(View.GONE);
                            bar.setVisibility(View.GONE);
                        }
                    });
                }
            });
        }

    }

    public void onSubmitClick(final View view) {
//        button.setBackgroundColor(Color.parseColor("#00FFFF"));
        final EditText login     = findViewById(R.id.login);
        final EditText password  = findViewById(R.id.password);
        final CheckBox remeberMe = findViewById(R.id.remember);
        hide(true);
        button.setEnabled(false);
        final DisconnectReason[] disconnectReason = new DisconnectReason[1];
        login.setEnabled(false);
        password.setEnabled(false);
        remeberMe.setEnabled(false);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                disconnectReason[0] = auth(login.getText().toString(), password.getText().toString());
                button.post(new Runnable() {
                    @Override
                    public void run() {
                        hide(false);
                        switch (disconnectReason[0]) {
                            case EXCEPTION:
                                Toast.makeText(getApplicationContext(), "Нет подключения", Toast.LENGTH_SHORT).show();
                                break;
                            case INCORRECT_DATA:
                                Toast.makeText(getApplicationContext(), "Неверный логин или пароль!", Toast.LENGTH_SHORT).show();
                                break;
                            case OK:
                                if (remeberMe.isChecked()) {
                                    saveInSP(login.getText().toString(), password.getText().toString());
                                }
                                Intent intent = new Intent(view.getContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                                break;
                        }
                        hide(false);
                        button.setEnabled(true);
                        login.setEnabled(true);
                        password.setEnabled(true);
                        remeberMe.setEnabled(true);
                    }
                });
            }
        });


    }


    private void hide(boolean need) {
        if (need){
            opback.setVisibility(View.VISIBLE);
            bar.setVisibility(View.VISIBLE);

        }else {
            bar.setVisibility(View.GONE);
            opback.setVisibility(View.GONE);
        }
        opback.invalidate();
        bar.invalidate();
    }


    private DisconnectReason auth(String login, String password) {
        AuthResult result = new AuthResult();
        try {
            result = ApiHandler.getInstance().getApi().getAuth(login, password);
        } catch (Exception e) {
//            Toast.makeText(getApplicationContext(), "Не удалось подключиться к сети!", Toast.LENGTH_SHORT).show();
            return DisconnectReason.EXCEPTION;
        }

        if (result.getToken() == null) {
            return DisconnectReason.INCORRECT_DATA;
        } else {
            ApiHandler.getInstance().setRole(result.getRole());
            ApiHandler.getInstance().setToken(result.getToken());
            ApiHandler.getInstance().setResult(result);
            return DisconnectReason.OK;
        }
    }


    public void saveInSP(String login, String password) {
        SharedPreferences preferences = getSharedPreferences("apple", MODE_PRIVATE);
        preferences.edit()
                .putString("login", login)
                .putString("password", password)
                .apply();
    }

    public Pair<String, String> getLoginAndPassword() {
        String login    = getSharedPreferences("apple", MODE_PRIVATE).getString("login", null);
        String password = getSharedPreferences("apple", MODE_PRIVATE).getString("password", null);
        return new Pair<>(login, password);
    }
}