package uk.org.brindy.d20tools;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class InitialActivity extends TabActivity {

	private TabHost mTabHost;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mTabHost = getTabHost();

		mTabHost.addTab(mTabHost
				.newTabSpec("diceroller")
				.setIndicator("Dice Roller",
						getResources().getDrawable(R.drawable.dice))
				.setContent(new Intent(this, DiceRollerTabActivity.class)));

		mTabHost.addTab(mTabHost
				.newTabSpec("inittracker")
				.setIndicator("Initiative Tracker",
						getResources().getDrawable(R.drawable.clover))
				.setContent(
						new Intent(this, InitiativeTrackerTabActivity.class)));

		mTabHost.setCurrentTab(0);
	}

}