package phoenix.idex;

/**
 * Created by Ravinder on 3/2/16.
 */
public class User {

    private String token, firstname, lastname, email, username, password, time;
    private int userID;
    // User for signing up
    public User(String token, String firstname, String lastname, String email, String username, String password, String time) {
        this.token = token;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.username = username;
        this.password = password;
        this.time = time;
    }

    // User for current user storage
    public User(int userID, String token, String firstname, String lastname, String email, String username, String time) {
        this.userID = userID;
        this.token = token;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.username = username;
        this.time = time;
    }

    // User for current user storage
    public User(int userID, String firstname, String lastname, String email, String username, String time) {
        this.userID = userID;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.username = username;
        this.time = time;
    }


    // User for current user storage
    public User(int userID, String firstname, String lastname, String email, String username) {
        this.userID = userID;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.username = username;
    }

    // User for logging in
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.email = "";
        this.firstname = "";
        this.lastname = "";
    }

    // Retrieve the data field of the class
    public String getFirstname() {
        return firstname;
    }
    public String getLastname() {
        return lastname;
    }
    public String getEmail() {
        return email;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public int getUserID() {
        return userID;
    }
    public String getTime() { return time;}
    public String getToken() { return token; }


    public void setFirstName(String firstname) {
        this.firstname = firstname;
    }
    public void setLastName(String lastname) {
        this.lastname = lastname;
    } public void setEmail(String email) {
        this.email = email;
    } public void setUsername(String username) {
        this.username = username;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void serUserID(int userID) {
        this.userID = userID;
    }
    public void setToken(String token) { this.token = token; }

}

