package com.pluripotence.celltd;

import java.util.Vector;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;


public class StartScreen implements Screen, InputProcessor{
	
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private ResourceManager Rm;
	private int SCREEN_W, SCREEN_H;
	private MyGdxGame Parent;
	private GameTextLabel Title, Start, Instructions, Quit, Highscore, Journal;
	private Vector<Sprite> Sprites;
	private long CurTime;
	private long AniTime;
	private Button MuteButton;
	
	StartScreen(OrthographicCamera c, SpriteBatch b, ResourceManager r, MyGdxGame g)
	{
		camera = c;
		batch = b;
		Rm = r;
		SCREEN_W = Gdx.graphics.getWidth();
		SCREEN_H = Gdx.graphics.getHeight();
		Parent = g;
		Sprites = new Vector<Sprite>();
		
		CurTime = TimeUtils.millis();
		AniTime = TimeUtils.millis();
		
		Title = new GameTextLabel("Cell TD",SCREEN_W,SCREEN_H);
		Title.setColor(new Color(250.0f/255.0f, 243.0f/255.0f, 55.0f/255.0f, 1.0f));
		Title.setTextFormat(GameTextLabel.TextFormat.EXPANDY);
		Title.setHAlignment(GameTextLabel.HAlignment.MIDDLE);
		Title.setVAlignment(GameTextLabel.VAlignment.MIDDLE);
		Title.setBounds(0.5f, 0.8f, 0.5f, 0.2f);
		Title.TotalAdj();
		
		Start = new GameTextLabel("Start Game",SCREEN_W,SCREEN_H);
		Start.setTextFormat(GameTextLabel.TextFormat.EXPANDY);
		Start.setHAlignment(GameTextLabel.HAlignment.MIDDLE);
		Start.setVAlignment(GameTextLabel.VAlignment.MIDDLE);
		Start.setBounds(0.5f, 0.5f, 0.5f, 0.05f);
		Start.TotalAdj();
		
		Instructions = new GameTextLabel("Tutorial",SCREEN_W,SCREEN_H);
		Instructions.setTextFormat(GameTextLabel.TextFormat.EXPANDY);
		Instructions.setHAlignment(GameTextLabel.HAlignment.MIDDLE);
		Instructions.setVAlignment(GameTextLabel.VAlignment.MIDDLE);
		Instructions.setBounds(0.5f, 0.4f, 0.5f, 0.05f);
		Instructions.TotalAdj();
		
		Highscore = new GameTextLabel("Highscore",SCREEN_W,SCREEN_H);
		Highscore.setTextFormat(GameTextLabel.TextFormat.EXPANDY);
		Highscore.setHAlignment(GameTextLabel.HAlignment.MIDDLE);
		Highscore.setVAlignment(GameTextLabel.VAlignment.MIDDLE);
		Highscore.setBounds(0.5f, 0.3f, 0.5f, 0.05f);
		Highscore.TotalAdj();
		
		Journal = new GameTextLabel("Micro Journal",SCREEN_W,SCREEN_H);
		Journal.setTextFormat(GameTextLabel.TextFormat.EXPANDY);
		Journal.setHAlignment(GameTextLabel.HAlignment.MIDDLE);
		Journal.setVAlignment(GameTextLabel.VAlignment.MIDDLE);
		Journal.setBounds(0.5f, 0.2f, 0.5f, 0.05f);
		Journal.TotalAdj();
		
		Quit = new GameTextLabel("Exit",SCREEN_W,SCREEN_H);
		Quit.setTextFormat(GameTextLabel.TextFormat.EXPANDY);
		Quit.setHAlignment(GameTextLabel.HAlignment.MIDDLE);
		Quit.setVAlignment(GameTextLabel.VAlignment.MIDDLE);
		Quit.setBounds(0.5f, 0.1f, 0.5f, 0.05f);
		Quit.TotalAdj();
		
		MuteButton = new Button(Rm.GetTexture("MusicOn.png"),new Vector2(0f,0.95f),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H);

	}
	@Override
	public void render(float delta) {
		
		if(TimeUtils.millis() - CurTime >= 1000)
		{
			double decision = Math.random();
			if(decision < 0.25f)
			{
			Sprites.add(new Sprite(Rm.GetTexture("Retrovirus.png")));
			Sprites.lastElement().setSize(SCREEN_W*0.05f, SCREEN_H*0.05f);
			Sprites.lastElement().setOrigin(Sprites.lastElement().getWidth()/2, Sprites.lastElement().getHeight()/2);
			Sprites.lastElement().setPosition((float)(Math.random()*SCREEN_W), SCREEN_H + Sprites.lastElement().getHeight());
			CurTime = TimeUtils.millis();
			}
			else if(decision >= 0.25f && decision< 0.5f)
			{
				Sprites.add(new Sprite(Rm.GetTexture("Mimivirus.png")));
				Sprites.lastElement().setSize(SCREEN_W*0.1f, SCREEN_H*0.1f);
				Sprites.lastElement().setOrigin(Sprites.lastElement().getWidth()/2, Sprites.lastElement().getHeight()/2);
				Sprites.lastElement().setPosition((float)(Math.random()*SCREEN_W), SCREEN_H + Sprites.lastElement().getHeight());
				CurTime = TimeUtils.millis();
			}
			else if(decision >= 0.5f && decision < 0.75f)
			{
				Sprites.add(new Sprite(Rm.GetTexture("Herpesvirus.png")));
				Sprites.lastElement().setSize(SCREEN_W*0.05f, SCREEN_H*0.05f);
				Sprites.lastElement().setOrigin(Sprites.lastElement().getWidth()/2, Sprites.lastElement().getHeight()/2);
				Sprites.lastElement().setPosition((float)(Math.random()*SCREEN_W), SCREEN_H + Sprites.lastElement().getHeight());
				CurTime = TimeUtils.millis();
			}
			else if(decision >= 0.75f)
			{
				Sprites.add(new Sprite(Rm.GetTexture("RabiesVirus.png")));
				Sprites.lastElement().setSize(SCREEN_W*0.05f, SCREEN_H*0.05f);
				Sprites.lastElement().setOrigin(Sprites.lastElement().getWidth()/2, Sprites.lastElement().getHeight()/2);
				Sprites.lastElement().setPosition((float)(Math.random()*SCREEN_W), SCREEN_H + Sprites.lastElement().getHeight());
				Sprites.lastElement().rotate90(false);
				CurTime = TimeUtils.millis();
			}
		}
		
		if(Sprites.size() != 0)
		{
			if(TimeUtils.millis() - AniTime >= 30)
			{
				for(int i = 0; i < Sprites.size(); i++) Sprites.get(i).translate(0.0f, -SCREEN_H/300);
				AniTime = TimeUtils.millis();
			}
		
			for(int i = 0; i < Sprites.size(); i++) if(Sprites.get(i).getBoundingRectangle().y <= -Sprites.get(i).getHeight()) Sprites.remove(i);
		}
		Gdx.gl.glClearColor(74.0f/255.0f, 214.0f/255.0f, 232.0f/255.0f, 0);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		if(Sprites.size() != 0) for(int i = 0; i < Sprites.size(); i++) Sprites.get(i).draw(batch);
		
		Title.Draw(batch);
		
			Start.Draw(batch);
			Instructions.Draw(batch);
			Highscore.Draw(batch);
			Journal.Draw(batch);
			Quit.Draw(batch);
		
			MuteButton.Draw(batch);
		batch.end();
		
	}

	@Override
	public void resize(int width, int height) {
		float wratio = (float)width/(float)SCREEN_W;
		float hratio = (float)height/(float)SCREEN_H;
		
		MuteButton.resize(width,height);
		Title.resize(width, height);
		Start.resize(width, height);
		Instructions.resize(width,  height);
		Highscore.resize(width, height);
		Journal.resize(width, height);
		Quit.resize(width, height);
		
		for(int i = 0; i < Sprites.size(); i++) Sprites.get(i).setBounds(Sprites.get(i).getX()*wratio, Sprites.get(i).getY()*hratio, Sprites.get(i).getWidth()*wratio, Sprites.get(i).getHeight()*hratio);
		
		SCREEN_W = width;
		SCREEN_H = height;
		
		camera.setToOrtho(false,SCREEN_W,SCREEN_H);
		camera.update();
		
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(this);
		SCREEN_W = Gdx.graphics.getWidth();
		SCREEN_H = Gdx.graphics.getHeight();
		camera.setToOrtho(false,SCREEN_W,SCREEN_H);
		camera.update();
		CurTime = TimeUtils.millis();
		AniTime = TimeUtils.millis();
		if(!Parent.BgMusic.isPlaying()) Parent.BgMusic.play();
		if(Parent.PlayMusic) MuteButton.ChangeSprite(Rm.GetTexture("MusicOn.png"));
		else MuteButton.ChangeSprite(Rm.GetTexture("MusicOff.png"));

	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		
		
		if(Start.IsWithin(screenX, screenY)) Parent.setScreen(Parent.CellTD);
		else if(Instructions.IsWithin(screenX, screenY)) Parent.setScreen(Parent.Tutorial);
		else if(Highscore.IsWithin(screenX, screenY)) Parent.setScreen(Parent.Highscore);
		else if(Journal.IsWithin(screenX, screenY)) Parent.setScreen(Parent.Journal);
		else if(Quit.IsWithin(screenX, screenY)) Gdx.app.exit();
		else if(MuteButton.IsWithin((float)screenX/(float)SCREEN_W, 1.0f - (float)screenY/(float)SCREEN_H))
		{
			Parent.Mute();
			if(Parent.PlayMusic) MuteButton.ChangeSprite(Rm.GetTexture("MusicOn.png"));
			else MuteButton.ChangeSprite(Rm.GetTexture("MusicOff.png"));
		}
		return true;
	}
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		
		return false;
	}
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

}
