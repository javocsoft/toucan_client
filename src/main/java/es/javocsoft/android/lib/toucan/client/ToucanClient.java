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
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import es.javocsoft.android.lib.toolbox.ToolBox;
import es.javocsoft.android.lib.toolbox.json.GsonProcessor;
import es.javocsoft.android.lib.toucan.client.request.ACKRequest;
import es.javocsoft.android.lib.toucan.client.request.AppDevTagsOperationRequest;
import es.javocsoft.android.lib.toucan.client.request.DeviceRegistrationRequest;
import es.javocsoft.android.lib.toucan.client.request.bean.DeviceRegistrationBean;
import es.javocsoft.android.lib.toucan.client.service.PendingOperationsDeliveryService;
import es.javocsoft.android.lib.toucan.client.thread.ToucanGetWorker;
import es.javocsoft.android.lib.toucan.client.thread.ToucanPostWorker;
import es.javocsoft.android.lib.toucan.client.thread.ToucanWorker;
import es.javocsoft.android.lib.toucan.client.thread.ToucanWorker.TOUCAN_WORKER_POST_DATA_TYPE;
import es.javocsoft.android.lib.toucan.client.thread.callback.ResponseCallback;

/**
 * This is the Toucan client API library main class.<br><br>
 * 
 * See http://toucan.javocsoft.es for more info.
 * 
 * @author JavocSoft Team 
 * @version 1.0 $Rev: 720 $
 * $Author: jgonzalez $
 * $Date: 2015-04-29 12:34:12 +0200 (Wed, 29 Apr 2015) $
 */
public class ToucanClient {

	/** The instance of the API client */
	private static ToucanClient toucanClient;
	
	//private static final String API_ENDPOINT_BASE = "https://api.toucan.javocsoft.es";
	private static final String API_ENDPOINT_BASE = "http://clientes.tatamia.com";
	
	private static final String OS_TAG = "Android";
	public static final String LOG_TAG = "ToucanClient";
	
	private Context context = null;
	private String appPublicKey = null;
	private String deviceUniqueId = null;
	private String deviceNotificationToken = null;
	private String apiToken = null;	
	
	private String OSInfo = null;
	private String DEVInfo = null;
	
	/** The Toucan HTTP worker type. */
	public static enum TOUCAN_WORKER_TYPE {GET, POST};
	
	
	private static final String PREF_NAME = "toucan_client_prefs";
	private static final String PREF_KEY_DEVICE_UNIQUEID = "toucan_client_key_devuniqueid";
	private static final String PREF_KEY_DEVICE_NOT_TOKEN = "toucan_client_key_devnottoken";
	
	/** All pending deliveries, when they can not be delivered, are store in cache files. */
	public static final String CACHED_REQUEST_FILE_PREFIX = "toucan_client_pending_request_";
	
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
	private static final String API_ENDPOINT_RESET_TAGS = API_ENDPOINT_BASE + "/PushNOTApi/NOTPushApi" +  "?dtrs";
		
	private static final String API_OPERATION_DEVICE_REGISTRATION = "DeviceRegistration";
	private static final String API_OPERATION_DEVICE_UNREGISTRATION = "DeviceUnRegistration";
	private static final String API_OPERATION_DEVICE_ENABLE = "DeviceEnableRegistered";
	private static final String API_OPERATION_INFORM_REFERRAL = "InformReferral";
	private static final String API_OPERATION_ADD_TAGS = "AddTags";
	private static final String API_OPERATION_REMOVE_TAGS = "RemoveTags";
	private static final String API_OPERATION_LIST_TAGS = "ListTags";
	private static final String API_OPERATION_RESET_TAGS = "ResetTags";
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
	
	
	/**
	 * Registers the device with the specified GCM registration token 
	 * for the application.
	 * 
	 * @param notificationToken	The GCM notification token.	
	 * @param callback	A callback to run when operation finishes.
	 */
	public void deviceRegistration(String notificationToken, ResponseCallback callback) {
		//Save the device GCM notification token.
		toucanClient.deviceNotificationToken = notificationToken;
		ToolBox.prefs_savePreference(toucanClient.context, PREF_NAME, PREF_KEY_DEVICE_NOT_TOKEN, String.class, notificationToken);
		
		if(callback!=null)
			callback.setCallbackOperation(ResponseCallback.CALLBACK_OPERATION_DEVICE_REGISTRATION);
		
		DeviceRegistrationRequest devRegRequest = generateDeviceRegistrationInfo(notificationToken);
		launchDeviceRegistrationRequest(devRegRequest, callback);		
	}
	
	/**
	 * Registers the device with the specified GCM registration token 
	 * for the application. We set also an external id for back-end
	 * purposes.
	 * 
	 * @param notificationToken	The GCM notification token.
	 * @param externalId	An external id that links GCM with some kind of internal back-end.
	 * @param callback	A callback to run when operation finishes.
	 */
	public void deviceRegistration(String notificationToken, int externalId, ResponseCallback callback) {
		//Save the device GCM notification token.
		toucanClient.deviceNotificationToken = notificationToken;
		ToolBox.prefs_savePreference(toucanClient.context, PREF_NAME, PREF_KEY_DEVICE_NOT_TOKEN, String.class, notificationToken);
		
		DeviceRegistrationRequest devRegRequest = generateDeviceRegistrationInfo(notificationToken);
		if(externalId>=1) {
			devRegRequest.getData().setExtId(externalId);
		
			String hashSignature = devRegRequest.getData().getSecurityHash(appPublicKey);
			devRegRequest.setHashSignature(hashSignature);
		}
			
		if(callback!=null)
			callback.setCallbackOperation(ResponseCallback.CALLBACK_OPERATION_DEVICE_REGISTRATION);
		
		launchDeviceRegistrationRequest(devRegRequest, callback);
	}
	
	/**
	 * Registers the device with the specified GCM registration token 
	 * for the application. When the installation URL has some referral 
	 * info, we send this info to the server to be saved.
	 * 
	 * @param notificationToken	The GCM notification token.
	 * @param installReferral	The installation referral data.
	 * @param callback	A callback to run when operation finishes.
	 */
	public void deviceRegistration(String notificationToken, String installReferral, ResponseCallback callback) {
		//Save the device GCM notification token.
		toucanClient.deviceNotificationToken = notificationToken;
		ToolBox.prefs_savePreference(toucanClient.context, PREF_NAME, PREF_KEY_DEVICE_NOT_TOKEN, String.class, notificationToken);
		
		DeviceRegistrationRequest devRegRequest = generateDeviceRegistrationInfo(notificationToken);
		if(installReferral!=null && installReferral.length()>0) {
			devRegRequest.getData().setInstallReferral(installReferral);
			
			String hashSignature = devRegRequest.getData().getSecurityHash(appPublicKey);
			devRegRequest.setHashSignature(hashSignature);
		}
		
		if(callback!=null)
			callback.setCallbackOperation(ResponseCallback.CALLBACK_OPERATION_DEVICE_REGISTRATION);
				
		launchDeviceRegistrationRequest(devRegRequest, callback);
	}
	
	/**
	 * Registers the device with the specified GCM registration token 
	 * for the application. We set also an external id for back-end
	 * purposes. When the installation URL has some referral 
	 * info, we send this info to the server to be saved.
	 * 
	 * @param notificationToken	The GCM notification token.
	 * @param externalId	An external id that links GCM with some kind of internal back-end.
	 * @param installReferral	The installation referral data.
	 * @param callback	A callback to run when operation finishes.
	 */
	public void deviceRegistration(String notificationToken, int externalId, String installReferral, ResponseCallback callback) {
		//Save the device GCM notification token.
		toucanClient.deviceNotificationToken = notificationToken;
		ToolBox.prefs_savePreference(toucanClient.context, PREF_NAME, PREF_KEY_DEVICE_NOT_TOKEN, String.class, notificationToken);
		
		DeviceRegistrationRequest devRegRequest = generateDeviceRegistrationInfo(notificationToken);
		if(installReferral!=null && installReferral.length()>0) {
			devRegRequest.getData().setInstallReferral(installReferral);
		}
		if(externalId>=1) {
			devRegRequest.getData().setExtId(externalId);		
		}
		
		String hashSignature = devRegRequest.getData().getSecurityHash(appPublicKey);
		devRegRequest.setHashSignature(hashSignature);
		
		if(callback!=null)
			callback.setCallbackOperation(ResponseCallback.CALLBACK_OPERATION_DEVICE_REGISTRATION);
		
		launchDeviceRegistrationRequest(devRegRequest, callback);
	}
	
	/**
	 * Informs to the API the installation referral.
	 * 
	 * @param installReferral	The installation referral data.
	 * @param callback	A callback to run when operation finishes.
	 */
	public void informInstallReferral(String installReferral, ResponseCallback callback) {
		
		if(isNotificationTokenPresent()) {
			DeviceRegistrationRequest devRegRequest = 
					generateDeviceRegistrationInfo(toucanClient.deviceNotificationToken);
			if(installReferral!=null && installReferral.length()>0) {
				devRegRequest.getData().setInstallReferral(installReferral);
				
				String hashSignature = devRegRequest.getData().getSecurityHash(appPublicKey);
				devRegRequest.setHashSignature(hashSignature);
			}
			
			if(callback!=null)
				callback.setCallbackOperation(ResponseCallback.CALLBACK_OPERATION_DEVICE_INFORMREFERRAL);
			
			launchInformReferralRequest(devRegRequest, callback);
		}else{
			Log.i(LOG_TAG, API_OPERATION_INFORM_REFERRAL.toUpperCase() + " Error. Notification token not stablished. Please, execute 'deviceRegistration()' first.");
		}
	}
	
	/**
	 * Informs to the API that a notification was received.
	 * 
	 * @param notificationBundle	Registers the device with the specified 
	 * 								GCM registration token for the application.
	 * @param callback	A callback to run when operation finishes.
	 */
	public void doReceivedACK(Bundle notificationBundle, ResponseCallback callback) {
		if(isNotificationTokenPresent()) {
			ACKRequest ackRequest = generateACKnfo(notificationBundle);
			if(callback!=null)
				callback.setCallbackOperation(ResponseCallback.CALLBACK_OPERATION_ACK_RECEIVED);
			
			if(ToolBox.net_isNetworkAvailable(context)){
				new ToucanPostWorker(context, apiToken, ackRequest, TOUCAN_WORKER_POST_DATA_TYPE.ACK, API_ENDPOINT_ACK_RECEIVED, API_OPERATION_ACK_RECEIVED, callback).start();				
			}else{
				cacheOperationRequest(new ToucanPostWorker(context, apiToken, ackRequest, TOUCAN_WORKER_POST_DATA_TYPE.ACK, API_ENDPOINT_ACK_RECEIVED, API_OPERATION_ACK_RECEIVED, callback), true);
			}
						
		}else{
			Log.i(LOG_TAG, API_OPERATION_ACK_RECEIVED.toUpperCase() + " Error. Notification token not stablished. Please, execute 'deviceRegistration()' first.");
		}
	}
	
	/**
	 * Informs to the API that a notification was read.
	 * 
	 * @param notificationBundle	Registers the device with the specified 
	 * 								GCM registration token for the application.
	 * @param callback	A callback to run when operation finishes.
	 */
	public void doReadACK(Bundle notificationBundle, ResponseCallback callback) {
		if(isNotificationTokenPresent()) {
			ACKRequest ackRequest = generateACKnfo(notificationBundle);
			if(callback!=null)
				callback.setCallbackOperation(ResponseCallback.CALLBACK_OPERATION_ACK_READ);
			
			if(ToolBox.net_isNetworkAvailable(context)){
				new ToucanPostWorker(context, apiToken, ackRequest, TOUCAN_WORKER_POST_DATA_TYPE.ACK, API_ENDPOINT_ACK_READ, API_OPERATION_ACK_READ, callback).start();				
			}else{
				cacheOperationRequest(new ToucanPostWorker(context, apiToken, ackRequest, TOUCAN_WORKER_POST_DATA_TYPE.ACK, API_ENDPOINT_ACK_READ, API_OPERATION_ACK_READ, callback), true);
			}
			
		}else{
			Log.i(LOG_TAG, API_OPERATION_ACK_READ.toUpperCase() + " Error. Notification token not stablished. Please, execute 'deviceRegistration()' first.");
		}
	}
	
	/**
	 * Allows adding tags for the application/deviceId.
	 * 
	 * @param tags	A list of tags.
	 * @param callback	A callback to run when operation finishes.
	 */
	public void doAddTags(List<String> tags,ResponseCallback callback) {
		//Prepare device registration request
		AppDevTagsOperationRequest tagAddRequest = new AppDevTagsOperationRequest();
		tagAddRequest.setAppKey(toucanClient.appPublicKey);
		tagAddRequest.setDevId(toucanClient.deviceUniqueId);
		tagAddRequest.setTags(tags);
				
		String appHashSignature = generateSHA1(appPublicKey + apiToken);
		tagAddRequest.setAppHashSignature(appHashSignature);
				
		String hashSignature = tagAddRequest.getSecurityHash(appPublicKey);
		tagAddRequest.setHashSignature(hashSignature);
		
		if(callback!=null)
			callback.setCallbackOperation(ResponseCallback.CALLBACK_OPERATION_ADD_TAGS);
				
		if(ToolBox.net_isNetworkAvailable(context)){
			new ToucanPostWorker(context, apiToken, tagAddRequest, TOUCAN_WORKER_POST_DATA_TYPE.TAGS, API_ENDPOINT_ADD_TAGS, API_OPERATION_ADD_TAGS, callback).start();
		}else{
			cacheOperationRequest(new ToucanPostWorker(context, apiToken, tagAddRequest, TOUCAN_WORKER_POST_DATA_TYPE.TAGS, API_ENDPOINT_ADD_TAGS, API_OPERATION_ADD_TAGS, callback), true);
		}
				
	}
	
	/**
	 * Deletes current application/DeviceId tags setting the specified
	 * tags.
	 * 
	 * @param tags		tags to add.
	 * @param callback	A callback to run when operation finishes.
	 */
	public void doResetTags(List<String> tags,ResponseCallback callback) {
		//Prepare device registration request
		AppDevTagsOperationRequest tagAddRequest = new AppDevTagsOperationRequest();
		tagAddRequest.setAppKey(toucanClient.appPublicKey);
		tagAddRequest.setDevId(toucanClient.deviceUniqueId);
		tagAddRequest.setTags(tags);
				
		String appHashSignature = generateSHA1(appPublicKey + apiToken);
		tagAddRequest.setAppHashSignature(appHashSignature);
				
		String hashSignature = tagAddRequest.getSecurityHash(appPublicKey);
		tagAddRequest.setHashSignature(hashSignature);
		
		if(callback!=null)
			callback.setCallbackOperation(ResponseCallback.CALLBACK_OPERATION_RESET_TAGS);
				
		if(ToolBox.net_isNetworkAvailable(context)){
			new ToucanPostWorker(context, apiToken, tagAddRequest, TOUCAN_WORKER_POST_DATA_TYPE.TAGS, API_ENDPOINT_RESET_TAGS, API_OPERATION_RESET_TAGS, callback).start();
		}else{
			cacheOperationRequest(new ToucanPostWorker(context, apiToken, tagAddRequest, TOUCAN_WORKER_POST_DATA_TYPE.TAGS, API_ENDPOINT_RESET_TAGS, API_OPERATION_RESET_TAGS, callback), true);
		}
				
	}
	
	/**
	 * Removes the specified tags for the application and deviceId.
	 * 
	 * @param tags		A list of tags to remove.
	 * @param callback	A callback to run when operation finishes.
	 */
	public void doRemoveTags(List<String> tags, ResponseCallback callback) {
		//Prepare device registration request
		AppDevTagsOperationRequest tagAddRequest = new AppDevTagsOperationRequest();
		tagAddRequest.setAppKey(toucanClient.appPublicKey);
		tagAddRequest.setDevId(toucanClient.deviceUniqueId);
		tagAddRequest.setTags(tags);
				
		String appHashSignature = generateSHA1(appPublicKey + apiToken);
		tagAddRequest.setAppHashSignature(appHashSignature);
				
		String hashSignature = tagAddRequest.getSecurityHash(appPublicKey);
		tagAddRequest.setHashSignature(hashSignature);
		
		if(callback!=null)
			callback.setCallbackOperation(ResponseCallback.CALLBACK_OPERATION_LIST_TAGS);
		
		if(ToolBox.net_isNetworkAvailable(context)){
			new ToucanPostWorker(context, apiToken, tagAddRequest, TOUCAN_WORKER_POST_DATA_TYPE.TAGS, API_ENDPOINT_REMOVE_TAGS, API_OPERATION_REMOVE_TAGS, callback).start();
		}else{
			cacheOperationRequest(new ToucanPostWorker(context, apiToken, tagAddRequest, TOUCAN_WORKER_POST_DATA_TYPE.TAGS, API_ENDPOINT_REMOVE_TAGS, API_OPERATION_REMOVE_TAGS, callback), true);
		}
		
	}
	
	/**
	 * Gets the list of tags for the application and device Id.
	 * 
	 * @param callback A callback to run when operation finishes.
	 */
	public void doListTags(ResponseCallback callback) {
		try{ 
			String appHashSignature = generateSHA1(appPublicKey + apiToken);
		
			String urlParams = "dUId=" + toucanClient.deviceUniqueId + "&appPubKey=" + appPublicKey + "&appHashSignature=" + appHashSignature;
			String encodedUrlParams = new String(Base64.encode(urlParams.getBytes(), 0), "UTF-8");
			String urlEncodedUrlParams = URLEncoder.encode(encodedUrlParams, "UTF-8");
		
			String finalUrl = API_ENDPOINT_LIST_TAGS + "=" + urlEncodedUrlParams;
		
			if(callback!=null)
				callback.setCallbackOperation(ResponseCallback.CALLBACK_OPERATION_LIST_TAGS);
			
			if(ToolBox.net_isNetworkAvailable(context)){
				new ToucanGetWorker(context, apiToken, finalUrl, API_OPERATION_LIST_TAGS, callback).start();
			}else{
				cacheOperationRequest(new ToucanGetWorker(context, apiToken, finalUrl, API_OPERATION_LIST_TAGS, callback), true);
			}
			
		}catch(Exception e){
			Log.e(LOG_TAG, "Error doing operation " + API_OPERATION_LIST_TAGS.toUpperCase() + " to Toucan API (" + e.getMessage() + ")", e);
		}
	}
	
	/**
	 * Un-registers a device from server. Avoiding delivering 
	 * notifications to it.
	 * 
	 * @param callback	A callback to run when operation finishes.
	 */
	public void doDeviceUnregister(ResponseCallback callback) {
		try{ 
			String appHashSignature = generateSHA1(appPublicKey + apiToken);
		
			String urlParams = "dUId=" + toucanClient.deviceUniqueId + "&appPubKey=" + appPublicKey + "&appHashSignature=" + appHashSignature;
			String encodedUrlParams = new String(Base64.encode(urlParams.getBytes(), 0), "UTF-8");
			String urlEncodedUrlParams = URLEncoder.encode(encodedUrlParams, "UTF-8");
		
			String finalUrl = API_ENDPOINT_UNREGISTRATION + "=" + urlEncodedUrlParams;
			
			if(ToolBox.net_isNetworkAvailable(context)){
				new ToucanGetWorker(context, apiToken, finalUrl, API_OPERATION_DEVICE_UNREGISTRATION, callback).start();
			}else{
				cacheOperationRequest(new ToucanGetWorker(context, apiToken, finalUrl, API_OPERATION_DEVICE_UNREGISTRATION, callback), true);
			}
			
		}catch(Exception e){
			Log.e(LOG_TAG, "Error doing operation " + API_OPERATION_DEVICE_UNREGISTRATION.toUpperCase() + " to Toucan API (" + e.getMessage() + ")", e);
		}
	}
	
	/**
	 * Enables a registered device.
	 * 
	 * @param callback	A callback to run when operation finishes.
	 */
	public void doEnableRegisteredDevice(ResponseCallback callback) {
		
		try{ 
			String appHashSignature = generateSHA1(appPublicKey + apiToken);
		
			String urlParams = "dUId=" + toucanClient.deviceUniqueId + "&appPubKey=" + appPublicKey + "&appHashSignature=" + appHashSignature;
			String encodedUrlParams = new String(Base64.encode(urlParams.getBytes(), 0), "UTF-8");
			String urlEncodedUrlParams = URLEncoder.encode(encodedUrlParams, "UTF-8");
		
			String finalUrl = API_ENDPOINT_ENABLE_REGISTERED_DEVICE + "=" + urlEncodedUrlParams;
			 
			if(ToolBox.net_isNetworkAvailable(context)){
				new ToucanGetWorker(context, apiToken, finalUrl, API_OPERATION_DEVICE_ENABLE, callback).start();
			}else{
				cacheOperationRequest(new ToucanGetWorker(context, apiToken, finalUrl, API_OPERATION_DEVICE_ENABLE, callback), true);
			}
			
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
	
	private void launchDeviceRegistrationRequest(DeviceRegistrationRequest devRegRequest, ResponseCallback callback) {
		if(ToolBox.net_isNetworkAvailable(context)){
			new ToucanPostWorker(context, apiToken, devRegRequest, TOUCAN_WORKER_POST_DATA_TYPE.REGISTRATION, API_ENDPOINT_REGISTRATION, API_OPERATION_DEVICE_REGISTRATION, callback).start();			
		}else{
			cacheOperationRequest(new ToucanPostWorker(context, apiToken, devRegRequest, TOUCAN_WORKER_POST_DATA_TYPE.REGISTRATION, API_ENDPOINT_REGISTRATION, API_OPERATION_DEVICE_REGISTRATION, callback), true);
		}
	}
	
	private void launchInformReferralRequest(DeviceRegistrationRequest devRegRequest, ResponseCallback callback) {
		if(ToolBox.net_isNetworkAvailable(context)){
			new ToucanPostWorker(context, apiToken, devRegRequest, TOUCAN_WORKER_POST_DATA_TYPE.REGISTRATION, API_ENDPOINT_REGISTRATION, API_OPERATION_INFORM_REFERRAL, callback).start();
		}else{
			cacheOperationRequest(new ToucanPostWorker(context, apiToken, devRegRequest, TOUCAN_WORKER_POST_DATA_TYPE.REGISTRATION, API_ENDPOINT_REGISTRATION, API_OPERATION_INFORM_REFERRAL, callback), true);
		}		
	}
		
	private static String generateSHA1(String data) {		
		return new String(Hex.encodeHex(DigestUtils.sha(data)));
	}

	/**
	 * Saves in disk an operation request to the API.
	 * 
	 * @param operation		Operation to save
	 * @param startPendingOperationsService	If TRUE, the pending operations service runs.
	 */
	private synchronized void cacheOperationRequest(ToucanWorker operation, boolean startPendingOperationsService) {
		try{
			String jsonData = GsonProcessor.getInstance().getGsonWithExposedFilter().toJson(operation);			
			String fileName = operation.getJobId();
			ToolBox.storage_storeDataInInternalStorage(context, fileName, jsonData.getBytes());
			Log.i(ToucanClient.LOG_TAG, "Saved pending operation request to disk (" + operation.getOperationName() + "/" + operation.getJobId() + ")");
			
			if(startPendingOperationsService) {
				//We start the service to process any pending operation
				Intent pendingOperationsServiceIntent = new Intent(context, PendingOperationsDeliveryService.class);
				context.startService(pendingOperationsServiceIntent);
			}
		}catch(Exception e) {
			Log.e(LOG_TAG, "Error cacheOperationRequest() - Operation request could not be cached [" + e.getMessage() + "].",e);
		}
	}
}
