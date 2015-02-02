package com.codelessweb.rank2048;

import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;

public class DisplayText {
	
	public Label Text;
	protected String WrappingStyle;
	
	public DisplayText(String Txt, LabelStyle Style) {
		Text = new Label(Txt, Style);
	}
	
	public void SetText(String Txt) {
		Text.setText(Txt);
		if(WrappingStyle.equals("w")) {
			TextBounds Tb = Text.getTextBounds();
			if(Math.abs(1.0f - Tb.width/Text.getWidth()) > 0.05f) {
				if(Tb.width > Text.getWidth()) {
					float ScaleFactor = 1.0f;
					while(Tb.width > Text.getWidth()) {
						ScaleFactor *= 0.95f;
						Text.setFontScale(ScaleFactor);
						Tb = Text.getTextBounds();
					}
				}
				else {
					float ScaleFactor = 1.0f;
					while(Tb.width < Text.getWidth()) {
						ScaleFactor *= 1.05f;
						Text.setFontScale(ScaleFactor);
						Tb = Text.getTextBounds();
					}
				}
			}
		}
		else if(WrappingStyle.equals("h")) {
			TextBounds Tb = Text.getTextBounds();
			if(Math.abs(1.0f - Tb.height/Text.getHeight()) > 0.05f) {
				if(Tb.height > Text.getHeight()) {
					float ScaleFactor = 1.0f;
					while(Tb.height > Text.getHeight()) {
						ScaleFactor *= 0.95f;
						Text.setFontScale(ScaleFactor);
						Tb = Text.getTextBounds();
					}
				}
				else {
					float ScaleFactor = 1.0f;
					while(Tb.height < Text.getHeight()) {
						ScaleFactor *= 1.05f;
						Text.setFontScale(ScaleFactor);
						Tb = Text.getTextBounds();
					}
				}
			}
		}
		else if(WrappingStyle.equals("wh")) {
			TextBounds Tb = Text.getTextBounds();
			if(Math.abs(1.0f - Tb.width/Text.getWidth()) > 0.05f) {
				if(Tb.width > Text.getWidth()) {
					float ScaleFactor = 1.0f;
					while(Tb.width > Text.getWidth()) {
						ScaleFactor *= 0.95f;
						Text.setFontScaleX(ScaleFactor);
						Tb = Text.getTextBounds();
					}
				}
				else {
					float ScaleFactor = 1.0f;
					while(Tb.width < Text.getWidth()) {
						ScaleFactor *= 1.05f;
						Text.setFontScaleX(ScaleFactor);
						Tb = Text.getTextBounds();
					}
				}
			}
			
			Tb = Text.getTextBounds();
			if(Math.abs(1.0f - Tb.height/Text.getHeight()) > 0.05f) {
				if(Tb.height > Text.getHeight()) {
					float ScaleFactor = 1.0f;
					while(Tb.height > Text.getHeight()) {
						ScaleFactor *= 0.95f;
						Text.setFontScaleY(ScaleFactor);
						Tb = Text.getTextBounds();
					}
				}
				else {
					float ScaleFactor = 1.0f;
					while(Tb.height < Text.getHeight()) {
						ScaleFactor *= 1.05f;
						Text.setFontScaleY(ScaleFactor);
						Tb = Text.getTextBounds();
					}
				}
			}
		}
	}
	
	public void SetPosAndWidth(float x, float y, float w) {
		WrappingStyle = "w";
		Text.setPosition(x, y);
		Text.setWidth(w);
		TextBounds Tb = Text.getTextBounds();
		
		if(Tb.width > Text.getWidth()) {
			float ScaleFactor = 1.0f;
			while(Tb.width > Text.getWidth()) {
				ScaleFactor *= 0.95f;
				Text.setFontScale(ScaleFactor);
				Tb = Text.getTextBounds();
			}
		}
		else {
			float ScaleFactor = 1.0f;
			while(Tb.width < Text.getWidth()) {
				ScaleFactor *= 1.05f;
				Text.setFontScale(ScaleFactor);
				Tb = Text.getTextBounds();
			}
		}
	}
	public void SetPosAndHeight(float x, float y, float h) {
		WrappingStyle = "h";
		Text.setPosition(x, y);
		Text.setHeight(h);
		TextBounds Tb = Text.getTextBounds();
		if(Tb.height > Text.getHeight()) {
			float ScaleFactor = 1.0f;
			while(Tb.height > Text.getHeight()) {
				ScaleFactor *= 0.95f;
				Text.setFontScale(ScaleFactor);
				Tb = Text.getTextBounds();
			}
		}
		else {
			float ScaleFactor = 1.0f;
			while(Tb.height < Text.getHeight()) {
				ScaleFactor *= 1.05f;
				Text.setFontScale(ScaleFactor);
				Tb = Text.getTextBounds();
			}
		}
	}
	public void SetPosAndXY(float x, float y, float w, float h) {
		WrappingStyle = "wh";
		Text.setBounds(x, y, w, h);
		TextBounds Tb = Text.getTextBounds();
		
		if(Tb.width > Text.getWidth()) {
			float ScaleFactor = 1.0f;
			while(Tb.width > Text.getWidth()) {
				ScaleFactor *= 0.95f;
				Text.setFontScaleX(ScaleFactor);
				Tb = Text.getTextBounds();
			}
		}
		else {
			float ScaleFactor = 1.0f;
			while(Tb.width < Text.getWidth()) {
				ScaleFactor *= 1.05f;
				Text.setFontScaleX(ScaleFactor);
				Tb = Text.getTextBounds();
			}
		}
		
		Tb = Text.getTextBounds();
		if(Tb.height > Text.getHeight()) {
			float ScaleFactor = 1.0f;
			while(Tb.height > Text.getHeight()) {
				ScaleFactor *= 0.95f;
				Text.setFontScaleY(ScaleFactor);
				Tb = Text.getTextBounds();
			}
		}
		else {
			float ScaleFactor = 1.0f;
			while(Tb.height < Text.getHeight()) {
				ScaleFactor *= 1.05f;
				Text.setFontScaleY(ScaleFactor);
				Tb = Text.getTextBounds();
			}
		}
	}
}
