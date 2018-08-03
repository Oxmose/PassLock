package io.github.oxmose.passlock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* Set the UI depending on the settings */
        setUI();
    }

    private void setUI() {

        /* Get components */
        CircularImageView lastUserIconImageView = (CircularImageView) findViewById(R.id.last_connection_imageview);
        TextView lastUsernameTextView = (TextView)  findViewById(R.id.last_username_textview);

        /* Get the settings singleton */
        Settings settings = Settings.getInstance();

        if(settings.getLastConnectionExists()) {
            /* Todo set the values */
        }
        else {
            /* Hide the useless components */
            lastUserIconImageView.setVisibility(View.INVISIBLE);
            lastUsernameTextView.setVisibility(View.INVISIBLE);
        }
    }
}
