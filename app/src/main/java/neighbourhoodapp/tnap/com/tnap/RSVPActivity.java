package neighbourhoodapp.tnap.com.tnap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class RSVPActivity extends AppCompatActivity
        implements RSVPAdapter.OnRSVPClickListener {

    private RSVPAdapter mAdapter;
    private RecyclerView mRSVPList;
    private TextView mNoRSVPError;
    private TextView mTitleBar;

    // Firebase stuff.
    private FirebaseFirestore mDatabase;
    private Query mQuery;
    private static final int LIMIT = 50; // for query

    private String cc;
    private int eventid;
    private String eventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rsvp);
        mDatabase = FirebaseFirestore.getInstance();
        cc = getIntent().getStringExtra("cc");
        eventid = getIntent().getIntExtra("eventid", 0);
        eventName = getIntent().getStringExtra("name");

        mRSVPList = (RecyclerView) findViewById(R.id.rv_rsvps);
        mNoRSVPError = (TextView) findViewById(R.id.no_rsvps_error);
        mTitleBar = (TextView) findViewById(R.id.title_bar);

        mTitleBar.setText(eventName);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRSVPList.setLayoutManager(layoutManager);
        mRSVPList.setHasFixedSize(true);

        mQuery = mDatabase.collection("rsvps").whereEqualTo("cc", cc)
                .whereEqualTo("eventid", eventid)
                // TODO: order by alphabetical order?
                // .orderBy("startdate", Query.Direction.DESCENDING)
                .limit(LIMIT);
        mAdapter = new RSVPAdapter(mQuery, this) {
            protected void onDataChanged() {
                // show/hide content if the query remains empty.
                if (getItemCount() == 0) {
                    mRSVPList.setVisibility(View.GONE);
                    mNoRSVPError.setVisibility(View.VISIBLE);
                } else {
                    mRSVPList.setVisibility(View.VISIBLE);
                    mNoRSVPError.setVisibility(View.GONE);
                }
            }
        };
        mRSVPList.setAdapter(mAdapter);
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

    /**
     * This is where we receive our callback.
     * This callback is invoked when you click on an item in the list.
     *
     * @param rsvp DocumentSnapshot of the RSVP clicked.
     */
    @Override
    public void onRSVPItemClick(DocumentSnapshot rsvp) {
        // rsvp.getData() returns a Map<String, Object> of the RSVP details
        String toastMessage = rsvp.getId();
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();

        /*
        Intent intent = new Intent(getActivity(), ShowEventActivity.class);
        intent.putExtra("email", getArguments().getString("email"));
        intent.putExtra("admin", isAdmin);
        intent.putExtra("cc", eventDetails.getCC());
        intent.putExtra("eventid", eventDetails.getEventid());
        startActivityForResult(intent, EVENT_DETAILS);
        */
    }

    private void refreshPage() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    // back button functionality
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }
}
