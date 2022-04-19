package com.oa8000.appservice.httraced.decode;

import com.oa8000.appservice.htentrance.HtCheckMethodPermission;
import com.oa8000.proxy.comm.HiUserInfo;
import com.oa8000.proxy.exception.OaException;
import java.lang.reflect.Method;

public class HtTraceViewService1Decode {
    public void inputMessage(Method method, HiUserInfo userInfo, Object[] objects) throws OaException {
        String[] checkButtonKey = null;
        String[] checkPageKey = null;
        boolean noNeedCheckFlg = false;
        if ("getTraceInstanceIndexByReadList".equals(method.getName())) {
            noNeedCheckFlg = true;
        }
        if ("getTraceInstanceIndexByWaitList".equals(method.getName())) {
            noNeedCheckFlg = true;
        }
        if ("getTraceInstanceIndexByManageList".equals(method.getName())) {
            noNeedCheckFlg = true;
        }
        if ("getTraceInstanceIndexByHandleList".equals(method.getName())) {
            noNeedCheckFlg = true;
        }
        if ("doWaitingHandleSendToUser".equals(method.getName())) {
            noNeedCheckFlg = true;
        }
        if ("getWaitUndertakeCount".equals(method.getName())) {
            noNeedCheckFlg = true;
        }
        if ("fetchSystemControlTraceData".equals(method.getName())) {
            noNeedCheckFlg = true;
        }
        if ("getMyFileCount".equals(method.getName())) {
            noNeedCheckFlg = true;
        }
        if (!noNeedCheckFlg && checkButtonKey == null && checkPageKey == null)
            throw new OaException("MSGcommon00035", "无权访问，请联系管理员");
        if (!noNeedCheckFlg)
            (new HtCheckMethodPermission()).checkMethodPermission(method.getName(), checkButtonKey, checkPageKey, userInfo);
    }
}
