package com.github.foxnic.commons.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.bean.BeanUtil;
import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.lang.StringUtil;

/**
 * 针对树形结构数据的处理
 * 解析到多棵树时会存在多个root 
 * */
public class Tree<T> {

	public static class Node<T> {
		
		private Tree<T> tree=null;
		private Object id=null;
		private Object parentId=null;
		private T data=null;
		private Set<Node<T>> chindren;
		
		/**
		 * 获得节点所在的树
		 * @return 树
		 * */
		public Tree<T>  getTree() {
			return tree;
		}
		
		/**
		 * 以当前节点为根节点，分叉出一新的棵树
		 * @return 树
		 * */
		public Tree<T> fork()
		{
			List<Node<T>> nodes=this.getDescendants();
			nodes.add(this);
			List<T> datas=(List<T>)BeanUtil.getFieldValueList(nodes,"data",this.getData().getClass());
			return new Tree<T>(datas,tree.getIdProperty(),tree.getParentIdProperty(),tree.getNameProperty());
		}
		
		private boolean isChildrenModifiable=true;
		
		/**
		 * 获得直接下级节点
		 * @return 节点清单
		 * */
		public Set<Node<T>> getChindren() {
			if(chindren==null) {
				return null;
			}
			if(isChildrenModifiable) {
				chindren=Collections.unmodifiableSet(chindren);
				isChildrenModifiable=false;
			}
			return chindren; 
		}
		
		
		private void addChild(Node<T> node)
		{
			if(chindren==null) {
				chindren=new HashSet<Tree.Node<T>>();
			}
			chindren.add(node);
		}
		
		/**
		 * 获得节点ID
		 * @return 节点ID
		 * */
		public Object getId() {
			return id;
		}
		
		/**
		 * 获得上级节点ID
		 * @return 上级节点ID
		 * */
		public Object getParentId() {
			return parentId;
		}
		
		/**
		 * 得到上级节点
		 * @return Node 上级节点
		 * */
		public Node<T> getParent() {
			return tree.getNode(this.getParentId());
		}
		
		/**
		 * 获得节点数据
		 * @return 数据
		 * */
		public T getData() {
			return data;
		}
		
		/**
		 * 获得所有的子孙节点
		 * @return 子孙节点清单
		 * */
		public List<Node<T>> getDescendants()
		{
			List<Node<T>> descendants=new ArrayList<Tree.Node<T>>();
			for (Node<T> node : tree.nodes) {
				if(StringUtil.noContent(node.getPath())) {
					continue;
				}
				if(node.getLevel()>this.getLevel() && node.getPath().startsWith(this.getPath())) {
					descendants.add(node);
				}
			}
			return descendants;
		}
		
		/**
		 * 获得所有祖先节点，顺序自下而上
		 * @return 节点清单
		 * */
		public List<Node<T>> getAncestors() {
			return getAncestors(false);
		}
		
		/**
		 * 获得所有祖先节点，顺序自下而上
		 * @param includeSelf  是否将自身作为第一个放入清单
		 * @return 节点清单
		 * */
		public List<Node<T>> getAncestors(boolean includeSelf) {
			List<Node<T>> ancestors=new ArrayList<Tree.Node<T>>();
			Node<T> parent=this;
			if(includeSelf) {
				ancestors.add(this);
			}
			while(true) {
				parent=parent.getParent();
				if(parent==null) {
					break;
				}
				ancestors.add(parent);
			}
			return ancestors;
		}
		
		/**
		 * 获得完整路径
		 * @param limit 层级限制，-1 表示全部
		 * @return 完整的路径名称
		 * */
		public String getFullName(int limit) {
			return getFullName(limit," / ");
		}
		
		/**
		 * 获得完整路径
		 * @param sep 分隔符
		 * @return 完整的路径名称
		 * */
		public String getFullName(String sep) {
			return getFullName(-1,sep);
		}
		
		/**
		 * 获得完整路径
		 * @return 完整的路径名称
		 * */
		public String getFullName() {
			return getFullName(-1," / ");
		}
		
		/**
		 * 获得完整路径
		 * @param limit 层级限制，-1 表示全部
		 * @param sep 分隔符
		 * @return 完整的路径名称
		 * */
		public String getFullName(int limit,String sep) {
			List<Node<T>> ans=this.getAncestors(true);
			int start=ans.size()-1;
			if(limit>0) {
				start=ans.size()-limit;
			}
			if(start<0) {
				start=0;
			}
			String[] arr=new String[start+1];
			for (int i = start; i >=0; i--) {
				arr[start-i]=ans.get(i).getName();
			}
			return StringUtil.join(arr, sep);
		}
		
		/**
		 * 获得名称
		 * @return 节点名称
		 * */
		public String getName() {
			Object name=this.tree.getPropertyValue(data, tree.nameProperty);
			return name==null?null:name.toString();
		}

		//
		Node(Tree<T> tree,Object id,Object parentId,T data) {
			this.tree=tree;
			this.id=id;
			this.parentId=parentId;
			this.data=data;
		}
		
		private String path=null;
		private int level=0;
		
		/**
		 * 获得路径
		 * @return 路径默认用斜杠隔开
		 * */
		public String getPath() {
			return path;
		}
		
		/**
		 * 获得层级
		 * @return 层级
		 * */
		public int getLevel() {
			return level;
		}
		
		private boolean isLeaf=false;

		/**
		 * 是否叶子节点
		 * @return 逻辑值
		 * */
		public boolean isLeaf() {
			return isLeaf;
		}
		
		/**
		 * 转JSONObject，不包括下级节点
		 * @return JSONObject
		 * */
		public JSONObject toJSONObject()
		{
			JSONObject json=new JSONObject();
			json.put("data",this.getData());
			json.put("id",this.getId());
			json.put("parentId",this.getParentId());
			json.put("isLeaf",this.isLeaf());
			json.put("path",this.getPath());
			json.put("name",this.getName());
			return json;
		}
		
		@Override
		public String toString() {
			return toJSONObject().toJSONString();
		}
 
	}
	
	private List<Node<T>> nodes;
	private Map<Object,Node<T>> nodeMap;
	private Node<T> root;
	private List<Node<T>> roots;
	
	/**
	 * 
	 * 获得根节点,第一个根节点
	 * @return 根节点
	 * */
	public Node<T> getRoot() {
		return root;
	}
	
	/**
	 * 根节点数量
	 * @return 根节点数量
	 * */
	public int getRootCount()
	{
		return this.roots.size();
	}
	
	/**
	 * 
	 * 获得根节点
	 * @param i 解析到多棵树的情况下，使用序号获得根节点
	 * @return 根节点
	 * */
	public Node<T> getRoot(int i) {
		return roots.get(i);
	}

	private boolean isMap=false;
     
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private  Map<String,T> toMap(List<T> nodes,String idProperty)
	{
		if(isMap) {
			return (Map<String,T>)MapUtil.toMap((List<Map>)nodes,idProperty);
		}
		return BeanUtil.toMap(nodes, idProperty);
	}
	
	@SuppressWarnings("rawtypes")
	private Object getPropertyValue(T data,String property)
	{
		if(isMap) {
			return ((Map)data).get(property);
		}
		return BeanUtil.getFieldValue(data, property);
	}
	
	private List<T> orignalNodes;
	
	/**
	 * 获得原始节点数据
	 * @return 原始节点数据
	 * */
	public List<T> getOrignalNodes() {
		return orignalNodes;
	}

	/**
	 * 获得节点ID属性名称
	 * @return ID属性名称
	 * */
	public String getIdProperty() {
		return idProperty;
	}

	/**
	 * 获得节点Name属性名称
	 * @return Name属性名称
	 * */
	public String getNameProperty() {
		return nameProperty;
	}

	private String idProperty;
	private String parentIdProperty;
	
	/**
	 * 获得节点上级ID属性名称
	 * @return 上级ID属性名称
	 * */
	public String getParentIdProperty() {
		return parentIdProperty;
	}

	private String nameProperty;
	
	/**
	 * @param nodes 节点数据
	 * @param idProperty ID属性
	 * @param parentIdProperty 上级ID属性
	 * @param nameProperty 名称属性
	 * */
	public Tree(List<T> nodes, String idProperty, String parentIdProperty,String nameProperty) {

		this.orignalNodes=nodes;
		this.idProperty=idProperty;
		this.parentIdProperty=parentIdProperty;
		this.nameProperty=nameProperty;
		
		if (nodes == null) {
			return;
		}
		if (nodes.size() == 0) {
			return;
		}
		Object firstNode = nodes.get(0);
		if (firstNode == null) {
			throw new IllegalArgumentException("节点元素不允许为 null");
		}

		this.isMap = firstNode instanceof Map;

		this.nodes = new ArrayList<Node<T>>(nodes.size());
		this.nodeMap=new HashMap<Object, Tree.Node<T>>(nodes.size());
		this.roots = new ArrayList<Tree.Node<T>>();

		Map<String, T> mnodes = toMap(nodes, idProperty);

		for (T node : nodes) {
			Object id=getPropertyValue(node, idProperty);
			// 对象化节点
			Node<T> n = new Node<T>(this,id , getPropertyValue(node, parentIdProperty),
					node);
			this.nodes.add(n);
			this.nodeMap.put(id,n);

			T parent = mnodes.get(getPropertyValue(node, parentIdProperty));

			if (parent == null) {
				if (this.root == null) {
					this.root = n;
				}
				this.roots.add(n);
			}
		}

		if (this.root == null) {
			throw new IllegalArgumentException("未发现根点");
		}
		
		buildHierarchical();

	}
	
	public Node<T> getNode(Object id) {
		return this.nodeMap.get(id);
	}
 
	/**
	 * 构建层级关系
	 * */
	private void buildHierarchical() {
		Object parentId;
		Node<T> parent;
		for (Node<T> node : nodes) {
			parentId=node.getParentId();
			parent=nodeMap.get(parentId);
			if(parent!=null) {
				parent.addChild(node);
			}
		}
		//
		for (Node<T> rn : this.roots) {
			buildHierarchical(rn,0);
		}
		
	}
	
	private String pathSeparator="/";
	
	/**
	 * 获得路径分隔符号，默认斜杠
	 * @return 路径分隔符
	 * */
	public String getPathSeparator() {
		return pathSeparator;
	}
	
	/**
	 * 设置路径分隔符号，默认斜杠
	 * @param  pathSeparator 路径分隔符
	 * */
	public void setPathSeparator(String pathSeparator) {
		this.pathSeparator = pathSeparator;
	}

	private void buildHierarchical(Node<T> node,int level)
	{
		node.level=level;
		Node<T> parent=node.getParent();
		if(parent==null) {
			node.path=node.getId().toString();
		} else {
			node.path=parent.path+pathSeparator+node.getId();
		}
		if(node.getChindren()!=null && node.getChindren().size()>0) {
			node.isLeaf=false;
			for (Node<T> cn : node.getChindren()) {
				buildHierarchical(cn,level+1);
			}
		} else  {
			node.isLeaf=true;
		}
	}
	

	/**
	 * 打印树形结构，仅输出 name 和 ID
	 * */
	public void print()
	{
		this.print(false);
	}
	
	/**
	 * 打印树形结构
	 * @param fullData 是否输出全部数据
	 * */
	public void print(boolean fullData)
	{
		int i=1;
		for (Node<T> rn : this.roots) {
			CodeBuilder code=new CodeBuilder("   ");
			code.ln("Tree("+i+"/"+this.getRootCount()+") : ");
			code.ln("━ "+rn.getName()+" "+(fullData?rn.toString():rn.getId()));
			addChildren(code,rn.getChindren(),1,fullData);
			System.out.println(code.toString());
			System.out.println();
			i++;
		}
		
	}

	private void addChildren(CodeBuilder code, Set<Node<T>> chindren, int i,boolean fullData) {
		if(chindren==null || chindren.size()==0) {
			return;
		}
		for (Node<T> node : chindren) {
			if(node.getChindren()!=null && node.getChindren().size()>0) {
				code.ln(i,"━ "+node.getName()+" , "+(fullData?node.toString():node.getId()));
				addChildren(code,node.getChindren(),i+1,fullData);
			} else {
				code.ln(i,"   "+node.getName()+" , "+(fullData?node.toString():node.getId()));
			}
		}
	}
	
	/**
	 * 节点数
	 * @return 节点数
	 * */
	public int size()
	{
		return this.nodes.size();
	}
	
}
