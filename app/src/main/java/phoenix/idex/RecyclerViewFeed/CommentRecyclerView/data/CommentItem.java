package phoenix.idex.RecyclerViewFeed.CommentRecyclerView.data;

/**
 * Created by Linh on 2/19/2016.
 */

public class CommentItem {
    private int commentID;
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


    public int getId() {
        return commentID;
    }

    public void setId(int id) {
        this.commentID = id;
    }

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
