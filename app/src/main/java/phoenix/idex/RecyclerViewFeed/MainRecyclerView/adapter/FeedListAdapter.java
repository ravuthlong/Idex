package phoenix.idex.RecyclerViewFeed.MainRecyclerView.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.List;

import phoenix.idex.R;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.app.AppController;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.data.FeedItem;
/**
 * Created by Ravinder on 2/25/16.
 */
public class FeedListAdapter extends RecyclerSwipeAdapter<FeedListAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater inflater;
    private List<FeedItem> feedItems;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public FeedListAdapter(Context context, List<FeedItem> feedItems) {
        inflater = LayoutInflater.from(context);
        this.mContext = context;
        this.feedItems = feedItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Custom root of recycle view
        View view = inflater.inflate(R.layout.item_mainfeed, parent, false);
        // Hold a structure of a view. See class viewholder, which holds the structure
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        final FeedItem currentPos = feedItems.get(position);
        holder.name.setText(currentPos.getName());

        // Converting timestamp into x ago format
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                Long.parseLong(currentPos.getTimeStamp()),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        holder.timestamp.setText(timeAgo);

        // Chcek for empty status message
        if (!TextUtils.isEmpty(currentPos.getStatus())) {
            holder.txtStatusMsg.setText(currentPos.getStatus());
            holder.txtStatusMsg.setVisibility(View.VISIBLE);
        } else {
            // status is empty, remove from view
            holder.txtStatusMsg.setVisibility(View.GONE);
        }

        // user profile pic
        holder.profilePic.setImageUrl(currentPos.getProfilePic(), imageLoader);

        holder.numKill.setText(Integer.toString(currentPos.getKill()));
        holder.numFill.setText(Integer.toString(currentPos.getFill()));

        holder.bFill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPos.hitFill();
                holder.bFill.setText("Fill (" + currentPos.getFill() + ")");
            }
        });
        holder.bKill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPos.hitKill();
                holder.bKill.setText("Kill (" + currentPos.getKill() + ")");
            }
        });


        holder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        // Drag From Left
        holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Left, holder.swipeLayout.findViewById(R.id.bottom_wrapper1));

        // Drag From Right
        holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, holder.swipeLayout.findViewById(R.id.bottom_wrapper));

        // Handling different events when swiping
        holder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onClose(SwipeLayout layout) {
                //when the SurfaceView totally cover the BottomView.
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                //you are swiping.
            }

            @Override
            public void onStartOpen(SwipeLayout layout) {

            }

            @Override
            public void onOpen(SwipeLayout layout) {
                //when the BottomView totally show.
            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                //when user's hand released.
            }
        });

        // Close the swipeLayout when user click on the post
        holder.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.swipeLayout.close();
            }
        });

        holder.tvLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(v.getContext(), "Clicked on Report ", Toast.LENGTH_SHORT).show();
            }
        });


        holder.tvFill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(view.getContext(), "Clicked on Fill ", Toast.LENGTH_SHORT).show();
            }
        });

        holder.tvKill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(view.getContext(), "Clicked on Kill  ", Toast.LENGTH_SHORT).show();
            }
        });

        // mItemManger is member in RecyclerSwipeAdapter Class
        mItemManger.bindView(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    // Holder knows and references where the fields are
    class ViewHolder extends RecyclerView.ViewHolder {

        SwipeLayout swipeLayout;
        NetworkImageView profilePic;
        TextView name;
        TextView timestamp;
        TextView txtStatusMsg;
        TextView numFill;
        TextView numKill;
        Button bFill;
        Button bKill;

        TextView tvFill;
        TextView tvKill;
        TextView tvLocation;

        public ViewHolder(View itemView) {
            super(itemView);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
            tvFill = (TextView) itemView.findViewById(R.id.tvFill);
            tvKill = (TextView) itemView.findViewById(R.id.tvKill);
            tvLocation = (TextView) itemView.findViewById(R.id.tvLocation);

            profilePic = (NetworkImageView) itemView.findViewById(R.id.profilePic);
            name = (TextView) itemView.findViewById(R.id.name);
            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
            txtStatusMsg = (TextView) itemView.findViewById(R.id.txtStatusMsg);
            numFill = (TextView) itemView.findViewById(R.id.tvFillNum);
            numKill = (TextView) itemView.findViewById(R.id.tvKillNum);
            bFill = (Button) itemView.findViewById(R.id.bFill);
            bKill = (Button) itemView.findViewById(R.id.bKill);
        }
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }
}
