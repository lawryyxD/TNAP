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
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

/**
 * The Events feature on our application allows users to view available upcoming events organised by the Community Club
 * and Residential Committees that cater to their neighbourhood. Users will also be able to sign up for these events by
 * RSVPing through the application (or withdraw their application before the deadline). For the CC/RC administrators,
 * they will have access to the additional functions of creating and managing events, such as modifying the details or
 * checking the current RSVP statuses.
 *
 * Create events (Community-Centre user group only): NOT DONE
 * Display events: NOT DONE
 * Filter events: NOT DONE (maybe don't need ba)
 * Notifications (for RSVP-ed events): NOT DONE
 */

public class EventsFragment extends Fragment {

    // 12: open AddEventActivity
    private final int REQUEST_ADD = 12;

    private static final int NUM_LIST_ITEMS = 100;
    private EventsAdapter mAdapter;
    private RecyclerView mEventsList;

    private Button mAddEventButton;

    // Firebase stuff.
    private FirebaseFirestore mDatabase;
    private Map<String, Object> userDetails;
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

                        boolean isAdmin = ((long) userDetails.get("admin") != 0);

                        // Include the Add New Event button for Admin only
                        if (isAdmin) {
                            mAddEventButton.setVisibility(View.VISIBLE);
                            // TODO: make the button change to either Add Event or My Events (filter for non-admin)
                            mAddEventButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // TODO: Change to AddEventActivity.class
                                    Intent intent = new Intent(getActivity(), CCInfoActivity.class);
                                    intent.putExtra("cc", cc);
                                    startActivityForResult(intent, REQUEST_ADD);
                                }
                            });
                        }

                        loadEvents(); // TODO: populate the list with events from Firestore

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
        mAdapter = new EventsAdapter(NUM_LIST_ITEMS);
        mEventsList.setAdapter(mAdapter);

        return view;
    }

    public void loadEvents() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD && resultCode == requestCode) { // returning from Add Event page
            // Can extract data passed in from AddEventActivity using data.getExtras()...
            // but in our case we don't need it here

            // instead, refresh the page
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            EventsFragment refreshFrag = new EventsFragment();
            refreshFrag.setArguments(getArguments());
            fragmentTransaction.replace(R.id.fragment_container, refreshFrag).addToBackStack(null).commit();
        }
    }
}
