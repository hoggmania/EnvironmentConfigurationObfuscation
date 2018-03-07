# EnvironmentConfigurationObfuscation
A pluggable mechanism to (de)obfuscate configuration secrets in a lights out management environment.

Can use static machine entropy (mac addresses etc) or a remote file in a shared/clustered model. Does not require the use of a HSM or keystore, instead it recreates the entropy to decrypt the secret.

Extensions to this could include: 
1. DP-API for Windows machines (but not in a VM scenario)
2. Keychain for Mac OS
3. External HSM 


Example usage

```java
String plain = "my string";

//Test PassphraseObfuscatorURL with local entropy file and algorithm overrides
Map<String, String> map = new java.util.HashMap<String, String>();
map.put(ObfuscatorInf.PWB_ALGORITM_HASH, "SHA-512");
map.put(ObfuscatorInf.PWB_ALGORITM_PBE,"PBEWithSHA1AndDESede");
map.put(ObfuscatorInf.PWB_URL, System.getProperty("user.home")+"/hoggmania.entropy");
map.put(ObfuscatorInf.PWB_IMPLEMENTATION, PassphraseObfuscatorURL.class.getName());

//To obfuscate the plain
String encryptedPropValue = ObfuscateSecretsHelper.obfuscatedSecretAndWrite(plain, map);

//To de-obfuscate 
String plain-back = ObfuscateSecretsHelper.parseObfuscatedSecretAndDecrypt(encryptedPropValue));
```
