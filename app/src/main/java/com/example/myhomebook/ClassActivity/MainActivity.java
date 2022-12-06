package com.example.myhomebook.ClassActivity;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private static final int RC_SIGN_IN = 0;
    //UI Object declaration
    AppCompatButton btnLoginMail;
    ImageButton btnLoginGoogle;
    TextInputEditText txtMail, txtPassword;
    private ProgressDialog loadingBar;

    //Google oAuth Object declaration
    SignInButton btSignIn;
    GoogleSignInClient googleSignInClient;
    FirebaseAuth mAuth;

    // TODO Crea la casella per chiedere di rimanere loggati
    //TODO update ui non serve a una sega da vedere come gestirlo
    //TODO separa le classi dei vari login per un maggiore ordine
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //Delete topbar
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

        //UI Association
        btnLoginGoogle = findViewById(R.id.btn_login_with_google);
        btnLoginMail = findViewById(R.id.btn_login_with_mail);
        txtMail = findViewById(R.id.txtLoginEmail);
        txtPassword = findViewById(R.id.txtLoginPassword);

        btnLoginGoogle.setOnClickListener(this);
        btnLoginMail.setOnClickListener(this);

        loadingBar = new ProgressDialog(this);


        //Google SignIn Object
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);

        //Firebase object
        mAuth = FirebaseAuth.getInstance();

    }

    //Update UI
    private void updateUI(GoogleSignInAccount account)
    {
        if (account != null)
        {
            displayToast(account.getEmail());
            googleSignInClient.signOut();

        }

    }


    private void displayToast(String s)
    {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
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

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        }
        catch (ApiException e)
        {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
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
    }

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

    public void btnGoogleClick()
    {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        {
            updateUIEmailPassword(currentUser);
        }

    }

    private void updateUIEmailPassword(FirebaseUser currentUser)
    {
        displayToast(currentUser.getEmail());
        mAuth.signOut();
    }


}


