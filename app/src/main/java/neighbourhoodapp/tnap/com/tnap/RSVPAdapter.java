package neighbourhoodapp.tnap.com.tnap;

import android.app.LauncherActivity;
import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class RSVPAdapter extends FirestoreAdapter<RSVPAdapter.RSVPViewHolder> {

    private static final String TAG = RSVPAdapter.class.getSimpleName();

    // an on-click handler to make it easy for an Activity to interface with the RecyclerView
    private OnRSVPClickListener mOnClickListener;

    /**
     *  The interface that receives onCLick messages.
     */
    public interface OnRSVPClickListener {
        void onRSVPItemClick(DocumentSnapshot event);
    }

    public RSVPAdapter(Query query, OnRSVPClickListener listener) {
        super(query);
        mOnClickListener = listener;
    }

    /**
     *
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new EventViewHolder that holds the View for each list item
     */
    @Override
    public RSVPViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.rsvp_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        RSVPViewHolder viewHolder = new RSVPViewHolder(view);

        return viewHolder;
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(RSVPViewHolder holder, int position) {
        // display the data
        holder.bind(getSnapshot(position), mOnClickListener);
    }

    /**
     * Cache of the children views for a list item.
     */
    class RSVPViewHolder extends RecyclerView.ViewHolder {

        TextView emailView;

        /**
         * Constructor for our ViewHolder. Within this constructor, we get a reference to our
         * TextViews and set an onClickListener to listen for clicks. Those will be handled in the
         * onClick method below.
         * @param itemView The View that you inflated in onCreateViewHolder.
         */
        public RSVPViewHolder(View itemView) {
            super(itemView);
            emailView = (TextView) itemView.findViewById(R.id.list_rsvp_email);
        }

        /**
         * This method will properly display the event listing.
         */
        void bind(final DocumentSnapshot snapshot, final OnRSVPClickListener listener) {
            emailView.setText(snapshot.getString("email"));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onRSVPItemClick(snapshot);
                    }
                }
            });
        }
    }
}
