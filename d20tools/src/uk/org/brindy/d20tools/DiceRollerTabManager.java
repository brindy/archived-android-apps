package uk.org.brindy.d20tools;

import java.util.Stack;

import android.app.TabActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DiceRollerTabManager {

	private Stack<Roll> history = new Stack<Roll>();

	private TextView output;

	private int total;

	public DiceRollerTabManager(TabActivity activity) {
		output = (TextView) activity.findViewById(R.id.diceroll_output);
	}

	public void saveState(Bundle bundle) {
		bundle.putString(getClass().getName() + ".output", output.getText()
				.toString());
		bundle.putInt(getClass().getName() + ".total", total);
	}

	public void loadState(Bundle bundle) {
		if (null != bundle) {
			output.setText(bundle.getString(getClass().getName() + ".output"));
			total = bundle.getInt(getClass().getName() + ".total");
		}
	}

	public void rollDice(String tag) {
		if ("reset".equals(tag)) {
			reset();
		} else if ("x".equals(tag)) {
			// promptForValue();
		} else if ("u".equals(tag)) {
			undo();
		} else {
			roll(Integer.parseInt(tag));
		}
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
