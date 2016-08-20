package com.omnisoculus;

import android.util.Log;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

/**
 * Created by sikander on 27.06.16.
 */
public class User {
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("Users");
    private DatabaseReference mUserRef;

    private DatabaseReference mUsernameRef;
    private DatabaseReference mUserFriendsRef;
    private DatabaseReference mUsernamesRef = mRootRef.child("Usernames");
    private DatabaseReference mLoginInfoRef;

    private DataSnapshot userinfo;

    public DataSnapshot userInfo;
    public String name, username, uId,email;

    /*public String getFriend(String username){

    }*/

    User(String uid){
        mUserRef = mUsersRef.child(uid);
        mLoginInfoRef = mUserRef.child("Logged in");
        mUserRef.addValueEventListener(valueListener);
        uId = uid;
        mUserFriendsRef = mUserRef.child("Friends");
    }

    public void setLoggedInStatus(boolean t){
        mLoginInfoRef.child("Logged in").setValue(t);
    }


    public String fetchUId(String username){
        final String[] ret = {""};
        DatabaseReference mUsernameRef = mUsernamesRef.child(username);
        mUsernameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String tmp = dataSnapshot.getValue(String.class);
                ret[0] = tmp;

                Log.i("tmp",ret[0]);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return ret[0];
    }

    public String fetchUsername(final String username){
        final String[] ret = {""};
        //mUsernameRef = mUsersRef.child(uid);
        DatabaseReference mUsernameRef = mUsernamesRef.child(username);
        mUsernameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String tmp = dataSnapshot.getValue(String.class);
                //Log.i("tmp2",tmp);
                if(!tmp.equals("")){
                    ret[0] = username;
                }
                else ret[0] = "";
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Log.i("tmp","*" + ret[0]);
        return ret[0];
    }


    public void addFriend(final String username){
        Log.i("UID to friend",fetchUId(username));

        final DatabaseReference mUsernameRef = mUsernamesRef.child(username);
        mUsernameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String tmp = dataSnapshot.getValue(String.class);
                mUserFriendsRef.child(tmp).setValue(username);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public String fetchName(String uid){
        final String[] ret = {""};
        mUserRef = mUsersRef.child(uid);
        DatabaseReference mUsernameRef = mUserRef.child("Name");
        mUsernameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String tmp = dataSnapshot.getValue(String.class);
                ret[0] = tmp;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return ret[0];
    }

    public String fetchEmail(String uid){
        final String[] ret = {""};
        mUserRef = mUsersRef.child(uid);
        DatabaseReference mUsernameRef = mUserRef.child("Email");
        mUsernameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String tmp = dataSnapshot.getValue(String.class);
                ret[0] = tmp;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return ret[0];
    }

    ValueEventListener valueListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            switch(dataSnapshot.getKey()){
                case "Username":
                    username = dataSnapshot.getValue(String.class);
                    break;
                case "Name":
                    name = dataSnapshot.getValue(String.class);
                    break;
                case "E-mail":
                    email = dataSnapshot.getValue(String.class);
                    break;

            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


}
