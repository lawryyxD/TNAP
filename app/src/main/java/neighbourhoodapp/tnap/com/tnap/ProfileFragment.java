package neighbourhoodapp.tnap.com.tnap;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;


public class ProfileFragment extends Fragment {

    // 20: open EditProfileActivity
    private final int REQUEST_EDIT = 20;

    private TextView mProfileName;
    private TextView mProfileNRIC;
    private TextView mProfileEmail;
    private TextView mProfileAddress;
    private Button mEditProfileButton;

    // Firebase stuff.
    private FirebaseFirestore mDatabase;
    private Map<String, Object> userDetails;

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

                        // Populate the page with details
                        mProfileName = (TextView) getView().findViewById(R.id.profile_name); // should put under onViewCreated()
                        mProfileNRIC = (TextView) getView().findViewById(R.id.profile_nric);
                        mProfileEmail = (TextView) getView().findViewById(R.id.profile_email);
                        mProfileAddress = (TextView) getView().findViewById(R.id.profile_address);

                        mProfileName.setText((String) userDetails.get("username"));
                        mProfileNRIC.setText((String) userDetails.get("nric"));
                        mProfileEmail.setText((String) userDetails.get("email"));
                        mProfileAddress.setText((String) userDetails.get("address"));

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

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mEditProfileButton = view.findViewById(R.id.edit_profile_button);

        mEditProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                intent.putExtra("email", getArguments().getString("email"));
                //startActivity(intent);
                startActivityForResult(intent, REQUEST_EDIT);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT && resultCode == requestCode) {
            // Can extract data passed in from EditProfileActivity using data.getExtras()...
            // but in our case we don't need it here

            // instead, refresh the page
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ProfileFragment refreshFrag = new ProfileFragment();
            refreshFrag.setArguments(getArguments());
            fragmentTransaction.replace(R.id.fragment_container, refreshFrag).addToBackStack(null).commit();
        }
    }
}
