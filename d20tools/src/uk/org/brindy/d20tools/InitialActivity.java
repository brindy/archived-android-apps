package uk.org.brindy.d20tools;

import android.app.Dialog;
import android.app.TabActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;

public class InitialActivity extends TabActivity {

	public static int DIALOG_DICEPROMPT = 0x01;

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

		mTabHost.addTab(mTabHost.newTabSpec("pointbuy").setIndicator(
				"Point Buy").setContent(R.id.pointbuy));

		mTabHost.setCurrentTab(0);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		diceRoller.saveState(outState);
	}

	public void rollDice(View view) {
		diceRoller.rollDice(String.valueOf(view.getTag()));
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		final Dialog dlg = new Dialog(this);
		dlg.setTitle("Custom Dice Roll");
		dlg.setContentView(R.layout.diceprompt);

		final EditText value = (EditText) dlg.findViewById(R.id.die_size);

		Button roll = (Button) dlg.findViewById(R.id.roll_button);
		roll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dlg.dismiss();
				diceRoller.rollDice(value.getText().toString());
			}
		});

		Button cancel = (Button) dlg.findViewById(R.id.cancel_button);
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dlg.dismiss();
			}
		});
		return dlg;
	}
}