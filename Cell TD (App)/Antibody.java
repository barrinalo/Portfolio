package com.pluripotence.celltd;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Antibody implements Entity, Projectile{

	private Vector2 Relative_Location, Relative_Size,Insidedir,Outsidedir;
	private boolean OutsideCell;
	private int SCREEN_W, SCREEN_H;
	private Texture Outside, Inside;
	private Sprite DisplaySprite;
	
	Antibody(Texture OutsideSprite, Texture InsideSprite, Vector2 RSize, Vector2 Target, int W,int H)
	{
		Outside = OutsideSprite;
		Inside = InsideSprite;
		Relative_Size = RSize;
		OutsideCell = false;
		SCREEN_W = W;
		SCREEN_H = H;
		Outsidedir = new Vector2();
		Insidedir = new Vector2();
		Outsidedir.x = (Target.x - 0.4f)/250.0f;
		Outsidedir.y = (Target.y - 0.5f)/250.0f;

		double xtar = Math.sqrt(Math.pow(0.36f,2)/(Math.pow(Outsidedir.y/Outsidedir.x,2)+Math.pow(0.36f/0.27f,2)));
		if(Target.x <= 0.4f && xtar >= 0.0f) xtar *= -1.0f;
		double ytar = Math.sqrt(Math.pow(0.36,2)-Math.pow(0.36f/0.27f*xtar, 2));
		if(Target.y <= 0.5f && ytar >= 0.0f) ytar *= -1.0f;
		
		if(Target.x <= 0.4f && Target.y >= 0.5f)
		{
			Relative_Location = new Vector2(0.3f,0.6f);
			Insidedir.x = (float) (xtar + 0.1f);
			Insidedir.y = (float) (ytar - 0.1f);
		}
		else if(Target.x <= 0.4f && Target.y < 0.5f)
		{
			Relative_Location = new Vector2(0.32f, 0.38f);
			Insidedir.x = (float) (xtar + 0.08f);
			Insidedir.y = (float) (ytar + 0.12f);
		}
		else if(Target.x > 0.4f && Target.y < 0.5f)
		{
			Relative_Location = new Vector2(0.49f,0.39f);
			Insidedir.x = (float) (xtar - 0.09f);
			Insidedir.y = (float) (ytar + 0.11f);
		}
		else if(Target.x > 0.4f && Target.y >= 0.5f)
		{
			Relative_Location = new Vector2(0.49f,0.61f);
			Insidedir.x = (float) (xtar - 0.09f);
			Insidedir.y = (float) (ytar - 0.11f);
		}
		double n = Math.sqrt(Math.pow(Insidedir.x,2)+Math.pow(Insidedir.y,2));
		Insidedir.x /= (n*150);
		Insidedir.y /= (n*150);
		
		DisplaySprite = new Sprite(Inside);
		DisplaySprite.setBounds((Relative_Location.x-Relative_Size.x/2)*SCREEN_W, (Relative_Location.y-Relative_Size.y/2)*SCREEN_H, Relative_Size.x*SCREEN_W, Relative_Size.y*SCREEN_H);
		DisplaySprite.setOrigin(DisplaySprite.getWidth()/2, DisplaySprite.getHeight()/2);
		
	}
	
	public Vector2 GetPosition() {return Relative_Location;}
	public boolean GetState() {return OutsideCell;}
	
	public boolean WithinScreen()
	{
		if(Relative_Location.x < 0 || Relative_Location.x > 0.8f || Relative_Location.y < 0 || Relative_Location.y > 1) return false;
		else return true;
	}
	
	public void Transform()
	{
		OutsideCell = true;
		DisplaySprite.setTexture(Outside);
		DisplaySprite.setBounds((Relative_Location.x-Relative_Size.x/2)*SCREEN_W, (Relative_Location.y-Relative_Size.y/2)*SCREEN_H, Relative_Size.x*SCREEN_W, Relative_Size.y*SCREEN_H);
		DisplaySprite.setOrigin(DisplaySprite.getWidth()/2, DisplaySprite.getHeight()/2);
	}
	@Override
	public void Move() {
		if(OutsideCell)
		{
		Relative_Location.x += Outsidedir.x;
		Relative_Location.y += Outsidedir.y;
		DisplaySprite.translate(Outsidedir.x*SCREEN_W, Outsidedir.y*SCREEN_H);
		}
		else
		{
			Relative_Location.x += Insidedir.x;
			Relative_Location.y += Insidedir.y;
			DisplaySprite.translate(Insidedir.x*SCREEN_W, Insidedir.y*SCREEN_H);
		}
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
