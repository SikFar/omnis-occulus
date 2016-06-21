package com.omnisoculus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignIn extends AppCompatActivity {

    private Button signIn, signOut;
    private EditText emailsin, passwordsin;
    private TextView signup;


    private static final String TAG = "EmailPassword";

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]
    //private FirebaseAuth mAuth;
    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    // [END declare_auth_listener]

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("Users");
    private FirebaseUser user;
    private String uid = null;
    private String mCustomToken;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Firebase.setAndroidContext(this);


        mAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);
        pd.setMessage("loading");


        signIn = (Button)findViewById(R.id.btnSignin);
        signIn.setOnClickListener(listener);

        signOut = (Button)findViewById(R.id.btnSignout);
        signOut.setOnClickListener(listener);

        emailsin = (EditText)findViewById(R.id.editTextEmail);
        emailsin.setOnClickListener(listener);

        passwordsin = (EditText)findViewById(R.id.editTextPasswordsin);
        passwordsin.setOnClickListener(listener);

        signup = (TextView)findViewById(R.id.textViewSignup);
        signup.setOnClickListener(listener);




        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                //uid = user.getUid();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Toast.makeText(getApplicationContext(), "Logged in as.",
                            Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivity(intent);
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // [START_EXCLUDE]
                //updateUI(user);
                // [END_EXCLUDE]
            }
        };
        // [END auth_state_listener]
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String password, email; //For simplicity we will use username instead of email for the time being.
            switch (v.getId()){
                case R.id.textViewSignup:
                    //Send the user to registerform
                    Intent intent = new Intent(getApplicationContext(), Signup.class);
                    startActivity(intent);
                    break;
                case R.id.btnSignin:
                    //Check wether the user exist int he database or not. Handle logic accordingly.
                    Log.i("password:","Kommer hit");
                    email = emailsin.getText().toString();
                    password = passwordsin.getText().toString();
                    signIn(email, password);
                    break;
                case R.id.btnSignout:
                    mAuth.signOut();
                    break;
            }
        }
    };

    private boolean validateForm() {
        boolean valid = true;

        String email = emailsin.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailsin.setError("Required.");
            valid = false;
        } else {
            emailsin.setError(null);
        }

        String password = passwordsin.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordsin.setError("Required.");
            valid = false;
        } else {
            passwordsin.setError(null);
        }

        return valid;
    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    // [END on_stop_remove_listener]

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        pd.show();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        //If sign in fails, display a message to the user. If sign in succeeds
                        //the auth state listener will be notified and logic to handle the
                        //signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail", task.getException());
                            Toast.makeText(SignIn.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        pd.hide();

                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }


}