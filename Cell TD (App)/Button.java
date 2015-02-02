package com.pluripotence.celltd;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;


public class Button implements Entity, Clickable{

	private Vector2 Relative_Location, Relative_Size;
	private Sprite DisplaySprite;
	private int SCREEN_W,SCREEN_H;
	
	public Button(Texture img, Vector2 Rloc, Vector2 Rsize, int W, int H)
	{
		SCREEN_W = W;
		SCREEN_H = H;
		Relative_Location = Rloc;
		Relative_Size = Rsize;
		DisplaySprite = new Sprite(img);
		DisplaySprite.setBounds((Relative_Location.x)*SCREEN_W, (Relative_Location.y)*SCREEN_H, Relative_Size.x*SCREEN_W, Relative_Size.y*SCREEN_H);
		DisplaySprite.setOrigin(DisplaySprite.getWidth()/2, DisplaySprite.getHeight()/2);
	}
	public void ChangeSprite(Texture img)
	{
		DisplaySprite.setTexture(img);
	}
	@Override
	public void OnClick(float x, float y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean CheckIntersect(double inx, double iny, double inRadius) {
		// TODO Auto-generated method stub
		return false;
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
		DisplaySprite.setBounds((Relative_Location.x)*SCREEN_W, (Relative_Location.y)*SCREEN_H, Relative_Size.x*SCREEN_W, Relative_Size.y*SCREEN_H);
		DisplaySprite.setOrigin(DisplaySprite.getWidth()/2, DisplaySprite.getHeight()/2);
	}

}
