package thanhdnh.ueh.edu.article_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
  public GridView gridview;

  private AdapterView.OnItemClickListener onitemclick = new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      User selectedUser = (User) gridview.getAdapter().getItem(position);
      Intent intent = new Intent(MainActivity.this, ViewArticleActivity.class);
      intent.putExtra("id", selectedUser.getId());
      intent.putExtra("user", selectedUser);
      startActivity(intent);
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (getSupportActionBar() != null) {
      getSupportActionBar().hide();
    }

    gridview = findViewById(R.id.gridview);

    String jsonUrl = "https://raw.githubusercontent.com/thanhdnh/json/main/products.json";
    new ArticleData(getBaseContext(), gridview).loadData(jsonUrl, this);

    gridview.setOnItemClickListener(onitemclick);
  }
}