package phoenix.idex.RecyclerViewFeed.CommentRecyclerView.data;

/**
 * Created by Linh on 2/19/2016.
 */

public class CommentItem {
    private int commentID, postID, recommended, recommendTotalCount;
    private String username, name, comment, profilePic, timeStamp;

    public CommentItem() {
    }

    public CommentItem(int commentID, String username, String name, String comment, String timeStamp) {
        this.commentID = commentID;
        this.username = username;
        this.name = name;
        this.comment = comment;
        this.timeStamp = timeStamp;
    }

    public void minusRecommendTotal() {
        this.recommendTotalCount -= 1;
    }

    public void addRecommendTotal() {
        this.recommendTotalCount += 1;
    }
    public void setRecommendTotalCount(int recommendTotalCount) {
        this.recommendTotalCount = recommendTotalCount;
    }

    public int getRecommendTotalCount() {
        return this.recommendTotalCount;
    }

    public void setRecommended(int recommended) {
        this.recommended = recommended;
    }

    public int getRecommended() {
        return this.recommended;
    }

    public int getPostId() {
        return postID;
    }

    public void setPostId(int id) {
        this.postID = id;
    }

    public void setCommentID(int id) { this.commentID = id;}

    public int getCommentID() { return commentID; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUsername(){
        return this.username;
    }

    public void setUsername(String username){
        this.username = username;
    }
}
