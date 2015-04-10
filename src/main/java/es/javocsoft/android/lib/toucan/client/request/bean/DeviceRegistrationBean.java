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
package es.javocsoft.android.lib.toucan.client.request.bean;

import java.util.Date;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;



/**
 * Device registration information bean.
 * 
 *  @author JavocSoft Team 
 *  @version 1.0 $Rev: 695 $
 *  $Author: jgonzalez $
 *  $Date: 2015-04-10 16:20:59 +0200 (Fri, 10 Apr 2015) $
 */
public class DeviceRegistrationBean {
	
	private int id;
		
	private int appVersion;
	
	private int extId;
	private String notToken;
	
	private String devId;
	private String devOs;
	private String devExtra;
	private String devLocale;
	private String devResType;
	private String installReferral;
	
	private Date tsCreation;
	private Date tsUpdate;
	
	
	public DeviceRegistrationBean(int id, int appVersion, int extId,
			String notToken, String devId, String devOs, String devExtra, 
			String devLocale, String devResType, 
			String installReferral,
			Date tsCreation, Date tsUpdate) {
		super();
		this.id = id;
		this.appVersion = appVersion;
		this.extId = extId;
		this.notToken = notToken;
		this.devId = devId;
		this.devOs = devOs;
		this.devExtra = devExtra;
		this.devLocale = devLocale;
		this.devResType = devResType;
		this.installReferral = installReferral;
		this.tsCreation = tsCreation;
		this.tsUpdate = tsUpdate;
	}
	
	public DeviceRegistrationBean() {
		super();
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
	
	/**
	 * gets the data as an string with field separated by ;#;
	 * @return
	 */
	public String getDataAsString() {
		return this.appVersion + ";#;" + this.extId + ";#;" +  
				(this.devId==null?"NONE":this.devId) + ";#;" + 				
				(this.devLocale==null?"NONE":this.devLocale) + ";#;" + 
				(this.devOs==null?"NONE":this.devOs) + ";#;" + 
				(this.devExtra==null?"NONE":this.devExtra) + ";#;" +
				(this.devResType==null?"NONE":this.devResType) + ";#;" + 
				(this.installReferral==null?"NONE":this.installReferral) + ";#;" +
				(this.notToken==null?"NONE":this.notToken);
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
	
	//GETTERS & SETTERS
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public int getAppVersion() {
		return appVersion;
	}
	public void setAppVersion(int appVersion) {
		this.appVersion = appVersion;
	}
	
	public int getExtId() {
		return extId;
	}
	public void setExtId(int extId) {
		this.extId = extId;
	}
	
	public String getNotToken() {
		return notToken;
	}
	public void setNotToken(String notToken) {
		this.notToken = notToken;
	}
	
	public String getDevId() {
		return devId;
	}
	public void setDevId(String devId) {
		this.devId = devId;
	}
	
	public String getDevOs() {
		return devOs;
	}
	public void setDevOs(String devOs) {
		this.devOs = devOs;
	}
	
	public String getDevExtra() {
		return devExtra;
	}
	public void setDevExtra(String devExtra) {
		this.devExtra = devExtra;
	}

	public String getDevLocale() {
		return devLocale;
	}
	public void setDevLocale(String devLocale) {
		this.devLocale = devLocale;
	}
	
	public String getDevResType() {
		return devResType;
	}
	public void setDevResType(String devResType) {
		this.devResType = devResType;
	}
	
	public String getInstallReferral() {
		return installReferral;
	}
	public void setInstallReferral(String installReferral) {
		this.installReferral = installReferral;
	}

	public Date getTsCreation() {
		return tsCreation;
	}
	public void setTsCreation(Date tsCreation) {
		this.tsCreation = tsCreation;
	}
	
	public Date getTsUpdate() {
		return tsUpdate;
	}
	public void setTsUpdate(Date tsUpdate) {
		this.tsUpdate = tsUpdate;
	}
	
}
