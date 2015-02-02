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


public class JournalScreen implements Screen, InputProcessor{

	private OrthographicCamera camera;
	private SpriteBatch batch;
	private ResourceManager Rm;
	private int SCREEN_W, SCREEN_H;
	private MyGdxGame Parent;
	private String JournalState;
	private Vector<JournalEntry> Entries;
	private Vector<Sprite> EntryIcons;
	private GameTextLabel Title,BackToMenu;
	//Journal Entry stuff
	private GameTextLabel EntryText, BackButton, EntryTitle;
	private Sprite MiddleSprite, LeftSprite, RightSprite;
	private Button MuteButton;
	JournalScreen(OrthographicCamera c, SpriteBatch b, ResourceManager r, MyGdxGame g)
	{
		Entries = new Vector<JournalEntry>();
		EntryIcons = new Vector<Sprite>();
		camera = c;
		batch = b;
		Rm = r;
		SCREEN_W = Gdx.graphics.getWidth();
		SCREEN_H = Gdx.graphics.getHeight();
		Parent = g;
		JournalState = "";
		
		MiddleSprite = new Sprite(Rm.GetTexture("Blank.png"));
		MiddleSprite.setBounds(0.4f*SCREEN_W, 0.75f*SCREEN_H, 0.2f*SCREEN_W, 0.2f*SCREEN_H);
		
		LeftSprite = new Sprite(Rm.GetTexture("Blank.png"));
		LeftSprite.setBounds(0.1f*SCREEN_W, 0.75f*SCREEN_H, 0.2f*SCREEN_W, 0.2f*SCREEN_H);
		
		RightSprite = new Sprite(Rm.GetTexture("Blank.png"));
		RightSprite.setBounds(0.7f*SCREEN_W, 0.75f*SCREEN_H, 0.2f*SCREEN_W, 0.2f*SCREEN_H);
		
		Title = new GameTextLabel("Microbiology Journal",SCREEN_W, SCREEN_H);
		Title.setColor(new Color(1.0f,1.0f,1.0f,1.0f));
		Title.setTextFormat(GameTextLabel.TextFormat.EXPANDX_AND_Y);
		Title.setHAlignment(GameTextLabel.HAlignment.MIDDLE);
		Title.setVAlignment(GameTextLabel.VAlignment.MIDDLE);
		Title.setBounds(0.5f, 0.85f, 0.9f, 0.2f);
		Title.wrapText(1.0f);
		Title.TotalAdj();
		
		BackToMenu = new GameTextLabel("Back",SCREEN_W, SCREEN_H);
		BackToMenu.setColor(new Color(1.0f,1.0f,1.0f,1.0f));
		BackToMenu.setTextFormat(GameTextLabel.TextFormat.EXPANDY);
		BackToMenu.setHAlignment(GameTextLabel.HAlignment.MIDDLE);
		BackToMenu.setVAlignment(GameTextLabel.VAlignment.MIDDLE);
		BackToMenu.setBounds(0.5f, 0.05f, 0.9f, 0.1f);
		BackToMenu.wrapText(1.0f);
		BackToMenu.TotalAdj();
		
		EntryText = new GameTextLabel(" ",SCREEN_W,SCREEN_H);
		EntryText.setColor(new Color(1.0f,1.0f,1.0f,1.0f));
		EntryText.setTextFormat(GameTextLabel.TextFormat.NONE);
		EntryText.setHAlignment(GameTextLabel.HAlignment.LEFT);
		EntryText.setVAlignment(GameTextLabel.VAlignment.BOTTOM);
		EntryText.setBounds(0.05f, 0.1f, 0.9f, 0.5f);
		EntryText.wrapText(1.0f);
		EntryText.TotalAdj();
		
		EntryTitle = new GameTextLabel(" ",SCREEN_W,SCREEN_H);
		EntryTitle.setColor(new Color(1.0f,1.0f,1.0f,1.0f));
		EntryTitle.setTextFormat(GameTextLabel.TextFormat.EXPANDY);
		EntryTitle.setHAlignment(GameTextLabel.HAlignment.MIDDLE);
		EntryTitle.setVAlignment(GameTextLabel.VAlignment.MIDDLE);
		EntryTitle.setBounds(0.5f, 0.65f, 0.9f, 0.1f);
		EntryTitle.TotalAdj();
		
		BackButton = new GameTextLabel("Back",SCREEN_W,SCREEN_H);
		BackButton.setColor(new Color(1.0f,1.0f,1.0f,1.0f));
		BackButton.setTextFormat(GameTextLabel.TextFormat.EXPANDY);
		BackButton.setHAlignment(GameTextLabel.HAlignment.MIDDLE);
		BackButton.setVAlignment(GameTextLabel.VAlignment.BOTTOM);
		BackButton.setBounds(0.5f, 0f, 0.9f, 0.05f);
		BackButton.TotalAdj();
		
		Entries.add(new JournalEntry("Cell.png","Cell.png","","","The Cell","Cells are considered the basic unit of life, as they are able to live and reproduce by themselves.  Viruses infect cells to borrow this ability to reproduce."));
		Entries.add(new JournalEntry("Ribosome.png", "Ribosome.png","","","The Ribosome", "Ribosomes help to make proteins, including enzymes.  They do so by 'reading' DNA and 'choosing' the right amino acids, the basic building blocks of proteins, to join together to make a protein. Hence they are the 'towers' in this sense, producing restriction enzymes."));
		Entries.add(new JournalEntry("Antibody.png","Antibody.png","","","Antibodies", "Antibodies are 'Y' shaped proteins that crucial to the immune system.  Each antibody is designed to fit onto another structure, usually on a bacteria or virus.  Once an antibody binds to that structure it facilitates various functions such as preventing virus entry by blocking receptors."));
		Entries.add(new JournalEntry("RestrictionEnzyme.png","RestrictionEnzyme.png","","","Restriction Enzymes", "Restriction enzymes are produced as a form of defence against viruses.  When viral DNA/RNA enters the cell restriction enzymes 'cut' the viral DNA/RNA to deactivate it."));
		Entries.add(new JournalEntry("Viruses.png","Retrovirus.png","Herpesvirus.png","Mimivirus.png","Viruses","Viruses are strange forms of life, and it is still debatable if they are considered living.  To reproduce, they must invade a host cell and use that cell to produce more of itself."));
		Entries.add(new JournalEntry("dsDNA.png","dsDNA.png","ssRNA.png","ssDNA.png","DNA","Deoxyribose nucleic acid(DNA) or Ribose nucleic acid(RNA) are the main forms of genetic material in organisms.  Both can come in double and single stranded forms.  The type of genetic material and its related replication method is used to classify viruses in the Baltimore scheme."));
		Entries.add(new JournalEntry("Retrovirus.png","Retrovirus.png","","","Retro Viruses","Retro viruses are so named because in order to reproduce, their RNA is converted back to DNA in the host cell.  The DNA is then used to replicate the virus.  They contain single stranded RNA."));
		Entries.add(new JournalEntry("Mimivirus.png","Mimivirus.png","","","Mimi Viruses","The Mimi virus is an exceptionally large virus and was once mistaken for a bacteria.  With its large size, the Mimivirus also has more DNA than most viruses.  The Mimi virus has double stranded DNA."));
		Entries.add(new JournalEntry("Herpesvirus.png","Herpesvirus.png","","","Herpes Viruses","The Herpes virus sprite was drawn to show the virus envelope(purple and yellow outside part) as well as its caspid(red inner part).  When a virus leaves a cell, it sometimes takes its host cell membrane along, to create an envelope.  Herpes virus has single stranded RNA"));
		Entries.add(new JournalEntry("RabiesVirus.png","RabiesVirus.png","","","Rabies Viruses","Many viruses are often spherical in shape.  However other viruses such as the Rabies virus are cylindrical.  There are also other more irregular viral shapes."));
		
		
		for(int i = 0; i < Entries.size(); i++)
		{
			Sprite s = new Sprite(Rm.GetTexture(Entries.get(i).SmallIcon));
			s.setBounds(((i%5)*0.2f + 0.2f)* SCREEN_W, (0.6f-(float)Math.floor(i/5)*0.2f)*SCREEN_H, 0.16f*SCREEN_W, 0.16f*SCREEN_H);
			EntryIcons.add(s);
		}
		
		MuteButton = new Button(Rm.GetTexture("MusicOn.png"),new Vector2(0f,0.95f),new Vector2(0.05f,0.05f),SCREEN_W,SCREEN_H);
	}
	@Override
	public void render(float delta) {
		
		Gdx.gl.glClearColor(0, 0.9f, 0.9f, 0);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		if(JournalState == "")
		{
			Title.Draw(batch);
			BackToMenu.Draw(batch);
			for(int i = 0; i < EntryIcons.size(); i++)
			{
				EntryIcons.get(i).draw(batch);
			}
		}
		else
		{
			MiddleSprite.draw(batch);
			LeftSprite.draw(batch);
			RightSprite.draw(batch);
			EntryTitle.Draw(batch);
			EntryText.Draw(batch);
			BackButton.Draw(batch);
		}
		MuteButton.Draw(batch);
		batch.end();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		SCREEN_W = width;
		SCREEN_H = height;
		for(int i = 0; i < EntryIcons.size(); i++)
		{
			EntryIcons.get(i).setBounds(((i%5)*0.2f + 0.02f)* SCREEN_W, (0.6f-(float)Math.floor(i/5)*0.2f)*SCREEN_H, 0.16f*SCREEN_W, 0.16f*SCREEN_H);
		}
		EntryTitle.resize(SCREEN_W, SCREEN_H);
		EntryText.resize(SCREEN_W, SCREEN_H);
		BackButton.resize(SCREEN_W,SCREEN_H);
		Title.resize(SCREEN_W, SCREEN_H);
		BackToMenu.resize(SCREEN_W,SCREEN_H);
		MiddleSprite.setBounds(0.4f*SCREEN_W, 0.75f*SCREEN_H, 0.2f*SCREEN_W, 0.2f*SCREEN_H);
		LeftSprite.setBounds(0.1f*SCREEN_W, 0.75f*SCREEN_H, 0.2f*SCREEN_W, 0.2f*SCREEN_H);
		RightSprite.setBounds(0.7f*SCREEN_W, 0.75f*SCREEN_H, 0.2f*SCREEN_W, 0.2f*SCREEN_H);
		MuteButton.resize(width,height);
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		Gdx.input.setInputProcessor(this);
		if(Parent.PlayMusic) MuteButton.ChangeSprite(Rm.GetTexture("MusicOn.png"));
		else MuteButton.ChangeSprite(Rm.GetTexture("MusicOff.png"));
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
		if(MuteButton.IsWithin((float)screenX/(float)SCREEN_W, 1.0f - (float)screenY/(float)SCREEN_H))
		{
			Parent.Mute();
			if(Parent.PlayMusic) MuteButton.ChangeSprite(Rm.GetTexture("MusicOn.png"));
			else MuteButton.ChangeSprite(Rm.GetTexture("MusicOff.png"));
		}
		if(JournalState == "")
		{
			if(BackToMenu.IsWithin(screenX, screenY)) Parent.setScreen(Parent.MainMenu);
			for(int i = 0; i < EntryIcons.size(); i++)
			{
				if(EntryIcons.get(i).getBoundingRectangle().contains(screenX,SCREEN_H-screenY))
				{
					JournalState = Entries.get(i).Title;
					EntryTitle.setText(Entries.get(i).Title);
					EntryTitle.setBounds(0.5f, 0.65f, 0.9f, 0.1f);
					EntryTitle.TotalAdj();
					EntryText.setText(Entries.get(i).Description);
					EntryText.wrapText(1.0f);
					EntryText.TotalAdj();
					if(Entries.get(i).LeftShowcase != "") LeftSprite.setTexture(Rm.GetTexture(Entries.get(i).LeftShowcase));
					else LeftSprite.setTexture(Rm.GetTexture("Blank.png"));
					if(Entries.get(i).RightShowcase != "") RightSprite.setTexture(Rm.GetTexture(Entries.get(i).RightShowcase));
					else RightSprite.setTexture(Rm.GetTexture("Blank.png"));
					MiddleSprite.setTexture(Rm.GetTexture(Entries.get(i).MidShowcase));
					break;
				}
			}
		}
		else
		{
			if(BackButton.IsWithin(screenX, screenY)) JournalState = "";
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

}
