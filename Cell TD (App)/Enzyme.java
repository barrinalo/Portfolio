package com.pluripotence.celltd;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

public class Enzyme implements Entity, Projectile{

	
	private Vector2 Relative_Location, Relative_Size, Relative_Direction;
	private int SCREEN_W, SCREEN_H;
	private Sprite DisplaySprite;
	private int Strength, bouncelim;
	private long reflectcd;
	
	public Enzyme(Texture t, Vector2 RLoc, Vector2 RSize, Vector2 RDir, int W,int H, float rotation, int StrengthLevel, int BounceLevel)
	{
		Relative_Location = new Vector2(RLoc);
		Relative_Size = RSize;
		Relative_Direction = RDir;
		SCREEN_W = W;
		SCREEN_H = H;
		DisplaySprite = new Sprite(t);
		DisplaySprite.setBounds((Relative_Location.x-Relative_Size.x/2)*SCREEN_W, (Relative_Location.y-Relative_Size.y/2)*SCREEN_H, Relative_Size.x*SCREEN_W, Relative_Size.y*SCREEN_H);
		DisplaySprite.setOrigin(DisplaySprite.getWidth()/2, DisplaySprite.getHeight()/2);
		DisplaySprite.setRotation(rotation);
		Strength = StrengthLevel;
		bouncelim = BounceLevel;
		reflectcd = TimeUtils.millis();
		
	}
	public int GetStrength() {return Strength;}
	public int GetBounce() {return bouncelim;}
	public Vector2 GetPosition() {return Relative_Location;}
	public Vector2 GetDir() {return Relative_Direction;};
	public void SetDir(Vector2 dir) 
	{
		if(TimeUtils.millis() - reflectcd > 200)
		{
		Relative_Direction = dir;
		float rotation = (float) Math.atan(Relative_Direction.y/Relative_Direction.x);
		rotation *= (180.0f/Math.PI);
		if(Relative_Direction.x < 0) rotation += 180.0f;
		DisplaySprite.setRotation(rotation);
		bouncelim -= 1;
		reflectcd = TimeUtils.millis();
		}
	}
	@Override
	public void Move() {
		Relative_Location.x += Relative_Direction.x;
		Relative_Location.y += Relative_Direction.y;
		DisplaySprite.translate(Relative_Direction.x*SCREEN_W, Relative_Direction.y*SCREEN_H);
		
	}

	@Override
	public boolean CheckIntersect(double inx, double iny, double inRadius) {
		if(Math.sqrt(Math.pow(inx-Relative_Location.x, 2) + Math.pow(iny-Relative_Location.y, 2)) < inRadius + Relative_Size.x/2.0f) return true;
		else return false;
	}

	@Override
	public boolean IsWithin(double inx, double iny) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void Draw(SpriteBatch SB) {
		DisplaySprite.draw(SB);
		
	}

	@Override
	public void resize(int x, int y) {
		SCREEN_W = x;
		SCREEN_H = y;
		DisplaySprite.setBounds((Relative_Location.x-Relative_Size.x/2)*SCREEN_W, (Relative_Location.y-Relative_Size.y/2)*SCREEN_H, Relative_Size.x*SCREEN_W, Relative_Size.y*SCREEN_H);
		DisplaySprite.setOrigin(DisplaySprite.getWidth()/2, DisplaySprite.getHeight()/2);
		
	}

}
