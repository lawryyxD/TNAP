package neighbourhoodapp.tnap.com.tnap;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditCCInfoActivity extends AppCompatActivity {

    private AutoCompleteTextView mAddressView;
    private AutoCompleteTextView mPhoneView;
    private AutoCompleteTextView mHoursView;
    private MultiAutoCompleteTextView mWriteupView;

    private String cc;

    // Firebase stuff.
    private FirebaseFirestore mDatabase;
    private Map<String, Object> ccDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cc_info);

        mAddressView = (AutoCompleteTextView) findViewById(R.id.update_cc_address);
        mPhoneView = (AutoCompleteTextView) findViewById(R.id.update_cc_phone);
        mHoursView = (AutoCompleteTextView) findViewById(R.id.update_cc_hours);
        mWriteupView = (MultiAutoCompleteTextView) findViewById(R.id.update_cc_writeup);

        mDatabase = FirebaseFirestore.getInstance();
        cc = getIntent().getStringExtra("cc");
        DocumentReference docRef = mDatabase.collection("ccs").document(cc);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // successfully loaded user info from database
                        ccDetails = document.getData();

                        // Populate the page with details
                        mAddressView.setText((String) ccDetails.get("address"));
                        mPhoneView.setText((String) ccDetails.get("phone"));
                        mHoursView.setText((String) ccDetails.get("hours"));
                        mWriteupView.setText((String) ccDetails.get("writeup"));

                        Log.d("TNAP", "CC data retrieved from Firestore");
                    } else {
                        mDatabase.collection("ccs").document(cc).set(new HashMap<String, Object>());
                        Log.d("TNAP", "No such CC, creating a new doc");
                    }
                } else {
                    Log.d("TNAP", "Failed to retrieve CC info with error: ", task.getException());
                }
            }
        });
    }

    // Executed when Save Profile button is pressed.
    public void saveUpdates(View v) {
        attemptSaving();
    }

    private void attemptSaving() {

        String address = mAddressView.getText().toString();
        String phone = mPhoneView.getText().toString();
        String hours = mHoursView.getText().toString();
        String writeup = mWriteupView.getText().toString();

        DocumentReference userRef = mDatabase.collection("ccs").document(cc);

        userRef.update("address", address,
                       "phone", phone,
                       "hours", hours,
                       "writeup", writeup)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TNAP", "DocumentSnapshot of CC successfully updated!");
                        cleanUp();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TNAP", "Error updating document of CC", e);
                    }
                });
    }

    private void cleanUp() {
        // Prepare data intent
        Intent data = new Intent();
        // can pass relevant data back as a result using data.putExtra(key, value);
        // Activity finished ok, return the data with result code
        setResult(20, data);
        finish();
        Toast.makeText(this, "Info saved.", Toast.LENGTH_SHORT).show();
    }
}
