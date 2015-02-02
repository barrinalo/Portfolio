package com.codelessweb.rank2048.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.android.vending.billing.IInAppBillingService;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.codelessweb.rank2048.Rank2048;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class AndroidLauncher extends AndroidApplication{
	
	LinearLayout l, Splash;
	Rank2048 Game;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_OK && requestCode == 1001) {
			try {
				JSONObject ResultData = new JSONObject(data.getStringExtra("INAPP_PURCHASE_DATA"));
				if(ResultData.getString("productId") == "100Undo") {
					String token[] = {ResultData.getString("purchaseToken"), getPackageName()};
					ConsumePurchase cp = new ConsumePurchase(Game, mService){
						@Override
						protected void onPostExecute(Integer result) {
							if(result == 0) {
								GameHandle.AddUndos();
							}
						}
					};
					cp.execute(token);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		bindService(new 
		        Intent("com.android.vending.billing.InAppBillingService.BIND"),
		                mServiceConn, Context.BIND_AUTO_CREATE);
		
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useWakelock = true;
        
        Display D = getWindowManager().getDefaultDisplay();
        int height = D.getHeight();
        
		l = new LinearLayout(this);
		l.setOrientation(LinearLayout.VERTICAL);
		
		
		
		AdView ad = new AdView(this);
		ad.setAdSize(AdSize.BANNER);
		ad.setAdUnitId("ca-app-pub-2079986346861991/8789014667");
		AdRequest request = new AdRequest.Builder().build();
		ad.loadAd(request);
		
		LayoutParams GameParams = new LayoutParams(LayoutParams.MATCH_PARENT,(int) (height * 0.8f));
		
		LayoutParams AdParams = new LayoutParams(LayoutParams.MATCH_PARENT,(int) (height * 0.2f));
		
		Game = new Rank2048(mService, this, this);
		View v = initializeForView(Game, config);
		ad.setBackgroundColor(Color.WHITE);
		l.addView(v, GameParams);
		l.addView(ad, AdParams);
		
		setContentView(l);
	}
	IInAppBillingService mService;

	ServiceConnection mServiceConn = new ServiceConnection() {
	   @Override
	   public void onServiceDisconnected(ComponentName name) {
	       mService = null;
	   }

	   @Override
	   public void onServiceConnected(ComponentName name, 
	      IBinder service) {
	       mService = IInAppBillingService.Stub.asInterface(service);
	   }
	};
}
