package com.github.foxnic.commons.network;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MachineTest {

	@Test
	public void testname() throws Exception {
		String ip = Machine.getIp();
		String hostName =  Machine.getHostName();
		String mac=Machine.getMacAddress();
		String id=Machine.getIdentity();
		
		assertTrue(id!=null && id.length()==16);
		assertTrue(mac!=null && mac.contains("-"));
		
		System.out.println(ip);
		System.out.println(hostName);
		System.out.println(id);
	}

}
