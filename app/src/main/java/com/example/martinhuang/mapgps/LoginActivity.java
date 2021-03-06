package com.example.martinhuang.mapgps;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends AppCompatActivity {

    //facebook
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private String facebookUserID;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference mDatabase;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private Firebase firebase;
    private String firebaseUserID;
    private String email;


    private TextView textview;
    private ImageView imageView;
    Drawable drawable;
    RelativeLayout relativeLayout;
    ProgressBar progressBar;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_login);
        Firebase.setAndroidContext(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseUser = mAuth.getCurrentUser();
        progressBar = (ProgressBar)findViewById(R.id.progress_spinner);
        progressBar.setVisibility(View.INVISIBLE);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                updateUI(firebaseUser);

            }
        };

        firebase = new Firebase(getString(R.string.firebase_url));
        initUI();

        if(isLoggedIn()) {
            //already logged in, go to Maps
//            progressBar.setVisibility(View.VISIBLE);
//            initFirebaseData();
            Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
            intentExtras(intent);
            startActivity(intent);
            overridePendingTransition(R.anim.enter,R.anim.exit);
            progressBar.setVisibility(View.INVISIBLE);
            finish();
        }


        initFB();

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    private void initFirebaseData() {


            firebaseUserID = firebaseUser.getUid();
            email = firebaseUser.getEmail();
            facebookUserID = Profile.getCurrentProfile().getId();
            mDatabase.child("users").child(firebaseUserID).child("FacebookID").setValue(facebookUserID);
            mDatabase.child("users").child(firebaseUserID).child("Email").setValue(email);


    }


    private void intentExtras(Intent intent) {
        initFirebaseData();
        intent.putExtra(MapsActivity.USER_ID, facebookUserID);
        intent.putExtra(MapsActivity.FIREBASE_ID, firebaseUserID);
        intent.putExtra(MapsActivity.EMAIL, email);
    }

    private void initUI() {
        imageView = (ImageView)findViewById(R.id.banner);
        relativeLayout = (RelativeLayout)findViewById(R.id.activity_login);
        //api 22
        drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.banner,null);
        imageView.setImageDrawable(drawable);
        drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.loginbackground,null);
        relativeLayout.setBackground(drawable);
    }

    private void initFB() {
        loginButton = (LoginButton)findViewById(R.id.login_button);
        textview = (TextView) findViewById(R.id.login_status);
        List<String> permissions = new ArrayList<>();
        permissions.add("email");
        loginButton.setReadPermissions(permissions);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                Log.d(TAG, "LOGGING IN!!!!!");


                textview.setText("Logged in !!! ");
                handleFacebookAccessToken(loginResult.getAccessToken());
//                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
//                initFirebaseData();
                intentExtras(intent);
                overridePendingTransition(R.anim.enter,R.anim.exit);
                progressBar.setVisibility(View.INVISIBLE);
                Log.d(TAG, "LOGGED IN!!!!!");
                startActivity(intent);
                finish();


            }

            @Override
            public void onCancel() {

                textview.setText("Login attempt canceled.");
            }

            @Override
            public void onError(FacebookException error) {
                textview.setText("Login attempt failed.");
            }
        });
    }

    private boolean isLoggedIn(){

        if(firebaseUser == null || firebaseUser.equals(null) || firebaseUser.getEmail().equals(null)
                ) {
            return false;
        }
        // check firebase login
        for (UserInfo user: FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
            if (user.getProviderId().equals("facebook.com")) {
                //check logged in to facebook
                AccessToken accessToken = AccessToken.getCurrentAccessToken();

                return accessToken != null;
            }
        }
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            //Code here for what you want to do after login


        }
    }

    private void FacebookSignOut() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        } else {
            new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                    .Callback() {
                @Override
                public void onCompleted(GraphResponse graphResponse) {

                    LoginManager.getInstance().logOut();

                }
            }).executeAsync();
        }
    }
    private void handleFacebookAccessToken(AccessToken token) {
        // ...
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...

                    }
                });
    }
}
