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
package es.javocsoft.android.lib.toucan.client.request;

import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;


/**
 * To make a request to insert tags for 
 * an application and a device.
 * 
 * @author JavocSoft Team 
 * @version 1.0 $Rev: 695 $
 * $Author: jgonzalez $
 * $Date: 2015-04-10 16:20:59 +0200 (Fri, 10 Apr 2015) $
 */
public class AppDevTagsOperationRequest {
	
	
	private String appKey;
	private String devId;
	private String appHashSignature; //SHA-1 of PubKey + API TOKEN
	private String hashSignature; //SHA-1 of the request data bean 
	
	private List<String> tags;
		
	
	public AppDevTagsOperationRequest() {}

	
	public AppDevTagsOperationRequest(String appKey, String devId, List<String> tags, String appHashSignature, String hashSignature) {		
		this.appKey = appKey;
		this.devId = devId;
		this.tags = tags;
		this.appHashSignature = appHashSignature;
		this.hashSignature = hashSignature;				
	}
	
	public void fillFromRequest(AppDevTagsOperationRequest data) {
		this.appKey = data.appKey;
		this.devId = data.devId;
		this.tags = data.tags;
		this.appHashSignature = data.appHashSignature;
		this.hashSignature = data.hashSignature;		
	}
	
	
	//GETTERS & SETTERS
	
	public String getAppKey() {
		return appKey;
	}
	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}
	
	public String getDevId() {
		return devId;
	}
	public void setDevId(String devId) {
		this.devId = devId;
	}

	public String getAppHashSignature() {
		return appHashSignature;
	}
	public void setAppHashSignature(String appHashSignature) {
		this.appHashSignature = appHashSignature;
	}

	public String getHashSignature() {
		return hashSignature;
	}
	public void setHashSignature(String hashSignature) {
		this.hashSignature = hashSignature;
	}

	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	
	/**
	 * Creates a hash using a key for afterwards
	 * verify the data.
	 * 
	 * @param key	A unique key for the application.
	 * @return
	 */
	public String getSecurityHash(String key) {
		return createSHA1Hash(key + "/" +
				getDataAsString());
	}
	
	public String getDataAsString() {
		
		StringBuffer sBuffer = new StringBuffer();
		for(String s:this.tags){			
			if(sBuffer.length()==0){
				sBuffer.append(s);
			}else{
				sBuffer.append(";#;").append(s);
			}			
		}		
		
		return sBuffer.toString();
	}
	
	/**
	 * Creates a SHA-1 hash from a string.
	 * 
	 * @param data
	 * @return
	 */
	private String createSHA1Hash(String data) {
		return new String(Hex.encodeHex(DigestUtils.sha(data)));				
	}
	
}
