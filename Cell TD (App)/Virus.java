package com.pluripotence.celltd;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

public class Virus implements Entity, Projectile{

	public enum Virus_State
	{
		NORMAL,
		NEUTRALISED,
		UNSHEATHED
	}
	
	private Vector2 Relative_Location, Relative_Size, Relative_Direction;
	private Virus_State State;
	private int SCREEN_W, SCREEN_H;
	private Texture Outside, Inside, Neutralised;
	private Sprite DisplaySprite;
	private int hits, total;
	private long reversecd;
	private boolean Mega, Slowed, Slows;
	
	public Virus(Texture OutsideSprite, Texture InsideSprite, Texture Neut, Vector2 RLoc, Vector2 RSize, int W,int H, int hits, boolean Mega)
	{
		Slows = false;
		Slowed = false;
		this.Mega = Mega;
		Outside = OutsideSprite;
		Inside = InsideSprite;
		Neutralised = Neut;
		Relative_Location = RLoc;
		Relative_Size = RSize;
		State = Virus_State.NORMAL;
		SCREEN_W = W;
		SCREEN_H = H;
		this.hits = hits;
		total = hits;
		DisplaySprite = new Sprite(Outside);
		DisplaySprite.setBounds((Relative_Location.x-Relative_Size.x/2)*SCREEN_W, (Relative_Location.y-Relative_Size.y/2)*SCREEN_H, Relative_Size.x*SCREEN_W, Relative_Size.y*SCREEN_H);
		DisplaySprite.setOrigin(DisplaySprite.getWidth()/2, DisplaySprite.getHeight()/2);
		Relative_Direction = new Vector2();
		if(!Mega)
		{
		Relative_Direction.x = -(Relative_Location.x - 0.4f)/300.0f;
		Relative_Direction.y = -(Relative_Location.y - 0.5f)/300.0f;
		}
		else
		{
			Relative_Direction.x = -(Relative_Location.x - 0.4f)/550.0f;
			Relative_Direction.y = -(Relative_Location.y - 0.5f)/550.0f;
		}
		if(Relative_Location.x > 0.4) DisplaySprite.setRotation((float) (Math.atan(Relative_Direction.y/Relative_Direction.x)*180.0f/Math.PI));
		else DisplaySprite.setRotation((float) (Math.atan(Relative_Direction.y/Relative_Direction.x)*180.0f/Math.PI)+180.0f);
		reversecd = TimeUtils.millis();
		
	}
	public void SetLocation(float x, float y)
	{
		Relative_Location.x = x;
		Relative_Location.y = y;
	}
	public boolean SlowsRibosome() {return Slows;}
	public void SetSlow(boolean s) {Slows = s;}
	public boolean GetMega() {return Mega;}
	public Vector2 GetSize() {return Relative_Size;}
	public float GetLife() {return (float)hits/(float)total;}
	public int GetStrength() {return hits;}
	public void SetStrength(int delta) { hits -= delta;};
	public Vector2 GetPosition() {return Relative_Location;}
	public Virus_State GetState() {return State;}
	public void Reverse() 
	{
		if(TimeUtils.millis()-reversecd > 800)
		{
		Relative_Direction.x *= -1.0f;
		Relative_Direction.y *= -1.0f;
		reversecd = TimeUtils.millis();
		}
		
	}
	public void UnsetCD() {reversecd -= 1000;}
	
	public boolean WithinScreen()
	{
		if(Relative_Location.x < 0 || Relative_Location.x > 0.8f || Relative_Location.y < 0 || Relative_Location.y > 1) return false;
		else return true;
	}
	public void Transform(Virus_State s)
	{
		State = s;
		switch(State)
		{
		case NORMAL:
			DisplaySprite.setTexture(Outside);
			break;
		case NEUTRALISED:
			DisplaySprite.setTexture(Neutralised);
			break;
		case UNSHEATHED:
			DisplaySprite.setTexture(Inside);
			break;
		default:
			break;
		}
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
		DisplaySprite.draw(SB);
	}

	@Override
	public void resize(int x, int y) {
		SCREEN_W = x;
		SCREEN_H = y;
		DisplaySprite.setBounds((Relative_Location.x-Relative_Size.x/2)*SCREEN_W, (Relative_Location.y-Relative_Size.y/2)*SCREEN_H, Relative_Size.x*SCREEN_W, Relative_Size.y*SCREEN_H);
		DisplaySprite.setOrigin(DisplaySprite.getWidth()/2, DisplaySprite.getHeight()/2);
	}
	
	public void SlowVirus(boolean Toggle)
	{
		if(Slowed == false && Toggle == true)
		{
			Slowed = true;
			Relative_Direction.y *= 0.5f;
			Relative_Direction.x *= 0.5f;
		}
		else if(Slowed == true && Toggle == false)
		{
			Slowed = false;
			Relative_Direction.x *= 2.0f;
			Relative_Direction.y *= 2.0f;
		}
		
	}
}
