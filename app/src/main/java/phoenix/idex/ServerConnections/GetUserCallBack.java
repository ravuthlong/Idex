package phoenix.idex.ServerConnections;

import phoenix.idex.User;

/**
 * Created by Ravinder on 11/19/15.
 */
public interface GetUserCallBack {
    abstract void done(User returnedUser);
}
