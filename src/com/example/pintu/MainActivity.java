package com.example.pintu;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pintu.view.GamePintuLayout;
import com.example.pintu.view.GamePintuLayout.GamePintuListener;

@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MainActivity extends ActionBarActivity implements
		android.view.View.OnClickListener {

	public final class GameViewListener implements GamePintuListener {
		@Override
		public void timeChanged(int currentTime) {
			mTime.setText("" + currentTime);
			if ("开始".equals(mPause.getText().toString())) {
				mPause.setText("暂停");
			}
		}

		@Override
		public void nextLevel(int nowLevel) {
			Toast.makeText(MainActivity.this, "第" + (nowLevel + 1) + "关",
					Toast.LENGTH_SHORT).show();
			SystemClock.sleep(300);
			mGameLayout.nextLevel();

			mHighest.setText("最高:" + mGameLayout.getHighest());
			mScore.setText("分数:" + mGameLayout.getScore());
			mLevel.setText("lv" + mGameLayout.getLevel());
			// new AlertDialog.Builder(MainActivity.this).setTitle("提示")
			// .setMessage("是否进行下一关")
			// .setPositiveButton("next level", new OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// mGameLayout.nextLevel();
			// mLevel.setText("lv" + mGameLayout.getLevel());
			// }
			// }).show();
		}

		@Override
		public void gameOver() {
			new AlertDialog.Builder(MainActivity.this).setTitle("提示")
					.setMessage("game over")
					.setPositiveButton("restart", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// 重新游戏
							mGameLayout.newGame(mGameLayout.getLevel());
						}
					}).setNegativeButton("quit", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// 退出游戏
							finish();
						}
					}).show();
		}

		@Override
		public void updateData() {
			if (mGameLayout == null) {
				return;
			}
			int level = mGameLayout.getLevel();
			int highest = mGameLayout.getHighest();
			int score = mGameLayout.getScore();
			int time = mGameLayout.getTime();
			mLevel.setText("lv" + level);
			mHighest.setText("最高:" + highest);
			mScore.setText("分数:" + score);
			mTime.setText("" + time);
		}
	}

	private GamePintuLayout mGameLayout;
	private TextView mTime;
	private TextView mLevel;
	public TextView mHighest;
	private TextView mScore;

	private Button mNew;
	private Button mShuffle;
	private Button mPause;
	private Button mNext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		initView();
	}

	private void initView() {
		setContentView(R.layout.activity_main);

		mGameLayout = (GamePintuLayout) findViewById(R.id.gly);
		mTime = (TextView) findViewById(R.id.id_time);
		mLevel = (TextView) findViewById(R.id.id_level);
		mScore = (TextView) findViewById(R.id.id_score);
		mHighest = (TextView) findViewById(R.id.id_highest);

		mShuffle = (Button) findViewById(R.id.id_shuffle);
		mNew = (Button) findViewById(R.id.id_new_game);
		mPause = (Button) findViewById(R.id.id_pause);
		mNext = (Button) findViewById(R.id.id_srcimg);

		mShuffle.setOnClickListener(this);
		mNew.setOnClickListener(this);
		mPause.setOnClickListener(this);
		mNext.setOnClickListener(this);
		mGameLayout.setGameListener(new GameViewListener());
	}

	@Override
	protected void onPause() {
		super.onPause();
		mGameLayout.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mGameLayout.resume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 结束时保存游戏数据
		mGameLayout.saveGameStatus();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_shuffle:
			mGameLayout.newGame(mGameLayout.getLevel());
			Toast.makeText(this, "准备完毕，点击开始", Toast.LENGTH_SHORT).show();
			break;
		case R.id.id_new_game:
			mGameLayout.newGame(0);
			break;
		case R.id.id_pause:
			String txt = mPause.getText().toString();
			if ("暂停".equals(txt)) {
				mGameLayout.pause();
				mPause.setText("开始");
			} else {
				mGameLayout.resume();
				Toast.makeText(this, "游戏开始计时", Toast.LENGTH_SHORT).show();
				mPause.setText("暂停");
			}
			break;
		case R.id.id_srcimg:
			if ("原图".equals(mNext.getText().toString())) {
				mGameLayout.showSrcImg();
				mNext.setText("隐藏");
			} else {
				mGameLayout.hideSrcImg();
				mNext.setText("原图");
			}

			break;

		default:
			break;
		}
	}
}
