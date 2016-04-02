package phoenix.idex;

import android.view.View;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import phoenix.idex.RecyclerViewFeed.MainRecyclerView.adapter.FeedListAdapter;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.data.FeedItem;

/**
 * Created by Ravinder on 4/1/16.
 */
public class JSONParser {
    private FeedListAdapter feedListAdapter;
    private List<FeedItem> feedItems;
    private ProgressBar spinner;

    public JSONParser(FeedListAdapter feedListAdapter, List<FeedItem> feedItems, ProgressBar spinner){
        this.feedItems = feedItems;
        this.feedListAdapter = feedListAdapter;
        this.spinner = spinner;
    }

    // Populate FeedItem Array with JSONArray from PHP
    public void parseJsonFeed(JSONObject response) {
        try {
            JSONArray feedArray = response.getJSONArray("feed");

            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);

                FeedItem item = new FeedItem();
                String name;
                name = feedObj.getString("firstname") + " " + feedObj.getString("lastname");
                item.setId(feedObj.getInt("postID"));
                item.setName(name);
                item.setUsername(feedObj.getString("username"));
                item.setStatus(feedObj.getString("post"));
                item.setProfilePic(feedObj.getString("userpic"));
                item.setTimeStamp(feedObj.getString("timeStamp"));
                item.setCurrentColumn(feedObj.getInt("currentColumn"));
                item.setOneFill(feedObj.getInt("oneFill"));
                item.setTwoFill(feedObj.getInt("twoFill"));
                item.setThreeFill(feedObj.getInt("threeFill"));
                item.setFourFill(feedObj.getInt("fourFill"));
                item.setFiveFill(feedObj.getInt("fiveFill"));
                item.setSixFill(feedObj.getInt("sixFill"));
                item.setSevenFill(feedObj.getInt("sevenFill"));
                item.setEightFill(feedObj.getInt("eightFill"));
                item.setNineFill(feedObj.getInt("nineFill"));
                item.setTenFill(feedObj.getInt("tenFill"));
                item.setOneKill(feedObj.getInt("oneKill"));
                item.setTwoKill(feedObj.getInt("twoKill"));
                item.setThreeKill(feedObj.getInt("threeKill"));
                item.setFourKill(feedObj.getInt("fourKill"));
                item.setFiveKill(feedObj.getInt("fiveKill"));
                item.setSixKill(feedObj.getInt("sixKill"));
                item.setSevenKill(feedObj.getInt("sevenKill"));
                item.setEightKill(feedObj.getInt("eightKill"));
                item.setNineKill(feedObj.getInt("nineKill"));
                item.setTenKill(feedObj.getInt("tenKill"));
                item.setTotalFill(feedObj.getInt("totalFill"));
                item.setTotalKill(feedObj.getInt("totalKill"));

                try{
                    item.setFillOrKill(feedObj.getInt("status"));
                } catch (JSONException e) {
                    item.setFillOrKill(-1); // Default value for posts user never clicked fill nor kill
                }
                //item.setFill(feedObj.getInt("fill"));
                //item.setKill(feedObj.getInt("kill"));
                //item.setValue();
                feedItems.add(item);
            }
            // notify data changes to list adapater
            feedListAdapter.notifyDataSetChanged();
            //progressDialog.hide();
            spinner.setVisibility(View.GONE);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
