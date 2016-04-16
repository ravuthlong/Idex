package phoenix.idex.RecyclerViewFeed.MainRecyclerView.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.HashMap;
import java.util.List;

import phoenix.idex.Activities.CommentActivity;
import phoenix.idex.Activities.EditProfileActivity;
import phoenix.idex.Graphing.GraphActivity;
import phoenix.idex.VolleyServerConnections.VolleyConnections;
import phoenix.idex.R;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.app.AppController;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.data.FeedItem;
import phoenix.idex.ServerConnections.ServerRequests;
import phoenix.idex.ServerRequestCallBacks.FetchColumnAndValueCallBack;
import phoenix.idex.ServerRequestCallBacks.PostExecutionCallBack;
import phoenix.idex.UserLocalStore;

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
    HashMap<Integer, String> hashMap;
    private boolean canClickAgain = true;
    private VolleyConnections volleyConnections;

    public FeedListAdapter(Context context, List<FeedItem> feedItems) {
        inflater = LayoutInflater.from(context);
        this.mContext = context;
        this.feedItems = feedItems;
        userLocalStore = new UserLocalStore(mContext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Custom root of recycle view
         this.postView = inflater.inflate(R.layout.item_mainfeed, parent, false);
        // Hold a structure of a view. See class viewholder, which holds the structure

        ViewHolder holder = new ViewHolder(this.postView);

        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        serverRequests = new ServerRequests(mContext);
        volleyConnections = new VolleyConnections(mContext);

        // Initialize fonts
        Typeface killFillFont = Typeface.createFromAsset(mContext.getAssets(), "Menufont.ttf");
        holder.name.setTypeface(killFillFont);

        final FeedItem currentPos = feedItems.get(position);

        holder.name.setText(currentPos.getName());
        holder.tvFillNum.setText("" + (currentPos.getTotalFill()));
        holder.tvKillNum.setText("-" + (currentPos.getTotalKill()));

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
        } else { // default
            holder.profilePic.setImageUrl("http://oi67.tinypic.com/24npqbk.jpg", imageLoader);
        }

        holder.numFill.setText("" + currentPos.getTotalFill());
        holder.numKill.setText("-" + currentPos.getTotalKill());

        holder.imgbFill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (userLocalStore.getLoggedInUser().getUsername().equals("dge93") ||
                        userLocalStore.getLoggedInUser().getUsername().equals("a")) {

                    // There's no fill so do normal operations
                    serverRequests.fetchCurrentColumnInBackground(currentPos.getId(), new FetchColumnAndValueCallBack() {
                        @Override
                        public void columnAndValueCallBack(Pair values) {
                            int currentColumn = (int) values.first;
                            int currentValue = (int) values.second;

                            if (currentColumn >= 20) {
                                serverRequests.updateFillAndResetColumnInBackground(currentPos.getId(), currentColumn);
                            } else {
                                //update fill only. change name
                                serverRequests.updateFillAndFillColumnInBackground(currentPos.getId(), currentColumn);
                            }
                            serverRequests.updateValueInBackground(currentPos.getId(), 2);

                        }
                    });
                    serverRequests.updateCurrentColumnInBackground(currentPos.getId(), new PostExecutionCallBack() {
                        @Override
                        public void postExecution() {
                        }
                    });
                    currentPos.hitFill();
                    holder.tvFillNum.setText("" + (currentPos.getTotalFill()));

                } else {
                    if (currentPos.getFillOrKill() == 1) {
                        // There's a fill already so cancel fill
                        serverRequests.updateValueInBackground(currentPos.getId(), -2);
                        serverRequests.minusFillInBackground(currentPos.getId());
                        serverRequests.cancelClickInBackground(userLocalStore.getLoggedInUser().getUserID(), currentPos.getId());
                        currentPos.hitFillSecondTime();
                        currentPos.setFillOrKill(-1);
                        holder.imgbFill.setBackgroundResource(R.drawable.fill);
                    } else {

                        if (currentPos.getFillOrKill() == 0) {
                            // Case the post already has a Kill. Replace the kill with fill.
                            currentPos.hitKillSecondTime();
                            holder.tvKillNum.setText("-" + currentPos.getTotalKill());
                            serverRequests.cancelClickInBackground(userLocalStore.getLoggedInUser().getUserID(), currentPos.getId());


                            serverRequests.updateFillAndCancelKillInBackground(currentPos.getId(), currentPos.getCurrentColumn());


                            holder.imgbKill.setBackgroundResource(R.drawable.kill);

                        } else {
                            // There's no fill so do normal operations
                            serverRequests.fetchCurrentColumnInBackground(currentPos.getId(), new FetchColumnAndValueCallBack() {
                                @Override
                                public void columnAndValueCallBack(Pair values) {
                                    int currentColumn = (int) values.first;
                                    int currentValue = (int) values.second;

                                    if (currentColumn >= 20) {
                                        serverRequests.updateFillAndResetColumnInBackground(currentPos.getId(), currentColumn);
                                    } else {
                                        //update fill only. change name
                                        serverRequests.updateFillAndFillColumnInBackground(currentPos.getId(), currentColumn);
                                    }
                                    serverRequests.updateValueInBackground(currentPos.getId(), 2);

                                }
                            });
                            serverRequests.updateCurrentColumnInBackground(currentPos.getId(), new PostExecutionCallBack() {
                                @Override
                                public void postExecution() {
                                }
                            });
                        }

                        // Add to user list of Fill clicks database table
                        serverRequests.addToFillListInBackground(userLocalStore.getLoggedInUser().getUserID(), currentPos.getId());
                        holder.imgbFill.setBackgroundResource(R.drawable.filled);
                        currentPos.hitFill();
                        currentPos.setFillOrKill(1);
                    }
                }
                holder.tvFillNum.setText("" + (currentPos.getTotalFill()));
                holder.swipeLayout.close();
            }
        });


        holder.imgbKill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (userLocalStore.getLoggedInUser().getUsername().equals("dge93") ||
                        userLocalStore.getLoggedInUser().getUsername().equals("a")) {
                    // There's no kill yet. Perform normal operations.
                    serverRequests.fetchCurrentColumnInBackground(currentPos.getId(), new FetchColumnAndValueCallBack() {
                        @Override
                        public void columnAndValueCallBack(Pair values) {
                            int currentColumn = (int) values.first;
                            int currentValue = (int) values.second;

                            if (currentValue <= 0) {
                                // Increase kill but set the column as -1, meaning floored with 0
                                serverRequests.updateKillAndFloorColumnInBackground(currentPos.getId(), currentColumn);
                            } else {
                                if (currentColumn >= 20) {
                                    serverRequests.updateKillAndResetColumnInBackground(currentPos.getId(), currentColumn);
                                } else {
                                    //update fill only. change name
                                    serverRequests.updateKillAndKillColumnInBackground(currentPos.getId(), currentColumn);
                                }
                                serverRequests.updateValueInBackground(currentPos.getId(), -1);
                            }
                        }
                    });


                    serverRequests.updateCurrentColumnInBackground(currentPos.getId(), new PostExecutionCallBack() {
                        @Override
                        public void postExecution() {
                        }
                    });
                    currentPos.hitKill();
                    holder.tvKillNum.setText("-" + currentPos.getTotalKill());

                } else {

                    if (currentPos.getFillOrKill() == 0) {
                        // There's already a kill. cancel kill.

                        serverRequests.updateValueInBackground(currentPos.getId(), +1);
                        serverRequests.minusKillInBackground(currentPos.getId());
                        serverRequests.cancelClickInBackground(userLocalStore.getLoggedInUser().getUserID(), currentPos.getId());
                        currentPos.hitKillSecondTime();
                        currentPos.setFillOrKill(-1);
                        holder.imgbKill.setBackgroundResource(R.drawable.kill);

                    } else {
                        // There's no kill yet. Perform normal operations.
                        serverRequests.fetchCurrentColumnInBackground(currentPos.getId(), new FetchColumnAndValueCallBack() {
                            @Override
                            public void columnAndValueCallBack(Pair values) {
                                int currentColumn = (int) values.first;
                                int currentValue = (int) values.second;

                                if (currentColumn >= 20) {
                                    serverRequests.updateKillAndResetColumnInBackground(currentPos.getId(), currentColumn);
                                } else {
                                    //update fill only. change name
                                    serverRequests.updateKillAndKillColumnInBackground(currentPos.getId(), currentColumn);
                                }
                                serverRequests.updateValueInBackground(currentPos.getId(), -1);

                            }
                        });

                        serverRequests.updateCurrentColumnInBackground(currentPos.getId(), new PostExecutionCallBack() {
                            @Override
                            public void postExecution() {
                            }
                        });

                        // Add to user list of Fill clicks database table
                        serverRequests.addToKillListInBackground(userLocalStore.getLoggedInUser().getUserID(), currentPos.getId());
                        holder.imgbKill.setBackgroundResource(R.drawable.killed);
                        currentPos.hitKill();
                        currentPos.setFillOrKill(0);
                    }
                }

                holder.tvKillNum.setText("-" + currentPos.getTotalKill());
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
                    volleyConnections.deleteAPostVolley(currentPos.getId());
                    //serverRequests.deleteAPostInBackground(currentPos.getId());
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
                //postView.setOnClickListener(mContext);
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

        SwipeLayout swipeLayout;
        NetworkImageView profilePic;
        Button bFill, bKill;
        ImageButton imgbFill, imgbKill;
        TextView tvGraph, numFill, numKill, txtStatusMsg, timestamp, name, tvManagePost, tvFillNum, tvKillNum,
                tvEditPost;
        LinearLayout bottomWrapper1;
        LinearLayout postLayout;

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
            tvFillNum = (TextView) itemView.findViewById(R.id.tvFillNum);
            tvKillNum = (TextView) itemView.findViewById(R.id.tvKillNum);
            tvEditPost = (TextView) itemView.findViewById(R.id.tvEditPost);
            bottomWrapper1 = (LinearLayout) itemView.findViewById(R.id.bottom_wrapper1);
            postLayout = (LinearLayout) itemView.findViewById(R.id.postLayout);

            postLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!UserLocalStore.isUserLoggedIn) {
                        System.out.println("HAHAA");
                        Toast.makeText(mContext, "Log in to comment", Toast.LENGTH_LONG).show();
                    } else {
                        System.out.println("WTF");

                        int position = getAdapterPosition();
                        FeedItem currentItem = feedItems.get(position);
                        Intent postInfo = new Intent(mContext, CommentActivity.class);
                        postInfo.putExtra("name", currentItem.getName());
                        postInfo.putExtra("time", currentItem.getTimeStamp());
                        postInfo.putExtra("numKill", currentItem.getTotalKill());
                        postInfo.putExtra("numFill", currentItem.getTotalFill());
                        postInfo.putExtra("post", currentItem.getStatus());
                        postInfo.putExtra("profilePic", currentItem.getProfilePic());
                        postInfo.putExtra("postID", currentItem.getId());
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
