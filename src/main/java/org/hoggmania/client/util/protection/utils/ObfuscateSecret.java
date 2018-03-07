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
package org.hoggmania.client.util.protection.utils;

/**
 * Utility class to obfuscate a secret
 * 
 * @author james.holland
 * 
 */
public class ObfuscateSecret {

	/**
	 * This utility is to obfuscate a secret. <br>
	 * System options (-D) available are: - <li>PWB_ALGORITM_PBE</li> <li>
	 * PWB_ALGORITM_HASH</li> <li>PWB_PROVIDER</li> <li>PWB_IMPLEMENTATION</li> <br>
	 * Warning, these options must be used when decrypting the value as well.
	 * Example: -<BR>
	 * <code>java -DPWB_PROVIDER=BC com.acti.crypto.generator.utils.ObfuscateSecret password01</code>
	 * 
	 * @param args
	 *            password to encrypt
	 */
	public static void main(String[] args) {
		try {
			boolean obfuscate = true;
			if (args.length == 2) {
				obfuscate = Boolean.parseBoolean(args[1]);
			} else if (args.length == 1) {
				obfuscate = true;
			} else {
				usage();
				System.exit(0);
			}
			
			
			if (obfuscate) {
				System.out.println("Obfuscated password: " + ObfuscateSecretsHelper.obfuscatedSecretAndWrite(args[0], null));	
			} else {
				System.out.println("De-obfuscated password: " + ObfuscateSecretsHelper.parseObfuscatedSecretAndDecrypt(args[0]));
			}
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void usage() {
		System.out
				.println("Usage <password> <true/false true=obfuscate(default) false=de-ofuscate> \n System options (-D) are <PWB_ALGORITM_PBE> <PWB_ALGORITM_HASH> <PWB_PROVIDER> <PWB_IMPLEMENTATION>");
	}

}
