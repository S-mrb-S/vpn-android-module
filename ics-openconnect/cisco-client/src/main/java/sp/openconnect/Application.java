/*
 * Copyright (c) 2013, Kevin Cernekee
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * In addition, as a special exception, the copyright holders give
 * permission to link the code of portions of this program with the
 * OpenSSL library.
 */
package sp.openconnect;

import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import sp.openconnect.core.FragCache;
import sp.openconnect.core.ProfileManager;

public abstract class Application extends de.blinkt.openvpn.core.App {
	@Override
	public void onCreate() {
		super.onCreate();

		try {
			checkProcessorModel();
			ProfileManager.init(getApplicationContext());
			FragCache.init();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private void checkProcessorModel() {
			String processorModel = Build.CPU_ABI;
			if (processorModel.equals("x86") || processorModel.equals("x86_64")) {
				if(isShowToastOpenVpn){
					Toast.makeText(this, "هشدار! این مدل از پردازنده برای اتصال به سیسکو پشتیبانی نمیشود.",
							Toast.LENGTH_LONG).show();
				}
			}else{
				System.loadLibrary("openconnect");
				System.loadLibrary("stoken");
			}
	}

}
