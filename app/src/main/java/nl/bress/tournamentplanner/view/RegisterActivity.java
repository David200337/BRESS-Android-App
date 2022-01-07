package nl.bress.tournamentplanner.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.internal.TextWatcherAdapter;
import com.google.firebase.messaging.FirebaseMessaging;

import org.w3c.dom.Text;

import java.io.IOException;

import nl.bress.tournamentplanner.R;
import nl.bress.tournamentplanner.dao.interfaces.IAuth;
import nl.bress.tournamentplanner.dao.interfaces.IGame;
import nl.bress.tournamentplanner.domain.LoginModel;
import nl.bress.tournamentplanner.domain.LoginResponse;
import nl.bress.tournamentplanner.domain.LoginResponseWrapper;
import nl.bress.tournamentplanner.domain.RegisterModel;
import nl.bress.tournamentplanner.domain.RegisterResponseWrapper;
import nl.bress.tournamentplanner.domain.ScoreModel;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity {

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);

        EditText email_input = findViewById(R.id.register_email_input);
        EditText password_input = findViewById(R.id.register_password_input);
        EditText confirm_password_input = findViewById(R.id.register_confirm_password_input);
        Button confirm_button = findViewById(R.id.register_bn_confirm);
        TextView login_page = findViewById(R.id.register_link);

        // Validate (with listeners)
        email_input.addTextChangedListener(new TextChangedListener(getWindow().getDecorView().getRootView()));
        confirm_password_input.addTextChangedListener(new TextChangedListener(getWindow().getDecorView().getRootView()));
        password_input.addTextChangedListener(new TextChangedListener(getWindow().getDecorView().getRootView()));

        // Button actions
        login_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        confirm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl(MainActivity.BASE_URL)
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

                        IAuth service = retrofit.create(IAuth.class);
                        service.register(new RegisterModel(email_input.getText().toString().toLowerCase(), password_input.getText().toString())).enqueue(new Callback<RegisterResponseWrapper>() {

                            @Override
                            public void onResponse(Call<RegisterResponseWrapper> call, Response<RegisterResponseWrapper> response) {
                                if(response.body() != null){
                                    if(response.body().getResult().playerExists()) {
                                        final String[] fbtoken = {null};

                                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                            @Override
                                            public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<String> task) {
                                                if (!task.isSuccessful()) {
                                                    Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    return;
                                                }

                                                fbtoken[0] = task.getResult();

                                                Retrofit retrofit = new Retrofit.Builder()
                                                        .baseUrl(MainActivity.BASE_URL)
                                                        .addConverterFactory(GsonConverterFactory.create())
                                                        .build();

                                                IAuth service = retrofit.create(IAuth.class);

                                                service.login(new LoginModel(email_input.getText().toString(), password_input.getText().toString(), fbtoken[0])).enqueue(new Callback<LoginResponseWrapper>() {
                                                    @Override
                                                    public void onResponse(Call<LoginResponseWrapper> call, Response<LoginResponseWrapper> response) {
                                                        if(response.body() != null){
                                                            LoginResponse loginResponse = response.body().result;
                                                            SharedPreferences.Editor edit = prefs.edit();
                                                            edit.putString("token", loginResponse.token);
                                                            edit.putInt("playerId", loginResponse.user.id);
                                                            edit.putString("playerEmail", loginResponse.user.email);
                                                            edit.apply();

                                                            startActivity(new Intent(RegisterActivity.this, CurrentGameActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                            finish();
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<LoginResponseWrapper> call, Throwable t) {
                                                        Toast.makeText(RegisterActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        });
                                    } else {
                                        SharedPreferences.Editor edit = prefs.edit();
                                        edit.putString("token", response.body().getResult().getToken());
                                        edit.apply();

                                        startActivity(new Intent(RegisterActivity.this, NewPlayerActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("email", email_input.getText().toString().toLowerCase()).putExtra("pass", password_input.getText().toString()));
                                        finish();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<RegisterResponseWrapper> call, Throwable t) {
                                Toast.makeText(RegisterActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).start();
            }
        });


    }
}