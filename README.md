##Control Software for WIT ROV for MATE 2015

Description to come soon


###Command Dictionary:
Commands are sent in up to six byte (char) groups, delimited by the newline char ('\n').
The first byte represents a function, the remaining are arguments. Pad single digit integers with leading 0s if the specific command expects multiple digits. If you send more than 6 bytes without a newline, you will be disconnected.

The microcontroller will not check for errors before using your input. Check your own data before you send.
```
Digital Write:
dXXY\n
XX = pin, ie 03 or 42
Y = value, 1 for HIGH, else LOW
return: nothing useful

Analog Write:
aXXYYY\n
XX = pin, ie 03 or 42
YYY = value, ie 000 - 255
return: nothing useful

Pin Mode:
pXXY\n
XX = pin, ie 03 or 42
Y = value, 0 for INPUT, 1 for OUTPUT, 2 for INPUT_PULLUP
return: nothing useful

Digital Read:
rXX\n
XX = pin, ie 03 or 42
return: not sure

Analog Read:
hXX\n
XX = pin, ie 03 or 42
return: '0' to '1023'

Attach Thruster
sXXY\n
XX = pin, ie 03 or 42
Y = id, 0-7
return: nothing useful

Set Thruster
tXYYY\n
X = id, 0-7
YYY = value, 000-800
return: nothing useful
Note: a speed of 000 is full reverse; 800 is full forward. 400 is stop.
```
###Laptop Setup
The UI currently only runs on a Windows Computer but is very simply to run.  First you need to connect the arduino to the computer via Ethernet and have the arduino's IP set to 192.168.0.2 and the computers IP set to 192.168.0.1. (Currently being set in arduino.cpp so this code just needs to be uploaded to the arduino) Then it is as simple as running the wit_rov.exe file.  Let it run for a few seconds if you don't have the proper configuration and it cannot connect to the arduino it will take a few seconds and then diplay a connection error.  

```
Current Functionality:
allows for you to enter any 6 bit opcode and send it to the arduino.
```