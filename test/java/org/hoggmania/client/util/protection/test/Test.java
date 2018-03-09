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
package org.hoggmania.client.util.protection.test;

import java.util.Map;

import org.hoggmania.client.util.protection.*;
import org.hoggmania.client.util.protection.utils.ObfuscateSecretsHelper;

/**
 * Simple test class
 * 
 * @author jholland
 * 
 */
public class Test {

	public static void main(String[] args) {
		try {
			

			
			
			// Setup some options
			Map<String, String> map = null;
			boolean logging = true;
			String plain = "James jumped over the quick brown fox";
			String encryptedPropValue;

			//Test ObfuscatePlain passthrough
			map = new java.util.HashMap<String, String>();
			map.put(ObfuscatorInf.PWB_IMPLEMENTATION, ObfuscatePlain.class.getName());			
			encryptedPropValue = ObfuscateSecretsHelper.obfuscatedSecretAndWrite(plain, map);
			assert(encryptedPropValue != null) : "ObfuscatePlain failed to obfuscate text";
			if (logging) System.out.println("Text from EncryptedInfo is =" + encryptedPropValue);			
			assert(ObfuscateSecretsHelper.parseObfuscatedSecretAndDecrypt(encryptedPropValue) == plain) : "ObfuscatePlain does not return correct de-obfusacted value for encode version";
			if (logging) System.out.println("decrypted secret =" + ObfuscateSecretsHelper.parseObfuscatedSecretAndDecrypt(encryptedPropValue));
			assert(ObfuscateSecretsHelper.parseObfuscatedSecretAndDecrypt(plain) == plain) : "ObfuscatePlain does not return correct de-obfusacted value for non-encode (plain) version";
			if (logging) System.out.println("decrypted secret =" + ObfuscateSecretsHelper.parseObfuscatedSecretAndDecrypt(plain));

			//Test PassphraseObfuscatorURL with local entropy file and algorithm overrides
			map = new java.util.HashMap<String, String>();
			//map.put(ObfuscatorInf.PWB_ALGORITM_HASH, "SHA-512");
			//map.put(ObfuscatorInf.PWB_ALGORITM_PBE,"PBEWithSHA1AndDESede");
			//map.put(ObfuscatorInf.PWB_URL, System.getProperty("user.home")+"/callsign.entropy");
			map.put(ObfuscatorInf.PWB_IMPLEMENTATION, PassphraseObfuscatorURL.class.getName());

			encryptedPropValue = ObfuscateSecretsHelper.obfuscatedSecretAndWrite(plain, map);
			if (logging) System.out.println("Text from EncryptedInfo is =" + encryptedPropValue);	
			assert(encryptedPropValue != null) : "PassphraseObfuscatorURL with entropy file failed to obfuscate text";
			
			
			assert(ObfuscateSecretsHelper.parseObfuscatedSecretAndDecrypt(encryptedPropValue) == plain) : "PassphraseObfuscatorURL does not return correct de-obfusacted value for encode version";
			if (logging) System.out.println("decrypted secret =" + ObfuscateSecretsHelper.parseObfuscatedSecretAndDecrypt(encryptedPropValue));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
