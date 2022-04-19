package com.oa8000.appservice.httraced;


import com.cnpower.base.OaSystemConstant;
import com.oa8000.appservice.httrace.HtTraceHandleService;
import com.oa8000.hthr.hthr01.manager.HtHr01NewManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01CategoryManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01UserManager;
import com.oa8000.httrace.httrace03.manager.HtNGTrace03TemplateManager;
import com.oa8000.httrace.httrace05.manager.HtNGTrace05TraceBaseManager;
import com.oa8000.httrace.msgTools.HtTraceMsgTools;
import com.oa8000.proxy.base.HibernatePage;
import com.oa8000.proxy.base.TransactionManager;
import com.oa8000.proxy.comm.HiOaMainClass;
import com.oa8000.proxy.comm.HiOaMainService;
import com.oa8000.proxy.comm.HiOaPubSystemMsg;
import com.oa8000.proxy.comm.HiOaPubptSort;
import com.oa8000.proxy.comm.HiUserInfo;
import com.oa8000.proxy.comm.HtCategoryManager;
import com.oa8000.proxy.comm.OaPubDateManager;
import com.oa8000.proxy.comm.OaTools;
import com.oa8000.proxy.comm.PageModel;
import com.oa8000.proxy.comm.SortModel;
import com.oa8000.proxy.dao.HiMainDao;
import com.oa8000.proxy.dao.HiMainFetchDao;
import com.oa8000.proxy.db.HiDbClassDictDetail;
import com.oa8000.proxy.db.HiDbHrStaffInfo;
import com.oa8000.proxy.db.HiDbSystemRegister;
import com.oa8000.proxy.db.HiDbTraceCategory;
import com.oa8000.proxy.db.HiDbTraceDefaultRole;
import com.oa8000.proxy.db.HiDbTraceForwardDetail;
import com.oa8000.proxy.db.HiDbTraceHandoutViewList;
import com.oa8000.proxy.db.HiDbTraceInstanceIndex;
import com.oa8000.proxy.db.HiDbTracePathIndex;
import com.oa8000.proxy.db.HiDbTracePublish;
import com.oa8000.proxy.db.HiDbTraceTableCss;
import com.oa8000.proxy.db.HiDbTraceTempFormRight;
import com.oa8000.proxy.db.HiDbTraceTemplateDict;
import com.oa8000.proxy.db.HiDbTraceTimeout;
import com.oa8000.proxy.db.HiDbUserUser;
import com.oa8000.proxy.exception.OaException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

public class HtNGTrace05ViewTraceManager1 extends HtNGTrace05TraceBaseManager {

    public HtNGTrace05ViewTraceManager1(String languageType) {
        super(languageType);
    }


    private String getCurrentIndexId(String otherData) {
        if (StringUtils.isNotBlank(otherData))
            try {
                JSONObject obj = JSONObject.fromObject(otherData);
                if (obj.has("currentTraceInstanceIndexId"))
                    return obj.getString("currentTraceInstanceIndexId");
            } catch (Exception e) {
                return "";
            }
        return "";
    }

    public HibernatePage fetchTraceInstanceIndexByWait(String generalSearchCondition, String templateId, String templateCategoryId, Integer traceMark, String startTime, String endTime, String docNum, String traceTitle, String createUserId, String createUserName, String userId, String focusFlg, String otherData, boolean fakerMarkFlg, SortModel sortModel, PageModel pageModel) {
        boolean searchAllFlag = (traceMark.intValue() == -1);
        List<Object> paramList = new ArrayList();
        String indexTableName = getRealIndexTableName(fakerMarkFlg);
        StringBuilder sqlBuffer = new StringBuilder();
        sqlBuffer.append("SELECT * FROM ( SELECT ");
        sqlBuffer.append("  t1.trace_instance_index_id, ");
        sqlBuffer.append("  \"get_count\"(t1.trace_instance_index_id) as getcount, ");
        sqlBuffer.append("  t1.trace_limit_date, ");
        sqlBuffer.append("  t1.trace_title, ");
        sqlBuffer.append("  t1.create_user_id, ");
        sqlBuffer.append("  t1.attachment_list, ");
        sqlBuffer.append("  t1.protected_info, ");
        sqlBuffer.append("  t1.directory, ");
        sqlBuffer.append("  t1.trace_mark, ");
        sqlBuffer.append("  t1.html_file_name, ");
        sqlBuffer.append("  t3.user_name, ");
        sqlBuffer.append("  t1.trace_start_time, ");
        sqlBuffer.append("  t1.current_step_title, ");
        sqlBuffer.append("  t6.path_title, ");
        sqlBuffer.append("  t1.main_table_name, ");
        sqlBuffer.append("  t1.trace_template_dict_id, ");
        sqlBuffer.append("  t1.doc_num, ");
        sqlBuffer.append("  t2.concern_list_id, ");
        sqlBuffer.append("  t1.trace_priority, ");
        sqlBuffer.append("  ttd.more_info, ");
        sqlBuffer.append("  ttd.template_title, ");
        sqlBuffer.append("  t1.secret_num, ");
        sqlBuffer.append("  t1.flag, ");
        sqlBuffer.append("  t4.start_time, ");
        sqlBuffer.append("  t1.external_modules_id ");
        sqlBuffer.append(" FROM ");
        sqlBuffer.append(indexTableName).append(" t1 ");
        sqlBuffer.append(" LEFT JOIN trace_instance_path  t6 ");
        sqlBuffer.append(" ON t6.trace_instance_path_id = t1.current_step_id ");
        sqlBuffer.append(" LEFT JOIN concern_list  t2 ");
        sqlBuffer.append(" ON t1.trace_instance_index_id = t2.link_id ");
        sqlBuffer.append(" AND t2.create_user_id = ? , ");
        sqlBuffer.append(" trace_template_dict ttd , ");
        sqlBuffer.append(" user_user t3, ");
        sqlBuffer.append(" ( SELECT ")
                .append(" MAX(start_time) start_time, ")
                .append(" trace_instance_index_id, ")
                .append(" remark_state, ")
                .append(" trace_user_id ")
                .append(" FROM ")
                .append(" trace_instance_detail ")
                .append(" where ")
                .append(" trace_user_id = ? and remark_state = ? ")
                .append(" GROUP BY trace_instance_index_id, remark_state, trace_user_id ) t4 ");
        sqlBuffer.append(" WHERE t1.trace_instance_index_id = t4.trace_instance_index_id ");
        sqlBuffer.append(" AND ");
        sqlBuffer.append("  t1.create_user_id = t3.user_id ");
        sqlBuffer.append(" AND ttd.trace_template_dict_id = t1.trace_template_dict_id ");
        sqlBuffer.append(" AND t1.flag != ? ");
        sqlBuffer.append(" AND t1.current_trace_state = ? ");
        paramList.add(userId);
        paramList.add(userId);
        paramList.add("-3");
        paramList.add(Integer.valueOf(-1));
        paramList.add(Integer.valueOf(1));
        String currentIndexId = getCurrentIndexId(otherData);
        if (!"".equals(currentIndexId)) {
            sqlBuffer.append(" AND t1.trace_instance_index_id != ? ");
            paramList.add(currentIndexId);
        }
        if (traceMark.intValue() == -3) {
            sqlBuffer.append(" AND (t1.trace_mark = ? OR t1.trace_mark = ? OR t1.trace_mark = ?) ");
            paramList.add(Integer.valueOf(0));
            paramList.add(Integer.valueOf(1));
            paramList.add(Integer.valueOf(2));
        } else if (traceMark.intValue() == 10000) {
            sqlBuffer.append(" AND (t1.trace_mark = ? OR t1.trace_mark > ?) ");
            paramList.add(Integer.valueOf(0));
            paramList.add(Integer.valueOf(2));
        } else if (traceMark.intValue() == 10001) {
            sqlBuffer.append(" AND (t1.trace_mark = ? OR t1.trace_mark = ?) ");
            paramList.add(Integer.valueOf(2));
            paramList.add(Integer.valueOf(1));
        } else if (!searchAllFlag && traceMark.intValue() != -2 && (traceMark.intValue() <= 3 || traceMark.intValue() > 900)) {
            if (HtTraceHandleService.hasKey(otherData, "H5Flag")) {
                sqlBuffer.append(" AND (t1.trace_mark = ? or t1.trace_mark = ?) ");
                paramList.add(Integer.valueOf(203));
                paramList.add(traceMark);
            } else {
                sqlBuffer.append(" AND t1.trace_mark = ? ");
                paramList.add(traceMark);
            }
        } else if (traceMark.intValue() == -2) {
            sqlBuffer.append(" AND ").append(appendSearchSqlForPhoneFetch("t1.trace_mark", paramList));
        } else {
            sqlBuffer.append(" AND t1.trace_mark >= ? ");
            paramList.add(traceMark);
        }
        if (StringUtils.isNotBlank(generalSearchCondition)) {
            sqlBuffer.append(" AND (t1.trace_title LIKE ? ");
            paramList.add("%" + generalSearchCondition + "%");
            sqlBuffer.append(" OR t1.doc_num LIKE ? ");
            paramList.add("%" + generalSearchCondition + "%");
            sqlBuffer.append(" OR t3.user_name like ?) ");
            paramList.add(generalSearchCondition);
            if (StringUtils.isNotBlank(templateId)) {
                sqlBuffer.append(" AND t1.trace_template_dict_id = ? ");
                paramList.add(templateId);
            } else {
                sqlBuffer.append(" AND t1.trace_template_dict_id != ? ");
                paramList.add("f651b45a51ce4c279c2f0326");
            }
        } else {
            String attention = "";
            if (StringUtils.isNotBlank(otherData)) {
                JSONObject jsonObject = JSONObject.fromObject(otherData);
                if (jsonObject.containsKey("mark"))
                    attention = MapUtils.getString((Map)jsonObject, "mark");
            }
            if ("attention".equals(attention) && StringUtils.isNotBlank(templateId)) {
                sqlBuffer.append(" AND t1.trace_template_dict_id in (" + (new HiOaMainService()).translateInStr(templateId) + ")");
            } else if (StringUtils.isNotBlank(templateId)) {
                sqlBuffer.append(" AND t1.trace_template_dict_id in (" + (new HiOaMainService()).translateInStr(templateId) + ")");
            } else if ("attention".equals(attention)) {
                sqlBuffer.append(" AND t1.trace_template_dict_id = '0'");
            }
            if (StringUtils.isNotBlank(templateCategoryId)) {
                sqlBuffer.append(" AND t1.trace_template_category_id = ? ");
                paramList.add(templateCategoryId);
            }
            if (StringUtils.isNotBlank(startTime)) {
                sqlBuffer.append(" AND t1.trace_start_time >= ? ");
                paramList.add(startTime);
            }
            if (StringUtils.isNotBlank(endTime)) {
                sqlBuffer.append(" AND t1.trace_start_time <= ? ");
                paramList.add(endTime);
            }
            if (StringUtils.isNotBlank(traceTitle)) {
                sqlBuffer.append(" AND t1.trace_title LIKE ? ");
                paramList.add("%" + traceTitle + "%");
            }
            if (StringUtils.isNotBlank(docNum)) {
                sqlBuffer.append(" AND t1.doc_num LIKE ? ");
                paramList.add("%" + docNum + "%");
            }
            if (StringUtils.isNotBlank(createUserId)) {
                sqlBuffer.append(" AND t1.create_user_id = ? ");
                paramList.add(createUserId);
            } else if (StringUtils.isNotBlank(createUserName)) {
                sqlBuffer.append(" AND t3.user_name = ? ");
                paramList.add("%" + createUserName + "%");
            }
            if (StringUtils.isNotBlank(focusFlg) && "0".equals(focusFlg)) {
                sqlBuffer.append(" AND t2.concern_list_id is null ");
            } else if (StringUtils.isNotBlank(focusFlg) && "1".equals(focusFlg)) {
                sqlBuffer.append(" AND t2.concern_list_id is not null ");
            }
        }
        sqlBuffer.append(") t4 ");
        if (sortModel != null && StringUtils.isNotBlank(sortModel.orderName)) {
            if ("create_user_name".equals(sortModel.orderName))
                sortModel.orderName = "user_name";
            String orderStr = " ORDER BY " + sortModel.orderName + " " + sortModel.orderType;
            sqlBuffer.append(orderStr);
        } else {
            sqlBuffer.append(" ORDER BY start_time DESC , trace_start_time DESC");
        }

        return (new HiMainFetchDao()).fetchListBySQLToMap(sqlBuffer.toString(), paramList, pageModel.pageNum, pageModel.pageSize);
    }

    private String appendSearchSqlForPhoneFetch(String fetchColumn, List<Object> paramList) {
        if (paramList == null)
            paramList = new ArrayList();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" ( ");
        stringBuilder.append(fetchColumn).append(" = ? ");
        paramList.add(Integer.valueOf(0));
        stringBuilder.append(" or ");
        stringBuilder.append(fetchColumn).append(" = ? ");
        paramList.add(Integer.valueOf(3));
        stringBuilder.append(" or ( ");
        stringBuilder.append(fetchColumn).append(" > ? and ").append(fetchColumn).append(" < ? ");
        paramList.add(Integer.valueOf(100));
        paramList.add(Integer.valueOf(200));
        stringBuilder.append(" ) or ");
        stringBuilder.append(fetchColumn).append(" = ? ");
        paramList.add(Integer.valueOf(203));
        stringBuilder.append(" ) ");
        return stringBuilder.toString();
    }

    public HibernatePage fetchTraceInstanceIndexByManage(String companyId, String generalSearchCondition, String templateId, String templateCategoryId, Integer currentStatus, Integer traceMark, String startTime, String endTime, String docNum, String traceTitle, String createUserId, String createUserName, HiUserInfo userInfo, String focusFlg, String otherData, SortModel sortModel, PageModel pageModel) {
        String userId = userInfo.webUsers.getUserId();
        String deptId = userInfo.webUsers.getDeptId();
        List<Object> paramList = new ArrayList();
        StringBuilder sqlBuffer = new StringBuilder();
        sqlBuffer.append("SELECT * FROM ( SELECT ");
        sqlBuffer.append("  t1.trace_instance_index_id, ");
        sqlBuffer.append("  \"get_count\"(t1.trace_instance_index_id) as getcount, ");
        sqlBuffer.append("  t1.trace_title, ");
        sqlBuffer.append("  t1.trace_mark, ");
        sqlBuffer.append("  t3.user_name, ");
        sqlBuffer.append("  t1.trace_start_time, ");
        sqlBuffer.append("  t1.html_file_name, ");
        sqlBuffer.append("  t1.create_user_id, ");
        sqlBuffer.append("  t1.current_trace_state, ");
        sqlBuffer.append("  t1.doc_num, ");
        sqlBuffer.append("  t1.current_step_title, ");
        sqlBuffer.append("  t6.path_title, ");
        sqlBuffer.append("  t7.staff_company, ");
        sqlBuffer.append("  t1.trace_priority, ");
        sqlBuffer.append("  t1.secret_num, ");
        sqlBuffer.append("  t1.company_id, ");
        sqlBuffer.append("  t1.flag ");
        sqlBuffer.append(" FROM ");
        sqlBuffer.append("  trace_instance_index t1 ");
        sqlBuffer.append(" LEFT JOIN trace_instance_path  t6 ");
        sqlBuffer.append(" ON t6.trace_instance_path_id = t1.current_step_id ");
        sqlBuffer.append(" LEFT JOIN hr_staff_info  t7 ");
        sqlBuffer.append(" ON t1.create_user_id = t7.hr_staff_info_id, ");
        sqlBuffer.append(" user_user t3 ");
        sqlBuffer.append(" WHERE ");
        sqlBuffer.append("  t1.create_user_id = t3.user_id ");
        sqlBuffer.append(" AND t1.current_trace_state != ? ");
        sqlBuffer.append(" AND t1.current_trace_state != ? ");
        sqlBuffer.append(" AND t1.flag != ? ");
        paramList.add(Integer.valueOf(5));
        paramList.add(Integer.valueOf(-999));
        paramList.add(Integer.valueOf(-1));
        if (!userInfo.superManagerFlg) {
            sqlBuffer.append(" AND (t1.form_manager_user_list like ? ");
            sqlBuffer.append(" OR t1.form_manager_user_list like ?  ");
            sqlBuffer.append(" OR t1.form_manager_user_list like ?  ");
            sqlBuffer.append(" OR t1.form_manager_user_list like ? ) ");
            paramList.add("%;" + userId + ";%");
            paramList.add(";;");
            paramList.add("%;" + deptId + ";%");
            paramList.add("%*" + companyId + "*%");
        }
        String currentIndexId = getCurrentIndexId(otherData);
        if (!"".equals(getCurrentIndexId(otherData))) {
            sqlBuffer.append(" AND t1.trace_instance_index_id != ? ");
            paramList.add(currentIndexId);
        }
        if (StringUtils.isNotBlank(companyId)) {
            sqlBuffer.append(" AND t1.company_id = ? ");
            paramList.add(companyId);
        }
        if (traceMark.intValue() != -1 && (traceMark.intValue() <= 3 || traceMark.intValue() > 900)) {
            sqlBuffer.append(" AND t1.trace_mark = ? ");
            paramList.add(traceMark);
        }
        if (StringUtils.isNotBlank(generalSearchCondition)) {
            sqlBuffer.append(" AND (t1.trace_title LIKE ? ");
            paramList.add("%" + generalSearchCondition + "%");
            sqlBuffer.append(" OR t1.doc_num LIKE ?) ");
            paramList.add("%" + generalSearchCondition + "%");
        } else {
            if (StringUtils.isNotBlank(templateId)) {
                sqlBuffer.append(" AND t1.trace_template_dict_id = ? ");
                paramList.add(templateId);
            } else {
                sqlBuffer.append(" AND t1.trace_template_dict_id != ? ");
                paramList.add("f651b45a51ce4c279c2f0326");
            }
            if (StringUtils.isNotBlank(templateCategoryId)) {
                sqlBuffer.append(" AND t1.trace_template_category_id = ? ");
                paramList.add(templateCategoryId);
            }
            if (currentStatus.intValue() > -999) {
                sqlBuffer.append(" AND t1.current_trace_state = ? ");
                paramList.add(currentStatus);
            }
            if (StringUtils.isNotBlank(startTime)) {
                sqlBuffer.append(" AND t1.trace_start_time >= ? ");
                OaPubDateManager dt = new OaPubDateManager(startTime);
                paramList.add(dt.getDayStart());
            }
            if (StringUtils.isNotBlank(endTime)) {
                sqlBuffer.append(" AND t1.trace_start_time <= ? ");
                OaPubDateManager dt = new OaPubDateManager(endTime);
                paramList.add(dt.getDayEnd());
            }
            if (StringUtils.isNotBlank(traceTitle)) {
                sqlBuffer.append(" AND t1.trace_title LIKE ? ");
                paramList.add("%" + traceTitle + "%");
            }
            if (StringUtils.isNotBlank(docNum)) {
                sqlBuffer.append(" AND t1.doc_num LIKE ? ");
                paramList.add("%" + docNum + "%");
            }
            if (StringUtils.isNotBlank(createUserId)) {
                sqlBuffer.append(" AND t1.create_user_id = ? ");
                paramList.add(createUserId);
            } else if (StringUtils.isNotBlank(createUserName)) {
                sqlBuffer.append(" AND t3.user_name like ? ");
                paramList.add("%" + createUserName + "%");
            }
            if (StringUtils.isNotBlank(focusFlg) && "0".equals(focusFlg)) {
                sqlBuffer.append(" AND t2.concern_list_id is null ");
            } else if (StringUtils.isNotBlank(focusFlg) && "1".equals(focusFlg)) {
                sqlBuffer.append(" AND t2.concern_list_id is not null ");
            }
        }
        sqlBuffer.append(")  t4 ");
        if (sortModel != null && StringUtils.isNotBlank(sortModel.orderName)) {
            if ("create_user_name".equals(sortModel.orderName))
                sortModel.orderName = "user_name";
            String orderStr = " ORDER BY " + sortModel.orderName + " " + sortModel.orderType;
            sqlBuffer.append(orderStr);
        } else {
            sqlBuffer.append(" ORDER BY trace_start_time DESC");
        }
        return (new HiMainFetchDao()).fetchListBySQLToMap(sqlBuffer.toString(), paramList, pageModel.pageNum, pageModel.pageSize);
    }

    public HibernatePage fetchTraceInstanceIndexByHandle(String generalSearchCondition, String templateId, String templateCategoryId, Integer traceMark, String startTime, String endTime, String docNum, String traceTitle, String createUserId, String createUserName, String userId, String focusFlg, String finishFlg, SortModel sortModel, PageModel pageModel) {
        List<Object> paramList = new ArrayList();
        StringBuilder sqlBuffer = new StringBuilder();
        sqlBuffer.append("SELECT * FROM ( SELECT ");
        sqlBuffer.append("  t1.trace_instance_index_id, ");
        sqlBuffer.append("  \"get_undertakecount\"(t1.trace_instance_index_id,'"+userId+"') as getundertakecount, ");
        sqlBuffer.append("  t1.trace_title, ");
        sqlBuffer.append("  t1.current_trace_state, ");
        sqlBuffer.append("  t1.create_user_id, ");
        sqlBuffer.append("  t1.html_file_name, ");
        sqlBuffer.append("  t3.user_name, ");
        sqlBuffer.append("  t1.trace_start_time, ");
        sqlBuffer.append("  t6.undertake_time, ");
        sqlBuffer.append("  t1.doc_num, ");
        sqlBuffer.append("  t1.directory, ");
        sqlBuffer.append("  t1.trace_mark, ");
        sqlBuffer.append("  t1.trace_priority, ");
        sqlBuffer.append("  t1.secret_num ");
        sqlBuffer.append(" FROM ");
        sqlBuffer.append("  trace_instance_index t1 ");
        if ("1".equals(finishFlg)) {
            sqlBuffer.append(" INNER JOIN ( ");
        } else {
            sqlBuffer.append(" LEFT JOIN ( ");
        }
        sqlBuffer.append(" SELECT  t5.trace_instance_index_id,max(t5.undertake_time)  undertake_time ");
        sqlBuffer.append("  FROM trace_undertake_detail  t5  ");
        sqlBuffer.append(" WHERE t5.undertake_user_id = ? ");
        sqlBuffer.append(" GROUP BY t5.trace_instance_index_id) t6 ");
        sqlBuffer.append(" ON t1.trace_instance_index_id = t6.trace_instance_index_id, ");
        sqlBuffer.append(" user_user t3 ");
        sqlBuffer.append(" WHERE ");
        sqlBuffer.append("  t1.create_user_id = t3.user_id ");
        sqlBuffer.append(" AND t1.current_trace_state != ? ");
        if ("2".equals(finishFlg))
            sqlBuffer.append(" AND t6.undertake_time is NULL ");
        sqlBuffer.append(" AND t1.current_trace_state != ? ");
        sqlBuffer.append(" AND t1.flag != ? ");
        sqlBuffer.append(" AND t1.handler_list like ? ");
        paramList.add(userId);
        paramList.add(Integer.valueOf(5));
        paramList.add(Integer.valueOf(-999));
        paramList.add(Integer.valueOf(-1));
        paramList.add("%;" + userId + ";%");
        if (traceMark.intValue() != -1 && (traceMark.intValue() <= 3 || traceMark.intValue() > 900)) {
            sqlBuffer.append(" AND t1.trace_mark = ? ");
            paramList.add(traceMark);
        }
        if (StringUtils.isNotBlank(generalSearchCondition)) {
            sqlBuffer.append(" AND (t1.trace_title LIKE ? ");
            paramList.add("%" + generalSearchCondition + "%");
            sqlBuffer.append(" OR t1.doc_num LIKE ?) ");
            paramList.add("%" + generalSearchCondition + "%");
        } else {
            if (StringUtils.isNotBlank(templateId)) {
                sqlBuffer.append(" AND t1.trace_template_dict_id = ? ");
                paramList.add(templateId);
            }
            if (StringUtils.isNotBlank(templateCategoryId)) {
                sqlBuffer.append(" AND t1.trace_template_category_id = ? ");
                paramList.add(templateCategoryId);
            }
            if (StringUtils.isNotBlank(startTime)) {
                sqlBuffer.append(" AND t1.trace_start_time >= ? ");
                paramList.add(startTime);
            }
            if (StringUtils.isNotBlank(endTime)) {
                sqlBuffer.append(" AND t1.trace_start_time <= ? ");
                paramList.add(endTime);
            }
            if (StringUtils.isNotBlank(traceTitle)) {
                sqlBuffer.append(" AND t1.trace_title LIKE ? ");
                paramList.add("%" + traceTitle + "%");
            }
            if (StringUtils.isNotBlank(docNum)) {
                sqlBuffer.append(" AND t1.doc_num LIKE ? ");
                paramList.add("%" + docNum + "%");
            }
            if (StringUtils.isNotBlank(createUserId)) {
                sqlBuffer.append(" AND t1.create_user_id = ? ");
                paramList.add(createUserId);
            } else if (StringUtils.isNotBlank(createUserName)) {
                sqlBuffer.append(" AND t3.user_name like ? ");
                paramList.add("%" + createUserName + "%");
            }
        }
        sqlBuffer.append(") t4 ");
        if (sortModel != null && StringUtils.isNotBlank(sortModel.orderName)) {
            String orderStr = " ORDER BY " + sortModel.orderName + " " + sortModel.orderType;
            sqlBuffer.append(orderStr);
        } else {
            sqlBuffer.append(" ORDER BY trace_start_time DESC");
        }
        return (new HiMainFetchDao()).fetchListBySQLToMap(sqlBuffer.toString(), paramList, pageModel.pageNum, pageModel.pageSize);
    }
    public String fetchTraceInstanceIndexByHandleForCount(Integer traceMark, String userId) {
        List<Object> paramList = new ArrayList();
        StringBuilder sqlBuffer = new StringBuilder();
        sqlBuffer.append("select ");
        sqlBuffer.append(" count(t1.trace_instance_index_id) ");
        sqlBuffer.append(" from ");
        sqlBuffer.append(" trace_instance_index t1 ");
        sqlBuffer.append(" where ");
        sqlBuffer.append(" t1.handler_list like ? ");
        sqlBuffer.append(" AND \"get_undertakecount\"(t1.trace_instance_index_id,'"+userId+"')>0 ");
        sqlBuffer.append(" AND t1.current_trace_state != ? ");
        sqlBuffer.append(" AND t1.current_trace_state != ? ");
        sqlBuffer.append(" AND t1.flag != ? ");
        paramList.add("%;" + userId + ";%");
        paramList.add(Integer.valueOf(5));
        paramList.add(Integer.valueOf(-999));
        paramList.add(Integer.valueOf(-1));
        if (traceMark.intValue() != -1 && (traceMark.intValue() <= 3 || traceMark.intValue() > 900)) {
            sqlBuffer.append(" AND t1.trace_mark = ? ");
            paramList.add(traceMark);
        }

        Object o = (new HiMainDao()).findBySQL(sqlBuffer.toString(), paramList);
        if (o != null)
            return o.toString();
        return "0";
    }

    public HibernatePage fetchSystemTraceInstanceIndex(String companyId, String generalSearchCondition, String templateId, String templateCategoryId, Integer currentStatus, Integer traceMark, String startTime, String endTime, String docNum, String traceTitle, String createUserId, SortModel sortModel, PageModel pageModel) {
        StringBuilder sqlBuffer = new StringBuilder();
        sqlBuffer.append(" SELECT * ,  \"get_count\"(trace_instance_index_id) as getcount  FROM TRACE_INSTANCE_INDEX  WHERE 1=1  ");
        ArrayList<Object> paramList = new ArrayList();
        if (StringUtils.isNotBlank(companyId)) {
            sqlBuffer.append(" AND company_id = ? ");
            paramList.add(companyId);
        }
        if (traceMark != null && traceMark.intValue() != -1) {
            sqlBuffer.append(" AND TRACE_MARK = ? ");
            paramList.add(traceMark);
        }
        sqlBuffer.append(" AND CURRENT_TRACE_STATE > ? ");
        paramList.add(Integer.valueOf(-999));
        sqlBuffer.append(" AND FLAG > ? ");
        paramList.add(Integer.valueOf(-1));
        if (StringUtils.isNotBlank(generalSearchCondition)) {
            sqlBuffer.append(" AND (trace_title LIKE ? ");
            paramList.add("%" + generalSearchCondition + "%");
            sqlBuffer.append(" OR doc_num LIKE ?) ");
            paramList.add("%" + generalSearchCondition + "%");
        } else {
            if (StringUtils.isNotBlank(templateId)) {
                sqlBuffer.append(" AND trace_template_dict_id = ? ");
                paramList.add(templateId);
            }
            if (StringUtils.isNotBlank(templateCategoryId)) {
                sqlBuffer.append(" AND trace_template_category_id = ? ");
                paramList.add(templateCategoryId);
            }
            if (currentStatus.intValue() > -999) {
                sqlBuffer.append(" AND current_trace_state = ? ");
                paramList.add(currentStatus);
            }
            if (StringUtils.isNotBlank(startTime)) {
                sqlBuffer.append("  AND trace_start_time >= ? ");
                OaPubDateManager dt = new OaPubDateManager(startTime);
                paramList.add(dt.getDayStart());
            }
            if (StringUtils.isNotBlank(endTime)) {
                sqlBuffer.append(" AND trace_start_time <= ? ");
                OaPubDateManager dt = new OaPubDateManager(endTime);
                paramList.add(dt.getDayEnd());
            }
            if (StringUtils.isNotBlank(traceTitle)) {
                sqlBuffer.append(" AND trace_title LIKE ? ");
                paramList.add("%" + traceTitle + "%");
            }
            if (StringUtils.isNotBlank(docNum)) {
                sqlBuffer.append(" AND doc_num LIKE ? ");
                paramList.add("%" + docNum + "%");
            }
            if (StringUtils.isNotBlank(createUserId)) {
                sqlBuffer.append(" AND create_user_id = ? ");
                paramList.add(createUserId);
            }
        }
        if (sortModel != null && StringUtils.isNotBlank(sortModel.orderName)) {
            String orderStr = " ORDER BY " + sortModel.orderName + " " + sortModel.orderType;
            sqlBuffer.append(orderStr);
        } else {
            sqlBuffer.append(" ORDER BY trace_start_time DESC");
        }
        return (new HiMainFetchDao()).fetchListBySQLToMap(sqlBuffer.toString(), paramList, pageModel.pageNum, pageModel.pageSize);
    }

    public String fetchTraceInstanceIndexMyFileCount(Integer traceMark, String userId) {
        List<Object> paramList = new ArrayList();
        StringBuilder sqlBuffer = new StringBuilder();
        sqlBuffer.append("select ");
        sqlBuffer.append(" count(t1.trace_instance_index_id) ");
        sqlBuffer.append(" from ");
        sqlBuffer.append(" trace_instance_index t1 ");
        sqlBuffer.append(" where ");
        sqlBuffer.append(" t1.current_trace_state in ( ? , ? , ? , ?)  ");
        sqlBuffer.append(" AND t1.current_trace_state != ? ");
        sqlBuffer.append(" AND t1.current_trace_state != ? ");
        sqlBuffer.append(" AND t1.flag != ? ");
        sqlBuffer.append(" AND t1.create_user_id = ? ");
        paramList.add(Integer.valueOf(-1));
        paramList.add(Integer.valueOf(-2));
        paramList.add(Integer.valueOf(-3));
        paramList.add(Integer.valueOf(3));
        paramList.add(Integer.valueOf(5));
        paramList.add(Integer.valueOf(-999));
        paramList.add(Integer.valueOf(-1));
        paramList.add(userId);
        if (traceMark.intValue() != -1 && (traceMark.intValue() <= 3 || traceMark.intValue() > 900)) {
            sqlBuffer.append(" AND t1.trace_mark = ? ");
            paramList.add(traceMark);
        }
        Object o = (new HiMainDao()).findBySQL(sqlBuffer.toString(), paramList);
        if (o != null)
            return o.toString();
        return "0";
    }

}
