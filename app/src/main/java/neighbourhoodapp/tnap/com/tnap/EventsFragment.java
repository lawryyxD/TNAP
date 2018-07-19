package neighbourhoodapp.tnap.com.tnap;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    private static final int NUM_LIST_ITEMS = 100;
    private EventsAdapter mAdapter;
    private RecyclerView mEventsList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events, container, false);

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
}
