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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Signup extends AppCompatActivity {

    EditText etName, etEmail, etPassword1, etPassword2, etUsername;
    Button btnsignUp;


    private static final String TAG = "EmailPassword";

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("Users");
    private DatabaseReference mUsernamesRef = mRootRef.child("Usernames");
    private DatabaseReference mLastKnownLocRef;
    private DatabaseReference mLastKnownLatRef;// = mCurrentUserRef.child("Last known location");
    private DatabaseReference mLastKnownLonRef;// = mCurrentUserRef.child("Last known location");

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    // [END declare_auth_listener]
    private ProgressDialog pd;
    private FirebaseUser user;
    private String uid = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        pd = new ProgressDialog(this);
        pd.setMessage("loading");

        etUsername = (EditText)findViewById(R.id.editTextUsername);
        etUsername.setOnClickListener(listener);

        etName = (EditText)findViewById(R.id.editTextName);
        etName.setOnClickListener(listener);

        etPassword1 = (EditText)findViewById(R.id.editTextPassword);
        etPassword1.setOnClickListener(listener);

        etPassword2 = (EditText)findViewById(R.id.editTextPassword2);
        etPassword2.setOnClickListener(listener);

        etEmail = (EditText)findViewById(R.id.editTextEmail);
        etEmail.setOnClickListener(listener);

        btnsignUp = (Button)findViewById(R.id.btnSignup);
        btnsignUp.setOnClickListener(listener);


        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                String password1 = null,email = null,name = null, phone = null,password2;
                password1 = etPassword1.getText().toString();
                password2 = etPassword2.getText().toString();
                email = etEmail.getText().toString();
                name = etName.getText().toString();
                String username = etUsername.getText().toString();

                if (user != null) {
                    uid = user.getUid();
                    // User is signed in
                    mUsernamesRef.child(username).setValue(uid);
                    DatabaseReference mUserRef = mUsersRef.child(uid);
                        mUserRef.child("Username").setValue(username);
                        mUserRef.child("Email").setValue(email);
                        mUserRef.child("Password").setValue(password1);
                        mUserRef.child("Name").setValue(name);
                        mUserRef.child("UserID").setValue(uid);
                        DatabaseReference mLoginInfoRef = mUserRef.child("Logged in");
                            mLoginInfoRef.child("Last time logged in");
                            mLoginInfoRef.child("Logged in").setValue(false);
                        DatabaseReference mRequestsRef = mUserRef.child("Pending requests");
                        DatabaseReference mFriendReqRef = mRequestsRef.child("Friend request");
                        DatabaseReference mLocReqRef = mRequestsRef.child("Location request");
                            DatabaseReference mObserveReqRef = mLocReqRef.child("Observe");
                            DatabaseReference mObservedReqRef = mLocReqRef.child("Obeserved");
                        DatabaseReference mFriendActivityRef = mUserRef.child("Friends locations");
                        String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                        mUserRef.child("Date created").setValue(mydate);
                        DatabaseReference mCurrentUserFriends = mUserRef.child("Friends");
                        mLastKnownLocRef = mUserRef.child("Last known location");
                            mLastKnownLatRef = mLastKnownLocRef.child("Latitude");
                            mLastKnownLonRef = mLastKnownLocRef.child("Longitude");
                        mUserRef.child("Last time loggend").setValue("");
                    Intent intentSignIn = new Intent(getApplicationContext(), SignIn.class);
                    startActivity(intentSignIn);
                    Toast.makeText(getApplicationContext(), "User created",
                            Toast.LENGTH_LONG).show();
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + uid);
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

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        pd.show();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        pd.hide();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = etEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Required.");
            valid = false;
        } else {
            etEmail.setError(null);
        }

        String password = etPassword1.getText().toString();
        if (TextUtils.isEmpty(password)) {
            etPassword1.setError("Required.");
            valid = false;
        } else {
            etPassword1.setError(null);
        }

        return valid;
    }

    boolean checkValiodation(){

        String password1,email,name,password2;
        password1 = etPassword1.getText().toString();
        password2 = etPassword2.getText().toString();
        email = etEmail.getText().toString();

        name = etName.getText().toString();
        boolean valid = true;
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Required.");
            valid = false;
        } else {
            etEmail.setError(null);
        }
        if (TextUtils.isEmpty(password1)) {
            etPassword1.setError("Required.");
            valid = false;
        } else {
            etPassword1.setError(null);
        }
        if (TextUtils.isEmpty(password2)) {
            etPassword2.setError("Required.");
            valid = false;
        } else {
            etPassword2.setError(null);
        }
        if (TextUtils.isEmpty(name)) {
            etName.setError("Required.");
            valid = false;
        } else {
            etName.setError(null);
        }
        return valid;
    }

    View.OnClickListener listener = new View.OnClickListener(){
        public void onClick(View v) {
            String password1,email, password2;
            switch (v.getId()) {
                case R.id.btnSignup:
                    password1 = etPassword1.getText().toString();
                    password2 = etPassword2.getText().toString();
                    email = etEmail.getText().toString();

                    if(checkValiodation() && password1.equals(password2)) {

                        createAccount(email, password1);
                    }
                    break;
            }
        }
    };


}
