package neighbourhoodapp.tnap.com.tnap;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Map;

/**
 * The Events feature on our application allows users to view available upcoming events organised by the Community Club
 * and Residential Committees that cater to their neighbourhood. Users will also be able to sign up for these events by
 * RSVPing through the application (or withdraw their application before the deadline). For the CC/RC administrators,
 * they will have access to the additional functions of creating and managing events, such as modifying the details or
 * checking the current RSVP statuses.
 *
 * Create events (Community-Centre user group only): DONE
 * Display events: DONE
 * Filter events: NOT DONE (maybe don't need ba)
 * Notifications (for RSVP-ed events): NOT DONE
 */

public class EventsFragment extends Fragment
        implements EventsAdapter.OnEventClickListener {

    // 12: open AddEventActivity
    private final int REQUEST_ADD = 12;
    // 15: open ShowEventActivity
    private final int EVENT_DETAILS = 15;

    private EventsAdapter mAdapter;
    private RecyclerView mEventsList;
    private TextView mNoEventsError;

    private Button mAddEventButton;

    // Firebase stuff.
    private FirebaseFirestore mDatabase;
    private Map<String, Object> userDetails;
    private Query mQuery;
    private static final int LIMIT = 50; // for query
    private String cc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabase = FirebaseFirestore.getInstance();
        String email = getArguments().getString("email");
        DocumentReference docRef = mDatabase.collection("users").document(email);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // successfully loaded user info from database
                        userDetails = document.getData();
                        cc = (String) userDetails.get("cc");

                        // get the events from the user's CC
                        // TODO: add DATE check and only show events that have not passed yet
                        mQuery = mDatabase.collection("events").whereEqualTo("cc", cc)
                                .orderBy("startdate", Query.Direction.DESCENDING)
                                .limit(LIMIT);
                        mAdapter.setQuery(mQuery);

                        boolean isAdmin = ((long) userDetails.get("admin") != 0);

                        // Include the Add New Event button for Admin only
                        if (isAdmin) {
                            mAddEventButton.setVisibility(View.VISIBLE);
                            // TODO: make the button change to either Add Event or My Events (filter for non-admin)
                            mAddEventButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getActivity(), AddEventActivity.class);
                                    intent.putExtra("cc", cc);
                                    startActivityForResult(intent, REQUEST_ADD);
                                }
                            });
                        }

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        mAddEventButton = (Button) view.findViewById(R.id.add_event_button);

        // We get a reference to our RecyclerView from xml using findViewById.
        // This allows us to do things like set the adapter of the RecyclerView and toggle visibility.
        mEventsList = (RecyclerView) view.findViewById(R.id.rv_events);
        mNoEventsError = (TextView) view.findViewById(R.id.no_events_error);

        /*
         * A LinearLayoutManager is responsible for measuring and positioning item views within a
         * RecyclerView into a linear list. This means that it can produce either a horizontal or
         * vertical list depending on which parameter you pass in to the LinearLayoutManager
         * constructor. By default, if you don't specify an orientation, you get a vertical list.
         * In our case, we want a vertical list, so we don't need to pass in an orientation flag to
         * the LinearLayoutManager constructor.
         */
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        mEventsList.setLayoutManager(layoutManager);
        mEventsList.setHasFixedSize(true);

        if (mQuery == null) {
            mQuery = mDatabase.collection("events").whereEqualTo("eventid", 0);
        }
        mAdapter = new EventsAdapter(mQuery,this) {
            protected void onDataChanged() {
                // show/hide content if the query remains empty.
                if (getItemCount() == 0) {
                    mEventsList.setVisibility(View.GONE);
                    mNoEventsError.setVisibility(View.VISIBLE);
                    // TODO: read up on Snackbars
                } else {
                    mEventsList.setVisibility(View.VISIBLE);
                    mNoEventsError.setVisibility(View.GONE);
                }
            }
        };
        mEventsList.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // TODO: apply filters? onFilter( );
        if (mAdapter != null) mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) mAdapter.stopListening();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_ADD || requestCode == EVENT_DETAILS) && resultCode == requestCode) {
            // Can extract data passed in from Add/ShowEventActivity using data.getExtras()...
            // but in our case we don't need it here

            // instead, refresh the page
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            EventsFragment refreshFrag = new EventsFragment();
            refreshFrag.setArguments(getArguments());
            fragmentTransaction.replace(R.id.fragment_container, refreshFrag).addToBackStack(null).commit();
        }
    }

    /**
     * This is where we receive our callback.
     * This callback is invoked when you click on an item in the list.
     *
     * @param event DocumentSnapshot of the event clicked.
     */
    @Override
    public void onEventItemClick(DocumentSnapshot event) {
        // event.getData() returns a Map<String, Object> of the event details
        // Go to the details page for the selected event
        Event eventDetails = event.toObject(Event.class);
        Long admin = (Long) userDetails.get("admin");
        // String toastMessage = eventDetails.get("venue") + "!!";
        //Toast.makeText(this.getContext(), toastMessage, Toast.LENGTH_LONG).show();


        Intent intent = new Intent(getActivity(), ShowEventActivity.class);
        intent.putExtra("email", getArguments().getString("email"));
        intent.putExtra("admin", admin.intValue());
        intent.putExtra("cc", eventDetails.getCC());
        intent.putExtra("eventid", eventDetails.getEventid());
        startActivityForResult(intent, EVENT_DETAILS);
    }
}
