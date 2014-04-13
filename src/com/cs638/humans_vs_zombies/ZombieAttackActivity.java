package com.cs638.humans_vs_zombies;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ZombieAttackActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zombie_attack);
    }

    public void onButtonClick(View view){

        switch (view.getId()){

            case R.id.btnZombieAttack:
                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainActivityIntent);
                finish();
                break;
        }
    }
}
