package com.oa8000.proxy.comm;

import com.cnpower.base.OaBaseSysInfo;
import com.cnpower.base.OaBaseTools;
import com.cnpower.base.OaBaseUserInfo;
import com.cnpower.base.OaSessionManager;
import com.cnpower.base.OaSystemConstant;
import com.cnpower.base.SessionStorage;
import com.oa8000.proxy.base.TransactionManager;
import com.oa8000.proxy.dao.HiMainDao;
import com.oa8000.proxy.db.HiDbHrDept;
import com.oa8000.proxy.db.HiDbHrStaffInfo;
import com.oa8000.proxy.db.HiDbSystemRegister;
import com.oa8000.proxy.db.HiDbUserGroupDetail;
import com.oa8000.proxy.db.HiDbUserSetting;
import com.oa8000.proxy.db.HiDbUserUser;
import com.oa8000.proxy.exception.OaException;
import com.oa8000.proxy.tools.foundation.NSLog;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.ldap.InitialLdapContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.w3c.dom.Element;

public class HiUserInfo extends OaBaseUserInfo implements Serializable {
    private static final ConcurrentHashMap<Integer, String> loginModelMap = new ConcurrentHashMap<>();

    public final int LOG_LEVEL_NORMAL = 10;

    public final int LOG_LEVEL_SPECIAL = 11;

    public final int LOG_LEVEL_SAFE = 12;

    public final int LOG_LEVEL_TASK = 13;

    public final int LOG_LEVEL_THROWABLE = 14;

    private static final Log log = LogFactory.getLog(HiUserInfo.class);

    public String languageType;

    public String styleType;

    public String imageRootCommPath;

    public String imageCommPath;

    public String imageUserPath;

    public String imagePath;

    public String cssCommPath;

    public String cssPath;

    public String jsCommPath;

    public String jsPath;

    public String rankGroup;

    public HiDbUserUser webUsers;

    public HiDbUserUser agentUsers;

    public boolean superManagerFlg;

    public boolean subCompanyManagerFlg;

    public String tryoutVersionInfo = null;

    public String ip = null;

    private final OaBaseSysInfo sysInfo;

    public int loginMode;

    public String roleId;

    public String groupIdList;

    public String companyId;

    public String userOsType;

    public String browseType;

    public float browseVersion;

    public final int scanQrcodeMark = 99;

    private boolean fakerMark;

    public final String fakerAttrKey = "fakerMark";

    static {
        loginModelMap.put(Integer.valueOf(0), "(PC)");
        loginModelMap.put(Integer.valueOf(1), "(PC)");
        loginModelMap.put(Integer.valueOf(2), "(APP)");
        loginModelMap.put(Integer.valueOf(3), "(PHONE)");
        loginModelMap.put(Integer.valueOf(98), "(DINGDING)");
        loginModelMap.put(Integer.valueOf(99), "(WECHAT)");
    }

    public HiUserInfo(int loginMode) {
        this.sysInfo = OaBaseTools.createOaBaseSysInfo();
        this.loginMode = loginMode;
    }

    public HttpSession getSession(String userId, int loginMode) {
        OaSessionManager oaSessionManager = OaSessionManager.getInstance();
        SessionStorage sessionStorage = oaSessionManager.getOnlineUser(userId, loginMode);
        if (sessionStorage == null)
            return null;
        return sessionStorage.getHttpSession();
    }

    public String login(String id, String pwd, String language, String style, String clientip, HttpServletRequest httpRequest) throws OaException {
        this.languageType = language;
        this.styleType = style;
        this.ip = clientip;
        String str = checkTest(this.sysInfo.getVersion(), this.sysInfo.isTryOutVersion());
        if (str != null)
            throw new OaException(str);
        str = checkUserEnvironment(httpRequest.getHeader("user-agent"), id, clientip);
        if (id == null || pwd == null)
            throw new OaException("MSGERR0015", "请输入用户名及密码后登录。");
                    login(id, pwd, clientip);
        writePathInfo(language, style);
        this.status = 1;
        this.session = httpRequest.getSession(true);
        String sessionId = null;
        if (this.session != null) {
            this.session.setAttribute("userInfo", this);
            sessionId = this.session.getId();
        }
        String loginModelOther = "(OTHER)";
        switch (this.loginMode) {
            case 1:
                writeLog("Log In", this.webUsers.getUserName() + ((loginModelMap.containsKey(Integer.valueOf(this.loginMode)) && loginModelMap.get(Integer.valueOf(this.loginMode)) != null) ? loginModelMap.get(Integer.valueOf(this.loginMode)) : loginModelOther), language, getEquipmentName(httpRequest.getHeader("user-agent")) + "/" + this.userOsType + "/" + this.browseType + this.browseVersion, sessionId, this.ip, this.webUsers.getUserShowId(), this.companyId, 10);
                return str;
            case 2:
                writeLog("Client Log In", this.webUsers.getUserName() + ((loginModelMap.containsKey(Integer.valueOf(this.loginMode)) && loginModelMap.get(Integer.valueOf(this.loginMode)) != null) ? loginModelMap.get(Integer.valueOf(this.loginMode)) : loginModelOther), language, "CLIENT/" + this.userOsType + "/" + this.browseType + this.browseVersion, sessionId, this.ip, this.webUsers.getUserShowId(), this.companyId, 10);
                return str;
            case 3:
                writeLog("Wap Log In", this.webUsers.getUserName() + ((loginModelMap.containsKey(Integer.valueOf(this.loginMode)) && loginModelMap.get(Integer.valueOf(this.loginMode)) != null) ? loginModelMap.get(Integer.valueOf(this.loginMode)) : loginModelOther), language, getEquipmentName(httpRequest.getHeader("user-agent")) + "/" + this.userOsType + "/" + this.browseType + this.browseVersion, sessionId, this.ip, this.webUsers.getUserShowId(), this.companyId, 10);
                return str;
        }
        writeLog("Log In", this.webUsers.getUserName() + ((loginModelMap.containsKey(Integer.valueOf(this.loginMode)) && loginModelMap.get(Integer.valueOf(this.loginMode)) != null) ? loginModelMap.get(Integer.valueOf(this.loginMode)) : loginModelOther), language, (String)null, sessionId, this.ip, this.webUsers.getUserShowId(), this.companyId, 10);
        return str;
    }

    private String getEquipmentName(String userAgent) {
        if (userAgent == null)
            return "PC";
        if (userAgent.contains("iPad"))
            return "iPad";
        if (userAgent.contains("iPhone"))
            return "iPhone";
        if (userAgent.contains("iPod"))
            return "iPod";
        String[] systemList = { "Mobile", "XiaoMi", "Android", "HTC", "HUAWEI" };
        for (String s : systemList) {
            if (userAgent.contains(s))
                return "Mobile";
        }
        return "PC";
    }

    private String checkUserEnvironment(String userAgent, String userId, String clientIp) {
        Properties perties = new Properties();
        try {
            FileInputStream is = new FileInputStream(this.sysInfo.getFullBasePath() + "js/" + "userOs.properties");
            perties.load(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean correctFlg = false;
        Iterator<Map.Entry<Object, Object>> it = perties.entrySet().iterator();
        while (it.hasNext()) {
            String key = ((String)((Map.Entry)it.next()).getKey()).replace('_', ' ');
            if (userAgent.contains(key)) {
                this.userOsType = key;
                correctFlg = true;
                break;
            }
        }
        String errStr = null;
        if (!correctFlg)
            this.userOsType = "noneOS";
        char[] numCharAry = new char[0];
        perties.clear();
        try {
            FileInputStream is = new FileInputStream(this.sysInfo.getFullBasePath() + "js/" + "browser.properties");
            perties.load(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        correctFlg = false;
        it = perties.entrySet().iterator();
        boolean checkChromeFlg = false;
        if (userAgent.toLowerCase().trim().contains("chrome"))
            checkChromeFlg = true;
        while (it.hasNext()) {
            String key = (String)((Map.Entry)it.next()).getKey();
            String value = (String)perties.get(key);
            String[] list = OaTools.split(value, ";");
            for (String s : list) {
                if (!"".equals(s.trim())) {
                    int index = userAgent.indexOf(s);
                    if (index > -1) {
                        this.browseType = key;
                        numCharAry = userAgent.substring(index + s.length()).toCharArray();
                        correctFlg = true;
                        if (checkChromeFlg) {
                            if (this.browseType.toLowerCase().contains("chrome")) {
                                checkChromeFlg = false;
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
            if (correctFlg && !checkChromeFlg)
                break;
        }
        if (!correctFlg) {
            this.browseType = "noneBS";
            log.error("loginUser is " + userId + ". loginClientIP is " + clientIp + ". userAgent = " + userAgent);
            HiOaPubSystemMsg os = new HiOaPubSystemMsg(this.languageType);
            errStr = os.message("MSGsystem00017", "您所使用的浏览器在使用OA时可能导致某些功能出现异常，建议使用IE(9、10、11)、FireFox、Safari、Chrome等浏览器使用OA");
        } else {
            Properties illegalProperties = new Properties();
            FileInputStream is = null;
            try {
                is = new FileInputStream(this.sysInfo.getFullBasePath() + "js/" + "illegalBrowser.properties");
                illegalProperties.load(is);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (is != null)
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
            it = illegalProperties.entrySet().iterator();
            while (it.hasNext()) {
                String key = (String)((Map.Entry)it.next()).getKey();
                String value = (String)illegalProperties.get(key);
                String[] list = OaTools.split(value, ";");
                for (String s : list) {
                    if (!"".equals(s.trim())) {
                        int index = userAgent.indexOf(s);
                        if (index > -1) {
                            this.browseType = key;
                            correctFlg = false;
                            break;
                        }
                    }
                }
                if (!correctFlg)
                    break;
            }
            if (!correctFlg) {
                HiOaPubSystemMsg os = new HiOaPubSystemMsg(this.languageType);
                errStr = os.message("MSGsystem00017", "您所使用的浏览器在使用OA时可能导致某些功能出现异常，建议使用IE(9、10、11)、FireFox、Safari、Chrome等浏览器使用OA");
            }
        }
        StringBuilder versionNum = new StringBuilder();
        for (char c : numCharAry) {
            if ("0123456789".indexOf(c) == -1)
                break;
            versionNum.append(c);
        }
        if ("IE".equals(this.browseType)) {
            correctFlg = false;
            String[] versionList = OaTools.split(perties.getProperty("BrowserVersion"), ";");
            for (String s : versionList) {
                if (!"".equals(s.trim()) &&
                        versionNum.toString().contains(s)) {
                    correctFlg = true;
                    break;
                }
            }
            if (!correctFlg) {
                log.error("loginUser is " + userId + ". loginClientIP is " + clientIp + ". userAgent = " + userAgent);
                if (userAgent.contains("compatible;") &&
                        errStr == null) {
                    HiOaPubSystemMsg os = new HiOaPubSystemMsg(this.languageType);
                    errStr = os.message("MSGsystem00018", "请关闭IE浏览器的兼容，使用正常的浏览器访问OA。");
                }
                if (errStr == null) {
                    HiOaPubSystemMsg os = new HiOaPubSystemMsg(this.languageType);
                    errStr = os.message("MSGsystem00016", "您所使用的浏览器在使用OA时可能导致某些功能出现异常，建议使用IE(9、10、11)、FireFox、Safari、Chrome等浏览器使用OA");
                }
            }
        }
        try {
            this.browseVersion = Float.parseFloat(versionNum.toString());
        } catch (Exception ex) {
            log.info("loginUser is " + userId + ". loginClientIP is " + clientIp + ". userAgent = " + userAgent);
            log.info("无法取得正确的IE版本号");
            this.browseVersion = 9.0F;
        }
        return errStr;
    }

    public String translateJasUser(String userShowId, String pwd, String clientIp) throws OaException {
        String language = "CN";
        String style = "BLUE";
        String s = clientLogin(userShowId, pwd, language, style, clientIp);
        if (s != null)
            return s;
        OaSessionManager oaSessionManager = OaSessionManager.getInstance();
        oaSessionManager.addOnlineUser(this.webUsers.getUserId(), userShowId, this.loginMode, null);
        return null;
    }

    private String clientLogin(String id, String pwd, String language, String style, String clientIp) throws OaException {
        this.languageType = language;
        this.styleType = style;
        this.ip = clientIp;
        loginWithoutLog(id, pwd, clientIp);
        HiDbHrDept currentDept = (new HiOaMainClass()).getDept(this.webUsers.getDeptId(), null);
        this.companyId = currentDept.getCompanyId();
        this.superManagerFlg = (this.webUsers.getManagerFlag() != null && 1 == this.webUsers.getManagerFlag().intValue());
        if (!this.superManagerFlg)
            this.subCompanyManagerFlg = (this.webUsers.getManagerFlag() != null && this.webUsers.getManagerFlag().intValue() == 0 && this.companyId != null);
        writePathInfo(language, style);
        this.status = 1;
        HiRankInfo rank = new HiRankInfo();
        this.rankGroup = rank.fetchUserGroupId(this.webUsers.getUserId());
        return null;
    }

    public String checkTest(String ver, boolean tryFlg) {
        return tryFlg ? checkVersion(ver) : null;
    }

    public void login(String id, String pwd, String clientIp) throws OaException {
        if (this.loginMode == 3 && !this.sysInfo.useWapFlg())
            throw new OaException("MSGmanager00171", "未开通手机版，请联系销售商购买后方可使用。");
        this.ip = clientIp;
        Element element = OaTools.readXMLFile("Config.xml");
        String adlog = null;
        if (element != null)
            adlog = OaTools.getItemValue(element, "ADLOG");
        if (element != null && adlog != null && adlog.equals("1") && isAdUser(id)) {
            if (id == null)
                throw new OaException("MSGERR0015", "请输入用户名及密码后登录。");
                        String userId = getUserShowId(id);
            if (userId == null)
                throw new OaException("MSGERR0016", "输入用户不存在，或密码错误，请重新输入");
                        HashMap<String, Object> map1 = new HashMap<>();
            map1.put("userId", userId);
            map1.put("active", Integer.valueOf(1));
            List<HiDbUserUser> queryResults1 = (new HiMainDao()).findList("UserUser", "Fetch01", map1, 1);
            if (queryResults1.size() <= 0)
                throw new OaException("MSGERR0016", "输入用户不存在，或密码错误，请重新输入");
            this.webUsers = queryResults1.get(0);
            HiRankInfo hiRankInfo = new HiRankInfo();
            this.rankGroup = hiRankInfo.fetchUserGroupId(this.webUsers.getUserId());
            checkAD(id, pwd);
        } else {
            String hql = "from com.oa8000.proxy.db.HiDbUserUser where activeFlag is not null and activeFlag <> -1 and userShowId = ?";
            List<String> paramList = new ArrayList<>();
            paramList.add(id);
            List<HiDbUserUser> queryResults = (new HiMainDao()).findList(hql, paramList, 1);
            if (queryResults.size() <= 0)
                throw new OaException("MSGcommon00027",  "用户账号不存在，请重新输入");
            this.webUsers = queryResults.get(0);
            this.agentUsers = this.webUsers;
            if (this.webUsers.getActiveFlag() == null || this.webUsers.getActiveFlag().intValue() == 0)
                throw new OaException("MSGmsg00244", "当前用户未激活，请联系管理员");
            if (this.webUsers.getActiveFlag().intValue() == 4)
                throw new OaException("MSGcommon00023", "用户账号已被锁定，请联系管理员");
            if (this.webUsers.getActiveFlag().intValue() != 1)
                throw new OaException("MSGcommon00027", "用户账号不存在，请重新输入");
                        getClass();
            if (99 == this.loginMode) {
                if (pwd == null || !pwd.equals(this.webUsers.getPassword())) {
                    dealPsdErr(this.webUsers.getUserId());
                    throw new OaException("MSGcommon00026", "密码错误，请重新输入");
                }
            } else {
                String enPwd = OaPubptDesEncrypter1.MD5Encode(pwd);
                if (enPwd == null || !enPwd.equals(this.webUsers.getPassword())) {
                    dealPsdErr(this.webUsers.getUserId());
                    throw new OaException("MSGcommon00026", "密码错误，请重新输入");
                }
            }
        }
        resetPsdErrNum(this.webUsers.getUserId());
        HiDbHrDept currentDept = (new HiOaMainClass()).getDept(this.webUsers.getDeptId(), null);
        this.companyId = (currentDept == null) ? "ROOT" : currentDept.getCompanyId();
        this.superManagerFlg = (this.webUsers.getManagerFlag() != null && 1 == this.webUsers.getManagerFlag().intValue());
        if (!this.superManagerFlg)
            this.subCompanyManagerFlg = (this.webUsers.getManagerFlag() != null && this.webUsers.getManagerFlag().intValue() == 0 && this.companyId != null);
        HiRankInfo rank = new HiRankInfo();
        this.rankGroup = rank.fetchUserGroupId(this.webUsers.getUserId());
        List<HiDbHrStaffInfo> list = fetchHrStaffInfoAry(this.webUsers.getUserId());
        HiDbHrStaffInfo staffInfo = (list == null || list.isEmpty()) ? null : list.get(0);
        this.roleId = (staffInfo == null) ? null : staffInfo.getRoleId();
        this.groupIdList = getGroupIdListByUserId(this.webUsers.getUserId());
        String loginModelOther = "(OTHER)";
        switch (this.loginMode) {
            case 2:
                writeLog("Client Log In", this.webUsers.getUserName() + ((loginModelMap.containsKey(Integer.valueOf(this.loginMode)) && loginModelMap.get(Integer.valueOf(this.loginMode)) != null) ? loginModelMap.get(Integer.valueOf(this.loginMode)) : loginModelOther), this.languageType, (String)null, (String)null, this.ip, this.webUsers.getUserShowId(), this.companyId, 10);
                break;
            case 3:
                writeLog("Wap Log In", this.webUsers.getUserName() + ((loginModelMap.containsKey(Integer.valueOf(this.loginMode)) && loginModelMap.get(Integer.valueOf(this.loginMode)) != null) ? loginModelMap.get(Integer.valueOf(this.loginMode)) : loginModelOther), this.languageType, (String)null, (String)null, this.ip, this.webUsers.getUserShowId(), this.companyId, 10);
                break;
        }
    }

    private boolean isAdUser(String id) {
        String userId = getUserShowId(id);
        return (userId != null && !"".equals(userId));
    }

    public void checkAD(String id, String pwd) throws OaException {
        Hashtable<String, String> HashEnv = new Hashtable<>();
        Element element = OaTools.readXMLFile("Config.xml");
        if (element == null)
            throw new OaException("MSGmsg00180", "请检查配置文件config.xml是否存在。");
                    String host = OaTools.getItemValue(element, "ADURL");
        String port = OaTools.getItemValue(element, "ADPORT");
        String searchBase = OaTools.getItemValue(element, "ADSEARCHBASE");
        String adminName = OaTools.getItemValue(element, "NAMEBEFOREUSERNAME") + id;
        if (host == null || port == null || searchBase == null || "".equals(host.trim()) || "".equals(port.trim()) || "".equals(searchBase.trim()))
            throw new OaException("MSGmsg00177", "请检查配置文件config.xml中的ADURL,ADPORT,ADSEARCHBASE是否填写。");
                    String url = "ldap://" + host + ":" + port;
        HashEnv.put("java.naming.security.authentication", "simple");
        HashEnv.put("java.naming.security.principal", adminName);
        HashEnv.put("java.naming.security.credentials", pwd);
        HashEnv.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
        HashEnv.put("java.naming.provider.url", url);
        boolean checkAdFlag = true;
        try {
            new InitialLdapContext(HashEnv, null);
            checkAdFlag = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (checkAdFlag)
            throw new OaException("MSGERR0016", "输入用户不存在，或密码错误，请重新输入");
    }

    public void loginWithoutLog(String id, String pwd, String clientIp) throws OaException {
        if (this.loginMode == 3 && !this.sysInfo.useWapFlg())
            throw new OaException("MSGmanager00171", "未开通手机版，请联系销售商购买后方可使用。");
        this.ip = clientIp;
        Element element = OaTools.readXMLFile("Config.xml");
        String adlog = null;
        if (element != null)
            adlog = OaTools.getItemValue(element, "ADLOG");
        if (element != null && adlog != null && adlog.equals("1") && isAdUser(id)) {
            if (id == null)
                throw new OaException("MSGERR0015", "请输入用户名及密码后登录。");
                        String userId = getUserShowId(id);
            if (userId == null)
                throw new OaException("MSGERR0016", "输入用户不存在，或密码错误，请重新输入");
                        HashMap<String, Object> map1 = new HashMap<>();
            map1.put("userId", userId);
            map1.put("active", Integer.valueOf(1));
            List<HiDbUserUser> queryResults1 = (new HiMainDao()).findList("UserUser", "Fetch01", map1, 1);
            if (queryResults1.size() <= 0)
                throw new OaException("MSGERR0016", "输入用户不存在，或密码错误，请重新输入");
            this.webUsers = queryResults1.get(0);
            checkAD(id, pwd);
        } else {
            HashMap<String, Object> map = new HashMap<>();
            map.put("userId", id);
            map.put("active", Integer.valueOf(1));
            List<HiDbUserUser> queryResults = (new HiMainDao()).findList("UserUser", "Fetch01", map, 1);
            if (queryResults.size() <= 0)
                throw new OaException("MSGERR0016", "输入用户不存在，或密码错误，请重新输入");
            this.webUsers = queryResults.get(0);
            this.agentUsers = this.webUsers;
            String enPwd = OaPubptDesEncrypter1.MD5Encode(pwd);
            if (enPwd == null || !enPwd.equals(this.webUsers.getPassword()))
                throw new OaException("MSGERR0016", "输入用户不存在，或密码错误，请重新输入");
            if (this.webUsers.getActiveFlag() == null || this.webUsers.getActiveFlag().intValue() == 0)
                throw new OaException("MSGERR0016", "输入用户不存在，或密码错误，请重新输入");
        }
        HiDbHrDept currentDept = (new HiOaMainClass()).getDept(this.webUsers.getDeptId(), null);
        this.companyId = (currentDept == null) ? "ROOT" : currentDept.getCompanyId();
        this.superManagerFlg = (this.webUsers.getManagerFlag() != null && 1 == this.webUsers.getManagerFlag().intValue());
        if (!this.superManagerFlg)
            this.subCompanyManagerFlg = (this.webUsers.getManagerFlag() != null && this.webUsers.getManagerFlag().intValue() == 0 && this.companyId != null);
        HiRankInfo rank = new HiRankInfo();
        this.rankGroup = rank.fetchUserGroupId(this.webUsers.getUserId());
        List<HiDbHrStaffInfo> list = fetchHrStaffInfoAry(this.webUsers.getUserId());
        HiDbHrStaffInfo staffInfo = (list == null || list.isEmpty()) ? null : list.get(0);
        this.roleId = (staffInfo == null) ? null : staffInfo.getRoleId();
        this.groupIdList = getGroupIdListByUserId(this.webUsers.getUserId());
    }

    public void setStatus(int value) {
        this.status = value;
    }

    private void writePathInfo(String language, String style) {
        if (language == null) {
            log.error("language error.");
            return;
        }
        if (style == null) {
            log.error("style error.");
            return;
        }
        OaBaseSysInfo info = OaBaseTools.createOaBaseSysInfo();
        String head = info.getBasePath();
        String langStyle = language + "/" + style + "/";
        String comm = "/comm/" + style + "/";
        this.imageRootCommPath = head + "image" + "/";
        this.imageCommPath = head + "image" + comm;
        this.imagePath = head + "image" + "/" + langStyle;
        this.imageUserPath = head + "image" + "/" + "comm" + "/";
        this.cssCommPath = head + "css" + comm;
        this.cssPath = head + "css" + "/" + langStyle;
        this.jsCommPath = head + "js" + "/";
        this.jsPath = head + "js" + "/" + language + "/";
    }

    public void writeStyleInfo(String style, String language) {
        if (style == null) {
            log.error("style error.");
            return;
        }
        OaBaseSysInfo info = OaBaseTools.createOaBaseSysInfo();
        this.styleType = style;
        String head = info.getBasePath();
        String langStyle = language + "/" + style + "/";
        String comm = "/comm/" + style + "/";
        this.imagePath = head + "image" + "/" + langStyle;
        this.cssPath = head + "css" + "/" + langStyle;
        this.imageCommPath = head + "image" + comm;
        this.cssCommPath = head + "css" + comm;
        if (this.session != null)
            this.session.setAttribute("userInfo", this);
    }

    public void writeLog(String operate, String memo1, String memo2, String memo3) {
        try {
            if (this.session != null) {
                writeLog(operate, memo1, memo2, memo3, this.session.getId(), this.ip, this.webUsers.getUserShowId(), this.companyId, 0);
            } else {
                writeLog(operate, memo1, memo2, memo3, "", this.ip, this.webUsers.getUserShowId(), this.companyId, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeLog(String operate, String memo1, String memo2, String memo3, String sessionId) {
        try {
            writeLog(operate, memo1, memo2, memo3, sessionId, this.ip, this.webUsers.getUserShowId(), this.companyId, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String checkVersion(String version) {
        String endDate;
        try {
            endDate = checkTestVersion(version);
        } catch (Exception e) {
            return "您安装的试用版已经到期，如欲购买请拨打销售热线：4006090086";
        }
        HiOaPubSystemMsg os = new HiOaPubSystemMsg(this.languageType);
        this.tryoutVersionInfo = os.message("MSGsystem00003", "您使用的是试用版, 版本到期日：");
        this.tryoutVersionInfo += endDate;
        NSLog.out.appendln(this.tryoutVersionInfo.replaceAll("\n", ""));
        return null;
    }

    public static String getSubsectorUserList(String deptId, String deptTree) {
        String sql = "select user_user.user_id,user_user.user_name,user_user.user_show_id,user_user.dept_id,user_dept.dept_tree  from user_user left join user_dept on user_user.dept_id =user_dept.dept_id where user_user.active_flag=1 and user_user.user_id != 'adms'";
        if (deptId != null && !deptTree.equals("0"))
            sql = sql + " and (user_dept.dept_tree like  ?  or user_user.dept_id =?) order by user_user.user_order";
        try {
            List<String> paramList = new LinkedList<>();
            paramList.add("%" + deptTree + deptId + "%");
            paramList.add(deptId);
            return getUserUserAry((new HiMainDao()).findListBySQLToMap(sql, paramList));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getUserUserAry(List userList) {
        if (userList == null || userList.size() == 0)
            return null;
        StringBuilder sb = new StringBuilder();
        for (Object o : userList) {
            Map eoHashMap = (Map)o;
            if (eoHashMap == null)
                continue;
            sb.append(eoHashMap.get("user_id")).append(";");
        }
        return sb.toString();
    }

    public void logout() {
        if (this.loginMode == 3 || this.loginMode == 1) {
            String sessionId = null;
            if (this.webUsers != null) {
                OaSessionManager oaSessionManager = OaSessionManager.getInstance();
                SessionStorage sessionStorage = oaSessionManager.getOnlineUser(this.webUsers.getUserId(), this.loginMode);
                if (sessionStorage != null)
                    try {
                        HttpSession session = sessionStorage.getHttpSession();
                        session.removeAttribute("userInfo");
                        sessionId = sessionStorage.getSessionId();
                        sessionStorage.getHttpSession().invalidate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            } else if (this.session != null) {
                try {
                    this.session.removeAttribute("userInfo");
                    sessionId = this.session.getId();
                    this.session.invalidate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                if (this.loginMode == 3)
                    if (sessionId != null) {
                        writeLog("wap logout", (String)null, (String)null, (String)null, sessionId, this.ip, this.webUsers.getUserShowId(), this.companyId, 10);
                    } else {
                        writeLog("wap logout", (String)null, (String)null, (String)null, "", this.ip, this.webUsers.getUserShowId(), this.companyId, 10);
                    }
                if (this.loginMode == 1)
                    if (sessionId != null) {
                        writeLog("logout", (String)null, (String)null, (String)null, sessionId, this.ip, this.webUsers.getUserShowId(), this.companyId, 10);
                    } else {
                        writeLog("logout", (String)null, (String)null, (String)null, "", this.ip, this.webUsers.getUserShowId(), this.companyId, 10);
                    }
            } catch (OaException e) {
                e.printStackTrace();
            }
            this.webUsers = null;
        }
        if (this.loginMode == 2) {
            if (this.webUsers != null) {
                OaSessionManager oaSessionManager = OaSessionManager.getInstance();
                oaSessionManager.removeOnlineClientUser(this.webUsers.getUserId());
            }
            try {
                writeLog("Client logout", (String)null, (String)null, (String)null, this.session.getId(), this.ip, this.webUsers.getUserShowId(), this.companyId, 10);
            } catch (OaException e) {
                e.printStackTrace();
            }
            this.webUsers = null;
        }
    }

    private String getUserShowId(String yuId) {
        List userAry = (new HiOaMainClass()).getUserArray();
        HiDbUserUser user = (HiDbUserUser)OaTools.fetchArray(userAry, "yuId", yuId);
        if (user != null && user.getUserShowId() != null)
            return user.getUserShowId();
        return null;
    }

    public List<HiDbHrStaffInfo> fetchHrStaffInfoAry(String hrStaffInfoId) {
        HashMap<String, Object> map = new HashMap<>();
        if (hrStaffInfoId != null)
            map.put("hrStaffInfoId", hrStaffInfoId);
        map.put("staffStatus", Integer.valueOf(1));
        return (new HiMainDao()).findList("HrStaffInfo", "FetchList", map);
    }

    private String getGroupIdListByUserId(String userId) {
        if (userId == null)
            return null;
        HashMap<String, String> map = new HashMap<>();
        map.put("userId", userId);
        List list = (new HiMainDao()).findList("UserGroupDetail", "FetchByUserId", map);
        StringBuilder buffer = new StringBuilder();
        buffer.append(";");
        for (Object o : list) {
            HiDbUserGroupDetail detail = (HiDbUserGroupDetail)o;
            buffer.append(detail.getUserGroupId()).append(";");
        }
        return buffer.toString();
    }

    public String login(String id, String language, String style, String clientip, HttpServletRequest httpRequest) throws OaException {
        setFakerMark(httpRequest);
        this.languageType = language;
        this.styleType = style;
        this.ip = clientip;
        String str = checkTest(this.sysInfo.getVersion(), this.sysInfo.isTryOutVersion());
        if (str != null)
            throw new OaException(str);
        str = checkUserEnvironment(httpRequest.getHeader("user-agent"), id, clientip);
        if (id == null)
            throw new OaException("MSGht_crm00013", "请输入用户名称");
                    login(id, clientip, httpRequest);
        writePathInfo(language, style);
        this.status = 1;
        this.session = httpRequest.getSession(true);
        String sessionId = null;
        if (this.session != null) {
            this.session.setAttribute("userInfo", this);
            sessionId = this.session.getId();
        }
        String loginModelOther = "(OTHER)";
        switch (this.loginMode) {
            case 1:
                writeLog("Log In", this.webUsers.getUserName() + ((loginModelMap.containsKey(Integer.valueOf(this.loginMode)) && loginModelMap.get(Integer.valueOf(this.loginMode)) != null) ? loginModelMap.get(Integer.valueOf(this.loginMode)) : loginModelOther), language, getEquipmentName(httpRequest.getHeader("user-agent")) + "/" + this.userOsType + "/" + this.browseType + this.browseVersion, sessionId, this.ip, this.webUsers.getUserShowId(), this.companyId, 10);
                return str;
            case 2:
                writeLog("Client Log In", this.webUsers.getUserName() + ((loginModelMap.containsKey(Integer.valueOf(this.loginMode)) && loginModelMap.get(Integer.valueOf(this.loginMode)) != null) ? loginModelMap.get(Integer.valueOf(this.loginMode)) : loginModelOther), language, "CLIENT/" + this.userOsType + "/" + this.browseType + this.browseVersion, sessionId, this.ip, this.webUsers.getUserShowId(), this.companyId, 10);
                return str;
            case 3:
                writeLog("Wap Log In", this.webUsers.getUserName() + ((loginModelMap.containsKey(Integer.valueOf(this.loginMode)) && loginModelMap.get(Integer.valueOf(this.loginMode)) != null) ? loginModelMap.get(Integer.valueOf(this.loginMode)) : loginModelOther), language, getEquipmentName(httpRequest.getHeader("user-agent")) + "/" + this.userOsType + "/" + this.browseType + this.browseVersion, sessionId, this.ip, this.webUsers.getUserShowId(), this.companyId, 10);
                return str;
        }
        writeLog("Log In", this.webUsers.getUserName() + ((loginModelMap.containsKey(Integer.valueOf(this.loginMode)) && loginModelMap.get(Integer.valueOf(this.loginMode)) != null) ? loginModelMap.get(Integer.valueOf(this.loginMode)) : loginModelOther), language, (String)null, sessionId, this.ip, this.webUsers.getUserShowId(), this.companyId, 10);
        return str;
    }

    public void login(String id, String clientIp, HttpServletRequest httpRequest) throws OaException {
        if (this.loginMode == 3 && !this.sysInfo.useWapFlg())
            throw new OaException("MSGmanager00171", "未开通手机版，请联系销售商购买后方可使用。");
        this.ip = clientIp;
        HashMap<String, Object> map = new HashMap<>();
        String userId = (String)httpRequest.getSession().getAttribute("userId");
        if (StringUtils.isNotBlank(userId)) {
            map.put("useId", userId);
        } else {
            map.put("userId", id);
        }
        map.put("active", Integer.valueOf(1));
        List<HiDbUserUser> queryResults = (new HiMainDao()).findList("UserUser", "Fetch01", map, 1);
        if (queryResults.size() <= 0)
            throw new OaException("MSGmsg0016", "输入用户不存在，或密码错误，请重新输入");
        this.webUsers = queryResults.get(0);
        this.agentUsers = this.webUsers;
        if (this.webUsers.getActiveFlag() == null || this.webUsers.getActiveFlag().intValue() == 0)
            throw new OaException("MSGmsg00244", "当前用户未激活，请联系管理员");
                    HiDbHrDept currentDept = (new HiOaMainClass()).getDept(this.webUsers.getDeptId(), null);
        this.companyId = (currentDept == null) ? "ROOT" : currentDept.getCompanyId();
        this.superManagerFlg = (this.webUsers.getManagerFlag() != null && 1 == this.webUsers.getManagerFlag().intValue());
        if (!this.superManagerFlg)
            this.subCompanyManagerFlg = (this.webUsers.getManagerFlag() != null && this.webUsers.getManagerFlag().intValue() == 0 && this.companyId != null);
        HiRankInfo rank = new HiRankInfo();
        this.rankGroup = rank.fetchUserGroupId(this.webUsers.getUserId());
        List<HiDbHrStaffInfo> list = fetchHrStaffInfoAry(this.webUsers.getUserId());
        HiDbHrStaffInfo staffInfo = (list == null || list.isEmpty()) ? null : list.get(0);
        this.roleId = (staffInfo == null) ? null : staffInfo.getRoleId();
        this.groupIdList = getGroupIdListByUserId(this.webUsers.getUserId());
        String loginModelOther = "(OTHER)";
        switch (this.loginMode) {
            case 2:
                writeLog("Client Log In", this.webUsers.getUserName() + ((loginModelMap.containsKey(Integer.valueOf(this.loginMode)) && loginModelMap.get(Integer.valueOf(this.loginMode)) != null) ? loginModelMap.get(Integer.valueOf(this.loginMode)) : loginModelOther), this.languageType, (String)null, (String)null, this.ip, this.webUsers.getUserShowId(), this.companyId, 10);
                return;
            case 3:
                writeLog("Wap Log In", this.webUsers.getUserName() + ((loginModelMap.containsKey(Integer.valueOf(this.loginMode)) && loginModelMap.get(Integer.valueOf(this.loginMode)) != null) ? loginModelMap.get(Integer.valueOf(this.loginMode)) : loginModelOther), this.languageType, (String)null, (String)null, this.ip, this.webUsers.getUserShowId(), this.companyId, 10);
                return;
        }
        writeLog("Log In", this.webUsers.getUserName() + ((loginModelMap.containsKey(Integer.valueOf(this.loginMode)) && loginModelMap.get(Integer.valueOf(this.loginMode)) != null) ? loginModelMap.get(Integer.valueOf(this.loginMode)) : loginModelOther), this.languageType, (String)null, (String)null, this.ip, this.webUsers.getUserShowId(), this.companyId, 10);
    }

    private void dealPsdErr(String userId) throws OaException {
        HiDbSystemRegister passwordWrongTimesLockRegister = (new HiOaMainClass()).getSystemRegisterNotNull("passwordWrongTimesLock");
        if (StringUtils.isBlank(passwordWrongTimesLockRegister.getItemMemo()) ||
                Integer.parseInt(passwordWrongTimesLockRegister.getItemMemo()) == 0)
            return;
        HiDbUserSetting userSetting = getUserSetting(userId);
        if (userSetting == null)
            return;
        Integer psdErrNum = userSetting.getPsdErrNum();
        List<Object> updateList = new ArrayList();
        if (psdErrNum == null)
            psdErrNum = Integer.valueOf(0);
        if (psdErrNum.intValue() + 1 >= Integer.parseInt(passwordWrongTimesLockRegister.getItemMemo())) {
            HiDbUserUser userUser = (new HiOaMainClass()).<HiDbUserUser>getObject(HiDbUserUser.class, userId);
            if (userUser == null)
                return;
            userUser.setActiveFlag(Integer.valueOf(4));
            updateList.add(userUser);
            userSetting.setPsdErrNum(Integer.valueOf(0));
            updateList.add(userSetting);
            (new HiOaMainClass()).saveChange(null, updateList, null, null);
            throw new OaException("MSGcommon00029", "由于密码连续错误，当前账号已被锁定");
        }
        userSetting.setPsdErrNum(Integer.valueOf(psdErrNum.intValue() + 1));
        updateList.add(userSetting);
        (new HiOaMainClass()).saveChange(null, updateList, null, null);
    }

    private void resetPsdErrNum(String userId) throws OaException {
        HiDbUserSetting userSetting = getUserSetting(userId);
        if (userSetting == null)
            return;
        userSetting.setPsdErrNum(Integer.valueOf(0));
        (new HiMainDao()).update(userSetting);
    }

    public boolean isFaker() {
        return this.fakerMark;
    }

    private void setFakerMark(HttpServletRequest request) {
        Object fakerObj = request.getAttribute("fakerMark");
        if (fakerObj instanceof Boolean)
            this.fakerMark = Boolean.parseBoolean(fakerObj.toString());
    }

    public HiDbUserSetting getUserSetting(String userId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        List<HiDbUserSetting> queryResults = (new HiMainDao()).findList("UserSetting", "FetchById", map, 1);
        if (queryResults == null || queryResults.isEmpty())
            return createUserSetting(userId);
        return queryResults.get(0);
    }

    private HiDbUserSetting createUserSetting(String userId) {
        Transaction tx = null;
        Session hiSession = null;
        try {
            hiSession = TransactionManager.getInstance().getCurrentSession();
            tx = hiSession.beginTransaction();
            HiMainDao mainDao = new HiMainDao(hiSession);
            HiDbUserSetting systemUs = getSystemUserSetting();
            if ("DEMO".equals(userId))
                return systemUs;
            HiDbUserSetting eoUserSetting = new HiDbUserSetting();
            eoUserSetting.setUserId(userId);
            eoUserSetting.setHardDiskSize(systemUs.getHardDiskSize());
            eoUserSetting.setSingleFileMaxSize(systemUs.getSingleFileMaxSize());
            eoUserSetting.setTotalFileMaxSize(systemUs.getTotalFileMaxSize());
            eoUserSetting.setAuthIpFlag(systemUs.getAuthIpFlag());
            eoUserSetting.setUserIpStart(systemUs.getUserIpStart());
            eoUserSetting.setUserIpEnd(systemUs.getUserIpEnd());
            eoUserSetting.setAuthInsideNetFlag(systemUs.getAuthInsideNetFlag());
            eoUserSetting.setAuthMacFlag(systemUs.getAuthMacFlag());
            eoUserSetting.setUserMac(systemUs.getUserMac());
            eoUserSetting.setSoundId(systemUs.getSoundId());
            eoUserSetting.setUseClientFlag(systemUs.getUseClientFlag());
            eoUserSetting.setAuthImgFlag(systemUs.getAuthImgFlag());
            eoUserSetting.setMenuType(systemUs.getMenuType());
            eoUserSetting.setUserSignFile(systemUs.getUserSignFile());
            eoUserSetting.setBrowserRefreshTime(systemUs.getBrowserRefreshTime());
            eoUserSetting.setClientRefreshTime(systemUs.getClientRefreshTime());
            eoUserSetting.setUserSignImgFile(systemUs.getUserSignImgFile());
            eoUserSetting.setAuthUsbKey(systemUs.getAuthUsbKey());
            eoUserSetting.setPsdUseSoftInput(systemUs.getPsdUseSoftInput());
            eoUserSetting.setPsdErrNum(systemUs.getPsdErrNum());
            eoUserSetting.setPsdErrCloseTime(systemUs.getPsdErrCloseTime());
            eoUserSetting.setMailSpaceSize(systemUs.getMailSpaceSize());
            eoUserSetting.setInternalMailSpaceSize(systemUs.getInternalMailSpaceSize());
            eoUserSetting.setUseWx(systemUs.getUseWx());
            eoUserSetting.setUseDd(systemUs.getUseDd());
            eoUserSetting.setUseRtx(systemUs.getUseRtx());
            eoUserSetting.setRtxInputName(systemUs.getRtxInputName());
            eoUserSetting.setLoginMode(systemUs.getLoginMode());
            eoUserSetting.setAuthAttFlag(systemUs.getAuthAttFlag());
            eoUserSetting.setAttIpStart(systemUs.getAttIpStart());
            eoUserSetting.setAttIpEnd(systemUs.getAttIpEnd());
            eoUserSetting.setAuthChangeSignFileFlag(systemUs.getAuthChangeSignFileFlag());
            eoUserSetting.setDefaultPortal(systemUs.getDefaultPortal());
            eoUserSetting.setSendtoall(systemUs.getSendtoall());
            eoUserSetting.setMsgSendUserNum(systemUs.getMsgSendUserNum());
            eoUserSetting.setMsgAwokeSetup(systemUs.getMsgAwokeSetup());
            eoUserSetting.setMsgAwokeUpdate(systemUs.getMsgAwokeUpdate());
            eoUserSetting.setWeatherCity(systemUs.getWeatherCity());
            eoUserSetting.setLastTime(systemUs.getLastTime());
            eoUserSetting.setAuthMobileAddrFlag(systemUs.getAuthMobileAddrFlag());
            if (userId != null)
                mainDao.persist(eoUserSetting);
            if (tx != null)
                tx.commit();
            return eoUserSetting;
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            e.printStackTrace();
            return null;
        } finally {
            if (tx != null)
                hiSession.close();
        }
    }

    public HiDbUserSetting getSystemUserSetting() {
        HashMap<String, String> map = new HashMap<>();
        map.put("userId", "DEMO");
        List<HiDbUserSetting> queryResults = (new HiMainDao()).findList("UserSetting", "FetchById", map, 1);
        if (queryResults != null && !queryResults.isEmpty())
            return queryResults.get(0);
        HiDbUserSetting systemUs = new HiDbUserSetting();
        systemUs.setUserId("DEMO");
        systemUs.setHardDiskSize(Integer.valueOf(0));
        systemUs.setSingleFileMaxSize(Integer.valueOf(0));
        systemUs.setTotalFileMaxSize(Integer.valueOf(0));
        systemUs.setAuthIpFlag(Integer.valueOf(0));
        systemUs.setUserIpStart(null);
        systemUs.setUserIpEnd(null);
        systemUs.setAuthInsideNetFlag(Integer.valueOf(0));
        systemUs.setAuthMacFlag(Integer.valueOf(0));
        systemUs.setUserMac(null);
        systemUs.setSoundId(null);
        systemUs.setUseClientFlag(Integer.valueOf(1));
        systemUs.setAuthImgFlag(null);
        systemUs.setMenuType(null);
        systemUs.setUserSignFile(null);
        systemUs.setBrowserRefreshTime(null);
        systemUs.setClientRefreshTime(Integer.valueOf(6000));
        systemUs.setUserSignImgFile(null);
        systemUs.setAuthUsbKey(Integer.valueOf(0));
        systemUs.setPsdUseSoftInput(null);
        systemUs.setPsdErrNum(Integer.valueOf(0));
        systemUs.setPsdErrCloseTime(Integer.valueOf(0));
        systemUs.setMailSpaceSize(Integer.valueOf(0));
        systemUs.setInternalMailSpaceSize(Integer.valueOf(0));
        systemUs.setUseRtx(Integer.valueOf(0));
        systemUs.setRtxInputName(Integer.valueOf(0));
        systemUs.setLoginMode(Integer.valueOf(1));
        systemUs.setAuthAttFlag(Integer.valueOf(0));
        systemUs.setAttIpStart(null);
        systemUs.setAttIpEnd(null);
        systemUs.setAuthChangeSignFileFlag(Integer.valueOf(0));
        systemUs.setDefaultPortal(null);
        systemUs.setSendtoall(Integer.valueOf(1));
        systemUs.setMsgSendUserNum(OaSystemConstant.ZERO);
        systemUs.setMsgAwokeSetup(null);
        systemUs.setMsgAwokeUpdate(Integer.valueOf(1));
        systemUs.setWeatherCity(null);
        systemUs.setLastTime(null);
        Session hiSession = null;
        Transaction tx = null;
        try {
            hiSession = TransactionManager.getInstance().getCurrentSession();
            tx = hiSession.beginTransaction();
            (new HiMainDao(hiSession)).persist(systemUs);
            tx.commit();
            return systemUs;
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null)
                tx.rollback();
            return null;
        } finally {
            if (hiSession != null)
                hiSession.close();
        }
    }

    public void insertServiceLog(String operate, String moduleName, String entityName, int flag) {
        if (this.fakerMark)
            return;
        String loginModelOther = "(OTHER)";
        try {
            writeLog(
                    StringUtils.defaultIfEmpty(operate, ""), this.webUsers
                            .getUserName() + ((loginModelMap.containsKey(Integer.valueOf(this.loginMode)) && loginModelMap.get(Integer.valueOf(this.loginMode)) != null) ? loginModelMap.get(Integer.valueOf(this.loginMode)) : loginModelOther), entityName, moduleName, "", this.ip, this.webUsers

                            .getUserShowId(), this.companyId, flag);
        } catch (OaException e) {
            e.printStackTrace();
        }
    }

    public String getIpAddr(HttpServletRequest request) {
         String ip = request.getHeader("X-Forwarded-For");

         if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {

             ip = request.getHeader("Proxy-Client-IP");
         }
         if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {

              ip = request.getHeader("WL-Proxy-Client-IP");
         }
         if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {

                ip = request.getRemoteAddr();
         }
          return ip;
     }

    private void writeLog(String operate, String memo1, String memo2, String memo3, String httpSessionId, String clientIp, String userShowId, String subCompanyId, int flag) throws OaException {
        if (this.fakerMark)
            return;
        String sql = "insert into system_log (system_log_id, adate, user_id, ip, operate, flag, memo1, memo2, memo3, session_id,sub_company_id)values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
        List<Object> valueAry = new LinkedList();
        valueAry.add(OaTools.gainedNo());
        valueAry.add(new Date());
        valueAry.add(userShowId);
        valueAry.add(clientIp);


        valueAry.add((operate == null) ? "" : operate);
        valueAry.add(Integer.valueOf(flag));
        valueAry.add((memo1 == null) ? "" : memo1);
        valueAry.add((memo2 == null) ? "" : memo2);
        valueAry.add((memo3 == null) ? "" : memo3);
        valueAry.add((httpSessionId == null) ? "" : httpSessionId);
        valueAry.add((subCompanyId == null) ? "" : subCompanyId);
        try {
            (new HiMainDao()).executeBySQL(sql, valueAry);
        } catch (Exception e) {
            e.printStackTrace();
            throw new OaException("MSGmanager00020", "保存数据错误。");
        }
    }

    public JSONObject toStringMap() {
        JSONObject map = new JSONObject();
        map.put("languageType", this.languageType);
        map.put("styleType", this.styleType);
        map.put("imageRootCommPath", this.imageRootCommPath);
        map.put("imageCommPath", this.imageCommPath);
        map.put("imageUserPath", this.imageUserPath);
        map.put("imagePath", this.imagePath);
        map.put("cssCommPath", this.cssCommPath);
        map.put("cssPath", this.cssPath);
        map.put("jsCommPath", this.jsCommPath);
        map.put("jsPath", this.jsPath);
        map.put("rankGroup", this.rankGroup);
        if (this.webUsers != null)
            map.put("webUsers", this.webUsers.toStringMap());
        if (this.agentUsers != null)
            map.put("agentUsers", this.agentUsers.toStringMap());
        map.put("superManagerFlg", Boolean.valueOf(this.superManagerFlg));
        map.put("subCompanyManagerFlg", Boolean.valueOf(this.subCompanyManagerFlg));
        map.put("tryoutVersionInfo", this.tryoutVersionInfo);
        map.put("ip", this.ip);
        map.put("loginMode", Integer.valueOf(this.loginMode));
        map.put("roleId", this.roleId);
        map.put("groupIdList", this.groupIdList);
        map.put("companyId", this.companyId);
        map.put("userOsType", this.userOsType);
        map.put("browseType", this.browseType);
        map.put("browseVersion", Float.valueOf(this.browseVersion));
        map.put("fakerMark", Boolean.valueOf(this.fakerMark));
        return map;
    }

    private String checkJsoKey(JSONObject jso, String key) {
        if (jso.containsKey(key))
            return jso.getString(key);
        return null;
    }

    public void setDatafromJSON(JSONObject json) {
        this.languageType = checkJsoKey(json, "languageType");
        this.styleType = checkJsoKey(json, "styleType");
        this.imageRootCommPath = checkJsoKey(json, "imageRootCommPath");
        this.imageCommPath = checkJsoKey(json, "imageCommPath");
        this.imageUserPath = checkJsoKey(json, "imageUserPath");
        this.imagePath = checkJsoKey(json, "password");
        this.cssCommPath = checkJsoKey(json, "cssCommPath");
        this.cssPath = checkJsoKey(json, "cssPath");
        this.jsCommPath = checkJsoKey(json, "jsCommPath");
        this.jsPath = checkJsoKey(json, "jsPath");
        this.rankGroup = checkJsoKey(json, "rankGroup");
        this.rankGroup = "'" + this.rankGroup + "'";
        if (json.containsKey("webUsers")) {
            this.webUsers = new HiDbUserUser();
            this.webUsers.setDatafromJSON(json.getJSONObject("webUsers"));
        }
        if (json.containsKey("agentUsers")) {
            this.agentUsers = new HiDbUserUser();
            this.agentUsers.setDatafromJSON(json.getJSONObject("agentUsers"));
        }
        if (StringUtils.isNotBlank(checkJsoKey(json, "superManagerFlg"))) {
            this.superManagerFlg = json.getBoolean("superManagerFlg");
        } else {
            this.superManagerFlg = false;
        }
        if (StringUtils.isNotBlank(checkJsoKey(json, "subCompanyManagerFlg"))) {
            this.subCompanyManagerFlg = json.getBoolean("subCompanyManagerFlg");
        } else {
            this.subCompanyManagerFlg = false;
        }
        this.tryoutVersionInfo = checkJsoKey(json, "tryoutVersionInfo");
        this.ip = checkJsoKey(json, "ip");
        if (StringUtils.isNotBlank(checkJsoKey(json, "loginMode"))) {
            this.loginMode = json.getInt("loginMode");
        } else {
            this.loginMode = 0;
        }
        this.roleId = checkJsoKey(json, "roleId");
        this.groupIdList = checkJsoKey(json, "groupIdList");
        this.companyId = checkJsoKey(json, "companyId");
        this.userOsType = checkJsoKey(json, "userOsType");
        this.browseType = checkJsoKey(json, "browseType");
        if (StringUtils.isNotBlank(checkJsoKey(json, "browseVersion"))) {
            this.browseVersion = json.getInt("browseVersion");
        } else {
            this.loginMode = 0;
        }
        if (StringUtils.isNotBlank(checkJsoKey(json, "fakerMark"))) {
            this.fakerMark = json.getBoolean("fakerMark");
        } else {
            this.fakerMark = false;
        }
    }
}
