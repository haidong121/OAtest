package com.oa8000.prison.job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.oa8000.proxy.base.HibernatePage;
import com.oa8000.proxy.base.TransactionManager;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.oa8000.appservice.httrace.HtTraceViewService;
import com.oa8000.htjob.htjob00.HtJob00Constant;
import com.oa8000.htjob.htjob00.HtJob00JobBase;
import com.oa8000.httrace.httrace05.manager.HtNGTrace05HandleTraceManager;
import com.oa8000.prison.SignatureUtils;
import com.oa8000.proxy.base.HibernatePage;
import com.oa8000.proxy.comm.HiOaPubSystemMsg;
import com.oa8000.proxy.comm.HiUserInfo;
import com.oa8000.proxy.comm.OaTools;
import com.oa8000.proxy.dao.HiMainFetchDao;
import com.oa8000.proxy.db.HiDbTracePublish;
import com.oa8000.proxy.exception.OaException;
import com.oa8000.proxy.tools.ParseException;
import com.oa8000.server.htjob.HtJobInterface;
import com.oa8000.yozo.HttpClientUtil;

import okhttp3.MediaType;

import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
public class KTDocumtnLibraryJob extends HtJob00JobBase implements HtJobInterface{
	  
	  private static CloseableHttpClient httpclient = HttpClients.createDefault();
	   public void execute(JobExecutionContext context) throws JobExecutionException {
	        super.execute(context);
	        try {
	            tryDoJob(context, this);
	        } catch (OaException e) {
	            e.printStackTrace();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	   /**
	     * 手动执行调用方法
	     *
	     * @param jobTaskDetailId 任务detail类id
	     * @return 日志map
	     * @throws OaException 异常处理
	     */
	    @Override
	    public Map doJob(String jobTaskDetailId) throws OaException {
	        super.doJob(jobTaskDetailId);
	        return doJob();
	    }

	    /**
	     * 执行job动作
	     */
	    private Map doJob() {
	        log.info("ContractRemind is running");
	        Map retMap = new HashMap();
	        retMap.put("status", HtJob00Constant.LOG_STATUS_SUCCESS);
	        retMap.put("repeatCount", 1);
	        HiUserInfo userInfo = new HiUserInfo(1);
	        userInfo.languageType = "CN";
	        try {
	        	doHangingNet();
	        } catch (Exception e) {
	            e.printStackTrace();
	            retMap.put("status", HtJob00Constant.LOG_STATUS_FAIL);
	        }
	        return retMap;
	    }
  private void doHangingNet() throws Exception {
	  HttpClientUtil httpClientUtil = new HttpClientUtil();
	  
	  List<Object> paramList = new ArrayList();
	  HtTraceViewService server =new HtTraceViewService();
	  StringBuffer bf=new StringBuffer();
	    	String sql="SELECT t3.* FROM ( SELECT "
	    			+ "      t1.trace_publish_id,    t1.trace_instance_index_id, "
	    			+ "      t1.trace_title,       t1.trace_start_time,       t1.trace_end_time,     t1.doc_file_name, "
	    			+ "      t1.html_file_name,       t1.doc_num,       t2.category_title, "
	    			+ "      t1.publish_category_id,       t1.send_time,       t1.cancel_time, "
	    			+ "      t1.secret_num,       t1.create_time,       t1.view_user_list, "
	    			+ "      t1.flag ,      t1.company_id"
	    			+ "     FROM "
	    			+ "      trace_publish  t1 "
	    			+ "     LEFT JOIN trace_category  t2 "
	    			+ "     ON t1.publish_category_id = t2.trace_category_id "
	    			+ "     WHERE 1 = 1 AND t1.company_id = 'ROOT' AND t1.trace_mark = 1"
	    			+ "   "
	    			+ "  ) t3,"
	    			+ "    trace_instance_index t4 "
	    			+ "    WHERE t3.trace_instance_index_id = t4.trace_instance_index_id "
	    			+ "    and  t4.flag != -1  and t3.publish_category_id is null  and t3.doc_num not like '%无文号%' "
	    			+ "    ";
	    	
	 HibernatePage page=	(new HiMainFetchDao()).fetchListBySQLToMap(sql, paramList, 0, 10);
	 
	 /**
	     * 推送门户
	     */
	 if (page != null && page.objects != null) {
	      List<Map> traceInstanceIndexList = page.objects;
		  List<Map> list = new ArrayList<Map>();
	      for (Map queryResultMap : traceInstanceIndexList) {
	        if (queryResultMap == null)
	          continue; 
	        Map res = new HashMap();
			res.put("fileId" , queryResultMap.get("doc_file_name"));
			res.put("fileName" , queryResultMap.get("trace_title"));
			res.put("filePath" , "/opt/htoa/appdata/temp/"+queryResultMap.get("doc_file_name"));
			res.put("date" , queryResultMap.get("create_time"));
			list.add(res);
	      }
	      String str=  JSONArray.fromObject(list).toString();
	     // String str = JSONArray.fromObject(list).toString();
		  String result = "";
			String uid = "administrator";
			try {
				long now = System.currentTimeMillis();
				String timestamp = SignatureUtils.getTimestamp(now);
				String nonce = SignatureUtils.getNonce(now);
				String signature = SignatureUtils.calcRequestSign(timestamp, "AQhMqzZJolRq0nGuJ2A8A8nQg+kw7n/ezzBLMPA8OergEXgXUPv7x8v/GI5xD59u", nonce, uid, "", "");
				
				
//				RestTemplate restTemplate = new RestTemplate();
//				HttpHeaders headers = new HttpHeaders();
//				headers.add("x-tif-uid-signature", signature);
//				headers.add("x-tif-timestamp", SignatureUtils.getTimestamp(now));
//				headers.add("x-tif-uid", uid);
//				headers.add("x-tif-clientId", "goixyrib");
//				headers.add("x-tif-nonce", nonce);
//				MediaType type = MediaType.parseMediaType("application/json;charset=UTF-8");
//				headers.setContentType(type);
//				HttpEntity<String> formEntity = new HttpEntity(str , headers);
//				result = (String)restTemplate.postForObject("http://192.168.1.222:9081/apis/service-api/oafile/insertoafileinfo", formEntity, String.class, new Object[0]);
//				System.out.println(result);
				
				
				doPost("http://192.168.100.37:9081/apis/service-api/oafile/insertoafileinfo", str, headerMap());
			} catch (Exception var15) {
				System.out.println(var15);
			}
	 }
	 
	 
	 
	 if (page != null && page.objects != null) {
	      List<Map> traceInstanceIndexList = page.objects;
	      for (Map queryResultMap : traceInstanceIndexList) {
	        if (queryResultMap == null)
	          continue; 
	        bf.append(queryResultMap.get("trace_publish_id")).append(";");   
	      }
	      
	 }
	 
	 //server.doHangingNet(getUserInfo() ,bf.toString(),"tracePublishlistDict0000;",getSendUserinfo(),false,"2122-03-31","","");
	 HtNGTrace05HandleTraceManager traceManager = new HtNGTrace05HandleTraceManager("CN");
	    HiOaPubSystemMsg os = new HiOaPubSystemMsg("CN");
	    List<HiDbTracePublish> publishDatalist = new ArrayList<>();
	    String[] idAry = OaTools.split(bf.toString(), ";");
	    if (idAry != null && idAry.length > 0)
	      for (String currentId : idAry) {
	        if (!isBlank(currentId)) {
	          HiDbTracePublish eoTracePublish = getObject(HiDbTracePublish.class, currentId);
	          publishDatalist.add(eoTracePublish);
	        } 
	      }  
	    traceManager.saveTracePublishList(publishDatalist, "tracePublishlistDict0000;", getSendUserinfo(), "2122-03-31","", false);
	   
   }
  public String doPost(String url, String parameter, Map<String, String> header) throws Exception {
	   
	    HttpPost httppost = new HttpPost(url);
	    if (header.containsKey("Content-Type")) {
	      UrlEncodedFormEntity entity;
	      StringEntity stringEntity;

	        StringEntity requestEntity = new StringEntity(parameter,"utf-8");
	      switch ((String)header.get("Content-Type")) {
	        case "application/x-www-form-urlencoded; charset=UTF-8":
	          //entity = getFormEntity(parameter);
	         // entity.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
	        //  httppost.setEntity((HttpEntity)entity);
	          break;
	        case "application/json; charset=UTF-8":
	         // stringEntity = getJsonEntity(parameter);
	          httppost.setEntity(requestEntity);
	          break;
	      } 
	    } 
	    addHeader((HttpRequestBase)httppost, header);
	    CloseableHttpResponse response = null;
	    try {
	      response = httpclient.execute((HttpUriRequest)httppost);
	    } catch (IOException e) {
	      e.printStackTrace();
	    } 
	    HttpEntity entity1 = response.getEntity();
	    String result = null;
	    try {
	      result = EntityUtils.toString(entity1);
	      this.log.warn(result);
	    } catch (IOException e) {
	      e.printStackTrace();
	    } 
	    return result;
	  }
  private void addHeader(HttpRequestBase httpRequestBase, Map<String, String> header) throws Exception {
	    if (!MapUtils.isEmpty(header))
	      for (Map.Entry<String, String> entry : header.entrySet())
	        httpRequestBase.addHeader(entry.getKey(), entry.getValue());  
	  }
  private StringEntity getJsonEntity(Map<String, Object> map) {
	    JSONObject jsonParam = new JSONObject();
	    if (!MapUtils.isEmpty(map))
	      for (Map.Entry<String, Object> entry : map.entrySet())
	        jsonParam.put(entry.getKey(), entry.getValue());  
	    return new StringEntity(jsonParam.toString(), ContentType.APPLICATION_JSON);
	  }
  private UrlEncodedFormEntity getFormEntity(Map<String, Object> map) {
	    List<NameValuePair> formparams = new ArrayList<>();
	    if (!MapUtils.isEmpty(map))
	      for (Map.Entry<String, Object> entry : map.entrySet())
	        formparams.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));  
	    return new UrlEncodedFormEntity(formparams, Consts.UTF_8);
	  }
  private Map<String, String> headerMap() {
	    Map<String, String> headers = new HashMap<>();
	    //String authorization = "Basic " + OaTools.encodeBase64String(WpsConstants.autoKey);
	    //headers.put("Authorization", authorization);
	    //headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
	    
	    
	    String uid = "administrator";
	    long now = System.currentTimeMillis();
		String timestamp = SignatureUtils.getTimestamp(now);
		String nonce = SignatureUtils.getNonce(now);
		String signature = SignatureUtils.calcRequestSign(timestamp, "AQhMqzZJolRq0nGuJ2A8A8nQg+kw7n/ezzBLMPA8OergEXgXUPv7x8v/GI5xD59u", nonce, uid, "", "");
		
		
		//RestTemplate restTemplate = new RestTemplate();
		//HttpHeaders headers = new HttpHeaders();
		headers.put("x-tif-uid-signature", signature);
		headers.put("x-tif-timestamp", SignatureUtils.getTimestamp(now));
		headers.put("x-tif-uid", uid);
		headers.put("x-tif-clientId", "goixyrib");
		headers.put("x-tif-nonce", nonce);
		//MediaType type = MediaType.parseMediaType("application/json;charset=UTF-8");
		//headers.setContentType(type);
		//HttpEntity<String> formEntity = new HttpEntity(str , headers);
	    return headers;
	  }
   public HiUserInfo getUserInfo() {
	   HiUserInfo userInfo = new HiUserInfo(1);
       userInfo.languageType = "CN";
       return userInfo;
   }
   public String getSendUserinfo() {
	   List<Object> paramList = new ArrayList();
	   int i=0;
	   StringBuffer bf=new StringBuffer();
	   String sql="select hr_staff_info_id from HR_STAFF_INFO where staff_company='ROOT' " ;
	   HibernatePage page=	(new HiMainFetchDao()).fetchListBySQLToMap(sql, paramList, 0, 1000);
		 if (page != null && page.objects != null) {
		      List<Map> traceInstanceIndexList = page.objects;
		      for (Map queryResultMap : traceInstanceIndexList) {
		        if (queryResultMap == null)
		          continue; 
		        if (i==0) {
		            bf.append(";").append(queryResultMap.get("hr_staff_info_id"));}
		         else
		            bf.append(queryResultMap.get("hr_staff_info_id")).append(";");
		      }
		      
		 }
	   return bf.toString();
   }
   public boolean isBlank(String str) {
	    return StringUtils.isBlank(str);
	  }
   public <T> T getObject(Class<T> targetClass, String id) {
	    if (id == null || targetClass == null)
	      return null; 
	    T t = null;
	    Session session = null;
	    try {
	      session = TransactionManager.getInstance().getCurrentSession();
	      Transaction tx = session.getTransaction();
	      tx.begin();
	      t = (T)session.get(targetClass, id);
	    } catch (Exception e) {
	      e.printStackTrace();
	    } finally {
	      if (session != null)
	        session.close(); 
	    } 
	    return t;
	  }
}
