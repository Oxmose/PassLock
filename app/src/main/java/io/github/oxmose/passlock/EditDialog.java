package io.github.oxmose.passlock;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
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

public class EditDialog extends Dialog implements
        android.view.View.OnClickListener {

    private Button saveButton;
    private Button clearButton;

    private EditText passwordNameEditText;
    private EditText passwordValueEditText;
    private EditText passwordAccountEditText;
    private EditText passwordNoteEditText;

    private RadioButton passwordCatRadioButton;
    private RadioButton pinCatRadioButton;
    private RadioButton digicodeCatRadioButton;

    private Toast infoToast = null;

    private PasswordViewActivity activity;

    private Password currentPassword;

    public EditDialog(PasswordViewActivity activity) {
        super(activity);
        this.activity = activity;

    }

    public EditDialog(PasswordViewActivity activity, int style) {
        super(activity, style);
        this.activity = activity;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_add_password);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        /* Get components */
        saveButton = findViewById(R.id.fragment_add_password_save_button);
        clearButton = findViewById(R.id.fragment_add_password_clear_button);

        passwordNameEditText = findViewById(R.id.fragment_add_password_name_edittext);
        passwordValueEditText = findViewById(R.id.fragment_add_password_value_edittext);
        passwordAccountEditText = findViewById(R.id.fragment_add_password_account_edittext);
        passwordNoteEditText = findViewById(R.id.fragment_add_password_note_edittext);

        passwordCatRadioButton = findViewById(R.id.fragment_add_password_password_cat);
        pinCatRadioButton = findViewById(R.id.fragment_add_password_pin_cat);
        digicodeCatRadioButton = findViewById(R.id.fragment_add_password_digicode_cat);

        /* Init compoments */
        initUI();
    }

    private void initUI() {

        currentPassword = activity.getCurrentPassword();

        passwordCatRadioButton.setChecked(currentPassword.isPassword());
        digicodeCatRadioButton.setChecked(currentPassword.isDigicode());
        pinCatRadioButton.setChecked(currentPassword.isPin());

        passwordNameEditText.setText(currentPassword.getName());
        passwordValueEditText.setText(activity.getDecryptedPassword());
        passwordAccountEditText.setText(currentPassword.getAssociatedAccount());
        passwordNoteEditText.setText(currentPassword.getNote());

        passwordNameEditText.setError(null);
        passwordValueEditText.setError(null);

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        clearButton.setText(R.string.cancel);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatabaseSingleton db = DatabaseSingleton.getInstance();

                String passwordName = passwordNameEditText.getText().toString();
                String passwordValue = passwordValueEditText.getText().toString();

                if(!digicodeCatRadioButton.isChecked() &&
                        !passwordCatRadioButton.isChecked() &&
                        !pinCatRadioButton.isChecked()) {
                    passwordCatRadioButton.setChecked(true);
                }

                if(passwordName.isEmpty()) {
                    passwordNameEditText.setError("Please set a name for the password");
                    return;
                }

                if(passwordValue.isEmpty()) {
                    passwordValueEditText.setError("Please set a value for the password");
                    return;
                }

                if(!savePassword(passwordName, passwordValue)) {
                    if(infoToast != null)
                        infoToast.cancel();
                    infoToast = Toast.makeText(getContext(),
                            "Could not save password",
                            Toast.LENGTH_LONG);
                    infoToast.show();
                }
                else {
                    if(infoToast != null)
                        infoToast.cancel();
                    infoToast = Toast.makeText(getContext(),
                            "Password successfully saved",
                            Toast.LENGTH_LONG);
                    infoToast.show();

                    exitDialog();
                }
            }
        });
    }

    private boolean savePassword(String passwordName, String passwordValue) {
        DatabaseSingleton db = DatabaseSingleton.getInstance();
        Session session = Session.getInstance();

        /* Encrypt password */
        try {
            passwordValue = AESEncrypt.encryptString(passwordValue,
                    session.getCurrentUser().getDecryptionKey());
        } catch (NoSuchPaddingException | NoSuchAlgorithmException |
                InvalidAlgorithmParameterException | IllegalBlockSizeException |
                UnsupportedEncodingException | BadPaddingException |
                InvalidKeySpecException | InvalidKeyException e) {
            e.printStackTrace();
            return false;
        }

        /* Edit password and save it */
        currentPassword.setName(passwordName);
        currentPassword.setValue(passwordValue);
        currentPassword.setNote(passwordNoteEditText.getText().toString());
        currentPassword.setAssociatedAccount(passwordAccountEditText.getText().toString());
        currentPassword.setPassword(passwordCatRadioButton.isChecked());
        currentPassword.setPin(pinCatRadioButton.isChecked());
        currentPassword.setDigicode(digicodeCatRadioButton.isChecked());

        return db.editPassword(currentPassword);
    }

    @Override
    public void onClick(View view) {

    }
    
    public void exitDialog() {
        activity.setCurrentPassword(currentPassword);
        activity.updatePasswordView();

        dismiss();
    }
}