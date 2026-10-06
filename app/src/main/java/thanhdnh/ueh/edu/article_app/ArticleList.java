package thanhdnh.ueh.edu.article_app;

import java.util.ArrayList;

public class ArticleList {
    private static ArrayList<User> userList = new ArrayList<>();

    public static ArrayList<User> getInstance() {
        return userList;
    }

    public static void setInstance(ArrayList<User> list) {
        userList = list;
    }

    public static User getUserById(String id) {
        if (userList != null) {
            for (User u : userList) {
                if (u.getId().equals(id)) {
                    return u;
                }
            }
        }
        return null;
    }
}