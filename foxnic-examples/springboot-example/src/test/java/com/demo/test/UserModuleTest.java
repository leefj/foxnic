package com.demo.test;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSONObject;
import com.demo.ExampleApplication;
import com.demo.business.user.domain.User;
import com.demo.business.user.domain.UserVO;
import com.demo.business.user.service.IUserService;
import com.github.foxnic.commons.collection.MapUtil;
import com.github.foxnic.springboot.mvc.Result;
import com.github.foxnic.springboot.starter.FoxnicApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {FoxnicApplication.class,ExampleApplication.class},webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserModuleTest {

	@Autowired
    private TestRestTemplate rest;
	
	@Autowired
    private IUserService service;
	
	public Result post(String url,Object data) {
		HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        HttpEntity formEntity = new HttpEntity(data,headers);
        Result res=rest.postForObject(url, formEntity, Result.class);
//        Result r=Result.fromJSON(res);
    	//Result r=(Result)res.getBody();
//    	return r;
        return res;
	}
	
	private String userName=null; 
 
	
    @Test
    public void testInsert() {
    	userName="LeeFJ-"+System.currentTimeMillis();
    	User user=new User();
    	user.setUsername(userName);
    	Result r=post("/usr/user/insert",user);
    	assertTrue(r.success());
    	
    	//数据库查询验证
    	List<User> us=service.queryEntities(user);
    	assertTrue(us.size()==1);
    	user=us.get(0);
    	
    	//接口查询验证
    	JSONObject data=MapUtil.asJSONObject("id",user.getId());
    	r=post("/usr/user/getById",data);
    	assertTrue(r.success());
    	user=(User)r.data();
    	assertTrue(user.getUsername().equals(userName));
    	
    	
    	//接口更新
    	userName="LeeFJ-"+System.currentTimeMillis();
    	user.setUsername(userName);
    	r=post("/usr/user/update",user);
    	
    	
    	//数据库查询验证
    	user=service.getById(user.getId());
    	assertTrue(user.getUsername().equals(userName));
    	
    	
    	//查询全部数据（谨慎使用）
    	r=post("/usr/user/queryList",null);
    	assertTrue(r.success());
    	us=(List<User>)r.data();
    	assertTrue(us.size()>0);
    	System.out.println();
    	
    	
    	
    	//查询全部数据（谨慎使用）
    	UserVO vo=new UserVO();
    	vo.setPageIndex(1);
    	vo.setPageSize(10);
    	r=post("/usr/user/queryPagedList",vo);
    	assertTrue(r.success());
    	us=(List<User>)r.data();
    	assertTrue(us.size()>0);
    	System.out.println();
    	
    }
    
    
	
}
