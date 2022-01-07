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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.bress.tournamentplanner.R;
import nl.bress.tournamentplanner.dao.interfaces.IAuth;
import nl.bress.tournamentplanner.dao.interfaces.IGame;
import nl.bress.tournamentplanner.dao.interfaces.IPlayer;
import nl.bress.tournamentplanner.dao.interfaces.ISkillLevel;
import nl.bress.tournamentplanner.domain.LoginModel;
import nl.bress.tournamentplanner.domain.LoginResponse;
import nl.bress.tournamentplanner.domain.LoginResponseWrapper;
import nl.bress.tournamentplanner.domain.NewPlayerModel;
import nl.bress.tournamentplanner.domain.PlayerResponseWrapper;
import nl.bress.tournamentplanner.domain.ScoreModel;
import nl.bress.tournamentplanner.domain.SkillLevel;
import nl.bress.tournamentplanner.domain.SkillLevelResponseWrapper;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewPlayerActivity extends AppCompatActivity {

    private Spinner spinner;
    private SkillLevel[] skillLevels;
    private List<String> skillLevelNames;
    private String email;
    private String pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_player);
        Intent intent = getIntent();
        if(intent.hasExtra("email")){
            email = intent.getStringExtra("email");
        }
        if(intent.hasExtra("pass")) {
            pass = intent.getStringExtra("pass");
        }


        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        skillLevelNames = new ArrayList<>();
        String token = prefs.getString("token", "");
        spinner = findViewById(R.id.newPlayer_spinner);
        Button confirm_btn = findViewById(R.id.newPlayer_bn_confirm);
        EditText name_input = findViewById(R.id.newPlayer_name_input);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Interceptor interceptor = new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("Accept", "application/json")
                                .addHeader("authorization", "Bearer " + token)
                                .build();
                        return chain.proceed(newRequest);
                    }
                };
                OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
                okHttpBuilder.addInterceptor(interceptor);
                OkHttpClient okHttpClient = okHttpBuilder.build();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(MainActivity.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(okHttpClient)
                        .build();

                ISkillLevel service = retrofit.create(ISkillLevel.class);
                service.getAllSkillLevels().enqueue(new Callback<SkillLevelResponseWrapper>() {

                    @Override
                    public void onResponse(Call<SkillLevelResponseWrapper> call, Response<SkillLevelResponseWrapper> response) {
                        if(response.body() != null){
                            for(SkillLevel sl : response.body().getResult()) {
                                skillLevelNames.add(sl.getName());
                            }
                            skillLevels = response.body().getResult();
                            ArrayAdapter aa = new ArrayAdapter(NewPlayerActivity.this, R.layout.support_simple_spinner_dropdown_item, skillLevelNames);
                            spinner.setAdapter(aa);
                        }
                    }

                    @Override
                    public void onFailure(Call<SkillLevelResponseWrapper> call, Throwable t) {
                        Toast.makeText(NewPlayerActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();

        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SkillLevel selectedSkillLevel = skillLevels[spinner.getSelectedItemPosition()];

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Interceptor interceptor = new Interceptor() {
                            @Override
                            public okhttp3.Response intercept(Chain chain) throws IOException {
                                Request newRequest = chain.request().newBuilder()
                                        .addHeader("Accept", "application/json")
                                        .addHeader("authorization", "Bearer " + token)
                                        .build();
                                return chain.proceed(newRequest);
                            }
                        };
                        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
                        okHttpBuilder.addInterceptor(interceptor);
                        OkHttpClient okHttpClient = okHttpBuilder.build();

                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl(MainActivity.BASE_URL)
                                .addConverterFactory(GsonConverterFactory.create())
                                .client(okHttpClient)
                                .build();

                        IPlayer service = retrofit.create(IPlayer.class);
                        service.createPlayer(new NewPlayerModel(name_input.getText().toString(), email, selectedSkillLevel.getId())).enqueue(new Callback<PlayerResponseWrapper>() {

                            @Override
                            public void onResponse(Call<PlayerResponseWrapper> call, Response<PlayerResponseWrapper> response) {
                                if(response.body() != null) {
                                    final String[] fbtoken = {null};

                                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                        @Override
                                        public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<String> task) {
                                            if (!task.isSuccessful()) {
                                                Toast.makeText(NewPlayerActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            fbtoken[0] = task.getResult();

                                            Retrofit retrofit = new Retrofit.Builder()
                                                    .baseUrl(MainActivity.BASE_URL)
                                                    .addConverterFactory(GsonConverterFactory.create())
                                                    .build();

                                            IAuth service = retrofit.create(IAuth.class);

                                            service.login(new LoginModel(email, pass, fbtoken[0])).enqueue(new Callback<LoginResponseWrapper>() {
                                                @Override
                                                public void onResponse(Call<LoginResponseWrapper> call, Response<LoginResponseWrapper> response) {
                                                    if(response.body() != null){
                                                        LoginResponse loginResponse = response.body().result;
                                                        SharedPreferences.Editor edit = prefs.edit();
                                                        edit.putString("token", loginResponse.token);
                                                        edit.putInt("playerId", loginResponse.user.id);
                                                        edit.putString("playerEmail", loginResponse.user.email);
                                                        edit.apply();

                                                        startActivity(new Intent(NewPlayerActivity.this, CurrentGameActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                        finish();
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<LoginResponseWrapper> call, Throwable t) {
                                                    Toast.makeText(NewPlayerActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onFailure(Call<PlayerResponseWrapper> call, Throwable t) {
                                Toast.makeText(NewPlayerActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).start();
            }
        });

        name_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().length() > 0) {
                    confirm_btn.setEnabled(true);
                } else {
                    confirm_btn.setEnabled(false);
                }
            }
        });

    }
}