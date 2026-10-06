package thanhdnh.ueh.edu.article_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class ArticleAdapter extends BaseAdapter {
  private Context context;
  private ArrayList<User> userList;
  private Handler mainHandler = new Handler(Looper.getMainLooper());

  public ArticleAdapter(Context context, ArrayList<User> userList) {
    this.context = context;
    this.userList = userList;
  }

  @Override
  public int getCount() {
    return userList != null ? userList.size() : 0;
  }

  @Override
  public Object getItem(int position) {
    return userList.get(position);
  }

  @Override
  public long getItemId(int position) {
    try {
      return Long.parseLong(userList.get(position).getId());
    } catch (Exception e) {
      return position;
    }
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    if (convertView == null) {
      convertView = LayoutInflater.from(context).inflate(R.layout.article_disp_tpl, parent, false);
      holder = new ViewHolder();
      holder.imageView = convertView.findViewById(R.id.imv_photo);
      holder.textView = convertView.findViewById(R.id.tv_title);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    User user = userList.get(position);

    if (holder.textView != null) {
      holder.textView.setText(user.getUname());
    }

    if (holder.imageView != null) {
      holder.imageView.setImageDrawable(null);
      String imgUrl = user.getUrl_profile();
      holder.imageView.setTag(imgUrl); // Gắn cờ kiểm tra ô hiển thị

      if (imgUrl != null && !imgUrl.isEmpty()) {
        new Thread(() -> {
          try {
            File file = Downloader.downloadFile(imgUrl, context.getCacheDir());
            if (file != null) {
              // Nén nhỏ về cỡ 300x300 vừa vặn với ô GridView 150dp
              Bitmap bitmap = Downloader.decodeSampledBitmap(file, 300, 300);
              if (bitmap != null) {
                mainHandler.post(() -> {
                  if (imgUrl.equals(holder.imageView.getTag())) {
                    holder.imageView.setImageBitmap(bitmap);
                  }
                });
              }
            }
          } catch (Throwable ignored) {
            // Bắt mọi lỗi ngoại lệ và thiếu RAM, bảo đảm app không bao giờ văng
          }
        }).start();
      }
    }

    return convertView;
  }

  static class ViewHolder {
    ImageView imageView;
    TextView textView;
  }
}