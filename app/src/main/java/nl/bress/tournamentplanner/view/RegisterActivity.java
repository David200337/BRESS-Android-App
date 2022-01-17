package nl.bress.tournamentplanner.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

    private SharedPreferences.Editor prefsEditor;
    private IAuth authService;

    // Views
    private EditText emailInput;
    private EditText passwordInput;
    private TextView duplicateError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Utilities
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        prefsEditor = prefs.edit();
        authService = ServiceFactory.createAuthService();

        duplicateError = findViewById(R.id.register_duplicate_error);
        emailInput = findViewById(R.id.register_email_input);
        passwordInput = findViewById(R.id.register_password_input);
        EditText confirmPasswordInput = findViewById(R.id.register_confirm_password_input);
        Button confirmButton = findViewById(R.id.register_bn_confirm);
        TextView loginPage = findViewById(R.id.register_link);

        // Validate (with listeners)
        emailInput.addTextChangedListener(new TextChangedListener(getWindow().getDecorView().getRootView()));
        confirmPasswordInput.addTextChangedListener(new TextChangedListener(getWindow().getDecorView().getRootView()));
        passwordInput.addTextChangedListener(new TextChangedListener(getWindow().getDecorView().getRootView()));

        // Button actions
        loginPage.setOnClickListener(view -> startActivity(new Intent(RegisterActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));

        confirmButton.setOnClickListener(view -> authService.register(new RegisterModel(emailInput.getText().toString().toLowerCase(), passwordInput.getText().toString())).enqueue(new Callback<RegisterResponseWrapper>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponseWrapper> call, @NonNull Response<RegisterResponseWrapper> response) {
                if(response.body() != null){
                    if(response.body().getResult().playerExists()) {
                        login();
                    } else {
                        prefsEditor.putString(MainActivity.PREFS_TOKEN, response.body().getResult().getToken());
                        prefsEditor.apply();

                        startActivity(new Intent(RegisterActivity.this, NewPlayerActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra(INTENT_EMAIL, emailInput.getText().toString().toLowerCase()).putExtra(INTENT_PASS, passwordInput.getText().toString()));
                        finish();
                    }
                }
                if(response.errorBody() != null) {
                    duplicateError.setVisibility(View.VISIBLE);
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

            authService.login(new LoginModel(emailInput.getText().toString(), passwordInput.getText().toString(), fbtoken[0])).enqueue(new Callback<LoginResponseWrapper>() {
                @Override
                public void onResponse(@NonNull Call<LoginResponseWrapper> call, @NonNull Response<LoginResponseWrapper> response) {
                    if(response.body() != null){
                        LoginResponse loginResponse = response.body().getResult();
                        prefsEditor.putString(MainActivity.PREFS_TOKEN, loginResponse.getToken());
                        prefsEditor.putInt(MainActivity.PREFS_PLAYER_ID, loginResponse.getUser().getId());
                        prefsEditor.putString(MainActivity.PREFS_PLAYER_EMAIL, loginResponse.getUser().getEmail());
                        prefsEditor.apply();

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