package com.pluripotence.celltd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class LoadingScreen implements Screen, InputProcessor{
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private ResourceManager Rm;
	private int SCREEN_W, SCREEN_H;
	private MyGdxGame Parent;
	private GameTextLabel Msg;
	private boolean loaded;
	private class Loader extends Thread
	{
		public void run()
		{
			Rm.LoadTexture("Cell.png");
			Rm.LoadTexture("Retrovirus.png");
			Rm.LoadTexture("dsDNA.png");
			Rm.LoadTexture("RetrovirusNeutralised.png");
			Rm.LoadTexture("Mimivirus.png");
			Rm.LoadTexture("ssRNA.png");
			Rm.LoadTexture("Herpesvirus.png");
			Rm.LoadTexture("HerpesvirusNeutralised.png");
			Rm.LoadTexture("HerpesvirusUnsheathed.png");
			Rm.LoadTexture("Ribosome.png");
			Rm.LoadTexture("RestrictionEnzyme.png");
			Rm.LoadTexture("Antibody.png");
			Rm.LoadTexture("AntibodyVesicle.png");
			Rm.LoadTexture("BounceUpgradeIcon.png");
			Rm.LoadTexture("Confirm.png");
			Rm.LoadTexture("Cancel.png");
			Rm.LoadTexture("StrengthUpgradeIcon.png");
			Rm.LoadTexture("RibosomeFireRateIcon.png");
			Rm.LoadTexture("AutoAntibodyIcon.png");
			Rm.LoadTexture("Blank.png");
			Rm.LoadTexture("SlowVirusPowerUp.png");
			Rm.LoadTexture("AntibodyPowerUp.png");
			Rm.LoadTexture("DNARepairPowerUp.png");
			Rm.LoadTexture("IncreaseFireRatePowerUp.png");
			Rm.LoadTexture("CHNPowerUp.png");
			Rm.LoadTexture("Viruses.png");
			
			Parent.CreateScreens();
		}
	}
	public LoadingScreen(OrthographicCamera c, SpriteBatch b, ResourceManager r, MyGdxGame g)
	{
		loaded = false;
		camera = c;
		batch = b;
		Rm = r;
		SCREEN_W = Gdx.graphics.getWidth();
		SCREEN_H = Gdx.graphics.getHeight();
		Parent = g;
		
		Msg = new GameTextLabel("Loading Resources...", SCREEN_W,SCREEN_H);
		Msg.setColor(new Color(1.0f,1.0f,1.0f,1.0f));
		Msg.setTextFormat(GameTextLabel.TextFormat.EXPANDY);
		Msg.setHAlignment(GameTextLabel.HAlignment.MIDDLE);
		Msg.setVAlignment(GameTextLabel.VAlignment.MIDDLE);
		Msg.setBounds(0.5f, 0.5f, 0.9f, 0.1f);
		Msg.wrapText(1.0f);
		Msg.TotalAdj();
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
		if(loaded)	
		{
			Parent.setScreen(Parent.MainMenu);
		}
		return false;
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
		// TODO Auto-generated method stub
		Gdx.gl.glClearColor(0, 0.9f, 0.9f, 0);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		Msg.Draw(batch);
		batch.end();
		
		//loaded = LoadResources();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		SCREEN_W = width;
		SCREEN_H = height;
		Msg.resize(width, height);
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
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
