# ObjectStream Blocker for Minecraft 1.7.10

This very simple mod-patcher prevents the exploitation of vulnerabilities in MC mods using `ObjectInputStream` aka IOS (_deserialization of the object_).

> It's not a fix. This is a security tool: it disables potentially unsafe code, giving you more time to understand and fix the vulnerability in the mod code.

## How it works?

The mod scans each class for usages of `ObjectInputStream` and makes an automatic replacement with a stub class. When cases of using OIS are detected, a message is displayed in the log: `SECURITY ALERT Detected usage of ObjectInputStream`, so that
you know which game modifications have security issues and need to be fixed.

By default, the mod makes substitutions wherever it finds unwanted code. You can add exceptions in the configuration file to allow the use of **OIS**, where it is needed.
