package nl.bress.tournamentplanner.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import nl.bress.tournamentplanner.R;
import nl.bress.tournamentplanner.data.factory.ServiceFactory;
import nl.bress.tournamentplanner.data.retrofit.RetrofitClient;
import nl.bress.tournamentplanner.data.services.IAuth;
import nl.bress.tournamentplanner.data.models.LoginModel;
import nl.bress.tournamentplanner.data.models.LoginResponse;
import nl.bress.tournamentplanner.data.models.LoginResponseWrapper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    // Constants
    public static final String BASE_URL = "https://serverbuijsen.nl/api/";
    public static final String PREFS_NAME = "myPrefs";
    public static final String PREFS_TOKEN = "token";
    public static final String PREFS_PLAYER_ID = "playerId";
    public static final String PREFS_PLAYER_EMAIL = "playerEmail";

    // Utilities
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefs_editor;
    private IAuth authService;

    // Views
    private EditText et_email;
    private EditText et_password;
    private Button bn_confirm;
    private TextView bn_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs_editor = prefs.edit();
        authService = ServiceFactory.createAuthService();

        // Redirect to CurrentgameActivity if user is logged in
        if(!prefs.getString(PREFS_TOKEN, "").equals("")){
            startActivity(new Intent(this, CurrentGameActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
        }

        et_email = findViewById(R.id.login_et_email);
        et_password = findViewById(R.id.login_et_password);
        bn_confirm = findViewById(R.id.login_bn_confirm);
        bn_register = findViewById(R.id.register_link);

        bn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        bn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    public void login() {
        final String[] fbtoken = {null};

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                fbtoken[0] = task.getResult();

                authService.login(new LoginModel(et_email.getText().toString(), et_password.getText().toString(), fbtoken[0])).enqueue(new Callback<LoginResponseWrapper>() {
                    @Override
                    public void onResponse(Call<LoginResponseWrapper> call, Response<LoginResponseWrapper> response) {
                        if(response.body() != null){
                            LoginResponse loginResponse = response.body().result;
                            prefs_editor.putString(MainActivity.PREFS_TOKEN, loginResponse.token);
                            prefs_editor.putInt(MainActivity.PREFS_PLAYER_ID, loginResponse.user.id);
                            prefs_editor.putString(MainActivity.PREFS_PLAYER_EMAIL, loginResponse.user.email);
                            prefs_editor.apply();

                            startActivity(new Intent(MainActivity.this, CurrentGameActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponseWrapper> call, Throwable t) {
                        Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!prefs.getString(MainActivity.PREFS_TOKEN, "").equals("")){
            startActivity(new Intent(this, CurrentGameActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
        }
    }
}