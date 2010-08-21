package uk.org.brindy.d20tools;

import java.util.Stack;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DiceRollerTabActivity extends Activity {

	private Stack<Roll> history = new Stack<Roll>();

	private TextView output;

	private int total;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.diceroller);

		output = (TextView) findViewById(R.id.diceroll_output);

		if (null != savedInstanceState) {
			output.setText(savedInstanceState.getString(getClass().getName()
					+ ".output"));
			total = savedInstanceState.getInt(getClass().getName() + ".total");

			int i = 0;
			while (true) {
				String oldText = savedInstanceState.getString(getClass()
						.getName() + ".roll." + i + ".oldText");
				if (oldText == null) {
					break;
				} else {
					Roll roll = new Roll();
					roll.oldText = oldText;
					roll.oldTotal = savedInstanceState.getInt(getClass()
							.getName() + ".roll." + i + ".oldTotal");
					history.add(roll);
				}
				i++;
			}

		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(getClass().getName() + ".output", output.getText()
				.toString());
		outState.putInt(getClass().getName() + ".total", total);

		int i = 0;
		for (Roll roll : history) {
			outState.putInt(getClass().getName() + ".roll." + i + ".oldtotal",
					roll.oldTotal);
			outState.putString(
					getClass().getName() + ".roll." + i + ".oldText",
					roll.oldText);
			i++;
		}

	}

	public void rollDice(View view) {
		rollDice(String.valueOf(view.getTag()));
	}

	public void rollDice(String tag) {
		if ("reset".equals(tag)) {
			reset();
		} else if ("x".equals(tag)) {
			showDialog(0);
		} else if ("u".equals(tag)) {
			undo();
		} else {
			roll(Integer.parseInt(tag));
		}
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
				rollDice(value.getText().toString());
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

	private void undo() {
		if (history.size() > 0) {
			history.pop().undo();
		}
	}

	private void reset() {
		history.clear();
		output.setText("");
		total = 0;
	}

	private void roll(int dice) {
		history.add(new Roll(dice));
	}

	class Roll {

		private String oldText;

		private int oldTotal;

		public Roll() {

		}

		public Roll(int dice) {
			this.oldText = output.getText().toString();
			this.oldTotal = total;
			int roll = (int) (Math.random() * dice) + 1;
			total = total + roll;

			String text = output.getText().toString();
			if (text.length() > 0) {
				text = "\n" + text;
			}

			text = "    total=" + total + "\n" + text;
			text = "roll D" + dice + " = " + roll + "\n" + text;
			output.setText(text);
		}

		public void undo() {
			total = oldTotal;
			output.setText(oldText);
		}

	}

}
