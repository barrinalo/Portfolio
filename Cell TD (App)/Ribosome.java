package com.pluripotence.celltd;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

public class Ribosome implements Entity{

	private Vector2 Relative_Location, Relative_Size;
	private int SCREEN_W, SCREEN_H;
	private Sprite DisplaySprite;
	private long ShootingCD;
	private int StrengthLevel,BounceLevel;
	
	public Ribosome(Texture T, Vector2 RLoc, Vector2 RSize, int w, int h, int EnzymeUpgradeLevel, int BounceLevel)
	{
		Relative_Location = RLoc;
		Relative_Size = RSize;
		SCREEN_W = w;
		SCREEN_H = h;
		DisplaySprite = new Sprite(T);
		DisplaySprite.setBounds((Relative_Location.x-Relative_Size.x/2.0f)*SCREEN_W, (Relative_Location.y-Relative_Size.y/2.0f)*SCREEN_H, Relative_Size.x*SCREEN_W, Relative_Size.y*SCREEN_H);
		ShootingCD = TimeUtils.millis();
		StrengthLevel = EnzymeUpgradeLevel;
		this.BounceLevel = BounceLevel;
	}
	
	public long GetCD() {return ShootingCD;}
	public void resetCD() {ShootingCD = TimeUtils.millis();}
	public void SetStrengthLevel(int i) {StrengthLevel = i;}
	public void SetBounceLevel(int i) {BounceLevel = i;}
	public Vector2 GetLocation() {return Relative_Location;}
	public Enzyme Shoot(double inx, double iny, Texture T)
	{
		double vecx = inx - Relative_Location.x;
		double vecy = iny - Relative_Location.y;
		double n = Math.sqrt(Math.pow(vecx,2) + Math.pow(vecy,2));
		vecx /= n;
		vecy /= n;
		vecx /=300;
		vecy /= 300;
		float rotation = (float) Math.atan(vecy/vecx);
		rotation *= (180.0f/Math.PI);
		if(vecx < 0) rotation += 180.0f;
		ShootingCD = TimeUtils.millis();
		return new Enzyme(T,Relative_Location, new Vector2(0.02f,0.02f),new Vector2((float)vecx,(float)vecy),SCREEN_W, SCREEN_H,rotation, StrengthLevel, BounceLevel-1);
	}
	@Override
	public boolean CheckIntersect(double inx, double iny, double inRadius) {
		if(Math.sqrt(Math.pow(inx-Relative_Location.x, 2) + Math.pow(iny-Relative_Location.y, 2)) < Relative_Size.x/2.0f + inRadius) return true;
		else return false;
	}

	@Override
	public boolean IsWithin(double inx, double iny) {
		if(inx >= Relative_Location.x && inx <= Relative_Location.x + Relative_Size.x && iny >= Relative_Location.y && iny <= Relative_Location.y + Relative_Size.y) return true;
		else return false;
	}

	@Override
	public void Draw(SpriteBatch SB) {
		DisplaySprite.draw(SB);
	}

	@Override
	public void resize(int x, int y) {
		SCREEN_W = x;
		SCREEN_H = y;
		DisplaySprite.setBounds((Relative_Location.x-Relative_Size.x/2.0f)*SCREEN_W, (Relative_Location.y-Relative_Size.y/2.0f)*SCREEN_H, Relative_Size.x*SCREEN_W, Relative_Size.y*SCREEN_H);
		
	}

}
