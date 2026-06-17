@echo off
set "JAVA_HOME=C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\jbr"
set "MVN=C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd"
"%MVN%" clean compile exec:java
pause
