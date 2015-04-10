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
package es.javocsoft.android.lib.toucan.client;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import es.javocsoft.android.lib.toolbox.ToolBox;
import es.javocsoft.android.lib.toolbox.ToolBox.HTTP_METHOD;
import es.javocsoft.android.lib.toucan.client.json.GsonProcessor;
import es.javocsoft.android.lib.toucan.client.request.ACKRequest;
import es.javocsoft.android.lib.toucan.client.request.AppDevTagsOperationRequest;
import es.javocsoft.android.lib.toucan.client.request.DeviceRegistrationRequest;
import es.javocsoft.android.lib.toucan.client.request.bean.DeviceRegistrationBean;

/**
 * This is the Toucan client API library main class.<br><br>
 * 
 * See http://toucan.javocsoft.es for more info.
 * 
 * @author JavocSoft Team 
 * @version 1.0 $Rev: 695 $
 * $Author: jgonzalez $
 * $Date: 2015-04-10 16:20:59 +0200 (Fri, 10 Apr 2015) $
 */
public class ToucanClient {

	/** The instance of the API client */
	private static ToucanClient toucanClient;
	
	private static final String API_ENDPOINT_BASE = "https://api.toucan.javocsoft.es";
	
	private static final String OS_TAG = "Android";
	private static final String LOG_TAG = "ToucanClient";
	
	
	private Context context = null;
	private String appPublicKey = null;
	private String deviceUniqueId = null;
	private String deviceNotificationToken = null;
	private String apiToken = null;	
	
	private String OSInfo = null;
	private String DEVInfo = null;
	
	
	
	private static final String PREF_NAME = "toucan_client_prefs";
	private static final String PREF_KEY_DEVICE_UNIQUEID = "toucan_client_key_devuniqueid";
	private static final String PREF_KEY_DEVICE_NOT_TOKEN = "toucan_client_key_devnottoken";
	
	/** The message content key */
	public static final String NOTIFICATION_MESSAGE_TEXT = "message";
	/** The message notification reference key */
	public static final String NOTIFICATION_MESSAGE_REF = "nRef";
	/** The message notification id key */
	public static final String NOTIFICATION_MESSAGE_ID = "nId";
	/** The message notification timestamp key */
	public static final String NOTIFICATION_MESSAGE_TS = "ts";
	
		
	//ENDPOINTS OF THE API OPERATIONS
	private static final String API_ENDPOINT_REGISTRATION = API_ENDPOINT_BASE + "/PushNOTApi/NOTPushApi" +  "?dr";
	private static final String API_ENDPOINT_UNREGISTRATION = API_ENDPOINT_BASE + "/PushNOTApi/NOTPushApi" +  "?du";
	private static final String API_ENDPOINT_ENABLE_REGISTERED_DEVICE = API_ENDPOINT_BASE + "/PushNOTApi/NOTPushApi" +  "?de";
	private static final String API_ENDPOINT_ACK_RECEIVED = API_ENDPOINT_BASE + "/PushNOTApi/ackreport" +  "?op=2";
	private static final String API_ENDPOINT_ACK_READ = API_ENDPOINT_BASE + "/PushNOTApi/ackreport" +  "?op=1";
	private static final String API_ENDPOINT_ADD_TAGS = API_ENDPOINT_BASE + "/PushNOTApi/NOTPushApi" +  "?dta";
	private static final String API_ENDPOINT_REMOVE_TAGS = API_ENDPOINT_BASE + "/PushNOTApi/NOTPushApi" +  "?dtr";
	private static final String API_ENDPOINT_LIST_TAGS = API_ENDPOINT_BASE + "/PushNOTApi/NOTPushApi" +  "?dtl";
	
		
	private static final String API_OPERATION_DEVICE_REGISTRATION = "DeviceRegistration";
	private static final String API_OPERATION_DEVICE_UNREGISTRATION = "DeviceUnRegistration";
	private static final String API_OPERATION_DEVICE_ENABLE = "DeviceEnableRegistered";
	private static final String API_OPERATION_INFORM_REFERRAL = "InformReferral";
	private static final String API_OPERATION_ADD_TAGS = "AddTags";
	private static final String API_OPERATION_REMOVE_TAGS = "RemoveTags";
	private static final String API_OPERATION_LIST_TAGS = "ListTags";
	private static final String API_OPERATION_ACK_RECEIVED = "NotificationReceivedACK";
	private static final String API_OPERATION_ACK_READ = "NotificationReadACK";
	
	
	
	
	/**
	 * Gets the instance of the Toucan client.
	 * 
	 * @param context
	 * @param apiToken
	 * @param appPublicKey
	 * @param deviceNotificationToken
	 * @return
	 */
	public static ToucanClient getInstance(Context context, 
			String apiToken, String appPublicKey) {
		if(toucanClient==null) {
			toucanClient = new ToucanClient();			
			toucanClient.context = context;
			toucanClient.appPublicKey = appPublicKey;			
			toucanClient.apiToken = apiToken;			
			
			toucanClient.init();
		}		
		return toucanClient;
	}
	
	private void init() {
		OSInfo = OS_TAG + " " + ToolBox.device_getOSVersion() + " - " + "(API Level " + ToolBox.device_getAPILevel() + ")";
		DEVInfo = ToolBox.device_getExtraInfo();
		
		//Obtain the device unique id
		if(!ToolBox.prefs_existsPref(context, PREF_NAME, PREF_KEY_DEVICE_UNIQUEID)) {
			String devUniqueId = ToolBox.device_getId(context);
			ToolBox.prefs_savePreference(context, PREF_NAME, PREF_KEY_DEVICE_UNIQUEID, String.class, devUniqueId);
			toucanClient.deviceUniqueId = devUniqueId;
		}else{
			toucanClient.deviceUniqueId = (String)ToolBox.prefs_readPreference(context, PREF_NAME, PREF_KEY_DEVICE_UNIQUEID, String.class);
		}
		
	}
	
	/* Avoids normal instance */
	private ToucanClient() {}
	
	
	//PUBLIC METHODS
		
	public void deviceRegistration(String notificationToken) {
		//Save the device GCM notification token.
		toucanClient.deviceNotificationToken = notificationToken;
		ToolBox.prefs_savePreference(toucanClient.context, PREF_NAME, PREF_KEY_DEVICE_NOT_TOKEN, String.class, notificationToken);
		
		DeviceRegistrationRequest devRegRequest = generateDeviceRegistrationInfo(notificationToken);
		launchDeviceRegistrationRequest(devRegRequest);		
	}
	
	public void deviceRegistration(String notificationToken, int externalId) {
		//Save the device GCM notification token.
		toucanClient.deviceNotificationToken = notificationToken;
		ToolBox.prefs_savePreference(toucanClient.context, PREF_NAME, PREF_KEY_DEVICE_NOT_TOKEN, String.class, notificationToken);
		
		DeviceRegistrationRequest devRegRequest = generateDeviceRegistrationInfo(notificationToken);
		if(externalId>=1) {
			devRegRequest.getData().setExtId(externalId);
		
			String hashSignature = devRegRequest.getData().getSecurityHash(appPublicKey);
			devRegRequest.setHashSignature(hashSignature);
		}
				
		launchDeviceRegistrationRequest(devRegRequest);
	}
	
	public void informInstallReferral(String installReferral) {
		
		if(isNotificationTokenPresent()) {
			DeviceRegistrationRequest devRegRequest = 
					generateDeviceRegistrationInfo(toucanClient.deviceNotificationToken);
			if(installReferral!=null && installReferral.length()>0) {
				devRegRequest.getData().setInstallReferral(installReferral);
				
				String hashSignature = devRegRequest.getData().getSecurityHash(appPublicKey);
				devRegRequest.setHashSignature(hashSignature);
			}
			
			launchInformReferralRequest(devRegRequest);
		}else{
			Log.i(LOG_TAG, API_OPERATION_INFORM_REFERRAL.toUpperCase() + " Error. Notification token not stablished. Please, execute 'deviceRegistration()' first.");
		}
	}
	
	public void doReceivedACK(Bundle notificationBundle) {
		if(isNotificationTokenPresent()) {
			ACKRequest ackRequest = generateACKnfo(notificationBundle);		
			new ToucanPostWorker(ackRequest, API_ENDPOINT_ACK_RECEIVED, API_OPERATION_ACK_RECEIVED).start();
		}else{
			Log.i(LOG_TAG, API_OPERATION_ACK_RECEIVED.toUpperCase() + " Error. Notification token not stablished. Please, execute 'deviceRegistration()' first.");
		}
	}
		
	public void doReadACK(Bundle notificationBundle) {
		if(isNotificationTokenPresent()) {
			ACKRequest ackRequest = generateACKnfo(notificationBundle);
			new ToucanPostWorker(ackRequest, API_ENDPOINT_ACK_READ, API_OPERATION_ACK_READ).start();
		}else{
			Log.i(LOG_TAG, API_OPERATION_ACK_READ.toUpperCase() + " Error. Notification token not stablished. Please, execute 'deviceRegistration()' first.");
		}
	}
	
	public void doAddTags(List<String> tags) {
		//Prepare device registration request
		AppDevTagsOperationRequest tagAddRequest = new AppDevTagsOperationRequest();
		tagAddRequest.setAppKey(toucanClient.appPublicKey);
		tagAddRequest.setDevId(toucanClient.deviceUniqueId);
		tagAddRequest.setTags(tags);
				
		String appHashSignature = generateSHA1(appPublicKey + apiToken);
		tagAddRequest.setAppHashSignature(appHashSignature);
				
		String hashSignature = tagAddRequest.getSecurityHash(appPublicKey);
		tagAddRequest.setHashSignature(hashSignature);
				
		new ToucanPostWorker(tagAddRequest, API_ENDPOINT_ADD_TAGS, API_OPERATION_ADD_TAGS).start();		
	}
	
	public void doRemoveTags(List<String> tags) {
		//Prepare device registration request
		AppDevTagsOperationRequest tagAddRequest = new AppDevTagsOperationRequest();
		tagAddRequest.setAppKey(toucanClient.appPublicKey);
		tagAddRequest.setDevId(toucanClient.deviceUniqueId);
		tagAddRequest.setTags(tags);
				
		String appHashSignature = generateSHA1(appPublicKey + apiToken);
		tagAddRequest.setAppHashSignature(appHashSignature);
				
		String hashSignature = tagAddRequest.getSecurityHash(appPublicKey);
		tagAddRequest.setHashSignature(hashSignature);
				
		new ToucanPostWorker(tagAddRequest, API_ENDPOINT_REMOVE_TAGS, API_OPERATION_REMOVE_TAGS).start();		
	}
	
	public void doListTags() {
		try{ 
			String appHashSignature = generateSHA1(appPublicKey + apiToken);
		
			String urlParams = "dUId=" + toucanClient.deviceUniqueId + "&appPubKey=" + appPublicKey + "&appHashSignature=" + appHashSignature;
			String encodedUrlParams = new String(Base64.encode(urlParams.getBytes(), 0), "UTF-8");
			String urlEncodedUrlParams = URLEncoder.encode(encodedUrlParams, "UTF-8");
		
			String finalUrl = API_ENDPOINT_LIST_TAGS + "=" + urlEncodedUrlParams;
		
			new ToucanGetWorker(finalUrl, API_OPERATION_LIST_TAGS).start();
			
		}catch(Exception e){
			Log.e(LOG_TAG, "Error doing operation " + API_OPERATION_LIST_TAGS.toUpperCase() + " to Toucan API (" + e.getMessage() + ")", e);
		}
	}
	
	public void doDeviceUnregister() {
		try{ 
			String appHashSignature = generateSHA1(appPublicKey + apiToken);
		
			String urlParams = "dUId=" + toucanClient.deviceUniqueId + "&appPubKey=" + appPublicKey + "&appHashSignature=" + appHashSignature;
			String encodedUrlParams = new String(Base64.encode(urlParams.getBytes(), 0), "UTF-8");
			String urlEncodedUrlParams = URLEncoder.encode(encodedUrlParams, "UTF-8");
		
			String finalUrl = API_ENDPOINT_UNREGISTRATION + "=" + urlEncodedUrlParams;
		
			new ToucanGetWorker(finalUrl, API_OPERATION_DEVICE_UNREGISTRATION).start();
			
		}catch(Exception e){
			Log.e(LOG_TAG, "Error doing operation " + API_OPERATION_DEVICE_UNREGISTRATION.toUpperCase() + " to Toucan API (" + e.getMessage() + ")", e);
		}
	}
	
	public void doEnableRegisteredDevice() {
		
		try{ 
			String appHashSignature = generateSHA1(appPublicKey + apiToken);
		
			String urlParams = "dUId=" + toucanClient.deviceUniqueId + "&appPubKey=" + appPublicKey + "&appHashSignature=" + appHashSignature;
			String encodedUrlParams = new String(Base64.encode(urlParams.getBytes(), 0), "UTF-8");
			String urlEncodedUrlParams = URLEncoder.encode(encodedUrlParams, "UTF-8");
		
			String finalUrl = API_ENDPOINT_ENABLE_REGISTERED_DEVICE + "=" + urlEncodedUrlParams;
		
			new ToucanGetWorker(finalUrl, API_OPERATION_DEVICE_ENABLE).start();
			
		}catch(Exception e){
			Log.e(LOG_TAG, "Error doing operation " + API_OPERATION_DEVICE_ENABLE.toUpperCase() + " to Toucan API (" + e.getMessage() + ")", e);
		}
	}
	
	
	
	//AUXILIAR
	
	private boolean isNotificationTokenPresent() {
		if(ToolBox.prefs_existsPref(toucanClient.context, PREF_NAME, PREF_KEY_DEVICE_NOT_TOKEN)){
			toucanClient.deviceNotificationToken = (String)ToolBox.prefs_readPreference(toucanClient.context, PREF_NAME, PREF_KEY_DEVICE_NOT_TOKEN, String.class);
		}
		
		if(toucanClient.deviceNotificationToken!=null && 
					toucanClient.deviceNotificationToken.length()>0) {
			return true;
		}else{
			return false;
		}		
	}
	
	private ACKRequest generateACKnfo(Bundle notificationBundle) {
		//Prepare device registration request
		ACKRequest ackRequest = new ACKRequest();
		ackRequest.setAppKey(toucanClient.appPublicKey);
		ackRequest.setnId(notificationBundle.getString(ToucanClient.NOTIFICATION_MESSAGE_ID));
		ackRequest.setnRef(notificationBundle.getString(ToucanClient.NOTIFICATION_MESSAGE_REF));
		ackRequest.setToken(toucanClient.deviceNotificationToken);		
		//ackRequest.setMessage(message);
		
		String appHashSignature = generateSHA1(appPublicKey + apiToken);		
		ackRequest.setAppHashSignature(appHashSignature);
		
		return ackRequest;
	}
	
	private DeviceRegistrationRequest generateDeviceRegistrationInfo(String notificationToken) {
		
		DeviceRegistrationBean devRegBean = new DeviceRegistrationBean();
		devRegBean.setDevId(deviceUniqueId);		
		devRegBean.setAppVersion(ToolBox.application_getVersionCode(context));
		devRegBean.setDevResType(ToolBox.device_getResolutionType(context).name());
		devRegBean.setDevLocale(ToolBox.device_getLanguage());
		devRegBean.setDevOs(OSInfo);
		devRegBean.setDevExtra(DEVInfo);
		
		devRegBean.setNotToken(notificationToken);
		
		//Prepare device registration request
		DeviceRegistrationRequest devRegRequest = new DeviceRegistrationRequest();
		devRegRequest.setAppKey(appPublicKey);
		devRegRequest.setData(devRegBean);
		
		String appHashSignature = generateSHA1(appPublicKey + apiToken);
		devRegRequest.setAppHashSignature(appHashSignature);
		
		String hashSignature = devRegBean.getSecurityHash(appPublicKey);
		devRegRequest.setHashSignature(hashSignature);
		
		return devRegRequest;
	}
	
	private void launchDeviceRegistrationRequest(DeviceRegistrationRequest devRegRequest) {
		new ToucanPostWorker(devRegRequest, API_ENDPOINT_REGISTRATION, API_OPERATION_DEVICE_REGISTRATION).start();
	}
	
	private void launchInformReferralRequest(DeviceRegistrationRequest devRegRequest) {
		new ToucanPostWorker(devRegRequest, API_ENDPOINT_REGISTRATION, API_OPERATION_INFORM_REFERRAL).start();
	}
		
	private static String generateSHA1(String data) {
		return new String(Hex.encodeHex(DigestUtils.sha(data)));
	}
	
	//AUXILIAR CLASSES
	
	class ToucanPostWorker extends Thread implements Runnable {

		private Object data;
		private String endpoint;
		private String opname;
		
		public ToucanPostWorker(Object data, String endpoint, String opName) {
			this.data = data;
			this.endpoint = endpoint;
			this.opname = opName;
		}
		
		
		@Override
		public void run() {
			doWork();
		}
		
		
		//AUXILIAR
		
		public void doWork() {			
			//Do the POST request to the API			
			try {
				//Prepare header data for Toucan API
				Map<String, String> headersData = new HashMap<String, String>();
				headersData.put("Authorization", "ttmSecTKN " + apiToken);
				
				String jsonData = GsonProcessor.getInstance().gson.toJson(data);
				String finalUrl = (endpoint);
				
				String jsonDataKey = null;
	        	String response = ToolBox.net_httpclient_doAction(
	        			HTTP_METHOD.POST, 
	        			finalUrl, 
	        			jsonDataKey, jsonData, headersData);
	        	
	        	Log.i(LOG_TAG, opname.toUpperCase() + ". Request <<" + jsonData + 
                        ">>. Sent to Toucan API. Call response '" + response + "'");                
	        	
        	}catch(Exception e) {
        		Log.e(LOG_TAG, "Error in operation " + opname.toUpperCase() + " request to Toucan API (" + e.getMessage() + ")", e);
        	}
		}
	}
	
	class ToucanGetWorker extends Thread implements Runnable {

		private String endpoint;
		private String opname;
		
		public ToucanGetWorker(String endpoint, String opName) {
			this.endpoint = endpoint;
			this.opname = opName;
		}
		
		
		@Override
		public void run() {
			doWork();
		}
		
		
		//AUXILIAR
		
		public void doWork() {			
			//Do the POST request to the API			
			try {
				//Prepare header data for Toucan API
				Map<String, String> headersData = new HashMap<String, String>();
				headersData.put("Authorization", "ttmSecTKN " + apiToken);
				
				String finalUrl = endpoint;
				
				String jsonData = null;
				String jsonDataKey = null;
	        	String response = ToolBox.net_httpclient_doAction(
	        			HTTP_METHOD.GET, 
	        			finalUrl, 
	        			jsonDataKey, jsonData, headersData);
	        	
	        	Log.i(LOG_TAG, opname.toUpperCase() + 
	        			". Sent to Toucan API. Call response '" + response + "'");                
	        	
        	}catch(Exception e) {
        		Log.e(LOG_TAG, "Error in operation " + opname.toUpperCase() + " request to Toucan API (" + e.getMessage() + ")", e);
        	}
		}
	}
	
}
