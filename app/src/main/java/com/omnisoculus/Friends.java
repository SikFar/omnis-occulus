package com.omnisoculus;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Friends extends AppCompatActivity {
    private ArrayAdapter friendsAddapter;

    private FirebaseAuth mAuth;

    private ProgressDialog pd;

    private FirebaseUser user = null;

    private NoticeDialogListener mListener;

    private String uid;
    private String inputFriendSearch = "";

    private ArrayList<String> friendsArray = new ArrayList<String>();


    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("Users");
    private DatabaseReference mUsernamesRef = mRootRef.child("Usernames");
    private DatabaseReference mUserFriendsRef;
    private DatabaseReference mUserRef;

    private ListView lvFriendsList;

    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lvFriendsList = (ListView)findViewById(R.id.lvFriends);
        friendsAddapter = new ArrayAdapter<String>(this,R.layout.friends,friendsArray);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(onClickListener);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pd = new ProgressDialog(this);
        pd.setMessage("Looking for your friend...");

        Intent intent = getIntent();
        String message = intent.getStringExtra(MapsActivity.EXTRA_MESSAGE);
        mUserRef = mUsersRef.child(message);
        currentUser = new User(message);
        lvFriendsList.setAdapter(friendsAddapter);

        mUserFriendsRef = mUserRef.child("Friends");

        mUserFriendsRef.addChildEventListener(childEventListener);



    }

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    void addFriend(String username){
        Log.i("Userfound","User found");
        Toast.makeText(getApplicationContext(),
                "We found your friend, a friendrequest has been sent", Toast.LENGTH_LONG).show();
        currentUser.addFriend(username);
        friendsArray.add(username);
        friendsAddapter.notifyDataSetChanged();
    }


    public String createAlertDialog(){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Add friend");
        alertDialogBuilder.setMessage("Enter friends username:");
        final String[] toRet = {""};

        final EditText input = new EditText(Friends.this);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(10,50);
        input.setLayoutParams(lp);
        alertDialogBuilder.setView(input);
        alertDialogBuilder.setPositiveButton("Search",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        inputFriendSearch = input.getText().toString();

                        Log.i("toRet",toRet[0]);
                    }
                });

        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        return toRet[0];
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final String retName = createAlertDialog();
            Log.i("Search res",retName);
            //final String username = "Farooq";
            mUsernamesRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if(dataSnapshot.getKey().equals(retName)){
                        Toast.makeText(getApplicationContext(),
                                "We found your friend, a friendrequest has been sent", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    };


    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String newFriend = dataSnapshot.getKey();
            friendsArray.add(newFriend);
            friendsAddapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            String newFriend = dataSnapshot.getKey();
            friendsArray.add(newFriend);
            friendsAddapter.notifyDataSetChanged();
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

}
