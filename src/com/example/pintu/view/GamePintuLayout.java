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
	 * 游戏交互接口
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
	 * 消息内容
	 */
	private static final int NEXT_LEVEL = 0x111;

	/**
	 * 消息内容
	 */
	private static final int TIME_CHANGED = 0x110;

	/**
	 * 动画进行中标志位
	 */
	private boolean isAniming;

	/**
	 * 第一次启动
	 */
	private boolean isFirstStart;

	/**
	 * 游戏结束
	 */
	private boolean isGameOver;

	/**
	 * 游戏暂停标志
	 */
	private boolean isPause;

	/**
	 * 游戏完成标志位
	 */
	private boolean isSuccess;

	/**
	 * 计时标志位
	 */
	private boolean isTimeEnabled = false;

	/**
	 * 动画层
	 */
	private RelativeLayout mAnimLayout;

	/**
	 * 游戏原始图
	 */
	private Bitmap mBitmap;

	/**
	 * 行列数
	 */
	private int mColumn = 3;

	/**
	 * 第一个被点击图标
	 */
	private ImageView mFirst;

	/**
	 * 放置图片的ImageView
	 */
	private ImageView[] mGamePintuItems;

	/**
	 * Handler 发送、处理消息
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
	 * 放置切分后ImagePiece元素的List
	 */
	private List<ImagePiece> mItemBitmaps;

	/**
	 * 放置切分后的ImageView元素，有序
	 */
	private List<ImagePiece> mItemBitmapsInit;

	/**
	 * 每个小图片的宽度
	 */
	private int mItemWidth;

	/**
	 * 游戏级别
	 */
	private int mLevel;

	/**
	 * 游戏回调接口
	 */
	public GamePintuListener mListener;

	/**
	 * 图片间距
	 */
	private int mMargin = 1;

	/**
	 * 游戏状态矩阵，实时记录游戏状态
	 */
	private List<String> mMatrix;

	/**
	 * 容器内边距
	 */
	private int mPadding;

	/**
	 * 游戏分数
	 */
	private int mScore;
	/** 第二个被点击图标 */
	private ImageView mSecond;
	/**
	 * 时间长度
	 */
	private int mTime;

	/**
	 * 图片宽度
	 */
	private int mWidth;

	/**
	 * 首次加载标志位，保证只初始化一次
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
	 * 初步检查一下是否存在保存的游戏状态
	 * @return 如果读取到游戏矩阵和游戏level，且矩阵是level x level，返回true
	 */
	public boolean checkIfExistsSavedGame() {
		mMatrix = ConfigUtil.getMatrix(getContext());
		if (null == mMatrix) {
			return false;
		}
		int level = ConfigUtil.getLevel(getContext());
		int column = level + 3;
		// 初步校验保存数据是否完整
		if (column * column != mMatrix.size()) {
			return false;
		}
		return true;
	}

	/**
	 * 检查游戏结束
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
			// 根据等级设置时间
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
	 * 交换两个ImageView
	 */
	private void exchangeView() {
		mFirst.setColorFilter(null);

		// 设置动画层
		setUpAnimLayout();

		setAnimationItems();
	}

	/**获得游戏级别*/
	public int getLevel() {
		return mLevel;
	}

	/* 通过标签得到index */
	private int getPost(String tag) {
		String[] split = tag.split("_");
		if (split.length < 2) {
			return -1;
		}
		return Integer.parseInt(split[1]);
	}

	/* 通过标签得到id */
	private int getPre(String tag) {
		String[] split = tag.split("_");
		if (split.length < 1) {
			return -1;
		}
		return Integer.parseInt(split[0]);
	}

	/**get 分数*/
	public int getScore() {
		return mScore;
	}

	/**get 剩余time*/
	public int getTime() {
		return mTime;
	}

	/* 初始化操作，定义内外边距，dp转px*/
	private void init() {
		mItemBitmapsInit = new ArrayList<ImagePiece>();
		mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				mMargin, getResources().getDisplayMetrics());
		mPadding = min(getPaddingLeft(), getPaddingRight(), getPaddingTop(),
				getPaddingBottom());
	}

	/**
	 * 加载并初始化Bitmap资源，切分，打乱
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
	 * 初始化切分后的图片矩阵，设置边距和位置，设置tag
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

			// 将每一个tag设置到对应的记录matrix中
			mMatrix.add(i + "_" + mItemBitmaps.get(i).getIndex());

			RelativeLayout.LayoutParams lp = new LayoutParams(mItemWidth,
					mItemWidth);

			// 最后一列不设置右边距
			if ((i + 1) % mColumn != 0) {
				lp.rightMargin = mMargin;
			}
			// 不是第一列，都在第一列的右边
			if (i % mColumn != 0) {
				lp.addRule(RelativeLayout.RIGHT_OF,
						mGamePintuItems[i - 1].getId());
			}
			// 第一行没有上边据
			if (i >= mColumn) {
				lp.topMargin = mMargin;
				lp.addRule(RelativeLayout.BELOW,
						mGamePintuItems[i - mColumn].getId());
			}
			addView(item, lp);
		}

	}

	/**判断倒计时开启*/
	public boolean isTimeEnabled() {
		return isTimeEnabled;
	}

	/**
	 * 读取保存的游戏状态
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

	/** 求最小的数 */
	private int min(int... params) {
		int rt = params[0];
		for (int i : params) {
			if (i < rt) {
				rt = i;
			}
		}
		return rt;
	}

	/**开始新游戏，从level级*/
	public void newGame(int level) {
		mLevel = level;
		isSuccess = false;
		isGameOver = false;
		mAnimLayout = null;
		setTimeEnabled(true);
		this.removeAllViews();
		// 切图，排序
		initBitmap();
		// 设置ImageView宽高
		initItem();
		checkTimeEnable();
		//更新界面显示:分数，级别，时间
		if (mListener != null) {
			mListener.updateData();
		}
		pause();
	}

	/**开始下一级别*/
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

	/**点击事件*/
	@Override
	public void onClick(View v) {
		// 如果暂停中，停止响应
		if (isPause) {
			return;
		}
		// 如果正在动画，不响应
		if (isAniming) {
			return;
		}
		// 两次点击同一个，取消
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
			// 如果读取到保存状态，恢复游戏，否则，重新开始游戏
			if (loadSavedGameStatus()) {
				resumeSavedGame();
			} else {
				newGame(0);
			}
		}
		once = true;
	}

	/**暂停*/
	public void pause() {
		isPause = true;
		mHandler.removeMessages(TIME_CHANGED);
	}

	/**重新开始*/
	public void resume() {
		if (isPause) {
			isPause = false;
			mHandler.sendEmptyMessage(TIME_CHANGED);
		}
	}

	/**恢复游戏，暂停*/
	private void resumeSavedGame() {
		mBitmap = BitmapFactory
				.decodeResource(getResources(), R.drawable.img01);
		// 恢复的游戏，mItemBitmaps 是有序的
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

	/**保存游戏状态到SP*/
	public void saveGameStatus() {
		// 结束时保存游戏数据
		ConfigUtil.saveGameStatus(getContext(), new int[] { mLevel, mScore,
				mTime });
		ConfigUtil.saveMatrix(getContext(), mMatrix);
	}

	/**实现动画效果*/
	private void setAnimationItems() {
		// 在动画层显示第一个ImageView
		ImageView first = new ImageView(getContext());
		first.setImageBitmap(mItemBitmapsInit.get(
				getPost((String) mFirst.getTag())).getBitmap());
		LayoutParams lp = new LayoutParams(mItemWidth, mItemWidth);
		lp.leftMargin = mFirst.getLeft() - mPadding;
		lp.topMargin = mFirst.getTop() - mPadding;
		first.setLayoutParams(lp);
		mAnimLayout.addView(first);

		// 第二个ImageView
		ImageView second = new ImageView(getContext());
		second.setImageBitmap(mItemBitmapsInit.get(
				getPost((String) mSecond.getTag())).getBitmap());
		LayoutParams lp2 = new LayoutParams(mItemWidth, mItemWidth);
		lp2.leftMargin = mSecond.getLeft() - mPadding;
		lp2.topMargin = mSecond.getTop() - mPadding;
		mAnimLayout.addView(second, lp2);

		// 设置动画1
		TranslateAnimation anim = new TranslateAnimation(0, mSecond.getLeft()
				- mFirst.getLeft(), 0, mSecond.getTop() - mFirst.getTop());
		anim.setDuration(300);
		anim.setFillAfter(true);
		first.startAnimation(anim);

		// 动画2，和动画1路径相反
		TranslateAnimation anim2 = new TranslateAnimation(0, -mSecond.getLeft()
				+ mFirst.getLeft(), 0, -mSecond.getTop() + mFirst.getTop());
		anim2.setDuration(300);
		anim2.setFillAfter(true);
		second.startAnimation(anim2);

		// 设置动画监听
		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				String firstTag = (String) mFirst.getTag();
				String secondTag = (String) mSecond.getTag();

				// 实时更新记录矩阵
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

				// 检查游戏结束
				checkSuccess();

				// 动画完成后，将原来图标重新显示
				mFirst.setVisibility(View.VISIBLE);
				mSecond.setVisibility(View.VISIBLE);
				mFirst = null;
				mSecond = null;
				// 动画层清空
				mAnimLayout.removeAllViews();
				// 设置动画结束标志
				isAniming = false;
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationStart(Animation animation) {
				isAniming = true;
				// 动画开始前，将原来图标隐藏
				mFirst.setVisibility(View.INVISIBLE);
				mSecond.setVisibility(View.INVISIBLE);
			}
		});
	}

	/**为游戏设置回调接口*/
	public void setGameListener(GamePintuListener mListener) {
		this.mListener = mListener;
	}

	/**开启计时*/
	public void setTimeEnabled(boolean isTimeEnabled) {
		this.isTimeEnabled = isTimeEnabled;
	}

	/** 设置动画层 */
	private void setUpAnimLayout() {
		if (mAnimLayout == null) {
			mAnimLayout = new RelativeLayout(getContext());
			addView(mAnimLayout);
		}
	}

	/**更新mMatrix，和当前界面图片位置实时对应，退出时保存*/
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

	/**点击两个ImageView后，他们交换位置，同时ImageView的tag实时更新	 */
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