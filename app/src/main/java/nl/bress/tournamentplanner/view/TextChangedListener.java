package nl.bress.tournamentplanner.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import nl.bress.tournamentplanner.R;

public class TextChangedListener implements TextWatcher {
    private EditText email_input;
    private EditText password_input;
    private EditText confirm_password_input;
    private TextView email_label;
    private Button confirm_button;

    public TextChangedListener(View v) {
        email_input = v.findViewById(R.id.register_email_input);
        password_input = v.findViewById(R.id.register_password_input);
        confirm_password_input = v.findViewById(R.id.register_confirm_password_input);
        confirm_button =  v.findViewById(R.id.register_bn_confirm);
        email_label = v.findViewById(R.id.register_email_label);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        validateForm();
    }

    public void validateForm() {
        String pass = password_input.getText().toString();
        String confirm_pass = confirm_password_input.getText().toString();
        String email = email_input.getText().toString();

        if(confirm_pass.equals(pass) && pass.matches(".*\\d+.*") && pass.length() > 7 && email.matches("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}")) {
            confirm_button.setEnabled(true);
        } else {
            confirm_button.setEnabled(false);
        }
    }

}
