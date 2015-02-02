package com.pluripotence.celltd;

import java.util.Vector;

import android.app.Activity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.revmob.RevMob;
import com.revmob.ads.fullscreen.RevMobFullscreen;


public class GameScreen implements Screen, InputProcessor{

	private RevMob revmob;
	private RevMobFullscreen fullscreen;
	private Activity ParentActivity;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private ResourceManager Rm;
	private int SCREEN_W, SCREEN_H;
	private MyGdxGame Parent;
	private ShapeRenderer SR;
	private Sprite CellBG;
	private static int Carbon, Nitrogen, Phosphorous, Hydrogen, Corruption, Killcount, Spawncount, AntibodyCount;  //Player resources
	private static int EnzymeStrengthLevel, EnzymeBounceLevel, RibosomeFireRateLevel, AntibodyGenerateLevel;
	private boolean AutoAntibodySwitch;
	private GameTextLabel Stats, Description, AutoAntibodyToggle, Upgrades;
	private Button BounceUpgradeButton, StrengthUpgradeButton, RibosomeFireRateUpgradeButton, AutoAntibodyUpgradeButton, ConfirmButton, CancelButton;
	private long GameTimer, VirusCD, FPSlim, AntibodyGenerateCD;
	private Vector<Virus> Virus_Spawner;
	private Vector<Ribosome> Ribosome_Spawner;
	private Vector<Enzyme> Enzyme_Spawner;
	private Vector<Antibody> Antibody_Spawner;
	private Vector<Sprite> AntibodyInterface;
	private Vector<PowerUp> PowerUp_Spawner;
	private boolean GameOver;
	private GameTextLabel GameOverText;
	private Button MuteButton;
	// Upgrade flags
	private boolean SlowViruses,IncreaseFireRate;
	private long RibosomeFireRateCD;
	private enum MenuState
	{
		NONE,
		BOUNCE_UPGRADE,
		STRENGTH_UPGRADE,
		FIRE_RATE_UPGRADE,
		ANTIBODY_UPGRADE
	};
	private MenuState InterfaceState;
	
	public GameScreen(OrthographicCamera c, SpriteBatch b, ResourceManager r, MyGdxGame g)
	{
		SR = new ShapeRenderer();
		camera = c;
		batch = b;
		Rm = r;
		SCREEN_W = Gdx.graphics.getWidth();
		SCREEN_H = Gdx.graphics.getHeight();
		Parent = g;
		Carbon = 800;
		Nitrogen = 400;
		Hydrogen = 1600;
		Phosphorous = 0;
		Corruption = 0;
		Killcount = 0;
		EnzymeStrengthLevel = 1;
		EnzymeBounceLevel = 1;
		RibosomeFireRateLevel = 1;
		AntibodyGenerateLevel = 0;
		Spawncount = 0;
		GameOver = false;
		AutoAntibodySwitch = false;
		AntibodyCount = 4;
		RibosomeFireRateCD = 2200 - RibosomeFireRateLevel * 200;
		InterfaceState = MenuState.NONE;
		GameTimer = TimeUtils.millis();
		VirusCD = TimeUtils.millis();
		AntibodyGenerateCD = TimeUtils.millis();
		FPSlim = TimeUtils.millis();
		Virus_Spawner = new Vector<Virus>();
		Ribosome_Spawner = new Vector<Ribosome>();
		Enzyme_Spawner = new Vector<Enzyme>();
		Antibody_Spawner = new Vector<Antibody>();
		PowerUp_Spawner = new Vector<PowerUp>();
		
		SlowViruses = false;
		IncreaseFireRate = false;
		
		CellBG = new Sprite(Rm.GetTexture("Cell.png"));
		CellBG.setSize(SCREEN_W*0.6f, SCREEN_H*0.8f);
		CellBG.setOrigin(CellBG.getWidth()/2, CellBG.getHeight()/2);
		CellBG.setPosition(0.4f*SCREEN_W - CellBG.getWidth()/2, 0.5f*SCREEN_H - CellBG.getHeight()/2);
		
		Stats = new GameTextLabel("Carbon: " + Carbon + "\nNitrogen: " + Nitrogen + "\nHydrogen: " + Hydrogen + "\nPhosphorous: " + Phosphorous + "\nDNA Corrupted: " + Corruption + "\nScore: " + Killcount + "\nAntibody Vesicles:",SCREEN_W,SCREEN_H);
		Stats.setBounds(0.8f, 0.75f, 0.2f, 0.25f);
		Stats.setTextFormat(GameTextLabel.TextFormat.EXPANDX_AND_Y);
		Stats.setHAlignment(GameTextLabel.HAlignment.LEFT);
		Stats.setVAlignment(GameTextLabel.VAlignment.BOTTOM);
		Stats.TotalAdj();
		Stats.SetHighlight(true, SR, camera);
		
		Description = new GameTextLabel(" ",SCREEN_W,SCREEN_H);
		Description.setBounds(0.8f, 0.05f,0.2f,0.4f);
		Description.setTextFormat(GameTextLabel.TextFormat.NONE);
		Description.setHAlignment(GameTextLabel.HAlignment.LEFT);
		Description.setVAlignment(GameTextLabel.VAlignment.BOTTOM);
		Description.wrapText(0.2f);
		Description.TotalAdj();
		
		AutoAntibodyToggle = new GameTextLabel("Turn on Auto-Antibodies",SCREEN_W,SCREEN_H);
		AutoAntibodyToggle.setBounds(0.8f, 0.65f, 0.2f, 0.03f);
		AutoAntibodyToggle.setTextFormat(GameTextLabel.TextFormat.EXPANDX_AND_Y);
		AutoAntibodyToggle.setHAlignment(GameTextLabel.HAlignment.LEFT);
		AutoAntibodyToggle.setVAlignment(GameTextLabel.VAlignment.BOTTOM);
		
		Upgrades = new GameTextLabel("Upgrades Available:", SCREEN_W, SCREEN_H);
		Upgrades.setBounds(0.8f, 0.6f, 0.2f, 0.03f);
		Upgrades.setTextFormat(GameTextLabel.TextFormat.EXPANDX_AND_Y);
		Upgrades.setHAlignment(GameTextLabel.HAlignment.LEFT);
		Upgrades.setVAlignment(GameTextLabel.VAlignment.BOTTOM);
		
		GameOverText = new GameTextLabel("GAME OVER", SCREEN_W, SCREEN_H);
		GameOverText.setBounds(0.5f, 0.5f, 0.5f, 0.25f);
		GameOverText.setColor(new Color(1.0f,0.0f,0.0f,1.0f));
		GameOverText.setVAlignment(GameTextLabel.VAlignment.MIDDLE);
		GameOverText.setHAlignment(GameTextLabel.HAlignment.MIDDLE);
		GameOverText.setTextFormat(GameTextLabel.TextFormat.EXPANDX);
		GameOverText.TotalAdj();
		
		BounceUpgradeButton = new Button(Rm.GetTexture("BounceUpgradeIcon.png"), new Vector2(0.816f,0.525f), new Vector2(0.075f,0.0734f),SCREEN_W,SCREEN_H);
		StrengthUpgradeButton = new Button(Rm.GetTexture("StrengthUpgradeIcon.png"), new Vector2(0.906f,0.525f), new Vector2(0.075f,0.0734f),SCREEN_W,SCREEN_H);
		RibosomeFireRateUpgradeButton = new Button(Rm.GetTexture("RibosomeFireRateIcon.png"), new Vector2(0.816f,0.45f), new Vector2(0.075f,0.0734f),SCREEN_W,SCREEN_H);
		AutoAntibodyUpgradeButton = new Button(Rm.GetTexture("AutoAntibodyIcon.png"), new Vector2(0.906f,0.45f), new Vector2(0.075f,0.0734f), SCREEN_W, SCREEN_H);
		ConfirmButton = new Button(Rm.GetTexture("Confirm.png"), new Vector2(0.8f,0),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H);
		CancelButton = new Button(Rm.GetTexture("Cancel.png"), new Vector2(0.95f,0),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H);
		
		AntibodyInterface = new Vector<Sprite>();
		for(int i = 0; i < 4; i++)
		{
			AntibodyInterface.add(new Sprite(Rm.GetTexture("AntibodyVesicle.png")));
			AntibodyInterface.get(i).setBounds((0.8f+i*0.05f)*SCREEN_W, 0.70f*SCREEN_H, 0.05f*SCREEN_W, 0.05f*SCREEN_H);
			AntibodyInterface.get(i).setOrigin(AntibodyInterface.get(i).getWidth()/2, AntibodyInterface.get(i).getHeight()/2);
		}
		MuteButton = new Button(Rm.GetTexture("MusicOn.png"),new Vector2(0f,0.95f),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H);
	}
	
	public void SetRevMob(RevMob rm, Activity PA)
	{
		revmob = rm;
		ParentActivity = PA;
		fullscreen = revmob.createFullscreen(ParentActivity, null);
	}
	public void Reset()
	{
		Parent.GameMusic.stop();
		Parent.GameMusic2.stop();
		Carbon = 800;
		Nitrogen = 400;
		Hydrogen = 1600;
		Phosphorous = 0;
		Corruption = 0;
		Killcount = 0;
		EnzymeStrengthLevel = 1;
		EnzymeBounceLevel = 1;
		RibosomeFireRateLevel = 1;
		AntibodyGenerateLevel = 0;
		Spawncount = 0;
		AntibodyCount = 4;
		RibosomeFireRateCD = 2200 - RibosomeFireRateLevel * 200;
		GameOver = false;
		AutoAntibodySwitch = false;
		InterfaceState = MenuState.NONE;
		Description.setText("");
		GameTimer = TimeUtils.millis();
		VirusCD = TimeUtils.millis();
		AntibodyGenerateCD = TimeUtils.millis();
		FPSlim = TimeUtils.millis();
		Virus_Spawner.clear();
		Ribosome_Spawner.clear();
		Enzyme_Spawner.clear();
		Antibody_Spawner.clear();
		PowerUp_Spawner.clear();
		SlowViruses = false;
		IncreaseFireRate = false;
		fullscreen = revmob.createFullscreen(ParentActivity, null);
	}
	
	public Vector2 Reflect(Vector2 IncidentVec, Vector2 pos)
	{
		Vector2 ResultantVec = new Vector2();
		Vector2 Gradient = new Vector2();
		
		Gradient.y = (float) ((Math.pow(0.36f/0.27f, 2)*(-(pos.x-0.4f))/(Math.sqrt(Math.pow(0.36f,2)-Math.pow(0.36f/0.27f*(pos.x-0.4f),2)))));
		Gradient.x = 1.0f;
		double Gradientn = Math.sqrt(Math.pow(Gradient.x,2) + Math.pow(Gradient.y,2));
		Gradient.y /= Gradientn;
		Gradient.x /= Gradientn;
		if(pos.y < 0.5f) Gradient.y *= -1.0f;
		double costheta;
		if(IncidentVec.x >= 0)
		{
			costheta = (IncidentVec.x*Gradient.x + IncidentVec.y*Gradient.y);
			ResultantVec.x = (float) (2*costheta*Gradient.x-IncidentVec.x);
			ResultantVec.y = (float) (2*costheta*Gradient.y-IncidentVec.y);
		}
		else
		{
			costheta = (-IncidentVec.x*Gradient.x + -IncidentVec.y*Gradient.y);
			ResultantVec.x = (float) (-2*costheta*Gradient.x-IncidentVec.x);
			ResultantVec.y = (float) (-2*costheta*Gradient.y-IncidentVec.y);
		}
		
		double n = Math.sqrt(Math.pow(ResultantVec.x,2) + Math.pow(ResultantVec.y,2));
		ResultantVec.y /= n;
		ResultantVec.x /= n;
		ResultantVec.x /= 300;
		ResultantVec.y /= 300;
		
		return ResultantVec;
	}
	public boolean WithinCell(Vector2 vec)
	{
		double lim, majaxis;
		majaxis = Math.pow(0.36f, 2);
		lim = Math.sqrt(majaxis-majaxis*Math.pow((vec.x-0.4f)/0.27f,2));
		if(vec.y <= 0.5f+lim && vec.y >= 0.5f-lim) return true;
		else return false;
	}
	public boolean CellBoundary(Vector2 vec)
	{
		double lim, majaxis;
		majaxis = Math.pow(0.4f, 2);
		lim = Math.sqrt(majaxis-majaxis*Math.pow((vec.x-0.4f)/0.3f,2));
		if(vec.y <= 0.5f+lim && vec.y >= 0.5f-lim) return true;
		else return false;
	}
	public boolean InCytoplasm(Vector2 vec)
	{
		double lim, majaxis;
		majaxis = Math.pow(0.32f, 2);
		lim = Math.sqrt(majaxis-majaxis*Math.pow((vec.x-0.4f)/0.24f,2));
		
		if(vec.x >= 0.29f && vec.x <= 0.5f)
		{
			if((vec.y <= 0.5f+lim && vec.y >= 0.625f) || (vec.y >= 0.5f-lim && vec.y <= 0.375f)) return true;
			else return false;
		}
		else
		{
			if(vec.y <= 0.5f+lim && vec.y >= 0.5f-lim) return true;
			else return false;
		}
		
	}
	
	@Override
	public void render(float delta) {
		if(TimeUtils.millis() - FPSlim > 40 && !GameOver)
		{
			if(Corruption >= 50 && Parent.GameMusic.isPlaying() == false)
			{
				
				Parent.GameMusic2.stop();
				Parent.GameMusic.play();
			}
			else if(Corruption < 50 && Parent.GameMusic2.isPlaying() == false)
			{
				Parent.GameMusic.stop();
				Parent.GameMusic2.play();
			}
			if(TimeUtils.millis() - GameTimer > 5000)
			{
				Carbon += 20;
				Nitrogen += 10;
				Hydrogen += 40;
				GameTimer = TimeUtils.millis();
			}
			if(Corruption >= 100)
			{
				Parent.GameMusic.stop();
				Parent.GameMusic2.stop();
				FileHandle HighscoreData = Gdx.files.local("Highscore.txt");
				Vector<Integer> Scores = new Vector<Integer>();
				if(HighscoreData.exists())
				{
					System.out.print("hello");
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
					boolean larger = true;
					for(int i = 0; i < Scores.size(); i++)
					{
						if(Scores.get(i) > Killcount)
						{
							larger = false;
							break;
						}
					}
					String outputstr = "";
					if(larger) outputstr += (Killcount + "\n");
					else outputstr += (Scores.get(0) + "\n");
					outputstr += Killcount + "\n" + Scores.get(1) + "\n" + Scores.get(2) + "\n" + Scores.get(3) + "\n" + Scores.get(4) + "\n";
					
					HighscoreData.writeString(outputstr, false);
				}
				GameOver = true;
				Virus_Spawner.clear();
				Enzyme_Spawner.clear();
				Antibody_Spawner.clear();
				PowerUp_Spawner.clear();
			}
			if((TimeUtils.millis() - VirusCD > (4000 - 50*Spawncount)) && TimeUtils.millis() - VirusCD > 1000)
			{
				int dir = (int)(Math.random()*4)+1;
				int hits = (int) (Math.floor((float)Spawncount/15.0f) + 1);
				if(dir == 1) Virus_Spawner.add(new Virus(Rm.GetTexture("Retrovirus.png"),Rm.GetTexture("ssRNA.png"),Rm.GetTexture("RetrovirusNeutralised.png"),new Vector2((float) (Math.random()*0.8f),1.0f),new Vector2(0.035f,0.035f),SCREEN_W,SCREEN_H,hits, false));
				else if (dir == 2) Virus_Spawner.add(new Virus(Rm.GetTexture("Retrovirus.png"),Rm.GetTexture("ssRNA.png"),Rm.GetTexture("RetrovirusNeutralised.png"),new Vector2((float) (Math.random()*0.8f),0),new Vector2(0.035f,0.035f),SCREEN_W,SCREEN_H,hits, false));
				else if (dir == 3) Virus_Spawner.add(new Virus(Rm.GetTexture("Retrovirus.png"),Rm.GetTexture("ssRNA.png"),Rm.GetTexture("RetrovirusNeutralised.png"),new Vector2(0, (float) Math.random()),new Vector2(0.035f,0.035f),SCREEN_W,SCREEN_H,hits, false));
				else if (dir == 4) Virus_Spawner.add(new Virus(Rm.GetTexture("Retrovirus.png"),Rm.GetTexture("ssRNA.png"),Rm.GetTexture("RetrovirusNeutralised.png"),new Vector2(0.8f, (float) Math.random()),new Vector2(0.035f,0.035f),SCREEN_W,SCREEN_H,hits, false));
				if(SlowViruses) Virus_Spawner.lastElement().SlowVirus(true);
				if(AntibodyCount > 0 && AutoAntibodySwitch)
				{
					Antibody_Spawner.add(new Antibody(Rm.GetTexture("Antibody.png"),Rm.GetTexture("AntibodyVesicle.png"),new Vector2(0.05f,0.05f),new Vector2(Virus_Spawner.lastElement().GetPosition()),SCREEN_W,SCREEN_H));
					AntibodyCount -= 1;
				}
				if(Spawncount >= 50 && Math.random()*100 < 1*Math.floor((float)(Spawncount-50)/25.0f))
				{
					dir = (int)(Math.random()*4)+1;
					hits = (int) (Spawncount * 1.5f);
					if(dir == 1) Virus_Spawner.add(new Virus(Rm.GetTexture("Mimivirus.png"),Rm.GetTexture("dsDNA.png"),Rm.GetTexture("RetrovirusNeutralised.png"),new Vector2((float) (Math.random()*0.8f),1.0f),new Vector2(0.1f,0.1f),SCREEN_W,SCREEN_H,hits, true));
					else if (dir == 2) Virus_Spawner.add(new Virus(Rm.GetTexture("Mimivirus.png"),Rm.GetTexture("dsDNA.png"),Rm.GetTexture("RetrovirusNeutralised.png"),new Vector2((float) (Math.random()*0.8f),0),new Vector2(0.1f,0.1f),SCREEN_W,SCREEN_H,hits, true));
					else if (dir == 3) Virus_Spawner.add(new Virus(Rm.GetTexture("Mimivirus.png"),Rm.GetTexture("dsDNA.png"),Rm.GetTexture("RetrovirusNeutralised.png"),new Vector2(0, (float) Math.random()),new Vector2(0.1f,0.1f),SCREEN_W,SCREEN_H,hits, true));
					else if (dir == 4) Virus_Spawner.add(new Virus(Rm.GetTexture("Mimivirus.png"),Rm.GetTexture("dsDNA.png"),Rm.GetTexture("RetrovirusNeutralised.png"),new Vector2(0.8f, (float) Math.random()),new Vector2(0.1f,0.1f),SCREEN_W,SCREEN_H,hits, true));
					if(SlowViruses) Virus_Spawner.lastElement().SlowVirus(true);
				}
				if(Spawncount >= 100 && Math.random()*100 < 1*Math.floor((float)(Spawncount-100)/25.0f))
				{
					dir = (int)(Math.random()*4)+1;
					hits = (int) (Math.floor((float)Spawncount/5.0f) + 1);
					if(dir == 1) Virus_Spawner.add(new Virus(Rm.GetTexture("RabiesVirus.png"),Rm.GetTexture("ssDNA.png"),Rm.GetTexture("RabiesVirusNeutralised.png"),new Vector2((float) (Math.random()*0.8f),1.0f),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H,hits, false));
					else if (dir == 2) Virus_Spawner.add(new Virus(Rm.GetTexture("RabiesVirus.png"),Rm.GetTexture("ssDNA.png"),Rm.GetTexture("RabiesVirusNeutralised.png"),new Vector2((float) (Math.random()*0.8f),0),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H,hits, false));
					else if (dir == 3) Virus_Spawner.add(new Virus(Rm.GetTexture("RabiesVirus.png"),Rm.GetTexture("ssDNA.png"),Rm.GetTexture("RabiesVirusNeutralised.png"),new Vector2(0, (float) Math.random()),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H,hits, false));
					else if (dir == 4) Virus_Spawner.add(new Virus(Rm.GetTexture("RabiesVirus.png"),Rm.GetTexture("ssDNA.png"),Rm.GetTexture("RabiesVirusNeutralised.png"),new Vector2(0.8f, (float) Math.random()),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H,hits, false));
					if(SlowViruses) Virus_Spawner.lastElement().SlowVirus(true);
					Virus_Spawner.lastElement().SetSlow(true);
				}
				if(Spawncount >= 200 && Math.random()*100 < 1*Math.floor((float)(Spawncount-200)/50.0f))
				{
					dir = (int)(Math.random()*8)+1;
					hits = (int) (Spawncount * 3);
					if(dir == 1) Virus_Spawner.add(new Virus(Rm.GetTexture("Herpesvirus.png"),Rm.GetTexture("HerpesvirusUnsheathed.png"),Rm.GetTexture("HerpesvirusNeutralised.png"),new Vector2((float) (Math.random()*0.2f),1.0f),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H,hits, false));
					else if (dir == 2) Virus_Spawner.add(new Virus(Rm.GetTexture("Herpesvirus.png"),Rm.GetTexture("HerpesvirusUnsheathed.png"),Rm.GetTexture("HerpesvirusNeutralised.png"),new Vector2((float) (0.8f-Math.random()*0.2f),0),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H,hits, false));
					else if (dir == 3) Virus_Spawner.add(new Virus(Rm.GetTexture("Herpesvirus.png"),Rm.GetTexture("HerpesvirusUnsheathed.png"),Rm.GetTexture("HerpesvirusNeutralised.png"),new Vector2(0, (float) Math.random()*0.25f),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H,hits, false));
					else if (dir == 4) Virus_Spawner.add(new Virus(Rm.GetTexture("Herpesvirus.png"),Rm.GetTexture("HerpesvirusUnsheathed.png"),Rm.GetTexture("HerpesvirusNeutralised.png"),new Vector2(0.8f, 1.0f - (float)Math.random()*0.25f),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H,hits, false));
					else if (dir == 5) Virus_Spawner.add(new Virus(Rm.GetTexture("Herpesvirus.png"),Rm.GetTexture("HerpesvirusUnsheathed.png"),Rm.GetTexture("HerpesvirusNeutralised.png"),new Vector2(0, 1.0f - (float)Math.random()*0.25f),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H,hits, false));
					else if (dir == 6) Virus_Spawner.add(new Virus(Rm.GetTexture("Herpesvirus.png"),Rm.GetTexture("HerpesvirusUnsheathed.png"),Rm.GetTexture("HerpesvirusNeutralised.png"),new Vector2(0.8f, (float) Math.random()*0.25f),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H,hits, false));
					else if (dir == 7) Virus_Spawner.add(new Virus(Rm.GetTexture("Herpesvirus.png"),Rm.GetTexture("HerpesvirusUnsheathed.png"),Rm.GetTexture("HerpesvirusNeutralised.png"),new Vector2((float) (Math.random()*0.2f),0),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H,hits, false));
					else if (dir == 8) Virus_Spawner.add(new Virus(Rm.GetTexture("Herpesvirus.png"),Rm.GetTexture("HerpesvirusUnsheathed.png"),Rm.GetTexture("HerpesvirusNeutralised.png"),new Vector2((float) (0.8f-Math.random()*0.2f),1.0f),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H,hits, false));
					
					if(SlowViruses) Virus_Spawner.lastElement().SlowVirus(true);
				}
				Spawncount += 1;
				VirusCD = TimeUtils.millis();
			}
			
			for(int i = 0; i < Enzyme_Spawner.size(); i++)
			{
				Enzyme_Spawner.get(i).Move();
				if(!WithinCell(new Vector2(Enzyme_Spawner.get(i).GetPosition().x,Enzyme_Spawner.get(i).GetPosition().y)) && Enzyme_Spawner.get(i).GetBounce() > 0) Enzyme_Spawner.get(i).SetDir(Reflect(Enzyme_Spawner.get(i).GetDir(), Enzyme_Spawner.get(i).GetPosition()));
				else if(!WithinCell(new Vector2(Enzyme_Spawner.get(i).GetPosition().x,Enzyme_Spawner.get(i).GetPosition().y))) Enzyme_Spawner.remove(i);
				else
				{
					for(int j = 0; j < Virus_Spawner.size(); j++)
					{
						if(Enzyme_Spawner.get(i).CheckIntersect(Virus_Spawner.get(j).GetPosition().x, Virus_Spawner.get(j).GetPosition().y, Virus_Spawner.get(j).GetSize().x/2))
						{
							Virus_Spawner.get(j).SetStrength(Enzyme_Spawner.get(i).GetStrength());
							if(Virus_Spawner.get(j).GetStrength() <= 0)
							{
								double PowerUpChance = Math.random();
								if(!Virus_Spawner.get(j).GetMega())
								{
								Carbon += 40;
								Hydrogen += 80;
								Nitrogen += 20;
								Phosphorous += 10;
								if(PowerUpChance < 0.025f)
								{
									PowerUpChance = Math.random();
									if(PowerUpChance < 0.2f) PowerUp_Spawner.add(new PowerUp(Rm.GetTexture("SlowVirusPowerUp.png"),"Slow Virus",Virus_Spawner.get(j).GetPosition(),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H));
									else if(PowerUpChance >=0.2f && PowerUpChance < 0.4f) PowerUp_Spawner.add(new PowerUp(Rm.GetTexture("IncreaseFireRatePowerUp.png"),"Increase Fire Rate", Virus_Spawner.get(j).GetPosition(),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H));
									else if(PowerUpChance >= 0.4f && PowerUpChance < 0.5f) PowerUp_Spawner.add(new PowerUp(Rm.GetTexture("DNARepairPowerUp.png"),"DNA Repair", Virus_Spawner.get(j).GetPosition(), new Vector2(0.05f,0.05f),SCREEN_W, SCREEN_H));
									else if(PowerUpChance >= 0.5f && PowerUpChance < 0.8f) PowerUp_Spawner.add(new PowerUp(Rm.GetTexture("CHNPowerUp.png"), "CHN", Virus_Spawner.get(j).GetPosition(), new Vector2(0.05f,0.05f),SCREEN_W, SCREEN_H));
									else if(PowerUpChance >= 0.8f) PowerUp_Spawner.add(new PowerUp(Rm.GetTexture("AntibodyPowerUp.png"),"Antibody Volley", Virus_Spawner.get(j).GetPosition(), new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H));
								}
								}
								else
								{
									Carbon += 300;
									Hydrogen += 600;
									Nitrogen += 150;
									Phosphorous += 150;
									if(PowerUpChance < 0.1f)
									{
										PowerUpChance = Math.random();
										if(PowerUpChance < 0.2f) PowerUp_Spawner.add(new PowerUp(Rm.GetTexture("SlowVirusPowerUp.png"),"Slow Virus",Virus_Spawner.get(j).GetPosition(),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H));
										else if(PowerUpChance >=0.2f && PowerUpChance < 0.4f) PowerUp_Spawner.add(new PowerUp(Rm.GetTexture("IncreaseFireRatePowerUp.png"),"Increase Fire Rate", Virus_Spawner.get(j).GetPosition(),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H));
										else if(PowerUpChance >= 0.4f && PowerUpChance < 0.5f) PowerUp_Spawner.add(new PowerUp(Rm.GetTexture("DNARepairPowerUp.png"),"DNA Repair", Virus_Spawner.get(j).GetPosition(), new Vector2(0.05f,0.05f),SCREEN_W, SCREEN_H));
										else if(PowerUpChance >= 0.5f && PowerUpChance < 0.8f) PowerUp_Spawner.add(new PowerUp(Rm.GetTexture("CHNPowerUp.png"), "CHN", Virus_Spawner.get(j).GetPosition(), new Vector2(0.05f,0.05f),SCREEN_W, SCREEN_H));
										else if(PowerUpChance >= 0.8f) PowerUp_Spawner.add(new PowerUp(Rm.GetTexture("AntibodyPowerUp.png"),"Antibody Volley", Virus_Spawner.get(j).GetPosition(), new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H));		
									}
								}
								Killcount += 1;
								Virus_Spawner.remove(j);
								
							}
							Enzyme_Spawner.remove(i);
							break;
						}
					}
				}
			}
			
			for(int i = 0; i < Virus_Spawner.size(); i++)
			{
				Virus_Spawner.get(i).Move();
				if(Virus_Spawner.get(i).CheckIntersect(0.4f, 0.5f, 0))
				{
					if(!Virus_Spawner.get(i).GetMega())
					{
						Corruption += 5;
						Gdx.input.vibrate(500);
					}
					else
					{
						Corruption += 10;
						Gdx.input.vibrate(1000);
					}
					Virus_Spawner.remove(i);
				}
				else if(!Virus_Spawner.get(i).WithinScreen())
				{
					if(Virus_Spawner.get(i).GetState() == Virus.Virus_State.NEUTRALISED)
					{
						Carbon += 40;
						Hydrogen += 80;
						Nitrogen += 20;
						Phosphorous += 10;
					}
					Virus_Spawner.remove(i);
				}
				else if(Virus_Spawner.get(i).GetState() == Virus.Virus_State.NORMAL && WithinCell(Virus_Spawner.get(i).GetPosition())) Virus_Spawner.get(i).Transform(Virus.Virus_State.UNSHEATHED);
				else if(Virus_Spawner.get(i).GetState() == Virus.Virus_State.NEUTRALISED && CellBoundary(Virus_Spawner.get(i).GetPosition())) Virus_Spawner.get(i).Reverse();
				
			}
			for(int i = 0; i < Ribosome_Spawner.size(); i++)
			{
				for(int j = 0; j < Virus_Spawner.size(); j++)
				{
					if(Ribosome_Spawner.get(i).CheckIntersect(Virus_Spawner.get(j).GetPosition().x, Virus_Spawner.get(j).GetPosition().y, 0.1f))
					{
						if(TimeUtils.millis() - Ribosome_Spawner.get(i).GetCD() >= RibosomeFireRateCD) Enzyme_Spawner.add(Ribosome_Spawner.get(i).Shoot(Virus_Spawner.get(j).GetPosition().x, Virus_Spawner.get(j).GetPosition().y, Rm.GetTexture("RestrictionEnzyme.png")));
					}
					if(Ribosome_Spawner.get(i).CheckIntersect(Virus_Spawner.get(j).GetPosition().x,Virus_Spawner.get(j).GetPosition().y,0.025f) && Virus_Spawner.get(j).SlowsRibosome()) Ribosome_Spawner.get(i).resetCD();
				}
			}
			for(int i = 0; i < Antibody_Spawner.size(); i++)
			{
				Antibody_Spawner.get(i).Move();
				if(!CellBoundary(new Vector2(Antibody_Spawner.get(i).GetPosition().x,Antibody_Spawner.get(i).GetPosition().y)))
				{
					if (!Antibody_Spawner.get(i).GetState()) Antibody_Spawner.get(i).Transform();
					else if(!Antibody_Spawner.get(i).WithinScreen())
					{
						Antibody_Spawner.remove(i);
						break;
					}
					else
					{
						for(int j = 0; j < Virus_Spawner.size(); j++)
						{
							if(Virus_Spawner.get(j).GetState() == Virus.Virus_State.NORMAL && Antibody_Spawner.get(i).GetState() && !Virus_Spawner.get(j).GetMega())
							{
								if(Antibody_Spawner.get(i).CheckIntersect(Virus_Spawner.get(j).GetPosition().x, Virus_Spawner.get(j).GetPosition().y, 0.0125f))
								{
									Antibody_Spawner.remove(i);
									Virus_Spawner.get(j).Transform(Virus.Virus_State.NEUTRALISED);
									break;
								}
							}
						}
					}
				}
			}
			
			if(TimeUtils.millis() - AntibodyGenerateCD >= 11000 - 1000*AntibodyGenerateLevel && AntibodyCount < 4)
			{
				AntibodyCount += 1;
				AntibodyGenerateCD = TimeUtils.millis();
			}
			for(int i = 0; i < PowerUp_Spawner.size(); i++)
			{
				if(PowerUp_Spawner.get(i).GetActivateStatus() == false) PowerUp_Spawner.get(i).Move();
				
				if(PowerUp_Spawner.get(i).GetName() == "Slow Virus")
				{
					if(TimeUtils.millis() - PowerUp_Spawner.get(i).GetExpiry() > 10000)
					{
						SlowViruses = false;
						for(int j = 0; j < PowerUp_Spawner.size(); j++)
						{
							if(PowerUp_Spawner.get(j).GetName() == "Slow Virus" && j != i && PowerUp_Spawner.get(j).GetActivateStatus() == true)
							{
								SlowViruses = true;
								break;
							}
						}
						if(!SlowViruses)
						{
							for(int j = 0; j < Virus_Spawner.size(); j++) Virus_Spawner.get(j).SlowVirus(false);
						}
						PowerUp_Spawner.remove(i);
					}
				}
				else if(PowerUp_Spawner.get(i).GetName() == "Increase Fire Rate")
				{
					if(TimeUtils.millis() - PowerUp_Spawner.get(i).GetExpiry() > 10000)
					{
					IncreaseFireRate = false;
					for(int j = 0; j < PowerUp_Spawner.size(); j++)
					{
						if(PowerUp_Spawner.get(j).GetName() == "Increase Fire Rate" && j != i && PowerUp_Spawner.get(j).GetActivateStatus() == true)
						{
							IncreaseFireRate = true;
							break;
						}
					}
					if(!IncreaseFireRate)
					{
						RibosomeFireRateCD = 2200 - RibosomeFireRateLevel * 200;
					}
					PowerUp_Spawner.remove(i);
					}
				}
				else if(TimeUtils.millis() - PowerUp_Spawner.get(i).GetExpiry() > 10000) PowerUp_Spawner.remove(i);
			}
			FPSlim = TimeUtils.millis();
		}
		
		if(TimeUtils.millis() - FPSlim > 40 && GameOver)
		{
			if(TimeUtils.millis() - VirusCD > 500)
			{
				int dir = (int)(Math.random()*4)+1;
				if(dir == 1) Virus_Spawner.add(new Virus(Rm.GetTexture("Retrovirus.png"),Rm.GetTexture("ssRNA.png"),Rm.GetTexture("RetrovirusNeutralised.png"),new Vector2((float) (Math.random()*0.8f),1.0f),new Vector2(0.035f,0.035f),SCREEN_W,SCREEN_H,1, false));
				else if (dir == 2) Virus_Spawner.add(new Virus(Rm.GetTexture("Retrovirus.png"),Rm.GetTexture("ssRNA.png"),Rm.GetTexture("RetrovirusNeutralised.png"),new Vector2((float) (Math.random()*0.8f),0),new Vector2(0.035f,0.035f),SCREEN_W,SCREEN_H,1, false));
				else if (dir == 3) Virus_Spawner.add(new Virus(Rm.GetTexture("Retrovirus.png"),Rm.GetTexture("ssRNA.png"),Rm.GetTexture("RetrovirusNeutralised.png"),new Vector2(0, (float) Math.random()),new Vector2(0.035f,0.035f),SCREEN_W,SCREEN_H,1, false));
				else if (dir == 4) Virus_Spawner.add(new Virus(Rm.GetTexture("Retrovirus.png"),Rm.GetTexture("ssRNA.png"),Rm.GetTexture("RetrovirusNeutralised.png"),new Vector2(0.8f, (float) Math.random()),new Vector2(0.035f,0.035f),SCREEN_W,SCREEN_H,1, false));
				
				Virus_Spawner.lastElement().SetLocation(0.4f,0.5f);
				Virus_Spawner.lastElement().UnsetCD();
				Virus_Spawner.lastElement().Reverse();
				Virus_Spawner.lastElement().Transform(Virus.Virus_State.UNSHEATHED);
				
				VirusCD = TimeUtils.millis();
			}
			for(int i = 0; i < Virus_Spawner.size(); i++)
			{
				Virus_Spawner.get(i).Move();
				if(!WithinCell(Virus_Spawner.get(i).GetPosition()) && Virus_Spawner.get(i).GetState() == Virus.Virus_State.UNSHEATHED) Virus_Spawner.get(i).Transform(Virus.Virus_State.NORMAL);
				if(!Virus_Spawner.get(i).WithinScreen()) Virus_Spawner.remove(i);
			}
			
			FPSlim = TimeUtils.millis();
		}
			
		
			Stats.setText("Carbon: " + Carbon + "\nNitrogen: " + Nitrogen + "\nHydrogen: " + Hydrogen + "\nPhosphorous: " + Phosphorous + "\nDNA Corrupted: " + Corruption + "\nScore: " + Killcount + "\nAntibody Vesicles:");
			
			Gdx.gl.glClearColor(74.0f/255.0f, 214.0f/255.0f, 232.0f/255.0f, 0);
			Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
			/* Draw Game stuff */
			batch.setProjectionMatrix(camera.combined);
			batch.begin();
			CellBG.draw(batch);
			for(int i = 0; i < Ribosome_Spawner.size(); i++) Ribosome_Spawner.get(i).Draw(batch);
			for(int i = 0; i < Virus_Spawner.size(); i++) Virus_Spawner.get(i).Draw(batch);
			for(int i = 0; i < Enzyme_Spawner.size(); i++) Enzyme_Spawner.get(i).Draw(batch);
			for(int i = 0; i < Antibody_Spawner.size(); i++) Antibody_Spawner.get(i).Draw(batch);
			for(int i = 0; i < PowerUp_Spawner.size(); i++) if(PowerUp_Spawner.get(i).WithinScreen() && PowerUp_Spawner.get(i).GetActivateStatus() == false) PowerUp_Spawner.get(i).Draw(batch);
			batch.end();
		
			/*Draw interface background*/
			SR.setProjectionMatrix(camera.combined);
			SR.begin(ShapeType.FilledRectangle);
			SR.setColor(new Color(0,0,1,1));
			SR.filledRect(SCREEN_W*0.8f, 0, SCREEN_W*0.2f, SCREEN_H);
			if(!GameOver)
			{
				for(int i = 0; i < Virus_Spawner.size(); i++)
				{
					if(Virus_Spawner.get(i).GetLife() > 0.5f) SR.setColor(new Color(0,1,0,1));
					else if(Virus_Spawner.get(i).GetLife() > 0.25f) SR.setColor(new Color(252.0f/255.0f,191.0f/255.0f,5.0f/255.0f,1));
					else SR.setColor(new Color(1,0,0,1));
					SR.filledRect(SCREEN_W*(Virus_Spawner.get(i).GetPosition().x - Virus_Spawner.get(i).GetSize().x/2), (Virus_Spawner.get(i).GetPosition().y+Virus_Spawner.get(i).GetSize().y)*SCREEN_H, SCREEN_W*Virus_Spawner.get(i).GetSize().x*Virus_Spawner.get(i).GetLife(), SCREEN_H*0.005f);
				}
			}
			SR.end();
			/*Draw interface stuff */
			batch.setProjectionMatrix(camera.combined);
			batch.begin();
			Stats.Draw(batch);
			Description.Draw(batch);
			AutoAntibodyToggle.Draw(batch);
			Upgrades.Draw(batch);
			for(int i = 0; i < AntibodyCount; i++) AntibodyInterface.get(i).draw(batch);
			BounceUpgradeButton.Draw(batch);
			StrengthUpgradeButton.Draw(batch);
			RibosomeFireRateUpgradeButton.Draw(batch);
			AutoAntibodyUpgradeButton.Draw(batch);
			if(InterfaceState != MenuState.NONE)
			{
				ConfirmButton.Draw(batch);
				CancelButton.Draw(batch);
			}
			MuteButton.Draw(batch);
			batch.end();
			
			if(GameOver)
			{
				Gdx.gl.glEnable(GL10.GL_BLEND);
				Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
				SR.setProjectionMatrix(camera.combined);
				SR.begin(ShapeType.FilledRectangle);
				SR.setColor(new Color(0,0,0,0.5f));
				SR.filledRect(0, 0, SCREEN_W, SCREEN_H);
				SR.end();
				Gdx.gl.glDisable(GL10.GL_BLEND);
				
				batch.setProjectionMatrix(camera.combined);
				batch.begin();
				GameOverText.Draw(batch);
				batch.end();
			}
		
			
	}

	@Override
	public void resize(int width, int height) {
		SCREEN_W = width;
		SCREEN_H = height;
		
		CellBG.setSize(SCREEN_W*0.6f, SCREEN_H*0.8f);
		CellBG.setOrigin(CellBG.getWidth()/2, CellBG.getHeight()/2);
		CellBG.setPosition(0.4f*SCREEN_W - CellBG.getWidth()/2, 0.5f*SCREEN_H - CellBG.getHeight()/2);
		
		Stats.resize(width,height);
		Description.resize(width,height);
		Description.wrapText(1.0f);
		AutoAntibodyToggle.resize(width, height);
		BounceUpgradeButton.resize(width, height);
		StrengthUpgradeButton.resize(width, height);
		RibosomeFireRateUpgradeButton.resize(width, height);
		AutoAntibodyUpgradeButton.resize(width, height);
		Upgrades.resize(width, height);
		ConfirmButton.resize(width,height);
		CancelButton.resize(width,height);
		GameOverText.resize(width, height);
		MuteButton.resize(width,height);
		for(int i = 0; i < Virus_Spawner.size(); i++) Virus_Spawner.get(i).resize(width, height);
		for(int i = 0; i < Ribosome_Spawner.size(); i++) Ribosome_Spawner.get(i).resize(width,height);
		for(int i = 0; i < Enzyme_Spawner.size(); i++) Enzyme_Spawner.get(i).resize(width, height);
		for(int i = 0; i < Antibody_Spawner.size(); i++) Antibody_Spawner.get(i).resize(width, height);
		for(int i = 0; i < PowerUp_Spawner.size(); i++) PowerUp_Spawner.get(i).resize(width, height);
		for(int i = 0; i < AntibodyInterface.size(); i++) AntibodyInterface.get(i).setBounds((0.8f+i*0.05f)*SCREEN_W, 0.70f*SCREEN_H, 0.05f*SCREEN_W, 0.05f*SCREEN_H);
		
		camera.setToOrtho(false,SCREEN_W,SCREEN_H);
		camera.update();
		
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(this);
		GameTimer = TimeUtils.millis();
		if(Parent.BgMusic.isPlaying()) Parent.BgMusic.stop();
		if(Parent.PlayMusic) MuteButton.ChangeSprite(Rm.GetTexture("MusicOn.png"));
		else MuteButton.ChangeSprite(Rm.GetTexture("MusicOff.png"));
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
		Parent.GameMusic.stop();
		Parent.GameMusic2.stop();
		
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
		
		Vector2 touch = new Vector2((float)screenX/(float)SCREEN_W,1.0f - (float)screenY/(float)SCREEN_H);
		for(int i = 0; i < PowerUp_Spawner.size(); i++)
		{
			if(PowerUp_Spawner.get(i).CheckIntersect((float)screenX/(float)SCREEN_W, 1.0f - (float)screenY/(float)SCREEN_H, 0.0125f))
			{
				if(PowerUp_Spawner.get(i).GetName() == "Slow Virus")
				{
					SlowViruses = true;
					for(int j = 0; j < Virus_Spawner.size(); j++)
					{
						Virus_Spawner.get(j).SlowVirus(true);
					}
					PowerUp_Spawner.get(i).Activate();
				}
				else if(PowerUp_Spawner.get(i).GetName() == "Increase Fire Rate")
				{
					RibosomeFireRateCD = 1100 - RibosomeFireRateLevel * 100;
					PowerUp_Spawner.get(i).Activate();
					IncreaseFireRate = true;
				}
				else if(PowerUp_Spawner.get(i).GetName() == "DNA Repair")
				{
					if(Corruption >= 5) Corruption -= 5;
					PowerUp_Spawner.remove(i);
				}
				else if(PowerUp_Spawner.get(i).GetName() == "CHN")
				{
					Carbon += 200;
					Hydrogen += 400;
					Nitrogen += 100;
					PowerUp_Spawner.remove(i);
				}
				else if(PowerUp_Spawner.get(i).GetName() == "Antibody Volley")
				{
					for(int j = 0; j < 15; j++)
					{
						double dir = Math.random();
						if(dir < 0.25f) Antibody_Spawner.add(new Antibody(Rm.GetTexture("Antibody.png"),Rm.GetTexture("AntibodyVesicle.png"),new Vector2(0.035f,0.035f),new Vector2((float)Math.random()*0.8f,0),SCREEN_W,SCREEN_H));
						else if(dir >=0.25f && dir < 0.5f) Antibody_Spawner.add(new Antibody(Rm.GetTexture("Antibody.png"),Rm.GetTexture("AntibodyVesicle.png"),new Vector2(0.035f,0.035f),new Vector2((float)Math.random()*0.8f,1),SCREEN_W,SCREEN_H));
						else if(dir >= 0.5f && dir < 0.75f) Antibody_Spawner.add(new Antibody(Rm.GetTexture("Antibody.png"),Rm.GetTexture("AntibodyVesicle.png"),new Vector2(0.035f,0.035f),new Vector2(0,(float)Math.random()),SCREEN_W,SCREEN_H));
						else if(dir >= 0.75f) Antibody_Spawner.add(new Antibody(Rm.GetTexture("Antibody.png"),Rm.GetTexture("AntibodyVesicle.png"),new Vector2(0.035f,0.035f),new Vector2(0.8f,(float)Math.random()),SCREEN_W,SCREEN_H));
					}
					PowerUp_Spawner.remove(i);
				}
				return true;
			}
		}
		if(MuteButton.IsWithin((float)screenX/(float)SCREEN_W, 1.0f - (float)screenY/(float)SCREEN_H))
		{
			Parent.Mute();
			if(Parent.PlayMusic) MuteButton.ChangeSprite(Rm.GetTexture("MusicOn.png"));
			else MuteButton.ChangeSprite(Rm.GetTexture("MusicOff.png"));
		}
		else if(InCytoplasm(touch) && !GameOver)
		{
			boolean clash = false;
			for(int i = 0; i < Ribosome_Spawner.size(); i++)
			{
				if(Ribosome_Spawner.get(i).CheckIntersect((float)screenX/(float)SCREEN_W, 1.0f - (float)screenY/(float)SCREEN_H, 0.0125f))
				{
					clash = true;
					break;
				}
			}
			if(!clash && Carbon >= 200 && Hydrogen >= 400 && Nitrogen >= 100)
			{
				Ribosome_Spawner.add(new Ribosome(Rm.GetTexture("Ribosome.png"),new Vector2((float)screenX/(float)SCREEN_W,1.0f - (float)screenY/(float)SCREEN_H),new Vector2(0.035f,0.035f),SCREEN_W,SCREEN_H, EnzymeStrengthLevel, EnzymeBounceLevel));
				
				Carbon -= 200;
				Hydrogen -= 400;
				Nitrogen -= 100;
				return true;
			}
			
		}
		else if(!WithinCell(touch) && touch.x < 0.8f && !GameOver)
		{
			if(AntibodyCount > 0)
			{
				Antibody_Spawner.add(new Antibody(Rm.GetTexture("Antibody.png"),Rm.GetTexture("AntibodyVesicle.png"),new Vector2(0.035f,0.035f),touch,SCREEN_W,SCREEN_H));
				AntibodyCount -= 1;
				return true;
			}
		}
		else if(AutoAntibodyToggle.IsWithin(screenX, screenY) &&!GameOver)
		{
			
			if(AutoAntibodySwitch)
			{
				AutoAntibodySwitch = false;
				AutoAntibodyToggle.setText("Turn on Auto-Antibodies");
			}
			else
			{
				AutoAntibodySwitch = true;
				AutoAntibodyToggle.setText("Turn off Auto-Antibodies");
			}
			return true;
		}
		else if(BounceUpgradeButton.IsWithin(touch.x, touch.y) && !GameOver)
		{
			InterfaceState = MenuState.BOUNCE_UPGRADE;
			Description.setText("Description:\nThis upgrade increases the number of bounces an enzyme can make before it denatures.  \nCurrent Level: " + EnzymeBounceLevel + "\nCurrent cost:\n Carbon: " + 100*EnzymeBounceLevel + "\nHydrogen: " + 100*EnzymeBounceLevel + "\nPhosphorous: " + 100*EnzymeBounceLevel + "\n\nUpgrade?");
			Description.wrapText(0.2f);
			Description.TotalAdj();
			return true;
		}
		else if(StrengthUpgradeButton.IsWithin(touch.x, touch.y) && !GameOver)
		{
			InterfaceState = MenuState.STRENGTH_UPGRADE;
			Description.setText("Description:\nThis upgrade increases the strength of enzymes, allowing it to destroy viral DNA in fewer hits.  \nCurrent Level: " + EnzymeStrengthLevel + "\nCurrent cost:\n Carbon: " + 100*(int)Math.pow(2, EnzymeStrengthLevel) + "\nHydrogen: " + 100*(int)Math.pow(2, EnzymeStrengthLevel) + "\nPhosphorous: " + 100*(int)Math.pow(2, EnzymeStrengthLevel) + "\n\nUpgrade?");
			Description.wrapText(0.2f);
			Description.TotalAdj();
			return true;
		}
		else if(RibosomeFireRateUpgradeButton.IsWithin(touch.x, touch.y) && !GameOver)
		{
			InterfaceState = MenuState.FIRE_RATE_UPGRADE;
			if(RibosomeFireRateLevel < 6) Description.setText("Description:\nThis upgrade lets ribosomes shoot enzymes at a faster rate.\nCurrent Level: " + RibosomeFireRateLevel + "\nCurrent cost:\n Carbon: " + 100*(int)Math.pow(3, RibosomeFireRateLevel) + "\nHydrogen: " + 100*(int)Math.pow(3, RibosomeFireRateLevel) + "\nPhosphorous: " + 100*(int)Math.pow(3, RibosomeFireRateLevel) + "\n\nUpgrade?");
			else if(RibosomeFireRateLevel == 6) Description.setText("Description:\nThis upgrade lets ribosomes shoot enzymes at a faster rate.\nCurrent Level: MAX");
			Description.wrapText(0.2f);
			Description.TotalAdj();
			return true;
		}
		else if(AutoAntibodyUpgradeButton.IsWithin(touch.x, touch.y) && !GameOver)
		{
			InterfaceState = MenuState.ANTIBODY_UPGRADE;
			Description.setText("Description:\nThis upgrade decreases antibody production time.\nCurrent Level: " + AntibodyGenerateLevel + "\nCurrent cost:\n Carbon: " + 100*(AntibodyGenerateLevel+1) + "\nHydrogen: " + 100*(AntibodyGenerateLevel+1) + "\nPhosphorous: " + 100*(AntibodyGenerateLevel+1) + "\n\nUpgrade?");
			Description.wrapText(0.2f);
			Description.TotalAdj();
			return true;
		}
		else if(InterfaceState != MenuState.NONE && !GameOver)
		{
			if(ConfirmButton.IsWithin(touch.x, touch.y))
			{
				switch(InterfaceState)
				{
				case BOUNCE_UPGRADE:
					if(Carbon >= 100*EnzymeBounceLevel && Hydrogen >= 100*EnzymeBounceLevel && Phosphorous >= 100*EnzymeBounceLevel)
					{
						Carbon -= (100*EnzymeBounceLevel);
						Hydrogen -= (100*EnzymeBounceLevel);
						Phosphorous -= (100*EnzymeBounceLevel);
						EnzymeBounceLevel += 1;
						for(int i = 0; i < Ribosome_Spawner.size(); i++) Ribosome_Spawner.get(i).SetBounceLevel(EnzymeBounceLevel);
						Description.setText("Description:\nThis upgrade increases the number of bounces an enzyme can make before it denatures.  \nCurrent Level: " + EnzymeBounceLevel + "\nCurrent cost:\n Carbon: " + 100*EnzymeBounceLevel + "\nHydrogen: " + 100*EnzymeBounceLevel + "\nPhosphorous: " + 100*EnzymeBounceLevel + "\n\nUpgrade?");
						Description.wrapText(0.2f);
						Description.TotalAdj();
					}
					break;
				case STRENGTH_UPGRADE:
					if(Carbon >= 100*(int)Math.pow(2, EnzymeStrengthLevel) && Hydrogen >= 100*(int)Math.pow(2, EnzymeStrengthLevel) && Phosphorous >= 100*(int)Math.pow(2, EnzymeStrengthLevel))
					{
						Carbon -= (100*(int)Math.pow(2, EnzymeStrengthLevel));
						Hydrogen -= (100*(int)Math.pow(2, EnzymeStrengthLevel));
						Phosphorous -= (100*(int)Math.pow(2, EnzymeStrengthLevel));
						EnzymeStrengthLevel += 1;
						for(int i = 0; i < Ribosome_Spawner.size(); i++) Ribosome_Spawner.get(i).SetStrengthLevel(EnzymeStrengthLevel);
						Description.setText("Description:\nThis upgrade increases the strength of enzymes, allowing it to destroy viral DNA in fewer hits.\nCurrent Level: " + EnzymeStrengthLevel + "\nCurrent cost:\n Carbon: " + 100*(int)Math.pow(2, EnzymeStrengthLevel) + "\nHydrogen: " + 100*(int)Math.pow(2, EnzymeStrengthLevel) + "\nPhosphorous: " + 100*(int)Math.pow(2, EnzymeStrengthLevel) + "\n\nUpgrade?");
						Description.wrapText(0.2f);
						Description.TotalAdj();
					}
					break;
				case FIRE_RATE_UPGRADE:
					if(RibosomeFireRateLevel < 6 && Carbon >= 100*Math.pow(3,RibosomeFireRateLevel) && Hydrogen >= 100*Math.pow(3,RibosomeFireRateLevel) && Phosphorous >= 100*Math.pow(3,RibosomeFireRateLevel))
					{
						Carbon -= (100*Math.pow(3,RibosomeFireRateLevel));
						Hydrogen -= (100*Math.pow(3,RibosomeFireRateLevel));
						Phosphorous -= (100*Math.pow(3,RibosomeFireRateLevel));
						RibosomeFireRateLevel += 1;
						if(RibosomeFireRateLevel < 6) Description.setText("Description:\nThis upgrade lets ribosomes shoot enzymes at a faster rate\nCurrent Level: " + RibosomeFireRateLevel + "\nCurrent cost:\n Carbon: " + 100*(int)Math.pow(3,RibosomeFireRateLevel) + "\nHydrogen: " + 100*(int)Math.pow(3,RibosomeFireRateLevel) + "\nPhosphorous: " + 100*(int)Math.pow(3,RibosomeFireRateLevel) + "\n\nUpgrade?");
						else Description.setText("Description:\nThis upgrade lets ribosomes shoot enzymes at a faster rate\nCurrent Level: MAX");
						Description.wrapText(0.2f);
						Description.TotalAdj();
					}
					break;
				case ANTIBODY_UPGRADE:
					if(Carbon >= 100*(AntibodyGenerateLevel + 1) && Hydrogen >= 100*(AntibodyGenerateLevel + 1) && Phosphorous >= 100*(AntibodyGenerateLevel + 1))
					{
						Carbon -= (100*(AntibodyGenerateLevel + 1));
						Hydrogen -= (100*(AntibodyGenerateLevel + 1));
						Phosphorous -= (100*(AntibodyGenerateLevel + 1));
						AntibodyGenerateLevel += 1;
						Description.setText("Description:\nThis upgrade decreases antibody production time.\nCurrent Level: " + AntibodyGenerateLevel + "\nCurrent cost:\n Carbon: " + 100*(AntibodyGenerateLevel+1) + "\nHydrogen: " + 100*(AntibodyGenerateLevel+1) + "\nPhosphorous: " + 100*(AntibodyGenerateLevel+1) + "\n\nUpgrade?");
						Description.wrapText(0.2f);
						Description.TotalAdj();
					}
					break;
				default:
					break;
				}
			}
			else if(CancelButton.IsWithin(touch.x, touch.y))
			{
				InterfaceState = MenuState.NONE;
				Description.setText("");
				Description.wrapText(1.0f);
				Description.TotalAdj();
			}
			return true;
		}
		else if(GameOver)
		{
			fullscreen.show();
			Reset();
			Parent.setScreen(Parent.Highscore);
			return true;
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
