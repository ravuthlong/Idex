package phoenix.idex.RecyclerViewFeed.MainRecyclerView.data;

/**
 * Created by Linh on 2/19/2016.
 */

public class FeedItem {
    private int id;
    private String username, name, status, profilePic, timeStamp;
    // new variable here
    private int fill, kill;
    private double value;

    public FeedItem() {
    }

    public FeedItem(int id, String username, String name, String status,
                    String profilePic, String timeStamp, int fill, int kill, double value) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.status = status;
        this.profilePic = profilePic;
        this.timeStamp = timeStamp;
        this.fill = fill;
        this.kill = kill;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    // new method start here
    public String getUsername(){
        return this.username;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public void setFill(int fill){
        this.fill = fill;
    }

    public int getFill(){return this.fill;}

    public void hitFill(){
        this.fill++;
        this.value++;
    }

    public void setKill(int kill){
        this.kill = kill;
    }

    public int getKill(){
        return this.kill;
    }

    public void hitKill(){
        this.kill++;
        if(this.value > 0){
            this.value -= 0.25;
        }
    }
    public void setValue(){
        this.value = 9 + ((this.fill - (this.kill*0.25)));
    }
    public double getValue(){
        return this.value;
    }

}
