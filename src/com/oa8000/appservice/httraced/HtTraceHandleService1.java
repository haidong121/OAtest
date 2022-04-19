package com.oa8000.appservice.httraced;

import com.cnpower.base.OaBaseSysInfo;
import com.cnpower.base.OaBaseTools;
import com.oa8000.appservice.htproject.HtProjectService;
import com.oa8000.appservice.htseal.HtSealService;
import com.oa8000.appservice.httrace.HtTraceBaseService;
import com.oa8000.appservice.ofdreader.UrlUtils;
import com.oa8000.htarchive.htarchive01.manager.HtArchive01NewManager;
import com.oa8000.htcrm.htcrm02.HtCrmColumnManager;
import com.oa8000.httrace.handler.detail.HtTraceDetailHandler;
import com.oa8000.httrace.handler.mark.HtTraceDetailEnum;
import com.oa8000.httrace.handler.util.DetailUtil;
import com.oa8000.httrace.httrace00.HtTrace00Constant;
import com.oa8000.httrace.httrace01.manager.HtTrace01ActionDetailManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01ActionManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01AgentManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01CategoryManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01DefineNoManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01KeyWordManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01QuickMindManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01RedHeadManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01SqlProcess;
import com.oa8000.httrace.httrace01.manager.HtTrace01TableCssManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01UserManager;
import com.oa8000.httrace.httrace02.manager.HtTrace02PathManager;
import com.oa8000.httrace.httrace03.manager.HtNGTrace03TemplateManager;
import com.oa8000.httrace.httrace03.manager.HtTrace03TemplateManager;
import com.oa8000.httrace.httrace05.manager.HtNGTrace05HandleTraceManager;
import com.oa8000.appservice.httraced.HtNGTrace05HandleTraceManager2;
import com.oa8000.httrace.httrace05.manager.HtNGTrace05TraceBaseManager;
import com.oa8000.httrace.httrace05.manager.HtNGTrace05ViewTraceManager;
import com.oa8000.httrace.httrace05.manager.HtTrace05TraceOfficeManager;
import com.oa8000.httrace.httrace05.manager.HtTrace05TraceTemplateManager;
import com.oa8000.httrace.httrace06.manager.HtNGTrace06EditTraceAttachmentManager;
import com.oa8000.httrace.httrace08.manager.HtTrace08RunClassManager;
import com.oa8000.httrace.httrace10.manager.HtTrace10Manager;
import com.oa8000.httrace.msgTools.HtTraceMsgTools;
import com.oa8000.httraceform.httraceform00.FormData;
import com.oa8000.httraceform.httraceform00.FormDataGlobalInfo;
import com.oa8000.httraceform.httraceform01.manager.HtTraceForm01Manager;
import com.oa8000.httraceform.httraceform01.manager.HtTraceFormLogManager;
import com.oa8000.httraceform.httraceform04.manager.HtTraceSystemViewManager;
import com.oa8000.httraceform.httraceform05.HtTraceForm05Manager;
import com.oa8000.indepservice.docconvert.OfficeConvertService;
import com.oa8000.proxy.base.HibernatePage;
import com.oa8000.proxy.comm.HiOaMainClass;
import com.oa8000.proxy.comm.HiOaPubSystemMsg;
import com.oa8000.proxy.comm.HiUserInfo;
import com.oa8000.proxy.comm.JasonUtility;
import com.oa8000.proxy.comm.OaLanguageManager;
import com.oa8000.proxy.comm.OaPubDateManager;
import com.oa8000.proxy.comm.OaPubptDesEncrypter1;
import com.oa8000.proxy.comm.OaTools;
import com.oa8000.proxy.comm.PageModel;
import com.oa8000.proxy.comm.SortModel;
import com.oa8000.proxy.comm.file.HiFile;
import com.oa8000.proxy.dao.HiMainDao;
import com.oa8000.proxy.db.HiDbArchiveFile;
import com.oa8000.proxy.db.HiDbClassDictDetail;
import com.oa8000.proxy.db.HiDbCommonDir;
import com.oa8000.proxy.db.HiDbFileStorage;
import com.oa8000.proxy.db.HiDbHrDept;
import com.oa8000.proxy.db.HiDbHrStaffInfo;
import com.oa8000.proxy.db.HiDbHtCrmColumnDic;
import com.oa8000.proxy.db.HiDbProjectStartRecord;
import com.oa8000.proxy.db.HiDbSystemRegister;
import com.oa8000.proxy.db.HiDbTraceActionDict;
import com.oa8000.proxy.db.HiDbTraceCategory;
import com.oa8000.proxy.db.HiDbTraceCheckLoginLog;
import com.oa8000.proxy.db.HiDbTraceDefaultRole;
import com.oa8000.proxy.db.HiDbTraceDefineField;
import com.oa8000.proxy.db.HiDbTraceDefineMind;
import com.oa8000.proxy.db.HiDbTraceForwardDetail;
import com.oa8000.proxy.db.HiDbTraceHandout;
import com.oa8000.proxy.db.HiDbTraceHandoutViewList;
import com.oa8000.proxy.db.HiDbTraceInstanceDetail;
import com.oa8000.proxy.db.HiDbTraceInstanceIndex;
import com.oa8000.proxy.db.HiDbTraceInstancePath;
import com.oa8000.proxy.db.HiDbTraceKey;
import com.oa8000.proxy.db.HiDbTracePathDetail;
import com.oa8000.proxy.db.HiDbTracePathIndex;
import com.oa8000.proxy.db.HiDbTracePublish;
import com.oa8000.proxy.db.HiDbTraceRedHead;
import com.oa8000.proxy.db.HiDbTraceRelationReceive;
import com.oa8000.proxy.db.HiDbTraceSecretHandout;
import com.oa8000.proxy.db.HiDbTraceTableCss;
import com.oa8000.proxy.db.HiDbTraceTemplateDict;
import com.oa8000.proxy.db.HiDbTraceTemplateRelation;
import com.oa8000.proxy.db.HiDbTraceUndertakeDetail;
import com.oa8000.proxy.db.HiDbTraceUserDefineListNo;
import com.oa8000.proxy.db.HiDbTraceUserRole;
import com.oa8000.proxy.db.HiDbUserSetting;
import com.oa8000.proxy.db.HiDbUserUser;
import com.oa8000.proxy.exception.OaException;
import com.oa8000.proxy.tools.foundation.NSLog;
import com.oa8000.server.htfile.HtFileServer;
import com.oa8000.server.hthr.HtHrServer;
import com.oa8000.server.htjob.HtJobScheduleOperationServer;
import com.oa8000.server.htmsg.HtSendSystemMsgServer;
import com.oa8000.wpsOffice.FileEntity;
import com.oa8000.wpsOffice.wpsOfficeManager;
import com.oa8000.yozo.YozoManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class  HtTraceHandleService1 extends HtTraceBaseService {

    private final String errorMsg = (new HiOaPubSystemMsg()).message("MSGtrace00732", "处理出错");

    public String doWaitingHandleSendToUser(HiUserInfo userInfo, String hiTraceInstanceIndexId, String commitCode, String externalModulesDataJsonStr, String realUserRole, String fileListJson, String otherInstanceView, String olderUpdateTime, String detailRemarkForSave, String remark, String detailMark, String undertakeRemark, String detailUserList, String viewUserList, String otherData) {
        logNormalBatch(userInfo, "承办/阅办提交给其他人继续承办/阅办", "审批", HiDbTraceInstanceIndex.class, hiTraceInstanceIndexId, "traceTitle");
        try {
            boolean uploadAttachmentFlg;
            JSONObject returnJso = new JSONObject();
            String returnStr = null;
            if (isBlank(detailUserList) && isBlank(viewUserList)) {
                HiOaPubSystemMsg os = new HiOaPubSystemMsg(userInfo.languageType);
                returnJso.put("alertMsg", os.message("MSGtrace00039", "请选择人员。"));
                return returnJsonString(returnJso);
            }
            if (isNotBlank(detailUserList) && (!detailUserList.startsWith(";") || detailUserList.endsWith(";")))
                detailUserList = (";" + detailUserList + ";").replace(";;", ";");
            if (isNotBlank(viewUserList) && (!viewUserList.startsWith(";") || viewUserList.endsWith(";")))
                viewUserList = (";" + viewUserList + ";").replace(";;", ";");
            HtNGTrace05HandleTraceManager2 traceManager = new HtNGTrace05HandleTraceManager2(userInfo.languageType);
            commWaitingGetTraceInfo(userInfo, hiTraceInstanceIndexId, 2);
            String checkReturnMsg = commCheckAlreadyUpdate(userInfo, this.hiTraceInstanceIndex, olderUpdateTime);
            if (checkReturnMsg != null)
                return checkReturnMsg;
            boolean isUnderTakePage = "2".equals(detailMark);
            FormData currentFormData = (new HtTraceForm01Manager(userInfo.languageType)).getCommitFormData(commitCode, this.hiTraceInstanceIndex);
            if (currentFormData != null)
                currentFormData.setTraceMind(remark);
            if (isUnderTakePage) {
                uploadAttachmentFlg = true;
            } else {
                uploadAttachmentFlg = commWaitingGetCanUploadAttachmentFile(userInfo, this.hiTraceInstancePath);
                commWaitingSaveRealUserRole(userInfo, this.hiTraceInstanceIndex, this.hiTraceInstanceDetail, realUserRole);
            }
            setOfficeTraceTitle(this.hiTraceInstanceIndex, otherData);
            List fileListAry = commGetAttachmentFileList(this.hiTraceInstanceIndex, fileListJson, uploadAttachmentFlg);
            if (isNotBlank(detailRemarkForSave) && this.hiTraceInstanceDetail != null)
                this.hiTraceInstanceDetail.setDetailRemark(detailRemarkForSave);
            if (isNotBlank(remark) && this.hiTraceInstanceDetail != null)
                this.hiTraceInstanceDetail.setTraceMind(remark);
            if (isUnderTakePage) {
                HiDbTraceUndertakeDetail traceUndertakeDetail = new HiDbTraceUndertakeDetail();
                traceUndertakeDetail.setUndertakeRemark(undertakeRemark);
                traceUndertakeDetail.setTraceInstanceIndexId(this.hiTraceInstanceIndex.getTraceInstanceIndexId());
                traceUndertakeDetail.setUndertakeUserId(userInfo.webUsers.getUserId());
                traceManager.createTraceUndertakeDetail(traceUndertakeDetail);
            }
            if (!isUnderTakePage) {
                if (isNotBlank(viewUserList)) {
                    this.hiTraceInstanceIndex = getObject(HiDbTraceInstanceIndex.class, this.hiTraceInstanceIndex.getTraceInstanceIndexId());
                    HtTrace01ActionManager actionManager = new HtTrace01ActionManager(userInfo.languageType);
                    HiDbTraceActionDict traceActionDict = actionManager.getTraceActionDict(this.hiTraceInstanceDetail.getTraceActionDictId());
                    if (traceActionDict == null)
                        traceActionDict = actionManager.getTraceActionDictUseForNoAction(this.hiTraceInstanceIndex.getTraceInstanceIndexId());
                    traceManager.doSendTraceToSelect(this.hiTraceInstanceIndex, this.hiTraceInstancePath, this.hiTraceInstanceDetail, traceActionDict, currentFormData, externalModulesDataJsonStr, 1, viewUserList, userInfo.webUsers
                            .getUserId(), userInfo.webUsers
                            .getDeptId(), realUserRole, userInfo.languageType, uploadAttachmentFlg, fileListAry, false);
                } else {
                    returnStr = doWaitingHandleGoOnNextStep(userInfo, hiTraceInstanceIndexId, commitCode, externalModulesDataJsonStr, realUserRole, fileListJson, otherInstanceView, "", detailRemarkForSave, remark, detailMark, undertakeRemark, otherData);
                }
            } else {
                saveAttachment(userInfo, this.hiTraceInstanceIndex, this.hiTraceInstanceDetail, fileListAry, detailMark);
            }
            commSaveOtherInstanceView(this.hiTraceInstanceIndex, otherInstanceView);
            HtTraceMsgTools msgTools = new HtTraceMsgTools(userInfo.languageType);
            msgTools.addDeleteMsg("deleteAllAwokeMsgByTracer", new Class[] { String.class, String.class, String.class }, new Object[] { this.hiTraceInstanceIndex

                    .getTraceInstanceIndexId(), userInfo.webUsers.getUserId(), "traceHandle" });
            msgTools.executeAllMsg(userInfo.isFaker());
            traceManager.actAddHandleUser(this.hiTraceInstanceIndex, currentFormData, externalModulesDataJsonStr, detailUserList, userInfo.webUsers
                    .getUserId(), userInfo.webUsers.getDeptId());
            if (this.hiTraceInstanceDetail != null) {
                HtJobScheduleOperationServer htScheduleOperation = new HtJobScheduleOperationServer();
                htScheduleOperation.deleteAndStopJobByLinkIdAndDetailForStep(this.hiTraceInstanceIndex
                        .getTraceInstanceIndexId(), this.hiTraceInstanceDetail.getTraceInstanceDetailId(), null);
            }
            traceManager.insertTraceEventLog(userInfo, "送其他人承办或阅办", this.hiTraceInstanceIndex
                    .getTraceInstanceIndexId());
            if (returnStr == null) {
                returnJso.put("alertMsg", traceManager.getReturnAlertMsg());
                returnJso.put("closeFlg", "1");
                return returnJsonString(returnJso);
            }
            return returnStr;
        } catch (OaException e) {
            e.printStackTrace();
            return returnErrorJsonString(e.message());
        } catch (Exception e) {
            e.printStackTrace();
            return returnErrorJsonString(this.errorMsg);
        }
    }
    private String commCheckAlreadyUpdate(HiUserInfo userInfo, HiDbTraceInstanceIndex hiTraceInstanceIndex, String olderUpdateTime) throws Exception {
        JSONObject jso = new JSONObject();
        if (isNotBlank(olderUpdateTime)) {
            boolean withoutUpdateFlg = (new HtNGTrace05HandleTraceManager(userInfo.languageType)).checkIndexWithoutUpdate(hiTraceInstanceIndex
                    .getTraceInstanceIndexId(), olderUpdateTime, userInfo);
            jso.put("olderUpdateTime", Long.valueOf(hiTraceInstanceIndex.getUpdateTime().getTime()));
            if (!withoutUpdateFlg) {
                HiOaPubSystemMsg os = new HiOaPubSystemMsg(userInfo.languageType);
                jso.put("alertMsg", os.message("MSGtrace00449", "您所打开的审批单已被他人同步办理了，现在为您重新加载数据。"));
                        jso.put("closeFlg", "1");
                return returnJsonString(jso);
            }
        } else {
            HiOaPubSystemMsg os = new HiOaPubSystemMsg(userInfo.languageType);
            jso.put("alertMsg", os.message("MSGtrace00449", "您所打开的审批单已被他人同步办理了，现在为您重新加载数据。"));
                    jso.put("closeFlg", "1");
            return returnJsonString(jso);
        }
        return null;
    }
    private boolean commWaitingGetCanUploadAttachmentFile(HiUserInfo userInfo, HiDbTraceInstancePath hiTraceInstancePath) throws Exception {
        HtTrace01ActionManager actionManager = new HtTrace01ActionManager(userInfo.languageType);
        if (hiTraceInstancePath == null)
            return true;
        HiDbTraceActionDict hiTracActionDict = actionManager.getTraceActionDict(hiTraceInstancePath.getTraceActionDictId());
        HtTrace01ActionDetailManager detailManager = new HtTrace01ActionDetailManager(hiTracActionDict, true);
        return detailManager.getAttachmentFlg();
    }
    private void commWaitingSaveRealUserRole(HiUserInfo userInfo, HiDbTraceInstanceIndex hiTraceInstanceIndex, HiDbTraceInstanceDetail hiTraceInstanceDetail, String realUserRole) throws Exception {
        if (hiTraceInstanceIndex != null && realUserRole != null && hiTraceInstanceDetail != null)
            if (hiTraceInstanceDetail.getAgentUser() != null && hiTraceInstanceDetail.getAgentUser().startsWith("TRACE")) {
                (new HtNGTrace05HandleTraceManager(userInfo.languageType)).doSaveTraceInstanceRoleDetail(hiTraceInstanceDetail
                        .getTraceInstanceIndexId(), hiTraceInstanceDetail.getTraceInstanceDetailId(), userInfo.webUsers
                        .getUserId(), null, null, hiTraceInstanceDetail.getAgentUser().replace("TRACE", ""), realUserRole);
            } else {
                (new HtNGTrace05HandleTraceManager(userInfo.languageType)).doSaveTraceInstanceRoleDetail(hiTraceInstanceDetail
                        .getTraceInstanceIndexId(), hiTraceInstanceDetail.getTraceInstanceDetailId(), userInfo.webUsers
                        .getUserId(), realUserRole, null, null, null);
            }
    }
    private void setOfficeTraceTitle(HiDbTraceInstanceIndex hiTraceInstanceIndex, String otherData) {
        if (hiTraceInstanceIndex != null && StringUtils.isNotBlank(otherData))
            try {
                if (hasKey(otherData, "traceTitle")) {
                    JSONObject obj = JSONObject.fromObject(otherData);
                    String title = obj.getString("traceTitle");
                    hiTraceInstanceIndex.setTraceTitle(title);
                }
            } catch (Exception e) {
                return;
            }
    }
    public static boolean hasKey(String otherData, String key) {
        try {
            JSONObject obj = JSONObject.fromObject(otherData);
            return obj.has(key);
        } catch (Exception e) {
            return false;
        }
    }
    private List commGetAttachmentFileList(HiDbTraceInstanceIndex hiTraceInstanceIndex, String fileListJson, boolean uploadAttachmentFlg) throws Exception {
        List fileListAry = null;
        if (uploadAttachmentFlg)
            fileListAry = convertAtachmentAryForNoEncode(fileListJson, "trace");
        return fileListAry;
    }
    public String doWaitingHandleGoOnNextStep(HiUserInfo userInfo, String hiTraceInstanceIndexId, String commitCode, String externalModulesDataJsonStr, String realUserRole, String fileListJson, String otherInstanceView, String olderUpdateTime, String detailRemarkForSave, String remark, String detailMark, String undertakeRemark, String otherData) {
        logNormalBatch(userInfo, "承办或阅办提交下一步", "审批", HiDbTraceInstanceIndex.class, hiTraceInstanceIndexId, "traceTitle");
        try {
            commWaitingGetTraceInfo(userInfo, hiTraceInstanceIndexId, 2);
            JSONObject returnJso = new JSONObject();
            FormData currentFormData = (new HtTraceForm01Manager(userInfo.languageType)).getCommitFormData(commitCode, this.hiTraceInstanceIndex);
            if (currentFormData != null)
                currentFormData.setTraceMind(remark);
            HtNGTrace05HandleTraceManager traceManager = new HtNGTrace05HandleTraceManager(userInfo.languageType);
            traceManager.insertTraceEventLog(userInfo, "送其他人承办或阅办", this.hiTraceInstanceIndex
                    .getTraceInstanceIndexId());
            if (!"2".equals(detailMark))
                return doWaitingHandleGoOn(userInfo, hiTraceInstanceIndexId, commitCode, externalModulesDataJsonStr, realUserRole, fileListJson, otherInstanceView, olderUpdateTime, detailRemarkForSave, remark,
                        Integer.valueOf(0), otherData);
            HiDbTraceUndertakeDetail traceUndertakeDetail = new HiDbTraceUndertakeDetail();
            traceUndertakeDetail.setUndertakeRemark(undertakeRemark);
            traceUndertakeDetail.setTraceInstanceIndexId(hiTraceInstanceIndexId);
            traceUndertakeDetail.setUndertakeUserId(userInfo.webUsers.getUserId());
            commWaitingGetTraceInfo(userInfo, hiTraceInstanceIndexId, 2);
            String checkReturnMsg = commCheckAlreadyUpdate(userInfo, this.hiTraceInstanceIndex, olderUpdateTime);
            if (checkReturnMsg != null)
                return checkReturnMsg;
            List fileListAry = commGetAttachmentFileList(this.hiTraceInstanceIndex, fileListJson, true);
            if (isNotBlank(detailRemarkForSave) && this.hiTraceInstanceDetail != null)
                this.hiTraceInstanceDetail.setDetailRemark(detailRemarkForSave);
            if (isNotBlank(remark) && this.hiTraceInstanceDetail != null)
                this.hiTraceInstanceDetail.setTraceMind(remark);
            setOfficeTraceTitle(this.hiTraceInstanceIndex, otherData);
            commSaveOtherInstanceView(this.hiTraceInstanceIndex, otherInstanceView);
            traceManager.doSaveUnderTakeInfo(this.hiTraceInstanceIndex, currentFormData, traceUndertakeDetail, userInfo.webUsers
                    .getUserId(), true, fileListAry, false);
            if (this.hiTraceInstanceDetail != null) {
                HtJobScheduleOperationServer htScheduleOperation = new HtJobScheduleOperationServer();
                htScheduleOperation.deleteAndStopJobByLinkIdAndDetailForStep(this.hiTraceInstanceIndex
                        .getTraceInstanceIndexId(), this.hiTraceInstanceDetail.getTraceInstanceDetailId(), null);
            }
            HtTraceMsgTools msgTools = new HtTraceMsgTools(userInfo.languageType);
            msgTools.addDeleteMsg("deleteAllAwokeMsgByTracer", new Class[] { String.class, String.class, String.class }, new Object[] { this.hiTraceInstanceIndex

                    .getTraceInstanceIndexId(), userInfo.webUsers.getUserId(), "traceHandle" });
            msgTools.executeAllMsg(userInfo.isFaker());
            returnJso.put("alertMsg", traceManager.getReturnAlertMsg());
            returnJso.put("closeFlg", "1");
            return returnJsonString(returnJso);
        } catch (OaException e) {
            e.printStackTrace();
            return returnErrorJsonString(e.message());
        } catch (Exception e) {
            e.printStackTrace();
            return returnErrorJsonString(this.errorMsg);
        }
    }
    private void saveAttachment(HiUserInfo userInfo, HiDbTraceInstanceIndex hiTraceInstanceIndex, HiDbTraceInstanceDetail hiTraceInstanceDetail, List fileListAry, String detailMark) throws OaException {
        Number attachmentSaveMode = hiTraceInstanceIndex.getAttachmentSaveMode();
        HtTrace05TraceOfficeManager traceOfficeManager = new HtTrace05TraceOfficeManager(userInfo.languageType);
        if (fileListAry == null || fileListAry.size() == 0)
            return;
        fileListAry = JasonUtility.jSONArrayToBeanList(JasonUtility.listToJSONArray(fileListAry), HiFile.class);
        if ("2".equals(detailMark)) {
            traceOfficeManager.saveAttachment(hiTraceInstanceIndex, fileListAry, userInfo.webUsers.getUserId());
        } else if (hiTraceInstanceDetail == null || (attachmentSaveMode != null && attachmentSaveMode.intValue() == 1)) {
            traceOfficeManager.saveAttachment(hiTraceInstanceIndex, fileListAry, userInfo.webUsers.getUserId());
        } else {
            traceOfficeManager.saveAttachment(hiTraceInstanceDetail, fileListAry, userInfo.webUsers.getUserId());
        }
    }
    private void commSaveOtherInstanceView(HiDbTraceInstanceIndex hiTraceInstanceIndex, String otherInstanceView) {
        if (StringUtils.isBlank(otherInstanceView))
            return;
        hiTraceInstanceIndex.setOtherInstanceView(otherInstanceView);
    }
    private String returnErrorJsonString(String str) {
        JSONObject returnJson = new JSONObject();
        returnJson.put("alertMsg", str);
        returnJson.put("errorFlg", "1");
        return returnJsonString(returnJson);
    }
    private String doWaitingHandleGoOn(HiUserInfo userInfo, String hiTraceInstanceIndexId, String commitCode, String externalModulesDataJsonStr, String realUserRole, String fileListJson, String otherInstanceView, String olderUpdateTime, String detailRemarkForSave, String remark, Integer state, String otherData) throws Exception {
        try {
            HtNGTrace05HandleTraceManager traceManager = new HtNGTrace05HandleTraceManager(userInfo.languageType);
            commWaitingGetTraceInfo(userInfo, hiTraceInstanceIndexId, 2);
            String realCreaterRole = traceManager.getRealTraceRole(hiTraceInstanceIndexId, null);
            FormData currentFormData = (new HtTraceForm01Manager(userInfo.languageType)).getCommitFormData(commitCode, this.hiTraceInstanceIndex);
            if (currentFormData != null)
                currentFormData.setTraceMind(remark);
            commSaveOtherInstanceView(this.hiTraceInstanceIndex, otherInstanceView);
            if (isNotBlank(remark) && this.hiTraceInstanceDetail != null)
                this.hiTraceInstanceDetail.setTraceMind(remark);
            Map nextStepMap = traceManager.doGetNextSteps(this.hiTraceInstanceIndex, this.hiTraceInstancePath, this.hiTracePathDetail, currentFormData, userInfo.webUsers
                    .getUserId(), null, realCreaterRole, realUserRole, state, null);
            HtNGTrace05HandleTraceManager nGTrace05HandleTraceManager = new HtNGTrace05HandleTraceManager(userInfo.languageType);
            HtTraceMsgTools msgTools = new HtTraceMsgTools(userInfo.languageType);
            msgTools.addDeleteMsg("deleteAllAwokeMsgByTracer", new Class[] { String.class, String.class, String.class }, new Object[] { this.hiTraceInstanceIndex

                    .getTraceInstanceIndexId(), userInfo.webUsers.getUserId(), "traceWait" });
            msgTools.executeAllMsg(userInfo.isFaker());
            boolean uploadAttachmentFlg = commWaitingGetCanUploadAttachmentFile(userInfo, this.hiTraceInstancePath);
            List fileListAry = commGetAttachmentFileList(this.hiTraceInstanceIndex, fileListJson, uploadAttachmentFlg);
            if (isNotBlank(detailRemarkForSave) && this.hiTraceInstanceDetail != null)
                this.hiTraceInstanceDetail.setDetailRemark(detailRemarkForSave);
            Map result = traceManager.doGotoNextStep(nextStepMap, this.hiTraceInstanceIndex, this.hiTraceInstancePath, this.hiTraceInstanceDetail, this.hiTracePathDetail, state
                    .intValue(), currentFormData, externalModulesDataJsonStr, new HashMap<>(), userInfo.webUsers
                    .getUserId(), userInfo.webUsers.getDeptId(), realCreaterRole, realUserRole, uploadAttachmentFlg, fileListAry, false);
            JSONObject jso = new JSONObject();
            if (result != null) {
                if ("STEP".equals(result.get("KEY")) || "CON".equals(result.get("KEY"))) {
                    boolean needChooseTracer = nGTrace05HandleTraceManager.checkNextPathDetailListNeedChooseTracer(this.hiTraceInstanceIndex, nextStepMap, currentFormData, userInfo);
                    if (!needChooseTracer && this.hiTracePathDetail != null && this.hiTracePathDetail
                            .getConcurrentFlag().intValue() == 2) {
                        jso = nGTrace05HandleTraceManager.sendForMustGoMoreStep(jso, userInfo, nextStepMap, this.hiTraceInstanceIndex, this.hiTraceInstancePath, this.hiTraceInstanceDetail, currentFormData, externalModulesDataJsonStr, new HashMap<>(), uploadAttachmentFlg, fileListAry, realUserRole);
                        jso.put("closeFlg", "1");
                    } else {
                        jso = nGTrace05HandleTraceManager.acSelectedStepBtn(userInfo, this.hiTraceInstanceIndex, this.hiTraceInstancePath, nextStepMap, fileListAry, currentFormData, commitCode, realCreaterRole, realUserRole);
                        jso.put("KEY", result.get("KEY"));
                    }
                    jso.put("pathTypeFlg", Boolean.valueOf(!"CON".equals(result.get("KEY"))));
                    return returnJsonString(jso);
                }
                if ("FREE".equals(result.get("KEY"))) {
                    jso = nGTrace05HandleTraceManager.actFreePathBtn(this.hiTraceInstanceIndex, this.hiTraceInstancePath, nextStepMap, commitCode);
                    jso.put("KEY", result.get("KEY"));
                    return returnJsonString(jso);
                }
                if ("USER".equals(result.get("KEY"))) {
                    jso = nGTrace05HandleTraceManager.actSelectUserBtn(userInfo, this.hiTraceInstanceIndex, this.hiTraceInstancePath, nextStepMap, fileListAry, currentFormData, commitCode);
                    jso.put("KEY", result.get("KEY"));
                    return returnJsonString(jso);
                }
            }
            (new HtNGTrace05HandleTraceManager(userInfo.languageType)).insertTraceEventLog(userInfo, "提交下一步", this.hiTraceInstanceIndex
                    .getTraceInstanceIndexId());
            String msg = traceManager.getReturnAlertMsg();
            String makeNewTraceMapData = getMakeNewTraceMapData(userInfo, traceManager, msg, jso);
            if (makeNewTraceMapData == null) {
                jso.put("alertMsg", msg + traceManager.newTraceMsg + traceManager.newTraceErrorMsg);
                jso.put("closeFlg", "1");
                return returnJsonString(jso);
            }
            return makeNewTraceMapData;
        } catch (OaException e) {
            e.printStackTrace();
            return returnErrorJsonString(e.message());
        } catch (Exception e) {
            e.printStackTrace();
            return returnErrorJsonString(this.errorMsg);
        }
    }
    private String getMakeNewTraceMapData(HiUserInfo userInfo, HtNGTrace05HandleTraceManager traceManager, String msg, JSONObject jso) {
        HiOaPubSystemMsg os = new HiOaPubSystemMsg(userInfo.languageType);
        HashMap makeNewTraceMap = traceManager.makeNewTraceMap;
        if (makeNewTraceMap.get("initTraceFlg") != null && "MakeNewTracePageId".equals(makeNewTraceMap.get("initTraceFlg"))) {
            String openPage = (String)makeNewTraceMap.get("tracePageId");
            if (openPage == null) {
                jso.put("alertMsg", msg + traceManager.newTraceMsg + os.message("MSGtrace00390", "没有可用流程，请联系管理员。"));
                        jso.put("closeFlg", "1");
                return returnJsonString(jso);
            }
            if ("HtTrace08MorePath".equals(openPage)) {
                HiDbTraceTemplateDict dict = (HiDbTraceTemplateDict)makeNewTraceMap.get("hiTraceTemplateDict");
                List<HiDbTracePathIndex> pathAry = (List)makeNewTraceMap.get("pathList");
                jso = traceManager.acSelectedPathBtn(makeNewTraceMap);
                jso.put("alertMsg", msg + traceManager.newTraceMsg + traceManager.newTraceErrorMsg);
                JSONArray jSONArray = new JSONArray();
                if (pathAry != null)
                    for (HiDbTracePathIndex eoTracePathIndex : pathAry) {
                        if (eoTracePathIndex == null)
                            continue;
                        JSONObject pathJso = new JSONObject();
                        pathJso.put("tracePathIndexId", eoTracePathIndex.getTracePathIndexId());
                        pathJso.put("pathTitle", eoTracePathIndex.getPathTitle());
                        pathJso.put("useFreePathFlg", eoTracePathIndex.getUseFreePathFlg());
                        jSONArray.add(pathJso);
                    }
                jso.put("traceTemplateDictId", dict.getTraceTemplateDictId());
                jso.put("templateTitle", dict.getTemplateTitle());
                jso.put("pathCount", Integer.valueOf((pathAry == null) ? 0 : pathAry.size()));
                jso.put("pathJsonAry", jSONArray);
                jso.put("agentUserId", makeNewTraceMap.get("agentUserId"));
                jso.put("initTraceFlg", "1");
                return returnJsonString(jso);
            }
            HiDbTraceInstanceIndex newTraceInstanceIndex = (HiDbTraceInstanceIndex)makeNewTraceMap.get("hiTraceInstanceIndex");
            Map initDataMap = (Map)makeNewTraceMap.get("initTraceDataMap");
            String initDataStr = HtTrace00Constant.mapToJsonString(initDataMap);
            jso.put("realOpenJsp", openPage + "Servlet.jsp");
            jso.put("initDataStr", initDataStr);
            List<HiDbTracePathIndex> pathList = (List<HiDbTracePathIndex>)makeNewTraceMap.get("pathList");
            HiDbTraceTemplateDict newTraceTemplateDict = getObject(HiDbTraceTemplateDict.class, newTraceInstanceIndex
                    .getTraceTemplateDictId());
            JSONArray returnJsonAry = new JSONArray();
            if (pathList != null)
                for (HiDbTracePathIndex eoTracePathIndex : pathList) {
                    if (eoTracePathIndex == null)
                        continue;
                    JSONObject pathJso = new JSONObject();
                    pathJso.put("tracePathIndexId", eoTracePathIndex.getTracePathIndexId());
                    pathJso.put("pathTitle", eoTracePathIndex.getPathTitle());
                    pathJso.put("useFreePathFlg", eoTracePathIndex.getUseFreePathFlg());
                    returnJsonAry.add(pathJso);
                }
            jso.put("traceTemplateDictId", newTraceInstanceIndex.getTraceTemplateDictId());
            jso.put("htmlFileName", newTraceInstanceIndex.getHtmlFileName());
            jso.put("templateTitle", newTraceTemplateDict.getTemplateTitle());
            jso.put("pathCount", Integer.valueOf((pathList == null) ? 0 : pathList.size()));
            jso.put("pathJsonAry", returnJsonAry);
            jso.put("agentUserId", makeNewTraceMap.get("agentUserId"));
            jso.put("traceInstanceIndexId", newTraceInstanceIndex.getTraceInstanceIndexId());
            jso.put("initTraceFlg", "1");
            jso.put("alertMsg", msg + traceManager.newTraceMsg + traceManager.newTraceErrorMsg);
            return returnJsonString(jso);
        }
        return null;
    }


}
