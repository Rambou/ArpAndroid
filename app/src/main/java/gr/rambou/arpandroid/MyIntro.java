package gr.rambou.arpandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro;

public class MyIntro extends AppIntro {
    public static final String MyPREFERENCES = "MyPrefs";
    public static SharedPreferences sharedpreferences;

    // Please DO NOT override onCreate. Use init.
    @Override
    public void init(Bundle savedInstanceState) {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        Boolean value = sharedpreferences.getBoolean("intro", false);
        if (value) {
            loadMainActivity();
            this.finish();
        }
        // Add your slide's fragments here.
        // AppIntro will automatically generate the dots indicator and buttons.
        addSlide(new FirstSlide(), getApplicationContext());
        addSlide(new SecondSlide(), getApplicationContext());

        // override bar/separator color if you want.
        setBarColor(Color.parseColor("#3F51B5"));
        setSeparatorColor(Color.parseColor("#2196F3"));

        // hide Skip button
        showSkipButton(false);
    }

    @Override
    public void onSkipPressed() {
        // Do when users tap on Skip button.
        loadMainActivity();
    }

    @Override
    public void onDonePressed() {
        // Do when users tap on Done button.
        loadMainActivity();
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("intro", true);
        editor.commit();
    }

    private void loadMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}