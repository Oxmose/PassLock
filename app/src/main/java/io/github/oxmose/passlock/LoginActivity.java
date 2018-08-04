package io.github.oxmose.passlock;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;

import io.github.oxmose.passlock.database.DatabaseSingleton;
import io.github.oxmose.passlock.database.User;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* Set the UI depending on the settings */
        setUI();

        /* Init the components */
        initComponents();
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

            Log.i("LastIcon", iconPath);

            /* If no image is set, display the default one */
            if(iconPath.equals("")) {
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

                    /* Save credential in case of "Remeber me" */
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

                }
                else {
                    usernameEditText.setError("Wrong username or password.");
                    passwordEditText.setError("Wrong username or password.");
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
}
