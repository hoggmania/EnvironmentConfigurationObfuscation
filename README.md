# EnvironmentConfigurationObfuscation
A pluggable mechanism to (de)obfuscate configuration secrets in a lights out management environment.

Can use static machine entropy (mac addresses etc) or a remote file in a shared/clustered model. Does not require the use of a HSM or keystore, instead it recreates the entropy to decrypt the secret.

Extensions to this could include: -
--DP-API for Windows machines (but not in a VM scenario)
--Keychain for Mac OS
--External HSM 