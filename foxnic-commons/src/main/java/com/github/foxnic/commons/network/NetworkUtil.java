package com.github.foxnic.commons.network;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;

/**
 * @author guyadong
 *
 */
public class NetworkUtil {
	
	private NetworkUtil() {} 
	
    private static enum Radix {
        /** 二进制 */BIN(2),
        /** 十进制 */DEC(10), 
        /** 十六进制 */HEX(16);
        final int value;
        Radix(int radix){
            this.value = radix;
        }
    }
    private static enum Filter implements Predicate<NetworkInterface>{
        /** 过滤器: 所有网卡 */ALL,
        /** 过滤器: 在线设备,see also {@link NetworkInterface#isUp()} */UP,
        /** 过滤器: 虚拟接口,see also {@link NetworkInterface#isVirtual()} */VIRTUAL,
        /** 过滤器:LOOPBACK, see also {@link NetworkInterface#isLoopback()} */LOOPBACK,
        /** 过滤器:物理网卡 */PHYICAL_ONLY;

        @Override
        public boolean apply(NetworkInterface input) {
            if(null == input ){
                return false;
            }
            try{
                byte[] hardwareAddress;
                switch(this){
                case UP:
                    return input.isUp();
                case VIRTUAL:
                    return input.isVirtual();
                case LOOPBACK:
                    return input.isLoopback();
                case PHYICAL_ONLY :
                    hardwareAddress = input.getHardwareAddress();
                    return null != hardwareAddress 
                            && hardwareAddress.length > 0 
                            && !input.isVirtual() 
                            && !isVMMac(hardwareAddress);
                case ALL:
                default :
                    return true;
                }
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        }
    }
    /**
     * 根据过滤器{@code filters}指定的条件(AND)返回网卡设备对象
     * @param filters
     * @return
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    private static Set<NetworkInterface> getNICs(Predicate<NetworkInterface> ...filters) {
        if(null == filters){
            filters = new Predicate[]{Filter.ALL};
        }
        try {
            Iterator<NetworkInterface> filtered = Iterators.filter(
                    Iterators.forEnumeration(NetworkInterface.getNetworkInterfaces()),
                    Predicates.and(filters));
            return ImmutableSet.copyOf(filtered);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } 
    }
    /**
     * 返回所有物理网卡
     * @return 物理网卡清单
     */
    public static Set<NetworkInterface> getPhysicalNICs() {
        return getNICs(Filter.PHYICAL_ONLY,Filter.UP);
    }
    /**
     * 将{@code byte[]} 转换为{@code radix}指定格式的字符串
     * 
     * @param source 
     * @param separator 分隔符
     * @param radix 进制基数
     * @return {@code source}为{@code null}时返回空字符串
     */
    private static final String format(byte[] source,String separator, final Radix radix) {
        if (null == source){
            return "";
        }
        if(null == separator){
            separator = "";
        }
        List<String> hex = Lists.transform(Bytes.asList(source),new Function<Byte,String>(){
            @Override
            public String apply(Byte input) {
                return String.copyValueOf(new char[]{
                        Character.forDigit((input & 240) >> 4, radix.value),
                        Character.forDigit(input & 15, radix.value)
                });
            }});
        return Joiner.on(separator).join(hex);
    }
    /** 
     * MAC地址格式(16进制)格式化{@code source}指定的字节数组 
     * @param source
     * @param separator
     */
    private static final String formatMac(byte[] source,String separator) {
        return format(source,separator,Radix.HEX);
    }
    /** 
     * 以IP地址格式(点分位)格式化{@code source}指定的字节数组<br>
     * @param source 
     */
    private static final String formatIp(byte[] source) {
        return format(source,".",Radix.DEC);
    }
    /**
     * 返回指定{@code address}绑定的网卡的物理地址(MAC)
     * @param address
     * @return 指定的{@code address}没有绑定在任何网卡上返回{@code null}
     */
    private static byte[] getMacAddress(InetAddress address) {
        try {
            NetworkInterface nic = NetworkInterface.getByInetAddress(address);
            return null == nic ? null  : nic.getHardwareAddress();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * @param nic 网卡对象
     * @param separator 格式化分隔符
     * @return 表示MAC地址的字符串
     */
    public static String getMacAddress(NetworkInterface nic,String separator) {
        try {
            return format(nic.getHardwareAddress(),separator, Radix.HEX);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 参见 {@link #getMacAddress(InetAddress)}
     * @param address  address
     * @param separator 格式化分隔符
     * @return 表示MAC地址的字符串
     */
    public static String getMacAddress(InetAddress address,String separator) {
        return format(getMacAddress(address),separator, Radix.HEX);     
    }
    private static byte invalidMacs[][] = {
    		// VMWare below
            {0x00, 0x05, 0x69},             
            {0x00, 0x1C, 0x14},  
            {0x00, 0x0C, 0x29},   
            {0x00, 0x50, 0x56},    
            // Virtualbox blow
            {0x08, 0x00, 0x27},             
            {0x0A, 0x00, 0x27},        
            // Virtual-PC
            {0x00, 0x03, (byte)0xFF},      
            // Hyper-V
            {0x00, 0x15, 0x5D}              
    };
    
    private static boolean isVMMac(byte[] mac) {
        if(null == mac) {
            return false;
        }

        for (byte[] invalid: invalidMacs){
            if (invalid[0] == mac[0] && invalid[1] == mac[1] && invalid[2] == mac[2]) {
                return true;
            }
        }

        return false;
    }
    
    
    private static void testPort(String host, int port) throws Exception {
        Socket s = new Socket();
        s.bind(new InetSocketAddress(host, port));
        s.close();
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
			isPortAvailable=NetworkUtil.isPortAvailable(port);
			checkedPort.add(port);
			i++;
		}
		return port;
    }
    
    
    

}