package thanhdnh.ueh.edu.article_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class Downloader {
  public static String cached_file_path = "";
  private static OkHttpClient client = new OkHttpClient();

  // Tải và lưu cache theo tên hash để không bị tải đi tải lại
  public static File downloadFile(String url, File cachedDir) {
    if (url == null || url.trim().isEmpty()) return null;
    url = url.replace("\\/", "/"); // Chuẩn hóa URL nếu có dấu gạch chéo ngược

    String filename = "img_" + Math.abs(url.hashCode()) + ".jpg";
    File file = new File(cachedDir, filename);

    // Nếu ảnh đã tải về trước đó thì dùng luôn, không tải lại
    if (file.exists() && file.length() > 0) {
      return file;
    }

    Request request = new Request.Builder().url(url).build();
    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful() || response.body() == null) return null;

      BufferedSink sink = Okio.buffer(Okio.sink(file));
      sink.writeAll(response.body().source());
      sink.close();
      return file;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  // Hàm giải mã ảnh nén theo kích thước view để TRÁNH TRÀN BỘ NHỚ RAM (OOM)
  public static Bitmap decodeSampledBitmap(File file, int reqWidth, int reqHeight) {
    if (file == null || !file.exists()) return null;
    try {
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;
      BitmapFactory.decodeFile(file.getAbsolutePath(), options);

      options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
      options.inJustDecodeBounds = false;
      options.inPreferredConfig = Bitmap.Config.RGB_565; // Giảm 50% dung lượng RAM

      return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    } catch (Throwable t) {
      t.printStackTrace();
      return null;
    }
  }

  private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
    int height = options.outHeight;
    int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {
      final int halfHeight = height / 2;
      final int halfWidth = width / 2;
      while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
        inSampleSize *= 2;
      }
    }
    return inSampleSize;
  }

  public static void downloadWithProgress(String inputurl, Handler mainHandler, Context context, File where2store, ProgressBar progressBar, ImageView imageView) {
    Request request = new Request.Builder().url(inputurl).build();
    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        mainHandler.post(() -> {
          if (progressBar != null) progressBar.setVisibility(ProgressBar.INVISIBLE);
        });
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (!response.isSuccessful() || response.body() == null) {
          mainHandler.post(() -> {
            if (progressBar != null) progressBar.setVisibility(ProgressBar.INVISIBLE);
          });
          return;
        }

        long totalBytes = response.body().contentLength();
        InputStream inputStream = response.body().byteStream();
        String contentType = response.header("Content-Type", "");
        String extension = getExtensionFromMimeType(contentType);

        try (OutputStream outputStream = new FileOutputStream(where2store + "/downloaded_file" + extension)) {
          byte[] buffer = new byte[1024];
          long downloadedBytes = 0;
          int bytesRead;

          while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            downloadedBytes += bytesRead;
            int progress = (int) ((downloadedBytes * 100) / totalBytes);
            mainHandler.post(() -> {
              if (progressBar != null) progressBar.setProgress(progress);
            });
          }
          outputStream.flush();

          mainHandler.post(() -> {
            cached_file_path = where2store + "/downloaded_file" + extension;
            Bitmap bitmap = decodeSampledBitmap(new File(cached_file_path), 500, 500);
            if (bitmap != null) {
              imageView.setImageBitmap(bitmap);
            }
            if (progressBar != null) progressBar.setVisibility(ProgressBar.INVISIBLE);
          });
        } catch (Exception e) {
          mainHandler.post(() -> {
            if (progressBar != null) progressBar.setVisibility(ProgressBar.INVISIBLE);
          });
        }
      }
    });
  }

  private static String getExtensionFromMimeType(String mimeType) {
    Map<String, String> mimeMap = new HashMap<>();
    mimeMap.put("image/jpeg", ".jpg");
    mimeMap.put("image/png", ".png");
    mimeMap.put("application/json", ".json");
    return mimeMap.getOrDefault(mimeType, ".jpg");
  }
}