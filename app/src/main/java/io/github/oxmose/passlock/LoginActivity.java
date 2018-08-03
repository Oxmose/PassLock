package io.github.oxmose.passlock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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
        CircularImageView circularImageView = (CircularImageView) findViewById(R.id.last_connection_imageview);


    }
}
