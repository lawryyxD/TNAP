package neighbourhoodapp.tnap.com.tnap;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_events, container, false);
    }
}
