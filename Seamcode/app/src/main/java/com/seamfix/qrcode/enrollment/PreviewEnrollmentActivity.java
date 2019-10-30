package com.seamfix.qrcode.enrollment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.seamfix.qrcode.HomeActivity;
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

import static android.view.View.VISIBLE;

public class PreviewEnrollmentActivity extends AppCompatActivity {

    private LinearLayout printLayout;
    private WebView webView;
    private DetailWebView detailWebView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.print_preview_layout);
        printLayout = findViewById(R.id.custom_toast_layout_id);
        webView = findViewById(R.id.web_view);

        webView.getSettings().setDomStorageEnabled(true);
        detailWebView = new DetailWebView();
        Button printBtn = findViewById(R.id.ok);
        Button exitBtn = findViewById(R.id.exit);

        exitBtn.setOnClickListener(v -> exitDialogWarning());
        printBtn.setOnClickListener(v -> saveImage(webView));

        showProgressDialog("Preparing Preview", false);
        Thread thread = new Thread(){
            @Override
            public void run(){
                prepareAndShowPreview();
            }
        };
        thread.start();
    }


    private void prepareAndShowPreview(){
        try {
            InputStream in = this.getAssets().open("jamb3.html");
            String html = FileUtils.getStringfromInputStream(in);

            String imageData = DataSession.getInstance().getTextData().get(R.layout.enrollment_activity_camera);
            String image     = String.format("data:image/png;base64,%s", imageData);
            Bitmap icon      = DataSession.getInstance().getQrBitmap();

            byte[] qrData   = PictUtil.convertBitmapToByteArray(icon);
            String qr       = Base64.encodeToString(qrData, Base64.NO_WRAP);
            String webQr    = String.format("data:image/png;base64,%s", qr);

            /*
            Generate and insert Qr code
             */
            Document doc = Jsoup.parse(html, "utf-8");
            Element divTag = doc.getElementById("userImage");
            divTag.attr("src", image);

            Element divQrTag = doc.getElementById("qrCode");
            divQrTag.attr("src", webQr);

            String firstName = DataSession.getInstance().getTextData().get(R.id.field_first_name);
            if(firstName != null) {
                Element firstNameTag = doc.getElementById("firstName");
                firstNameTag.text(firstName);
            }

            String lastName = DataSession.getInstance().getTextData().get(R.id.field_last_name);
            if(lastName != null) {
                Element tag = doc.getElementById("lastName");
                tag.text(lastName);
            }

            String regNum = DataSession.getInstance().getTextData().get(R.id.field_registration_number);
            if(regNum != null) {
                Element tag = doc.getElementById("regNumber");
                tag.text(regNum);
            }

            String finalHtml = doc.toString();
            System.out.println(doc.toString());

            webView.post(() -> {
                webView.setWebViewClient(detailWebView);
                webView.loadData(finalHtml, "text/html", null);
                stopProgressDialog();
            });

        } catch (IOException e) {
            e.printStackTrace();
            webView.post(this::stopProgressDialog);
        }
    }


    public void saveImage(WebView view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        int iHeight = bitmap.getHeight();
        canvas.drawBitmap(bitmap, 0, iHeight, paint);
        view.draw(canvas);

        String fileDirectory = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/qrs");
        File dir = new File(fileDirectory);
        boolean isExist = dir.exists() || dir.mkdir();
        if(isExist) {
            String fileName = "IMG-" + System.currentTimeMillis() + ".jpg";
            File tempFile = new File(fileDirectory.concat(File.separator).concat(fileName));
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                Log.i("SAVE", "Image saved");
                Toast.makeText(this, "Image saved to gallery successfully", Toast.LENGTH_SHORT).show();
                exitEnrollment();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void exitDialogWarning(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.text_cancel_save);
        builder.setMessage(R.string.text_message_exit_enrollment);
        builder.setPositiveButton(R.string.text_yes_cancel, (dialog, which) -> {
            exitEnrollment();
        });
        builder.setNegativeButton(R.string.text_just_kidding, null);
        builder.show();
    }

    private void exitEnrollment(){
        DataSession.getInstance().destroy();
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public class DetailWebView extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            printLayout.setVisibility(VISIBLE);
            webView = view;
        }
    }

    public void showProgressDialog(String message, boolean cancel) {
        if (!this.isFinishing() && !this.isDestroyed()) {
            stopProgressDialog();
            if(progressDialog == null){
                progressDialog = new ProgressDialog(this);
            }
            progressDialog.setMessage(message);
            progressDialog.setCancelable(cancel);
            progressDialog.show();
        }
    }

    public void stopProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
