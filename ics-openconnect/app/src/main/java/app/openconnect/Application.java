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
package app.openconnect;

import android.os.Build;
import android.widget.Toast;

import app.openconnect.core.FragCache;
import app.openconnect.core.ProfileManager;

public class Application extends android.app.Application {
	protected boolean showUnSupportToast = true;

	public void onCreate() {
		super.onCreate();

		checkProcessorModel();
		System.loadLibrary("openconnect");
		System.loadLibrary("stoken");
		ProfileManager.init(getApplicationContext());
		FragCache.init();
	}

	private void checkProcessorModel() {
		if(showUnSupportToast){
			String processorModel = Build.CPU_ABI;

			if (processorModel.equals("x86") || processorModel.equals("x86_64")) {
				Toast.makeText(this, "هشدار! این مدل از پردازنده برای اتصال به سیسکو پشتیبانی نمیشود.",
						Toast.LENGTH_LONG).show();
			}
		}
	}

}
