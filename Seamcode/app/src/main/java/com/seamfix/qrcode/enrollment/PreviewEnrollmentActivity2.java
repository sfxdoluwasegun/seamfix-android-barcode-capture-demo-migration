package com.seamfix.qrcode.enrollment;


import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import com.izettle.html2bitmap.Html2Bitmap;
import com.izettle.html2bitmap.content.WebViewContent;
import com.seamfix.seamcode.R;
import com.sf.bio.lib.PictUtil;
import com.sf.bio.lib.util.FileUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PreviewEnrollmentActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.preview_enrollment);
        ImageView enrollmentPreview = findViewById(R.id.id_enrollment_preview);

        try {
            InputStream in = this.getAssets().open("jamb.html");
            String html = FileUtils.getStringfromInputStream(in);


            String imageData = DataSession.getInstance().getTextData().get(R.layout.enrollment_activity_camera);
            String image     = String.format("data:image/png;base64,%s", imageData);

            Bitmap icon     = DataSession.getInstance().getQrBitmap();
            byte[] qrData   = PictUtil.convertBitmapToByteArray(icon);
            String qr       = Base64.encodeToString(qrData, Base64.NO_WRAP);
            String webQr    = String.format("data:image/png;base64,%s", qr);

//            Bitmap icon     = BitmapFactory.decodeResource(getResources(), R.drawable.qr_sample);
//            byte[] qrData   = PictUtil.convertBitmapToByteArray(icon);
//            String qr       = Base64.encodeToString(qrData, Base64.NO_WRAP);
//            String webQr    = String.format("data:image/png;base64,%s", qr);


            Document doc = Jsoup.parse(html, "utf-8");
            Element divTag = doc.getElementById("userImage");
            divTag.attr("src", image);

            Element divQrTag = doc.getElementById("qrCode");
            divQrTag.attr("src", webQr);

            String finalHtml = doc.toString();
            System.out.println(doc.toString());




//            Bitmap bitmap = new Html2Bitmap.Builder().setContext(this).setContent(WebViewContent.html(html)).build().getBitmap();
//            enrollmentPreview.setImageBitmap(bitmap);
            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... voids) {
                    //String html = "<html><body><p>Hello world!</p><br/>Html bitmap</body><html>";
                    //InputStream in = PreviewEnrollmentActivity.this.getAssets().open("preview.html");
                    //String html = FileUtils.getStringfromInputStream(in);

                    Html2Bitmap.Builder builder =  new Html2Bitmap.Builder();
                    //builder.setBitmapWidth(View.MeasureSpec.UNSPECIFIED);
                    Bitmap bitmap = builder.setContext(PreviewEnrollmentActivity2.this).setContent(WebViewContent.html(finalHtml)).build().getBitmap();

                    String fileDirectory = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/qrs");
                    File dir = new File(fileDirectory);
                    boolean isExist = dir.exists() || dir.mkdir();
                    if(isExist) {
                        String fileName = "IMG-" + System.currentTimeMillis() + ".jpg";
                        File tempFile = new File(fileDirectory.concat(File.separator).concat(fileName));
                        try (FileOutputStream out = new FileOutputStream(tempFile)) {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                            // PNG is a lossless format, the compression factor (100) is ignored
                            Log.i("SAVE", "Image saved");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return bitmap;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    if (bitmap != null) {
                        enrollmentPreview.setImageBitmap(bitmap);
                    }
                }
            }.execute();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
