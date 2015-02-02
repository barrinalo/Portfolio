package com.codelessweb.rank2048;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;


public class ResourceLoader extends Thread{
	
	protected Texture Imgs[];
	protected Map<Integer, String> SetsToLoad;
	public ResourceLoader(Texture t[], Map<Integer, String> SetsToLoad) {
		Imgs = t;
		this.SetsToLoad = SetsToLoad;
	}
	public void run() {
		for(int i = 0; i < Imgs.length; i++) if(SetsToLoad.containsKey(i)) Imgs[i] = new Texture(Gdx.files.internal(SetsToLoad.get(i)));
	}
}
