package com.example.pintu;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pintu.view.GamePintuLayout;
import com.example.pintu.view.GamePintuLayout.GamePintuListener;

@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MainActivity extends ActionBarActivity implements OnTouchListener {

	public final class GameViewListener implements GamePintuListener {
		@Override
		public void timeChanged(int currentTime) {
			mTime.setText(""+currentTime);
		}

		@Override
		public void nextLevel(int nextLevel) {
			//剩余的时间就是分数，也就是越快分数越高
			int oldScore = Integer.valueOf(mScore.getText().toString());
			int score = oldScore + mGameLayout.getTime();
			mScore.setText(""+score);
			new AlertDialog.Builder(MainActivity.this).setTitle("提示")
					.setMessage("是否进行下一关")
					.setPositiveButton("next level", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							mGameLayout.nextLevel();
							mLevel.setText("lv"+mGameLayout.getLevel());
						}
					}).show();
		}

		@Override
		public void gameOver() {
			new AlertDialog.Builder(MainActivity.this).setTitle("提示")
					.setMessage("game over")
					.setPositiveButton("restart", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							//重新游戏
							mGameLayout.newGame(mGameLayout.getLevel());
						}
					}).setNegativeButton("quit", new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//退出游戏
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
			int score = mGameLayout.getScore();
			int time = mGameLayout.getTime();
			mLevel.setText("lv"+level);
			mScore.setText(""+score);
			mTime.setText(""+time);
		}
	}

	private GamePintuLayout mGameLayout;
	private TextView mTime;
	private TextView mLevel;
	private TextView mScore;

	private TextView mNew;
	private TextView mPause;
	private TextView mNext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		

		mGameLayout = (GamePintuLayout) findViewById(R.id.gly);
		mTime = (TextView) findViewById(R.id.id_time);
		mLevel = (TextView) findViewById(R.id.id_level);
		mScore = (TextView) findViewById(R.id.id_score);

		mNew = (TextView) findViewById(R.id.id_new);
		mPause = (TextView) findViewById(R.id.id_pause);
		mNext = (TextView) findViewById(R.id.id_next);
		
		mNew.setOnTouchListener(this);
		mPause.setOnTouchListener(this);
		mNext.setOnTouchListener(this);
		
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
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		//mGameLayout.start();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		//结束时保存游戏数据
		mGameLayout.saveGameStatus();
		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
		case R.id.id_new:
			mGameLayout.newGame(mGameLayout.getLevel());
			Toast.makeText(this, "准备完毕，点击开始", Toast.LENGTH_SHORT).show();
			break;
		case R.id.id_pause:
			String txt = mPause.getText().toString();
			if ("暂停".equals(txt)) {
				mGameLayout.pause();
				mPause.setText("开始");
				Toast.makeText(this, "游戏开始计时", Toast.LENGTH_SHORT).show();
			}else{
				mGameLayout.resume();
				mPause.setText("暂停");
			}
			break;
		case R.id.id_next:
			Toast.makeText(this, "敬请期待", Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
		return false;
	}
}
