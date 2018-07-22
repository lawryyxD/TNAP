package neighbourhoodapp.tnap.com.tnap;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class AddEventActivity extends AppCompatActivity {
    private AutoCompleteTextView mNameView;
    private AutoCompleteTextView mDescView;
    private AutoCompleteTextView mVenueView;
    private AutoCompleteTextView mCapacityView;
    private AutoCompleteTextView mRegisterByView;
    private AutoCompleteTextView mStartView;
    private AutoCompleteTextView mEndView;

    static final SimpleDateFormat format = new SimpleDateFormat(
            "dd/MM/yyyy hh:mm a", Locale.US); // format for saving dates, eg. 02/09/2018 02:00 PM

    private String cc;
    private int nextEventId;

    // Firebase stuff.
    private FirebaseFirestore mDatabase;
    private Map<String, Object> ccDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        mNameView = (AutoCompleteTextView) findViewById(R.id.add_event_name);
        mDescView = (AutoCompleteTextView) findViewById(R.id.add_event_desc);
        mVenueView = (AutoCompleteTextView) findViewById(R.id.add_event_venue);
        mCapacityView = (AutoCompleteTextView) findViewById(R.id.add_event_capacity);
        mRegisterByView = (AutoCompleteTextView) findViewById(R.id.add_event_registerby);
        mStartView = (AutoCompleteTextView) findViewById(R.id.add_event_start);
        mEndView = (AutoCompleteTextView) findViewById(R.id.add_event_end);

        mDatabase = FirebaseFirestore.getInstance();
        cc = getIntent().getStringExtra("cc");
        DocumentReference docRef = mDatabase.collection("ccs").document(cc);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // successfully loaded CC info from database
                        ccDetails = document.getData();
                        Long eventId = (Long) ccDetails.get("eventid");
                        nextEventId = eventId.intValue() + 1;
                        Log.d("TNAP", "CC data retrieved from Firestore");
                    } else {
                        Log.d("TNAP", "No such CC");
                    }
                } else {
                    Log.d("TNAP", "Failed to retrieve CC info with error: ", task.getException());
                }
            }
        });
    }

    // Executed when Save Event button is pressed.
    public void addEvent(View v) {
        attemptSaving();
    }

    public Date getDateFromString(String dateToSave) {
        try {
            return format.parse(dateToSave);
        } catch (ParseException e) {
            return null;
        }
    }

    private void attemptSaving() {

        String name = mNameView.getText().toString();
        String desc = mDescView.getText().toString();
        String venue = mVenueView.getText().toString();
        int capacity = Integer.parseInt(mCapacityView.getText().toString());
        Date registerby = getDateFromString(mRegisterByView.getText().toString());
        Date start = getDateFromString(mStartView.getText().toString());
        Date end = getDateFromString(mEndView.getText().toString());

        Event newEvent = new Event(cc, capacity, desc, end, nextEventId, 0, name, registerby, start, venue);

        mDatabase.collection("events").document(cc + " " + nextEventId).set(newEvent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TNAP", "DocumentSnapshot of new event successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TNAP", "Error writing document of new event", e);
                    }
                });

        mDatabase.collection("ccs").document(cc).update("eventid", nextEventId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TNAP", "CC's eventid successfully updated!");
                        cleanUp();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TNAP", "Error updating eventid of CC", e);
                    }
                });
    }

    private void cleanUp() {
        // Prepare data intent
        Intent data = new Intent();
        // can pass relevant data back as a result using data.putExtra(key, value);
        // Activity finished ok, return the data with result code
        setResult(12, data);
        finish();
        Toast.makeText(this, "Event added.", Toast.LENGTH_SHORT).show();
    }

    // back button functionality
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }
}
