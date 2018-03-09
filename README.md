# EnvironmentConfigurationObfuscation
A pluggable mechanism to (de)obfuscate configuration secrets in a lights out management environment. 

<WARNING> 
This is not encryption, it is obfuscation only. An attacker with full access to the hoist machine could de-obfuscate the secret. If you need full FIPS-like secret protection , buy a HSM! (even then how will you protected the HSM pin!? For LOM it's a Catch 22 situation). There are solution (very expensive >$50k) to this problem, but for simple LOM obfuscation is the really the only thing you can offer. Its security theatre at best.   
  
 Can't say you haven't been warned. 
</WARNING>
  
Can use static machine entropy (mac addresses etc) or a remote file in a shared/clustered model. Does not require the use of a HSM or keystore, instead it recreates the entropy to decrypt the secret.

Extensions to this could include: 
1. DP-API for Windows machines (but not in a VM scenario)
2. Keychain for Mac OS
3. External HSM 


Example usage to obfuscate

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
```

Example usage to obfuscate
```java
//To de-obfuscate 
String plain-back = ObfuscateSecretsHelper.parseObfuscatedSecretAndDecrypt(encryptedPropValue));
```

# How soon will my ticket be fixed?
The best way to have a bug fixed or feature request implemented is to to fork the repository and send a pull request. If the pull request is reasonable it has a good chance of making it into the next release. If you build the release yourself, even more chance!

If you don't fix the bug yourself, the bug might never get fixed. If it is a serious bug, other people than you might care enough to provide a fix.

In other words, there is no guarantee that a bug or feature request gets fixed. Tickets that are more than 6 months old are likely to be closed to keep the backlog manageable.
