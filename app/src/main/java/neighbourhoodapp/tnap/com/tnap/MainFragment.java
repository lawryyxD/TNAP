package neighbourhoodapp.tnap.com.tnap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;


public class MainFragment extends Fragment {

    // 15: open ShowEventActivity
    private final int EVENT_DETAILS = 15;

    private ImageView mCCBanner;
    private TextView mEventsOne;
    private TextView mEventsTwo;
    private TextView mEventsThree;
    private Button mEventsButton;
    private BottomNavigationView navigationView;

    private int eventNum = 1; // counter to increment

    // Firebase stuff.
    private FirebaseFirestore mDatabase;

    // Info loaded from MainActivity
    private String email;
    private String cc;
    private boolean isAdmin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseFirestore.getInstance();
        email = getArguments().getString("email");
        cc = getArguments().getString("cc");
        isAdmin = getArguments().getBoolean("isAdmin");

        loadMyEvents();
    }

    /**
     * Load the latest 3 events (by eventid).
     */
    public void loadMyEvents() {
        Query mMyEventsQuery = mDatabase.collection("rsvps").whereEqualTo("email", email)
                .orderBy("eventid", Query.Direction.DESCENDING)
                .limit(3);
        mMyEventsQuery.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            updateMyEvents(task.getResult());
                        } else {
                            Log.d("TNAP", "Error getting my events: ", task.getException());
                        }
                    }
                });
    }

    /**
     * Retrieve the event details.
     * @param query The 3 events loaded from Firestore.
     */
    public void updateMyEvents(QuerySnapshot query) {
        CollectionReference allEvents = mDatabase.collection("events");
        for (QueryDocumentSnapshot document : query) {
            allEvents.document(cc + " " + document.getData().get("eventid")).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot eventDoc = task.getResult();
                                if (eventDoc.exists()) {
                                    // successfully loaded event info from database
                                    Event event = eventDoc.toObject(Event.class);
                                    showMyEvent(event);
                                    Log.d("TNAP", "Event data retrieved from Firestore");
                                } else {
                                    Log.d("TNAP", "No such event");
                                }
                            } else {
                                Log.d("TNAP", "Failed to retrieve event with error: ", task.getException());
                            }
                        }
                    });
        }
    }

    /**
     * Show the event details on the app.
     * @param event The event details retrieved from Firestore.
     */
    public void showMyEvent(Event event) { // hardcoded
        final int eventid = event.getEventid();
        if (eventNum == 1) {
            mEventsOne.setText(event.getName());
            mEventsOne.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openEventDetails(eventid);
                }
            });

        } else if (eventNum == 2) {
            mEventsTwo.setText(event.getName());
            mEventsTwo.setVisibility(View.VISIBLE);
            mEventsTwo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openEventDetails(eventid);
                }
            });

        } else {
            mEventsThree.setText(event.getName());
            mEventsThree.setVisibility(View.VISIBLE);
            mEventsThree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openEventDetails(eventid);
                }
            });
        }
        eventNum++;
    }

    private void openEventDetails(int eventid) {
        Intent intent = new Intent(getActivity(), ShowEventActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("admin", isAdmin);
        intent.putExtra("cc", cc);
        intent.putExtra("eventid", eventid);
        startActivityForResult(intent, EVENT_DETAILS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EVENT_DETAILS && resultCode == requestCode) {
            // Can extract data passed in from ShowEventActivity using data.getExtras()...
            // but in our case we don't need it here

            // instead, refresh the page
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            MainFragment refreshFrag = new MainFragment();
            refreshFrag.setArguments(getArguments());
            fragmentTransaction.replace(R.id.fragment_container, refreshFrag).addToBackStack(null).commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mEventsButton = view.findViewById(R.id.main_events_button);
        mCCBanner = (ImageView) view.findViewById(R.id.cc_banner);
        mEventsOne = (TextView) view.findViewById(R.id.my_events_1);
        mEventsTwo = (TextView) view.findViewById(R.id.my_events_2);
        mEventsThree = (TextView) view.findViewById(R.id.my_events_3);

        mEventsTwo.setVisibility(View.GONE);
        mEventsThree.setVisibility(View.GONE);


        switch (cc) {
            case "Computing CC":
                mCCBanner.setImageResource(R.drawable.cc_banner_computing);
                break;
            case "Business CC":
                mCCBanner.setImageResource(R.drawable.cc_banner_business);
                break;
            default:
                mCCBanner.setImageResource(R.drawable.cc_banner_senja);
        }

        // allow clicking on the banner
        mCCBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CCInfoActivity.class);
                intent.putExtra("cc", cc);
                startActivity(intent);
            }
        });

        // TODO: add "See More" text for the events button; main screen only shows 3 events at most
        mEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                EventsFragment eventFrag = new EventsFragment();
                eventFrag.setArguments(getArguments());
                fragmentTransaction.replace(R.id.fragment_container, eventFrag).addToBackStack(null).commit();

                navigationView = getActivity().findViewById(R.id.navigation);
                navigationView.getMenu().getItem(1).setChecked(true);
            }
        });

        return view;
    }
}
