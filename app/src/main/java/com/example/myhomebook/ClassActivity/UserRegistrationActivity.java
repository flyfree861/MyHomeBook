package com.example.myhomebook.ClassActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.myhomebook.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class UserRegistrationActivity extends AppCompatActivity implements View.OnClickListener
{

    //UI Object
    TextInputEditText txtMail, txtPassword, txtConfPassword;
    AppCompatButton btnSignin;
    ProgressBar progressBar;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_user_registration);

        //Inizialize UI
        initializeUI();

        //Get Extra to speedup registration
        Intent intent= getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle!=null)
        {
            String _mail =(String) bundle.get("email");
            String _password =(String) bundle.get("password");
            txtMail.setText(_mail);
            txtPassword.setText(_password);
        }

        //Button Pressed
        btnSignin.setOnClickListener(this);

        //Firebase auth
        mAuth = FirebaseAuth.getInstance();
    }

    private void initializeUI()
    {
        txtMail = findViewById(R.id.txtRegEmail);
        txtPassword = findViewById(R.id.txtregPassword);
        txtConfPassword = findViewById(R.id.txtRegConfPassword);
        btnSignin = findViewById(R.id.btn_Registration);
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.btn_Registration)
        {
            SignInMethod();
        }

    }

    private void SignInMethod()
    {

        String email, password;
        //check if email is empty
        if (TextUtils.isEmpty(txtMail.getText()))
        {
            displayToast("This field cannot be empty");
            txtMail.setError("field empty");
            return;
        }
        //check if password is empty
        if (TextUtils.isEmpty(txtPassword.getText()))
        {
            displayToast("This field cannot be empty");
            txtPassword.setError("field empty");
            return;
        }
        //check if conf password is empty
        if (TextUtils.isEmpty(txtConfPassword.getText()))
        {
            displayToast("This field cannot be empty");
            txtConfPassword.setError("field empty");
            return;
        }
        //check if password and conf password are equal
        if (!TextUtils.equals(txtConfPassword.getText(), txtPassword.getText()))
        {
            displayToast("Passwords do not match");
            txtPassword.setError("field do not match");
            txtConfPassword.setError("field do not match");
            return;
        }

        email = txtMail.getText().toString();
        password = txtPassword.getText().toString();
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            displayToast("Registration successful!");
                            progressBar.setVisibility(View.GONE);

                            Intent intent = new Intent(UserRegistrationActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Registration failed! Please try again later", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void displayToast(String s)
    {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
}
