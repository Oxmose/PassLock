package io.github.oxmose.passlock;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

    private static final String KEY_NAME = "PassLockFingerPrintKey";
    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private FingerprintHandler fingerprintHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* Init fingerprint technology */
        initFingerPrints();

        /* Set the UI depending on the settings */
        setUI();

        /* Init the components */
        initComponents();
    }

    private void initFingerPrints() {
        TextView fingerPrintInfo = findViewById(R.id.activity_login_use_finger_textview);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Get an instance of KeyguardManager and FingerprintManager//
            keyguardManager =
                    (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            fingerprintManager =
                    (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

            //Check whether the device has a fingerprint sensor//
            if (!fingerprintManager.isHardwareDetected()) {
                fingerPrintInfo.setText(getString(R.string.fingerprint_not_supported));
            }
            //Check whether the user has granted your app the USE_FINGERPRINT permission//
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                fingerPrintInfo.setText(R.string.enable_fingerprint);
            }

            //Check that the user has registered at least one fingerprint//
            if (!fingerprintManager.hasEnrolledFingerprints()) {
                fingerPrintInfo.setText(R.string.register_fingerprints);
            }

            //Check that the lockscreen is secured//
            if (!keyguardManager.isKeyguardSecure()) {
                fingerPrintInfo.setText(R.string.enable_lockscreen);
            } else {
                try {
                    generateKey();
                } catch (FingerprintException e) {
                    e.printStackTrace();
                }

                if (initCipher()) {
                    //If the cipher is initialized successfully, then create a CryptoObject instance//
                    cryptoObject = new FingerprintManager.CryptoObject(cipher);

                    fingerprintHandler = new FingerprintHandler(this);
                    fingerprintHandler.startAuth(fingerprintManager, cryptoObject);
                }
                else {
                    fingerPrintInfo.setText(getResources().getString(R.string.fingerprint_not_supported));
                }
            }
        }
        else {
            fingerPrintInfo.setText(getResources().getString(R.string.fingerprint_not_supported));
        }
    }

    private void generateKey() throws FingerprintException {
        try {
            // Obtain a reference to the Keystore using the standard Android keystore container identifier (“AndroidKeystore”)//
            keyStore = KeyStore.getInstance("AndroidKeyStore");

            //Generate the key//
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            //Initialize an empty KeyStore//
            keyStore.load(null);

            //Initialize the KeyGenerator//
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyGenerator.init(new

                        //Specify the operation(s) this key can be used for//
                        KeyGenParameterSpec.Builder(KEY_NAME,
                        KeyProperties.PURPOSE_ENCRYPT |
                                KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)

                        //Configure this key so that the user has to confirm their identity with a fingerprint each time they want to use it//
                        .setUserAuthenticationRequired(true)
                        .setEncryptionPaddings(
                                KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build());
            }

            //Generate the key//
            keyGenerator.generateKey();

        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException exc) {
            exc.printStackTrace();
            throw new FingerprintException(exc);
        }
    }

    //Create a new method that we’ll use to initialize our cipher//
    public boolean initCipher() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                //Obtain a cipher instance and configure it with the properties required for fingerprint authentication//
                cipher = Cipher.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES + "/"
                                + KeyProperties.BLOCK_MODE_CBC + "/"
                                + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            } catch (NoSuchAlgorithmException |
                    NoSuchPaddingException e) {
                Log.e("Cipher Failed", e.toString());
                return false;
            }

            try {
                keyStore.load(null);
                SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                        null);
                cipher.init(Cipher.ENCRYPT_MODE, key);
                //Return true if the cipher has been initialized successfully//
                return true;
            } catch (KeyStoreException | CertificateException
                    | UnrecoverableKeyException | IOException
                    | NoSuchAlgorithmException | InvalidKeyException e) {
                Log.e("Cipher Failed", e.toString());
                e.printStackTrace();
                return false;
            }
        }
        else {
            return false;
        }
    }

    private class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
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

    public void fingerPrintLogin() {
        /* Get the database singleton */
        DatabaseSingleton db = DatabaseSingleton.getInstance();

        EditText usernameEditText = findViewById(R.id.activity_login_username_edittext);
        EditText passwordEditText = findViewById(R.id.activity_login_password_edittext);

        /* Check the principal user */
        User loggedUser = db.getPincipalUser();
        if(loggedUser != null){
            usernameEditText.setError(null);
            passwordEditText.setError(null);

            /* We logged in */
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            i.putExtra("username", loggedUser.getUsername());
            startActivity(i);

            finish();
        }
        else {
            Toast.makeText(this, R.string.not_any_fingerprint_account, Toast.LENGTH_SHORT).show();
            fingerprintHandler.cancelListening();
            initFingerPrints();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        fingerprintHandler.cancelListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        fingerprintHandler.cancelListening();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
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
