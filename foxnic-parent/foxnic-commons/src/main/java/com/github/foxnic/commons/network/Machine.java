package com.github.foxnic.commons.network;

import com.github.foxnic.commons.encrypt.MD5Util;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;

 

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
		SystemInfo si = new SystemInfo();
		HardwareAbstractionLayer hal = si.getHardware();
		CentralProcessor processor=hal.getProcessor();
		
		String serial=StringUtil.join(new Object[] {
				processor.getProcessorIdentifier(),
				processor.getProcessorIdentifier().getProcessorID(),
				processor.getPhysicalPackageCount(),
				processor.getPhysicalProcessorCount(),
				processor.getLogicalProcessorCount()
				},"|");
 
		
//		List<NetIntf> macs=getMacAddressList();
//		String[] macAddrs=BeanUtil.getFieldValueArray(macs, "mac", String.class);
//		String[] names=BeanUtil.getFieldValueArray(macs, "name", String.class);
//		String serial=StringUtil.join(names,",")+"|"+StringUtil.join(macAddrs,",");
		serial=MD5Util.encrypt16(serial);
//		machineId=serial;
		
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
		List<InetAddress> list=getInet4AddressList();
		if(list.isEmpty()) return "127.0.0.1";
		ip=list.get(0).getHostAddress();
		return ip;
	}

	/*
   获取本机网内地址
    */
	public static List<InetAddress> getInet4AddressList() {
		List<InetAddress> list=new ArrayList<>();
		try {
			//获取所有网络接口
			Enumeration<NetworkInterface> allNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
			//遍历所有网络接口
			for (; allNetworkInterfaces.hasMoreElements(); ) {
				NetworkInterface networkInterface = allNetworkInterfaces.nextElement();
				//如果此网络接口为 回环接口 或者 虚拟接口(子接口) 或者 未启用 或者 描述中包含VM
				if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp() || networkInterface.getDisplayName().contains("VM")) {
					//继续下次循环
					continue;
				}
				//如果不是Intel与Realtek的网卡
//                if(!(networkInterface.getDisplayName().contains("Intel"))&&!(networkInterface.getDisplayName().contains("Realtek"))){
//                         //继续下次循环
//                            continue;
//                }
				//遍历此接口下的所有IP（因为包括子网掩码各种信息）
				for (Enumeration<InetAddress> inetAddressEnumeration = networkInterface.getInetAddresses(); inetAddressEnumeration.hasMoreElements(); ) {
					InetAddress inetAddress = inetAddressEnumeration.nextElement();
					//如果此IP不为空
					if (inetAddress != null) {
						//如果此IP为IPV4 则返回
						if (inetAddress instanceof Inet4Address) {
							list.add(inetAddress);
						}
                       /*
                      // -------这样判断IPV4更快----------
                       if(inetAddress.getAddress().length==4){
                           return inetAddress;
                       }

                        */

					}
				}


			}
			return list;

		} catch (SocketException e) {
			Logger.exception("获取网卡信息异常",e);
			return list;
		}
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
