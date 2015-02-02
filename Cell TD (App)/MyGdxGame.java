package com.pluripotence.celltd;

import android.app.Activity;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.revmob.RevMob;

public class MyGdxGame extends Game {
	
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private ResourceManager Rm;
	private RevMob revmob;
	private Activity ParentActivity;
	private String GameType;
	public StartScreen MainMenu;
	public GameScreen CellTD;
	public TutorialScreen Tutorial;
	public JournalScreen Journal;
	public HighscoreScreen Highscore;
	//public LoadingScreen Loading;
	public Music BgMusic, GameMusic, GameMusic2;
	public boolean PlayMusic;
	
	public MyGdxGame()
	{
		GameType = "desktop";
	}
	public MyGdxGame(RevMob rm, Activity ParentActivity)
	{
		revmob = rm;
		this.ParentActivity = ParentActivity;
		GameType = "android";
	}
	@Override
	public void create() {
		PlayMusic = true;
		
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		Rm = new ResourceManager();
		camera = new OrthographicCamera(w, h);
		batch = new SpriteBatch();
		
		//Loading = new LoadingScreen(camera, batch, Rm, this);
		
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
		Rm.LoadTexture("MusicOn.png");
		Rm.LoadTexture("MusicOff.png");
		Rm.LoadTexture("RabiesVirus.png");
		Rm.LoadTexture("RabiesVirusNeutralised.png");
		Rm.LoadTexture("ssDNA.png");
		
		CreateScreens();
		
		BgMusic = Gdx.audio.newMusic(Gdx.files.internal("data/Cell TD Menu.ogg"));
		BgMusic.setLooping(true);
		BgMusic.setVolume(0.2f);
		
		
		GameMusic = Gdx.audio.newMusic(Gdx.files.internal("data/Cell TD Main Theme.ogg"));
		GameMusic.setLooping(true);
		GameMusic.setVolume(0.2f);
		
		GameMusic2 = Gdx.audio.newMusic(Gdx.files.internal("data/Cell TD Main Theme2.ogg"));
		GameMusic2.setLooping(true);
		GameMusic2.setVolume(0.2f);
		
		BgMusic.play();
		setScreen(MainMenu);
		
		
	}
	public void Mute()
	{
		if(PlayMusic)
		{
				BgMusic.setVolume(0);
				GameMusic.setVolume(0);
				GameMusic2.setVolume(0);
				PlayMusic = false;
		}
		else
		{
			BgMusic.setVolume(0.2f);
			GameMusic.setVolume(0.2f);
			GameMusic2.setVolume(0.2f);
			PlayMusic = true;
		}
	}
	public void CreateScreens()
	{
		MainMenu = new StartScreen(camera, batch, Rm, this);
		CellTD = new GameScreen(camera, batch, Rm, this);
		if(GameType == "android") CellTD.SetRevMob(revmob, ParentActivity);
		Journal = new JournalScreen(camera, batch, Rm, this);
		Highscore = new HighscoreScreen(camera, batch, Rm, this);
		Tutorial = new TutorialScreen(camera, batch, Rm, this);
	}
	@Override
	public void dispose() {
	}

	@Override
	public void resize(int width, int height) {
		if(MainMenu != null) MainMenu.resize(width, height);
		if(CellTD != null )CellTD.resize(width, height);
		if(Journal != null) Journal.resize(width,height);
		if(Tutorial != null) Tutorial.resize(width, height);
		//if(Loading != null) Loading.resize(width, height);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

}
