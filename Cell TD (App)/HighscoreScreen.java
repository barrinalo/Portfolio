package com.pluripotence.celltd;


import java.util.Vector;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class HighscoreScreen implements Screen, InputProcessor{

	private OrthographicCamera camera;
	private SpriteBatch batch;
	private ResourceManager Rm;
	private int SCREEN_W, SCREEN_H;
	private MyGdxGame Parent;
	private GameTextLabel MainMenu, Board;
	private FileHandle HighscoreData;
	private Vector<Integer> Scores = new Vector<Integer>();
	private Button MuteButton;
	
	public HighscoreScreen(OrthographicCamera c, SpriteBatch b, ResourceManager r, MyGdxGame g)
	{
		camera = c;
		batch = b;
		Rm = r;
		SCREEN_W = Gdx.graphics.getWidth();
		SCREEN_H = Gdx.graphics.getHeight();
		Parent = g;
		
		Board = new GameTextLabel("Highest Score:\n\nPrevious Attempts:\n1.\n2.\n3.\n4.\n5.", SCREEN_W, SCREEN_H);
		Board.setTextFormat(GameTextLabel.TextFormat.NONE);
		Board.setHAlignment(GameTextLabel.HAlignment.LEFT);
		Board.setVAlignment(GameTextLabel.VAlignment.BOTTOM);
		Board.setBounds(0.05f, 0.15f, 0.9f, 0.8f);
		Board.wrapText(1.0f);
		
		MainMenu = new GameTextLabel("Menu", SCREEN_W, SCREEN_H);
		MainMenu.setTextFormat(GameTextLabel.TextFormat.EXPANDX_AND_Y);
		MainMenu.setHAlignment(GameTextLabel.HAlignment.LEFT);
		MainMenu.setVAlignment(GameTextLabel.VAlignment.BOTTOM);
		MainMenu.setBounds(0.4f, 0.0f, 0.2f, 0.05f);
		
		MuteButton = new Button(Rm.GetTexture("MusicOn.png"),new Vector2(0f,0.95f),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H);
		UpdateHighScore();
		
	}
	
	public void UpdateHighScore()
	{
		HighscoreData = Gdx.files.local("Highscore.txt");
		
		if(HighscoreData.exists())
		{
			Scores.clear();
			char[] d = HighscoreData.readString().toCharArray();
		
			String ss = "";
			for(int i = 0; i < d.length; i++)
			{
				if(d[i] == '\n')
				{
					Scores.add(new Integer(ss));
					ss = "";
				}
				else ss += d[i];
			}
			while(Scores.size() < 6) Scores.add(new Integer(0));
			Board.setText("Highest Score: " + Scores.get(0) + "\n\nPrevious Attempts:\n1. " + Scores.get(1) + "\n2. " + Scores.get(2) + "\n3. " + Scores.get(3) + "\n4. " + Scores.get(4) + "\n5. " + Scores.get(5));
			Board.setBounds(0.05f, 0.15f, 0.9f, 0.8f);
			Board.wrapText(1.0f);
		}
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
		if(MainMenu.IsWithin(screenX, screenY)) Parent.setScreen(Parent.MainMenu);
		if(MuteButton.IsWithin((float)screenX/(float)SCREEN_W, 1.0f - (float)screenY/(float)SCREEN_H))
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

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0.9f, 0.9f, 0);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		Board.Draw(batch);
		MainMenu.Draw(batch);
		MuteButton.Draw(batch);
		batch.end();
		
	}

	@Override
	public void resize(int width, int height) {
		SCREEN_W = width;
		SCREEN_H = height;
		MainMenu.resize(width, height);
		Board.resize(width, height);
		MuteButton.resize(width,height);
		
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(this);
		UpdateHighScore();
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

}
