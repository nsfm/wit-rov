###Control Software for WIT ROV for MATE 2015

Description to come soon


##Opcode Dictionary:

Commands are sent in up to six byte (char) groups, delimited by the newline char ('\n').
The first byte represents a function, the remaining are arguments. Pad single digit integers with leading 0s if the specific command expects multiple digits. If you send more than 6 bytes, you will be disconnected.
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
sXXY
XX = pin, ie 03 or 42
Y = id, 0-7
return: nothing useful

Set Thruster
tXYYY
X = id, 0-7
YYY = value, 000-800
return: nothing useful
Note: a speed of 000 is full reverse; 800 is full forward. 400 is stop.
```
