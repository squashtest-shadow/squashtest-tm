/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.csp.launcher.ihm;

import java.awt.Desktop;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.squashtest.csp.launcher.ihm.window.SimpleWindow;

public class Starter {

	//Squash url
	private static final String SQUASH_URL = "http://127.0.0.1:8080/squash/";
	//Number of milliseconds until new http request
	static int intervals = 5000;

	/**
	 * @param args
	 */
	public static void main(String[] args) {


		SimpleWindow sw = new SimpleWindow();
		sw.setVisible( true );

		boolean stop = false;

		while(!stop){
			stop = myTask();
			try {
				Thread.sleep(intervals); //5 secs
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(stop){
			try {
				Desktop.getDesktop().browse(new URI(Starter.SQUASH_URL));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//close window
			System.exit(0);
		}
	}

	public static boolean myTask(){
		HttpURLConnection http = null;
		String response = "";
		try {
			http = (HttpURLConnection) new URL(Starter.SQUASH_URL).openConnection();
			http.setRequestMethod("GET");
			http.connect();
			response = http.getResponseMessage();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// System.out.println("response : " + response);

		return response.equalsIgnoreCase("ok")?true:false;
	}
}
