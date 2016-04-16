package phoenix.idex.RecyclerViewFeed.CommentRecyclerView.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.List;

import phoenix.idex.Activities.EditProfileActivity;
import phoenix.idex.R;
import phoenix.idex.RecyclerViewFeed.CommentRecyclerView.data.CommentItem;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.app.AppController;
import phoenix.idex.ServerConnections.ServerRequests;
import phoenix.idex.UserLocalStore;

/**
 * Created by Ravinder on 2/25/16.
 */
public class CommentListAdapter extends RecyclerSwipeAdapter<CommentListAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater inflater;
    private List<CommentItem> commentItems;
    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();;
    private ServerRequests serverRequests;
    private UserLocalStore userLocalStore;
    private View postView;

    public CommentListAdapter(Context context, List<CommentItem> commentItems) {
        inflater = LayoutInflater.from(context);
        this.mContext = context;
        this.commentItems = commentItems;
        userLocalStore = new UserLocalStore(mContext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Custom root of recycle view
         this.postView = inflater.inflate(R.layout.item_comment, parent, false);
        // Hold a structure of a view. See class viewholder, which holds the structure
        return new ViewHolder(this.postView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        serverRequests = new ServerRequests(mContext);

        // Initialize fonts
        Typeface killFillFont = Typeface.createFromAsset(mContext.getAssets(), "Menufont.ttf");
        holder.name.setTypeface(killFillFont);

        final CommentItem currentPos = commentItems.get(position);
        holder.name.setText(currentPos.getName());
        holder.txtComment.setText(currentPos.getComment());

        // Converting timestamp into x ago format
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                Long.parseLong(currentPos.getTimeStamp()),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        holder.timestamp.setText(timeAgo);


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

        holder.tvGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.tvManagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (holder.tvManagePost.getText().toString().equals("Delete")) {

                    serverRequests.deleteAPostInBackground(currentPos.getId());
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
        return commentItems.size();
    }

    // Holder knows and references where the fields are
    public class ViewHolder extends RecyclerView.ViewHolder {
        SwipeLayout swipeLayout;
        NetworkImageView profilePic;
        TextView tvGraph, txtComment, timestamp, name, tvManagePost, tvEditPost;
        LinearLayout bottomWrapper1;
        LinearLayout commentLayout;
        TextView tvRecommend;

        public ViewHolder(View itemView) {
            super(itemView);

            txtComment = (TextView) itemView.findViewById(R.id.txtComment);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipeCommentLayout);
            tvGraph = (TextView) itemView.findViewById(R.id.tvGraph);
            tvManagePost = (TextView) itemView.findViewById(R.id.tvManagePost);
            profilePic = (NetworkImageView) itemView.findViewById(R.id.profilePicComment);
            name = (TextView) itemView.findViewById(R.id.nameComment);
            timestamp = (TextView) itemView.findViewById(R.id.timestampComment);
            tvEditPost = (TextView) itemView.findViewById(R.id.tvEditPost);
            bottomWrapper1 = (LinearLayout) itemView.findViewById(R.id.bottom_wrapper1);
            commentLayout = (LinearLayout) itemView.findViewById(R.id.commentLayout);
            tvRecommend = (TextView) itemView.findViewById(R.id.tvRecommend);

            tvRecommend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "Adding to recommend...", Toast.LENGTH_SHORT).show();
                }
            });

        }

    }
    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipeCommentLayout;
    }
}
