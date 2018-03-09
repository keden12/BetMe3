package Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Models.Bets;
import Models.User;
import app.betme.betme.MainActivity;
import app.betme.betme.R;

/**
 * Created by Kamil on 2018-02-09.
 */

public class BetMeAPI {

private static Map<String, User> users = new HashMap<>();
private static Map<Long, Bets> bets = new HashMap<>();



public static String PACKAGE_NAME;

public static User addUser(String username, String password, String email, Double balance, Long bets)
{
    User user = new User(username,password,email,balance,Long.valueOf(0));
    users.put(user.username,user);

    return user;

}

public static User getUserByUsername(String username)
{
    return users.get(username);
}

//Login check
public static boolean authenticate(String username, String password)
    {

        if (users.containsKey(username))
        {
            User user = users.get(username);
            if (user.password.matches(password))
            {
                return true;
            }
        }
        return false;
    }


}
