package com.swachataapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private EditText Userlogin, Userpassword;
    private Button Login, Reg;

    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 234;
    private static final String TAG = MainActivity.class.getSimpleName();
    SignInButton signInButton;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseReference;


    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar mToolbar = findViewById(R.id.login_layout_Toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.Sign_In);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("515617451710-hfivej0ros8fm7v5n28fcfqt5falptg9.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        signInButton = findViewById(R.id.login_button1);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        Login = findViewById(R.id.login_button);
        Reg = findViewById(R.id.register_button);
        Reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            }
        });


        Userlogin = findViewById(R.id.userlogin);
        Userpassword = findViewById(R.id.userpassword);
        Login = findViewById(R.id.login_button);


        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userlogin = Userlogin.getText().toString();
                String userpassword = Userpassword.getText().toString();

                signIn(userlogin, userpassword);
            }
        });
        SignIn();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {

        }


    }

    private void SignIn() {
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if the requestCode is the Google Sign In code that we defined at starting
        if (requestCode == RC_SIGN_IN) {

            //Getting the GoogleSignIn Task
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                //authenticating with firebase
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loging You In");
        progressDialog.setMessage("Please Wait While we are Verifying");
        progressDialog.setCancelable(false);
        progressDialog.show();
        //getting the auth credential
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        //Now using firebase we are signing in the user here
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            String current_user = mAuth.getCurrentUser().getUid();
                            final String name = mAuth.getCurrentUser().getDisplayName();
                            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(current_user);
                            databaseReference.child("user_profile_name").setValue(name)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                                mainIntent.putExtra("name", name);
                                                mainIntent.putExtra("url", mAuth.getCurrentUser().getPhotoUrl());
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainIntent);
                                                finish();
                                                progressDialog.dismiss();
                                            }
                                        }
                                    });
                            Toast.makeText(getApplicationContext(), "User Signed In", Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();

                        }

                        // ...
                    }
                });
    }


    //this method is called on click
    private void signIn() {
        //getting the google signin intent
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();

        //starting the activity for result
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d("Error", "onConnectionFailed:" + connectionResult);
    }

    private void signIn(String userlogin, String userpass) {

        if (TextUtils.isEmpty(userlogin)) {
            Userlogin.setError("Email Requried");
        }
        if (TextUtils.isEmpty(userpass)) {
            Userpassword.setError("Password Requried");
        } else {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Loging You In");
            progressDialog.setMessage("Please Wait While we are Verifying");
            progressDialog.setCancelable(false);
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(userlogin, userpass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Error ! Please Check Email and " +
                                        "Password Again", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
        }
    }


}

