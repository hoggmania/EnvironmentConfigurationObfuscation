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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class retrieves the Network entropy for the local machine. It will
 * concatenate all NIC mac addresses into one string.
 * 
 * @author james.holland
 * 
 */
public class FindNetworkEntropy {

	private static final String	MAC_DELIMITER	= " ";
	public static final String	MAC_MATCHER		= "[0-9a-fA-F]{2}[-:][0-9a-fA-F]{2}[-:][0-9a-fA-F]{2}[-:][0-9a-fA-F]{2}[-:][0 -9a-fA-F]{2}[-:][0-9a-fA-F]{2}";

	/**
	 * Retrieves all NIC mac addresses.
	 * 
	 * @return
	 */
	public final static String[] getAllMacAddresses() {
		return getMacAddresses().split(MAC_DELIMITER);
	}

	/**
	 * Concatenates all NIC mac addresses into one string.
	 * 
	 * @return
	 */
	public final static String getNetworkEntropy() {
		return getMacAddresses().replace(MAC_DELIMITER, "");
	}

	/**
	 * Retrieves all NIC mac addresses in a space charater delimited string
	 * 
	 * @return
	 */
	private final static String getMacAddresses() {
		String mac = "";
		String os = System.getProperty("os.name");

		try {
			if (os.startsWith("Windows")) {
				mac = parseNetworkEntropy(runIpCommand("ipconfig /all"), ":");
			} else if (os.startsWith("Mac OS X")) {
				mac = parseNetworkEntropy(runIpCommand("ifconfig"), "ether");
			} else {
				mac = parseNetworkEntropy(runIpCommand("ifconfig"), "HWaddr");
			}
		} catch (Exception ex) {
			ex.printStackTrace();

		}
		return mac;
	}

	/**
	 * Get the NIC mac addresses
	 * 
	 * @return
	 */
	public final static String getLocalhostMacAddress() {
		String mac = "";
		String os = System.getProperty("os.name");

		try {
			if (os.startsWith("Windows")) {
				mac = parseMacAddress(runIpCommand("ipconfig /all"), ":");
			} else if (os.startsWith("Mac OS X")) {
				mac = parseMacAddress(runIpCommand("ifconfig"), "ether");
			} else {
				mac = parseMacAddress(runIpCommand("ifconfig"), "HWaddr");
			}
		} catch (Exception ex) {
			ex.printStackTrace();

		}
		return mac;
	}

	private final static String parseNetworkEntropy(String ipConfigResponse, String macSearch) throws ParseException {
		StringTokenizer tokenizer = new StringTokenizer(ipConfigResponse, "\n");
		String lastMacAddress = "";

		while (tokenizer.hasMoreTokens()) {
			String line = tokenizer.nextToken().trim();

			// see if line contains MAC address
			int macAddressPosition = line.indexOf(macSearch);
			if (macAddressPosition < 0)
				continue;

			String macAddressCandidate = line.substring(macAddressPosition + macSearch.length()).trim();
			if (isMacAddress(macAddressCandidate)) {
				lastMacAddress = lastMacAddress + MAC_DELIMITER + macAddressCandidate;
				continue;
			}
		}
		return lastMacAddress.trim();
	}

	private final static String parseMacAddress(String ipConfigResponse, String macSearch) throws ParseException {
		String localHost = null;
		try {
			localHost = InetAddress.getLocalHost().getHostAddress();
		} catch (java.net.UnknownHostException ex) {
			ex.printStackTrace();
			throw new ParseException(ex.getMessage(), 0);
		}

		StringTokenizer tokenizer = new StringTokenizer(ipConfigResponse, "\n");
		String lastMacAddress = null;

		while (tokenizer.hasMoreTokens()) {
			String line = tokenizer.nextToken().trim();

			// see if line contains IP address
			if (line.endsWith(localHost) && lastMacAddress != null) {
				return lastMacAddress;
			}

			// see if line contains MAC address
			int macAddressPosition = line.indexOf(macSearch);
			if (macAddressPosition <= 0)
				continue;

			String macAddressCandidate = line.substring(macAddressPosition + macSearch.length()).trim();
			if (isMacAddress(macAddressCandidate)) {
				lastMacAddress = macAddressCandidate;
				continue;
			}
		}

		ParseException ex = new ParseException("cannot read MAC address from [" + ipConfigResponse + "]", 0);
		ex.printStackTrace();
		throw ex;
	}

	private final static boolean isMacAddress(String macAddressCandidate) {
		Pattern macPattern = Pattern.compile(MAC_MATCHER);
		Matcher m = macPattern.matcher(macAddressCandidate);
		return m.matches();
	}

	private final static String runIpCommand(String command) throws IOException {
		Process p = Runtime.getRuntime().exec(command);
		InputStream stdoutStream = new BufferedInputStream(p.getInputStream());
		StringBuffer buffer = new StringBuffer();
		for (;;) {
			int c = stdoutStream.read();
			if (c == -1)
				break;
			buffer.append((char) c);
		}
		String outputText = buffer.toString();
		stdoutStream.close();
		return outputText;
	}

	/*
	 * Main
	 */
	public final static void main(String[] args) {
		try {
			System.out.println("Network info:");

			System.out.println("  Operating System: " + System.getProperty("os.name"));
			System.out.println("  IP/Localhost: " + InetAddress.getLocalHost().getHostAddress());

			String[] mac = getAllMacAddresses();
			for (int i = 0; i < mac.length; i++) {
				System.out.println("  MAC Address[" + i + "]: " + mac[i]);
			}

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
