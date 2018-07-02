package neighbourhoodapp.tnap.com.tnap;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private AutoCompleteTextView mUsernameView;
    private AutoCompleteTextView mGenderView;
    private AutoCompleteTextView mBirthdateView;
    private AutoCompleteTextView mPhoneNumView;
    private AutoCompleteTextView mAddressView;
    private AutoCompleteTextView mCCView;

    private String email;

    // Firebase stuff.
    private FirebaseFirestore mDatabase;
    private Map<String, Object> userDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mUsernameView = (AutoCompleteTextView) findViewById(R.id.update_username);
        mGenderView = (AutoCompleteTextView) findViewById(R.id.update_gender);
        mBirthdateView = (AutoCompleteTextView) findViewById(R.id.update_birthday);
        mPhoneNumView = (AutoCompleteTextView) findViewById(R.id.update_phone);
        mAddressView = (AutoCompleteTextView) findViewById(R.id.update_address);
        mCCView = (AutoCompleteTextView) findViewById(R.id.update_cc);

        mDatabase = FirebaseFirestore.getInstance();
        email = getIntent().getStringExtra("email");
        DocumentReference docRef = mDatabase.collection("users").document(email);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // successfully loaded user info from database
                        userDetails = document.getData();

                        // Populate the page with details
                        mUsernameView.setText((String) userDetails.get("username"));
                        mGenderView.setText((String) userDetails.get("gender"));
                        mBirthdateView.setText((String) userDetails.get("birthdate"));
                        mPhoneNumView.setText((String) userDetails.get("phone"));
                        mAddressView.setText((String) userDetails.get("address"));
                        mCCView.setText((String) userDetails.get("cc"));

                        Log.d("TNAP", "User data retrieved from Firestore");
                    } else {
                        Log.d("TNAP", "No such user");
                    }
                } else {
                    Log.d("TNAP", "Failed to retrieve user with error: ", task.getException());
                }
            }
        });
    }

    // Executed when Save Profile button is pressed.
    public void saveUpdates(View v) {
        attemptSaving();
    }

    private void attemptSaving() {

        String username = mUsernameView.getText().toString();
        String gender = mGenderView.getText().toString();
        String birthdate = mBirthdateView.getText().toString();
        String phone = mPhoneNumView.getText().toString();
        String address = mAddressView.getText().toString();
        String cc = mCCView.getText().toString();

        DocumentReference userRef = mDatabase.collection("users").document(email);

        userRef.update("username", username,
                       "gender", gender,
                       "birthdate", birthdate,
                       "phone", phone,
                       "address", address,
                       "cc", cc)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TNAP", "DocumentSnapshot of user successfully updated!");
                        finish();
                        // TODO: Refresh the ProfileFragment but idk how lel
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TNAP", "Error updating document of user", e);
                    }
                });
    }
}
