package io.github.oxmose.passlock;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.Manifest;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    // You should use the CancellationSignal method whenever your app can no longer process user input, for example when your app goes
    // into the background. If you don’t use this method, then other apps will be unable to access the touch sensor, including the lockscreen!//
    private CancellationSignal cancellationSignal;
    private Toast infoToast = null;
    private LoginActivity parent;

    public FingerprintHandler(LoginActivity parent) {
        this.parent = parent;
    }

    public void cancelListening() {
        if(cancellationSignal != null)
            cancellationSignal.cancel();

        cancellationSignal = null;
    }

    //Implement the startAuth method, which is responsible for starting the fingerprint authentication process//
    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {

        cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(parent, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override
    //onAuthenticationError is called when a fatal error has occurred. It provides the error code and error message as its parameters//

    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if(infoToast != null)
            infoToast.cancel();
        /* Avoid cancelation messages */
        if(errMsgId != 5) {
            infoToast = Toast.makeText(parent, "Authentication error (" + errMsgId + "):" + errString, Toast.LENGTH_LONG);
            infoToast.show();
        }
    }

    @Override
    //onAuthenticationFailed is called when the fingerprint doesn’t match with any of the fingerprints registered on the device//
    public void onAuthenticationFailed() {
        if(infoToast != null)
            infoToast.cancel();
        infoToast = Toast.makeText(parent, "Authentication failed", Toast.LENGTH_LONG);
        infoToast.show();
    }

    @Override
    //onAuthenticationHelp is called when a non-fatal error has occurred. This method provides additional information about the error,
    //so to provide the user with as much feedback as possible I’m incorporating this information into my toast//
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        if(infoToast != null)
            infoToast.cancel();
        infoToast = Toast.makeText(parent, "Authentication failed", Toast.LENGTH_LONG);
        infoToast.show();
    }

    @Override
    //onAuthenticationSucceeded is called when a fingerprint has been successfully matched to one of the fingerprints stored on the user’s device//
    public void onAuthenticationSucceeded(
            FingerprintManager.AuthenticationResult result) {
        if(infoToast != null)
            infoToast.cancel();
        parent.fingerPrintLogin();
    }

}