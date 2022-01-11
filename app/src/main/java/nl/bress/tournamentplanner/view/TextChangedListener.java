package nl.bress.tournamentplanner.view;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import nl.bress.tournamentplanner.R;

public class TextChangedListener implements TextWatcher {
    private final EditText emailInput;
    private final EditText passwordInput;
    private final EditText confirmPasswordInput;
    private final Button confirmButton;

    public TextChangedListener(View v) {
        emailInput = v.findViewById(R.id.register_email_input);
        passwordInput = v.findViewById(R.id.register_password_input);
        confirmPasswordInput = v.findViewById(R.id.register_confirm_password_input);
        confirmButton =  v.findViewById(R.id.register_bn_confirm);
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

        confirmButton.setEnabled(confirmPass.equals(pass) && pass.matches(".*\\d+.*") && pass.length() > 7 && email.matches("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}"));
    }

}
