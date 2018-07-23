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

    // Firebase stuff.
    private FirebaseFirestore mDatabase;
    private Map<String, Object> userDetails;

    private Bundle userBundle; // to access user details among fragments

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    MainFragment mainFrag = new MainFragment();
                    mainFrag.setArguments(userBundle);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, mainFrag).addToBackStack(null).commit();
                    return true;
                case R.id.navigation_events:
                    EventsFragment eventsFrag = new EventsFragment();
                    eventsFrag.setArguments(userBundle);
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
                    profileFrag.setArguments(userBundle);
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

        // restored from prev state, don't need to do anything
        if (savedInstanceState != null) return;

        userBundle = getIntent().getExtras();
        mDatabase = FirebaseFirestore.getInstance();
        String email = getIntent().getStringExtra("email");
        DocumentReference docRef = mDatabase.collection("users").document(email);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // successfully loaded user info from database
                        userDetails = document.getData();
                        userBundle.putString("cc", (String) userDetails.get("cc"));
                        userBundle.putBoolean("isAdmin", ((long) userDetails.get("admin") != 0));

                        MainFragment mainFrag = new MainFragment();
                        mainFrag.setArguments(userBundle);
                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.fragment_container, mainFrag).commit();

                        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
                        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
                        navigation.setItemBackgroundResource(R.drawable.menubackground);

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

}
