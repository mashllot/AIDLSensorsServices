package com.zak.aidlsensorsservices;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zak.aidlsensorsservices.widget.ActionBar;
import com.zak.aidlsensorsservices.widget.ActionBar.AbstractAction;
import com.zak.aidlsensorsservices.widget.ActionBar.IntentAction;

public class AboutActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.underconstruction);

		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		// You can also assign the title programmatically by passing a
		// CharSequence or resource id.
		 actionBar.setTitle("About");
		actionBar.setHomeAction(new IntentAction(this, MainActivity
				.createIntent(this), R.drawable.ic_title_home_default));
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.addAction(new IntentAction(this, createShareIntent(),
				R.drawable.ic_title_share_default));
		actionBar.addAction(new ExampleAction());

		TextView helloTxt = (TextView) findViewById(R.id.output);
		helloTxt.setText(readTxt());
	}

	private String readTxt() {

		InputStream inputStream = getResources().openRawResource(R.raw.help);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		int i;
		try {
			i = inputStream.read();
			while (i != -1) {
				byteArrayOutputStream.write(i);
				i = inputStream.read();
			}
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return byteArrayOutputStream.toString();
	}

	private Intent createShareIntent() {
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, "https://github.com/naimbic/Android_AIDL_Sensors_Plus.");
		return Intent.createChooser(intent, "Share");
	}

	private class ExampleAction extends AbstractAction {

		public ExampleAction() {
			super(R.drawable.ic_title_export_default);
		}

		@Override
		public void performAction(View view) {
			Toast.makeText(AboutActivity.this, "Example action",
					Toast.LENGTH_SHORT).show();
		}

	}

}