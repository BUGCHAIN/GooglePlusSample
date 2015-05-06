package app.bugchain.googleplussample;

import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.squareup.picasso.Picasso;

public class MainActivity extends Activity implements
        OnClickListener, ConnectionCallbacks,
        OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;
    private static final int PROFILE_PIC_SIZE = 200;
    // Google client to communicate with Google
    private GoogleApiClient mGoogleApiClient;
    // Logcat tag
    private static final String TAG = "MainActivity";

    private boolean mIntentInProgress;
    private boolean mSignInClicked;

    private ConnectionResult mConnectionResult;
    private SignInButton btnSignIn;
    private Button btnSignOut,btnRevokeAccess;
    private ImageView imageProfile,profile;
    private TextView textUsername, textEmail;
    private LinearLayout profileFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);
        btnSignIn.setOnClickListener(this);

        imageProfile = (ImageView) findViewById(R.id.image);
        textUsername = (TextView) findViewById(R.id.username);
        textEmail = (TextView) findViewById(R.id.email);

        profile = (ImageView)findViewById(R.id.profile);

        profileFrame = (LinearLayout) findViewById(R.id.profileFrame);
        btnSignOut = (Button)findViewById(R.id.btn_sign_out);
        btnRevokeAccess = (Button)findViewById(R.id.btn_revoke_access);

        btnSignOut.setOnClickListener(this);
        btnRevokeAccess.setOnClickListener(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this)
                .addApi(Plus.API,Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /* Method to resolve any signin errors*/
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }

        if (!mIntentInProgress) {
            // store mConnectionResult
            mConnectionResult = result;
            if (mSignInClicked) {
                resolveSignInError();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if(requestCode == RC_SIGN_IN){
            if(responseCode != RESULT_OK){
                mSignInClicked = false;
            }
            mIntentInProgress = false;
            if(!mGoogleApiClient.isConnecting()){
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        mSignInClicked = false;
        Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
        getProfileInformation();
        updateUI(true);
    }

    private void updateUI(boolean isSignIn){
        if(isSignIn){
            btnSignIn.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.VISIBLE);
            btnRevokeAccess.setVisibility(View.VISIBLE);
            profileFrame.setVisibility(View.VISIBLE);
        }   else {
            btnSignIn.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.GONE);
            btnRevokeAccess.setVisibility(View.GONE);
            profileFrame.setVisibility(View.GONE);
        }
    }

    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

                String personName = currentPerson.getDisplayName();
                String personPhotoUrl = currentPerson.getImage().getUrl();


                textUsername.setText(personName);
//                emailLabel.setText(email);


//                Toast.makeText(getApplicationContext(), personName + "\n" + email + "\n" + personPhotoUrl,Toast.LENGTH_SHORT).show();

                personPhotoUrl = personPhotoUrl.substring(0,personPhotoUrl.length()-2) + PROFILE_PIC_SIZE;
                Log.d("Info", personName);

                Log.d("Info", personPhotoUrl);
                Picasso.with(MainActivity.this).load("https://lh5.googleusercontent.com/-3duRD5uUWSI/AAAAAAAAAAI/AAAAAAAAABQ/rG7jr1ZhMyE/photo.jpg?sz=200")
                        .error(R.mipmap.ic_launcher).into(profile);
                Picasso.with(MainActivity.this).load(personPhotoUrl).into(imageProfile);
              //  currentPerson.getId();

                String emailAddress = Plus.AccountApi.getAccountName(mGoogleApiClient);
                Log.d("Info", emailAddress);





            }else{
                Toast.makeText(getApplicationContext(),"Person information is null",Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.d(TAG,e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
        updateUI(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_in:
                    signInWithGooglePlus();
                break;
            case R.id.btn_sign_out:
                    signOutWithGooglePlus();
                break;
            case R.id.btn_revoke_access:
                    revokeGoogleAccess();
                break;
        }
    }

    /* Sign-in into Google*/
    private void signInWithGooglePlus(){
        if(!mGoogleApiClient.isConnecting()){
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    private void signOutWithGooglePlus(){
        if(mGoogleApiClient.isConnected()){
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            updateUI(false);
        }
    }


    private void revokeGoogleAccess(){
        try {
            if (mGoogleApiClient.isConnected()) {
                Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                Log.e(TAG, "User access revoked");
                                mGoogleApiClient.connect();
                                updateUI(false);
                            }
                        });
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // download Google Account profile image, to complete profile
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {

        ImageView downloadedImage;

        public LoadProfileImage(ImageView image) {
            this.downloadedImage = image;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap icon = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                icon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return icon;
        }

        protected void onPostExecute(Bitmap result) {
            downloadedImage.setImageBitmap(result);
        }
    }

}