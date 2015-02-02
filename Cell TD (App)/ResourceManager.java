package com.pluripotence.celltd;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

public class ResourceManager {
	
	public ResourceManager()
	{
		Textures = new Hashtable<String, Texture>();
	}
	public boolean LoadTexture(String TextureName)
	{
		if(Textures.containsKey(TextureName))
		{
			return false;
		}
		else
		{
			Texture t = new Texture(Gdx.files.internal("data/"+TextureName));
			
			t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			Textures.put(TextureName,t);
			return true;
		}
	}
	
	public Texture GetTexture(String TextureName)
	{
		return Textures.get(TextureName);
	}
	
	public void Dispose()
	{
		Set<String> set = Textures.keySet();
		Iterator<String> itr = set.iterator();
		while(itr.hasNext()) Textures.get(itr.next()).dispose();
		
	}
	
	protected Hashtable<String,Texture> Textures;
}
