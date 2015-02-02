package com.pluripotence.celltd;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public interface Entity {
	
	public abstract boolean CheckIntersect(double inx, double iny, double inRadius);
	public abstract boolean IsWithin(double inx, double iny);
	public abstract void Draw(SpriteBatch SB);
	public abstract void resize(int x, int y);
}
