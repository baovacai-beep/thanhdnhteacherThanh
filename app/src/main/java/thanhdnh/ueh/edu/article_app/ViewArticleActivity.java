package thanhdnh.ueh.edu.article_app;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class ViewArticleActivity extends AppCompatActivity {

  private ImageView imgProfile;
  private TextView tvUname;
  private TextView tvBio;
  private Handler mainHandler = new Handler(Looper.getMainLooper());

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_view_article);

    if (getSupportActionBar() != null) {
      getSupportActionBar().hide();
    }

    // Khớp đúng ID trong activity_view_article.xml của thầy
    imgProfile = findViewById(R.id.iv_detail);
    tvUname = findViewById(R.id.tv_detail_title);
    tvBio = findViewById(R.id.tv_detail_description);

    User user = null;

    // Lấy User được gửi từ MainActivity
    if (getIntent().hasExtra("user")) {
      user = (User) getIntent().getSerializableExtra("user");
    } else if (getIntent().hasExtra("id")) {
      String id = getIntent().getStringExtra("id");
      user = ArticleList.getUserById(id);
    }

    if (user != null) {
      if (tvUname != null) {
        tvUname.setText(user.getUname());
      }

      if (tvBio != null) {
        String detailInfo = "User ID: " + user.getId() + "\n"
                + "Password: " + user.getPassword() + "\n\n"
                + "Short Bio:\n" + user.getShort_bio();
        tvBio.setText(detailInfo);
      }

      // Tải ảnh lớn qua Downloader
      String imgUrl = user.getUrl_profile();
      if (imgProfile != null && imgUrl != null && !imgUrl.isEmpty()) {
        new Thread(() -> {
          File file = Downloader.downloadFile(imgUrl, getCacheDir());
          if (file != null) {
            mainHandler.post(() -> {
              imgProfile.setImageURI(Uri.fromFile(file));
            });
          }
        }).start();
      }
    }
  }
}