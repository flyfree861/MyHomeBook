package com.example.myhomebook.ClassActivity;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myhomebook.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private static final int RC_SIGN_IN = 0;
    //UI Object declaration
    AppCompatButton btnLoginMail;
    ImageButton btnLoginGoogle;
    TextInputEditText txtMail, txtPassword;
    TextView linkOpenReg;
    CheckBox chkStaylogged;
    private ProgressDialog loadingBar;

    //Google oAuth Object declaration
    GoogleSignInAccount account;
    GoogleSignInOptions gso;
    GoogleSignInClient googleSignInClient;

    //Firebase
    FirebaseAuth mAuth;
    FirebaseUser currentUSer;


    //public variables
    Boolean remainLogged;
    SharedPreferences SharPrefLoginSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //Delete topbar
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

        //UI Association
        InizializeUI();

        //Eventhandler UI
        btnLoginGoogle.setOnClickListener(this);
        btnLoginMail.setOnClickListener(this);
        linkOpenReg.setOnClickListener(this);
        loadingBar = new ProgressDialog(this);
        chkStaylogged.setOnClickListener(this);

        //Read Shared preferences
        SharPrefLoginSettings = this.getPreferences(Context.MODE_PRIVATE);
        remainLogged = SharPrefLoginSettings.getBoolean("isLoggedStatus", false);

        //set checkbox Stay Logged
        if (remainLogged)
        {
            chkStaylogged.setChecked(true);
        }
        else
        {
            chkStaylogged.setChecked(false);
        }

//region Google authantication declaration
        if (chkStaylogged.isChecked())
        {
            SigninGoogleAccount();
            // Check for existing Google Sign In account, if the user is already signed in
            // the GoogleSignInAccount will be non-null.
            account = GoogleSignIn.getLastSignedInAccount(this);
        }
        //Google SignIn Object


        if (chkStaylogged.isChecked())
        {
            btnGoogleClick();
        }

//endregion

//Firebase object
        mAuth = FirebaseAuth.getInstance();

    }

    private void InizializeUI()
    {
        btnLoginGoogle = findViewById(R.id.btn_login_with_google);
        btnLoginMail = findViewById(R.id.btn_login_with_mail);
        txtMail = findViewById(R.id.txtLoginEmail);
        txtPassword = findViewById(R.id.txtLoginPassword);
        linkOpenReg = findViewById(R.id.linkOpenReg);
        chkStaylogged = findViewById(R.id.chkStayLogged);
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.btn_login_with_google)
        {
            btnGoogleClick();
        }
        else if (view.getId() == R.id.btn_login_with_mail)
        {
            btnLoginClick();
        }
        else if (view.getId() == R.id.linkOpenReg)
        {
            openRegistrationActivity();
        }
        else if (view.getId() == R.id.chkStayLogged)
        {
            if (chkStaylogged.isChecked())
            {
                SharedPreferences.Editor edt = SharPrefLoginSettings.edit();
                edt.putBoolean("isLoggedStatus", true);
                edt.apply();
            }
            else if (!chkStaylogged.isChecked())
            {
                SharedPreferences.Editor edt = SharPrefLoginSettings.edit();
                edt.putBoolean("isLoggedStatus", false);
                edt.apply();
            }
        }

    }


//region Google Authantication

    public void SigninGoogleAccount()
    {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN)
        {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask)
    {
        try
        {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            displayToast("Welcome back \n\r" + account.getDisplayName());
            // Signed in successfully, show authenticated UI.
            //TODO open activity main
        }

        catch (ApiException e)
        {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    public void btnGoogleClick()
    {
        SigninGoogleAccount();
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    //endregion


    //region Firebase Email/password access
    private void btnLoginClick()
    {

        String email = "";
        String password = "";
        //Check if field is null
        if (TextUtils.isEmpty(txtMail.getText().toString()))
        {
            displayToast("Insert your email");
            txtMail.setError("This field can't be empty");
            return;

        }
        if (TextUtils.isEmpty(txtPassword.getText().toString()))
        {
            displayToast("Insert your password");
            txtPassword.setError("This field can't be empty");
            return;

        }
        email = txtMail.getText().toString();
        password = txtPassword.getText().toString();
        loadingBar.setTitle("Check user");
        loadingBar.setMessage("Please wait while we check your credential");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            //Open Main activity
                            Toast.makeText(MainActivity.this, "Logged in succesfully", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                        else
                        {
                            //Open registration activity
                            loadingBar.dismiss();
                            CreateNewAccount();
                        }
                    }
                });

    }

    private void CreateNewAccount()
    {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_warning);
        builder.setMessage("Would you create a new user?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    //Click yes
                    public void onClick(DialogInterface dialog, int id)
                    {
                        Intent intent = new Intent(MainActivity.this, UserRegistrationActivity.class);
                        String email = "";
                        String password = "";
                        intent.putExtra("email", txtMail.getText().toString());
                        intent.putExtra("password", txtPassword.getText().toString());
                        startActivity(intent);
                        //  finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    //Click No
                    public void onClick(DialogInterface dialog, int id)
                    {
                        //  Action for 'NO' Button
                        dialog.cancel();
                    }
                });
        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        builder.setTitle("Would you create a new user?");
        alert.show();

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        {
            if (chkStaylogged.isChecked() == true)
            {
                //TODO Apro l'activity principale
            }

            else
            {
                txtMail.setText(currentUser.getEmail());
                mAuth.signOut();
            }


        }

    }


    //endregion


    private void openRegistrationActivity()
    {
        Intent intent = new Intent(this, UserRegistrationActivity.class);
        startActivity(intent);
    }

    private void displayToast(String s)
    {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }


}


