package nl.bress.tournamentplanner.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import nl.bress.tournamentplanner.R;
import nl.bress.tournamentplanner.data.factory.ServiceFactory;
import nl.bress.tournamentplanner.data.services.IAuth;
import nl.bress.tournamentplanner.data.models.LoginModel;
import nl.bress.tournamentplanner.data.models.LoginResponse;
import nl.bress.tournamentplanner.data.models.LoginResponseWrapper;
import nl.bress.tournamentplanner.data.models.RegisterModel;
import nl.bress.tournamentplanner.data.models.RegisterResponseWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    // Constants
    public static final String TAG = RegisterActivity.class.getSimpleName();
    public static final String INTENT_EMAIL = "email";
    public static final String INTENT_PASS = "pass";

    private SharedPreferences.Editor prefs_editor;
    private IAuth authService;

    // Views
    private EditText email_input;
    private EditText password_input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Utilities
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        prefs_editor = prefs.edit();
        authService = ServiceFactory.createAuthService();

        email_input = findViewById(R.id.register_email_input);
        password_input = findViewById(R.id.register_password_input);
        EditText confirm_password_input = findViewById(R.id.register_confirm_password_input);
        Button confirm_button = findViewById(R.id.register_bn_confirm);
        TextView login_page = findViewById(R.id.register_link);

        // Validate (with listeners)
        email_input.addTextChangedListener(new TextChangedListener(getWindow().getDecorView().getRootView()));
        confirm_password_input.addTextChangedListener(new TextChangedListener(getWindow().getDecorView().getRootView()));
        password_input.addTextChangedListener(new TextChangedListener(getWindow().getDecorView().getRootView()));

        // Button actions
        login_page.setOnClickListener(view -> startActivity(new Intent(RegisterActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));

        confirm_button.setOnClickListener(view -> authService.register(new RegisterModel(email_input.getText().toString().toLowerCase(), password_input.getText().toString())).enqueue(new Callback<RegisterResponseWrapper>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponseWrapper> call, @NonNull Response<RegisterResponseWrapper> response) {
                if(response.body() != null){
                    if(response.body().getResult().playerExists()) {
                        login();
                    } else {
                        prefs_editor.putString(MainActivity.PREFS_TOKEN, response.body().getResult().getToken());
                        prefs_editor.apply();

                        startActivity(new Intent(RegisterActivity.this, NewPlayerActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra(INTENT_EMAIL, email_input.getText().toString().toLowerCase()).putExtra(INTENT_PASS, password_input.getText().toString()));
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegisterResponseWrapper> call, @NonNull Throwable t) {
                Toast.makeText(RegisterActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        }));
    }

    private void login () {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            final String[] fbtoken = {null};
            fbtoken[0] = task.getResult();

            authService.login(new LoginModel(email_input.getText().toString(), password_input.getText().toString(), fbtoken[0])).enqueue(new Callback<LoginResponseWrapper>() {
                @Override
                public void onResponse(@NonNull Call<LoginResponseWrapper> call, @NonNull Response<LoginResponseWrapper> response) {
                    if(response.body() != null){
                        LoginResponse loginResponse = response.body().result;
                        prefs_editor.putString(MainActivity.PREFS_TOKEN, loginResponse.token);
                        prefs_editor.putInt(MainActivity.PREFS_PLAYER_ID, loginResponse.user.id);
                        prefs_editor.putString(MainActivity.PREFS_PLAYER_EMAIL, loginResponse.user.email);
                        prefs_editor.apply();

                        startActivity(new Intent(RegisterActivity.this, CurrentGameActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        finish();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<LoginResponseWrapper> call, @NonNull Throwable t) {
                    Log.d(TAG, "" + t.getMessage());
                }
            });
        });
    }
}