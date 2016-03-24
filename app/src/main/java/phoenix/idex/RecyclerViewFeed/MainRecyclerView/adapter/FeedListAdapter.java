package phoenix.idex.RecyclerViewFeed.MainRecyclerView.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.List;

import phoenix.idex.DateColumn;
import phoenix.idex.EditProfileActivity;
import phoenix.idex.Graphing.GraphActivity;
import phoenix.idex.R;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.app.AppController;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.data.FeedItem;
import phoenix.idex.ServerConnections.ServerRequests;
import phoenix.idex.UserLocalStore;

/**
 * Created by Ravinder on 2/25/16.
 */
public class FeedListAdapter extends RecyclerSwipeAdapter<FeedListAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater inflater;
    private List<FeedItem> feedItems;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();;
    ServerRequests serverRequests;
    private UserLocalStore userLocalStore;

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
        userLocalStore = new UserLocalStore(mContext);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        serverRequests = new ServerRequests(mContext);

        // Initialize fonts
        Typeface killFillFont = Typeface.createFromAsset(mContext.getAssets(), "Menufont.ttf");
        holder.name.setTypeface(killFillFont);

        final FeedItem currentPos = feedItems.get(position);
        holder.name.setText(currentPos.getName());

        // Logged in user's post will get delete option instead of report option
        if (currentPos.getUsername().equals(userLocalStore.getLoggedInUser().getUsername())) {
            holder.tvManagePost.setText("Delete");
        } else {
            holder.tvManagePost.setText("Report");
        }

        // Converting timestamp into x ago format
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                Long.parseLong(currentPos.getTimeStamp()),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        holder.timestamp.setText(timeAgo);

        // Check for empty status message
        if (!TextUtils.isEmpty(currentPos.getStatus())) {
            holder.txtStatusMsg.setText(currentPos.getStatus());
            holder.txtStatusMsg.setVisibility(View.VISIBLE);
        } else {
            // status is empty, remove from view
            holder.txtStatusMsg.setVisibility(View.GONE);
        }

        // If the user uploaded a new photo through the editing page, load current JSON and not Cache
        // This is achieved through setImageLoaderNull()
        if (EditProfileActivity.isNewPhotoUploaded()) {
            EditProfileActivity.setIsNewPhotoUploaded(false);
            AppController.getInstance().setImageLoaderNull();
        }
        imageLoader = AppController.getInstance().getImageLoader();

        // user profile pic
        if (!currentPos.getProfilePic().equals("")) {
            holder.profilePic.setImageUrl(currentPos.getProfilePic(), imageLoader);
        } else { // default
            holder.profilePic.setImageUrl("http://oi67.tinypic.com/24npqbk.jpg", imageLoader);
        }

        //holder.bFill.setText("Fill (" + currentPos.getTotalFill() + ")");
        //holder.bKill.setText("Kill (" + currentPos.getTotalKill() + ")");

        holder.numFill.setText("" + currentPos.getTotalFill());
        holder.numKill.setText("" + currentPos.getTotalKill());

        holder.imgbFill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (currentPos.getCurrentColumn() == DateColumn.getRowNumber()) {
                    // Update Fill number in the database. Increment by 1 through SQL command in .php
                    serverRequests.updateFillInBackground(currentPos.getId(), currentPos.getCurrentColumn());
                } else {
                    // Update both fill number and current column because it's a new day
                    serverRequests.updateFillAndCurrentColumnInBackground(currentPos.getId(), DateColumn.getRowNumber());
                }
                currentPos.hitFill();
                holder.numFill.setText("" + currentPos.getTotalFill());
                holder.swipeLayout.close();
            }
        });
        holder.imgbKill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentPos.getCurrentColumn() == DateColumn.getRowNumber()) {
                    // Update Kill number in the database. Decrement by 1 through SQL command in .php
                    serverRequests.updateKillInBackground(currentPos.getId(), currentPos.getCurrentColumn());
                } else {
                    // Update both fill number and current column because it's a new day
                    serverRequests.updateKillAndCurrentColumnInBackground(currentPos.getId(), DateColumn.getRowNumber());
                }
                currentPos.hitKill();
                holder.numKill.setText("" + currentPos.getTotalKill());
                holder.swipeLayout.close();
            }
        });

        holder.tvGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, GraphActivity.class);
                intent.putExtra("fillArray", currentPos.getFillArray());
                intent.putExtra("killArray", currentPos.getKillArray());
                mContext.startActivity(intent);
            }
        });

        // Close the swipeLayout when user click on the post
        holder.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.swipeLayout.close();
            }
        });

        holder.tvManagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (holder.tvManagePost.getText().toString().equals("Delete")) {

                    serverRequests.deleteAPostInBackground(currentPos.getId());
                } else {
                    System.out.println(currentPos.getFillArray());

                    Toast.makeText(v.getContext(), "Clicked on Report ", Toast.LENGTH_SHORT).show();
                }
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
        Button bFill, bKill;
        ImageButton imgbFill, imgbKill;
        TextView tvGraph, numFill, numKill, txtStatusMsg, timestamp, name, tvManagePost;

        public ViewHolder(View itemView) {
            super(itemView);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
            tvGraph = (TextView) itemView.findViewById(R.id.tvGraph);
            tvManagePost = (TextView) itemView.findViewById(R.id.tvManagePost);
            profilePic = (NetworkImageView) itemView.findViewById(R.id.profilePic);
            name = (TextView) itemView.findViewById(R.id.name);
            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
            txtStatusMsg = (TextView) itemView.findViewById(R.id.txtStatusMsg);
            numFill = (TextView) itemView.findViewById(R.id.tvFillNum);
            numKill = (TextView) itemView.findViewById(R.id.tvKillNum);
            imgbFill = (ImageButton) itemView.findViewById(R.id.imgbFill);
            imgbKill = (ImageButton) itemView.findViewById(R.id.imgbKill);

            //bFill = (Button) itemView.findViewById(R.id.bFill);
            //bKill = (Button) itemView.findViewById(R.id.bKill);
        }
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    public static class MyDialog extends DialogFragment {
        private int postID;
        private ServerRequests serverRequests;

        public MyDialog() {
            super();

        }
    }

}
