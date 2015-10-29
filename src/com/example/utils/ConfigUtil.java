package com.example.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ConfigUtil {

	private final static String gameFileName = "game_status";
	
	public static SharedPreferences getSP(Context context,String name){
		return context.getSharedPreferences(name, 0);
	}
	
	public static int getTime(Context context){
		return getSP(context, gameFileName).getInt("time", 0);
	}
	
	public static int getScore(Context context){
		return getSP(context, gameFileName).getInt("score", 0);
	}
	
	public static int getLevel(Context context){
		return getSP(context, gameFileName).getInt("level", 0);
	}
	
	public static int getMaxScore(Context context){
		return getSP(context, gameFileName).getInt("max", 0);
	}
	
	public static List<String> getMatrix(Context context){
		String matrix = getSP(context, gameFileName).getString("matrix","");
		if ("".equals(matrix)) {
			return null;
		}
		String[] items = matrix.split(";");
		
		List<String> rtMatirx = new ArrayList<String>();
		for (String item : items) {
			rtMatirx.add(item);
		}
		return rtMatirx;
	}
	
	/**
	 * 遵循约定value数组依次是Level，Score，Time数据
	 * @param context
	 * @param value
	 */
	public static void saveGameStatus(Context context,int...value){
		Editor edit = getSP(context,"game_status").edit();
		edit.putInt("level", value[0]);
		edit.putInt("score", value[1]);
		edit.putInt("time", value[2]);
		
		//如果高于最高分，更新最高分
		if (value[1]>getMaxScore(context)) {
			edit.putInt("max", value[1]);
		}
		edit.commit();
	}
	
	public static void saveMatrix(Context context,List<String> matrix){
		StringBuffer sb = new StringBuffer();
		for (String string : matrix) {
			sb.append(string).append(";");
		}
		getSP(context, gameFileName).edit().putString("matrix", sb.substring(0,sb.lastIndexOf(";"))).commit();
	}
	
}
