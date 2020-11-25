package com.github.foxnic.commons.collection;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.github.foxnic.commons.bean.BeanUtil;

public class TreeUtilTest {
	
	public static class Tn{
		private String id;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getParentId() {
			return parentId;
		}
		public void setParent_id(String parentId) {
			this.parentId = parentId;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		private String parentId;
		private String name;
	}

	public List<Map> getTreeListData()
	{
		List<Map> nodes=new ArrayList<Map>();
		nodes.add(MapUtil.asMap("id","root","parentId",null,"name","ROOT"));
		
		//
		nodes.add(MapUtil.asMap("id","lv1.1","parentId","root","name","LV-1.1"));
		nodes.add(MapUtil.asMap("id","lv1.2","parentId","root","name","LV-1.2"));
		nodes.add(MapUtil.asMap("id","lv1.3","parentId","root","name","LV-1.3"));
		
		//
		nodes.add(MapUtil.asMap("id","lv1.1.1","parentId","lv1.1","name","LV-1.1.1"));
		nodes.add(MapUtil.asMap("id","lv1.1.2","parentId","lv1.1","name","LV-1.1.2"));
		nodes.add(MapUtil.asMap("id","lv1.1.3","parentId","lv1.1","name","LV-1.1.3"));
		
		//
		nodes.add(MapUtil.asMap("id","lv1.2.1","parentId","lv1.2","name","LV-1.2.1"));
		nodes.add(MapUtil.asMap("id","lv1.2.2","parentId","lv1.2","name","LV-1.2.2"));
		nodes.add(MapUtil.asMap("id","lv1.2.3","parentId","lv1.2","name","LV-1.2.3"));
		
		//
		nodes.add(MapUtil.asMap("id","lv1.3.1","parentId","lv1.3","name","LV-1.3.1"));
		nodes.add(MapUtil.asMap("id","lv1.3.2","parentId","lv1.3","name","LV-1.3.2"));
		nodes.add(MapUtil.asMap("id","lv1.3.3","parentId","lv1.3","name","LV-1.3.3"));
		
		
		return nodes;
	}
	
	
	
	@Test
	public void test_map()
	{
		List<Map> nodes=getTreeListData();
		
		Tree<Map> tree=new Tree<Map>(nodes, "id","parentId","name");
		
		
 
		assertTrue(tree.getRoot()!=null);
		assertTrue(tree.getRoot().getData().get("id").equals("root"));
		
		assertTrue(tree.getRoot().getPath().equals("root"));
		
		assertTrue(tree.getNode("lv1.3.3").getPath().equals("root/lv1.3/lv1.3.3"));
		
		assertTrue(tree.getNode("lv1.3.3").getLevel()==2);
		assertTrue(tree.getNode("lv1.3.3").isLeaf());
		
		String fn=tree.getNode("lv1.3.3").getFullName();
		assertTrue("ROOT / LV-1.3 / LV-1.3.3".equals(fn));
		fn=tree.getNode("lv1.3.3").getFullName(2);
		assertTrue("LV-1.3 / LV-1.3.3".equals(fn));
	 
		
		
		tree.print(true);
	}
	
	
	
	@Test
	public void test_bean()
	{
		List<Map> nodes=getTreeListData();
		List<Tn> beans=BeanUtil.toList(nodes, Tn.class);
		//
		Tree<Tn> tree=new Tree<Tn>(beans, "id","parentId","name");
		//
		assertTrue(tree.getRoot()!=null);
		assertTrue(tree.getRoot().getData().getId().equals("root"));
		
		assertTrue(tree.getRoot().getPath().equals("root"));
		
		assertTrue(tree.getNode("lv1.3.3").getPath().equals("root/lv1.3/lv1.3.3"));
		
		assertTrue(tree.getNode("lv1.3.3").getLevel()==2);
		assertTrue(tree.getNode("lv1.3.3").isLeaf());
		
		assertTrue(tree.getRoot().getDescendants().size()==12);
		
		Tree.Node<Tn> n=tree.getNode("lv1.3");
		Tree<Tn> t1_3=n.fork();
		assertTrue(t1_3.size()==4);
		
		
		tree.print(true);
		
		t1_3.print();
	}
	
}
