package com.github.foxnic.commons.network;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;

import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.encrypt.MD5Util;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;

 

/**
 * @author fangjieli 
 * 获得服务器相关信息
 * */
public class Machine {
 
	/**
	 * 获得主机名
	 * @return 主机名
	 * */
	public static String getHostName() {
		if(hostName!=null) {
			return hostName;
		} 
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			hostName = addr.getHostName().toString();
		} catch (UnknownHostException e) {
			Logger.error(e);
		}
		return hostName;
	}
	
	/**
	 * 获得主机ID
	 * @return 主机唯一标识
	 * */
	public static String getIdentity() {
		if(machineId!=null) {
			return machineId;
		} 
		List<NetIntf> macs=getMacAddressList();
		String[] macAddrs=BeanUtil.getFieldValueArray(macs, "mac", String.class);
		String[] names=BeanUtil.getFieldValueArray(macs, "name", String.class);
		String serial=StringUtil.join(names,",")+"|"+StringUtil.join(macAddrs,",");
		serial=MD5Util.encrypt16(serial);
		machineId=serial;
		return serial;
	}
	
	/**
	 * 获得主机Mac地址
	 * @return mac地址
	 * */
	public static String getMacAddress() {
		if(macAddress!=null) {
			return macAddress;
		} 
		List<NetIntf> macs=getMacAddressList();
		if(macs==null || macs.size()==0) return null;
		macAddress=macs.get(0).mac;
		return macAddress;
	}
	
	/**
	 * 获得主机ip地址
	 * @return ip地址
	 * */
	public static String getIp() {
		if(ip!=null) {
			return ip;
		} 
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			ip = addr.getHostAddress();
		} catch (UnknownHostException e) {
			Logger.error(e);
		}
		return ip;
	}
	
	private static String machineId=null;
	private static String hostName=null;
	private static String macAddress=null;
	private static String ip=null;
	
	 
	
 
	public static List<NetIntf> getMacAddressList() {
		List<NetIntf> netIntfs=new ArrayList	<NetIntf>();
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            byte[] mac = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
               
                if (netInterface.isLoopback() || netInterface.isVirtual() || netInterface.isPointToPoint() || !netInterface.isUp()) {
                    continue;
                } else {
                	if(netInterface.equals("Hyper-V Virtual Ethernet Adapter")) {
                		continue;
                	}
                	NetIntf ni=new NetIntf();
                    mac = netInterface.getHardwareAddress();
                    if (mac != null) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < mac.length; i++) {
                            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                        }
                        if (sb.length() > 0) {
                            //return sb.toString();
                        	//macs.put(netInterface.getDisplayName(),sb.toString());
                        	ni.name=netInterface.getName();
                        	ni.displayName=netInterface.getDisplayName();
                        	ni.mac=sb.toString();
                        	Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                        	while (addresses.hasMoreElements()) {
                                InetAddress addr = addresses.nextElement();
                                if (addr instanceof Inet4Address) {
                                    ni.ipV4s.add(addr.getHostAddress());
                                } else if (addr instanceof Inet6Address) {
                                	ni.ipV4s.add(addr.getHostAddress());
                                }
                            }
                        	netIntfs.add(ni);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.error("MAC地址获取失败", e);
        }
        return netIntfs;
    }
	
	 /**
     * 本机端口是否可用
     * */
    public static boolean isPortAvailable(int port) {
        try {
        	testPort("0.0.0.0", port);
        	testPort(InetAddress.getLocalHost().getHostAddress(), port);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获得指定范围内的有效端口
     * */
    public static int getAvailablePort(int min,int max) {
    	int port=-1;
		boolean isPortAvailable=false;
		int i=0;
		HashSet<Integer> checkedPort=new HashSet<Integer>();
		while(!isPortAvailable) {
			if(i>(max-min)*1.5) {
				throw new RuntimeException("尝试次数过多");
			}
			port=(int)(Math.random()*(max-min));
			port=min+port;
			if(checkedPort.contains(port)) continue;
			isPortAvailable=isPortAvailable(port);
			checkedPort.add(port);
			i++;
		}
		return port;
    }
    
    private static void testPort(String host, int port) throws Exception {
        Socket s = new Socket();
        s.bind(new InetSocketAddress(host, port));
        s.close();
    }
	
	private static class NetIntf {
		private String name;
		private String displayName;
		private String mac;
		private List<String> ipV4s=new ArrayList<>();
		private List<String> ipV6s=new ArrayList<>();
	}
    
	
}
