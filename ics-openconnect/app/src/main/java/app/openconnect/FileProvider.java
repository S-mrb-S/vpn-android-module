/*
 * Adapted from OpenVPN for Android
 * Copyright (c) 2012-2013, Arne Schwabe
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentProvider;
import android.content.ContentProvider.PipeDataWriter;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;

/**
 * A very simple content provider that can serve arbitrary asset files from
 * our .apk.
 */
public class FileProvider extends ContentProvider
implements PipeDataWriter<InputStream> {
	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		try {
			File dumpfile = getFileFromURI(uri);


			MatrixCursor c = new MatrixCursor(projection);

			Object[] row = new Object[projection.length];
			int i=0;
			for (String r:projection) {
				if(r.equals(OpenableColumns.SIZE))
					row[i] = dumpfile.length();
				if(r.equals(OpenableColumns.DISPLAY_NAME))
					row[i] = dumpfile.getName();
				i++;
			}
			c.addRow(row);
			return c;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}


	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// Don't support inserts.
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// Don't support deletes.
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// Don't support updates.
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// For this sample, assume all files are .apks.
		return "application/octet-stream";
	}

	@Override
	public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
		File dumpfile = getFileFromURI(uri);

		try {

			InputStream is = new FileInputStream(dumpfile);
			// Start a new thread that pipes the stream data back to the caller.
			return new AssetFileDescriptor(
					openPipeHelper(uri, null, null, is, this), 0,
					dumpfile.length());
		} catch (IOException e) {
            throw new FileNotFoundException("Unable to open minidump " + uri);
		}
	}

	private File getFileFromURI(Uri uri) throws FileNotFoundException {
		// Try to open an asset with the given name.
		String path = uri.getPath();
		if(path.startsWith("/"))
			path = path.replaceFirst("/", "");       

		// I think this already random enough, no need for magic secure cookies
		// 1f9563a4-a1f5-2165-255f2219-111823ef.dmp
		if (!path.matches("^[0-9a-z-.]*(dmp|dmp.log)$"))
			throw new FileNotFoundException("url not in expect format " + uri);
		File cachedir = getContext().getCacheDir();
        return new File(cachedir,path);
	}

	@Override
	public void writeDataToPipe(ParcelFileDescriptor output, Uri uri, String mimeType,
			Bundle opts, InputStream args) {
		// Transfer data from the asset to the pipe the client is reading.
		byte[] buffer = new byte[8192];
		int n;
		FileOutputStream fout = new FileOutputStream(output.getFileDescriptor());
		try {
			while ((n=args.read(buffer)) >= 0) {
				fout.write(buffer, 0, n);
			}
		} catch (IOException e) {
			Log.i("OpenVPNFileProvider", "Failed transferring", e);
		} finally {
			try {
				args.close();
			} catch (IOException e) {
			}
			try {
				fout.close();
			} catch (IOException e) {
			}
		}
	}
}
