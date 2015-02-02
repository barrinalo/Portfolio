package com.pluripotence.celltd;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

public class PowerUp implements Entity, Clickable, Projectile{
	private Sprite DisplaySprite;
	private String PowerUpName;
	private Vector2 Relative_Location, Relative_Size, Relative_Direction;
	private int SCREEN_W, SCREEN_H;
	private long DirChange;
	private long Expiry;
	private boolean Activated;
	
	public boolean GetActivateStatus()
	{
		return Activated;
	}
	
	public void Activate()
	{
		Activated = true;
	}
	public long GetExpiry()
	{
		return Expiry;
	}
	public PowerUp(Texture s, String Name, Vector2 RLoc, Vector2 RSize, int w, int h)
	{
		Relative_Location = RLoc;
		Relative_Size = RSize;
		SCREEN_W = w;
		SCREEN_H = h;
		
		DisplaySprite = new Sprite(s);
		DisplaySprite.setBounds((Relative_Location.x-Relative_Size.x/2)*SCREEN_W, (Relative_Location.y-Relative_Size.y/2)*SCREEN_H, Relative_Size.x*SCREEN_W, Relative_Size.y*SCREEN_H);
		DisplaySprite.setOrigin(DisplaySprite.getWidth()/2, DisplaySprite.getHeight()/2);
		
		
		Relative_Direction = new Vector2((float)Math.random(),(float)Math.random());
		if(Math.random() > 0.5f) Relative_Direction.x *= -1.0f;
		else if(Math.random() > 0.5f) Relative_Direction.y *= -1.0f;
		float n = (float) Math.sqrt(Math.pow(Relative_Direction.x, 2) + Math.pow(Relative_Direction.y, 2));
		Relative_Direction.x *= 0.004f/n;
		Relative_Direction.y *= 0.004f/n;
		
		DirChange = TimeUtils.millis();
		Expiry = TimeUtils.millis();
		PowerUpName = Name;
		Activated = false;
	}
	
	@Override
	public void OnClick(float x, float y) {
		// TODO Auto-generated method stub
		
	}
	public String GetName()
	{
		return PowerUpName;
	}
	@Override
	public boolean CheckIntersect(double inx, double iny, double inRadius) {
		if(Math.sqrt(Math.pow(inx-Relative_Location.x, 2) + Math.pow(iny-Relative_Location.y, 2)) < inRadius + Relative_Size.x/2.0f) return true;
		else return false;
	}

	@Override
	public boolean IsWithin(double inx, double iny) {
		return false;
	}

	@Override
	public void Draw(SpriteBatch SB) {
		// TODO Auto-generated method stub
		if(TimeUtils.millis() - DirChange > 3000)
		{
			Relative_Direction.x = (float) Math.random();
			Relative_Direction.y = (float) Math.random();
			if(Math.random() > 0.5f) Relative_Direction.x *= -1.0f;
			else if(Math.random() > 0.5f) Relative_Direction.y *= -1.0f;
			float n = (float) Math.sqrt(Math.pow(Relative_Direction.x, 2) + Math.pow(Relative_Direction.y, 2));
			Relative_Direction.x *= 0.004f/n;
			Relative_Direction.y *= 0.004f/n;
			DirChange = TimeUtils.millis();
		}
		DisplaySprite.draw(SB);
	}

	@Override
	public void resize(int x, int y) {
		// TODO Auto-generated method stub
		SCREEN_W = x;
		SCREEN_H = y;
		DisplaySprite.setBounds((Relative_Location.x-Relative_Size.x/2)*SCREEN_W, (Relative_Location.y-Relative_Size.y/2)*SCREEN_H, Relative_Size.x*SCREEN_W, Relative_Size.y*SCREEN_H);
		DisplaySprite.setOrigin(DisplaySprite.getWidth()/2, DisplaySprite.getHeight()/2);
	}

	@Override
	public void Move() {
		// TODO Auto-generated method stub
		Relative_Location.x += Relative_Direction.x;
		Relative_Location.y += Relative_Direction.y;
		DisplaySprite.translate(Relative_Direction.x*SCREEN_W, Relative_Direction.y*SCREEN_H);
	}
	
	public boolean WithinScreen()
	{
		if(Relative_Location.x < 0 || Relative_Location.x > 0.8f || Relative_Location.y < 0 || Relative_Location.y > 1) return false;
		else return true;
	}
}
