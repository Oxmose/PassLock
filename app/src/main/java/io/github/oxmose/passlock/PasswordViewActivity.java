package io.github.oxmose.passlock;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import io.github.oxmose.passlock.data.Session;
import io.github.oxmose.passlock.database.DatabaseSingleton;
import io.github.oxmose.passlock.database.Password;
import io.github.oxmose.passlock.database.User;
import io.github.oxmose.passlock.model.ListPasswordRowItem;
import io.github.oxmose.passlock.tools.AESEncrypt;

public class PasswordViewActivity extends AppCompatActivity {

    private TextView passwordTitle;

    private ImageView passwordIcon;

    private TextView passwordValue;
    private TextView passwordType;

    private Button editButton;
    private Button deleteButton;

    private User user;
    private Password password;
    private String decryptedPassword = null;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_view);

        /* Get components */
        passwordTitle = findViewById(R.id.activity_password_view_title_textview);
        passwordIcon = findViewById(R.id.activity_password_view_icon_imageview);
        passwordValue = findViewById(R.id.activity_password_view_value_textview);
        passwordType = findViewById(R.id.activity_password_view_type_textview);
        editButton = findViewById(R.id.activity_password_view_edit_button);
        deleteButton = findViewById(R.id.activity_password_view_delete_button);
        toolbar = findViewById(R.id.activity_password_view_toolbar);

        /* Set the UI */
        initUI();
    }

    private void initUI() {
        Intent intent = getIntent();
        int id = intent.getIntExtra("passwordId", -1);
        if(id == -1) {
            Toast.makeText(this, "Cannot load password data", Toast.LENGTH_LONG).show();
            finish();
        }
        /* Get the current session password */
        password = DatabaseSingleton.getInstance().getPasswordById(id);
        if(password == null) {
            Toast.makeText(this, "Cannot load password data", Toast.LENGTH_LONG).show();
            finish();
        }

        user = Session.getInstance().getCurrentUser();

        passwordTitle.setText(password.getName());
        passwordValue.setText("Decrypting");

        id = getResources()
                .getIdentifier("io.github.oxmose.passlock:drawable/ic_lock_outline",
                        null, null);

        if(password.isPassword()) {
            passwordType.setText("Password");
            id = getResources()
                    .getIdentifier("io.github.oxmose.passlock:drawable/ic_lock_outline",
                            null, null);

        }
        else if(password.isPin()) {
            passwordType.setText("Pin");
            getResources()
                    .getIdentifier("io.github.oxmose.passlock:drawable/ic_credit_card",
                            null, null);
        }
        else if(password.isDigicode()) {
            passwordType.setText("Digicode");
            getResources()
                    .getIdentifier("io.github.oxmose.passlock:drawable/ic_dialpad",
                            null, null);
        }
        else {
            passwordType.setText("Password");
            id = getResources()
                    .getIdentifier("io.github.oxmose.passlock:drawable/ic_lock_outline",
                            null, null);
        }

        passwordIcon.setImageResource(id);

        if(decryptedPassword == null)
            new DecryptPasswordAync().execute();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseSingleton.getInstance().deletePassword(password);
                user.setPasswordCount(user.getPasswordCount() - 1);
                finish();
            }
        });
    }

    private class DecryptPasswordAync extends AsyncTask<Void, Void, Void> {
        DecryptPasswordAync() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                decryptedPassword = AESEncrypt.decryptString(password.getValue(), user.getDecryptionKey());
            } catch (InvalidAlgorithmParameterException | InvalidKeyException |
                    NoSuchPaddingException | BadPaddingException |
                    NoSuchAlgorithmException | UnsupportedEncodingException |
                    IllegalBlockSizeException | InvalidKeySpecException e) {
                e.printStackTrace();
                decryptedPassword = "Decryption failed.";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            passwordValue.setText(decryptedPassword);
        }
    }
}
