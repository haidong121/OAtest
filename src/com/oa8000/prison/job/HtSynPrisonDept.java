package com.oa8000.prison.job;

import com.oa8000.appservice.htsystemsetting.HtSystemSettingService;
import com.oa8000.hthr.hthr01.manager.HtHr01NewManager;
import com.oa8000.htjob.htjob00.HtJob00JobBase;
import com.oa8000.prison.PrisonManager;
import com.oa8000.proxy.comm.HiOaMainClass;
import com.oa8000.proxy.comm.HiUserInfo;
import com.oa8000.proxy.comm.OaTools;
import com.oa8000.proxy.db.HiDbHrDept;
import com.oa8000.proxy.exception.OaException;
import com.oa8000.server.htjob.HtJobInterface;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class HtSynPrisonDept extends HtJob00JobBase implements HtJobInterface {
    private static final Log log = LogFactory.getLog(HtSynPrisonDept.class);

    private HtSystemSettingService server = new HtSystemSettingService();

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

    public Map doJob(String jobTaskDetailId) throws OaException {
        super.doJob(jobTaskDetailId);
        return doJob();
    }

    private Map doJob() {
        System.out.println("ContractRemind is running");
        Map<Object, Object> retMap = new HashMap<>();
        retMap.put("status", Integer.valueOf(0));
        retMap.put("repeatCount", Integer.valueOf(1));
        HiUserInfo userInfo = new HiUserInfo(1);
        userInfo.languageType = "CN";
        try {
            synDept();
        } catch (Exception e) {
            e.printStackTrace();
            retMap.put("status", Integer.valueOf(1));
        }
        return retMap;
    }

    private void synDept() throws Exception {
        try {
            String deptInfo = (new PrisonManager()).synDeptInfo();
            if (StringUtils.isNotBlank(deptInfo)) {
                JSONObject dataObj = JSONObject.fromObject(deptInfo);
                JSONArray jsa = dataObj.getJSONArray("records");
                HiOaMainClass manager = new HiOaMainClass();
                List<HiDbHrDept> insertList = new LinkedList<>();
                List<HiDbHrDept> updateList = new LinkedList<>();
                for (Object o : jsa) {
                    JSONObject eo = (JSONObject)o;
                    String did = eo.getString("deptId");
                    String name = eo.getString("deptName");
                    String pid = eo.getString("parentId");
                    String order = eo.getString("sortIndex");
                    String num = eo.getString("deptCode");
                    System.out.println("====================did   "+did);
                    System.out.println("====================name   "+name);
                    System.out.println("====================pid    "+pid);
                    System.out.println("====================order  "+order);
                    System.out.println("====================num  "+num);
                    String pathId = eo.getString("pathId");  //获取部门的分级结构例如  "/150000000/1368828691869642754/1387973722354339841",
                    int delFlg = 0;
                    if (eo.containsKey("delFlag"))
                        delFlg = eo.getInt("delFlag");
                    if (did.equals("150000000"))
                        continue;
                    String deptId = "BM" + did;
                    String parentId = null;
                    if (pid.equals("150000000")) {
                        parentId = "ROOT";
                    } else {
                        parentId = "BM" + pid;
                    }
                    HiDbHrDept dept = (HiDbHrDept)manager.getObject(HiDbHrDept.class, deptId);
                    if (dept != null) {
                        dept.setUpdateTime(new Date());
                        dept.setUpdateUserId("adms");
                        updateList.add(dept);
                    } else {
                        dept = new HiDbHrDept();
                        dept.setHrDeptId(deptId);
                        dept.setCreateTime(new Date());
                        dept.setCreateUserId("adms");
                        insertList.add(dept);
                    }
                    dept.setDeptName(name);
                    dept.setDeptParentId(parentId);
                    dept.setDeptNum(num);
                    if (StringUtils.isNotBlank(order)) {
                        dept.setDeptOrder(Integer.valueOf(order));
                    } else {
                        dept.setDeptOrder(Integer.valueOf(999));
                    }
                    dept.setDelFlag(Integer.valueOf(delFlg));
                    String jtid="1368828691869642754";//表示集团的deptid
                    String deptType = eo.getString("deptType");
//                    if ("2".equals(deptType)) {
//                        dept.setIsSonCompany(Integer.valueOf(1));
//                        dept.setCompanyId(deptId);
//                    } else {
//                        dept.setIsSonCompany(Integer.valueOf(0));
//                        dept.setCompanyId("ROOT");
//                    }
                    if (pathId.indexOf(jtid)>-1) {
                        if  (pid.equals("150000000")) {
                            dept.setIsSonCompany(Integer.valueOf(1));
                            dept.setCompanyId(deptId);
                        }else {
                            if (pid.equals(jtid)) {
                                //表示是集团下面的部门
                                if ("2".equals(deptType)) {
                                    dept.setIsSonCompany(Integer.valueOf(1));//单位
                                    dept.setCompanyId(deptId);
                                } else {
                                    dept.setIsSonCompany(Integer.valueOf(0));
                                    dept.setCompanyId("BM1368828691869642754");//部门
                                }


//                                dept.setIsSonCompany(Integer.valueOf(1));
//                                dept.setCompanyId(deptId);
                            } else {
                                //表示是集团下面的分公司
//                                String aryDept[] = pathId.split("/");
                                System.out.println("===============pathId    "+pathId);
                                dept.setIsSonCompany(Integer.valueOf(1));
//                                dept.setCompanyId("BM" + aryDept[4]);
                                dept.setCompanyId(parentId);

                            }
                        }
                    }else {
                        //表示监狱局部分逻辑
                        if(pid.equals("150000000")){
                            if ("2".equals(deptType)) {
                                dept.setIsSonCompany(Integer.valueOf(1));
                                dept.setCompanyId(deptId);
                            } else {
                                dept.setIsSonCompany(Integer.valueOf(0));
                                dept.setCompanyId("ROOT");
                            }
                        }else{
                            //表示监狱下部门
                            dept.setIsSonCompany(Integer.valueOf(1));
                            dept.setCompanyId(parentId);

                        }




//                        //表示监狱局部分逻辑
//                        if (pid.equals("150000000")) {
//                            //表示监狱的部门
//                            dept.setIsSonCompany(Integer.valueOf(0));
//                            dept.setCompanyId("ROOT");
//                        }else {
//                            //表示下面的监狱
//                            dept.setIsSonCompany(Integer.valueOf(1));
//                            dept.setCompanyId(deptId);
//                        }

                    }
                    dept.setIsPayroll(Integer.valueOf(0));
                    dept.setPayrollNum(Integer.valueOf(0));
                    dept.setDeptShift("fromOther");
                }
                manager.saveChange(insertList, updateList, null, null);
                HiOaMainClass.setDeptMapNull();
                updateAllDeptTree();
            }
        } catch (OaException e) {
            throw e;
        }
    }

    private void updateAllDeptTree() throws OaException {
        HiOaMainClass manager = new HiOaMainClass();
        List<HiDbHrDept> hrDeptList = manager.fetchDept();
        List<HiDbHrDept> allFirstLevelDep = (new HtHr01NewManager()).queryHrDept(null, "ROOT", null);
        List<HiDbHrDept> updateObjs = new LinkedList();
        for (HiDbHrDept dept : allFirstLevelDep) {
            updateObjs.add(dept);
            dept.setDeptTree("ROOT" + dept.getHrDeptId());
            updateObjs = saveNewDeptSonList(hrDeptList, dept, updateObjs);
        }
        manager.saveChange(null, updateObjs, null, null);
    }

    private List saveNewDeptSonList(List<HiDbHrDept> hrDeptList, HiDbHrDept parentDept, List<HiDbHrDept> updateObjs) {
        List<HiDbHrDept> deptAry = OaTools.fetchArrayReturnArray(hrDeptList, "deptParentId", parentDept.getHrDeptId());
        int count = deptAry.size();
        if (count > 0)
            for (int i = 0; i < count; i++) {
                HiDbHrDept dept = deptAry.get(i);
                if (dept != null) {
                    String deptTree = parentDept.getDeptTree();
                    if (deptTree == null)
                        deptTree = "";
                    dept.setDeptTree(deptTree + dept.getHrDeptId());
 //                   dept.setCompanyId(parentDept.getCompanyId());
                    updateObjs.add(dept);
                    updateObjs = saveNewDeptSonList(hrDeptList, dept, updateObjs);
                }
            }
        return updateObjs;
    }

    private JSONArray getDeptList(String url, String access_token) {
        String action = "/v1/dept/relation";
        String p = "access_token=" + access_token + "&did=0";
        String re = sendGet(url + action + "?" + p);
        JSONObject jso = JSONObject.fromObject(re);
        String errcode = jso.getString("errcode");
        if ("0".equals(errcode))
            return jso.getJSONArray("datas");
        return null;
    }

    private String getToken(String url, String corpid, String corpsecret) {
        String action = "/v1/gettoken";
        String p = "corpid=" + corpid + "&corpsecret=" + corpsecret;
        String re = sendGet(url + action + "?" + p);
        JSONObject jso = JSONObject.fromObject(re);
        String errcode = jso.getString("errcode");
        if ("0".equals(errcode))
            return jso.getString("access_token");
        return null;
    }

    public static String sendGet(String urlNameString) {
        String result = "";
        BufferedReader in = null;
        try {
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.connect();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null)
                result = result + line;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }
}
