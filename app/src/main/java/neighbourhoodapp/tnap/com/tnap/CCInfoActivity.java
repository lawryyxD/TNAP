package neighbourhoodapp.tnap.com.tnap;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class CCInfoActivity extends AppCompatActivity {

    public final static int MY_PERMISSIONS_REQUEST_CALL_PHONE = 10;

    private ImageView mCCBanner;
    private TextView mAddressView;
    private TextView mHoursView;
    private TextView mWriteupView;
    private Button mContactButton;
    private String cc;

    // Firebase instance variables
    private FirebaseFirestore mDatabase;
    private Map<String, Object> ccDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ccinfo);
        mCCBanner = (ImageView) findViewById(R.id.cc_info_banner);
        mAddressView = (TextView) findViewById(R.id.cc_info_address);
        mHoursView = (TextView) findViewById(R.id.cc_info_hours);
        mWriteupView = (TextView) findViewById(R.id.cc_info_writeup);
        mContactButton = (Button) findViewById(R.id.call_cc_button);

        mDatabase = FirebaseFirestore.getInstance();
        cc = getIntent().getStringExtra("cc");

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

        DocumentReference docRef = mDatabase.collection("ccs").document(cc);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // successfully loaded user info from database
                        ccDetails = document.getData();

                        // Populate the page with details
                        mAddressView.setText((String) ccDetails.get("address"));
                        mHoursView.setText((String) ccDetails.get("hours"));
                        mWriteupView.setText((String) ccDetails.get("writeup"));

                        mContactButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent callIntent = new Intent(Intent.ACTION_CALL);
                                callIntent.setData(Uri.parse("tel:" + ccDetails.get("phone")));
                                tryCalling(callIntent);
                            }
                        });

                        Log.d("TNAP", "CC data retrieved from Firestore");
                    } else {
                        mAddressView.setText("Sorry, CC Information not available!");
                        Log.d("TNAP", "CC information not loaded");
                    }
                } else {
                    Log.d("TNAP", "Failed to retrieve CC info with error: ", task.getException());
                }
            }
        });

    }

    private void tryCalling(Intent callIntent) {
        if (callIntent.resolveActivity(getPackageManager()) != null) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {
                startActivity(callIntent); // permission granted
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.CALL_PHONE},
                        MY_PERMISSIONS_REQUEST_CALL_PHONE);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! can call alr!
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Unable to call from app.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

}

