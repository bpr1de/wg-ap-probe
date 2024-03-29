/*
 * Copyright (c) 2023, Bryan Phillippe <bp@darkforest.org>
 * May be used under the terms of the BSD license.
 */

package org.darkforest.wgwifi;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * A definition of the description metadata for WatchGuard wireless access
 * points. This information will be returned by the access point when it is
 * queried.
 */
public class APInfo {
  // The primary IP Address the access point was reached on.
  private InetAddress address;

  // The port number the access point was queried on.
  private short port;

  // The friendly-name assigned to the access point.
  private String name;

  // The model of the access point.
  private String model;

  // The primary LAN port MAC address of the access point.
  private String mac;

  // The GUID serial number of the access point.
  private String serial;

  // The firmware version of the access point.
  private String version;

  // How long the access point has been powered on.
  private String uptime;

  public APInfo(int address, short port, String sysInfo) {
    byte[] bytes = BigInteger.valueOf(address).toByteArray();
    try {
      this.address = InetAddress.getByAddress(bytes);
    } catch (UnknownHostException e) {
      // Ignore invalid IP addresses
    }
    this.port = port;

    String[] lines = sysInfo.split("\\n");
    switch (lines.length) {
    case 0:
      break;

    default:
      if (!lines[10].trim().isEmpty()) {
        model = lines[10];
      }

    case 10: // Unused - local controller IP address
    case 9:  // Unused - configuration revision
    case 8:  // Unused - cloud vs. local operating mode
    case 7:  // Unused
    case 6:  // AP operating system runtime (uptime)
      uptime = lines[5];

    case 5:  // AP firmware version
      version = lines[4];

    case 4:  // AP serial number
      serial = lines[3];

    case 3:  // AP primary MAC address
      mac = lines[2];

    case 2:  // AP base model (could be extended later)
      if (model == null) {
        model = lines[1];
      }

    case 1:  // AP Name
      name = lines[0];
    }
  }

  public String getAddress() {
    return address.getHostAddress();
  }

  public String getName() {
    return name;
  }

  public String getModel() {
    return model;
  }

  public String getMac() {
    return mac;
  }

  public String getSerial() {
    return serial;
  }

  public String getVersion() {
    return version;
  }

  /**
   * Convert the standard Linux uptime value into a human-readable format.
   *
   * @return a string representation of the access point runtime.
   */
  public String getUptime() {
    long remainder = Double.valueOf(uptime).longValue();

    long days = TimeUnit.SECONDS.toDays(remainder);
    remainder -= TimeUnit.DAYS.toSeconds(days);

    long hours = TimeUnit.SECONDS.toHours(remainder);
    remainder -= TimeUnit.HOURS.toSeconds(hours);

    long minutes = TimeUnit.SECONDS.toMinutes(remainder);
    remainder -= TimeUnit.MINUTES.toSeconds(minutes);

    long seconds = TimeUnit.SECONDS.toSeconds(remainder);

    return days + "d, " + hours + ":" + minutes + "+" + seconds + "s";
  }

  @Override
  public String toString() {
    return getName() +
        " (" + getModel() + " " + getSerial() + ")" +
        " up: " + getUptime() +
        " @" + getAddress();
  }
}
