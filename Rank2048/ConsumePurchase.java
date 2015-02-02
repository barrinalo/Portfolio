package com.codelessweb.rank2048.android;

import com.android.vending.billing.IInAppBillingService;
import com.codelessweb.rank2048.Rank2048;

import android.os.AsyncTask;
import android.os.RemoteException;

public class ConsumePurchase extends AsyncTask<String, Integer, Integer>{

	Rank2048 GameHandle;
	IInAppBillingService mService;
	public ConsumePurchase(Rank2048 gh, IInAppBillingService serv) {
		GameHandle = gh;
		mService = serv;
	}
	@Override
	protected Integer doInBackground(String... data) {
		int response = -1;
		try {
			response = mService.consumePurchase(3, data[1], data[0]);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

}
