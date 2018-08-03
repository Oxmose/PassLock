package io.github.oxmose.passlock;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;

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
        CircularImageView lastUserIconImageView = findViewById(R.id.last_connection_imageview);
        TextView lastUsernameTextView = findViewById(R.id.last_username_textview);

        /* Get the settings singleton */
        Settings settings = Settings.getInstance();

        if(settings.getLastConnectionExists()) {

            /* Get components settings */
            String iconPath = settings.getLastConnectionImage();
            String username = settings.getLastConnectionUsername();

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
}
