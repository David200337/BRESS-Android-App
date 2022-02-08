package nl.bress.tournamentplanner.view;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import org.w3c.dom.Text;

import nl.bress.tournamentplanner.R;

public class TextChangedListener implements TextWatcher {
    private final EditText emailInput;
    private final EditText passwordInput;
    private final EditText confirmPasswordInput;
    private final Button confirmButton;
    private TextView emailError;
    private TextView passwordError;
    private TextView confirmPasswordError;

    public TextChangedListener(View v) {
        emailInput = v.findViewById(R.id.register_email_input);
        passwordInput = v.findViewById(R.id.register_password_input);
        confirmPasswordInput = v.findViewById(R.id.register_confirm_password_input);
        confirmButton =  v.findViewById(R.id.register_bn_confirm);
        emailError = v.findViewById(R.id.register_email_error);
        passwordError = v.findViewById(R.id.register_password_error);
        confirmPasswordError = v.findViewById(R.id.register_confirm_password_error);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // Not used
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // Not used
    }

    @Override
    public void afterTextChanged(Editable editable) {
        validateForm();
    }

    public void validateForm() {
        String pass = passwordInput.getText().toString();
        String confirmPass = confirmPasswordInput.getText().toString();
        String email = emailInput.getText().toString();

        boolean valid = true;
        if (confirmPass.equals(pass)) {
            confirmPasswordError.setVisibility(View.INVISIBLE);
        } else {
            valid = false;
            confirmPasswordError.setVisibility(View.VISIBLE);
        }
        if (pass.matches(".*\\d+.*") && pass.length() > 7) {
            passwordError.setVisibility(View.INVISIBLE);
        } else {
            valid = false;
            passwordError.setVisibility(View.VISIBLE);
        }
        if(email.matches("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}")) {
            emailError.setVisibility(View.INVISIBLE);
        } else {
            valid = false;
            emailError.setVisibility(View.VISIBLE);
        }

        if (valid) {
            confirmButton.setEnabled(true);
        } else {
            confirmButton.setEnabled(false);
        }
    }

}
