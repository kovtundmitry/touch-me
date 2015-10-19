package tachos.ru.touch.me.data;

import java.util.List;

public class Users {
    private String objectId;
    private String email;
    private String name;
    private long lastOnline;
    private List<Users> likedUsers;

    public String getObjectId() {
        return objectId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public long getLastOnline() {
        return lastOnline;
    }

    public List<Users> getLikedUsers() {
        return likedUsers;
    }
}
