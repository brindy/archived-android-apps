package uk.org.brindy.d20tools;

import android.app.TabActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;

public class InitialActivity extends TabActivity {

	private TabHost mTabHost;

	private DiceRollerTabManager diceRoller;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		diceRoller = new DiceRollerTabManager(this);
		diceRoller.loadState(savedInstanceState);

		mTabHost = getTabHost();

		mTabHost.addTab(mTabHost.newTabSpec("diceroll").setIndicator(
				"Dice Roller").setContent(R.id.diceroll));

		mTabHost.setCurrentTab(0);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		diceRoller.saveState(outState);
	}

	public void rollDice(View view) {
		diceRoller.rollDice(String.valueOf(view.getTag()));
	}

}