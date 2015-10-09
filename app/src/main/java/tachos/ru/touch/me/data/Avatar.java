package tachos.ru.touch.me.data;

import com.backendless.Backendless;

//D0A9028F-58C3-E876-FF41-340945A8B900
//https://api.backendless.com/<application id>/<version name>/files/<path>/<file name>
public class Avatar {
    private static String basePathToAvatar = "https://api.backendless.com/" + Backendless.getApplicationId() + "/" + Backendless.getVersion() + "/files/";

    public static String generateFullPathToAva(String id) {
        return basePathToAvatar + generatePathToAva(id) + id + ".jpg";
    }

    public static String generatePathToAva(String id) {
        String path = "avatars/";
        while (id.charAt(0) != '-') {
            path += id.charAt(0) + "/";
            id = id.substring(1);
        }
        return path;
    }
    public static String generateInnerPathToAva(String id) {
        String path = "";
        while (id.charAt(0) != '-') {
            path += id.charAt(0) + "/";
            id = id.substring(1);
        }
        return path;
    }

}
