package com.rco.rcotrucks.activities.forms;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.adapters.Cadp;

import java.util.ArrayList;
import java.util.List;

import static com.rco.rcotrucks.adapters.Cadp.EXTRA_PREVIEW;
import static com.rco.rcotrucks.adapters.Cadp.EXTRA_PREVIEW_URL;

public class EvaluationPreviewActivity extends AppCompatActivity {
    private static String TAG = "EvaluationPreview";

    protected WebView mWebView;
    private ImageView back;
    private TextView title, save;
    private String url ;
    List<FormField> javascriptAttributList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluatiion_preview);

        mWebView = findViewById(R.id.evaluation_webview);
        back = findViewById(R.id.btn_back);
        title = findViewById(R.id.tv_title);
        save = findViewById(R.id.textViewSave);
        title.setText("Preview");
        save.setVisibility(View.GONE);

        WebSettings webSettings = mWebView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        List<FormField> spfFieldList = (ArrayList<FormField>) getIntent().getSerializableExtra(EXTRA_PREVIEW);

        for (FormField spfField : spfFieldList) {
            if (spfField.getValue() != null && !spfField.getValue().isEmpty()) {
                javascriptAttributList.add(spfField);
            }
        }
        url = (String) getIntent().getStringExtra(EXTRA_PREVIEW_URL);
        injectJavascript(mWebView, javascriptAttributList);
        mWebView.loadUrl(url);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void injectJavascript(final WebView webView, final List<FormField> javascriptAttributList) {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                StringBuilder sbuf = new StringBuilder("javascript:(function() { var uselessvar;\n");
                genValueSettingJavascript(sbuf, javascriptAttributList);
                sbuf.append("})()");

                Log.d(TAG, "injectJavascript() sbuf=" + sbuf + "\n");
                Log.d(TAG, "injectJavascript() about to call webView.loadUrl()");

                webView.loadUrl(
                        sbuf.toString()
                );
            }
        });
    }

    private static StringBuilder genValueSettingJavascript(StringBuilder sbuf, List<FormField> listItems) {

        for (FormField preview : listItems) {
            switch (preview.getFormat()) {
                case Cadp.HTML_ELEM_TYPE_SPAN:
                    sbuf.append("uselessvar = document.getElementById('").append(preview.getScriptKey()).append("').innerHTML='").append(preview.getValue()).append("';").append("\n");
                    break;
                case Cadp.HTML_ELEM_TYPE_TEXT:
                    sbuf.append("uselessvar = document.getElementById('").append(preview.getScriptKey()).append("').value='").append(preview.getValue()).append("';").append("\n");
                    break;
                case Cadp.HTML_ELEM_TYPE_CHECKBOX:
                    sbuf.append("uselessvar = document.getElementById('").append(preview.getScriptKey()).append("').checked=").append(preview.getValue()).append(";").append("\n");
                    break;
                case Cadp.HTML_ELEM_TYPE_BITMAP:
                    sbuf.append("uselessvar = document.getElementById(\"").append(preview.getScriptKey()).append("\").src = \"data:image/png;base64, ").append(preview.getValue()).append("\";").append("\n");
                    break;
                case Cadp.HTML_FORMAT_DATE:
                case Cadp.HTML_FORMAT_TIME_FROM_DATE:
                case Cadp.HTML_FORMAT_AMPM_FROM_DATE:
                    sbuf.append("uselessvar = document.getElementById('").append(preview.getScriptKey()).append("').value='").append(preview.getValue()).append("';").append("\n");
                    break;
            }
        }

        return sbuf;
    }
}
