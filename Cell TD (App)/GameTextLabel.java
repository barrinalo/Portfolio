package com.pluripotence.celltd;

import java.util.Vector;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class GameTextLabel implements Entity, Clickable{

	private Label DisplayText;
	private int SCREEN_W, SCREEN_H;
	private Vector2 Relative_Location;
	private Vector2 Relative_Size;
	private boolean textwrap;
	private String originaltext;
	private boolean Highlight;
	private Color HighlightColor;
	private OrthographicCamera camera;
	private ShapeRenderer SR;
	//private float AvgFontScale;
	public enum TextFormat
	{
		EXPANDX_AND_Y,
		EXPANDX,
		EXPANDY,
		NONE
	};
	public enum HAlignment
	{
		LEFT,
		MIDDLE,
		RIGHT
	}
	public enum VAlignment
	{
		TOP,
		MIDDLE,
		BOTTOM
	}
	TextFormat tf;
	HAlignment Halign;
	VAlignment Valign;

	public GameTextLabel(String Text, int SCREEN_W, int SCREEN_H)
	{
		//AvgFontScale = 0.0f;
		originaltext = Text;
		Highlight = false;
		HighlightColor = new Color(1,0,0,0.5f);
		textwrap = false;
		DisplayText = new Label(Text, new Label.LabelStyle(new BitmapFont(Gdx.files.internal("data/CellTdFont.fnt"),false) ,new Color(1.0f,1.0f,1.0f,1.0f)));
		Relative_Location = new Vector2();
		Relative_Size = new Vector2();
		this.SCREEN_W = SCREEN_W;
		this.SCREEN_H = SCREEN_H;
		DisplayText.setOrigin(DisplayText.getWidth()/2,DisplayText.getHeight()/2);
		tf = TextFormat.EXPANDX_AND_Y;
		Halign = HAlignment.LEFT;
		Valign = VAlignment.TOP;
		Relative_Location.x = DisplayText.getX()/(float)this.SCREEN_W;
		Relative_Location.y = DisplayText.getY()/(float)this.SCREEN_H;
		Relative_Size.x = DisplayText.getWidth()/(float)this.SCREEN_W;
		Relative_Size.y = DisplayText.getHeight()/(float)this.SCREEN_H;
	}
	
	public Vector2 GetRelativeLocation() {return Relative_Location;}
	public Vector2 GetRelativeSize() {return Relative_Size;}
	public void setTextFormat(TextFormat tf){ this.tf = tf;}
	public void setHAlignment(HAlignment hf){Halign = hf;}
	public void setVAlignment(VAlignment vf){Valign = vf;}
	public void setText(String Text)
	{
		originaltext = Text;
		DisplayText.setText(Text);
		if(DisplayText.getTextBounds().width > DisplayText.getWidth() || DisplayText.getTextBounds().height > DisplayText.getHeight()) setBounds(Relative_Location.x,Relative_Location.y,Relative_Size.x,Relative_Size.y);
	}
	
	public void setColor(Color c){DisplayText.setColor(c);}
	
	public void SetHighlight(boolean h, ShapeRenderer SR, OrthographicCamera camera)
	{
		this.SR = SR;
		Highlight = h;
		this.camera = camera;
	}
	public void wrapText(float fontScale)
	{
		if(fontScale == 0.0f)
		{
			textwrap = false;
		}
		else
		{
			textwrap = true;
			float fontscale = fontScale;
			Vector<String> s = new Vector<String>();
			CharSequence s2 = originaltext;
			int tempnumber = 0;
			for(int i = 0; i < s2.length(); i++)
			{
				if(s2.charAt(i) == ' ')
				{
					s.add(s2.subSequence(tempnumber, i).toString());
					tempnumber = i;
				}
			}
			s.add(s2.subSequence(tempnumber, s2.length()).toString());
			do
			{
				DisplayText.setFontScale(fontscale);
				DisplayText.setText("");
				String s3 = "";
				
				for(int i = 0; i < s.size(); i++)
				{
					DisplayText.setText(DisplayText.getText() + s.get(i));
					if(DisplayText.getTextBounds().width > DisplayText.getWidth())
					{
						s3 += "\n";
						s3 += s.get(i);
					}
					else
					{
						s3 += s.get(i);
					}
					DisplayText.setText(s3);
				}
				if(DisplayText.getHeight() < DisplayText.getTextBounds().height) fontscale *= 0.9f;
			}	while(DisplayText.getHeight() < DisplayText.getTextBounds().height);
		}
	}
	public void setBounds(float x, float y, float width, float height)
	{
		Relative_Location.x = x;
		Relative_Location.y = y;
		Relative_Size.x = width;
		Relative_Size.y = height;
		DisplayText.setBounds(x*SCREEN_W, y*SCREEN_H, width*SCREEN_W, height*SCREEN_H);
		
		if(tf == TextFormat.EXPANDX_AND_Y)
		{
			float fontscale = 1.0f;
			DisplayText.setFontScale(fontscale);
			if(DisplayText.getTextBounds().width > DisplayText.getWidth())
			{
				while(DisplayText.getTextBounds().width > DisplayText.getWidth())
				{
					fontscale -= 0.01f;
					DisplayText.setFontScaleX(fontscale);
				}
			}
			else
			{
				while(DisplayText.getTextBounds().width < DisplayText.getWidth())
				{
					fontscale += 0.01f;
					DisplayText.setFontScaleX(fontscale);
				}
			}
			fontscale = 1.0f;
			if(DisplayText.getTextBounds().height >  DisplayText.getHeight())
			{
				while(DisplayText.getTextBounds().height > DisplayText.getHeight())
				{
					fontscale -= 0.01f;
					DisplayText.setFontScaleY(fontscale);
				}
			}
			else
			{
				while(DisplayText.getTextBounds().height < DisplayText.getHeight())
				{
					fontscale += 0.01f;
					DisplayText.setFontScaleY(fontscale);
				}
			}
		}
		else if(tf == TextFormat.EXPANDX)
		{
			float fontscale = 1.0f;
			DisplayText.setFontScale(fontscale);
			if(DisplayText.getTextBounds().width > DisplayText.getWidth())
			{
				while(DisplayText.getTextBounds().width > DisplayText.getWidth())
				{
					fontscale -= 0.01f;
					DisplayText.setFontScale(fontscale);
				}
			}
			else
			{
				while(DisplayText.getTextBounds().width < DisplayText.getWidth())
				{
					fontscale += 0.01f;
					DisplayText.setFontScale(fontscale);
				}
			}
			DisplayText.setHeight(DisplayText.getTextBounds().height);
			fontscale = 1.0f;
			if(DisplayText.getTextBounds().height >  DisplayText.getHeight())
			{
				while(DisplayText.getTextBounds().height > DisplayText.getHeight())
				{
					fontscale -= 0.01f;
					DisplayText.setFontScaleY(fontscale);
				}
			}
			else
			{
				while(DisplayText.getTextBounds().height < DisplayText.getHeight())
				{
					fontscale += 0.01f;
					DisplayText.setFontScaleY(fontscale);
				}
			}
			
		}
		else if(tf == TextFormat.EXPANDY)
		{
			float fontscale = 1.0f;
			DisplayText.setFontScale(fontscale);
			if(DisplayText.getTextBounds().height >  DisplayText.getHeight())
			{
				while(DisplayText.getTextBounds().height > DisplayText.getHeight())
				{
					fontscale -= 0.01f;
					DisplayText.setFontScale(fontscale);
				}
			}
			else
			{
				while(DisplayText.getTextBounds().height < DisplayText.getHeight())
				{
					fontscale += 0.01f;
					DisplayText.setFontScale(fontscale);
				}
			}
			DisplayText.setWidth(DisplayText.getTextBounds().width);
			fontscale = 1.0f;
			if(DisplayText.getTextBounds().width > DisplayText.getWidth())
			{
				while(DisplayText.getTextBounds().width > DisplayText.getWidth())
				{
					fontscale -= 0.01f;
					DisplayText.setFontScaleX(fontscale);
				}
			}
			else
			{
				while(DisplayText.getTextBounds().width < DisplayText.getWidth())
				{
					fontscale += 0.01f;
					DisplayText.setFontScaleX(fontscale);
				}
			}
		}
	}
	
	public void HorizontalAdj()
	{
		if(Halign == HAlignment.LEFT) DisplayText.setX(Relative_Location.x * SCREEN_W);
		else if(Halign == HAlignment.MIDDLE) DisplayText.setX(Relative_Location.x * SCREEN_W - DisplayText.getWidth()/2);
		else if(Halign == HAlignment.RIGHT) DisplayText.setX(Relative_Location.x * SCREEN_W - DisplayText.getWidth());
	}
	public void VerticalAdj()
	{
		if(Valign == VAlignment.TOP) DisplayText.setY(Relative_Location.y * SCREEN_H + Relative_Size.y*SCREEN_H-DisplayText.getTextBounds().height/2); 
		else if(Valign == VAlignment.MIDDLE) DisplayText.setY(Relative_Location.y * SCREEN_H - DisplayText.getTextBounds().height/2); 
		else if(Valign == VAlignment.BOTTOM) DisplayText.setY(Relative_Location.y * SCREEN_H);
	}
	public void TotalAdj()
	{
		HorizontalAdj();
		VerticalAdj();
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
		if(inx >= DisplayText.getX() && inx <= DisplayText.getRight() && iny <= SCREEN_H - DisplayText.getY() && iny >= SCREEN_H - DisplayText.getTop()) return true;
		else return false;
	}

	@Override
	public void Draw(SpriteBatch SB) {
		if(Highlight)
		{
			Gdx.gl.glEnable(GL10.GL_BLEND);
			Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			SR.setProjectionMatrix(camera.combined);
			SR.begin(ShapeType.FilledRectangle);
			SR.setColor(HighlightColor);
			SR.filledRect(Relative_Location.x*(float)SCREEN_W , Relative_Location.y*(float)SCREEN_H, Relative_Size.x*SCREEN_W, Relative_Size.y*SCREEN_H);
			SR.end();
			Gdx.gl.glDisable(GL10.GL_BLEND);
		}
		DisplayText.draw(SB, 1.0f);
	}

	@Override
	public void resize(int x, int y) {
		SCREEN_W = x;
		SCREEN_H = y;
		setBounds(Relative_Location.x,Relative_Location.y,Relative_Size.x,Relative_Size.y);
		if(textwrap == true)
		{
			wrapText(1.0f);
		}
		TotalAdj();
	}

}
