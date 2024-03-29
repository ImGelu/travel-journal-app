package com.travel.journal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.travel.journal.room.User;
import com.travel.journal.room.UserDao;
import com.travel.journal.room.UserDataBase;

public class RegisterActivity extends AppCompatActivity {

    private EditText userName, userEmail, userPassword, userPasswordVerification;
    private TextInputLayout userEmailLayout;
    private TextInputLayout userPasswordLayout;
    private TextInputLayout userPasswordVerificationLayout;

    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        UserDataBase userDataBase = Room.databaseBuilder(this, UserDataBase.class, GlobalData.USERS_DB_NAME).allowMainThreadQueries().build();
        userDao = userDataBase.getUserDao();

        userName = findViewById(R.id.text_field_name_value);
        userEmail = findViewById(R.id.text_field_email_value);
        userPassword = findViewById(R.id.text_field_password_value);
        userPasswordVerification = findViewById(R.id.text_field_password_verification_value);

        userEmailLayout = findViewById(R.id.text_field_email);
        userPasswordLayout = findViewById(R.id.text_field_password);
        userPasswordVerificationLayout = findViewById(R.id.text_field_password_verification);

        userEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!userEmail.getText().toString().isEmpty()) userEmailLayout.setError(null);
                else userEmailLayout.setError(getString(R.string.error_required));

                if (!TextUtils.isEmpty(s) && !Patterns.EMAIL_ADDRESS.matcher(s).matches())
                    userEmailLayout.setError(getString(R.string.error_email_pattern));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        userPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!userPassword.getText().toString().isEmpty()) {
                    if (!userPasswordVerification.getText().toString().isEmpty() && !userPasswordVerification.getText().toString().equals(userPassword.getText().toString()))
                        userPasswordVerificationLayout.setError(getString(R.string.error_password_verification));
                    else userPasswordVerificationLayout.setError(null);

                    userPasswordLayout.setError(null);
                } else userPasswordLayout.setError(getString(R.string.error_required));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        userPasswordVerification.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!userPasswordVerification.getText().toString().isEmpty())
                    userPasswordVerificationLayout.setError(null);

                if (!userPasswordVerification.getText().toString().equals(userPassword.getText().toString()))
                    userPasswordVerificationLayout.setError(getString(R.string.error_password_verification));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    public void openLoginActivity(View view) {
        Intent intent = new Intent(view.getContext(), LoginActivity.class);
        startActivity(intent);
    }

    public void createAccount(View view) {
        String name = userName.getText().toString().trim();
        String email = userEmail.getText().toString().trim();
        String password = userPassword.getText().toString().trim();
        String passwordVerification = userPasswordVerification.toString().trim();

        if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !passwordVerification.isEmpty()) {
            User existingUser = userDao.getUserByEmail(email);

            if (existingUser != null) {
                Snackbar.make(view, getString(R.string.email_taken), BaseTransientBottomBar.LENGTH_SHORT).show();
            } else {
                if (!userPasswordVerification.getText().toString().equals(userPassword.getText().toString())) {
                    Snackbar.make(view, R.string.error_form_generic, BaseTransientBottomBar.LENGTH_SHORT);
                } else {
                    User newUser = new User(name, email, password);
                    long newUserId = userDao.insert(newUser); //! Since all User objects are initialized with id = 0, unless returned from the DB, we can return the ID with the insert annotation. Is there a better way to do this? (Like returning the whole Object, without having to call the setId setter everytime...)
                    newUser.setId(newUserId);

                    GlobalData.setLoggedInUser(newUser, view.getContext());

                    Intent intent = new Intent(view.getContext(), HomeActivity.class);
                    startActivity(intent);
                }
            }
        } else {
            Snackbar.make(view, R.string.error_unfilled, BaseTransientBottomBar.LENGTH_SHORT).show();
        }
    }
}