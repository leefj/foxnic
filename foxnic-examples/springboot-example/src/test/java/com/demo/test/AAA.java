package com.demo.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.demo.ExampleApplication;
import com.demo.business.user.domain.User;
import com.github.foxnic.commons.busi.Result;
import com.github.foxnic.springboot.starter.FoxnicApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {FoxnicApplication.class,ExampleApplication.class},webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AAA {

	@Autowired
    private TestRestTemplate rest;
	
	public Result postForEntity(String url,Object entity) {
		HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        HttpEntity formEntity = new HttpEntity(entity,headers);
        ResponseEntity res=rest.postForEntity(url, formEntity,Result.class);
    	Result r=(Result)res.getBody();
    	return r;
	}
	
    @Test
    public void getName() {
    	User user=new User();
    	user.setUsername("LeeFJ-"+System.currentTimeMillis());

    	Result r=postForEntity("/usr/user/insert",user);
    	
    	assertTrue(r.success());
        //String name = rest.getForObject("/usr/user/insert", String.class);
        //System.out.println(name);
//        Assert.assertEquals("Adam", name);
    }
	
}
