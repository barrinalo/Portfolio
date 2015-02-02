package com.codelessweb.rank2048;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.RemoteException;

import com.android.vending.billing.IInAppBillingService;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;

public class Rank2048 extends ApplicationAdapter implements InputProcessor{
	//Game Display Constants
	public static final float MARGINS = 0.05f;
	public static final float INTER_BLOCK_MARGIN = 0.025f;
	public static final float BLOCK_SIZE = 0.16f;
	DisplayText ScoreTitle, BestTitle, Restart, LoadingTitle, Undo, CurRank, BestRank;
	LabelStyle	TitleStyle, NormalStyle;
	IInAppBillingService mService;
	Context AppContext;
	Activity App;
	//Game Variables
	OrthographicCamera camera;
	SpriteBatch batch;
	ShapeRenderer BgRenderer;
	DisplayText CurScore, Best;
	
	static BoardPiece Pieces[][];
	static int Board[][];
	static int touchx, touchy, touchid;
	static boolean Animate;
	static int AnimationCounter;
	static Vector<AnimationData> Instructions;
	static boolean GameOver;
	public static int SCREEN_W, SCREEN_H, HIGHSCORE, CURSCORE;
	public static String HIGHRANK;
	static boolean loaded;
	static HashMap<Integer, String> Images;
	static AssetManager Manager;
	static Vector<int[][]> PrevStates;
	
	public Rank2048(IInAppBillingService mService, Context AppContext, Activity App) {
		this.mService = mService;
		this.AppContext = AppContext;
		this.App = App;
	}
	@Override
	public void resume() {
		
	}
	@Override
	public void create () {
		loaded = false;
		Gdx.graphics.setContinuousRendering(true);
		Gdx.input.setInputProcessor(this);
		Animate = false;
		AnimationCounter = 0;
		Instructions = new Vector<AnimationData>();
		PrevStates = new Vector<int[][]>();
		//Get Screen dimensions
		SCREEN_W = Gdx.graphics.getWidth();
		SCREEN_H = Gdx.graphics.getHeight();
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.update();
		
		batch = new SpriteBatch();
		batch.enableBlending();
		BgRenderer = new ShapeRenderer();
		
		//Load Highscore
		Preferences Highscore = Gdx.app.getPreferences("Highscore");
		if(!Highscore.contains("Highscore")) {
			HIGHSCORE = 0;
			Highscore.putInteger("Highscore", 0);
			Highscore.flush();
		}
		else HIGHSCORE = Highscore.getInteger("Highscore");
		if(!Highscore.contains("Rank")) {
			Highscore.putString("Rank", "");
			Highscore.flush();
		}
		else HIGHRANK = Highscore.getString("Rank");
		
		//Set up labels
		TitleStyle = new LabelStyle(new BitmapFont(Gdx.files.internal("Rank2048FontFile.fnt")), new Color(51.0f/255.0f,51.0f/255.0f,51.0f/255.0f,1));
		NormalStyle = new LabelStyle(new BitmapFont(Gdx.files.internal("Rank2048FontFile.fnt")), new Color(153.0f/255.0f,153.0f/255.0f,153.0f/255.0f,1));
		
		LoadingTitle = new DisplayText("Loading...", TitleStyle);
		LoadingTitle.SetPosAndWidth((MARGINS + 0.35f) * (float) SCREEN_W, (float) SCREEN_H * (1.0f - MARGINS - 0.45f), 0.2f * (float) SCREEN_W);
		
		ScoreTitle = new DisplayText("Score", TitleStyle);
		ScoreTitle.SetPosAndHeight(MARGINS * (float) SCREEN_W, (float) SCREEN_H * (1.0f - MARGINS - 0.05f), 0.05f * (float) SCREEN_H);
		
		BestTitle = new DisplayText("Best", TitleStyle);
		BestTitle.SetPosAndHeight(MARGINS * (float) SCREEN_W, (float) SCREEN_H * (1.0f - MARGINS - 0.05f - 0.075f), 0.05f * (float) SCREEN_H);
		
		CurScore = new DisplayText(String.valueOf(CURSCORE), NormalStyle);
		CurScore.SetPosAndHeight((MARGINS + INTER_BLOCK_MARGIN) * (float) SCREEN_W + ScoreTitle.Text.getTextBounds().width, (float) SCREEN_H * (1.0f - MARGINS - 0.05f), 0.05f * (float) SCREEN_H);
		
		CurRank = new DisplayText(" ", NormalStyle);
		
		Best = new DisplayText(String.valueOf(HIGHSCORE), NormalStyle);
		Best.SetPosAndHeight((MARGINS + INTER_BLOCK_MARGIN) * (float) SCREEN_W + ScoreTitle.Text.getTextBounds().width, (float) SCREEN_H * (1.0f - MARGINS - 0.05f - 0.075f), 0.05f * (float) SCREEN_H);
		
		if(HIGHRANK != "") BestRank = new DisplayText("(" + HIGHRANK + ")", NormalStyle);
		else BestRank = new DisplayText(" ", NormalStyle);
		BestRank.SetPosAndHeight(Best.Text.getX() + Best.Text.getTextBounds().width + INTER_BLOCK_MARGIN * SCREEN_W, (float) SCREEN_H * (1.0f - MARGINS - 0.05f - 0.075f), 0.05f * (float) SCREEN_H);
		
		Restart = new DisplayText("Restart", NormalStyle);
		Restart.SetPosAndHeight((MARGINS + INTER_BLOCK_MARGIN) * (float) SCREEN_W, 5 * (1.4f * INTER_BLOCK_MARGIN + BLOCK_SIZE) * (float) SCREEN_W, 0.025f * (float) SCREEN_H);
		
		Preferences Undos = Gdx.app.getPreferences("Undo");
		if(!Undos.contains("Undo")) Undos.putInteger("Undo", 0);
		int curundos = Undos.getInteger("Undo");
		if(curundos > 0) Undo = new DisplayText("Undo x " + String.valueOf(curundos), NormalStyle);
		else Undo = new DisplayText("Buy Undos", NormalStyle);
		Undo.SetPosAndXY((1.0f - MARGINS - INTER_BLOCK_MARGIN - 0.15f) * (float) SCREEN_W, 5 * (1.4f * INTER_BLOCK_MARGIN + BLOCK_SIZE) * (float) SCREEN_W, 0.15f * (float) SCREEN_W,0.025f * (float) SCREEN_H);
		
		//Set up sprites
				Pieces = new BoardPiece[5][5];
				for(int i = 0; i < 5; i++) {
					for(int j = 0; j < 5; j++) {
						Pieces[i][j] = new BoardPiece();
						Pieces[i][j].setBounds((MARGINS + j*(BLOCK_SIZE+INTER_BLOCK_MARGIN))*(float)SCREEN_W, i*(BLOCK_SIZE + INTER_BLOCK_MARGIN)*(float)SCREEN_W + INTER_BLOCK_MARGIN*(float)SCREEN_W, (float)SCREEN_W * BLOCK_SIZE, (float)SCREEN_W * BLOCK_SIZE);
					}
				}
				
		//Load Images
		Images = new HashMap<Integer, String>();
		Images.put(-1, "blank.png");
		Images.put(0, "pte.png");
		Images.put(1, "lcp.png");
		Images.put(2, "cpl.png");
		Images.put(3, "cfc.png");
		Images.put(4, "3sg.png");
		Images.put(5, "2sg.png");
		Images.put(6, "1sg.png");
		Images.put(7, "ssg.png");
		Images.put(8, "msg.png");
		Images.put(9, "2wo.png");
		Images.put(10, "1wo.png");
		Images.put(11, "mwo.png");
		Images.put(12, "swo.png");
		Images.put(13, "2lt.png");
		Images.put(14, "lta.png");
		Images.put(15, "cpt.png");
		Images.put(16, "maj.png");
		Images.put(17, "ltc.png");
		Images.put(18, "col.png");
		Images.put(19, "bg.png");
		Images.put(20, "mg.png");
		Images.put(21, "lg.png");
		Images.put(22, "wll.png");
		Images.put(23, "fail.jpg");
		Manager = new AssetManager();
		
		Manager.load(Images.get(-1), Texture.class);
		Manager.load(Images.get(0), Texture.class);
		Manager.load(Images.get(1), Texture.class);
		Manager.load(Images.get(2), Texture.class);
		Manager.finishLoading();
		
		//Read save state if it exists
		FileHandle savestate = Gdx.files.local("SaveFile.txt");
		if(savestate.exists()) {
			String data = savestate.readString();
			if(data.equals("")) Reset(false);
			else LoadGame();
		}
		else {
			savestate.writeString("", false);
			Reset(false);
		}
		for(int i = 0; i < 23; i++) if(Manager.isLoaded(Images.get(i), Texture.class)) Manager.load(Images.get(23), Texture.class);
		
	}

	@Override
	public void render () {
		if(!Manager.update()) {
			Gdx.gl.glClearColor(1, 1, 1, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			batch.setProjectionMatrix(camera.combined);
			batch.begin();
			LoadingTitle.Text.draw(batch, 1);
			batch.end();
		}
		else {
		if(Animate) {
			for(int i = 0; i < Instructions.size(); i++) Pieces[Instructions.get(i).x][Instructions.get(i).y].Move(Instructions.get(i).Dir.x, Instructions.get(i).Dir.y);
			AnimationCounter++;
			if(AnimationCounter == 5) {
				Animate = false;
				AnimationCounter = 0;
				Instructions.clear();
				//Update Score
				CurScore.SetText(String.valueOf(CURSCORE));
				//Generate New Block
				int count = 0;
				while(count < 1) {
					int x = (int)(Math.random() * 5.0f);
					int y = (int)(Math.random() * 5.0f);
					if(Board[x][y] == -1) {
						Board[x][y] = (int)(Math.random() * 2.0f);
						count++;
					}
				}
				int largestrank = 0;
				int[][] tempmap = new int[5][5];
				for(int i = 0; i < 5; i++) {
					for(int j = 0; j < 5; j++) {
						if(Board[i][j] > largestrank) largestrank = Board[i][j];
						tempmap[i][j] = Board[i][j];
						if(Board[i][j] != -1) {
							if(!Manager.isLoaded(Images.get(Board[i][j]))) {
								Manager.load(Images.get(Board[i][j]), Texture.class);
								Manager.finishLoading();
							}
							Pieces[i][j].setTexture(Manager.get(Images.get(Board[i][j]),Texture.class));
							Pieces[i][j].Reset();
						}
						else {
							if(!Manager.isLoaded(Images.get(-1))) {
								Manager.load(Images.get(-1), Texture.class);
								Manager.finishLoading();
							}
							Pieces[i][j].setTexture(Manager.get(Images.get(-1), Texture.class));
							Pieces[i][j].Reset();
						}
					}
				}
				PrevStates.add(tempmap);
				if(PrevStates.size() > 10) PrevStates.removeElementAt(0);
				String largesttextrank = Images.get(largestrank).substring(0, Images.get(largestrank).indexOf(".")).toUpperCase();
				String temprank = "";
				String temprank2 = "";
				boolean newHighRank = false;
				Iterator<Entry<Integer, String>> tempranks = Images.entrySet().iterator();
				while(tempranks.hasNext()) {
					Entry<Integer, String> pairs = tempranks.next();
					String val = pairs.getValue();
					if(val.substring(0,val.indexOf(".")).toUpperCase().equals(largesttextrank) && temprank.equals("")) {
						newHighRank = true;
						temprank2 = largesttextrank;
					}
					if(val.substring(0,val.indexOf(".")).toUpperCase().equals(CurRank.Text.getText().toString())) {
						newHighRank = false;
						temprank = CurRank.Text.getText().toString();
					}
					if(!temprank.equals("") || !temprank2.equals("")) break;
				}
				
				if(newHighRank) {
					CurRank.SetPosAndHeight(CurScore.Text.getX() + CurScore.Text.getTextBounds().width + INTER_BLOCK_MARGIN * SCREEN_W, (float) SCREEN_H * (1.0f - MARGINS - 0.05f), 0.05f * (float) SCREEN_H);
					CurRank.SetText("(" + temprank2 + ")");
				}
				//Check if gameover
				boolean bothertocheck = true;
				for(int i = 0; i < 5; i++) {
					for(int j = 0; j < 5; j++) {
						if(Board[i][j] == -1) {
							bothertocheck = false;
							break;
						}
					}
					if(!bothertocheck) break;
				}
				if(bothertocheck) GameOver = TestGameOver();
			}
		}
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		BgRenderer.setProjectionMatrix(camera.combined);
		BgRenderer.begin(ShapeType.Filled);
		BgRenderer.setColor(209.0f/255.0f,224.0f/255.0f,193.0f/255.0f,1);
		for(int i = 0; i < 5; i++) for(int j = 0; j < 5; j++) BgRenderer.rect((MARGINS + j * INTER_BLOCK_MARGIN) * (float)SCREEN_W + (float)SCREEN_W * BLOCK_SIZE * j, (BLOCK_SIZE + INTER_BLOCK_MARGIN) * (float)SCREEN_W * i + INTER_BLOCK_MARGIN*(float)SCREEN_W, (float)SCREEN_W * BLOCK_SIZE, (float)SCREEN_W * BLOCK_SIZE);
		BgRenderer.rect(MARGINS * (float)SCREEN_W, Restart.Text.getY() - INTER_BLOCK_MARGIN * SCREEN_W, Restart.Text.getTextBounds().width + 2 * INTER_BLOCK_MARGIN * (float) SCREEN_W, Restart.Text.getTextBounds().height + 2 * INTER_BLOCK_MARGIN * (float)SCREEN_W);
		BgRenderer.rect((1.0f - MARGINS - 2 * INTER_BLOCK_MARGIN) * (float)SCREEN_W - Undo.Text.getTextBounds().width, Undo.Text.getY() - INTER_BLOCK_MARGIN * SCREEN_W, Undo.Text.getTextBounds().width + 2 * INTER_BLOCK_MARGIN * (float) SCREEN_W, Undo.Text.getTextBounds().height + 2 * INTER_BLOCK_MARGIN * (float)SCREEN_W);
		BgRenderer.flush();
		BgRenderer.end();
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		ScoreTitle.Text.draw(batch, 1);
		BestTitle.Text.draw(batch, 1);
		CurScore.Text.draw(batch, 1);
		Best.Text.draw(batch, 1);
		Restart.Text.draw(batch, 1);
		Undo.Text.draw(batch, 1);
		CurRank.Text.draw(batch, 1);
		BestRank.Text.draw(batch, 1);
		for(int i = 0; i < 5; i++) for(int j = 0; j < 5; j++) Pieces[i][j].draw(batch);
		batch.end();
		
		if(GameOver) {
			if(!Manager.isLoaded(Images.get(23))) {
				Manager.load(Images.get(23), Texture.class);
				Manager.finishLoading();
			}
			batch.setProjectionMatrix(camera.combined);
			batch.begin();
			//batch.setColor(batch.getColor().r,batch.getColor().g,batch.getColor().b, 0.3f);
			batch.draw(Manager.get(Images.get(23),Texture.class), 0, 0,SCREEN_W, SCREEN_H);
			batch.end();
		}
		}
	}
	
	@Override
	public void dispose () {
		SaveGame();
		Gdx.input.setInputProcessor(null);
	}
	
	@Override
	public void resize (int width, int height) {
		SCREEN_W = width;
		SCREEN_H = height;
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
		if(!Animate && !GameOver) {
		touchid = pointer;
		touchx = screenX;
		touchy = screenY;
		}
		else if(GameOver) {
			Reset(true);
		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		
		if(pointer == touchid && !Animate && !GameOver) {
			
			int difx = touchx - screenX;
			int dify = touchy - screenY;
			double totaldist = Math.sqrt(Math.pow(difx, 2) + Math.pow(dify, 2));
			if(Math.abs(difx) > Math.abs(dify) && totaldist > 0.2f * SCREEN_W) {
				if(difx > 0) MoveBlocks(2, Board, true);
				else MoveBlocks(3, Board, true);
			}
			else if(Math.abs(difx) < Math.abs(dify) && totaldist > 0.2f * SCREEN_W) {
				if(dify > 0) MoveBlocks(0, Board, true);
				else MoveBlocks(1, Board, true);
			}
			else if(totaldist <= 0.2f * SCREEN_W) {
				if(screenX >= (MARGINS * (float)SCREEN_W) && screenX <= (MARGINS + 2*INTER_BLOCK_MARGIN) * (float)SCREEN_W + Restart.Text.getTextBounds().width && screenY <= (SCREEN_H - (5 * (BLOCK_SIZE + INTER_BLOCK_MARGIN) * (float)SCREEN_W)) && screenY >= (SCREEN_H-(5 * (BLOCK_SIZE + 1.2f*INTER_BLOCK_MARGIN) * (float)SCREEN_W + Restart.Text.getTextBounds().height))) Reset(false);
				else if(screenX >= ((1.0f - (MARGINS + 2 * INTER_BLOCK_MARGIN)) * SCREEN_W - Undo.Text.getTextBounds().width) && screenX <= (1.0f - MARGINS) * SCREEN_W && screenY <= (SCREEN_H - (5 * (BLOCK_SIZE + INTER_BLOCK_MARGIN) * (float)SCREEN_W)) && screenY >= (SCREEN_H-(5 * (BLOCK_SIZE + 1.2f*INTER_BLOCK_MARGIN) * (float)SCREEN_W + Undo.Text.getTextBounds().height))) Undo();
			}
			touchx = touchy  = touchid = -1;
			
		}
		
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
	
	public void MoveBlocks(int dir, int BoardToUse[][], boolean UpdateScore) {
		switch(dir) {
		case 0:
			//Process Up Swipe
			for(int i = 0; i < 5; i++) {
				int base = 4;
				for(int j = 3; j >= 0; j--) {
					if(BoardToUse[j][i] != -1) {
						if(BoardToUse[j][i] == BoardToUse[base][i] && BoardToUse[j][i] < 22) {
							Instructions.add(new AnimationData(new Vector2(Pieces[j][i].Piece.getX(),Pieces[j][i].Piece.getY()) , new Vector2(Pieces[base][i].Piece.getX(),Pieces[base][i].Piece.getY()),j,i));
							BoardToUse[base][i] += 1;
							BoardToUse[j][i] = -1;
							if(UpdateScore) CURSCORE += (BoardToUse[base][i] + 1)*2;
							base--;
							
						}
						else if(BoardToUse[base][i] == -1) {
							Instructions.add(new AnimationData(new Vector2(Pieces[j][i].Piece.getX(),Pieces[j][i].Piece.getY()) , new Vector2(Pieces[base][i].Piece.getX(),Pieces[base][i].Piece.getY()),j,i));
							BoardToUse[base][i] = BoardToUse[j][i];
							BoardToUse[j][i] = -1;
						}
						else if(BoardToUse[base-1][i] == -1) {
							base--;
							Instructions.add(new AnimationData(new Vector2(Pieces[j][i].Piece.getX(),Pieces[j][i].Piece.getY()) , new Vector2(Pieces[base][i].Piece.getX(),Pieces[base][i].Piece.getY()),j,i));
							BoardToUse[base][i] = BoardToUse[j][i];
							BoardToUse[j][i] = -1;
						}
						else if(BoardToUse[base-1][i] != -1) base--;
					}
				}
			}
			if(Instructions.size() != 0) Animate = true;
			break;
		case 1:
			//Process Down Swipe
			for(int i = 0; i < 5; i++) {
				int base = 0;
				for(int j = 1; j < 5; j++) {
					if(BoardToUse[j][i] != -1) {
						if(BoardToUse[j][i] == BoardToUse[base][i]) {
							Instructions.add(new AnimationData(new Vector2(Pieces[j][i].Piece.getX(),Pieces[j][i].Piece.getY()) , new Vector2(Pieces[base][i].Piece.getX(),Pieces[base][i].Piece.getY()),j,i));
							BoardToUse[base][i] += 1;
							BoardToUse[j][i] = -1;
							if(UpdateScore) CURSCORE += (BoardToUse[base][i] + 1)*2;
							base++;
						}
						else if(BoardToUse[base][i] == -1) {
							Instructions.add(new AnimationData(new Vector2(Pieces[j][i].Piece.getX(),Pieces[j][i].Piece.getY()) , new Vector2(Pieces[base][i].Piece.getX(),Pieces[base][i].Piece.getY()),j,i));
							BoardToUse[base][i] = BoardToUse[j][i];
							BoardToUse[j][i] = -1;
						}
						else if(BoardToUse[base+1][i] == -1) {
							base++;
							Instructions.add(new AnimationData(new Vector2(Pieces[j][i].Piece.getX(),Pieces[j][i].Piece.getY()) , new Vector2(Pieces[base][i].Piece.getX(),Pieces[base][i].Piece.getY()),j,i));
							BoardToUse[base][i] = BoardToUse[j][i];
							BoardToUse[j][i] = -1;
						}
						else if(BoardToUse[base+1][i] != -1) base++;
					}
				}
			}
			if(Instructions.size() != 0) Animate = true;
			break;
		case 2:
			//Process Left Swipe
			for(int i = 0; i < 5; i++) {
				int base = 0;
				for(int j = 1; j < 5; j++) {
					if(BoardToUse[i][j] != -1) {
						if(BoardToUse[i][j] == BoardToUse[i][base]) {
							Instructions.add(new AnimationData(new Vector2(Pieces[i][j].Piece.getX(),Pieces[i][j].Piece.getY()) , new Vector2(Pieces[i][base].Piece.getX(),Pieces[i][base].Piece.getY()),i,j));
							BoardToUse[i][base] += 1;
							BoardToUse[i][j] = -1;
							if(UpdateScore) CURSCORE += (BoardToUse[i][base] + 1)*2;
							base++;
						}
						else if(BoardToUse[i][base] == -1) {
							Instructions.add(new AnimationData(new Vector2(Pieces[i][j].Piece.getX(),Pieces[i][j].Piece.getY()) , new Vector2(Pieces[i][base].Piece.getX(),Pieces[i][base].Piece.getY()),i,j));
							BoardToUse[i][base] = BoardToUse[i][j];
							BoardToUse[i][j] = -1;
						}
						else if(BoardToUse[i][base+1] == -1) {
							base++;
							Instructions.add(new AnimationData(new Vector2(Pieces[i][j].Piece.getX(),Pieces[i][j].Piece.getY()) , new Vector2(Pieces[i][base].Piece.getX(),Pieces[i][base].Piece.getY()),i,j));
							BoardToUse[i][base] = BoardToUse[i][j];
							BoardToUse[i][j] = -1;
						}
						else if(BoardToUse[i][base+1] != -1) base++;
					}
				}
			}
			if(Instructions.size() != 0) Animate = true;
			break;
		case 3:
			//Process Right Swipe
			for(int i = 0; i < 5; i++) {
				int base = 4;
				for(int j = 3; j >= 0; j--) {
					if(BoardToUse[i][j] != -1) {
						if(BoardToUse[i][j] == BoardToUse[i][base]) {
							Instructions.add(new AnimationData(new Vector2(Pieces[i][j].Piece.getX(),Pieces[i][j].Piece.getY()) , new Vector2(Pieces[i][base].Piece.getX(),Pieces[i][base].Piece.getY()),i,j));
							BoardToUse[i][base] += 1;
							BoardToUse[i][j] = -1;
							if(UpdateScore) CURSCORE += (BoardToUse[i][base] + 1)*2;
							base--;
						}
						else if(BoardToUse[i][base] == -1) {
							Instructions.add(new AnimationData(new Vector2(Pieces[i][j].Piece.getX(),Pieces[i][j].Piece.getY()) , new Vector2(Pieces[i][base].Piece.getX(),Pieces[i][base].Piece.getY()),i,j));
							BoardToUse[i][base] = BoardToUse[i][j];
							BoardToUse[i][j] = -1;
						}
						else if(BoardToUse[i][base-1] == -1) {
							base--;
							Instructions.add(new AnimationData(new Vector2(Pieces[i][j].Piece.getX(),Pieces[i][j].Piece.getY()) , new Vector2(Pieces[i][base].Piece.getX(),Pieces[i][base].Piece.getY()),i,j));
							BoardToUse[i][base] = BoardToUse[i][j];
							BoardToUse[i][j] = -1;
						}
						else if(BoardToUse[i][base-1] != -1) base--;
					}
				}
			}
			if(Instructions.size() != 0) Animate = true;
			break;
		default:
			break;
		}
	}
	
	public void Reset(boolean SaveHighScore) {
		if(CURSCORE > Gdx.app.getPreferences("Highscore").getInteger("Highscore") && SaveHighScore) {
			Preferences prefs = Gdx.app.getPreferences("Highscore");
			prefs.putInteger("Highscore", CURSCORE);
			prefs.flush();
			Best.SetText(String.valueOf(CURSCORE));
			HIGHSCORE = CURSCORE;
		}
		if(SaveHighScore) {
			Preferences prefs = Gdx.app.getPreferences("Highscore");
			String largesttextrank = BestRank.Text.getText().toString();
			String temprank = "";
			String temprank2 = "";
			boolean newHighRank = false;
			Iterator<Entry<Integer, String>> tempranks = Images.entrySet().iterator();
			while(tempranks.hasNext()) {
				Entry<Integer, String> pairs = tempranks.next();
				String val = pairs.getValue();
				if(val.substring(0,val.indexOf(".")).toUpperCase().equals(largesttextrank) && temprank.equals("")) {
					newHighRank = true;
					temprank2 = largesttextrank;
				}
				if(val.substring(0,val.indexOf(".")).toUpperCase().equals(CurRank.Text.getText().toString())) {
					newHighRank = false;
					temprank = CurRank.Text.getText().toString();
				}
				if(!temprank.equals("") || !temprank2.equals("")) break;
			}
			if(newHighRank) {
				prefs.putString("Rank", temprank2);
				BestRank.SetText("(" + temprank2 + ")");
			}
		}
		GameOver = false;
		Instructions.clear();
		Animate = false;
		CURSCORE = 0;
		CurRank.SetPosAndHeight(CurScore.Text.getX() + CurScore.Text.getTextBounds().width + INTER_BLOCK_MARGIN * SCREEN_W, (float) SCREEN_H * (1.0f - MARGINS - 0.05f), 0.05f * (float) SCREEN_H);
		CurRank.SetText("(PTE");
		Board = new int[5][5];
		for(int i = 0; i < 5; i++) for(int j = 0; j < 5; j++) Board[i][j] = -1;
		
		int count = 0;
		while(count < 2) {
			int x = (int)(Math.random() * 5.0f);
			int y = (int)(Math.random() * 5.0f);
			if(Board[x][y] == -1) {
				Board[x][y] = (int)(Math.random() * 2.0f);
				count++;
			}
		}
		
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < 5; j++) {
				if(Board[i][j] != -1) {
					if(!Manager.isLoaded(Images.get(Board[i][j]))) {
						Manager.load(Images.get(Board[i][j]), Texture.class);
						Manager.finishLoading();
					}
					Pieces[i][j].setTexture(Manager.get(Images.get(Board[i][j]), Texture.class));
				}
				else {
					if(!Manager.isLoaded(Images.get(-1))) {
						Manager.load(Images.get(-1), Texture.class);
						Manager.finishLoading();
					}
					Pieces[i][j].setTexture(Manager.get(Images.get(-1), Texture.class));
				}
				Pieces[i][j].Reset();
			}
		}
		
		if(CurScore != null) CurScore.SetText(String.valueOf(CURSCORE));
	}
	
	public boolean TestGameOver() {
		boolean isGameOver = true;
		for(int i = 0; i < 4; i++) {
			int TestMap[][] = new int[5][5];
			for(int j = 0; j < 5; j++) for(int k = 0; k < 5; k++) TestMap[j][k] = Board[j][k];
			MoveBlocks(i, TestMap, false);
			if(Instructions.size() != 0) {
				isGameOver = false;
				break;
			}
		}
		Instructions.clear();
		Animate = false;
		return isGameOver;
	}
	
	public void SaveGame() {
		FileHandle savefile = Gdx.files.local("SaveFile.txt");
		int max = 0;
		for(int i = 0; i < 5; i++) for(int j = 0; j < 5; j++) if(Board[i][j] > max) max = Board[i][j];
		savefile.writeString(Images.get(max).substring(0,Images.get(max).indexOf(".")).toUpperCase() + "\n", false);
		savefile.writeString(String.valueOf(CURSCORE) + "\n", true);
		for(int i = 0; i < 5; i++) for(int j = 0; j < 5; j++) savefile.writeString(String.valueOf(Board[i][j]) + ",", true);
	}
	
	public void LoadGame(){
		FileHandle savefile = Gdx.files.local("SaveFile.txt");
		String txt = savefile.readString();
		String data[] = txt.split("\n");
		String SavedRank = data[0];
		String Score = data[1];
		String grid[] = data[2].split(",");
		Board = new int[5][5];
		CURSCORE = Integer.valueOf(Score);
		CurRank.SetPosAndHeight(CurScore.Text.getX() + CurScore.Text.getTextBounds().width + INTER_BLOCK_MARGIN * SCREEN_W, (float) SCREEN_H * (1.0f - MARGINS - 0.05f), 0.05f * (float) SCREEN_H);
		CurRank.SetText("(" + SavedRank + ")");
		
		
		for(int i = 0; i < 25; i++) {
			Board[i/5][i%5] = Integer.valueOf(grid[i]);
		}
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < 5; j++) {
				if(Board[i][j] != -1) {
					if(!Manager.isLoaded(Images.get(Board[i][j]))) {
						Manager.load(Images.get(Board[i][j]), Texture.class);
						Manager.finishLoading();
					}
					Pieces[i][j].setTexture(Manager.get(Images.get(Board[i][j]), Texture.class));
				}
				else {
					if(!Manager.isLoaded(Images.get(-1))) {
						Manager.load(Images.get(-1), Texture.class);
						Manager.finishLoading();
					}
					Pieces[i][j].setTexture(Manager.get(Images.get(-1), Texture.class));
				}
				Pieces[i][j].Reset();
			}
		}
	}
	
	public void Undo() {
		Preferences Prefs = Gdx.app.getPreferences("Undo");
		int curundos = Prefs.getInteger("Undo");
		if(curundos > 0 && PrevStates.size() != 0) {
			for(int i = 0; i < 5; i++) for(int j = 0; j < 5; j++) Board[i][j] = PrevStates.get(i)[i][j];
			curundos--;
			Prefs.putInteger("Undo", curundos);
			if(curundos > 0) Undo.SetText("Undo x " + String.valueOf(curundos));
			else {
				Undo.SetText("Buy Undos");
			}
		}
		else if(curundos == 0 && mService != null) {
			try {
				Bundle buyIntentBundle = mService.getBuyIntent(3, AppContext.getPackageName(), "100Undo", "inapp", "");
				if(buyIntentBundle.getInt("BILLING_RESPONSE_OK") == 0) {
					PendingIntent pi = buyIntentBundle.getParcelable("BUY_INTENT");
					App.startIntentSenderForResult(pi.getIntentSender(), 1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SendIntentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void AddUndos() {
		Preferences prefs = Gdx.app.getPreferences("Undo");
		prefs.putInteger("Undo", 100);
		Undo.SetText("Undo x 100");
	}
}
