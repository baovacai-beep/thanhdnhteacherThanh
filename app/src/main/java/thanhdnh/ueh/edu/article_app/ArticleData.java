package thanhdnh.ueh.edu.article_app;

import android.app.Activity;
import android.content.Context;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ArticleData {
    private Context context;
    private GridView gridView;

    public ArticleData(Context context, GridView gridView) {
        this.context = context;
        this.gridView = gridView;
    }

    public void loadData(String urlString, Activity activity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    String jsonStr = sb.toString();
                    ArrayList<User> users = new ArrayList<>();

                    JSONArray jsonArray;
                    if (jsonStr.trim().startsWith("[")) {
                        jsonArray = new JSONArray(jsonStr);
                    } else {
                        JSONObject jsonObject = new JSONObject(jsonStr);
                        if (jsonObject.has("users")) {
                            jsonArray = jsonObject.getJSONArray("users");
                        } else if (jsonObject.has("articles")) {
                            jsonArray = jsonObject.getJSONArray("articles");
                        } else {
                            jsonArray = jsonObject.names() != null && jsonObject.names().length() > 0 ?
                                    jsonObject.getJSONArray(jsonObject.names().getString(0)) : new JSONArray();
                        }
                    }

                    // ========================================================
                    // >>> PARSE ĐẦY ĐỦ 5 THUỘC TÍNH CỦA USER THEO ĐỀ BÀI <<<
                    // ========================================================
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject item = jsonArray.getJSONObject(i);

                        String id = item.has("id") ? item.getString("id") :
                                (item.has("article_id") ? item.getString("article_id") : String.valueOf(i + 1));

                        String uname = item.has("uname") ? item.getString("uname") :
                                (item.has("article_title") ? item.getString("article_title") : "user_" + id);

                        String password = item.has("password") ? item.getString("password") : "123456";

                        String url_profile = item.has("url_profile") ? item.getString("url_profile") :
                                (item.has("article_image") ? item.getString("article_image") : "");

                        String short_bio = item.has("short_bio") ? item.getString("short_bio") :
                                (item.has("article_description") ? item.getString("article_description") : "");

                        users.add(new User(id, uname, password, url_profile, short_bio));
                    }
                    // ========================================================

                    ArticleList.setInstance(users);

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArticleAdapter adapter = new ArticleAdapter(context, users);
                            gridView.setAdapter(adapter);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}