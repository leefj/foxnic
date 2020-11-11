package com.github.foxnic.commons.lang;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ArrayUtil_Normal {

	@Test
	public void test_toArray()
	{
		String x="a|b|c";
		String xx[]=x.split("\\|");
		
		List<Integer> list=new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			list.add(i);
		}
		
		Integer[] arr=ArrayUtil.toArray(list);
		assertTrue(arr[0]==0);
		assertTrue(arr[5]==5);
		
	}
	
	@Test
	public void test_toArray_performance()
	{
		List<Integer> list=new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			list.add(i);
		}
		int size=100000;
		long t0=System.currentTimeMillis();
		for (int i = 0; i <size; i++) {
			Integer[] arr=list.toArray(new Integer[list.size()]);
		}
		long t1=System.currentTimeMillis();
		for (int i = 0; i <size; i++) {
			Integer[] arr=ArrayUtil.toArray(list);
		}
		long t2=System.currentTimeMillis();
		 
		System.out.println(t1-t0);
		System.out.println(t2-t1);
	}
	
	@Test
	public void test_A()
	{
		Integer[] a1= {2,5,7,8};
		Integer[] a2= {2,5,7,8};
		
		Integer[] a3=ArrayUtil.merge(a1,a2);
		
		assertTrue(a3[4]==2); 
		assertTrue(a3[7]==8); 
		
		Integer[] a4=ArrayUtil.subArray(a3, 2, 5);
		assertTrue(a4.length==4); 
		assertTrue(a4[0]==7); 
		assertTrue(a4[1]==8); 
		assertTrue(a4[2]==2); 
		assertTrue(a4[3]==5); 
		
		Integer[] a5=ArrayUtil.unshift(a1, 1,3);
		assertTrue(a5.length==6); 
		assertTrue(a5[0]==1); 
		assertTrue(a5[1]==3); 
		
		Integer[] a6=ArrayUtil.append(a1, 1,3,67);
		assertTrue(a6.length==7); 
		assertTrue(a6[4]==1); 
		assertTrue(a6[5]==3); 
		assertTrue(a6[6]==67); 
		
		List<Integer> list=new ArrayList<Integer>();
		list=ArrayUtil.addToList(list, a1,a2);
		assertTrue(list.size()==8); 
		assertTrue(list.get(0)==2);
		assertTrue(list.get(1)==5); 
		assertTrue(list.get(2)==7); 
		assertTrue(list.get(3)==8); 
		
		assertTrue(list.get(4)==2);
		assertTrue(list.get(5)==5); 
		assertTrue(list.get(6)==7); 
		assertTrue(list.get(7)==8); 
		 
	}
	
	@Test
	public void test_B()
	{
		Integer[] a1= {9,5,2,7};
		Integer[] a2= {9,5,2,7,4,2};
		
		assertTrue(ArrayUtil.contains(a1, 2)); 
		assertTrue(ArrayUtil.indexOf(a1, 2,0)==2); 
		assertTrue(ArrayUtil.indexOf(a2, 2,3)==5); 
		
	}
	
	@Test
	public void test_C()
	{
		String[] a1= {"good","Feel","Good"};

		assertTrue(ArrayUtil.contains(a1, "2")==false); 
		assertTrue(ArrayUtil.contains(a1, "good",false)); 
		assertTrue(ArrayUtil.contains(a1, "feel",true)); 
		
	}
	
	@Test
	public void test_D()
	{
		String[] a1= {"4","7","1.8"};
		
		Double[] dd=ArrayUtil.castArrayType(a1, Double.class); 
		assertTrue(dd!=null && dd.length==3);
		
		assertTrue(dd[0]==4);
		assertTrue(dd[1]==7);
		assertTrue(dd[2]==1.8);
		 
		
	}
	
}
