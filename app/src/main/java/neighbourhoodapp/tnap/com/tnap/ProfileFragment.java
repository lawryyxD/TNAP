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
    private Button mLogoutButton;

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
    }

    private void loadAdminView() {
        mProfileName.setText(cc);
        DocumentReference docRef = mDatabase.collection("ccs").document(cc);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // successfully loaded CC info from database
                        Map<String, Object> ccDetails = document.getData();
                        mProfileNRIC.setText((String) ccDetails.get("hours"));
                        mProfileEmail.setText((String) ccDetails.get("phone"));
                        mProfileAddress.setText((String) ccDetails.get("address"));
                        Log.d("TNAP", "CC data retrieved from Firestore");
                    } else {
                        Log.d("TNAP", "No such CC");
                    }
                } else {
                    Log.d("TNAP", "Failed to retrieve CC info with error: ", task.getException());
                }
            }
        });

        mEditProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditCCInfoActivity.class);
                intent.putExtra("cc", cc);
                //startActivity(intent);
                startActivityForResult(intent, REQUEST_EDIT);
            }
        });

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View abc) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                getActivity().finish();
                startActivity(intent);
            }
        });
    }

    private void loadUserView() {
        DocumentReference docRef = mDatabase.collection("users").document(email);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // successfully loaded user info from database
                        Map<String, Object> userDetails = document.getData();
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

        mEditProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                intent.putExtra("email", email);
                //startActivity(intent);
                startActivityForResult(intent, REQUEST_EDIT);
            }
        });

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View abc) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                getActivity().finish();
                startActivity(intent);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mProfileName = (TextView) view.findViewById(R.id.profile_name);
        mProfileNRIC = (TextView) view.findViewById(R.id.profile_nric);
        mProfileEmail = (TextView) view.findViewById(R.id.profile_email);
        mProfileAddress = (TextView) view.findViewById(R.id.profile_address);
        mEditProfileButton = view.findViewById(R.id.edit_profile_button);
        mLogoutButton = view.findViewById(R.id.logout_button);

        if (isAdmin) {
            loadAdminView();
        } else {
            loadUserView();
        }

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT && resultCode == requestCode) { // returning from Edit Profile page
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
