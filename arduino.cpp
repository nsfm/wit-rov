#include <SPI.h>
#include <Ethernet.h>

#define DEBUG 0 // debug true or false
#define OPBUF 8 // the command buffer size
#define BUILD 2 // the build number

// Networking Configuration
byte mac[] = { 0x00, 0xde, 0xad, 0xfa, 0xca, 0xde }; // MAC must be unique on this network
IPAddress ip(192,168,0,2);                           // IP of this device
IPAddress gateway(192,168,0,1);                      // IP of the connected computer
EthernetServer server(23);                           // Telnet is on port 23, convenient

void setup() {
  Ethernet.begin(mac, ip, gateway);
  server.begin();
  Serial.begin(115200);
}

void loop() {
  // wait for a new client:
  EthernetClient client = server.available();
  if (client) {
    
    // prepare to accept a command
    char a;
    char op[OPBUF] = {0};
    int i = 0;
    
    // keep reading/waiting until the command is complete
    while (true) {
      // read the bytes incoming from the client:
      if (client.available() > 0) {
        a = client.read();
        Serial.write(a);
        
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
      case 'd':
        // get the pin they want to set
        server.write("!");
        break;
      case 'v':
        // return build number
        server.print(BUILD);
        break;
      case 'q':
        // disconnect this client gracefully
        server.write("goodbye");
        client.flush();
        client.stop();
        break;
      case '\r':
        server.write("windows...?");
        break;
      case '0':
        server.write("...");
        break; 
      default:
        server.write("?");
    }
    
    // end our response (windows requires \r because why not?)
    server.write('\r');
    server.write('\n');
    
  }
}


