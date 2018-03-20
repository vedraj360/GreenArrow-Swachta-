package com.swachataapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private Button logbutton;
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 234;
    private static final String TAG = MainActivity.class.getSimpleName();
    SignInButton signInButton;
    private int check = 0;

    private EditText password, passwordc;
    private AutoCompleteTextView Username, Email;
    private Button Register;
    private CoordinatorLayout coord;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("515617451710-hfivej0ros8fm7v5n28fcfqt5falptg9.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        signInButton = findViewById(R.id.logGoogle);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        Username = findViewById(R.id.username);
        Email = findViewById(R.id.email);

        password = findViewById(R.id.password);
        passwordc = findViewById(R.id.passwordc);
        Register = findViewById(R.id.register_button);

        Register();
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
                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        //getting the auth credential
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        //Now using firebase we are signing in the user here
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();

                            Toast.makeText(RegisterActivity.this, "User Signed In", Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

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


    private void Register() {
        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog = new ProgressDialog(RegisterActivity.this);

                //  progressDialog.show();
                hideKeyboard();

                final String name = Username.getText().toString();
                String email = Email.getText().toString();
                String pass = password.getText().toString();
                String passc = passwordc.getText().toString();


                AccountReg(name, email, pass, passc);
                if (!Objects.equals(passc, pass)) {
                    setSnackbar();
                }
                //  Registring(email,pass);
            }
        });


    }

    private void AccountReg(String name, String email, String pass, String passc) {

        Email.setError(null);
        Username.setError(null);
        password.setError(null);
        passwordc.setError(null);


        if (TextUtils.isEmpty(name)) {

            Username.setError("User Name Not Written");

        } else if (!isValidUser(name)) {
            Username.setError("Username is too short");
        }

        if (TextUtils.isEmpty(email)) {
            Email.setError("Email not Written");
        } else if (!isValidEmail(email)) {
            Email.setError("Email Not Valid");
        }


        if (TextUtils.isEmpty(pass)) {
            password.setError(getString(R.string.Password_Requiored));
        } else if (!isValidPassword(pass)) {
            password.setError(getString(R.string.error_invalid_password));
        }

        if (TextUtils.isEmpty(passc) && !isValidPassword(passc)) {
            passwordc.setError(getString(R.string.Password_Requiored));
        } else if (!isValidPassword(passc)) {
            passwordc.setError(getString(R.string.error_invalid_password));
        } else {
            progressDialog.setTitle("Registering");
            progressDialog.setMessage("It may take few Seconds");
            progressDialog.setCancelable(false);
            progressDialog.show();
            Registring(email, pass, name);
        }
    }


    private void setSnackbar() {
        Snackbar snackbar = Snackbar.make(coord, "Please Check The Password", Snackbar.LENGTH_LONG);
        //v.setBackgroundResource(R.color.Background);
        View V = snackbar.getView();
        TextView snackview = (TextView) V.findViewById(android.support.design.R.id.snackbar_text);
        snackview.setTextColor(Color.BLACK);
        snackview.setPadding(320, 0, 0, 0);
        //V.setBackgroundResource();
        snackbar.show();
    }

    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();

    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8;
    }

    private boolean isValidUser(String name) {
        return name.length() >= 5;
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

    }


    private void Registring(String email, final String pass, final String name) {
        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            String current_user = mAuth.getCurrentUser().getUid();
                            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(current_user);
                            databaseReference.child("user_profile_name").setValue(name)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainIntent);
                                                finish();
                                                progressDialog.dismiss();
                                            }
                                        }
                                    });

                        } else {

                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Error ! Please Try Again", Toast.LENGTH_LONG).show();
                        }


                    }
                });
    }
}
