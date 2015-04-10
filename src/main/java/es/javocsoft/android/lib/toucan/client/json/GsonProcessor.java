/**
 * JavocSoft Toucan API Client Library.
 *
 *   Copyright (C) 2013 JavocSoft - Javier González Serrano.
 *
 *   This file is part of JavcoSoft Toucan API client Library.
 *
 *   This library is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   JavocSoft Toucan Client Library is distributed in the hope that it will 
 *   be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 *   of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with JavocSoft Toucan API Client Library. If not, 
 *   see <http://www.gnu.org/licenses/>.
 */
package es.javocsoft.android.lib.toucan.client.json;

import java.lang.reflect.Modifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This singleton is used to get a valid simple JSON parser.
 * 
 * @author JavocSoft Team 
 * @version 1.0 $Rev: 695 $
 * $Author: jgonzalez $
 * $Date: 2015-04-10 16:20:59 +0200 (Fri, 10 Apr 2015) $
 */
public class GsonProcessor {

	private static GsonProcessor gsonProcessor = null;
	public Gson gson = null;

	public static GsonProcessor getInstance() {
		if (gsonProcessor == null) {
			gsonProcessor = new GsonProcessor();
		}

		return gsonProcessor;
	}
	
	private GsonProcessor(){
		GsonBuilder gsonBuilder = new GsonBuilder();
		
		//This allows to avoid fields with the transient attribute
		gson = gsonBuilder.excludeFieldsWithModifiers(Modifier.TRANSIENT).create();
	}
}
