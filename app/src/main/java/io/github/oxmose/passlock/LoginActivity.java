package io.github.oxmose.passlock;

import android.Manifest;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import io.github.oxmose.passlock.database.DatabaseSingleton;
import io.github.oxmose.passlock.database.User;

public class LoginActivity extends AppCompatActivity {

    private FingerPrintAuthHelper fingerPrintAuthHelper;
    private CancellationSignal cancellationSignal;
    private Toast infoToast = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);


        /* TODO REMOVE FOR DEV PURPOSE ONLY */

        //User loggedUser = checkLogin("oxmose", "oxmose");
       // if(loggedUser != null) {
            /* We logged in */
            /*
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            i.putExtra("username", loggedUser.getUsername());
            i.putExtra("decryptionKey", loggedUser.getDecryptionKey());
            startActivity(i);
            */
      //  }

        /* Set the UI depending on the settings */
        setUI();

        /* Init the components */
        initComponents();

        /* Init fingerprint technology */
        fingerPrintAuthHelper = new FingerPrintAuthHelper(this,
                (TextView)findViewById(R.id.activity_login_use_finger_textview));
        initFingerPrints();

    }

    private void initFingerPrints() {
        if(cancellationSignal != null && !cancellationSignal.isCanceled())
            cancellationSignal.cancel();
        cancellationSignal = null;
        cancellationSignal = new CancellationSignal();

        if(fingerPrintAuthHelper.init()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                fingerPrintAuthHelper.getPassword(cancellationSignal, new LoginCallback());
            }
        }
    }

    private void setUI() {

        /* Get components */
        CircularImageView lastUserIconImageView = findViewById(R.id.activity_login_last_connection_imageview);
        TextView lastUsernameTextView = findViewById(R.id.activity_login_last_username_textview);
        EditText usernameEditText = findViewById(R.id.activity_login_username_edittext);

        Switch rememberSwitch = findViewById(R.id.activity_login_remember_me_switch);

        /* Get the settings singleton */
        Settings settings = Settings.getInstance();

        if(settings.getLastConnectionExists()) {

            /* Get components settings */
            String iconPath = settings.getLastConnectionImage();
            String username = settings.getLastConnectionUsername();

            /* If no image is set, display the default one */
            if(iconPath.isEmpty()) {
                int id = getResources()
                        .getIdentifier("io.github.oxmose.passlock:drawable/ic_account_circle",
                                null, null);
                lastUserIconImageView.setImageResource(id);
            }
            else {
                File imgFile = new  File(iconPath);

                if(imgFile.exists()){
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    lastUserIconImageView.setImageBitmap(myBitmap);
                }
                else {
                    int id = getResources()
                            .getIdentifier("io.github.oxmose.passlock:drawable/ic_account_circle",
                                    null, null);
                    lastUserIconImageView.setImageResource(id);
                }
            }
            lastUsernameTextView.setText(username);
            usernameEditText.setText(username);

            rememberSwitch.setChecked(true);

            lastUserIconImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ForgetDialog cdd=new ForgetDialog(LoginActivity.this);
                    cdd.show();
                }
            });

            /* Diaplay the components */
            lastUserIconImageView.setVisibility(View.VISIBLE);
            lastUsernameTextView.setVisibility(View.VISIBLE);
        }
        else {
            /* Hide the useless components */
            lastUserIconImageView.setVisibility(View.INVISIBLE);
            lastUsernameTextView.setVisibility(View.INVISIBLE);
        }


    }

    private void initComponents() {
        /* Get components */
        TextView createAccountTextView = findViewById(R.id.activity_login_create_accountt_textview);
        Button loginButton = findViewById(R.id.activity_login_login_button);
        final Switch rememberSwitch = findViewById(R.id.activity_login_remember_me_switch);
        final EditText usernameEditText = findViewById(R.id.activity_login_username_edittext);
        final EditText passwordEditText = findViewById(R.id.activity_login_password_edittext);

        /* Set components */
        createAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CreateAccountActivity.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User loggedUser = checkLogin(usernameEditText.getText().toString(),
                                             passwordEditText.getText().toString());
                if(loggedUser != null) {
                    usernameEditText.setError(null);
                    passwordEditText.setError(null);

                    /* Set decryption key */
                    loggedUser.setDecryptionKey(passwordEditText.getText().toString());

                    /* Get the settings singleton */
                    Settings settings = Settings.getInstance();

                    /* Save credential in case of "Remember me" */
                    if(rememberSwitch.isChecked()) {
                        settings.setLastConnectionUsername(loggedUser.getUsername());
                        settings.setLastConnectionImage(loggedUser.getAvatar());
                        settings.setLastConnectionExists(true);
                    }
                    else {
                        settings.setLastConnectionUsername("");
                        settings.setLastConnectionImage("");
                        settings.setLastConnectionExists(false);
                    }

                    /* We logged in */
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    i.putExtra("username", loggedUser.getUsername());
                    i.putExtra("decryptionKey", loggedUser.getDecryptionKey());
                    startActivity(i);

                    finish();

                }
                else {
                    usernameEditText.setError(getString(R.string.wrong_usr_psswd));
                    passwordEditText.setError(getString(R.string.wrong_usr_psswd));
                }
            }
        });
    }

    private User checkLogin(String username, String password) {
        /* Get the database singleton */
        DatabaseSingleton db = DatabaseSingleton.getInstance();
        User user = db.getUser(username, Tools.hashPassword(password));

        return user;
    }

    public void forgetUser() {

        CircularImageView lastUserIconImageView = findViewById(R.id.activity_login_last_connection_imageview);
        TextView lastUsernameTextView = findViewById(R.id.activity_login_last_username_textview);
        EditText usernameEditText = findViewById(R.id.activity_login_username_edittext);

        Switch rememberSwitch = findViewById(R.id.activity_login_remember_me_switch);

        /* Get the settings singleton */
        Settings settings = Settings.getInstance();

        settings.setLastConnectionUsername("");
        settings.setLastConnectionImage("");
        settings.setLastConnectionExists(false);

        lastUsernameTextView.setText("");
        usernameEditText.setText("");

        rememberSwitch.setChecked(false);


        /* Diaplay the components */
        lastUserIconImageView.setVisibility(View.INVISIBLE);
        lastUsernameTextView.setVisibility(View.INVISIBLE);

    }

    public class LoginCallback implements FingerPrintAuthHelper.Callback {

        @Override
        public void onSuccess(String savedPass) {
            /* Get the database singleton */
            DatabaseSingleton db = DatabaseSingleton.getInstance();

            EditText usernameEditText = findViewById(R.id.activity_login_username_edittext);
            EditText passwordEditText = findViewById(R.id.activity_login_password_edittext);

            /* Check the principal user */
            User loggedUser = db.getPincipalUser();
            if(loggedUser != null){
                usernameEditText.setError(null);
                passwordEditText.setError(null);

                loggedUser.setDecryptionKey(savedPass);

                /* We logged in */
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                i.putExtra("username", loggedUser.getUsername());
                i.putExtra("decryptionKey", loggedUser.getDecryptionKey());
                startActivity(i);
                finish();
            }
            else {
                if(infoToast != null)
                    infoToast.cancel();

                infoToast = Toast.makeText(LoginActivity.this, getString(R.string.not_any_fingerprint_account),
                               Toast.LENGTH_SHORT);
                infoToast.show();

                /* Cancel fingerprint auth */
                if(cancellationSignal != null)
                    cancellationSignal.cancel();

                initFingerPrints();
            }
        }

        @Override
        public void onFailure(String message) {
            if(infoToast != null)
                infoToast.cancel();

            infoToast = Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT);
            infoToast.show();
        }

        @Override
        public void onHelp(int helpCode, String helpString) {
            if(infoToast != null)
                infoToast.cancel();

            infoToast = Toast.makeText(LoginActivity.this, helpString, Toast.LENGTH_SHORT);
            infoToast.show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        /* Cancel fingerprint auth */
        if(cancellationSignal != null)
            cancellationSignal.cancel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        /* Cancel fingerprint auth */
        if(cancellationSignal != null)
            cancellationSignal.cancel();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        /* Cancel fingerprint auth */
        if(cancellationSignal != null)
            cancellationSignal.cancel();

        /* Init fingerprint technology */
        initFingerPrints();

        /* Set the UI depending on the settings */
        setUI();

        /* Init the components */
        initComponents();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /* Init fingerprint technology */
        initFingerPrints();

    }
}
