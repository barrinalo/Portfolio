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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

public class TutorialScreen implements Screen, InputProcessor{
	
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
	
	private boolean GameActive;
	private GameTextLabel Info;
	private Vector2 HighlightLoc;
	private Vector2 HighlightSize;
	private long Anitimer;
	
	private enum TutorialCheckpoints
	{
		INTRODUCTION,
		CELL,
		STATS,
		ANTIBODIES,
		UPGRADES,
		DESCRIPTIONS,
		PLACE_RIBOSOME,
		VIRUS_SPAWN,
		VIRUS_SPAWN2,
		END
	}
	private enum MenuState
	{
		NONE,
		BOUNCE_UPGRADE,
		STRENGTH_UPGRADE,
		FIRE_RATE_UPGRADE,
		ANTIBODY_UPGRADE
	};
	
	private MenuState InterfaceState;
	private TutorialCheckpoints TutorialProgress;
	
	public TutorialScreen(OrthographicCamera c, SpriteBatch b, ResourceManager r, MyGdxGame g)
	{
		HighlightLoc = new Vector2(0,0);
		HighlightSize = new Vector2(0,0);
		TutorialProgress = TutorialCheckpoints.INTRODUCTION;
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
		AutoAntibodySwitch = false;
		GameActive = false;
		AntibodyCount = 4;
		InterfaceState = MenuState.NONE;
		GameTimer = TimeUtils.millis();
		VirusCD = TimeUtils.millis();
		AntibodyGenerateCD = TimeUtils.millis();
		FPSlim = TimeUtils.millis();
		Virus_Spawner = new Vector<Virus>();
		Ribosome_Spawner = new Vector<Ribosome>();
		Enzyme_Spawner = new Vector<Enzyme>();
		Antibody_Spawner = new Vector<Antibody>();
		
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
		
		Info = new GameTextLabel("Welcome to the tutorial\n In Cell TD the goal is to prevent viruses from invading your cell", SCREEN_W, SCREEN_H);
		Info.setBounds(0.2f, 0.5f, 0.4f, 0.2f);
		Info.setTextFormat(GameTextLabel.TextFormat.NONE);
		Info.setHAlignment(GameTextLabel.HAlignment.LEFT);
		Info.setVAlignment(GameTextLabel.VAlignment.BOTTOM);
		Info.setColor(new Color(1,0,0,1));
		
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
	}
	
	public void Reset()
	{
		TutorialProgress = TutorialCheckpoints.INTRODUCTION;
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
		AutoAntibodySwitch = false;
		GameActive = false;
		InterfaceState = MenuState.NONE;
		GameTimer = TimeUtils.millis();
		VirusCD = TimeUtils.millis();
		AntibodyGenerateCD = TimeUtils.millis();
		FPSlim = TimeUtils.millis();
		Virus_Spawner.clear();
		Ribosome_Spawner.clear();
		Enzyme_Spawner.clear();
		Antibody_Spawner.clear();
		
		
		Info.setBounds(0.2f, 0.5f, 0.4f, 0.2f);
		Info.setText("Welcome to the tutorial\n In Cell TD the goal is to prevent viruses from invading your cell");
		Info.wrapText(0.4f);
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
		
		if(vec.x >= 0.30f && vec.x <= 0.5f)
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
		
		if(TutorialProgress == TutorialCheckpoints.PLACE_RIBOSOME && Ribosome_Spawner.size() > 0)
		{
			TutorialProgress = TutorialCheckpoints.VIRUS_SPAWN;
			HighlightLoc.x = 0.0f;
			HighlightLoc.y = 0.0f;
			HighlightSize.x = 0.0f;
			HighlightSize.y = 0.0f;
			
			Info.setBounds(0.8f,0.1f,0.2f,0.45f);
			Info.setText("Look a virus is approaching!\nWatch as your ribosome defends the cell.");
			Info.wrapText(0.5f);
			
			Vector2 gradient = new Vector2();
			gradient.x = Ribosome_Spawner.lastElement().GetLocation().x - 0.4f;
			gradient.y = Ribosome_Spawner.lastElement().GetLocation().y - 0.5f;
			
			double n = Math.sqrt(Math.pow(gradient.x,2)+Math.pow(gradient.y,2));
			gradient.x /= n;
			gradient.y /= n;
			gradient.mul(0.01f);
			
			Vector2 Loc = new Vector2(0.4f,0.5f);
			while(Loc.x > 0.0f && Loc.x < 1.0f && Loc.y < 1.0f && Loc.y > 0.0f)
			{
				Loc.add(gradient);
			}
			Virus_Spawner.add(new Virus(Rm.GetTexture("Retrovirus.png"),Rm.GetTexture("ssRNA.png"),Rm.GetTexture("RetrovirusNeutralised.png"),Loc,new Vector2(0.035f,0.035f),SCREEN_W,SCREEN_H,1,false));
			while(!Virus_Spawner.lastElement().WithinScreen()) Virus_Spawner.lastElement().Move();
		}
		else if(TutorialProgress == TutorialCheckpoints.VIRUS_SPAWN && Virus_Spawner.size() == 0)
		{
			TutorialProgress = TutorialCheckpoints.VIRUS_SPAWN2;
			
			Info.setBounds(0.8f,0.1f,0.2f,0.45f);
			Info.setText("Great!\n\nNow try shooting some antibodies by touching outside the cell");
			Info.wrapText(0.5f);
		}
		else if(TutorialProgress == TutorialCheckpoints.VIRUS_SPAWN2)
		{
			for(int i = 0; i < Virus_Spawner.size(); i++)
			{
				if(!Virus_Spawner.get(i).WithinScreen() && Virus_Spawner.get(i).GetState() == Virus.Virus_State.NEUTRALISED)
				{
					TutorialProgress = TutorialCheckpoints.END;
					GameActive = false;
					Antibody_Spawner.clear();
					Virus_Spawner.clear();
					Ribosome_Spawner.clear();
					Enzyme_Spawner.clear();
					
					Info.setBounds(0.1f,0.2f,0.6f,0.6f);
					Info.setText("This is the end of the tutorial.\nHave fun playing the game! =)");
					Info.wrapText(0.5f);
				}
			}
		}

		if(TimeUtils.millis() - FPSlim > 40)
		{
			if(TimeUtils.millis() - GameTimer > 5000)
			{
				Carbon += 20;
				Nitrogen += 10;
				Hydrogen += 40;
				GameTimer = TimeUtils.millis();
			}
			
			
			if(TutorialProgress == TutorialCheckpoints.VIRUS_SPAWN2)
			{
				if(TimeUtils.millis()-VirusCD > 3000)
				{
					int dir = (int)(Math.random()*4)+1;
					int hits = 1;
					if(dir == 1) Virus_Spawner.add(new Virus(Rm.GetTexture("Retrovirus.png"),Rm.GetTexture("ssRNA.png"),Rm.GetTexture("RetrovirusNeutralised.png"),new Vector2((float) (Math.random()*0.8f),1.0f),new Vector2(0.035f,0.035f),SCREEN_W,SCREEN_H,hits, false));
					else if (dir == 2) Virus_Spawner.add(new Virus(Rm.GetTexture("Retrovirus.png"),Rm.GetTexture("ssRNA.png"),Rm.GetTexture("RetrovirusNeutralised.png"),new Vector2((float) (Math.random()*0.8f),0),new Vector2(0.035f,0.035f),SCREEN_W,SCREEN_H,hits, false));
					else if (dir == 3) Virus_Spawner.add(new Virus(Rm.GetTexture("Retrovirus.png"),Rm.GetTexture("ssRNA.png"),Rm.GetTexture("RetrovirusNeutralised.png"),new Vector2(0, (float) Math.random()),new Vector2(0.035f,0.035f),SCREEN_W,SCREEN_H,hits, false));
					else if (dir == 4) Virus_Spawner.add(new Virus(Rm.GetTexture("Retrovirus.png"),Rm.GetTexture("ssRNA.png"),Rm.GetTexture("RetrovirusNeutralised.png"),new Vector2(0.8f, (float) Math.random()),new Vector2(0.035f,0.035f),SCREEN_W,SCREEN_H,hits, false));
					if(AntibodyCount > 0 && AutoAntibodySwitch)
					{
						Antibody_Spawner.add(new Antibody(Rm.GetTexture("Antibody.png"),Rm.GetTexture("AntibodyVesicle.png"),new Vector2(0.035f,0.035f),new Vector2(Virus_Spawner.lastElement().GetPosition()),SCREEN_W,SCREEN_H));
						AntibodyCount -= 1;
					}
					
					VirusCD = TimeUtils.millis();
				}
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
								if(!Virus_Spawner.get(j).GetMega())
								{
								Carbon += 40;
								Hydrogen += 80;
								Nitrogen += 20;
								Phosphorous += 10;
								}
								else
								{
									Carbon += 600;
									Hydrogen += 1200;
									Nitrogen += 300;
									Phosphorous += 150;
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
						//Corruption += 5;
						Gdx.input.vibrate(500);
					}
					else
					{
						//Corruption += 10;
						Gdx.input.vibrate(1000);
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
						if(TimeUtils.millis() - Ribosome_Spawner.get(i).GetCD() >= 2200 - RibosomeFireRateLevel*200) Enzyme_Spawner.add(Ribosome_Spawner.get(i).Shoot(Virus_Spawner.get(j).GetPosition().x, Virus_Spawner.get(j).GetPosition().y, Rm.GetTexture("RestrictionEnzyme.png")));
					}
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
			batch.end();
		
			/*Draw interface background*/
			SR.setProjectionMatrix(camera.combined);
			SR.begin(ShapeType.FilledRectangle);
			SR.setColor(new Color(0,0,1,1));
			SR.filledRect(SCREEN_W*0.8f, 0, SCREEN_W*0.2f, SCREEN_H);
			for(int i = 0; i < Virus_Spawner.size(); i++)
			{
				if(Virus_Spawner.get(i).GetLife() > 0.5f) SR.setColor(new Color(0,1,0,1));
				else if(Virus_Spawner.get(i).GetLife() > 0.25f) SR.setColor(new Color(252.0f/255.0f,191.0f/255.0f,5.0f/255.0f,1));
				else SR.setColor(new Color(1,0,0,1));
				SR.filledRect(SCREEN_W*(Virus_Spawner.get(i).GetPosition().x - Virus_Spawner.get(i).GetSize().x/2), (Virus_Spawner.get(i).GetPosition().y+Virus_Spawner.get(i).GetSize().y)*SCREEN_H, SCREEN_W*Virus_Spawner.get(i).GetSize().x*Virus_Spawner.get(i).GetLife(), SCREEN_H*0.005f);
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
			batch.end();
			
			//Tutorial Stuff
			
			Gdx.gl.glEnable(GL10.GL_BLEND);
			Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			SR.setProjectionMatrix(camera.combined);
			SR.begin(ShapeType.FilledRectangle);
			SR.setColor(new Color(1,0,0,0.5f));
			SR.filledRect(HighlightLoc.x*SCREEN_W, HighlightLoc.y*SCREEN_H, HighlightSize.x*SCREEN_W, HighlightSize.y*SCREEN_H);
			SR.end();
			Gdx.gl.glDisable(GL10.GL_BLEND);
			
			SR.setProjectionMatrix(camera.combined);
			SR.begin(ShapeType.FilledRectangle);
			SR.setColor(new Color(194.0f/255.0f,1,10.0f/255.0f,1));
			SR.filledRect(Info.GetRelativeLocation().x*SCREEN_W, Info.GetRelativeLocation().y*SCREEN_H, Info.GetRelativeSize().x*SCREEN_W, Info.GetRelativeSize().y*SCREEN_H);
			SR.end();
			
			batch.setProjectionMatrix(camera.combined);
			batch.begin();
			Info.Draw(batch);
			batch.end();
			
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
		Info.resize(width, height);
		Info.wrapText(1.0f);
		AutoAntibodyToggle.resize(width, height);
		BounceUpgradeButton.resize(width, height);
		StrengthUpgradeButton.resize(width, height);
		RibosomeFireRateUpgradeButton.resize(width, height);
		AutoAntibodyUpgradeButton.resize(width, height);
		Upgrades.resize(width, height);
		ConfirmButton.resize(width,height);
		CancelButton.resize(width,height);
		
		
		for(int i = 0; i < Virus_Spawner.size(); i++) Virus_Spawner.get(i).resize(width, height);
		for(int i = 0; i < Ribosome_Spawner.size(); i++) Ribosome_Spawner.get(i).resize(width,height);
		for(int i = 0; i < Enzyme_Spawner.size(); i++) Enzyme_Spawner.get(i).resize(width, height);
		for(int i = 0; i < Antibody_Spawner.size(); i++) Antibody_Spawner.get(i).resize(width, height);
		for(int i = 0; i < AntibodyInterface.size(); i++) AntibodyInterface.get(i).setBounds((0.8f+i*0.05f)*SCREEN_W, 0.70f*SCREEN_H, 0.05f*SCREEN_W, 0.05f*SCREEN_H);
		
		camera.setToOrtho(false,SCREEN_W,SCREEN_H);
		camera.update();
		
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(this);
		GameTimer = TimeUtils.millis();
		if(Parent.BgMusic.isPlaying())
		{
			Parent.BgMusic.stop();
			
		}
		
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
		if(TutorialProgress == TutorialCheckpoints.INTRODUCTION)
		{
			TutorialProgress = TutorialCheckpoints.CELL;
			HighlightLoc.x = 0.1f;
			HighlightLoc.y = 0.1f;
			HighlightSize.x = 0.6f;
			HighlightSize.y = 0.8f;
			
			Info.setBounds(0.7f, 0.2f, 0.3f, 0.6f);
			Info.setText("On your left is your cell.\n\nViruses will try to get to the purple nucleus at the center of your cell.\n\nThe beige region around it is your cytoplasm, where you will be building ribosomes which fire restriction enzymes at the invading viral DNA and releasing resources in the process.");
			Info.wrapText(0.5f);
		}
		else if(TutorialProgress == TutorialCheckpoints.CELL)
		{
			TutorialProgress = TutorialCheckpoints.STATS;
			HighlightLoc.x = 0.8f;
			HighlightLoc.y = 0.77f;
			HighlightSize.x = 0.2f;
			HighlightSize.y = 0.25f;
			
			Info.setBounds(0.1f, 0.2f, 0.6f, 0.6f);
			Info.setText("On your right you have an interface.  The highlighted region contains information on your resources, score, and life (DNA Corruption)\n\nCarbon, Nitrogen, Hydrogen are used to build ribosomes, your main defense.\n\nCarbon, Nitrogen, Phosphorous are used to upgrade your ribosomes and cells\n\nWhen DNA Corruption hits 100 its game over");
			Info.wrapText(0.5f);
		}
		else if(TutorialProgress == TutorialCheckpoints.STATS)
		{
			TutorialProgress = TutorialCheckpoints.ANTIBODIES;
			HighlightLoc.x = 0.8f;
			HighlightLoc.y = 0.65f;
			HighlightSize.x = 0.2f;
			HighlightSize.y = 0.13f;
			
			Info.setBounds(0.1f, 0.2f, 0.6f, 0.6f);
			Info.setText("Next up, antibodies, one of your secondary defences.\n\nThis interface shows you how many vesicles or shots of antibodies you can release before needing to wait for the cell to produce more.  Antibodies neutralise viruses outside the cell, preventing them from coming in.  \n\nYou can toggle auto antibodies also, and the cell will release the antibodies for you if the setting is on.");
			Info.wrapText(0.4f);
		}
		else if(TutorialProgress == TutorialCheckpoints.ANTIBODIES)
		{
			TutorialProgress = TutorialCheckpoints.UPGRADES;
			HighlightLoc.x = 0.8f;
			HighlightLoc.y = 0.45f;
			HighlightSize.x = 0.3f;
			HighlightSize.y = 0.2f;
			
			Info.setBounds(0.1f, 0.2f, 0.6f, 0.6f);
			Info.setText("Then we have upgrades. By pressing on any of the upgrade buttons, you can view its description.  There are 4 kinds of upgrades:\n\nEnzyme Integrity - Allows enzymes to maintain their functional structure for a longer time, allowing them to bounce around inside the cell.\n\nEnzyme Strength - Improves the rate of catalysis of Viral DNA.  Fewer hits required to degrade the viral DNA.\n\nRibosome Fire Rate - Improves the speed of translation, allowing ribosomes to make enzymes faster, resulting in a higher fire rate.\n\nAntibody Upgrade - Improves the rate at which the cell can produce vesicles of Antibodies.");
			Info.wrapText(0.4f);
		}
		else if(TutorialProgress == TutorialCheckpoints.UPGRADES)
		{
			TutorialProgress = TutorialCheckpoints.DESCRIPTIONS;
			HighlightLoc.x = 0.8f;
			HighlightLoc.y = 0.0f;
			HighlightSize.x = 0.2f;
			HighlightSize.y = 0.45f;
			
			InterfaceState = MenuState.STRENGTH_UPGRADE;
			Description.setText("Description:\nThis upgrade increases the efficiency of catalysis, enabling viral DNA to be dentaured in fewer hits.  \nCurrent Level: " + EnzymeStrengthLevel + "\nCurrent cost:\n Carbon: " + 200*EnzymeStrengthLevel + "\nHydrogen: " + 200*EnzymeStrengthLevel + "\nPhosphorous: " + 200*EnzymeStrengthLevel + "\n\nUpgrade?");
			Description.wrapText(0.2f);
			Description.TotalAdj();
			
			Info.setBounds(0.1f, 0.2f, 0.6f, 0.6f);
			Info.setText("Here you will be able to view a description of each of the upgrades when you press one of the upgrade buttons.\n\nYou will be able to see the cost of each upgrade and confirm whether you want to upgrade or not.");
			Info.wrapText(0.4f);
		}
		else if(TutorialProgress == TutorialCheckpoints.DESCRIPTIONS)
		{
			TutorialProgress = TutorialCheckpoints.PLACE_RIBOSOME;
			HighlightLoc.x = 0.1f;
			HighlightLoc.y = 0.1f;
			HighlightSize.x = 0.6f;
			HighlightSize.y = 0.8f;
			
			InterfaceState = MenuState.NONE;
			Description.setText(" ");
			Description.wrapText(0.2f);
			Description.TotalAdj();
			
			Info.setBounds(0.8f,0.1f,0.2f,0.45f);
			Info.setText("Now place a ribosome in the cytoplasm by pressing inside the cytoplasm (beige area), each ribosome costs:\n\nCarbon: 200\nHydrogen: 400\nNitrogen: 100");
			Info.wrapText(0.4f);
			
			GameActive = true;
		}
		
		else if(TutorialProgress == TutorialCheckpoints.END)
		{
			Reset();
			Parent.setScreen(Parent.MainMenu);
		}
		
		else if(GameActive)
		{
		Vector2 touch = new Vector2((float)screenX/(float)SCREEN_W,1.0f - (float)screenY/(float)SCREEN_H);
		if(InCytoplasm(touch))
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
			}
			
		}
		else if(!WithinCell(touch) && touch.x < 0.8f && TutorialProgress == TutorialCheckpoints.VIRUS_SPAWN2)
		{
			if(AntibodyCount > 0)
			{
				Antibody_Spawner.add(new Antibody(Rm.GetTexture("Antibody.png"),Rm.GetTexture("AntibodyVesicle.png"),new Vector2(0.035f,0.035f),touch,SCREEN_W,SCREEN_H));
				AntibodyCount -= 1;
			}
		}
		else if(AutoAntibodyToggle.IsWithin(screenX, screenY))
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
			
		}
		else if(BounceUpgradeButton.IsWithin(touch.x, touch.y))
		{
			InterfaceState = MenuState.BOUNCE_UPGRADE;
			Description.setText("Description:\nThis upgrade increases the number of bounces an enzyme can make before it denatures.  \nCurrent Level: " + EnzymeBounceLevel + "\nCurrent cost:\n Carbon: " + 100*EnzymeBounceLevel + "\nHydrogen: " + 100*EnzymeBounceLevel + "\nPhosphorous: " + 100*EnzymeBounceLevel + "\n\nUpgrade?");
			Description.wrapText(0.2f);
			Description.TotalAdj();
		}
		else if(StrengthUpgradeButton.IsWithin(touch.x, touch.y))
		{
			InterfaceState = MenuState.STRENGTH_UPGRADE;
			Description.setText("Description:\nThis upgrade increases the efficiency of catalysis, enabling viral DNA to be dentaured in fewer hits.  \nCurrent Level: " + EnzymeStrengthLevel + "\nCurrent cost:\n Carbon: " + 200*EnzymeStrengthLevel + "\nHydrogen: " + 200*EnzymeStrengthLevel + "\nPhosphorous: " + 200*EnzymeStrengthLevel + "\n\nUpgrade?");
			Description.wrapText(0.2f);
			Description.TotalAdj();
		}
		else if(RibosomeFireRateUpgradeButton.IsWithin(touch.x, touch.y))
		{
			InterfaceState = MenuState.FIRE_RATE_UPGRADE;
			if(RibosomeFireRateLevel < 6) Description.setText("Description:\nThis upgrade increases the efficiency of mRNA translation, enabling enzymes to be produced faster.  \nCurrent Level: " + RibosomeFireRateLevel + "\nCurrent cost:\n Carbon: " + 300*RibosomeFireRateLevel + "\nHydrogen: " + 300*RibosomeFireRateLevel + "\nPhosphorous: " + 300*RibosomeFireRateLevel + "\n\nUpgrade?");
			else if(RibosomeFireRateLevel == 6) Description.setText("Description:\nThis upgrade increases the efficiency of mRNA translation, enabling enzymes to be produced faster.  \nCurrent Level: MAX");
			Description.wrapText(0.2f);
			Description.TotalAdj();
		}
		else if(AutoAntibodyUpgradeButton.IsWithin(touch.x, touch.y))
		{
			InterfaceState = MenuState.ANTIBODY_UPGRADE;
			Description.setText("Description:\nThis upgrade automates antibody production, allowing the cell to throw out vesicles of antibodies occasionally.  \nCurrent Level: " + AntibodyGenerateLevel + "\nCurrent cost:\n Carbon: " + 100*(AntibodyGenerateLevel+1) + "\nHydrogen: " + 100*(AntibodyGenerateLevel+1) + "\nPhosphorous: " + 100*(AntibodyGenerateLevel+1) + "\n\nUpgrade?");
			Description.wrapText(0.2f);
			Description.TotalAdj();
		}
		else if(InterfaceState != MenuState.NONE)
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
					if(Carbon >= 200*EnzymeStrengthLevel && Hydrogen >= 200*EnzymeStrengthLevel && Phosphorous >= 200*EnzymeStrengthLevel)
					{
						Carbon -= (200*EnzymeStrengthLevel);
						Hydrogen -= (200*EnzymeStrengthLevel);
						Phosphorous -= (200*EnzymeStrengthLevel);
						EnzymeStrengthLevel += 1;
						for(int i = 0; i < Ribosome_Spawner.size(); i++) Ribosome_Spawner.get(i).SetStrengthLevel(EnzymeStrengthLevel);
						Description.setText("Description:\nThis upgrade increases the efficiency of catalysis, enabling viral DNA to be dentaured in fewer hits.  \nCurrent Level: " + EnzymeStrengthLevel + "\nCurrent cost:\n Carbon: " + 200*EnzymeStrengthLevel + "\nHydrogen: " + 200*EnzymeStrengthLevel + "\nPhosphorous: " + 200*EnzymeStrengthLevel + "\n\nUpgrade?");
						Description.wrapText(0.2f);
						Description.TotalAdj();
					}
					break;
				case FIRE_RATE_UPGRADE:
					if(RibosomeFireRateLevel < 6 && Carbon >= 300*RibosomeFireRateLevel && Hydrogen >= 300*RibosomeFireRateLevel && Phosphorous >= 300*RibosomeFireRateLevel)
					{
						Carbon -= (300*RibosomeFireRateLevel);
						Hydrogen -= (300*RibosomeFireRateLevel);
						Phosphorous -= (300*RibosomeFireRateLevel);
						RibosomeFireRateLevel += 1;
						Description.setText("Description:\nThis upgrade increases the efficiency of mRNA translation, enabling enzymes to be produced faster.  \nCurrent Level: " + RibosomeFireRateLevel + "\nCurrent cost:\n Carbon: " + 300*RibosomeFireRateLevel + "\nHydrogen: " + 300*RibosomeFireRateLevel + "\nPhosphorous: " + 300*RibosomeFireRateLevel + "\n\nUpgrade?");
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
						Description.setText("Description:\nThis upgrade automates antibody production, allowing the cell to throw out vesicles of antibodies occasionally.  \nCurrent Level: " + AntibodyGenerateLevel + "\nCurrent cost:\n Carbon: " + 100*(AntibodyGenerateLevel+1) + "\nHydrogen: " + 100*(AntibodyGenerateLevel+1) + "\nPhosphorous: " + 100*(AntibodyGenerateLevel+1) + "\n\nUpgrade?");
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
		}
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
