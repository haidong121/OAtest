package com.oa8000.appservice.htentrance;

import com.cnpower.base.OaBaseSysInfo;
import com.cnpower.base.OaBaseTools;
import com.oa8000.appservice.comm.HtLoginInitManager;
import com.oa8000.comm.JedisService;
import com.oa8000.htsystemSetting.HtSystemSetting01Manager;
import com.oa8000.htsystemSetting.verifyCodeUtils;
import com.oa8000.pageopen.tool.PageOpenTools;
import com.oa8000.proxy.comm.HiOaPubSystemMsg;
import com.oa8000.proxy.comm.HiUserInfo;
import com.oa8000.proxy.comm.OaTools;
import com.oa8000.proxy.comm.SortModel;
import com.oa8000.proxy.dao.HiMainDao;
import com.oa8000.proxy.db.HiDbManagerInternalIp;
import com.oa8000.proxy.db.HiDbThreadLog;
import com.oa8000.proxy.db.HiDbUserSetting;
import com.oa8000.proxy.exception.OaException;
import com.oa8000.secretfree.server.SecretFreeServer;
import com.oa8000.server.hthr.HtHrServer;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

public class HtEntranceContent {
    public static String LOGIN_SERVICE = "DoLoginService";

    public static String LOGIN_MOBILE = "mobile";

    public static String LOGIN_INIT_SERVICE = "DoLoginInitService";

    public static String COMMON_SERVICE = "commService";

    public static String DINGDING_SERVICE = "dingDingService";

    public static String WEIXIN_SERVICE = "weiXinService";

    public static String CREATE_TRACE_LOGIN_METHOD_WEIXIN = "weiXinCreateTraceLogin";

    public static String GET_QRCODE = "getQrcode";

    public static String OPEN_QRCODE_CONN = "openQrcodeConn";

    public static String CLOSE_QRCODE_CONN = "closeQrcodeConncet";

    public static String GET_QRCODE_SERVICE = "getQrcodeService";

    public static String LOGIN_METHOD = "DoLogin";

    public static String LOGIN_METHOD_DINDING = "dingDingLogin";

    public static String LOGIN_METHOD_WEIXIN = "weiXinLogin";

    public static String LOGIN_METHOD_INIT = "DoLoginInit";

    public static String LOGINOUT_METHOD = "removeOnlineUser";

    public static String CHANGE_STYLE_METHOD = "changeStyle";

    public static String LOGIN_METHOD_AIM = "DoGetAuthImage";

    public static String LOGIN_METHOD_SMS = "DoGetSMS";

    public static String LOGIN_METHOD_CHECK_SMS = "DoCheckSMSCode";

    public static boolean isImLoginFlg = false;

    public static String methonMonitorUserId;

    public static int loginInvalidateTime = -1;

    public static String jsVersion;

    public static String DO_CODE_FLG = "1";

    public static String USERINFOMARK = "e_userInf_";

    public static String USERINFOMARKIM = "im_userInf_";

    public static String LOGINUUIDMARK = "e_uuid_";

    public static String LOGINCHECKMARK = "e_logincheck_";

    public static String LOGINERRMARK = "e_loginerr_";

    public static String LOGINCHECKSMSMARK = "e_loginCheckSMS_";

    public static String PHONE_CODE_SPLIT_MARK = "@@PHONE_NUMBER";

    public static String DINGDINGMAPMARK = "e_dingding_";

    public static String WEIXINMAPMARK = "e_weixin_";

    public static String SPECIALMAPMARK = "e_special_";

    public static int RANDOM_CHECK_LIMIT_TIME = 3;

    public static int PHONE_CODE_TIME_OUT = 0;

    public static int PHONE_CODE_WRONG = 1;

    public static int PHONE_CODE_PASS = 2;

    public static int PHONE_CODE_ERROR = 3;

    public static Properties loginProps;

    static {
        Element element = OaTools.readXMLFile("Config.xml");
        String doCodeMark = OaTools.getItemValue(element, "doCodeMark");
        if ("2".equals(doCodeMark))
            DO_CODE_FLG = "2";
        String time = OaTools.getItemValue(element, "loginInvalidateTime");
        if (time != null)
            loginInvalidateTime = Integer.parseInt(time);
        methonMonitorUserId = getWriteSpLogFlag();
        jsVersion = StringUtils.defaultIfEmpty(OaTools.getItemValue(element, "jsVersion"), "99");
    }

    public static String getVersionToken() {
        return jsVersion;
    }

    public static String getServiceClassName(String serviceName) {
        return HtEntranceProperties.getInstance().getServiceClassName(serviceName);
    }

    public static String jsonString(String s) {
        StringBuilder finalStr = new StringBuilder();
        String[] temp = s.split(",");
        int n = temp.length;
        for (int i = 0; i < n; i++) {
            String str = temp[i].trim();
            int first = str.indexOf("'");
            int last = str.lastIndexOf("'");
            String content = str;
            if (first > -1 && last > -1 && last > first) {
                content = str.substring(first, last);
                content = content.replaceAll("'", "");
                content = content.replaceAll("\n", "#@#");
                content = "'" + content + "'";
                int num = i;
                while (++num < n) {
                    String con = temp[num].trim();
                    if (!con.startsWith("'"))
                        try {
                            Integer.parseInt(con);
                        } catch (NumberFormatException e) {
                            if (!"true".equals(con) && !"false".equals(con))
                                content = content + con;
                        }
                }
                i = num - 1;
                content = content + ",";
            } else {
                content = content + ",";
            }
            finalStr.append(content);
        }
        if (finalStr.toString().endsWith(","))
            finalStr = new StringBuilder(finalStr.substring(0, finalStr.length() - 1));
        if (!finalStr.toString().startsWith("["))
            finalStr.insert(0, "[");
        if (!finalStr.toString().endsWith("]"))
            finalStr.append("]");
        return finalStr.toString();
    }

    public static char[] oaEncode(char[] str, int n) {
        int len = str.length;
        char[] miwen = new char[len];
        for (int i = 0; i < len; i++)
            miwen[i] = (char)(str[i] + n);
        return miwen;
    }

    public static char[] oaDecode(char[] miwen, int n) {
        int len = miwen.length;
        char[] str = new char[len];
        for (int i = 0; i < len; i++)
            str[i] = (char)(miwen[i] - n);
        return str;
    }

    public static boolean sql_inj(String str) {
        String inj_str = "'|(|)|*|%|;|-|+|,|;";
        String[] inj_stra = OaTools.split(inj_str, "|");
        for (String anInj_stra : inj_stra) {
            if (!StringUtils.isBlank(anInj_stra) &&
                    str.contains(anInj_stra))
                return true;
        }
        return false;
    }

    public static String shaEncode(String inStr) {
        MessageDigest sha;
        try {
            sha = MessageDigest.getInstance("SHA");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        byte[] byteArray = inStr.getBytes(StandardCharsets.UTF_8);
        byte[] md5Bytes = sha.digest(byteArray);
        StringBuilder hexValue = new StringBuilder();
        for (byte md5Byte : md5Bytes) {
            int val = md5Byte & 0xFF;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    public static String getRandomCode(boolean isImLoginFlg) throws OaException {
        Random r = new Random(System.currentTimeMillis());
        try {
            String imMark = "";
            if (isImLoginFlg)
                imMark = "IM_";
            return imMark + shaEncode(r.nextInt() + "");
        } catch (Exception e) {
            e.printStackTrace();
            throw new OaException("MSGcommon00018", "随机code生成失败。");
        }
    }

    public static void invokeParams(HttpServletRequest request) {
        Enumeration<String> e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            if (key.trim().startsWith("{")) {
                JSONObject jso = JSONObject.fromObject(key);
                Set<String> s = jso.keySet();
                for (String _s : s)
                    request.setAttribute(_s, jso.get(_s).toString());
                continue;
            }
            request.setAttribute(key, request.getParameterValues(key));
        }
    }

    public static String getAttr(String params) {
        if (params == null)
            return null;
        params = params.replace("x6x;", "+");
        params = params.replace("x7x;", "=");
        return OaTools.decodeBase64String(params);
    }

    public static void initSortModel(String sortModel, Class[] classes, Object[] os) {
        if (sortModel != null) {
            classes[1] = SortModel.class;
            SortModel sortModel1 = new SortModel(sortModel);
            if (StringUtils.isBlank(sortModel1.orderName)) {
                os[1] = null;
            } else {
                os[1] = sortModel1;
            }
        }
    }

    public static String getProperty(String key) {
        getLoginProps();
        return loginProps.getProperty(key);
    }

    public static void getLoginProps() {
        if (loginProps == null) {
            loginProps = new Properties();
            try {
                String propertiesFilePath = getPropertiesFilePath();
                loginProps.load(new FileInputStream(propertiesFilePath));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static String getPropertiesFilePath() {
        String propertiesFilePath;
        OaBaseSysInfo os = OaBaseTools.createOaBaseSysInfo();
        if (OaBaseSysInfo.getSystemPath().endsWith("/")) {
            propertiesFilePath = OaBaseSysInfo.getSystemPath() + "login.properties";
        } else {
            propertiesFilePath = OaBaseSysInfo.getSystemPath() + "/login.properties";
        }
        return propertiesFilePath;
    }

    public static String userLan(HiDbUserSetting us, String language, String ip) throws OaException {
        if (us.getAuthInsideNetFlag() == null || us.getAuthInsideNetFlag().intValue() != 1)
            return null;
        HiOaPubSystemMsg os = new HiOaPubSystemMsg(language);
        if (ip == null)
            return os.message("MSGERR0316", "用户IP无法确认。");
        if (ip.equals("127.0.0.1"))
            return null;
        List ipAry = (new HiMainDao()).findList("ManagerInternalIp", "FetchList", null);
        if (ipAry == null || ipAry.size() == 0)
            return null;
        boolean fondFlg = false;
        String ownerIP = OaTools.getIP(ip);
        for (Object o : ipAry) {
            HiDbManagerInternalIp systemIP = (HiDbManagerInternalIp)o;
            String startIP = OaTools.getIP(systemIP.getStartIp());
            String endIP = OaTools.getIP(systemIP.getEndIp());
            if (ownerIP.compareTo(startIP) >= 0 && ownerIP.compareTo(endIP) <= 0) {
                fondFlg = true;
                break;
            }
        }
        if (!fondFlg)
            throw new OaException("MSGERR0318", "用户IP不能进行内网认证。");
        return null;
    }

    public static String checkIp(HiDbUserSetting us, String ip) throws OaException {
        if (us == null)
            throw new OaException();
        if (us.getAuthIpFlag() == null || us.getAuthIpFlag().intValue() != 1)
            return null;
        if (ip == null)
            throw new OaException("MSGERR0316", "用户IP无法确认。");
        if (!ip.equals("127.0.0.1")) {
            boolean fondFlg = false;
            String ownerIP = OaTools.getIP(ip);
            String startIP = OaTools.getIP(us.getUserIpStart());
            String endIP = OaTools.getIP(us.getUserIpEnd());
            if (ownerIP.compareTo(startIP) >= 0 && ownerIP.compareTo(endIP) <= 0)
                fondFlg = true;
            if (!fondFlg)
                throw new OaException("MSGcommon00030", "请使用指定地址段内的IP登录系统。");
        }
        return null;
    }

    public static String checkMac(HiDbUserSetting us, String language, String macAddr, int loginMode) throws OaException {
        if (us == null || us.getAuthMacFlag() == null || us.getAuthMacFlag().intValue() != 1)
            return null;
        String clientMac = (macAddr == null) ? "******" : macAddr;
        String clientMac1 = clientMac.replace(":", "-");
        if ("******".equals(clientMac)) {
            if (loginMode == 1)
                throw new OaException("MSGmsg00147", "因启用了Mac地址认证，您的mac地址有误，请使用匹配的电脑登录。");
            throw new OaException("MSGhr00661", "系统中启用了Mac地址认证，但您的地址未取到。");
        }
        if ("".equals(clientMac)) {
            if (loginMode == 1)
                throw new OaException("MSGhr00651", "Mac地址未取到，请打开浏览器中ActiveX的相关设置。");
            throw new OaException("MSGhr00661", "系统中启用了Mac地址认证，但您的地址未取到。");
        }
        if (us.getUserMac() == null) {
            HiOaPubSystemMsg hiOaPubSystemMsg = new HiOaPubSystemMsg(language);
            if (loginMode == 1)
                return hiOaPubSystemMsg.message("MSGhr00649", "需要绑定Mac地址才能够使用本系统，是否将当前Mac地址发送给管理员作为绑定地址？");
            (new HtHrServer(language)).applyAddrBind(us.getUserId(), clientMac, 1);
            return hiOaPubSystemMsg.message("MSGhr00660", "需要绑定Mac地址才能够使用本系统，已将您的Mac地址的绑定申请发送给管理员，请等待管理员审批通过后您将能够访问本系统。");
        }
        List systemMacList = OaTools.partitionString(us.getUserMac(), ';');
        for (Object o : systemMacList) {
            String mac = (String)o;
            if ("".equals(mac))
                continue;
            if (clientMac.contains(mac) || clientMac1.contains(mac))
                return null;
        }
        HiOaPubSystemMsg systemMsg = new HiOaPubSystemMsg(language);
        if (loginMode == 1)
            return systemMsg.message("MSGhr00657", "当前登录的Mac地址不是系统中已经绑定的地址，是否要发送新的地址给管理员申请绑定？");
        (new HtHrServer(language)).applyAddrBind(us.getUserId(), clientMac, 1);
        return systemMsg.message("MSGhr00662", "当前登录的Mac地址不是系统中已经绑定的地址，已将您的Mac地址的绑定申请发送给管理员，请等待管理员审批通过后您将能够访问本系统。");
    }

    public static void writeSpLog(int inflag, String serviceName, String methodName, String returnMessg, String userShowId) {
        try {
            if (StringUtils.isBlank(methonMonitorUserId) || userShowId == null || !methonMonitorUserId.contains(userShowId))
                return;
            HiDbThreadLog log = new HiDbThreadLog();
            log.setThreadLogId(OaTools.gainedNo());
            log.setThreadClassName(serviceName);
            log.setDetailId(methodName);
            log.setLastInvokeDate(new Date());
            log.setInvokeTimes(Integer.valueOf(inflag));
            log.setAverageInvokeTime(returnMessg);
            (new HiMainDao()).persist(log);
        } catch (OaException e) {
            e.printStackTrace();
        }
    }

    public static String getWriteSpLogFlag() {
        String writeSpLogFlag;
        try {
            writeSpLogFlag = (new HtSystemSetting01Manager()).getWriteSpLogFlag();
        } catch (OaException e) {
            return null;
        }
        if (!"0".equals(writeSpLogFlag))
            return writeSpLogFlag;
        return null;
    }

    public static void addCookie(HttpServletResponse resp, String key, String value) {
        PageOpenTools.addCookie(resp, key, value);
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-ip");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("Proxy-Client-IP");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("HTTP_CLIENT_IP");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
            ip = request.getRemoteAddr();
        return ip;
    }

    public static String getPhoneNumber(String authSMSUuid) {
        if (authSMSUuid != null && authSMSUuid.contains(PHONE_CODE_SPLIT_MARK))
            return authSMSUuid.substring(authSMSUuid.indexOf(PHONE_CODE_SPLIT_MARK) + PHONE_CODE_SPLIT_MARK.length());
        return null;
    }

    public static void responseErrorMsg(String msg, HttpServletResponse response, String serviceName, String methodName, String userShowId, boolean notCountRandom) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", Integer.valueOf(0));
        jsonObject.put("info", msg);
        jsonObject.put("showRandomCodFlg", Boolean.valueOf((!notCountRandom && HtLoginInitManager.getShowRandomCodeFlg())));
        if (StringUtils.isNotBlank(userShowId) && LOGIN_METHOD.equals(methodName) && !notCountRandom) {
            int errrcount;
            String errrcountStr = JedisService.getValue(LOGINERRMARK + userShowId);
            if (StringUtils.isNotBlank(errrcountStr)) {
                errrcount = Integer.parseInt(errrcountStr) + 1;
            } else {
                errrcount = 1;
            }
            JedisService.setValue(LOGINERRMARK + userShowId, String.valueOf(errrcount));
            if (errrcount >= RANDOM_CHECK_LIMIT_TIME)
                jsonObject.put("showRandomCodFlg", Boolean.valueOf(true));
        }
        String params = jsonObject.toString();
        params = OaTools.encodeBase64String(params);
        if ("2".equals(DO_CODE_FLG)) {
            params = new String(oaEncode(params.toCharArray(), 17));
            response.getWriter().write("htoa" + OaTools.encodeBase64String(params));
        } else {
            response.getWriter().write(params);
        }
        writeSpLog(2, serviceName, methodName, jsonObject.toString(), userShowId);
        response.getWriter().flush();
    }

    public static void responseInfoMsg(String returnInfo, HttpServletRequest request, HttpServletResponse response, String serviceName, String methodName, String userShowId) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", Integer.valueOf(1));
        jsonObject.put("info", returnInfo);
        writeSpLog(1, serviceName, methodName, jsonObject.toString(), userShowId);
        SecretFreeServer.closeSecretRemoveCookie(request, response, methodName);
        String params = jsonObject.toString();
        params = OaTools.encodeBase64String(params);
        if ("2".equals(DO_CODE_FLG)) {
            params = new String(oaEncode(params.toCharArray(), 17));
            response.getWriter().write("htoa" + OaTools.encodeBase64String(params));
        } else {
            response.getWriter().write(params);
        }
        response.getWriter().flush();
    }

    public static int checkAuthSMS(String authImageUuid, String authSMSRandom) {
        try {
            Map<String, String> trueRandomMap = JedisService.getMap(LOGINCHECKSMSMARK + authImageUuid);
            String logTimeStr = trueRandomMap.get(authSMSRandom.toLowerCase());
            if (logTimeStr == null)
                return PHONE_CODE_WRONG;
            Date logTime = new Date(Long.parseLong(logTimeStr));
            if ((new Date()).getTime() - logTime.getTime() > 180000L) {
                JedisService.delValue(LOGINCHECKSMSMARK + authImageUuid);
                return PHONE_CODE_TIME_OUT;
            }
            return PHONE_CODE_PASS;
        } catch (Exception e) {
            e.printStackTrace();
            return PHONE_CODE_ERROR;
        }
    }

    public static boolean needCheckRandom(String userShowId) {
        if (StringUtils.isBlank(userShowId))
            return false;
        String erro = JedisService.getValue(LOGINERRMARK + userShowId);
        if (StringUtils.isBlank(erro))
            return false;
        return (Integer.parseInt(erro) >= RANDOM_CHECK_LIMIT_TIME);
    }

    public static void doLoginMethodAIM(HttpServletRequest req, HttpServletResponse resp, String serviceName, String methodName, String userShowId) throws IOException {
        String authImageUuid = (String)req.getAttribute("authImageUuid");
        String strRandom = verifyCodeUtils.generateVerifyCode(4);
        if (StringUtils.isBlank(authImageUuid))
            authImageUuid = UUID.randomUUID().toString();
        JedisService.setValue(LOGINCHECKMARK + authImageUuid, strRandom);
        JSONObject _jsa = new JSONObject();
        _jsa.put("authImageUuid", authImageUuid);
        _jsa.put("authImagePath", (new HtSystemSetting01Manager()).getAuthImage(strRandom));
        responseInfoMsg(_jsa.toString(), req, resp, serviceName, methodName, userShowId);
    }

    public static JSONArray doGetParams(HiUserInfo userInfo, HttpServletResponse resp, String serviceName, String methodName, String userShowId, String params, boolean defultDecodeFlg) throws IOException {
        try {
            JSONArray _jsa;
            if (params == null || "".equals(params)) {
                params = "[]";
            } else if (params.startsWith("htoa") && "2".equals(DO_CODE_FLG)) {
                params = params.substring(4);
                String str = getAttr(params);
                params = new String(oaDecode(str.toCharArray(), 17));
            }
            if (defultDecodeFlg) {
                String jsonParams = jsonString(getAttr(params));
                _jsa = JSONArray.fromObject(jsonParams.replaceAll("[\\t\\n\\r]", ""));
            } else {
                _jsa = JSONArray.fromObject(params);
            }
            return _jsa;
        } catch (Exception e) {
            e.printStackTrace();
            responseErrorMsg(serviceName + "=>" + methodName + (new HiOaPubSystemMsg(userInfo.languageType)).message("MSGcommon00019", "错误的方法参数传递。"), resp, serviceName, methodName, userShowId, false);
            return null;
        }
    }
}
