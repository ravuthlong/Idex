package phoenix.idex.RecyclerViewFeed.MainRecyclerView.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.List;

import phoenix.idex.Activities.CommentActivity;
import phoenix.idex.Activities.EditProfileActivity;
import phoenix.idex.Graphing.GraphActivity;
import phoenix.idex.R;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.app.AppController;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.data.FeedItem;
import phoenix.idex.ServerConnections.ServerRequests;
import phoenix.idex.SoundPlayer;
import phoenix.idex.UserLocalStore;
import phoenix.idex.VolleyServerConnections.VolleyMainPosts;

/**
 * Created by Ravinder on 2/25/16.
 */
public class FeedListAdapter extends RecyclerSwipeAdapter<FeedListAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater inflater;
    private List<FeedItem> feedItems;
    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();;
    private ServerRequests serverRequests;
    private UserLocalStore userLocalStore;
    private View postView;
    boolean swipeIsActive = true;
    private VolleyMainPosts volleyMainPosts;
    private SoundPlayer fillSound, killSound;
    private AlertDialog.Builder editPostDialog;

    public FeedListAdapter(Context context, List<FeedItem> feedItems) {
        inflater = LayoutInflater.from(context);
        this.mContext = context;
        this.feedItems = feedItems;
        userLocalStore = new UserLocalStore(mContext);
        editPostDialog = new AlertDialog.Builder(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Custom root of recycle view
         this.postView = inflater.inflate(R.layout.item_mainfeed, parent, false);
        // Hold a structure of a view. See class viewholder, which holds the structure

        ViewHolder holder = new ViewHolder(this.postView);

        // Initialize fill and kill sounds
        fillSound = new SoundPlayer(mContext, R.raw.fillsound);
        killSound = new SoundPlayer(mContext, R.raw.killsound);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        serverRequests = new ServerRequests(mContext);
        volleyMainPosts = new VolleyMainPosts(mContext);

        // Initialize fonts
        //Typeface killFillFont = Typeface.createFromAsset(mContext.getAssets(), "Menufont.ttf");
        //holder.name.setTypeface(killFillFont);

        holder.name.setTextColor(ContextCompat.getColor(mContext, R.color.font));
        holder.txtStatusMsg.setTextColor(ContextCompat.getColor(mContext, R.color.font));
        holder.timestamp.setTextColor(ContextCompat.getColor(mContext, R.color.font));

        holder.numFill.setTextColor(ContextCompat.getColor(mContext, R.color.font));
        holder.numKill.setTextColor(ContextCompat.getColor(mContext, R.color.font));


        final FeedItem currentPos = feedItems.get(position);

        holder.name.setText(currentPos.getName());

        if (currentPos.getTotalFill() >= 100) {
            holder.numFill.setTextSize(TypedValue.COMPLEX_UNIT_PX, holder.numKill.getTextSize() / (float) 1.2);
        } else if (currentPos.getTotalKill() >= 100) {
            holder.numKill.setTextSize(TypedValue.COMPLEX_UNIT_PX, holder.numKill.getTextSize() / (float) 1.2);
        }
        holder.numFill.setText("" + (currentPos.getTotalFill()));
        holder.numKill.setText("-" + (currentPos.getTotalKill()));

        if (currentPos.getFillOrKill() == 0) {
            holder.imgbKill.setBackgroundResource(R.drawable.killed);
        } else if (currentPos.getFillOrKill() == 1) {
            holder.imgbFill.setBackgroundResource(R.drawable.filled);
        }

        // Logged in user's post will get delete option instead of report option
        if (currentPos.getUsername().equals(userLocalStore.getLoggedInUser().getUsername())) {
            holder.tvManagePost.setText("Delete");
            holder.tvEditPost.setVisibility(View.VISIBLE);

        } else {
            holder.bottomWrapper1.setWeightSum(1);
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
            //holder.profilePic.setBackgroundResource(R.drawable.profilepic_border);
        } else { // default
            holder.profilePic.setImageUrl("http://oi67.tinypic.com/24npqbk.jpg", imageLoader);
        }

        holder.numFill.setText("" + currentPos.getTotalFill());
        holder.numKill.setText("-" + currentPos.getTotalKill());

        holder.imgbFill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserLocalStore.allowRefresh = true;

                fillSound.playSound();

                if (userLocalStore.getLoggedInUser().getUsername().equals("a") ||
                        userLocalStore.getLoggedInUser().getUsername().equals("sealcub22")) {

                    // There's no fill so do normal operations
                    serverRequests.updateFillAndFillColumnInBackground(currentPos.getId());

                    currentPos.hitFill();
                    holder.numFill.setText("" + (currentPos.getTotalFill()));

                } else {
                    if (currentPos.getFillOrKill() == 1) {
                        System.out.println("HAS FILL CANCEL FILL");

                        // There's a fill already so cancel fill
                        serverRequests.minusFillInBackground(currentPos.getId());
                        serverRequests.cancelClickInBackground(userLocalStore.getLoggedInUser().getUserID(), currentPos.getId());
                        currentPos.hitFillSecondTime();
                        currentPos.setFillOrKill(-1);
                        holder.imgbFill.setBackgroundResource(R.drawable.fill);
                    } else {

                        if (currentPos.getFillOrKill() == 0) {

                            System.out.println("CLICK FILL. ALREADY HAS KILL");
                            serverRequests.updateFillAndFillColumnInBackground(currentPos.getId());
                            serverRequests.minusKillInBackground(currentPos.getId());

                            // Case the post already has a Kill. Replace the kill with fill. Switching option.
                            currentPos.hitKillSecondTime();
                            holder.numKill.setText("-" + currentPos.getTotalKill());
                            serverRequests.cancelClickInBackground(userLocalStore.getLoggedInUser().getUserID(), currentPos.getId());
                            //serverRequests.updateFillAndCancelKillInBackground(currentPos.getId(), currentPos.getCurrentColumn());
                            holder.imgbKill.setBackgroundResource(R.drawable.kill);

                        } else {
                            System.out.println("FILL NORMAL");

                            // There's no fill so do normal operations
                            serverRequests.updateFillAndFillColumnInBackground(currentPos.getId());
                        }

                        // Add to user list of Fill clicks database table
                        serverRequests.addToFillListInBackground(userLocalStore.getLoggedInUser().getUserID(), currentPos.getId());
                        holder.imgbFill.setBackgroundResource(R.drawable.filled);
                        currentPos.hitFill();
                        currentPos.setFillOrKill(1);
                    }
                }
                holder.numFill.setText("" + (currentPos.getTotalFill()));
                holder.swipeLayout.close();
            }
        });


        holder.imgbKill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UserLocalStore.allowRefresh = true;
                killSound.playSound();

                if (userLocalStore.getLoggedInUser().getUsername().equals("a") ||
                         userLocalStore.getLoggedInUser().getUsername().equals("sealcub22")) {

                    //update fill only. change name
                    serverRequests.updateKillAndKillColumnInBackground(currentPos.getId());
                    currentPos.hitKill();
                    holder.numKill.setText("-" + currentPos.getTotalKill());

                }  else {
                    if (currentPos.getFillOrKill() == 0) {
                        System.out.println("CANCEL KILL");

                        // There's a kill already so cancel kill
                        serverRequests.minusKillInBackground(currentPos.getId());
                        serverRequests.cancelClickInBackground(userLocalStore.getLoggedInUser().getUserID(), currentPos.getId());
                        currentPos.hitKillSecondTime();
                        currentPos.setFillOrKill(-1);
                        holder.imgbKill.setBackgroundResource(R.drawable.kill);
                    } else {

                        if (currentPos.getFillOrKill() == 1) {
                            System.out.println("CLICK KILL. ALREADY HAS FILL");

                            serverRequests.updateKillAndKillColumnInBackground(currentPos.getId());
                            serverRequests.minusFillInBackground(currentPos.getId());

                            // Case the post already has a Kill. Replace the kill with fill. Switching option.
                            currentPos.hitFillSecondTime();
                            holder.numFill.setText("" + currentPos.getTotalFill());

                            serverRequests.cancelClickInBackground(userLocalStore.getLoggedInUser().getUserID(), currentPos.getId());
                            //serverRequests.updateKillAndCancelFillInBackground(currentPos.getId(), currentPos.getCurrentColumn());
                            holder.imgbFill.setBackgroundResource(R.drawable.fill);

                        } else {
                            System.out.println("KILL NORMAL");
                            // There's no fill so do normal operations
                            //update fill only. change name
                            serverRequests.updateKillAndKillColumnInBackground(currentPos.getId());
                        }

                        // Add to user list of Fill clicks database table
                        serverRequests.addToKillListInBackground(userLocalStore.getLoggedInUser().getUserID(), currentPos.getId());
                        holder.imgbKill.setBackgroundResource(R.drawable.killed);
                        currentPos.hitKill();
                        currentPos.setFillOrKill(0);
                    }
                }
                holder.numKill.setText("-" + currentPos.getTotalKill());
                holder.swipeLayout.close();
            }
        });

        holder.tvGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, GraphActivity.class);
                intent.putExtra("postID", currentPos.getId());
                mContext.startActivity(intent);
            }
        });

        holder.tvEditPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText edittext = new EditText(mContext);
                edittext.setText(currentPos.getStatus());

                //editPostDialog.setMessage("Enter Your Message");
                editPostDialog.setTitle("Edit Post");
                editPostDialog.setView(edittext);

                editPostDialog.setPositiveButton("Save Changes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        volleyMainPosts.updateAPost(currentPos.getId(), edittext.getText().toString());
                    }
                });

                editPostDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });
                editPostDialog.show();
            }
        });
        /*
       //  Close the swipeLayout when user click on the post
        holder.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("333");

                    holder.swipeLayout.close();

            }
        });*/

        holder.tvManagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (holder.tvManagePost.getText().toString().equals("Delete")) {
                    volleyMainPosts.deleteAPostVolley(currentPos.getId());
                } else {
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
                swipeIsActive = false;

            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

            }

            @Override
            public void onStartOpen(SwipeLayout layout) {

            }

            @Override
            public void onOpen(SwipeLayout layout) {

            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
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
    public class ViewHolder extends RecyclerView.ViewHolder {

        private SwipeLayout swipeLayout;
        private NetworkImageView profilePic;
        private ImageButton imgbFill, imgbKill;
        private TextView tvGraph, numFill, numKill, txtStatusMsg, timestamp, name, tvManagePost,
                tvEditPost;
        private LinearLayout bottomWrapper1;
        private LinearLayout postLayout;

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
            tvEditPost = (TextView) itemView.findViewById(R.id.tvEditPost);
            bottomWrapper1 = (LinearLayout) itemView.findViewById(R.id.bottom_wrapper1);
            postLayout = (LinearLayout) itemView.findViewById(R.id.postLayout);

            postLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!UserLocalStore.isUserLoggedIn) {
                        Toast.makeText(mContext, "Log in to comment", Toast.LENGTH_LONG).show();
                    } else {
                        int position = getAdapterPosition();
                        FeedItem currentItem = feedItems.get(position);
                        Intent postInfo = new Intent(mContext, CommentActivity.class);
                        postInfo.putExtra("userID", currentItem.getUserID());
                        postInfo.putExtra("name", currentItem.getName());
                        postInfo.putExtra("time", currentItem.getTimeStamp());
                        postInfo.putExtra("numKill", currentItem.getTotalKill());
                        postInfo.putExtra("numFill", currentItem.getTotalFill());
                        postInfo.putExtra("post", currentItem.getStatus());
                        postInfo.putExtra("profilePic", currentItem.getProfilePic());
                        postInfo.putExtra("postID", currentItem.getId());
                        postInfo.putExtra("clickStatus", currentItem.getFillOrKill());
                        postInfo.putExtra("username", currentItem.getUsername());
                        postInfo.putExtra("fillOrkill", currentItem.getFillOrKill());


                        mContext.startActivity(postInfo);
                    }
                }
            });
        }
    }


    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }
}
