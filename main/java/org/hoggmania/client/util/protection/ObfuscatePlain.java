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
 * Used when no obfuscation is required but the process of obfuscation is used.
 * @author james.holland
 *  */
public class ObfuscatePlain implements ObfuscatorInf {


	public byte[] decrypt(EncryptedInfo encrypted_value) throws Exception {
		return encrypted_value.getBytes();
	}


	public EncryptedInfo encrypt(byte[] value) throws Exception {

		return new EncryptedInfo(value, ObfuscatePlain.class.getCanonicalName(), null);
	}

	public Map<String, String> getInitializeParameters() {
		return null;
	}


	public void initializeParameters(Map<String, String> map) throws Exception {
	}

}
