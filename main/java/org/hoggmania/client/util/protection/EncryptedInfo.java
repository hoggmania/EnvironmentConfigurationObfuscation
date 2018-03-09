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

import java.util.Iterator;
import java.util.Map;

import com.migcomponents.migbase64.Base64;

/**
 * Container class for the encryption definition
 * 
 * @author james.holland
 * 
 */
public class EncryptedInfo {

	private byte[]	bytes;
	private String  code;
	private Map<String, String> parameters;

	/**
	 * This constructor takes the encrypted bytes and a keyAlias as parameters.
	 * This constructor is used to pass to or from the WebSphere Application
	 * Server runtime to enable the runtime to associate the bytes with a
	 * specific key that is used to encrypt the bytes.
	 * 
	 * @param bytes must be non-Base64 encode
	 * @param code of the implementation
	 * @param parameters
	 */
	public EncryptedInfo(byte[] bytes, String code, Map<String, String> parameters) {
		this.bytes = bytes;
		this.code = code;
		this.parameters = parameters;
	}

	/**
	 * This command returns the encrypted bytes.
	 * 
	 * @return byte[]
	 */
	public byte[] getBytes() {
		return bytes;
	}
	
	/**
	 * @return the code
	 */
	public String getCode() {
		return this.code;
	}

	/**
	 * This command returns the parameters
	 * 
	 * @return Map<String, String>
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	public String toString() {
		String parms = " parameters are [";
		if (!(parameters == null || parameters.isEmpty())) {
			for (Iterator<String> iterator = parameters.keySet().iterator(); iterator.hasNext();) {
				String key = iterator.next();
				parms = parms.concat(key+"="+parameters.get(key)+", ");	
			}
			//Remove the end comma
			parms = parms.substring(0, parms.length()-2)+"]";
		}
		return this.getClass().getCanonicalName() + " code=" + getCode() + " " + " encrypted=" + Base64.encodeToString(getBytes(), true)+parms;
	}

}
