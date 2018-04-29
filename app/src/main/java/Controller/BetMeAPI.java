package Controller;

import java.util.HashMap;
import java.util.Map;

import Models.User;



public class BetMeAPI {

private static Map<String, User> users = new HashMap<>();




public static User getUserByUsername(String username)
{
    return users.get(username);
}





}
