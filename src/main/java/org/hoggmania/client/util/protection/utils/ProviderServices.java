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

import java.io.PrintStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.crypto.Cipher;

/**
 * helper class to manage getting required algo from JCE as they very per RE
 * 
 * @author james.holland
 *
 */
public class ProviderServices {

	public static Object[] getProviderServices(String providerName, String factoryName, PrintStream out){
		Provider provider = Security.getProvider(providerName);
		if (out!= null)out.println("Provider is : " + provider.getName());
	    Iterator<Object>  it = provider.keySet().iterator();
	    ArrayList<String> list = new ArrayList<String>();
	    while (it.hasNext())
	    {
	        String	entry = (String)it.next();
	        
	        // this indicates the entry refers to another entry
	        
	        if (entry.startsWith("Alg.Alias."))
	        {
	            entry = entry.substring("Alg.Alias.".length());
	        }
	        
	        String  factoryClass = entry.substring(0, entry.indexOf('.'));
	        String  name = entry.substring(factoryClass.length() + 1);
	        //System.out.println(providerName+":"+factoryClass+":"+name);
	        if (factoryName != null) {	        	
	        	if (factoryClass.toLowerCase().indexOf(factoryName)!= -1) {
	        			list.add(name);
	        			if (out!= null)out.println("factoryClass="+factoryClass+" name ="+name); 
	        	}	        
	        } else {
	        	list.add(name);
	        	if (out!= null)out.println("factoryClass="+factoryClass+" name ="+name);
	        }
	    }
		return list.toArray();
	}

	public static boolean verifyEncrypt (KeyPair keyPair, String CLIENT_CRP, String CLIENT_PROVIDER, String SERVER_CRP, String SERVER_PROVIDER) {
		try {
	        byte[] message = "1233412347854571".getBytes();
			
	        // encryption step - client side
			Cipher cipher_bc = Cipher.getInstance(CLIENT_CRP, CLIENT_PROVIDER); 
	        cipher_bc.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate());
	        byte[] cipherText = cipher_bc.doFinal(message);
	        
	        
	       
	        // decryption step- server side
	        Cipher cipher_ibm = Cipher.getInstance(SERVER_CRP, SERVER_PROVIDER); 
	        cipher_ibm.init(Cipher.DECRYPT_MODE, keyPair.getPublic());	
	        byte[] plainText = cipher_ibm.doFinal(cipherText);
	        
	        return Arrays.equals(message, plainText);
		} catch (Exception e) {
		    System.out.println("verifyEncrypt error "+e.getMessage()+" [CLIENT_CRP="+CLIENT_CRP+"] and [SERVER_CRP="+SERVER_CRP+"]");
		}
		return false;
	
	}

	/**
	 * @param keyPair
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	public static boolean verifySignature(KeyPair keyPair, String CLIENT_SIG, String CLIENT_PROVIDER, String SERVER_SIG, String SERVER_PROVIDER) {
		try {
			Signature           signatureClient = Signature.getInstance(CLIENT_SIG, CLIENT_PROVIDER);
			Signature           signatureServer = Signature.getInstance(SERVER_SIG, SERVER_PROVIDER);
			// generate a signature
			signatureClient.initSign(keyPair.getPrivate());
	
			byte[] message = "001233412347854571".getBytes();
	
			signatureClient.update(message);
	
			byte[]  sigBytes = signatureClient.sign();
			
			
			// verify a signature
			signatureServer.initVerify(keyPair.getPublic());
	
			signatureServer.update(message);
	
		    return signatureServer.verify(sigBytes);
			
		} catch (Exception e) {
		    System.out.println("Signature verification error "+e.getMessage()+" [CLIENT="+CLIENT_SIG+"] and [SERVER="+SERVER_SIG+"]");
		}
		return false;
	}

	/**
	 * 
	 */
	public static Provider[] printProviders(PrintStream out) {
		Provider[] providers = Security.getProviders();
		for (int i = 0; i < providers.length; i++) {
			out.println("Provider name is :"+providers[i].getName());
			out.println("Provider info is :"+providers[i].getInfo());
		}
		return providers;
	}

}
