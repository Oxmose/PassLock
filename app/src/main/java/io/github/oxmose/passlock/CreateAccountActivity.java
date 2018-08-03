package io.github.oxmose.passlock;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
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
import java.util.Random;

import io.github.oxmose.passlock.database.DatabaseSingleton;


public class CreateAccountActivity extends Activity {

    private static final int RESULT_LOAD_IMG = 1;
    private static final String AVATAR_FOLDER = "avatars";
    private static final int MIN_PASSWD_LENGTH = 5;
    private static final int MIN_USERNAME_LENGTH = 3;

    private Toast infoToast = null;
    private String avatarFileName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);



        /* Get activity components */
        final EditText usernameEditText = findViewById(R.id.activity_create_account_username_edittext);
        final EditText passwordEditText = findViewById(R.id.activity_create_account_password_edittext);
        final EditText passwordConfEditText =
                            findViewById(R.id.activity_create_account_password_confirm_edittext);

        CheckBox fingerprintsCheckBox =
                         findViewById(R.id.activity_create_account_fingerprints_enable_checkbox);

        Button saveButton = findViewById(R.id.activity_create_account_save_button);
        Button cancelButton = findViewById(R.id.activity_create_account_cancel_button);
        Button addAvatarButton = findViewById(R.id.activity_create_account__add_avatar_button);

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
                    /* Get the settings */
                    Settings settings = Settings.getInstance();

                    /* Check if an account is already registered with fingerprints */
                    if(settings.getFingerprintAccountSet()) {
                        if(infoToast != null) {
                            infoToast.cancel();
                        }
                        infoToast = Toast.makeText(CreateAccountActivity.this,
                                "Cannot unlock this account with fingerprints: an account is already set to be unlocked with fingerprints",
                                Toast.LENGTH_LONG);
                        infoToast.show();
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
            }
        });
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                /* Save the image */

                String newFile = saveImage(selectedImage);

                /* If we have a new image and an other one was already chosen, delete it */
                if(!newFile.isEmpty() && !avatarFileName.isEmpty()) {
                    String root = getFilesDir().toString();
                    File myDir = new File(root + "/" + AVATAR_FOLDER);
                    File file = new File(myDir, avatarFileName);
                    if(file.exists()) {
                        file.delete();
                    }
                }

                avatarFileName = newFile;
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
        myDir.mkdirs();
        Random generator = new Random();
        File file = null;
        String fname = "";
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
}
