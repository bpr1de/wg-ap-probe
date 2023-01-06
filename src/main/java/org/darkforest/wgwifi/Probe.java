/*
 * Copyright (c) 2023, Bryan Phillippe (bp@darkforest.org)
 * May be used under the terms of the BSD license.
 */

package org.darkforest.wgwifi;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Probe {
  /*
   * These magic values are defined in the vendor APIs for querying access
   * point system information using a broadcast discovery mechanism.
   */
  private static final int SEND_PORT = 2528;
  private static final int RECEIVE_PORT = 2529;
  private static final int PROBE_TYPE_GWC = 0x57475743;
  private static final int PROBE_CODE_DISCOVERY = 0x44495343;
  private static final int RESPONSE_TYPE_AP = 0x57474150;
  private static final int RESPONSE_CODE_IP4 = 0x49503440;

  /**
   * Returns all the broadcast addresses for the given network interface by
   * name.
   *
   * @param name the network interface to get broadcast addresses for.
   *
   * @return a list of broadcast addresses.
   */
  public List<InetAddress> getBroadcastAddressesFor(String name) {
    List<InetAddress> broadcastList = new ArrayList<>();
    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

      while (interfaces.hasMoreElements()) {
        NetworkInterface networkInterface = interfaces.nextElement();

        // Weed out interfaces that we can't (or won't) discover on.
        if (networkInterface.isLoopback() ||
            !networkInterface.isUp() ||
            !networkInterface.getDisplayName().equals(name)) {
          continue;
        }

        networkInterface.getInterfaceAddresses().stream()
            .map(InterfaceAddress::getBroadcast)
            .filter(Objects::nonNull)
            .forEach(broadcastList::add);
      }
    } catch (SocketException e) {
      System.out.println("socket exception: " + e);
    }

    return broadcastList;
  }

  /**
   * Parse the system information blob returned by the access point in
   * response to a probe request.
   *
   * @param data the raw blob of system information returned from the access
   * point.
   *
   * @param length the length (in bytes) of the system information blob.
   *
   * @return the APInfo parsed from the system information blob.
   */
  private APInfo parseInfo(byte[] data, int length) {
    // Discard invalid system information lengths.
    if (length <= 16 || length > data.length) {
      return null;
    }

    ByteBuffer receiveBuffer = ByteBuffer.wrap(data, 0, 16);

    // Discard unrecognized or unsupported system information types.

    if (RESPONSE_TYPE_AP != receiveBuffer.getInt()) {
      return null;
    }

    if (RESPONSE_CODE_IP4 != receiveBuffer.getInt()) {
      return null;
    }

    int address = receiveBuffer.getInt();
    short port = receiveBuffer.getShort();
    // 2byte reserved

    String sysinfo = new String(data, 16, length - 16,
        StandardCharsets.UTF_8);
    return new APInfo(address, port, sysinfo);
  }

  /**
   * Sends out a discovery datagram on the given broadcast address.
   *
   * @param address the address to send a broadcast discovery datagram on.
   */
  public void probeTo(InetAddress address) {
    try {
      DatagramSocket socket = new DatagramSocket(new InetSocketAddress(RECEIVE_PORT));
      APInfo apInfo;

      socket.setBroadcast(true);
      socket.setSoTimeout((int)TimeUnit.SECONDS.toMillis(1));

      ByteBuffer sendBuffer = ByteBuffer.allocate(8);
      sendBuffer.putInt(PROBE_TYPE_GWC);
      sendBuffer.putInt(PROBE_CODE_DISCOVERY);
      byte[] message = sendBuffer.array();

      socket.send(new DatagramPacket(message, message.length, address, SEND_PORT));

      try {
        while (true) {
          byte[] receiveBuffer = new byte[1024];
          DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
          socket.receive(packet);
          apInfo = parseInfo(packet.getData(), packet.getLength());
          System.out.println(apInfo);
        }
      } catch (SocketTimeoutException e) {
        // Maximum wait time for responses expired.
      }
      socket.close();
    } catch (IOException e) {
      System.out.println("IO exception: " + e);
    }
  }

  /**
   * Probe for WatchGuard access points on the network attached to the given
   * interface.
   *
   * @param args command-line arguments, containing the (required) interface
   * to issue the probe on.
   */
  static public void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Must supply an interface name.");
      System.exit(-1);
    }

    String name = args[0];
    Probe probe = new Probe();

    List<InetAddress> broadcastList = probe.getBroadcastAddressesFor(name);
    if (broadcastList.size() == 0) {
      System.out.println("No broadcast addresses for interface '" + name + "'");
      System.exit(-1);
    }

    for (InetAddress address: broadcastList) {
      System.out.println("Broadcasting on '" + address.getHostAddress() + "'...");
      probe.probeTo(address);
    }
  }
}