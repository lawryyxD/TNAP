package neighbourhoodapp.tnap.com.tnap;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Bundle emailBundle; // to access user details among fragments

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    MainFragment mainFrag = new MainFragment();
                    mainFrag.setArguments(emailBundle);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, mainFrag).addToBackStack(null).commit();
                    return true;
                case R.id.navigation_events:
                    EventsFragment eventsFrag = new EventsFragment();
                    eventsFrag.setArguments(emailBundle);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, eventsFrag).addToBackStack(null).commit();
                    return true;
//                case R.id.navigation_requests:
//                    RequestsFragment requestFrag = new RequestsFragment();
//                    requestFrag.setArguments(emailBundle);
//                    getSupportFragmentManager().beginTransaction()
//                            .replace(R.id.fragment_container, requestFrag).addToBackStack(null).commit();
//                    return true;
                case R.id.navigation_profile:
                    ProfileFragment profileFrag = new ProfileFragment();
                    profileFrag.setArguments(emailBundle);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, profileFrag).addToBackStack(null).commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailBundle = getIntent().getExtras();

        // restored from prev state, don't need to do anything
        if (savedInstanceState != null) return;

        MainFragment mainFrag = new MainFragment();
        mainFrag.setArguments(emailBundle);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mainFrag).commit();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setItemBackgroundResource(R.drawable.menubackground);

    }

}
