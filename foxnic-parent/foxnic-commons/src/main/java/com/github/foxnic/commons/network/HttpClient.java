package com.github.foxnic.commons.network;

import com.alibaba.fastjson.JSONObject;
import com.github.foxnic.commons.log.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;

public class HttpClient {

	private  CloseableHttpClient httpClient;

	private  RequestConfig requestConfig;

	public HttpClient() {
		httpClient = HttpClientBuilder.create().build();
		requestConfig = RequestConfig.DEFAULT;
	}

	public HttpClient(RequestConfig config) {
		httpClient = HttpClientBuilder.create().build();
		requestConfig = config;
	}

	public HttpClient(int connectionRequestTimeout,int connectTimeout,int socketTimeout) {
		httpClient = HttpClientBuilder.create().build();
		requestConfig = RequestConfig.custom().setConnectionRequestTimeout(connectionRequestTimeout).setConnectTimeout(connectTimeout)
				.setSocketTimeout(socketTimeout).build();
	}

	public String get(String url) throws Exception {
		return get(url,"utf-8");
	}
	public String get(String url,String charset) throws Exception {
		//
		HttpGet get = new HttpGet(url);
		//
		HttpResponse httpResponse = httpClient.execute(get);
		String response = EntityUtils.toString(httpResponse.getEntity(), charset);
		return response;
	}

	public String post(String url, Map<String, String> params,String charset) throws Exception {
		//
		HttpPost post = new HttpPost(url);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		//灌入参数
		if(params!=null) {
			for (Entry<String, String> e : params.entrySet()) {
				list.add(new BasicNameValuePair(e.getKey(), e.getValue()));
			}
		}
		UrlEncodedFormEntity form = new UrlEncodedFormEntity(list, charset);
		post.setEntity(form);
		post.setConfig(requestConfig);
		//
		HttpResponse httpResponse = httpClient.execute(post);
		String response = EntityUtils.toString(httpResponse.getEntity(), charset);
		return response;
	}

	public String postJSONObject(String url, JSONObject params) throws IOException {
		return postMap(url, params.toJSONString(),new HashMap<>());
	}

	public String postMap(String url, String bodyJsonParams, Map<String, String> headers) throws IOException {
		HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader("Content-Type", "application/json");
		httpPost.setEntity(new StringEntity(bodyJsonParams));

		if (headers != null && !headers.keySet().isEmpty()) {
			Set<String> keySet = headers.keySet();
			Iterator<String> iterator = keySet.iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				String value = headers.get(key);
				httpPost.addHeader(key, value);
			}
		}
		return execute(httpPost);
	}

	private static String execute(HttpUriRequest httpUriRequest) throws IOException {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			CloseableHttpResponse response = httpClient.execute(httpUriRequest);
			response.getAllHeaders();
			if (response.getStatusLine().getStatusCode() == 200) {// 请求成功状态
				try (BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(response.getEntity().getContent()))) {
                     String result="";
                     String tmp=null;
                     while((tmp=bufferedReader.readLine())!=null){
                      result+=tmp;
                     }
					return result;
				}
			}
		} catch (Exception e) {
			Logger.error("请求失败",e);
		}
		return null;
	}




}
