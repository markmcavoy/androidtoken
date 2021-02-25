package uk.co.bitethebullet.android.token;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class About  extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        setupHyperlink();
    }

    public void closeHandler(View v){
        finish();
    }

    private void setupHyperlink() {
        TextView linkTextView = findViewById(R.id.about_url);
        TextView linkZalabria = findViewById(R.id.about_info);

        linkTextView.setMovementMethod(LinkMovementMethod.getInstance());
        linkZalabria.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
