package com.oa8000.httrace.httrace01.manager;

import com.google.gson.Gson;
import com.oa8000.httraceform.httraceform02.TraceField;
import com.oa8000.httraceform.httraceform04.MacroViewOtherParam;
import com.oa8000.httraceform.httraceform04.MarcoViewQueryField;
import com.oa8000.proxy.comm.OaTools;
import com.oa8000.proxy.db.HiDbTraceDefineField;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import net.sf.json.JSONObject;
public abstract class HtTrace01DM {
    public static String getSelectTableSQL(String tableName) {
        return "SELECT count(*) as recordcount FROM user_tables WHERE table_name = '" + tableName.toUpperCase() + "'";
    }

    public static String getSelectTableFieldSQL(String tableName, String fieldName) {
        return "SELECT * FROM USER_TAB_COLUMNS WHERE TABLE_NAME = '" + tableName.toUpperCase() + "' AND COLUMN_NAME = '" + fieldName.toUpperCase() + "'";
    }

    public static String getCreateTableSQL(List<HiDbTraceDefineField> fieldAry, String tableName) {
        StringBuffer buf = new StringBuffer();
        buf.append("CREATE TABLE");
        buf.append(" ");
        buf.append(tableName);
        buf.append(" ");
        buf.append("(");
        buf.append("table_key varchar2(24) default '' NOT NULL, ");
        int size = fieldAry.size();
        for (int i = 0; i < size; i++) {
            HiDbTraceDefineField field = fieldAry.get(i);
            buf.append(field.getCreateFieldName());
            buf.append(" ").append(getFieldType(field)).append(" ");
            buf.append("default NULL, ");
        }
        buf.append("PRIMARY KEY (table_key)");
        buf.append(")");
        return buf.toString();
    }

    public static String getCreateHiddenTableSQL(List<HiDbTraceDefineField> fieldAry, String tableName) {
        StringBuffer buf = new StringBuffer();
        buf.append("CREATE TABLE");
        buf.append(" ");
        buf.append(tableName);
        buf.append(" ");
        buf.append("(");
        buf.append("table_key varchar2(24) default '' NOT NULL, ");
        buf.append("trace_instance_index_id varchar2(24) default NULL, ");
        buf.append("trace_state integer default NULL, ");
        buf.append("main_table_key varchar(24) default NULL, ");
        buf.append("attachment_list varchar(4000) default NULL, ");
        buf.append("trace_template_dict_id varchar2(24) default NULL, ");
        buf.append("doc_file_id varchar2(24) default NULL, ");
        buf.append("list_index integer default NULL, ");
        buf.append("list_order integer default NULL, ");
        buf.append("trace_flag1 integer default NULL, ");
        buf.append("trace_flag2 integer default NULL, ");
        buf.append("trace_flag3 integer default NULL, ");
        buf.append("trace_flag varchar2(255) default NULL, ");
        buf.append("trace_user_id varchar2(4000) default NULL, ");
        buf.append("trace_step varchar2(255) default NULL, ");
        buf.append("create_user_id varchar2(24) default NULL, ");
        buf.append("create_date date default NULL, ");
        buf.append("update_user_id varchar2(24) default NULL, ");
        buf.append("update_date date default NULL, ");
        buf.append("can_edit integer default NULL, ");
        buf.append("can_delete integer default NULL, ");
        int size = fieldAry.size();
        for (int i = 0; i < size; i++) {
            HiDbTraceDefineField field = fieldAry.get(i);
            buf.append(field.getCreateFieldName());
            buf.append(" ").append(getFieldType(field)).append(" ");
            buf.append("default NULL, ");
        }
        buf.append("PRIMARY KEY (table_key)");
        buf.append(")");
        return buf.toString();
    }

    private static String getFieldType(HiDbTraceDefineField defineField) {
        int type = (defineField.getFieldType() == null) ? 2 : defineField.getFieldType().intValue();
        if (defineField.getCreateTableMark().intValue() == 0 && !";SYS_SEL_DEPTS;SYS_SEL_PERSONS;SYS_SEL_HRROWS;".contains(";" + defineField.getShowType() + ";"))
            type = 2;
        if (type == 0)
            return "int";
        if (type == 1)
            return "decimal(18,4)";
        if (type == 3 || type == 4 || type == 7)
            return "date";
        if (type == 5)
            return "varchar2(4000)";
        StringBuffer sb = new StringBuffer();
        sb.append("varchar2(");
        Number fieldLength = defineField.getFieldLength();
        if (fieldLength == null || fieldLength.intValue() == 0) {
            sb.append("255");
        } else {
            sb.append(fieldLength);
        }
        sb.append(")");
        return sb.toString();
    }

    public static String[] getAddTableFieldSQL(List<HiDbTraceDefineField> fieldAry, String tableName) {
        boolean oldTableFlag = false;
        if (tableName.endsWith("_hidden") && (new HtTrace01SqlProcess()).checkTableFieldExist(tableName, "doc_file_id") == 0)
            oldTableFlag = true;
        if ((fieldAry == null || fieldAry.size() == 0) && !oldTableFlag)
            return null;
        int count = oldTableFlag ? (fieldAry.size() + 3) : fieldAry.size();
        String[] sql = new String[count];
        for (int i = 0; i < count; i++) {
            HiDbTraceDefineField field = fieldAry.get(i);
            StringBuffer buf = new StringBuffer();
            buf.append("ALTER TABLE ");
            buf.append(tableName);
            buf.append(" ADD ");
            buf.append(field.getCreateFieldName());
            buf.append(" ").append(getFieldType(field));
            sql[i] = buf.toString();
        }
        if (oldTableFlag) {
            StringBuffer buf = new StringBuffer();
            buf.append("ALTER TABLE ");
            buf.append(tableName);
            buf.append(" ADD attachment_list varchar2(4000) ");
            sql[count] = buf.toString();
            StringBuffer buf1 = new StringBuffer();
            buf1.append("ALTER TABLE ");
            buf1.append(tableName);
            buf1.append(" ADD trace_template_dict_id varchar2(24) ");
            sql[count + 1] = buf1.toString();
            StringBuffer buf2 = new StringBuffer();
            buf2.append("ALTER TABLE ");
            buf2.append(tableName);
            buf2.append(" ADD doc_file_id varchar2(24) ");
            sql[count + 2] = buf2.toString();
        }
        return sql;
    }

    public static String[] getModifyTableSQL(List<HiDbTraceDefineField> fieldAry, String tableName) {
        if (fieldAry == null || fieldAry.size() == 0)
            return null;
        int size = fieldAry.size();
        String[] sql = new String[size];
        Element element = OaTools.readXMLFile("Config.xml");
        String domesticDbFlg = StringUtils.defaultIfEmpty(OaTools.getItemValue(element, "domesticDbFlg"), "0");
        for (int i = 0; i < size; i++) {
            HiDbTraceDefineField field = fieldAry.get(i);
            if ("0".equals(domesticDbFlg)) {
                StringBuffer sb = new StringBuffer();
                sb.append("ALTER TABLE ");
                sb.append(tableName);
                sb.append(" MODIFY ");
                sb.append(field.getCreateFieldName());
                sb.append(" ").append(getFieldType(field));
                sql[i] = sb.toString();
            } else if ("1".equals(domesticDbFlg)) {
                StringBuffer sb = new StringBuffer();
                sb.append("ALTER TABLE ");
                sb.append(tableName);
                sb.append(" ALTER COLUMN ");
                sb.append(field.getCreateFieldName());
                sb.append(" type ").append(getFieldType(field));
                sql[i] = sb.toString();
            }
        }
        return sql;
    }

    public static String selectSqlView(String fixedSql, List queryAry, String extQueryCondition, HashMap orderAry) {
        StringBuffer buf = new StringBuffer();
        buf.append("SELECT * FROM (");
        buf.append(fixedSql);
        boolean extQueryFlg = (extQueryCondition != null && !"".equals(extQueryCondition.trim()));
        if (extQueryFlg) {
            boolean whereFlg = (fixedSql.toLowerCase().indexOf("where") > -1);
            if (whereFlg) {
                buf.append(" AND (").append(extQueryCondition).append(") ");
            } else {
                buf.append(" WHERE ").append(extQueryCondition);
            }
        }
        buf.append(") ");
        boolean queryFlg = (queryAry != null && queryAry.size() > 0);
        if (queryFlg)
            buf.append(" WHERE ").append(getQueryArySql(queryAry));
        buf.append(getOrderArySql(orderAry));
        return buf.toString();
    }

    public static String getOrderArySql(HashMap orderAry) {
        if (orderAry == null || orderAry.size() == 0)
            return "";
        Set key = orderAry.keySet();
        StringBuffer orderBuf = new StringBuffer();
        orderBuf.append(" ORDER BY ");
        int i = 0;
        for (Iterator it = key.iterator(); it.hasNext(); ) {
            if (i != 0)
                orderBuf.append(", ");
            orderBuf.append((String)orderAry.get(it.next()));
            i++;
        }
        return orderBuf.toString();
    }

    public static String getQueryArySql(List<MarcoViewQueryField> queryAry) {
        Gson gson = new Gson();
        if (queryAry == null || queryAry.size() == 0)
            return null;
        int queryCount = queryAry.size();
        String lastMode = null;
        StringBuffer queryBuf = new StringBuffer();
        for (int i = 0; i < queryCount; i++) {
            Object obj= queryAry.get(i);
            MarcoViewQueryField dict =null;
            dict = queryAry.get(i);
            String fieldName =dict.getFieldName();// (String)dict.get("fieldName");
            if (fieldName != null) {
                String fieldValue =(String) dict.getValue();// (String)dict.get("value");
                if (fieldValue != null) {
                    String fieldType =dict.getType();// (String)dict.get("type");
                    if (fieldType != null) {
                        String condition =dict.getCondition();// (String)dict.get("condition");
                        if (condition != null) {
                            String linkMode =dict.getLink();// (String)dict.get("link");
                            if (linkMode == null || "".equals(linkMode))
                                linkMode = "AND";
                            if (lastMode != null)
                                queryBuf.append(" ").append(lastMode).append(" ");
                            lastMode = linkMode;
                            String l_Brackets =dict.getL_Brackets();// (String)dict.get("l_Brackets");
                            if (l_Brackets==null) l_Brackets="";
                            queryBuf.append(l_Brackets);
                            String r_Brackets =dict.getR_Brackets();// (String)dict.get("r_Brackets");
                            if (r_Brackets==null) r_Brackets="";
                            condition = condition.toUpperCase();
                            if ("IS".equals(condition) || "IS NOT".equals(condition)) {
                                queryBuf.append(fieldName).append(" ").append(condition).append(" NULL");
                                queryBuf.append(r_Brackets);
                            } else {
                                int type = HtTrace01SqlManager.getType(fieldType);
                                switch (type) {
                                    case 0:
                                    case 1:
                                        if ("LIKE".equals(condition))
                                            condition = "=";
                                        queryBuf.append(fieldName).append(" ").append(condition).append(" ? ");
                                        queryBuf.append(r_Brackets);
                                        break;
                                    case 3:
                                    case 4:
                                    case 7:
                                        if ("LIKE".equals(condition)) {
                                            queryBuf.append(fieldName).append(" >= ? AND ").append(fieldName).append(" <= ?");
                                            queryBuf.append(r_Brackets);
                                            break;
                                        }
                                        queryBuf.append(fieldName).append(" ").append(condition).append(" ? ");
                                        queryBuf.append(r_Brackets);
                                        break;
                                    default:
                                        if ((";" + condition + ";").contains(";>;>=;<;<=;"))
                                            condition = "LIKE";
                                        queryBuf.append(fieldName).append(" ").append(condition).append(" ?");
                                        queryBuf.append(r_Brackets).append("");
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("**********************************************************************");
        System.out.println(queryBuf.toString());
        System.out.println("**********************************************************************");
        return queryBuf.toString();
    }

    public static String getSelectData(String tableName, List queryAry, List fieldAry, HashMap orderDict, String extQueryCondition, int traceDataFilterFlag) {
        return HtTrace01MYSQL.getSelectData(tableName, queryAry, fieldAry, orderDict, extQueryCondition, traceDataFilterFlag);
    }

    public static String getSelectData(String tableName, List queryAry, List fieldAry, HashMap orderDict, MacroViewOtherParam param, String extQueryCondition, int traceDataFilterFlag) {
        return HtTrace01MYSQL.getSelectData(tableName, queryAry, fieldAry, orderDict, param, extQueryCondition, traceDataFilterFlag);
    }

    public static String getData(String tableName, boolean mainFlg, boolean orderFlg) {
        if (tableName == null)
            return null;
        StringBuilder buf = new StringBuilder();
        buf.append("SELECT ").append(tableName).append(".* ");
        if (!mainFlg)
            buf.append(", ").append(tableName).append("_hidden.list_index ");
        buf.append(" FROM ").append(tableName).append(", ").append(tableName).append("_hidden ");
        buf.append(" WHERE ").append(tableName).append(".table_key=").append(tableName).append("_hidden.main_table_key ");
        buf.append(" AND ").append(tableName).append("_hidden.trace_instance_index_id=?");
        if (!mainFlg && orderFlg)
            buf.append(" ORDER BY ").append(tableName).append("_hidden.list_order");
        if (!mainFlg && !orderFlg)
            buf.append(" ORDER BY ").append(tableName).append("_hidden.list_index");
        return buf.toString();
    }

    public static String getSelectMainTableKey(String tableName, String indexId) {
        if (indexId == null || "".equals(indexId))
            return null;
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT table_key, main_table_key FROM ");
        sb.append(tableName.toLowerCase()).append("_hidden");
        sb.append(" WHERE trace_instance_index_id = ");
        sb.append("'").append(indexId).append("'");
        return sb.toString();
    }

    public static String getInsertData(String tableName, List fieldAry) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(tableName);
        sb.append(" (");
        sb.append("table_key");
        int size = fieldAry.size();
        for (Object obj : fieldAry) {
            if (obj instanceof HiDbTraceDefineField) {
                HiDbTraceDefineField field = (HiDbTraceDefineField)obj;
                sb.append(", ").append(field.getCreateFieldName());
                continue;
            }
            if (obj instanceof TraceField) {
                TraceField field = (TraceField)obj;
                sb.append(", ").append(field.getFieldDefine().getCreateFieldName());
            }
        }
        sb.append(") VALUES (");
        sb.append("?");
        for (int i = 0; i < size; i++)
            sb.append(", ?");
        sb.append(")");
        return sb.toString();
    }

    public static String getInsertHiddenData(String tableName, List fieldAry, boolean mainTableFlg, boolean orderFlg) {
        boolean paramFlag = false;
        if ((new HtTrace01SqlProcess()).checkTableFieldExist(tableName, "doc_file_id") == 1)
            paramFlag = true;
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(tableName);
        sb.append(" (");
        sb.append("table_key");
        if (!mainTableFlg)
            sb.append(", list_index");
        if (!mainTableFlg && orderFlg)
            sb.append(", list_order");
        sb.append(", trace_instance_index_id");
        sb.append(", trace_state");
        sb.append(", main_table_key");
        sb.append(", trace_user_id");
        sb.append(", trace_step");
        sb.append(", create_date");
        sb.append(", create_user_id");
        if (mainTableFlg && paramFlag) {
            sb.append(", trace_template_dict_id");
            sb.append(", attachment_list");
            sb.append(", doc_file_id");
        }
        StringBuilder paramBf = new StringBuilder();
        for (Object obj : fieldAry) {
            HiDbTraceDefineField field = null;
            if (obj instanceof HiDbTraceDefineField) {
                field = (HiDbTraceDefineField)obj;
            } else if (obj instanceof TraceField) {
                field = ((TraceField)obj).getHiddenFieldDefine();
            }
            if (field == null)
                continue;
            sb.append(", ").append(field.getCreateFieldName());
            paramBf.append(", ?");
        }
        sb.append(") VALUES (");
        sb.append("?").append(", ?").append(", ?").append(", ?");
        sb.append(", ?").append(", ?");
        sb.append(", ?");
        sb.append(", ?");
        if (!mainTableFlg)
            sb.append(", ?");
        if (!mainTableFlg && orderFlg)
            sb.append(", ?");
        if (mainTableFlg && paramFlag)
            sb.append(", ?").append(", ?").append(", ?");
        sb.append(paramBf);
        sb.append(")");
        return sb.toString();
    }

    public static String getUpdateData(String tableName, List fieldAry) {
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ").append(tableName.toLowerCase()).append(" SET ");
        if (fieldAry != null)
            for (int i = 0; i < fieldAry.size(); i++) {
                Object obj = fieldAry.get(i);
                HiDbTraceDefineField field = null;
                if (obj instanceof HiDbTraceDefineField) {
                    field = (HiDbTraceDefineField)obj;
                } else if (obj instanceof TraceField) {
                    field = ((TraceField)obj).getFieldDefine();
                }
                if (field != null) {
                    if (i > 0)
                        sb.append(", ");
                    sb.append(field.getCreateFieldName()).append(" = ? ");
                }
            }
        sb.append(" WHERE table_key = ? ");
        return sb.toString();
    }

    public static String getUpdateHiddenData(String tableName, List fieldAry, boolean mainTableFlg, boolean orderFlg) {
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ").append(tableName.toLowerCase()).append(" SET ");
        if (!mainTableFlg)
            sb.append("list_index = ?, ");
        if (!mainTableFlg && orderFlg)
            sb.append("list_order = ?, ");
        sb.append("trace_instance_index_id = ? ");
        sb.append(", trace_state = ? ");
        sb.append(", main_table_key = ? ");
        sb.append(", trace_user_id = ? ");
        sb.append(", trace_step = ? ");
        sb.append(", update_date = ? ");
        if (mainTableFlg && (
                new HtTrace01SqlProcess()).checkTableFieldExist(tableName, "doc_file_id") == 1) {
            sb.append(", trace_template_dict_id = ? ");
            sb.append(", attachment_list = ? ");
            sb.append(", doc_file_id = ? ");
        }
        if (fieldAry != null)
            for (Object obj : fieldAry) {
                HiDbTraceDefineField field = null;
                if (obj instanceof HiDbTraceDefineField) {
                    field = (HiDbTraceDefineField)obj;
                } else if (obj instanceof TraceField) {
                    field = ((TraceField)obj).getHiddenFieldDefine();
                }
                if (field == null)
                    continue;
                sb.append(", ").append(field.getCreateFieldName()).append(" = ? ");
            }
        sb.append(" WHERE table_key = ? ");
        return sb.toString();
    }

    public static String getDeleteData(String tableName, String mainTableKey) {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append(tableName);
        sb.append(" WHERE table_key = ");
        sb.append("'").append(mainTableKey).append("'");
        return sb.toString();
    }

    public static String getDeleteHiddenData(String tableName, String indexId) {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append(tableName.toLowerCase());
        sb.append(" WHERE trace_instance_index_id = ");
        sb.append("'").append(indexId).append("'");
        return sb.toString();
    }

    public static String getDropTable(String tableName) {
        StringBuilder sb = new StringBuilder();
        sb.append("DROP TABLE ");
        sb.append(tableName);
        return sb.toString();
    }

    public static String getSelectSql(String tableName, List<HiDbTraceDefineField> fieldAry) {
        if (fieldAry == null || fieldAry.size() == 0)
            return null;
        StringBuilder selSql = new StringBuilder();
        selSql.append("SELECT DISTINCT ").append(tableName).append("_hidden.trace_instance_index_id ");
        selSql.append("FROM ").append(tableName).append(", ").append(tableName).append("_hidden ");
        selSql.append("WHERE ").append(tableName).append(".table_key = ").append(tableName).append("_hidden.main_table_key ");
        int size = fieldAry.size();
        for (int i = 0; i < size; i++) {
            HiDbTraceDefineField field = fieldAry.get(i);
            selSql.append(" AND (").append(tableName).append(".").append(field.getCreateFieldName());
            boolean likeFlg = (field.getFieldType() != null && (field.getFieldType().intValue() == 2 || field.getFieldType().intValue() == 5));
            selSql.append(likeFlg ? " like ? " : " = ? ");
            if (field.getHiddenFieldName() != null && !"".equals(field.getHiddenFieldName()))
                selSql.append(" OR ").append(tableName).append("_hidden.").append(field.getHiddenFieldName())
                        .append(likeFlg ? " like ? " : " = ? ");
            selSql.append(")");
        }
        return selSql.toString();
    }

    public static String convertSQLStatement(String strSQL) {
        strSQL = strSQL.replace("NOW()", "sysdate");
        return strSQL;
    }

    public static String getDeleteDataByKey(String tableName) {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append(tableName.toLowerCase());
        sb.append(" WHERE table_key = ?");
        return sb.toString();
    }

    public static String getDeleteHiddenDataByKey(String tableName) {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append(tableName.toLowerCase() + "_hidden");
        sb.append(" WHERE main_table_key = ?");
        return sb.toString();
    }

    public static String getSqlForBatchUpdateTemplateId(String tableName, String traceTemplateDictId) {
        return HtTrace01ORACLE.getSqlForBatchUpdateTemplateId(tableName, traceTemplateDictId);
    }

    public static String[] getSqlForAddTemplateColumn(String tableName) {
        return HtTrace01ORACLE.getSqlForAddTemplateColumn(tableName);
    }
}
