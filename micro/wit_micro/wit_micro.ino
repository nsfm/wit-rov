/*
WIT MATE ROV - Embedded code for microcontrollers the WIT ROV
Copyright (C) 2015 Nathaniel Dube

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

#include <SPI.h>
#include <Ethernet.h>
#include <Servo.h>
#include <Wire.h>
// Device Libraries
#include "MS5803_I2C.h" // Depth Sensor
#include "MPU6050.h"
#include "SFE_HMC6343.h"

#define DEBUG 0         // debug true or false
#define OPBUF 8         // the command buffer size
#define TIMER 3000      // ticks until timeout
#define SERVO 6         // number of thrusters (servos) attached to machine
#define STEPR 3         // number of stepper motors attached to machine


//Camera switcher 1 47,45,43
//Camera Switcher 2 41,39,37
//Stepper pins 46-44 42-40 38-36

// Networking Configuration
byte mac[] = { 0x00, 0xde, 0xad, 0xfa, 0xca, 0xde }; // MAC must be unique on this network
IPAddress ip(192,168,0,2);                           // IP of this device
IPAddress gateway(192,168,0,1);                      // IP of the connected computer
EthernetServer server(23);                           // Telnet is on port 23, convenient

// Get thrusters ready ahead of time
Servo thruster[SERVO];

// Prepare our stepper motors
int dirPin[STEPR];
int stpPin[STEPR];

// Initialize the depth sensor
MS5803 depth(ADDRESS_HIGH); // alt 0x77
SFE_HMC6343 compass; // Declare the sensor object
MPU6050 nineAxisSensor;

void setup() {
  Ethernet.begin(mac, ip, gateway);
  server.begin();
  #if DEBUG
    Serial.begin(115200);
  #endif

  // Start depth sensor
  depth.reset();
  depth.begin();
  
  nineAxisSensor.initialize();
  #if DEBUG
  if(!compass.init())
  {
    Serial.println("Sensor Init Failed");
  }
  #endif
}

void loop() {
  // wait for a new client:
  EthernetClient client = server.available();
  if (client) {
    
    // prepare to accept a command
    char a;
    char op[OPBUF] = {0};
    int i = 0;
    int timer = TIMER;
    
    // keep reading/waiting until the command is complete
    while (true) {
      compass.readHeading();
      // if we go so many ticks without hearing anything, time out the client
      //timer--;
      //if (timer == 0) {
      //  server.write("timeout!");
      //  server.write('\n');
      //  client.flush();
      //  client.stop();
      //  break;
      //}
      
      // read the bytes incoming from the client:
      if (client.available() > 0) {
        timer = TIMER; // reset the timer
        
        a = client.read();
        #if DEBUG
          Serial.write(a);
        #endif
        // commands are newline delimited
        if (a == '\n') {
          break;
        }
        
        // if we have room in our buffer, keep accepting bytes
        if (i < OPBUF) {
          op[i] = a;
          i++;
        } else {
          // if you overflow the command buffer, you are disconnected
          server.write("overflow!");
          server.write('\n');
          client.flush();
          client.stop();
          break;
        }
      }      
    } 
      
    // interpret the command sent
    switch(op[0]) {
      // digital write
      case 'd':
        if (op[3] == '1') {
          digitalWrite(char2int(op[1],op[2]),HIGH);
        } else {
          digitalWrite(char2int(op[1],op[2]),LOW);
        }
        server.write("!");
        break;
        
      // analog write
      case 'a':        
        analogWrite(char2int(op[1],op[2]),char2int(op[3],op[4],op[5]));
        server.write("!");
        break;
        
      // pin mode
      case 'p':
        switch(op[3]) {
          case '0':
            pinMode(char2int(op[1],op[2]), INPUT);
            break;
          case '1':
            pinMode(char2int(op[1],op[2]), OUTPUT);
            break;
          case '2':
            pinMode(char2int(op[1],op[2]), INPUT_PULLUP);
            break;
          default:
            server.write("?");
        }
        server.write("!");
        break;
      
      // analog read
      case 'h':
        server.write(analogRead(char2int(op[1],op[2])));
        server.write("!");
        break;
        
      // digital read
      case 'r':
        server.write(digitalRead(char2int(op[1],op[2])));
        server.write("!");
        break;
        
      // attach thruster
      case 's':
        thruster[char2int(op[3])].attach(char2int(op[1],op[2]));
        server.write("!");
        break;
        
      // set thruster
      case 't':
        thruster[char2int(op[1])].writeMicroseconds(1100+char2int(op[2],op[3],op[4]));
        server.write("!");
        break;
      
      // attach stepper
      case 'g':
        dirPin[char2int(op[5])] = char2int(op[1],op[2]);
        stpPin[char2int(op[5])] = char2int(op[3],op[4]);
        pinMode(char2int(op[1],op[2]), OUTPUT);
        pinMode(char2int(op[3],op[4]), OUTPUT);
        server.write("!");
        break;

      // actuate a stepper
      case 'x':
        {
          digitalWrite(dirPin[char2int(op[1])],char2int(op[2]));
          int c = char2int(op[3],op[4])*10;
          for (int i=0;i<c;i++) {
            digitalWrite(stpPin[char2int(op[1])], HIGH);
            delayMicroseconds(800);
            digitalWrite(stpPin[char2int(op[1])], LOW);
            delayMicroseconds(800);
          }
        }
        server.write("!");
        break;

      // read pressure
      case 'u':
        server.write(depth.getPressure(ADC_2048));
        server.write("!");
        break;
      
      // read temperature (external)
      case 'i':
        server.write(depth.getTemperature(CELSIUS, ADC_512));
        server.write("!");
        break;

      // get compass data
      case 'c':
        switch(op[1])
        {     
          case 'h':
            server.write((float)compass.heading/10.0); //heading in degrees
            break;
          case 'p':
            server.write((float)compass.pitch/10.0); //pitch in degrees
            break;
          case 'r':
            server.write((float)compass.roll/10.0); //roll in degrees
            break;
        }
        server.write("!");
        break;

      // end session
      case 'q':
        server.write("goodbye!");
        server.write('\n');
        client.flush();
        client.stop();
        break;
      //exxyy
      //x -> device number
      //y -> bytes to read
      case 'e':
        {
          int bytes = char2int(op[3], op[4]);
          Wire.requestFrom(char2int(op[1],op[2]), bytes);
          while(Wire.available())
          {
            char c = Wire.read();
            server.write(c);
          }
          server.write("!");
        }
        break;
      default:
        server.write("?");
    }
    
    // end our response
    server.write('\n');
    
  }
  // TODO: Idle block 
  // this part of the loop is active only when no clients are connected
  // what should we do while we're idling and waiting for commands?
  // TODO: Thruster comms timeout
}

// convert a few characters to ints
int char2int(char val) {
  return (val-'0');
}
int char2int(char val, char val2) {
  return ((val-'0')*10)+(val2-'0');
}
int char2int(char val, char val2, char val3) {
  return ((val-'0')*100)+((val2-'0')*10)+(val3-'0');
}

