package com.example.pintu.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.pintu.R;
import com.example.utils.ConfigUtil;
import com.example.utils.ImagePiece;
import com.example.utils.ImageSplitterUtil;

public class GamePintuLayout extends RelativeLayout implements OnClickListener {

	/**
	 * ��Ϸ�����ӿ�
	 * @author laxian
	 *
	 */
	public interface GamePintuListener {

		void gameOver();

		void nextLevel(int nextLevel);

		void timeChanged(int currentTime);

		void updateData();
	}

	/**
	 * ��Ϣ����
	 */
	private static final int NEXT_LEVEL = 0x111;

	/**
	 * ��Ϣ����
	 */
	private static final int TIME_CHANGED = 0x110;

	/**
	 * ���������б�־λ
	 */
	private boolean isAniming;

	/**
	 * ��һ������
	 */
	private boolean isFirstStart;

	/**
	 * ��Ϸ����
	 */
	private boolean isGameOver;

	/**
	 * ��Ϸ��ͣ��־
	 */
	private boolean isPause;

	/**
	 * ��Ϸ��ɱ�־λ
	 */
	private boolean isSuccess;

	/**
	 * ��ʱ��־λ
	 */
	private boolean isTimeEnabled = false;

	/**
	 * ������
	 */
	private RelativeLayout mAnimLayout;

	/**
	 * ��Ϸԭʼͼ
	 */
	private Bitmap mBitmap;

	/**
	 * ������
	 */
	private int mColumn = 3;

	/**
	 * ��һ�������ͼ��
	 */
	private ImageView mFirst;

	/**
	 * ����ͼƬ��ImageView
	 */
	private ImageView[] mGamePintuItems;

	/**
	 * Handler ���͡�������Ϣ
	 */
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case TIME_CHANGED:
				if (isSuccess || isGameOver || isPause) {
					return;
				}
				if (mListener != null) {
					mListener.timeChanged(mTime);
					if (mTime == 0) {
						isGameOver = true;
						mListener.gameOver();
						return;
					}
				}
				mTime--;
				mHandler.sendEmptyMessageDelayed(TIME_CHANGED, 1000);
				break;
			case NEXT_LEVEL:
				if (mListener != null) {
					mListener.nextLevel(mLevel);
				} else
					nextLevel();

				break;

			default:
				break;
			}
		}
	};

	/**
	 * �����зֺ�ImagePieceԪ�ص�List
	 */
	private List<ImagePiece> mItemBitmaps;

	/**
	 * �����зֺ��ImageViewԪ�أ�����
	 */
	private List<ImagePiece> mItemBitmapsInit;

	/**
	 * ÿ��СͼƬ�Ŀ��
	 */
	private int mItemWidth;

	/**
	 * ��Ϸ����
	 */
	private int mLevel;

	/**
	 * ��Ϸ�ص��ӿ�
	 */
	public GamePintuListener mListener;

	/**
	 * ͼƬ���
	 */
	private int mMargin = 1;

	/**
	 * ��Ϸ״̬����ʵʱ��¼��Ϸ״̬
	 */
	private List<String> mMatrix;

	/**
	 * �����ڱ߾�
	 */
	private int mPadding;

	/**
	 * ��Ϸ����
	 */
	private int mScore;
	/** �ڶ��������ͼ�� */
	private ImageView mSecond;
	/**
	 * ʱ�䳤��
	 */
	private int mTime;

	/**
	 * ͼƬ���
	 */
	private int mWidth;

	/**
	 * �״μ��ر�־λ����ֻ֤��ʼ��һ��
	 */
	private boolean once;

	public GamePintuLayout(Context context) {
		super(context, null);
	}

	public GamePintuLayout(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
	}

	public GamePintuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	/**
	 * �������һ���Ƿ���ڱ������Ϸ״̬
	 * @return �����ȡ����Ϸ�������Ϸlevel���Ҿ�����level x level������true
	 */
	public boolean checkIfExistsSavedGame() {
		mMatrix = ConfigUtil.getMatrix(getContext());
		if (null == mMatrix) {
			return false;
		}
		int level = ConfigUtil.getLevel(getContext());
		int column = level + 3;
		// ����У�鱣�������Ƿ�����
		if (column * column != mMatrix.size()) {
			return false;
		}
		return true;
	}

	/**
	 * �����Ϸ����
	 */
	private void checkSuccess() {
		for (int i = 0; i < mGamePintuItems.length; i++) {
			int index = getPost((String) mGamePintuItems[i].getTag());
			if (index != i) {
				isSuccess = false;
				return;
			}
		}
		isSuccess = true;
		mHandler.sendEmptyMessage(NEXT_LEVEL);
		Toast.makeText(getContext(), "You win!", Toast.LENGTH_SHORT).show();
	}

	private void checkTimeEnable() {
		if (isTimeEnabled) {
			// ���ݵȼ�����ʱ��
			if (!isFirstStart) {
				countTimeBaseLevel();
				isFirstStart = false;
			}
			mHandler.sendEmptyMessage(TIME_CHANGED);
		}
	}

	private void countTimeBaseLevel() {
		mTime = (int) Math.pow(2, mLevel) * 60;
	}

	/**
	 * ��������ImageView
	 */
	private void exchangeView() {
		mFirst.setColorFilter(null);

		// ���ö�����
		setUpAnimLayout();

		setAnimationItems();
	}

	/**�����Ϸ����*/
	public int getLevel() {
		return mLevel;
	}

	/* ͨ����ǩ�õ�index */
	private int getPost(String tag) {
		String[] split = tag.split("_");
		if (split.length < 2) {
			return -1;
		}
		return Integer.parseInt(split[1]);
	}

	/* ͨ����ǩ�õ�id */
	private int getPre(String tag) {
		String[] split = tag.split("_");
		if (split.length < 1) {
			return -1;
		}
		return Integer.parseInt(split[0]);
	}

	/**get ����*/
	public int getScore() {
		return mScore;
	}

	/**get ʣ��time*/
	public int getTime() {
		return mTime;
	}

	/* ��ʼ����������������߾࣬dpתpx*/
	private void init() {
		mItemBitmapsInit = new ArrayList<ImagePiece>();
		mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				mMargin, getResources().getDisplayMetrics());
		mPadding = min(getPaddingLeft(), getPaddingRight(), getPaddingTop(),
				getPaddingBottom());
	}

	/**
	 * ���ز���ʼ��Bitmap��Դ���з֣�����
	 */
	private void initBitmap() {
		mBitmap = BitmapFactory
				.decodeResource(getResources(), R.drawable.img01);
		mItemBitmaps = ImageSplitterUtil.splitImage(mBitmap, mColumn);
		mItemBitmapsInit = ImageSplitterUtil.splitImage(mBitmap, mColumn);
		Collections.sort(mItemBitmaps, new Comparator<ImagePiece>() {

			@Override
			public int compare(ImagePiece lhs, ImagePiece rhs) {
				return Math.random() > 0.5 ? 1 : -1;
			}
		});
	}

	/**
	 * ��ʼ���зֺ��ͼƬ�������ñ߾��λ�ã�����tag
	 */
	private void initItem() {
		mItemWidth = (mWidth - mPadding * 2 - mMargin * (mColumn - 1))
				/ mColumn;
		mGamePintuItems = new ImageView[mColumn * mColumn];
		mMatrix = new ArrayList<String>();

		for (int i = 0; i < mGamePintuItems.length; i++) {
			ImageView item = new ImageView(getContext());
			item.setOnClickListener(this);
			item.setImageBitmap(mItemBitmaps.get(i).getBitmap());
			item.setId(i + 1);
			item.setTag(i + "_" + mItemBitmaps.get(i).getIndex());
			mGamePintuItems[i] = item;

			// ��ÿһ��tag���õ���Ӧ�ļ�¼matrix��
			mMatrix.add(i + "_" + mItemBitmaps.get(i).getIndex());

			RelativeLayout.LayoutParams lp = new LayoutParams(mItemWidth,
					mItemWidth);

			// ���һ�в������ұ߾�
			if ((i + 1) % mColumn != 0) {
				lp.rightMargin = mMargin;
			}
			// ���ǵ�һ�У����ڵ�һ�е��ұ�
			if (i % mColumn != 0) {
				lp.addRule(RelativeLayout.RIGHT_OF,
						mGamePintuItems[i - 1].getId());
			}
			// ��һ��û���ϱ߾�
			if (i >= mColumn) {
				lp.topMargin = mMargin;
				lp.addRule(RelativeLayout.BELOW,
						mGamePintuItems[i - mColumn].getId());
			}
			addView(item, lp);
		}

	}

	/**�жϵ���ʱ����*/
	public boolean isTimeEnabled() {
		return isTimeEnabled;
	}

	/**
	 * ��ȡ�������Ϸ״̬
	 */
	public boolean loadSavedGameStatus() {
		mMatrix = ConfigUtil.getMatrix(getContext());
		if (null == mMatrix) {
			return false;
		}
		mLevel = ConfigUtil.getLevel(getContext());
		mScore = ConfigUtil.getScore(getContext());
		mTime = ConfigUtil.getTime(getContext());
		mColumn += mLevel;
		isFirstStart = true;
		if (mListener != null) {
			mListener.updateData();
		}
		return true;
	}

	/** ����С���� */
	private int min(int... params) {
		int rt = params[0];
		for (int i : params) {
			if (i < rt) {
				rt = i;
			}
		}
		return rt;
	}

	/**��ʼ����Ϸ����level��*/
	public void newGame(int level) {
		mLevel = level;
		isSuccess = false;
		isGameOver = false;
		mAnimLayout = null;
		setTimeEnabled(true);
		this.removeAllViews();
		// ��ͼ������
		initBitmap();
		// ����ImageView���
		initItem();
		checkTimeEnable();
		//���½�����ʾ:����������ʱ��
		if (mListener != null) {
			mListener.updateData();
		}
		pause();
	}

	/**��ʼ��һ����*/
	public void nextLevel() {
		++mLevel;
		mScore = mTime;
		checkTimeEnable();
		this.removeAllViews();
		mAnimLayout = null;
		mColumn++;
		isSuccess = false;
		initBitmap();
		initItem();
	}

	/**����¼�*/
	@Override
	public void onClick(View v) {
		// �����ͣ�У�ֹͣ��Ӧ
		if (isPause) {
			return;
		}
		// ������ڶ���������Ӧ
		if (isAniming) {
			return;
		}
		// ���ε��ͬһ����ȡ��
		if (mFirst == v) {
			mFirst.setColorFilter(null);
			mFirst = null;
			return;
		}

		if (mFirst == null) {
			mFirst = (ImageView) v;
			mFirst.setColorFilter(Color.parseColor("#55ff0000"));
		} else {
			mSecond = (ImageView) v;
			exchangeView();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mWidth = min(getMeasuredHeight(), getMeasuredWidth());

		if (!once) {
			// �����ȡ������״̬���ָ���Ϸ���������¿�ʼ��Ϸ
			if (loadSavedGameStatus()) {
				resumeSavedGame();
			} else {
				newGame(0);
			}
		}
		once = true;
	}

	/**��ͣ*/
	public void pause() {
		isPause = true;
		mHandler.removeMessages(TIME_CHANGED);
	}

	/**���¿�ʼ*/
	public void resume() {
		if (isPause) {
			isPause = false;
			mHandler.sendEmptyMessage(TIME_CHANGED);
		}
	}

	/**�ָ���Ϸ����ͣ*/
	private void resumeSavedGame() {
		mBitmap = BitmapFactory
				.decodeResource(getResources(), R.drawable.img01);
		// �ָ�����Ϸ��mItemBitmaps �������
		mItemBitmaps = ImageSplitterUtil.splitImage(mBitmap, mColumn);
		mItemBitmapsInit = mItemBitmaps;

		List<ImagePiece> newList = new ArrayList<ImagePiece>();
		for (String s : mMatrix) {
			int index = getPost(s);
			newList.add(mItemBitmaps.get(index));
		}
		mItemBitmaps = newList;
		initItem();

		setTimeEnabled(true);
		checkTimeEnable();
		isFirstStart = false;
		pause();
	}

	/**������Ϸ״̬��SP*/
	public void saveGameStatus() {
		// ����ʱ������Ϸ����
		ConfigUtil.saveGameStatus(getContext(), new int[] { mLevel, mScore,
				mTime });
		ConfigUtil.saveMatrix(getContext(), mMatrix);
	}

	/**ʵ�ֶ���Ч��*/
	private void setAnimationItems() {
		// �ڶ�������ʾ��һ��ImageView
		ImageView first = new ImageView(getContext());
		first.setImageBitmap(mItemBitmapsInit.get(
				getPost((String) mFirst.getTag())).getBitmap());
		LayoutParams lp = new LayoutParams(mItemWidth, mItemWidth);
		lp.leftMargin = mFirst.getLeft() - mPadding;
		lp.topMargin = mFirst.getTop() - mPadding;
		first.setLayoutParams(lp);
		mAnimLayout.addView(first);

		// �ڶ���ImageView
		ImageView second = new ImageView(getContext());
		second.setImageBitmap(mItemBitmapsInit.get(
				getPost((String) mSecond.getTag())).getBitmap());
		LayoutParams lp2 = new LayoutParams(mItemWidth, mItemWidth);
		lp2.leftMargin = mSecond.getLeft() - mPadding;
		lp2.topMargin = mSecond.getTop() - mPadding;
		mAnimLayout.addView(second, lp2);

		// ���ö���1
		TranslateAnimation anim = new TranslateAnimation(0, mSecond.getLeft()
				- mFirst.getLeft(), 0, mSecond.getTop() - mFirst.getTop());
		anim.setDuration(300);
		anim.setFillAfter(true);
		first.startAnimation(anim);

		// ����2���Ͷ���1·���෴
		TranslateAnimation anim2 = new TranslateAnimation(0, -mSecond.getLeft()
				+ mFirst.getLeft(), 0, -mSecond.getTop() + mFirst.getTop());
		anim2.setDuration(300);
		anim2.setFillAfter(true);
		second.startAnimation(anim2);

		// ���ö�������
		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				String firstTag = (String) mFirst.getTag();
				String secondTag = (String) mSecond.getTag();

				// ʵʱ���¼�¼����
				updateMatrix(firstTag, secondTag);

				Bitmap firstBitmap = mItemBitmapsInit.get(getPost(firstTag))
						.getBitmap();
				Bitmap secondBitmap = mItemBitmapsInit.get(getPost(secondTag))
						.getBitmap();

				mSecond.setImageBitmap(firstBitmap);
				mFirst.setImageBitmap(secondBitmap);

				// mFirst.setTag(secondTag);
				// mSecond.setTag(firstTag);

				updateTag(mFirst, mSecond);

				// �����Ϸ����
				checkSuccess();

				// ������ɺ󣬽�ԭ��ͼ��������ʾ
				mFirst.setVisibility(View.VISIBLE);
				mSecond.setVisibility(View.VISIBLE);
				mFirst = null;
				mSecond = null;
				// ���������
				mAnimLayout.removeAllViews();
				// ���ö���������־
				isAniming = false;
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationStart(Animation animation) {
				isAniming = true;
				// ������ʼǰ����ԭ��ͼ������
				mFirst.setVisibility(View.INVISIBLE);
				mSecond.setVisibility(View.INVISIBLE);
			}
		});
	}

	/**Ϊ��Ϸ���ûص��ӿ�*/
	public void setGameListener(GamePintuListener mListener) {
		this.mListener = mListener;
	}

	/**������ʱ*/
	public void setTimeEnabled(boolean isTimeEnabled) {
		this.isTimeEnabled = isTimeEnabled;
	}

	/** ���ö����� */
	private void setUpAnimLayout() {
		if (mAnimLayout == null) {
			mAnimLayout = new RelativeLayout(getContext());
			addView(mAnimLayout);
		}
	}

	/**����mMatrix���͵�ǰ����ͼƬλ��ʵʱ��Ӧ���˳�ʱ����*/
	private void updateMatrix(String tag1, String tag2) {
		int pre1 = getPre(tag1);
		int idx1 = getPost(tag1);

		int pre2 = getPre(tag2);
		int idx2 = getPost(tag2);

		String newTag1 = pre1 + "_" + idx2;
		String newTag2 = pre2 + "_" + idx1;

		mMatrix.set(pre1, newTag1);
		mMatrix.set(pre2, newTag2);
	}

	/**�������ImageView�����ǽ���λ�ã�ͬʱImageView��tagʵʱ����	 */
	private void updateTag(View v1, View v2) {
		String tag1 = (String) v1.getTag();
		String tag2 = (String) v2.getTag();

		int pre1 = getPre(tag1);
		int idx1 = getPost(tag1);

		int pre2 = getPre(tag2);
		int idx2 = getPost(tag2);

		String newTag1 = pre1 + "_" + idx2;
		String newTag2 = pre2 + "_" + idx1;

		v1.setTag(newTag1);
		v2.setTag(newTag2);
	}
}