package io.github.oxmose.passlock.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import io.github.oxmose.passlock.R;
import io.github.oxmose.passlock.data.Session;
import io.github.oxmose.passlock.database.DatabaseSingleton;
import io.github.oxmose.passlock.database.Password;
import io.github.oxmose.passlock.database.User;
import io.github.oxmose.passlock.tools.AESEncrypt;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddPasswordFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddPasswordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddPasswordFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private Button saveButton;
    private Button clearButton;

    private EditText passwordNameEditText;
    private EditText passwordValueEditText;

    private RadioButton passwordCatRadioButton;
    private RadioButton pinCatRadioButton;
    private RadioButton digicodeCatRadioButton;

    private Toast infoToast = null;

    public AddPasswordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment AddPasswordFragment.
     */
    public static AddPasswordFragment newInstance() {
        AddPasswordFragment fragment = new AddPasswordFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* Get components */
        saveButton = view.findViewById(R.id.fragment_add_password_save_button);
        clearButton = view.findViewById(R.id.fragment_add_password_clear_button);

        passwordNameEditText = view.findViewById(R.id.fragment_add_password_name_edittext);
        passwordValueEditText = view.findViewById(R.id.fragment_add_password_value_edittext);

        passwordCatRadioButton = view.findViewById(R.id.fragment_add_password_password_cat);
        pinCatRadioButton = view.findViewById(R.id.fragment_add_password_pin_cat);
        digicodeCatRadioButton = view.findViewById(R.id.fragment_add_password_digicode_cat);

        /* Init compoments */
        initUI();
    }

    private void initUI() {
        passwordCatRadioButton.setChecked(true);

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passwordCatRadioButton.setChecked(true);
                passwordNameEditText.setText("");
                passwordValueEditText.setText("");

                passwordNameEditText.setError(null);
                passwordValueEditText.setError(null);
            }
        });

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

                /* Check if a password  with this name already exists, the name must be unique! */
                if(db.passwordNameExists(passwordName,
                                         Session.getInstance().getCurrentUser().getUsername())) {
                    passwordNameEditText.setError("A password with this name already exists");
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

                    /* Update user */
                    User user = Session.getInstance().getCurrentUser();
                    user.setPasswordCount(user.getPasswordCount() + 1);

                    /* Update main UI */
                    mListener.updatePasswordCount();
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

        /* Create password and save it */
        Password newPassword = new Password(passwordName, passwordValue,
                                            session.getCurrentUser().getUsername(),
                                            passwordCatRadioButton.isChecked(),
                                            pinCatRadioButton.isChecked(),
                                            digicodeCatRadioButton.isChecked());

        return db.createPassword(newPassword);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_password, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void updatePasswordCount();
    }
}
