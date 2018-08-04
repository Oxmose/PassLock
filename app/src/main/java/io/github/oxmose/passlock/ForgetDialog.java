package io.github.oxmose.passlock;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class ForgetDialog extends Dialog implements View.OnClickListener {

    public Dialog d;
    public Button cancel, forget;
    public LoginActivity a;

    public ForgetDialog(LoginActivity a) {
        super(a);
        this.a = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_forget_user);
        cancel = (Button) findViewById(R.id.dialog_forget_user_cancel_button);
        forget = (Button) findViewById(R.id.dialog_forget_user_forget_button);
        cancel.setOnClickListener(this);
        forget.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_forget_user_cancel_button:
                dismiss();
                break;
            case R.id.dialog_forget_user_forget_button:
                a.forgetUser();
                Toast.makeText(a, "User forgotten", Toast.LENGTH_SHORT).show();

                break;
            default:
                break;
        }
        dismiss();
    }
}