package nl.bress.tournamentplanner.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import nl.bress.tournamentplanner.R;
import nl.bress.tournamentplanner.dao.interfaces.IAuth;
import nl.bress.tournamentplanner.domain.LoginModel;
import nl.bress.tournamentplanner.domain.LoginResponse;
import nl.bress.tournamentplanner.domain.LoginResponseWrapper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);

        if(!prefs.getString("token", "").equals("")){
            startActivity(new Intent(this, CurrentGameActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
        }

        EditText et_email = findViewById(R.id.login_et_email);
        EditText et_password = findViewById(R.id.login_et_password);
        Button bn_confirm = findViewById(R.id.login_bn_confirm);

        bn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl("https://bress-api.azurewebsites.net/api/")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

                        //Creating object for our interface
                        IAuth service = retrofit.create(IAuth.class);

                        service.login(new LoginModel(et_email.getText().toString(), et_password.getText().toString())).enqueue(new Callback<LoginResponseWrapper>() {
                            @Override
                            public void onResponse(Call<LoginResponseWrapper> call, Response<LoginResponseWrapper> response) {
                                LoginResponse loginResponse = response.body().result;
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.putString("token", loginResponse.token);
                                edit.putInt("playerId", loginResponse.id);
                                edit.commit();

                                startActivity(new Intent(MainActivity.this, CurrentGameActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            }

                            @Override
                            public void onFailure(Call<LoginResponseWrapper> call, Throwable t) {
                                System.out.println(t.getMessage());
                            }
                        });
                    }
                }).start();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);

        if(!prefs.getString("token", "").equals("")){
            startActivity(new Intent(this, CurrentGameActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
        }
    }
}