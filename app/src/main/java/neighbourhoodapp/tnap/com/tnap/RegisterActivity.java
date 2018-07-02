package neighbourhoodapp.tnap.com.tnap;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements OnItemSelectedListener{

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;

    private AutoCompleteTextView mUsernameView;
    private AutoCompleteTextView mNRICView;
    private AutoCompleteTextView mGenderView;
    private AutoCompleteTextView mBirthdateView;
    private AutoCompleteTextView mPhoneNumView;
    private AutoCompleteTextView mAddressView;
    private AutoCompleteTextView mCCView;

    // Firebase instance variables
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.register_email);
        mPasswordView = (EditText) findViewById(R.id.register_password);
        mConfirmPasswordView = (EditText) findViewById(R.id.register_confirm_password);

        mUsernameView = (AutoCompleteTextView) findViewById(R.id.register_username);
        mNRICView = (AutoCompleteTextView) findViewById(R.id.register_nric);
        mGenderView = (AutoCompleteTextView) findViewById(R.id.register_gender);
        mBirthdateView = (AutoCompleteTextView) findViewById(R.id.register_birthday);
        mPhoneNumView = (AutoCompleteTextView) findViewById(R.id.register_phone);
        mAddressView = (AutoCompleteTextView) findViewById(R.id.register_address);
        mCCView = (AutoCompleteTextView) findViewById(R.id.register_cc);

        // Keyboard sign in action
        mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.integer.register_form_finished || id == EditorInfo.IME_NULL) {
                    attemptRegistration();
                    return true;
                }
                return false;
            }
        });

        // Get hold of an instance of FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        // Initiate drop-down menus
        // Gender drop-down spinner
        Spinner gender = findViewById(R.id.gender_dropdown);
        ArrayAdapter<CharSequence> gender_adapter = ArrayAdapter.createFromResource(this, R.array.gender_array, R.layout.spinner_item);
        gender_adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        gender.setAdapter(gender_adapter);
        gender.setOnItemSelectedListener(this);

        // Initiate drop-down menus
        // Community Centre drop-down
        Spinner communitycentre = findViewById(R.id.cc_dropdown);
        ArrayAdapter<CharSequence> communitycentre_adapter = ArrayAdapter.createFromResource(this, R.array.cc_array, R.layout.spinner_item);
        communitycentre_adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        communitycentre.setAdapter(communitycentre_adapter);
        communitycentre.setOnItemSelectedListener(this);

    }

    // Executed when Sign Up button is pressed.
    public void signUp(View v) {
        attemptRegistration();
    }

    private void attemptRegistration() {

        // Reset errors displayed in the form.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            createFirebaseUser();
        }
    }

    private boolean isEmailValid(String email) {
        // You can add more checking logic here.
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        // TODO: Add own logic to check for a valid password (minimum 6 characters) + validity checks for NRIC etc.
        String confirmPassword = mConfirmPasswordView.getText().toString();
        return confirmPassword.equals(password) && password.length() > 4;
    }

    private void createFirebaseUser() {

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d("TNAP", "createUser onComplete: " + task.isSuccessful());

                if (!task.isSuccessful()) {
                    Log.d("TNAP", "user creation failed");
                    showErrorDialog("Registration attempt failed");
                } else {
                    addUserToDatabase(); // only add to database if account successfully created
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    finish();
                    startActivity(intent);
                }
            }
        });
    }

    private void addUserToDatabase() {
        String email = mEmailView.getText().toString();
        String username = mUsernameView.getText().toString();
        String nric = mNRICView.getText().toString();
        String gender = mGenderView.getText().toString();
        String birthdate = mBirthdateView.getText().toString();
        String phone = mPhoneNumView.getText().toString();
        String address = mAddressView.getText().toString();
        String cc = mCCView.getText().toString();

        Map<String, Object> newUser = new HashMap<>();
        newUser.put("email", email);
        newUser.put("username", username);
        newUser.put("nric", nric);
        newUser.put("gender", gender);
        newUser.put("birthdate", birthdate);
        newUser.put("phone", phone);
        newUser.put("address", address);
        newUser.put("cc", cc);
        newUser.put("admin", 0);

        mDatabase.collection("users").document(email).set(newUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TNAP", "DocumentSnapshot of new user successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TNAP", "Error writing document of new user", e);
                    }
                });
    }


    // TODO: Create an alert dialog to show in case registration failed
    private void showErrorDialog (String message) {

        new AlertDialog.Builder(this)
                .setTitle("Oops")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        //Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
