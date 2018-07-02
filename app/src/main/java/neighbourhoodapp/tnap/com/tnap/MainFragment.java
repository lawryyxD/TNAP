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
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;


public class MainFragment extends Fragment {

    private ImageView mCCBanner;
    private TextView mMainCC;
    private Button mRequestsButton;
    private BottomNavigationView navigationView;

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

                        final String userCC = (String) userDetails.get("cc");
                        // update CC banner
                        switch (userCC) {
                            case "Computing CC":
                                mCCBanner.setImageResource(R.drawable.cc_banner_computing);
                                break;
                            case "Business CC":
                                mCCBanner.setImageResource(R.drawable.cc_banner_business);
                                break;
                            default:
                                mCCBanner.setImageResource(R.drawable.cc_banner_senja);
                        }

                        // allow clicking
                        mCCBanner.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), CCInfoActivity.class);
                                intent.putExtra("cc", userCC);
                                startActivity(intent);
                            }
                        });

                        // update CC text
                        mMainCC.setText(userCC);

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
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mRequestsButton = view.findViewById(R.id.main_requests_button);
        mCCBanner = (ImageView) view.findViewById(R.id.cc_banner);
        mMainCC = (TextView) view.findViewById(R.id.main_cc);

        mRequestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                RequestsFragment requestFrag = new RequestsFragment();
                requestFrag.setArguments(getArguments());
                fragmentTransaction.replace(R.id.fragment_container, requestFrag).addToBackStack(null).commit();

                navigationView = getActivity().findViewById(R.id.navigation);
                navigationView.getMenu().getItem(2).setChecked(true);
            }
        });

        return view;
    }
}
