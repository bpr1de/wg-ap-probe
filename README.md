# WatchGuard Access Point Probe

A simple Java utility for probing and displaying information about any
WatchGuard-brand wireless access points connected to the local network.

## Usage

Build the package with `mvn package` and then run the main class with a 
single argument referring to the network interface name to send the 
discovery broadcast probes on.

For example: `java -jar target/wg-ap-probe-1.0-SNAPSHOT.jar en0`

If there are compatible access points on the network connected via `en0`, they 
will return system information similar to the below:

```
Broadcasting on '192.168.111.255'...
Sitting Room 420 (AP420 42AP027473C11) up: 0d, 19:11+32s @192.168.111.14
Office 420 (AP420 42AP027205E53) up: 0d, 19:5+32s @192.168.111.15
Family Room 325 (AP325 35AP027432977) up: 0d, 19:1+41s @192.168.111.16
Patio 322 (AP322 33AP0293837D5) up: 0d, 18:59+53s @192.168.111.13
Game Room 320 (AP320 32AP02AAAA071) up: 0d, 19:9+55s @192.168.111.11
Driveway 322 (AP322 33AP027460037) up: 0d, 19:7+54s @192.168.111.17
Bedroom AP320 (AP320 32AP053FDFC12) up: 0d, 18:57+54s @192.168.111.10
Deck 322 (AP322 33AP0286B4406) up: 0d, 19:3+54s @192.168.111.12
```

## License

Copyright © 2023 Bryan Phillippe, <bp@darkforest.org>

This program and the accompanying materials are made available under the
BSD license. https://opensource.org/licenses/BSD-3-Clause
