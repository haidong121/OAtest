package com.oa8000.appservice.httraced;


import com.cnpower.base.OaBaseSysInfo;
import com.cnpower.base.OaBaseTools;
import com.cnpower.base.OaSystemConstant;
import com.oa8000.appservice.hthrwork.HtHrWorkService;
import com.oa8000.appservice.httrace.HtTraceBaseService;
import com.oa8000.appservice.httraceform.HtTraceSystemDefineService;
import com.oa8000.htbrrow.htbrrow01.HtBrrowManager;
import com.oa8000.htcrm.htcrm02.HtCrmColumnManager;
import com.oa8000.hthr.hthr01.manager.HtHr01NewManager;
import com.oa8000.htknowledge.htknowledge00.utils.Utility;
import com.oa8000.htnkpi.htnkpi00.HtKpiTools;
import com.oa8000.httrace.httrace00.HtTrace00Constant;
import com.oa8000.httrace.httrace01.manager.HtTrace01ActionManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01AgentManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01CategoryManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01DbSourceManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01DefineNoManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01KeyWordManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01QuickMindManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01RedHeadManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01SqlManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01SqlProcess;
import com.oa8000.httrace.httrace01.manager.HtTrace01TableCssManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01UserManager;
import com.oa8000.httrace.httrace02.manager.HtTrace02PathManager;
import com.oa8000.httrace.httrace03.manager.HtNGTrace03TemplateManager;
import com.oa8000.httrace.httrace03.manager.HtTrace03TemplateManager;
import com.oa8000.httrace.httrace05.manager.HtNGTrace05HandleTraceManager;
import com.oa8000.httrace.httrace05.manager.HtNGTrace05TraceBaseManager;

import com.oa8000.httrace.httrace05.manager.HtNGTrace05ViewTraceManager;
import com.oa8000.httrace.httrace08.manager.HtTrace08ViewManager;
import com.oa8000.httrace.msgTools.HtTraceMsgTools;
import com.oa8000.httraceform.dbconnect.ExternalConnectionProvider;
import com.oa8000.httraceform.httraceform01.manager.HtTraceForm01Manager;
import com.oa8000.httraceform.httraceform04.manager.HtTraceSystemViewManager;
import com.oa8000.httraceform.httraceform07.HtTraceForm07Manager;
import com.oa8000.proxy.base.HibernatePage;
import com.oa8000.proxy.comm.FunctionModule;
import com.oa8000.proxy.comm.HiOaMainClass;
import com.oa8000.proxy.comm.HiOaMainService;
import com.oa8000.proxy.comm.HiOaPubSystemMsg;
import com.oa8000.proxy.comm.HiUserInfo;
import com.oa8000.proxy.comm.JasonUtility;
import com.oa8000.proxy.comm.OaPubDateManager;
import com.oa8000.proxy.comm.OaTools;
import com.oa8000.proxy.comm.PageModel;
import com.oa8000.proxy.comm.SortModel;
import com.oa8000.proxy.comm.file.HiFile;
import com.oa8000.proxy.dao.HiMainDao;
import com.oa8000.proxy.db.*;
import com.oa8000.proxy.exception.OaException;
import com.oa8000.server.htfile.HtFileServer;
import com.oa8000.server.hthr.HtHrServer;
import com.oa8000.server.htmanager.HtMenuForeignAPI;
import com.oa8000.wpsOffice.wpsOfficeManager;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;


public class HtTraceViewService1 extends HtTraceBaseService {
    public String getTraceInstanceIndexByReadList(HiUserInfo userInfo, SortModel sortModel, PageModel pageModel, String templateDictId, String generalSearchCondition, Integer traceMark, String startTime, String endTime, String readStartTime, String readEndTime, Integer readFlg, String traceTitle, String createReadUserId, String docNum, String focusFlg, String otherData) throws Exception {
        HtNGTrace05ViewTraceManager htNGTrace05TraceManager = new HtNGTrace05ViewTraceManager(userInfo.languageType);
        HibernatePage page = htNGTrace05TraceManager.fetchTraceInstanceIndexByRead(templateDictId, generalSearchCondition, traceMark, startTime, endTime, readStartTime, readEndTime, readFlg, traceTitle, createReadUserId, docNum, userInfo.webUsers
                .getUserId(), focusFlg, otherData, sortModel, pageModel);
        JSONArray returnJsonAry = new JSONArray();
        Date today = new Date();
        if (page != null && page.objects != null) {
            List<Map> traceInstanceIndexList = page.objects;
            for (Map queryResultMap : traceInstanceIndexList) {
                if (queryResultMap == null)
                    continue;
                JSONObject jso = new JSONObject();
                jso.put("trace_handout_view_list_id", queryResultMap.get("trace_handout_view_list_id"));
                jso.put("trace_instance_index_id", queryResultMap.get("trace_instance_index_id"));
                jso.put("create_user_id", queryResultMap.get("create_user_id"));
                jso.put("attachment_list", queryResultMap.get("attachment_list"));
                jso.put("protected_info", queryResultMap.get("protected_info"));
                jso.put("directory", queryResultMap.get("directory"));
                jso.put("trace_title", queryResultMap.get("trace_title"));
                jso.put("trace_mark", queryResultMap.get("trace_mark"));
                jso.put("read_time", dateToStringUntilMinute(queryResultMap.get("read_time")));
                jso.put("trace_title_text", HtTrace00Constant.getTraceTitleTextValue(String.valueOf(queryResultMap.get("trace_title"))));
                Object traceStartTime = queryResultMap.get("trace_start_time");
                jso.put("trace_start_time", dateToStringUntilMinute(traceStartTime));
                jso.put("user_name", queryResultMap.get("user_name"));
                jso.put("doc_num", queryResultMap.get("doc_num"));
                jso.put("flag", queryResultMap.get("flag"));
                jso.put("concern_list_id", queryResultMap.get("concern_list_id"));
                jso.put("create_time", dateToStringUntilDay(queryResultMap.get("create_time")));
                jso.put("secret_num", queryResultMap.get("secret_num"));
                jso.put("isHtmlFlg", Boolean.valueOf(isNotBlank((queryResultMap.get("html_file_name") == null) ? null : queryResultMap.get("html_file_name").toString())));
                jso.put("external_modules_id", queryResultMap.get("external_modules_id"));
                jso.put("intervalDays", Integer.valueOf((traceStartTime == null) ?
                        compareDays(today, (Date)queryResultMap.get("create_time")) :
                        compareDays(today, (Date)queryResultMap.get("create_time"))));
                jso = getUserHeadData(userInfo, jso, (String)queryResultMap.get("create_user_id"));
                returnJsonAry.add(jso);
            }
        }
        return returnJsonAryString(makePagePackage((List)returnJsonAry, page));
    }
    private Object dateToStringUntilDay(Object time) {
        if (time instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return sdf.format(time);
        }
        return null;
    }

    private Object dateToStringUntilMinute(Object time) {
        if (time instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return sdf.format(time);
        }
        return null;
    }
    private JSONObject getUserHeadData(HiUserInfo userInfo, JSONObject jso, String userId) {
        HiDbUserUser creatUserObj = (new HtNGTrace05HandleTraceManager(userInfo.languageType)).getUserUser(userId);
        if (creatUserObj != null) {
            JSONObject _jso = new JSONObject();
            OaBaseSysInfo sysInfo = OaTools.createOaBaseSysInfo();
            _jso.put("userId", userId);
            _jso.put("user_name", creatUserObj.getUserName());
            _jso.put("pinyin", creatUserObj.getPinyin());
            _jso.put("img_path", sysInfo.getDataUserImagePath());
            jso.put("authorImgSrc", (new HtNGTrace05HandleTraceManager(userInfo.languageType)).getUserImgSrc(userId));
            jso.put("userHeadData", _jso);
        }
        return jso;
    }

    public String getTraceInstanceIndexByWaitList(HiUserInfo userInfo, SortModel sortModel, PageModel pageModel, String generalSearchCondition, String templateId,
                                                  String templateCategoryId, Integer traceMark, String startTime, String endTime, String docNum, String traceTitle,
                                                  String createUserId, String createUserName, String focusFlg, String otherData) {
        HtNGTrace05ViewTraceManager1 htNGTrace05TraceManager = new HtNGTrace05ViewTraceManager1(userInfo.languageType);
        HibernatePage page = htNGTrace05TraceManager.fetchTraceInstanceIndexByWait(generalSearchCondition, templateId, templateCategoryId, traceMark, startTime, endTime, docNum, traceTitle, createUserId, createUserName, userInfo.webUsers
                .getUserId(), focusFlg, otherData, userInfo.isFaker(), sortModel, pageModel);
        JSONArray returnJsonAry = new JSONArray();
        Date today = new Date();
        if (page != null && page.objects != null) {
            List<Map> traceInstanceIndexList = page.objects;
            for (Map queryResultMap : traceInstanceIndexList) {
                if (queryResultMap == null)
                    continue;
                JSONObject jso = new JSONObject();
                jso.put("trace_instance_index_id", queryResultMap.get("trace_instance_index_id"));
                jso.put("getcount", queryResultMap.get("getcount"));
                jso.put("trace_title", queryResultMap.get("trace_title"));
                jso.put("directory", queryResultMap.get("directory"));
                jso.put("trace_mark", queryResultMap.get("trace_mark"));
                jso.put("create_user_id", queryResultMap.get("create_user_id"));
                jso.put("attachment_list", queryResultMap.get("attachment_list"));
                jso.put("protected_info", queryResultMap.get("protected_info"));
                jso.put("trace_title_text", HtTrace00Constant.getTraceTitleTextValue(String.valueOf(queryResultMap.get("trace_title"))));
                Object traceStartTime = queryResultMap.get("trace_start_time");
                Object start_time = queryResultMap.get("start_time");
                jso.put("trace_start_time", dateToStringUntilMinute(traceStartTime));
                jso.put("create_user_name", queryResultMap.get("user_name"));
                jso.put("doc_num", queryResultMap.get("doc_num"));
                jso.put("current_step_title", queryResultMap.get("current_step_title"));
                jso.put("path_title", queryResultMap.get("path_title"));
                jso.put("more_info", getFormValue(String.valueOf(queryResultMap.get("trace_instance_index_id")),
                        String.valueOf(queryResultMap.get("trace_template_dict_id")),
                        String.valueOf(queryResultMap.get("main_table_name")),
                        String.valueOf(queryResultMap.get("more_info"))));
                jso.put("concern_list_id", queryResultMap.get("concern_list_id"));
                jso.put("trace_priority", queryResultMap.get("trace_priority"));
                jso.put("secret_num", queryResultMap.get("secret_num"));
                jso.put("isHtmlFlg", Boolean.valueOf(isNotBlank((queryResultMap.get("html_file_name") == null) ? null : queryResultMap.get("html_file_name").toString())));
                jso.put("intervalDays", Integer.valueOf((start_time == null) ? 0 : compareDays(today, (Date)start_time)));
                jso.put("flag", queryResultMap.get("flag"));
                jso.put("external_modules_id", queryResultMap.get("external_modules_id"));
                jso.put("red_title_flg", Boolean.valueOf(getRedTitleFlg(userInfo, queryResultMap)));
                jso.put("trace_template_dict_id", queryResultMap.get("trace_template_dict_id"));
                jso.put("template_title", queryResultMap.get("template_title"));
                jso = getUserHeadData(userInfo, jso, (String)queryResultMap.get("create_user_id"));
                returnJsonAry.add(jso);
            }
        }
        return returnJsonAryString(makePagePackage((List)returnJsonAry, page));
    }

    private String getFormValue(String hiTraceInstanceIndexId, String templateId, String mainTableName, String columNameList) {
        if (isBlank(mainTableName) || "null".equals(mainTableName))
            return "";
        List<Map> list = (new HtTrace01SqlManager()).getData(mainTableName, hiTraceInstanceIndexId, true);
        if (list == null || list.size() == 0)
            return "";
        Map dataMap = list.get(0);
        HtTrace03TemplateManager templateManager = new HtTrace03TemplateManager();
        if (isBlank(columNameList) || ";".equals(columNameList))
            return "";
        String[] columNameAry = columNameList.split(";");
        StringBuilder str = new StringBuilder();
        for (String columId : columNameAry) {
            if (!"".equals(columId)) {
                HiDbTraceDefineField eoDefinedFile = templateManager.getTraceDefineField(templateId, columId);
                if (eoDefinedFile != null) {
                    String createFieldName = eoDefinedFile.getCreateFieldName();
                    String value = String.valueOf(dataMap.get(createFieldName));
                    if (isBlank(value) || "null".equals(value))
                        value = "";
                    if ("CHECKBOX".equals(eoDefinedFile.getShowType()))
                        if ("1".equals(String.valueOf(value))) {
                            value = "选中";
                        } else {
                            value = "未选中";
                        }
                    if (StringUtils.isBlank(value))
                        return "";
                    if (value.contains("00:00:00.0"))
                        value = value.replace("00:00:00.0", "");
                    str.append(columId).append(":").append(value).append(";");
                }
            }
        }
        return str.toString();
    }

    private boolean getRedTitleFlg(HiUserInfo userInfo, Map currentTraceInstanceIndex) {
        if (currentTraceInstanceIndex == null)
            return false;
        Date extendedDate = (Date)currentTraceInstanceIndex.get("current_step_extended_date");
        Date now = new Date();
        if (extendedDate != null)
            if (now.after(extendedDate))
                return true;
        Integer count = null;
        Object traceLimitDateObj = currentTraceInstanceIndex.get("trace_limit_date");
        if (traceLimitDateObj instanceof BigDecimal) {
            BigDecimal limitDate = (BigDecimal)traceLimitDateObj;
            count = Integer.valueOf(limitDate.intValue());
        } else {
            count = (Integer)traceLimitDateObj;
        }
        if (count != null && count.intValue() > 0) {
            HtHrWorkService hrWorkServer = new HtHrWorkService();
            int workDayCount = 0;
            try {
                String s = hrWorkServer.getMeasureWorktime(userInfo, userInfo.webUsers.getUserId(), OaTools.dateStr((Date)currentTraceInstanceIndex.get("trace_start_time")), OaTools.dateStr(), null);
                JSONObject jso = JSONObject.fromObject(s);
                workDayCount = ((Integer)jso.get("countWorktime")).intValue();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            if (workDayCount > count.intValue())
                return true;
        }
        return false;
    }

    public String getTraceInstanceIndexByManageList(HiUserInfo userInfo, SortModel sortModel, PageModel pageModel, String companyId, String generalSearchCondition, String templateId, String templateCategoryId, Integer traceMark, Integer currentStatus, String startTime, String endTime, String docNum, String traceTitle, String createUserId, String createUserName, String focusFlg, String otherData) throws Exception {
        HtNGTrace05ViewTraceManager1 htNGTrace05TraceManager = new HtNGTrace05ViewTraceManager1(userInfo.languageType);
        if (!userInfo.superManagerFlg)
            companyId = userInfo.companyId;
        HibernatePage page = htNGTrace05TraceManager.fetchTraceInstanceIndexByManage(companyId, generalSearchCondition, templateId, templateCategoryId, currentStatus, traceMark, startTime, endTime, docNum, traceTitle, createUserId, createUserName, userInfo, focusFlg, otherData, sortModel, pageModel);
        JSONArray returnJsonAry = new JSONArray();
        Date today = new Date();
        if (page != null && page.objects != null) {
            List<Map> traceInstanceIndexList = page.objects;
            for (Map queryResultMap : traceInstanceIndexList) {
                if (queryResultMap == null)
                    continue;
                JSONObject jso = new JSONObject();
                jso.put("getcount", queryResultMap.get("getcount"));
                jso.put("trace_instance_index_id", queryResultMap.get("trace_instance_index_id"));
                jso.put("trace_title", queryResultMap.get("trace_title"));
                jso.put("trace_title_text", HtTrace00Constant.getTraceTitleTextValue(String.valueOf(queryResultMap.get("trace_title"))));
                Object traceStartTime = queryResultMap.get("trace_start_time");
                jso.put("trace_start_time", dateToStringUntilMinute(traceStartTime));
                jso.put("create_user_name", queryResultMap.get("user_name"));
                jso.put("trace_mark", queryResultMap.get("trace_mark"));
                jso.put("current_trace_state", queryResultMap.get("current_trace_state"));
                jso.put("doc_num", queryResultMap.get("doc_num"));
                jso.put("current_step_title", queryResultMap.get("current_step_title"));
                jso.put("path_title", queryResultMap.get("path_title"));
                jso.put("concern_list_id", queryResultMap.get("concern_list_id"));
                jso.put("trace_priority", queryResultMap.get("trace_priority"));
                jso.put("secret_num", queryResultMap.get("secret_num"));
                if (queryResultMap.get("company_id") != null)
                    jso.put("company_name", ";;".equals(queryResultMap.get("company_id").toString()) ? "全集团": (new HiOaMainService()).getDeptName(queryResultMap.get("company_id").toString()));
                            jso.put("isHtmlFlg", Boolean.valueOf(isNotBlank((queryResultMap.get("html_file_name") == null) ? null : queryResultMap.get("html_file_name").toString())));
                jso.put("intervalDays", Integer.valueOf((traceStartTime == null) ? 0 : compareDays(today, (Date)traceStartTime)));
                jso.put("flag", queryResultMap.get("flag"));
                returnJsonAry.add(jso);
            }
        }
        return returnJsonAryString(makePagePackage((List)returnJsonAry, page));
    }

    public String getTraceInstanceIndexByHandleList(HiUserInfo userInfo, SortModel sortModel, PageModel pageModel, String generalSearchCondition, String templateId, String templateCategoryId, Integer traceMark, String startTime, String endTime, String docNum, String traceTitle, String createUserId, String createUserName, String focusFlg, String finishFlg, String otherData) {
        HtNGTrace05ViewTraceManager1 htNGTrace05TraceManager = new HtNGTrace05ViewTraceManager1(userInfo.languageType);
        HibernatePage page = htNGTrace05TraceManager.fetchTraceInstanceIndexByHandle(generalSearchCondition, templateId, templateCategoryId, traceMark, startTime, endTime, docNum, traceTitle, createUserId, createUserName, userInfo.webUsers
                .getUserId(), focusFlg, finishFlg, sortModel, pageModel);
        JSONArray returnJsonAry = new JSONArray();
        Date today = new Date();
        if (page != null && page.objects != null) {
            List<Map> traceInstanceIndexList = page.objects;
            for (Map queryResultMap : traceInstanceIndexList) {
                if (queryResultMap == null)
                    continue;
                JSONObject jso = new JSONObject();
                jso.put("trace_instance_index_id", queryResultMap.get("trace_instance_index_id"));
                jso.put("trace_title", queryResultMap.get("trace_title"));
                jso.put("getundertakecount", queryResultMap.get("getundertakecount"));
                jso.put("directory", queryResultMap.get("directory"));
                jso.put("current_trace_state", queryResultMap.get("current_trace_state"));
                jso.put("trace_title_text", HtTrace00Constant.getTraceTitleTextValue(String.valueOf(queryResultMap.get("trace_title"))));
                Object traceStartTime = queryResultMap.get("trace_start_time");
                jso.put("trace_start_time", dateToStringUntilMinute(traceStartTime));
                jso.put("undertake_time", (queryResultMap.get("undertake_time") != null) ? dateToStringUntilMinute(queryResultMap.get("undertake_time")) : "");
                jso.put("create_user_name", queryResultMap.get("user_name"));
                jso.put("create_user_id", queryResultMap.get("create_user_id"));
                jso.put("doc_num", queryResultMap.get("doc_num"));
                jso.put("trace_mark", queryResultMap.get("trace_mark"));
                jso.put("concern_list_id", queryResultMap.get("concern_list_id"));
                jso.put("trace_priority", queryResultMap.get("trace_priority"));
                jso.put("secret_num", queryResultMap.get("secret_num"));
                jso.put("isHtmlFlg", Boolean.valueOf(isNotBlank((queryResultMap.get("html_file_name") == null) ? null : queryResultMap.get("html_file_name").toString())));
                jso.put("intervalDays", Integer.valueOf((traceStartTime == null) ? 0 : compareDays(today, (Date)traceStartTime)));
                jso = getUserHeadData(userInfo, jso, (String)queryResultMap.get("create_user_id"));
                returnJsonAry.add(jso);
            }
        }
        return returnJsonAryString(makePagePackage((List)returnJsonAry, page));
    }
    public String getDeptAndUserList(HiUserInfo userInfo, String deptId, String selectUserIdList, String selectDeptIdList, Boolean showAllGroupFlag, String otherData) throws Exception {
        List userList;
        System.out.println("userInfo.companyId====" + userInfo.companyId);
        System.out.println("deptId=======" + deptId);
        System.out.println("selectUserIdList=======" + selectUserIdList);
        System.out.println("selectDeptIdList=======" + selectDeptIdList);
        System.out.println("showAllGroupFlag=======" + showAllGroupFlag);
        System.out.println("otherData=======" + otherData);

        String queryCompanyId = userInfo.companyId;
        Integer searchFlg = null;
        if (StringUtils.isNotBlank(otherData)) {
            JSONObject jsonObject = getJSONObjectFromS(otherData);
            if (jsonObject.containsKey("companyId"))
                queryCompanyId = jsonObject.getString("companyId");
            if (jsonObject.containsKey("searchFlg"))
                searchFlg = Integer.valueOf(Integer.parseInt(jsonObject.getString("searchFlg")));
        }
        if (StringUtils.isBlank(deptId))
            deptId = "ROOT";
        String hq1="select * from hr_dept where hr_dept_id='"+userInfo.webUsers.getDeptId()+"'";
        System.out.println("======"+hq1+"====");
        List userdeptList = (new HiMainDao()).findListBySQLToMap(hq1,  new ArrayList());
        String companyid=userInfo.companyId;
        if (userdeptList.size()>0) {
            companyid=((Map)userdeptList.get(0)).get("company_id").toString();
        }
        String hql = "select a.*,(select count(b.hr_dept_id) from  hr_dept b where b.dept_parent_id=a.hr_dept_id) as c from hr_dept a where  (a.del_flag is null or a.del_flag = ? )";
        List<Object> paramList = new ArrayList();
        paramList.add(Integer.valueOf(0));
        if (deptId.equals("ROOT")) {
            if ("ROOT".equals(companyid)) {
                hql = hql + " and a.dept_parent_id = 'ROOT'  and a.company_id='ROOT'";
            } else {
                hql = hql + " and a.hr_dept_id in (select \"iDomainId\" from \"tbOsDomain\"  where \"sRelateId\"='" + companyid + "'union select '" + companyid + "' as \"iDomainId\")";
            }
        } else {
            hql = hql + " and a.dept_parent_id = '" + deptId + "' ";
        }
        hql = hql + " order by a.dept_order ";
        List deptList = (new HiMainDao()).findListBySQLToMap(hql, paramList);
        if (searchFlg == null || searchFlg.intValue() == 0) {
            userList = (new HiOaMainClass()).getUserArrayByDeptId(deptId);
        } else {
            userList = (new HtHr01NewManager(userInfo.languageType)).fetchUserUserByStaffInfo(searchFlg, deptId, null, null, null);
        }
        JSONObject jso = new JSONObject();
        JSONArray deptAry = new JSONArray();
        JSONArray userAry = new JSONArray();
        int deptCount = 0;
        int userCount = 0;
        for (Object o : deptList) {
            Map dept = (Map)o;
            String _deptId = dept.get("hr_dept_id").toString();
            String _companyId = dept.get("company_id").toString();
            JSONObject _jso = new JSONObject();
            if (StringUtils.isNotBlank(selectDeptIdList) &&
                    !OaSystemConstant.ALL_USER.equals(selectDeptIdList) &&
                    !selectDeptIdList.contains(_deptId) && !selectDeptIdList.equals("*" + _companyId))
                continue;
            _jso.put("deptId", _deptId);
            _jso.put("name", dept.get("dept_name"));
            _jso.put("deptOrder", dept.get("dept_order"));
            _jso.put("isSon", Boolean.valueOf((Integer.parseInt(dept.get("c").toString()) > 0)));
            deptAry.add(_jso);
            deptCount++;
        }
        for (Object o : userList) {
            HiDbUserUser user = (HiDbUserUser)o;
            JSONObject _jso = new JSONObject();
            if ("adms".equals(user.getUserId()))
                continue;
            if (StringUtils.isNotBlank(selectUserIdList) && !selectUserIdList.contains(user.getUserId()))
                continue;
            if (StringUtils.isNotBlank(selectDeptIdList) && !selectDeptIdList.contains(user.getDeptId()))
                continue;
            if (user.getOutUserFlag() != null && user.getOutUserFlag().equals("1"))
                continue;
            _jso.put("userId", user.getUserId());
            _jso.put("name", user.getUserName());
            _jso.put("userOrder", user.getUserOrder());
            HiDbHrAddressListDetail addressListDetail = HiOaMainClass.getAddressFormJedis(_jso.getString("userId"));
            if (addressListDetail != null) {
                _jso.put("email", addressListDetail.getPersonalEmail());
                _jso.put("mobile", addressListDetail.getPersonnelMobile());
            }
            userCount++;
            userAry.add(_jso);
        }
        jso.put("deptArray", deptAry);
        jso.put("userArray", userAry);
        jso.put("deptCount", Integer.valueOf(deptCount));
        jso.put("userCount", Integer.valueOf(userCount));
        return returnJsonString(jso);
    }
    public String getWaitUndertakeCount(HiUserInfo userInfo, Integer traceMark, String otherData) throws Exception {
        HtNGTrace05ViewTraceManager1 nGTrace05ViewTraceManager = new HtNGTrace05ViewTraceManager1(userInfo.languageType);
        String count = nGTrace05ViewTraceManager.fetchTraceInstanceIndexByHandleForCount(traceMark, userInfo.webUsers.getUserId());
        JSONObject returnJsonObj = new JSONObject();
        returnJsonObj.put("waitUndertakeCount", StringUtils.isBlank(count) ? "0" : (count + ""));
        return returnJsonString(returnJsonObj);
    }

    public String fetchSystemControlTraceData(HiUserInfo userInfo, SortModel sortModel, PageModel pageModel, String companyId, String generalSearchCondition, String templateId, String templateCategoryId, Integer traceMark, Integer currentStatus, String startTime, String endTime, String docNum, String traceTitle, String createUserId, String createUserName, String focusFlg, String otherData) throws Exception {
        HtNGTrace05ViewTraceManager1 htNGTrace05TraceManager = new HtNGTrace05ViewTraceManager1(userInfo.languageType);
        if (!userInfo.superManagerFlg)
            companyId = userInfo.companyId;
        HibernatePage page = htNGTrace05TraceManager.fetchSystemTraceInstanceIndex(companyId, generalSearchCondition, templateId, templateCategoryId, currentStatus, traceMark, startTime, endTime, docNum, traceTitle, createUserId, sortModel, pageModel);
        JSONArray returnJsonAry = new JSONArray();
        Date today = new Date();
        if (page != null && page.objects != null) {
            List<Map> traceInstanceIndexList = page.objects;
            for (Map queryResultMap : traceInstanceIndexList) {
                if (queryResultMap == null)
                    continue;
                JSONObject jso = new JSONObject();
                jso.put("trace_instance_index_id", queryResultMap.get("trace_instance_index_id"));
                jso.put("trace_title", queryResultMap.get("trace_title"));
                jso.put("getcount", queryResultMap.get("getcount"));
                jso.put("trace_title_text", HtTrace00Constant.getTraceTitleTextValue(String.valueOf(queryResultMap.get("trace_title"))));
                Object traceStartTime = queryResultMap.get("trace_start_time");
                jso.put("trace_start_time", dateToStringUntilMinute(traceStartTime));
                jso.put("create_user_name", getUserNameList((new StringBuilder()).append(queryResultMap.get("create_user_id")).append("").toString()));
                jso.put("doc_num", queryResultMap.get("doc_num"));
                jso.put("trace_mark", queryResultMap.get("trace_mark"));
                jso.put("current_step_title", queryResultMap.get("current_step_title"));
                jso.put("path_title", queryResultMap.get("path_title"));
                jso.put("current_trace_state", queryResultMap.get("current_trace_state"));
                jso.put("external_modules_id", queryResultMap.get("external_modules_id"));
                jso.put("trace_priority", queryResultMap.get("trace_priority"));
                jso.put("secret_num", queryResultMap.get("secret_num"));
                jso.put("company_name", (new HiOaMainService(userInfo.languageType)).getDeptName(queryResultMap.get("company_id").toString()));
                jso.put("isHtmlFlg", Boolean.valueOf(isNotBlank((queryResultMap.get("html_file_name") == null) ? null : queryResultMap.get("html_file_name").toString())));
                jso.put("intervalDays", Integer.valueOf((traceStartTime == null) ? 0 : compareDays(today, (Date)traceStartTime)));
                jso.put("flag", queryResultMap.get("flag"));
                returnJsonAry.add(jso);
            }
        }
        return returnJsonAryString(makePagePackage((List)returnJsonAry, page));
    }

    public String getMyFileCount(HiUserInfo userInfo, Integer traceMark, String otherData) throws Exception {
        HtNGTrace05ViewTraceManager1 nGTrace05ViewTraceManager = new HtNGTrace05ViewTraceManager1(userInfo.languageType);
        String count = nGTrace05ViewTraceManager.fetchTraceInstanceIndexMyFileCount(traceMark, userInfo.webUsers.getUserId());
        JSONObject returnJsonObj = new JSONObject();
        returnJsonObj.put("myFileCount", StringUtils.isBlank(count) ? "0" : (count + ""));
        return returnJsonString(returnJsonObj);
    }

}
