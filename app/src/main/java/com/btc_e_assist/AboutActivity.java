package com.btc_e_assist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

public class AboutActivity extends ActionBarActivity {
    private final String projectUrl = "https://github.com/alexandersjn/btc-e-assist";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(projectUrl));
        startActivity(browserIntent);
    }
}