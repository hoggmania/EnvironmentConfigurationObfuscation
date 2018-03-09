/*
*******************************************************************************
*   Environment Configuration Obfuscation
*   (c) 2018 James Holland
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*******************************************************************************
*/
package org.hoggmania.client.util.protection;

import java.util.Map;

/**
 * Definition of a password encryption interface
 * 
 * @author james.holland
 * 
 */
public interface ObfuscatorInf {

	public static final String	PWB_ALGORITM_PBE					= "PWB_ALGORITM_PBE";
	public static final String	PWB_ALGORITM_HASH				= "PWB_ALGORITM_HASH";
	public static final String	PWB_PROVIDER						= "PWB_PROVIDER";
	public static final String	PWB_IMPLEMENTATION				= "PWB_IMPLEMENTATION";
	public static final String	PWB_OVERRIDE_SALT				= "PWB_OVERRIDE_SALT";
	public static final String	PWB_OVERRIDE_NETWORK_ENTROPY		= "PWB_OVERRIDE_NETWORK_ENTROPY";
	public static final String	PWB_URL							= "PWB_URL";
	public static final String	PWB_OVERRIDE_ITERATION			= "PWB_OVERRIDE_ITERATION";	
	public static final String	PWB_OVERRIDE_USERNAME			= "PWB_OVERRIDE_USERNAME";
	public static final String	PWB_OVERRIDE_USERHOME			= "PWB_OVERRIDE_USERHOME";
	public static final String	PWB_DO_NOT_GENERATE_ENTROPY		= "PWB_DO_NOT_GENERATE_ENTROPY";
	
	
	/**
	 * Method of passing parameters required by the implementation
	 * 
	 * @param map
	 * @throws Throwable
	 */
	void initializeParameters(Map<String, String> map) throws Exception;

	/**
	 * Encrypts a secret with machine generated information
	 * 
	 * @param value
	 * @return encrypted with PBE and Base64 encoded value
	 */
	EncryptedInfo encrypt(final byte[] value) throws Exception;

	/**
	 * Decrypt a secret
	 * 
	 * @param encrypted_value
	 *            EncryptedInfo with value in Base64 encoding
	 * @return byte[] plain text (no encoding)
	 */
	byte[] decrypt(final EncryptedInfo encrypted_value) throws Exception;

	
	/**
	 * Returns the Initialisation Parameters used.
	 * @return
	 */
	public Map<String, String> getInitializeParameters();

}
