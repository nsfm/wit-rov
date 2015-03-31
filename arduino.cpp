#include <SPI.h>
#include <Ethernet.h>
#include <Servo.h>

#define DEBUG 0    // debug true or false
#define OPBUF 6    // the command buffer size
#define BUILD 2    // the build number
#define TIMER 3000 // ticks until timeout
#define SERVO 6    // number of thrusters (servos) attached to machine

// Networking Configuration
byte mac[] = { 0x00, 0xde, 0xad, 0xfa, 0xca, 0xde }; // MAC must be unique on this network
IPAddress ip(192,168,0,2);                           // IP of this device
IPAddress gateway(192,168,0,1);                      // IP of the connected computer
EthernetServer server(23);                           // Telnet is on port 23, convenient

// Get thrusters ready ahead of time
Servo thruster[SERVO];

void setup() {
  Ethernet.begin(mac, ip, gateway);
  server.begin();
  #if DEBUG
    Serial.begin(115200);
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
      
      // if we go so many ticks without hearing anything, time out the client
      timer--;
      if (timer == 0) {
        server.write("timeout\r\n");
        client.flush();
        client.stop(); 
        break;
      }
      
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
          server.write("please no\r\n");
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
        // get the pin they want to set
        int pin = ((op[1]-'0')*10)+(op[2]-'0');
        
        if (op[3] == '1') {
          digitalWrite(pin,HIGH)
        } else {
          digitalWrite(pin,LOW)
        }
        
        server.write("!");
        break;
        
      // analog write
      case 'a':
        // get the pin they want to set
        int pin = ((op[1]-'0')*10)+(op[2]-'0');
        int val = ((op[3]-'0')*100)+((op[4]-'0')*10)+(op[5]-'0');
        
        analogWrite(pin,val)

        server.write("!");
        break;
        
      // pin mode
      case 'p':
        int pin = ((op[1]-'0')*10)+(op[2]-'0');
        
        switch(op[3]) {
          case '0':
            pinMode(pin, INPUT);
            break;
          case '1':
            pinMode(pin, OUTPUT);
            break;
          case '2':
            pinMode(pin, INPUT_PULLUP);
            break
          default:
            server.write("?");
        }
        
        server.write("!");
        break;
      
      // analog read
      case 'h':
        int pin = ((op[1]-'0')*10)+(op[2]-'0');
        server.print(analogRead(pin));
        break;
        
      // digital read
      case 'r':
        int pin = ((op[1]-'0')*10)+(op[2]-'0');
        server.print(digitalRead(pin));
        break;
        
      // attach thruster
      case 's':
        int pin = ((op[1]-'0')*10)+(op[2]-'0');
        int id = op[3]-'0';
        
        thruster[id].attach(pin)
        
        server.write("!");
        break;
        
      // set thruster
      case 't':
        int id = op[1]-'0';
        int val = ((op[2]-'0')*100)+((op[3]-'0')*10)+(op[4]-'0');
        
        thruster[id].writeMicroseconds(1100+val);
        
        server.write("!");
        break;
      
      // version
      case 'v':
        // return build number
        server.print(BUILD);
        break;
      
      // end session
      case 'q':
        // disconnect this client gracefully
        server.write("goodbye!");
        client.flush();
        client.stop();
        break;
        
      case '\r':
        server.write("windows?");
        break;
        
      default:
        server.write("?");
    }
    
    // end our response (windows requires \r because why not?)
    server.write('\r');
    server.write('\n');
    
  }
  
  // this part of the loop is active only when no clients are connected
  // what should we do while we're idling and waiting for commands?
}

