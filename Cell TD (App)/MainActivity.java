package com.pluripotence.celltd;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.pluripotence.celltd.MyGdxGame;
import com.revmob.RevMob;

public class MainActivity extends AndroidApplication{
	private RevMob rm;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useGL20 = false;
        cfg.useWakelock = true;
        
        
        FileOutputStream fo;
        try {
			fo = openFileOutput("Highscore.txt", Context.MODE_APPEND);
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
        
        rm = RevMob.start(this);
        //rm.setTestingMode(RevMobTestingMode.WITH_ADS);
        initialize(new MyGdxGame(rm, this), cfg);
    }
}