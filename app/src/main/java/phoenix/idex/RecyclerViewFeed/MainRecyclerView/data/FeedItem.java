package phoenix.idex.RecyclerViewFeed.MainRecyclerView.data;

import java.util.ArrayList;

/**
 * Created by Linh on 2/19/2016.
 */

public class FeedItem {
    private int id;
    private String username, name, status, profilePic, timeStamp;
    private ArrayList<Integer> fillArray = new ArrayList<>();
    private ArrayList<Integer> killArray = new ArrayList<>();
    private int totalFill;
    private int totalKill;
    private int currentColumn; // if 10, reset to 0 and update all fill/kill columns to 0
    private double value;
    private int fillOrKill; // Fill has value 1 and kill has value 0

    public FeedItem() {
    }

    public FeedItem(int id, String username, String name, String status, String timeStamp, double value) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.status = status;
        this.timeStamp = timeStamp;
        this.value = value;
    }

    public void setCurrentColumn(int currentColumn) {
        this.currentColumn = currentColumn;
    }

    public int getCurrentColumn() {
        return currentColumn;
    }

    public void setFillOrKill(int fillOrKill) {
        this.fillOrKill = fillOrKill;
    }
    public void setOneFill(int numFill) {
        fillArray.add(0, numFill);
    }
    public void setTwoFill(int numFill) {
        fillArray.add(1, numFill);
    }public void setThreeFill(int numFill) {
        fillArray.add(numFill);
    }public void setFourFill(int numFill) {
        fillArray.add(numFill);
    }public void setFiveFill(int numFill) {
        fillArray.add(numFill);
    }public void setSixFill(int numFill) {
        fillArray.add(numFill);
    }public void setSevenFill(int numFill) {
        fillArray.add(numFill);
    }public void setEightFill(int numFill) {
        fillArray.add(numFill);
    }public void setNineFill(int numFill) {
        fillArray.add(numFill);
    }public void setTenFill(int numFill) {
        fillArray.add(numFill);
    }

    public void setOneKill(int numFill) {
        killArray.add(0, numFill);
    }
    public void setTwoKill(int numFill) {
        killArray.add(numFill);
    }public void setThreeKill(int numFill) {
        killArray.add(numFill);
    }public void setFourKill(int numFill) {
        killArray.add(numFill);
    }public void setFiveKill(int numFill) {
        killArray.add(numFill);
    }public void setSixKill(int numFill) {
        killArray.add(numFill);
    }public void setSevenKill(int numFill) {
        killArray.add(numFill);
    }public void setEightKill(int numFill) {
        killArray.add(numFill);
    }public void setNineKill(int numFill) {
        killArray.add(numFill);
    }public void setTenKill(int numFill) {
        killArray.add(numFill);
    }

    public void setTotalFill(int totalFill) {
        this.totalFill = totalFill;
    }

    public void setTotalKill(int totalKill) {
        this.totalKill = totalKill;
    }

    public int getTotalFill() {
        return totalFill;
    }
    public int getTotalKill() {
        return totalKill;
    }
    public int getFillOrKill() { return fillOrKill; }

    public ArrayList<Integer> getFillArray() {
        return fillArray;
    }
    public ArrayList<Integer> getKillArray() {
        return killArray;
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

    public void insertFill(int column) {

    }

    public void insertKill() {

    }

    public void hitKill(){
        totalKill++;
    }
    public void hitFill(){
       totalFill++;
    }

    public void hitKillSecondTime() { totalKill--; }
    public void hitFillSecondTime() { totalFill--; }

/*
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
    }*/

}
