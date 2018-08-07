package io.github.oxmose.passlock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import io.github.oxmose.passlock.database.DatabaseSingleton;
import io.github.oxmose.passlock.database.User;
import io.github.oxmose.passlock.tools.FingerPrintAuthHelper;
import io.github.oxmose.passlock.tools.Tools;


public class CreateAccountActivity extends Activity {

    private static final int RESULT_LOAD_IMG = 1;
    private static final String AVATAR_FOLDER = "avatars";
    private static final int MIN_PASSWD_LENGTH = 5;
    private static final int MIN_USERNAME_LENGTH = 3;

    private Toast infoToast = null;
    private String avatarFileName = "";

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText passwordConfEditText;

    private CheckBox fingerprintsCheckBox;

    private CancellationSignal cancellationSignal;
    private FingerPrintAuthHelper fingerPrintAuthHelper;

    private User newUser;
    private AlertDialog fingerInfoDialog;

    private void initFingerPrints() {
        if(cancellationSignal != null && !cancellationSignal.isCanceled())
            cancellationSignal.cancel();
        cancellationSignal = null;
        cancellationSignal = new CancellationSignal();

        if(fingerPrintAuthHelper.init()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                fingerPrintAuthHelper.savePassword(passwordEditText.getText().toString(),
                        cancellationSignal,
                        new CreateAccountActivity.FingerPrintCallback());
            }
        }
        else {
            fingerprintsCheckBox.setChecked(false);
            fingerprintsCheckBox.setEnabled(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        /* Cancel fingerprint auth */
        if(cancellationSignal != null)
            cancellationSignal.cancel();
        if(fingerInfoDialog != null)
            fingerInfoDialog.cancel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        /* Cancel fingerprint auth */
        if(cancellationSignal != null)
            cancellationSignal.cancel();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        /* Init fingerprint technology */
        fingerPrintAuthHelper = new FingerPrintAuthHelper(this, null);
        if(!fingerPrintAuthHelper.init()) {
            fingerprintsCheckBox.setChecked(false);
            fingerprintsCheckBox.setEnabled(false);
            fingerprintsCheckBox.setText(R.string.cannot_use_fingerprints);
        }

        /* Get activity components */
        usernameEditText = findViewById(R.id.activity_create_account_username_edittext);
        passwordEditText = findViewById(R.id.activity_create_account_password_edittext);
        passwordConfEditText =
                            findViewById(R.id.activity_create_account_password_confirm_edittext);

        fingerprintsCheckBox =
                         findViewById(R.id.activity_create_account_fingerprints_enable_checkbox);

        Button saveButton = findViewById(R.id.activity_create_account_save_button);
        Button cancelButton = findViewById(R.id.activity_create_account_cancel_button);
        Button addAvatarButton = findViewById(R.id.dialog_forget_user_forget_button);

        /* Set components */
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        fingerprintsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                /* Check status */
                if(b) {
                    /* Get the database singleton */
                    DatabaseSingleton db = DatabaseSingleton.getInstance();

                    /* Check if an account is already registered with fingerprints */
                    if(db.isFingerprintAccountSet()) {
                        if(infoToast != null) {
                            infoToast.cancel();
                        }
                        infoToast = Toast.makeText(CreateAccountActivity.this,
                                "Cannot unlock this account with fingerprints: an account is already set to be unlocked with fingerprints",
                                Toast.LENGTH_LONG);
                        infoToast.show();

                        fingerprintsCheckBox.setChecked(false);
                    }
                    else {
                        if(infoToast != null) {
                            infoToast.cancel();
                        }
                        infoToast = Toast.makeText(CreateAccountActivity.this,
                                               "This account will be unlocked with the use of fingerprints",
                                                Toast.LENGTH_LONG);
                        infoToast.show();
                    }
                }
            }
        });

        addAvatarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* Get the database singleton */
                DatabaseSingleton db = DatabaseSingleton.getInstance();

                /* Check username */
                String usernameText = usernameEditText.getText().toString();
                if(usernameText.length() < MIN_USERNAME_LENGTH) {
                    usernameEditText.setError("Username must be at least " + MIN_USERNAME_LENGTH + " characters long");
                    return;
                }
                if(db.usernameExists(usernameText)) {
                    usernameEditText.setError("Username already used");
                    return;
                }

                /* Check password */
                String passwordText = passwordEditText.getText().toString();
                if(passwordText.length() < MIN_PASSWD_LENGTH) {
                    passwordEditText.setError("Password must be at least " + MIN_PASSWD_LENGTH + " characters long");
                    return;
                }
                else {
                    passwordEditText.setError(null);
                    passwordConfEditText.setError(null);
                }
                if(!passwordText.equals(passwordConfEditText.getText().toString())) {
                    passwordEditText.setError("Password and password confirmation do not match");
                    passwordConfEditText.setError("Password and password confirmation do not match");
                    return;
                }
                else {
                    passwordEditText.setError(null);
                    passwordConfEditText.setError(null);
                }

                /* Everything went well, save the user to the database */
                if(saveUser()) {
                    if(fingerprintsCheckBox.isChecked()) {
                        fingerInfoDialog = new AlertDialog.Builder(CreateAccountActivity.this)
                                .setMessage("Please scan you fingerprints")
                                .create();
                        fingerInfoDialog.setCancelable(false);
                        fingerInfoDialog.show();
                        initFingerPrints();
                    }
                    else {
                        if(infoToast != null) {
                            infoToast.cancel();
                        }
                        infoToast = Toast.makeText(CreateAccountActivity.this,
                                "User saved",
                                Toast.LENGTH_LONG);
                        infoToast.show();

                        finish();
                    }
                }
                else {
                    if(infoToast != null) {
                        infoToast.cancel();
                    }
                    infoToast = Toast.makeText(CreateAccountActivity.this,
                            "Cannot save user, unknown error",
                            Toast.LENGTH_LONG);
                    infoToast.show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                InputStream imageStream = null;
                if (imageUri != null) {
                    imageStream = getContentResolver().openInputStream(imageUri);
                }
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                /* Save the image */
                String newFile = saveImage(selectedImage);
                String root = getFilesDir().toString();

                /* If we have a new image and an other one was already chosen, delete it */
                if(!newFile.isEmpty() && !avatarFileName.isEmpty()) {

                    File myDir = new File(root + "/" + AVATAR_FOLDER);
                    File file = new File(myDir, avatarFileName);
                    if(file.exists()) {
                        boolean delete = file.delete();
                        if(!delete) {
                            Log.v("Avatar Deletion", "Returned false");
                        }
                    }
                }
                avatarFileName = root + "/" + AVATAR_FOLDER + "/" + newFile;

                Toast.makeText(CreateAccountActivity.this, "Avatar saved", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(CreateAccountActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(CreateAccountActivity.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    private String saveImage(Bitmap finalBitmap) {

        String root = getFilesDir().toString();
        File myDir = new File(root + "/" + AVATAR_FOLDER);
        boolean mkdirs = myDir.mkdirs();
        if(!mkdirs) {
            return "";
        }
        Random generator = new Random();
        File file;
        String fname;
        int n = 10000;
        do {
            n = generator.nextInt(n);
            fname = "Image-" + n + ".jpg";
            file = new File(myDir, fname);
        } while(file.exists());
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fname;
    }

    private boolean saveUser() {
        /* Get the database singleton */
        DatabaseSingleton db = DatabaseSingleton.getInstance();

        String usernameText = usernameEditText.getText().toString();
        String passwordText = passwordEditText.getText().toString();

        /* Hash password */
        try {
            passwordText = Tools.hashPassword(passwordText);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return false;
        }
        if(passwordText.isEmpty()) {
            return false;
        }

        newUser = new User(usernameText, passwordText, fingerprintsCheckBox.isChecked(),
                                avatarFileName);

        return db.createUser(newUser);
    }

    public class FingerPrintCallback implements FingerPrintAuthHelper.Callback {

        @Override
        public void onSuccess(String savedPass) {
            fingerInfoDialog.hide();
            fingerInfoDialog.cancel();
            if(infoToast != null) {
                infoToast.cancel();
            }
            infoToast = Toast.makeText(CreateAccountActivity.this,
                    "User saved",
                    Toast.LENGTH_LONG);
            infoToast.show();

            finish();
        }

        @Override
        public void onFailure(String message) {
            fingerInfoDialog.hide();
            if(infoToast != null) {
                infoToast.cancel();
            }
            infoToast = Toast.makeText(CreateAccountActivity.this,
                    "Cannot save user " + message,
                    Toast.LENGTH_LONG);
            infoToast.show();

            /* Get the database singleton */
            DatabaseSingleton db = DatabaseSingleton.getInstance();
            db.deleteUser(newUser);
        }

        @Override
        public void onHelp(int helpCode, String helpString) {
            fingerInfoDialog.hide();
            if(infoToast != null) {
                infoToast.cancel();
            }
            infoToast = Toast.makeText(CreateAccountActivity.this,
                    helpString,
                    Toast.LENGTH_LONG);
            infoToast.show();
        }
    }
}
