package uk.co.bitethebullet.android.token;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;

import org.w3c.dom.Text;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class QRCodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String qrUrl = getIntent().getStringExtra("qrUrl");
        String fullName = getIntent().getStringExtra("fullName");

        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3 / 4;


        setContentView(R.layout.activity_q_r_code);

        ImageView qrImage = (ImageView) findViewById(R.id.qr_code_image);
        TextView qrFullName = (TextView) findViewById(R.id.qr_code_full_name);
        qrFullName.setText(fullName);

        QRGEncoder qrgEncoder = new QRGEncoder(qrUrl, null, QRGContents.Type.TEXT, smallerDimension);

        try {
            // Getting QR-Code as Bitmap
             Bitmap bitmap = qrgEncoder.getBitmap();
            // Setting Bitmap to ImageView
            qrImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.e("QRCode", e.getMessage());
        }
    }
}