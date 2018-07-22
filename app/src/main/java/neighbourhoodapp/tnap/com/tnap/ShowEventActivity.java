package neighbourhoodapp.tnap.com.tnap;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ShowEventActivity extends AppCompatActivity {

    // 13: open EditEventActivity
    private final int REQUEST_EDIT = 13;

    private TextView mNameView;
    private TextView mDescView;
    private TextView mVenueView;
    private TextView mCapacityView;
    private TextView mRegisterByView;
    private TextView mStartView;
    private TextView mEndView;
    private Button mEventActionButton;

    static final SimpleDateFormat format = new SimpleDateFormat(
            "dd/MM/yyyy hh:mm a", Locale.US); // format for saving dates, eg. 02/09/2018 02:00 PM

    private String email;
    private String cc;
    private int eventid;
    private boolean isAdmin;
    private boolean isAttending;
    private String rsvpId;

    // Firebase stuff.
    private FirebaseFirestore mDatabase;
    private Event eventDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_event);

        mNameView = (TextView) findViewById(R.id.show_event_name);
        mDescView = (TextView) findViewById(R.id.show_event_desc);
        mVenueView = (TextView) findViewById(R.id.show_event_venue);
        mCapacityView = (TextView) findViewById(R.id.show_event_capacity);
        mRegisterByView = (TextView) findViewById(R.id.show_event_registerby);
        mStartView = (TextView) findViewById(R.id.show_event_start);
        mEndView = (TextView) findViewById(R.id.show_event_end);
        mEventActionButton = (Button) findViewById(R.id.event_action_button);

        mDatabase = FirebaseFirestore.getInstance();
        email = getIntent().getStringExtra("email");
        cc = getIntent().getStringExtra("cc");
        eventid = getIntent().getIntExtra("eventid", 0);
        isAdmin = getIntent().getIntExtra("admin", 0) == 1;

        DocumentReference docRef = mDatabase.collection("events").document(cc + " " + eventid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // successfully loaded event info from database, display info
                        eventDetails = document.toObject(Event.class);
                        displayEvent();
                        Log.d("TNAP", "Event data retrieved from Firestore");
                    } else {
                        Log.d("TNAP", "No such event");
                        // TODO: TOAST an error occurred, go back to prev page
                    }
                } else {
                    Log.d("TNAP", "Failed to retrieve event info with error: ", task.getException());
                }
            }
        });

        if (isAdmin) {
            mEventActionButton.setText("Edit Event"); // TODO: use R.strings
            mEventActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Change to EditEventActivity.class
                    // TODO: Create EditEventActivity.class
                    Intent intent = new Intent(getParent(), AddEventActivity.class);
                    intent.putExtra("cc", cc);
                    intent.putExtra("eventid", eventid);
                    startActivityForResult(intent, REQUEST_EDIT);
                    }
            });
        } else {
            // check if user is attending
            Query mMyEventQuery = mDatabase.collection("rsvps").whereEqualTo("email", email)
                    .whereEqualTo("eventid", eventid);
            mMyEventQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        if (result.isEmpty()) { // not attending
                            isAttending = false;
                            mEventActionButton.setText("RSVP"); // TODO: use R.strings
                        } else {
                            isAttending = true;
                            rsvpId = result.getDocuments().get(0).getId();
                            mEventActionButton.setText("Cancel RSVP"); // TODO: use R.strings
                        }
                        mEventActionButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onRSVP();
                            }
                        });
                    } else {
                        Log.d("TNAP", "Error getting my events: ", task.getException());
                    }
                }
            });
        }
    }

    private void onRSVP() {
        // TODO: Add confirmation message before adding/removing RSVP
        if (isAttending) {
            if (eventDetails.getRegisterby().before(new Date())) {
                // Too late already
                Toast.makeText(this, "Please contact the CC.", Toast.LENGTH_SHORT).show();
            } else {
                // Remove RSVP
                mDatabase.collection("rsvps").document(rsvpId).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("TNAP", "RSVP successfully deleted!");
                                // update headcount
                                eventDetails.setHeadcount(eventDetails.getHeadcount() - 1);
                                mDatabase.collection("events")
                                        .document(cc + " " + eventid)
                                        .set(eventDetails);
                                refreshPage();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("TNAP", "Error deleting RSVP", e);
                            }
                        });
            }
        } else {
            if (eventDetails.getHeadcount() >= eventDetails.getCapacity()) {
                // Full already
                Toast.makeText(this, "This event is full.", Toast.LENGTH_SHORT).show();
            } else if (eventDetails.getRegisterby().before(new Date())) {
                // Too late already
                Toast.makeText(this, "You can no longer sign up.", Toast.LENGTH_SHORT).show();
            } else {
                // Add RSVP
                Map<String, Object> newRSVP = new HashMap<>();
                newRSVP.put("cc", cc);
                newRSVP.put("email", email);
                newRSVP.put("eventid", eventid);
                mDatabase.collection("rsvps").add(newRSVP)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("TNAP", "New RSVP written with ID: " + documentReference.getId());
                                // update headcount
                                eventDetails.setHeadcount(eventDetails.getHeadcount() + 1);
                                mDatabase.collection("events")
                                        .document(cc + " " + eventid)
                                        .set(eventDetails);
                                // need success/failure listeners for this one?
                                refreshPage();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("TNAP", "Error adding RSVP", e);
                            }
                        });
            }
        }

    }

    private void refreshPage() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    private void displayEvent() {
        String capacityText = eventDetails.getHeadcount() + "/" + eventDetails.getCapacity();

        mNameView.setText(eventDetails.getName());
        mDescView.setText(eventDetails.getDescription());
        mVenueView.setText(eventDetails.getVenue());
        mCapacityView.setText(capacityText);

        mRegisterByView.setText(new SimpleDateFormat(
                "dd/MM/yyyy hh:mm a", Locale.US).format(eventDetails.getRegisterby()));

        mStartView.setText(new SimpleDateFormat(
                "dd/MM/yyyy hh:mm a", Locale.US).format(eventDetails.getStartdate()));

        mEndView.setText(new SimpleDateFormat(
                "dd/MM/yyyy hh:mm a", Locale.US).format(eventDetails.getEnddate()));
    }

    // back button functionality
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        Intent data = new Intent();
        setResult(15, data);
        return true;
    }
}

