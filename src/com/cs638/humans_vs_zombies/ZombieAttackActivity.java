package com.cs638.humans_vs_zombies;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

public class ZombieAttackActivity extends Activity {

    SessionManager session;

    TextView battleOutcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.zombie_attack);

        session = new SessionManager(getApplicationContext());

        battle();
    }

    public void onButtonClick(View view){

        switch (view.getId()){

            case R.id.btnZombieAttack:
                finish();
                break;
        }
    }

    /**
     * Random event to determine if human player is infected in a zombie attack
     */
    private void battle(){
        Random rand = new Random();
        battleOutcomeTextView = (TextView) findViewById(R.id.attackOutcomeTextView);

        if (rand.nextBoolean() == true){
            battleOutcomeTextView.setText("You were infected!");
            battleOutcomeTextView.setTextColor(Color.RED);
            session.updateStatus(true); // Player is now a zombie
        } else {
            battleOutcomeTextView.setText("You were NOT infected!");
            battleOutcomeTextView.setTextColor(Color.GREEN);
        }
    }
}
