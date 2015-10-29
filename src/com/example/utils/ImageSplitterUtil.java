package com.example.utils;

import java.util.ArrayList;
import java.util.List;
import android.graphics.Bitmap;

public class ImageSplitterUtil {
	/**
	 * �������bitmap��ͼ��С��
	 * @param bm �������Ҫ����ͼ��bitmap
	 * @param pieces ��ͼ�Ŀ���
	 * @return List<ImagePiece> ����һ����ͼ�б�
	 */
	public static List<ImagePiece> splitImage(Bitmap bm,int pieces){
		List<ImagePiece> imagePieces = new ArrayList<ImagePiece>();
		int width = bm.getWidth();
		int height = bm.getHeight();
		
		int pieceWidth = Math.max(width,height)/pieces;
		for (int i = 0; i < pieces; i++) {
			for (int j = 0; j < pieces; j++) {
				ImagePiece imagePiece = new ImagePiece();
				imagePiece.setIndex(j+i*pieces);
				
				int x = j*pieceWidth;
				int y = i*pieceWidth;
				
				imagePiece.setBitmap(Bitmap.createBitmap(bm, x, y, pieceWidth, pieceWidth));
				imagePieces.add(imagePiece);
			}
		}
		return imagePieces;
	}
}
