package com.codelessweb.rank2048;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BoardPiece {
	public Sprite Piece;
	float Originalx, Originaly, Originalw, Originalh;
	public BoardPiece() {
		Piece = new Sprite();
	}
	
	public void setBounds(float x, float y, float w, float h) {
		Originalx = x;
		Originaly = y;
		Originalw = w;
		Originalh = h;
		Piece.setBounds(x, y, w, h);
	}
	
	public void Reset() {
		Piece.setBounds(Originalx, Originaly, Originalw, Originalh);
	}
	
	public void setTexture(Texture t) {
		Piece.setRegion(t);
	}
	
	public void Move(float x, float y) {
		Piece.translate(x,y);
	}
	public void draw(SpriteBatch batch) {
		Piece.draw(batch, 1);
	}
}
