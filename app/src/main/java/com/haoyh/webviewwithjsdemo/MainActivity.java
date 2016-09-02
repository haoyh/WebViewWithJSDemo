package com.haoyh.webviewwithjsdemo;

import android.content.Context;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.util.EncodingUtils;

public class MainActivity extends AppCompatActivity {

    private TextView mTvShowInfo; // 显示交互的数据
    private WebView mWbPage; // webview控件

    private String mLoadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
//        mWbPage.loadUrl("http://statictest.jinrongbaguanv.com/articles/article-100252-6475.html");
        mLoadUrl = "http://www.baidu.com";
        mWbPage.loadUrl(mLoadUrl);
    }

    private void initViews() {
        mTvShowInfo = (TextView) findViewById(R.id.tv_show_info);
        RelativeLayout rlShowWebView = (RelativeLayout) findViewById(R.id.rl_show_webview);
        mWbPage = new WebView(this);
        rlShowWebView.addView(mWbPage); //添加webview,显示到界面上
        // webview 设置
        WebSettings webSettings = mWbPage.getSettings();
        webSettings.setJavaScriptEnabled(true); // 支持JS

        // 需要实现效果,及对应解决办法

        // 1.加载的时候会跳出webview,到系统浏览器,对于特殊情况可以自定义WebViewClient去处理加载网页操作
        mWbPage.setWebViewClient(new WebViewClient());
        // 2.有些请求跟用户相关,需要添加token
        String tokenStr = "xxxxxxx";
        setWebViewToken(mLoadUrl, tokenStr);
        // 3.添加user-agent
        String userAgentStr = webSettings.getUserAgentString(); // 获取原有的user-agent
        webSettings.setUserAgentString(userAgentStr + ";-xxxx"); // 添加自定义的user-agent,格式: 分号内容
        // 4.JS显示alert方法不调用
        mWbPage.setWebChromeClient(new WebChromeClient());
        // 5.JS同App交互,互传数据
        /**
         * 对应JS的写法
         * window.InterfaceWithApp.showToast("test App Toast");
         */
        mWbPage.addJavascriptInterface(new JSInterface(this), "InterfaceWithApp"); // InterfaceWithApp是同前端协定好的
        // 6.在网页中发送post请求
        String url = "";
        String urlParam = "";
        mWbPage.postUrl(url, EncodingUtils.getBytes(urlParam, "BASE64")); // url:需要访问的ur; urlParam: 参数

        // 7.加载https请求可能会遇到不能成功显示的问题,由于安全机制的原因导致,具体可查看api文档
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }

    private void setWebViewToken(String url, String tokenStr) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        CookieManager cookieManager = CookieManager.getInstance();
        String tempCookie = "wallet_token=" + tokenStr;
        cookieManager.setCookie(url, tempCookie);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.flush();
        } else {
            CookieSyncManager syncManager = CookieSyncManager.createInstance(this);
            syncManager.sync();
        }
    }

    // 定义交互方法的类
    class JSInterface {

        private Context mContext;

        public JSInterface(Context context) {
            this.mContext = context;
        }

        @JavascriptInterface // 注解一定要写,
        public void showToast(String infoStr) { // 该方法就是前端代码中调用的方法名,需要注意参数类型
            Toast.makeText(mContext, infoStr, Toast.LENGTH_LONG).show();
        }

    }

}
