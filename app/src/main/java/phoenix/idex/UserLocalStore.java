package phoenix.idex;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Ravinder on 3/3/16.
 */
public class UserLocalStore {
    public static final String SP_NAME = "userDetails";
    private SharedPreferences userLocalDataStore;
    private static User storedUser;

    public UserLocalStore(Context context) {
        userLocalDataStore = context.getSharedPreferences(SP_NAME, 0);
    }
    public void storeUserData(User user) {
        SharedPreferences.Editor spEditor = userLocalDataStore.edit();
        spEditor.putString("firstname", user.getFirstname());
        spEditor.putString("lastname", user.getLastname());
        spEditor.putString("email", user.getEmail());
        spEditor.putString("username", user.getUsername());
        spEditor.commit();
    }

    public User getLoggedInUser() {
        String firstname = userLocalDataStore.getString("firstname", "");
        String lastname = userLocalDataStore.getString("lastname", "");
        String email = userLocalDataStore.getString("email", "");
        String username = userLocalDataStore.getString("username", "");

        storedUser = new User(firstname, lastname, email, username);
        return storedUser;
    }

    public void setUserLoggedIn(boolean loggedIn) {
        SharedPreferences.Editor spEditor = userLocalDataStore.edit();
        spEditor.putBoolean("loggedIn", loggedIn);
        spEditor.commit();
    }

    public void clearUserData() {
        SharedPreferences.Editor spEditor = userLocalDataStore.edit();
        spEditor.clear();
        spEditor.commit();
    }
}