package io.github.oxmose.passlock;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import io.github.oxmose.passlock.data.Session;
import io.github.oxmose.passlock.database.DatabaseSingleton;
import io.github.oxmose.passlock.database.Password;
import io.github.oxmose.passlock.database.User;
import io.github.oxmose.passlock.tools.AESEncrypt;

public class PasswordViewActivity extends AppCompatActivity {

    private TextView passwordTitle;

    private ImageView passwordIcon;

    private TextView passwordValue;
    private TextView passwordNote;
    private TextView passwordAccount;

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
        passwordAccount = findViewById(R.id.activity_password_view_account_textview);
        passwordNote = findViewById(R.id.activity_password_view_note_textview);
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
        passwordValue.setText(R.string.decrypting);


        String account = password.getAssociatedAccount();
        String note = password.getNote();

        if(account.isEmpty())
            passwordAccount.setText("None");
        else
            passwordAccount.setText(password.getAssociatedAccount());
        if(note.isEmpty())
            passwordNote.setText("None");
        else
            passwordNote.setText(password.getNote());

        if(password.isPassword()) {
            id = getResources()
                    .getIdentifier("io.github.oxmose.passlock:drawable/ic_lock_outline",
                            null, null);

        }
        else if(password.isPin()) {
            id = getResources()
                    .getIdentifier("io.github.oxmose.passlock:drawable/ic_credit_card",
                            null, null);
        }
        else if(password.isDigicode()) {
            id = getResources()
                    .getIdentifier("io.github.oxmose.passlock:drawable/ic_dialpad",
                            null, null);
        }
        else {
            id = getResources()
                    .getIdentifier("io.github.oxmose.passlock:drawable/ic_lock_outline",
                            null, null);
        }

        passwordIcon.setImageResource(id);

        if(decryptedPassword == null)
            new DecryptPasswordAync(this).execute();

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

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditDialog editDiag = new EditDialog(PasswordViewActivity.this, R.style.dialog_style);

                editDiag.show();
            }
        });
    }

    public Password getCurrentPassword() {
        return password;
    }

    public String getDecryptedPassword() {
        return decryptedPassword;
    }

    public void setCurrentPassword(Password currentPassword) {
        this.password = currentPassword;
    }

    public void updatePasswordView() {
        int id;

        passwordTitle.setText(password.getName());
        passwordValue.setText(R.string.decrypting);

        String account = password.getAssociatedAccount();
        String note = password.getNote();

        if(account.isEmpty())
            passwordAccount.setText(R.string.none);
        else
            passwordAccount.setText(password.getAssociatedAccount());
        if(note.isEmpty())
            passwordNote.setText(R.string.none);
        else
            passwordNote.setText(password.getNote());

        if(password.isPassword()) {
            id = getResources()
                    .getIdentifier("io.github.oxmose.passlock:drawable/ic_lock_outline",
                            null, null);

        }
        else if(password.isPin()) {
            id = getResources()
                    .getIdentifier("io.github.oxmose.passlock:drawable/ic_credit_card",
                            null, null);
        }
        else if(password.isDigicode()) {
            id = getResources()
                    .getIdentifier("io.github.oxmose.passlock:drawable/ic_dialpad",
                            null, null);
        }
        else {
            id = getResources()
                    .getIdentifier("io.github.oxmose.passlock:drawable/ic_lock_outline",
                            null, null);
        }

        passwordIcon.setImageResource(id);

        new DecryptPasswordAync(this).execute();

    }

    private static class DecryptPasswordAync extends AsyncTask<Void, Void, Void> {
        private WeakReference<PasswordViewActivity> activityRef;

        DecryptPasswordAync(PasswordViewActivity activityRef) {
            this.activityRef = new WeakReference<>(activityRef);
        }

        @Override
        protected Void doInBackground(Void... params) {
            PasswordViewActivity activity = activityRef.get();
            try {
                activity.decryptedPassword = AESEncrypt.decryptString(activity.password.getValue(), activity.user.getDecryptionKey());
            } catch (InvalidAlgorithmParameterException | InvalidKeyException |
                    NoSuchPaddingException | BadPaddingException |
                    NoSuchAlgorithmException | UnsupportedEncodingException |
                    IllegalBlockSizeException | InvalidKeySpecException e) {
                e.printStackTrace();
                activity.decryptedPassword = "Decryption failed.";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            PasswordViewActivity activity = activityRef.get();

            String formatedPassword = formatPassword(activity.decryptedPassword);

            activity.passwordValue.setText(Html.fromHtml(formatedPassword));
        }

        private String formatPassword(String decryptedPassword) {
            int lastColor = 0;
            String colors[] = new String[2];
            colors[0] = "#3F51B5";
            colors[1] = "#FF5722";

            String finalStr = "<font color='" + colors[lastColor] + "'>";

            for(int i = 0; i < decryptedPassword.length(); ++i) {
                boolean isDigit =  Character.isDigit(decryptedPassword.charAt(i));
                if(lastColor == 0 && isDigit) {
                    lastColor = 1;
                    finalStr += "</font>" + "<font color='" + colors[lastColor] + "'>";
                }
                else if (lastColor == 1 && !isDigit){
                    lastColor = 0;
                    finalStr += "</font>" + "<font color='" + colors[lastColor] + "'>";
                }

                finalStr += decryptedPassword.charAt(i);
            }

            finalStr += "</font>";
            return finalStr;
        }
    }
}
