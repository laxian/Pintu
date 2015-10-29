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
			//ʣ���ʱ����Ƿ�����Ҳ����Խ�����Խ��
			int oldScore = Integer.valueOf(mScore.getText().toString());
			int score = oldScore + mGameLayout.getTime();
			mScore.setText(""+score);
			new AlertDialog.Builder(MainActivity.this).setTitle("��ʾ")
					.setMessage("�Ƿ������һ��")
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
			new AlertDialog.Builder(MainActivity.this).setTitle("��ʾ")
					.setMessage("game over")
					.setPositiveButton("restart", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							//������Ϸ
							mGameLayout.newGame(mGameLayout.getLevel());
						}
					}).setNegativeButton("quit", new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//�˳���Ϸ
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
		
		//����ʱ������Ϸ����
		mGameLayout.saveGameStatus();
		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
		case R.id.id_new:
			mGameLayout.newGame(mGameLayout.getLevel());
			Toast.makeText(this, "׼����ϣ������ʼ", Toast.LENGTH_SHORT).show();
			break;
		case R.id.id_pause:
			String txt = mPause.getText().toString();
			if ("��ͣ".equals(txt)) {
				mGameLayout.pause();
				mPause.setText("��ʼ");
				Toast.makeText(this, "��Ϸ��ʼ��ʱ", Toast.LENGTH_SHORT).show();
			}else{
				mGameLayout.resume();
				mPause.setText("��ͣ");
			}
			break;
		case R.id.id_next:
			Toast.makeText(this, "�����ڴ�", Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
		return false;
	}
}
