package com.hq.picup;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class SplashScreen extends ActionBarActivity {
    private final int SPLASH_DISPLAY_LENGTH = 500; //In ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash_screen, menu);



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //launch the next activity
                Intent mapIntent = new Intent(SplashScreen.this, MapScreen.class);
                SplashScreen.this.startActivity(mapIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                SplashScreen.this.finish();

            }
        }, SPLASH_DISPLAY_LENGTH);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
