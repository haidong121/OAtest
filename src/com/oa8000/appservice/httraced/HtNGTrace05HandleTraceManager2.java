package com.oa8000.appservice.httraced;

import com.oa8000.appservice.htproject.HtProjectService;
import com.oa8000.htinvoice.constant.HtInvoiceConstant;
import com.oa8000.httrace.httrace01.manager.HtTrace01AgentManager;
import com.oa8000.httrace.httrace01.manager.HtTrace01DefineNoManager;
import com.oa8000.httrace.httrace03.manager.HtNGTrace03TemplateManager;
import com.oa8000.httrace.httrace03.manager.HtTrace03TemplateManager;
import com.oa8000.httrace.httrace05.manager.HtNGTrace05HandleTraceManager;
import com.oa8000.httrace.httrace05.manager.HtTrace05TraceOfficeManager;
import com.oa8000.httraceform.httraceform00.FormData;
import com.oa8000.httraceform.httraceform01.manager.HtTraceForm01Manager;
import com.oa8000.proxy.base.TransactionManager;
import com.oa8000.proxy.comm.HiOaMainClass;
import com.oa8000.proxy.comm.OaTools;
import com.oa8000.proxy.dao.HiMainDao;
import com.oa8000.proxy.db.*;
import com.oa8000.proxy.exception.OaException;
import com.oa8000.server.htinvoice.statechanger.HtInvoiceStateChanger;
import com.oa8000.server.htinvoice.statechanger.HtInvoiceStateChangerCreator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import java.util.*;

public class HtNGTrace05HandleTraceManager2 extends HtNGTrace05HandleTraceManager {

    private String alreadySaveDataIndexId = "";
    private HtInvoiceStateChanger invoiceStateChanger;
    public HtNGTrace05HandleTraceManager2(String languageType) {
        super(languageType);
    }

    public void actAddHandleUser(HiDbTraceInstanceIndex traceInstanceIndex, FormData currentFormData, String externalModulesDataJsonStr, String handlerIdStr, String currentUserId, String currentDeptId) throws OaException {
        (new HtTrace05TraceOfficeManager(this.languageType)).doInitCheckOffice(traceInstanceIndex, 1);
        String currentHandlerIdStr = traceInstanceIndex.getHandlerList();
        StringBuilder currentHandlerList = new StringBuilder((currentHandlerIdStr == null) ? ";" : currentHandlerIdStr);
        if (StringUtils.isNotBlank(handlerIdStr)) {
            List<String> handlerList = OaTools.partitionString(handlerIdStr, ';');
            for (String handlerId : handlerList) {
 //               if (StringUtils.isBlank(handlerId) || currentHandlerList.indexOf(handlerId) > 0)
      //              continue;
                currentHandlerList.append(handlerId).append(";");
            }
            this.returnAlertMsg = this.systemMsg.message("MSGtrace00433", "已交由所选人员继续承办");
        } else {
            this.returnAlertMsg = this.systemMsg.message("MSGtrace00335", "您的办理已完成");
        }
        traceInstanceIndex.setHandlerList(currentHandlerList.toString());
        Session hiSession = null;
        Transaction tx = null;
        try {
            saveData(currentFormData, traceInstanceIndex, currentUserId);
            hiSession = TransactionManager.getInstance().getCurrentSession();
            tx = hiSession.beginTransaction();
            inSaveDataAfter(traceInstanceIndex, (HiDbTraceInstancePath)null, (HiDbTraceInstanceDetail)null, currentFormData, externalModulesDataJsonStr, (Map)null, true, false, hiSession, currentUserId, currentDeptId);
            saveObjects(this.insertObjectList, this.updateObjectList, hiSession);
            clearDataAfterSaveData();
            traceInstanceIndex.setUpdateUserId(currentUserId);
            (new HiMainDao(hiSession)).update(traceInstanceIndex);
            tx.commit();
            getWaitingTraceUserMsg(traceInstanceIndex);
            delCopyTable(currentFormData);
            this.traceMsgTools.addInsertMsg("sendMsgToHandle", new Class[] { HiDbTraceInstanceIndex.class, String.class, String.class, String.class }, new Object[] { traceInstanceIndex, handlerIdStr, currentUserId, currentDeptId });
            this.traceMsgTools.executeAllMsg(isFakerUserFlg(traceInstanceIndex));
        } catch (OaException e) {
            e.printStackTrace();
            if (tx != null)
                tx.rollback();
            (new HtTraceForm01Manager(this.languageType)).rollBack(currentFormData, traceInstanceIndex.getTraceInstanceIndexId());
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null)
                tx.rollback();
            throw new OaException(e.getMessage());
        } finally {
            if (hiSession != null)
                hiSession.close();
            try {
                HiDbTraceInstanceIndex index = (HiDbTraceInstanceIndex)getObject(HiDbTraceInstanceIndex.class, traceInstanceIndex.getTraceInstanceIndexId());
                updateTraceStateOnly(index, (HiDbTraceInstanceDetail)null, (Session)null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void saveData(FormData currentFormData, HiDbTraceInstanceIndex traceInstanceIndex, String currentUserId) throws OaException {
        if (this.alreadySaveDataIndexId.equals(traceInstanceIndex.getTraceInstanceIndexId()))
            return;
        this.traceTemplateManager.saveData(currentFormData, traceInstanceIndex, currentUserId);
        this.alreadySaveDataIndexId = traceInstanceIndex.getTraceInstanceIndexId();
        createProjectTrace(traceInstanceIndex, currentFormData, currentUserId);
    }
    private void inSaveDataAfter(HiDbTraceInstanceIndex eoTraceInstanceIndex, HiDbTraceInstancePath eoTraceInstancePath, HiDbTraceInstanceDetail eoTraceInstanceDetail, FormData currentFormData, String externalModulesDataJsonStr, Map dataMap, boolean yesFlg, boolean doTracingFlg, Session hiSession, String userId, String userDeptId) throws OaException {
        boolean pass = false;
        boolean notPass = false;
        Map mainData = null;
        Map listData = null;
        if (Objects.nonNull(currentFormData)) {
            mainData = currentFormData.getMainTableDataMap();
            listData = currentFormData.getListTableDataMap();
        }
        try {
            if (eoTraceInstanceIndex.getCurrentTraceState().intValue() == 2)
                if (eoTraceInstanceIndex.getTraceMark().intValue() < 3 || eoTraceInstanceIndex.getTraceMark().intValue() == 901)
                    createTracePublish(eoTraceInstanceIndex);
            this.runningManager.doInitRunningData(eoTraceInstanceIndex, eoTraceInstancePath, eoTraceInstanceDetail, null, externalModulesDataJsonStr, userId, userDeptId, hiSession, dataMap, null);
            pass = (eoTraceInstanceIndex.getCurrentTraceState().intValue() == 2);
            notPass = (eoTraceInstanceIndex.getCurrentTraceState().intValue() == 3);
            if (pass || notPass) {
                this.runningManager.traceOverAfter(eoTraceInstanceIndex, mainData, listData, yesFlg, dataMap);
                HtInvoiceConstant traceCondition = pass ? HtInvoiceConstant.INVOICE_TRACE_PASS : HtInvoiceConstant.INVOICE_TRACE_NO_PASS;
                this.invoiceStateChanger = HtInvoiceStateChangerCreator.getInvoiceChangerInstance(currentFormData, traceCondition, this.languageType, eoTraceInstanceIndex
                        .getTraceInstanceIndexId());
                this.invoiceStateChanger.change();
                HtProjectService projectService = new HtProjectService();
                String indexId = eoTraceInstanceIndex.getTraceInstanceIndexId();
                if (projectService.isProjectStepIndex(this.languageType, indexId) || projectService
                        .isProjectStartIndex(this.languageType, indexId))
                    return;
                if (yesFlg)
                    if (eoTraceInstanceIndex.getTraceMark().intValue() == 0 || eoTraceInstanceIndex.getTraceMark().intValue() == 2) {
                        JSONObject eoJsonObj = new JSONObject();
                        HtNGTrace03TemplateManager templateManager = new HtNGTrace03TemplateManager(this.languageType);
                        HiDbTraceTemplateDict template = null;
                        List eoTraceTemplateRelationList = templateManager.fetchTraceTemplateRelation(eoTraceInstanceIndex
                                .getTraceTemplateDictId());
                        StringBuilder relationTraceList = new StringBuilder();
                        if (StringUtils.isNotBlank(eoTraceInstanceIndex.getRelationIndexList()))
                            relationTraceList.append(eoTraceInstanceIndex.getRelationIndexList());
                        if (eoTraceTemplateRelationList != null) {
                            boolean conditionTrue = false;
                            for (Object o : eoTraceTemplateRelationList) {
                                HiDbTraceTemplateRelation traceTemplateRelation = (HiDbTraceTemplateRelation)o;
                                if (traceTemplateRelation == null)
                                    continue;
                                eoJsonObj.accumulate("initTraceData", "initTraceData");
                                String relationCondition = traceTemplateRelation.getRelationCondition();
                                if (relationCondition != null && !"".equals(relationCondition)) {
                                    List<String> conditionList = OaTools.partitionString(relationCondition, ';');
                                    if (conditionList.size() < 2)
                                        return;
                                    String conditionField = conditionList.get(0);
                                    if (conditionField == null)
                                        return;
                                    String formFieldValue = (String)mainData.get(conditionField);
                                    String hiddenFormFieldValue = (String)mainData.get(conditionField + "_hidden");
                                    String conidionValue = conditionList.get(1);
                                    if (conidionValue == null || "".equals(conidionValue))
                                        return;
                                    if (conidionValue.equals(formFieldValue))
                                        conditionTrue = true;
                                    if (conidionValue.equals(hiddenFormFieldValue))
                                        conditionTrue = true;
                                } else {
                                    conditionTrue = true;
                                }
                                if (conditionTrue) {
                                    if (traceTemplateRelation.getState().intValue() == 3 || traceTemplateRelation.getState().intValue() == 1) {
                                        String relationIndexId = relationSendAndSaveOver(eoTraceInstanceIndex, eoTraceInstancePath, traceTemplateRelation, traceTemplateRelation.getState(), mainData, dataMap, userId, userDeptId, hiSession);
                                        if (relationIndexId == null)
                                            continue;
                                        relationTraceList.append(relationIndexId).append(";");
                                    }
                                    boolean isOpenFlg = false;
                                    if (traceTemplateRelation.getState().intValue() == 0) {
                                        Map relationMap = relationSend(eoTraceInstanceIndex, eoTraceInstancePath, traceTemplateRelation, traceTemplateRelation.getState(), mainData, dataMap, userId, userDeptId, hiSession);
                                        if (relationMap != null)
                                            this.returnAlertMsg = "当前审批已经完成。自动触发申请失败，关联模板流程不支持。";
                                    }
                                    if (traceTemplateRelation.getState().intValue() == 2 || isOpenFlg) {
                                        String relationMemo = traceTemplateRelation.getRelationMemo();
                                        if (StringUtils.isNotBlank(relationMemo))
                                            eoJsonObj = (new HtTraceForm01Manager(this.languageType)).formTraceRelationValue(mainData, relationMemo, true);
                                        template = templateManager.getTraceTemplateDict(traceTemplateRelation.getRelationTempId());
                                        break;
                                    }
                                }
                            }
                        }
                        if (template != null) {
                            runningClassMakeNewTrace(eoTraceInstanceIndex, eoTraceInstancePath, template, userId, userDeptId);
                            this.makeNewTraceMap.put("initTraceDataMap", eoJsonObj);
                            this.makeNewTraceMap.put("traceUser", userId);
                            this.makeNewTraceMap.put("initTraceFlg", "MakeNewTracePageId");
                        }
                        if (dataMap != null && dataMap.get("allTemplateData") != null) {
                            ArrayList dataList = (ArrayList)dataMap.get("allTemplateData");
                            ArrayList<Map> resultList = new ArrayList<>();
                            for (Object o : dataList) {
                                HashMap currentMap = (HashMap)o;
                                HiDbTraceInstanceIndex currentTraceInstanceIndex = runningClassSendNewTrace((String)currentMap.get("SendNewTracePageId"), eoTraceInstanceIndex
                                        .getTraceMark().intValue(), userId, userDeptId);
                                Map<String, Object> currentResultData = new HashMap<>();
                                currentResultData.put("initTraceDataMap", currentMap.get("newTraceData"));
                                currentResultData.put("traceUser", currentMap.get("traceUser"));
                                currentResultData.put("hiTraceInstanceIndex", currentTraceInstanceIndex);
                                resultList.add(currentResultData);
                            }
                            this.makeNewTraceMap.put("allTemplateData", resultList);
                            this.makeNewTraceMap.put("initTraceFlg", "SendNewTracePageId");
                        }
                        eoTraceInstanceIndex.setRelationIndexList(relationTraceList.toString());
                    }
            } else if (doTracingFlg) {
                if (this.insertObjectList != null && this.insertObjectList.size() > 0)
                    for (Object obj : this.insertObjectList) {
                        if (!"HiDbTraceInstancePath".equals(obj.getClass().getSimpleName()))
                            continue;
                        HiDbTraceInstancePath nextPath = (HiDbTraceInstancePath)obj;
                        if (nextPath.getTraceInstancePathId().equals(eoTraceInstanceIndex.getCurrentStepId()))
                            this.runningManager.doInitRunningData(eoTraceInstanceIndex, eoTraceInstancePath, eoTraceInstanceDetail, nextPath, externalModulesDataJsonStr, userId, userDeptId, null, dataMap, null);
                    }
                this.runningManager.traceAfter(eoTraceInstanceIndex, mainData, listData, yesFlg, dataMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if ((pass || notPass) &&
                    this.invoiceStateChanger != null)
                this.invoiceStateChanger.rollBack();
            throw e;
        }
    }
    private void createProjectTrace(HiDbTraceInstanceIndex traceInstanceIndex, FormData currentFormData, String currentUserId) throws OaException {
        int traceState = traceInstanceIndex.getCurrentTraceState().intValue();
        String indexId = traceInstanceIndex.getTraceInstanceIndexId();
        HiDbUserUser userUser = (HiDbUserUser)getObject(HiDbUserUser.class, currentUserId);
        if (traceState != 2)
            return;
        if (currentFormData != null) {
            JSONObject o = currentFormData.getBusinessData();
            Map mainDataMap = currentFormData.getMainTableDataMap();
            if (o != null) {
                String KEY = o.getString("KEY");
                Object ID = o.get("ID");
                if (ID == null)
                    throw new OaException("MSGsystemSetting00047", "请选择项目类别");
                if ("SYS_SEL_BUSINESS".equals(KEY)) {
                    createProjectStepTraceData(this.languageType, mainDataMap, userUser

                            .getUserId(), userUser
                            .getDeptId(), indexId, ID + "");
                    return;
                }
            }
        }
        if ((new HtProjectService()).isProjectStepIndex(this.languageType, indexId))
            createProjectStepTraceData(this.languageType, new HashMap<>(), userUser

                    .getUserId(), userUser
                    .getDeptId(), indexId, "");
    }
    private void createProjectStepTraceData(String languageType, Map mainDataMap, String userId, String deptId, String indexId, String categoryId) throws OaException {
        JSONObject stepObj;
        HtProjectService projectService = new HtProjectService();
        String createTemplateId = "";
        String createIndexId = "";
        String projectId = "";
        HiDbTraceInstanceIndex instanceIndex = (HiDbTraceInstanceIndex)getObject(HiDbTraceInstanceIndex.class, indexId);
        boolean projectCreateFlg = StringUtils.isNotBlank(categoryId);
        if (projectCreateFlg) {
            String projectName = instanceIndex.getTraceTitle();
            String relationProjectId = "";
            stepObj = projectService.createProjectRecord(languageType, categoryId, indexId, projectName, userId, mainDataMap, relationProjectId);
        } else {
            projectService.updateProjectRecordListState(languageType, indexId);
            stepObj = projectService.getNextProjectRecordListObj(languageType, indexId);
            if (stepObj == null)
                return;
        }
        JSONArray stepObjAry = stepObj.getJSONArray("stepAry");
        int lastStepId = stepObj.getInt("lastStepId");
        projectId = stepObj.getString("projectId");
        if (stepObjAry == null || stepObjAry.size() == 0) {
            if (projectCreateFlg) {
                projectService.updateProjectRecordState(languageType, indexId);
            } else {
                projectService.updateProjectRecordStateByListIndexId(languageType, indexId);
            }
            return;
        }

    }
    private void clearDataAfterSaveData() {
        this.insertObjectList.clear();
        this.updateObjectList.clear();
    }
    private void delCopyTable(FormData currentFormData) {
        if (currentFormData != null)
            try {
                currentFormData.delCopyTable();
            } catch (OaException e) {
                e.printStackTrace();
            }
    }
    private void updateTraceStateOnly(HiDbTraceInstanceIndex traceInstanceIndex, HiDbTraceInstanceDetail traceInstanceDetail, Session hiSession) throws Exception {
        String sql;
        HiDbTraceTemplateDict dict = (new HtTrace03TemplateManager(this.languageType)).getCurrentTraceTemplate(traceInstanceIndex);
        if (dict == null)
            return;
        if (traceInstanceDetail == null) {
            sql = "update #tableName_hidden set trace_state = ? where trace_instance_index_id = ?";
        } else {
            sql = "update #tableName_hidden set trace_state = ?, trace_user_id = ? where trace_instance_index_id = ?";
        }
        Transaction tx = null;
        try {
            if (hiSession == null) {
                hiSession = TransactionManager.getInstance().getCurrentSession();
                tx = hiSession.beginTransaction();
            }
            String mainTable = dict.getMainTableName();
            HiMainDao dao = new HiMainDao(hiSession);
            List<String> paramList = new LinkedList();
            paramList.add(traceInstanceIndex.getCurrentTraceState().toString());
            if (traceInstanceDetail != null)
                paramList.add(traceInstanceDetail.getTraceUserId());
            paramList.add(traceInstanceIndex.getTraceInstanceIndexId());
            dao.executeBySQL(sql.replace("#tableName", mainTable), paramList);
            if (StringUtils.isNotBlank(dict.getListTableName())) {
                List<String> listTableAry = OaTools.partitionString(dict.getListTableName(), ';');
                if (listTableAry != null && !listTableAry.isEmpty())
                    for (String listTable : listTableAry) {
                        if (StringUtils.isBlank(listTable))
                            continue;
                        dao.executeBySQL(sql.replace("#tableName", listTable), paramList);
                    }
            }
            if (tx != null)
                tx.commit();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            throw e;
        } finally {
            if (hiSession != null && tx != null)
                hiSession.close();
        }
    }
    private String relationSendAndSaveOver(HiDbTraceInstanceIndex currenIndex, HiDbTraceInstancePath eoTraceInstancePath, HiDbTraceTemplateRelation traceTemplateRelation, Integer state, Map mainData, Map dataMap, String userId, String userDeptId, Session hiSession) throws OaException {
        Transaction tx = null;
        try {
            if (hiSession == null) {
                hiSession = TransactionManager.getInstance().getCurrentSession();
                tx = hiSession.beginTransaction();
            }
            String relationCondition = traceTemplateRelation.getRelationCondition();
            String relationTemplateId = traceTemplateRelation.getRelationTempId();
            boolean conditionTrue = false;
            if (relationCondition != null && !"".equals(relationCondition)) {
                List<String> conditionList = OaTools.partitionString(relationCondition, ';');
                if (conditionList.size() < 2)
                    return null;
                String conditionField = conditionList.get(0);
                if (conditionField == null)
                    return null;
                String formFieldValue = (String)mainData.get(conditionField);
                String conidionValue = conditionList.get(1);
                if (conidionValue == null || "".equals(conidionValue))
                    return null;
                if (conidionValue.equals(formFieldValue))
                    conditionTrue = true;
            }
            if (conditionTrue || relationCondition == null) {
                List<HiDbTracePathIndex> tracePathIndexList;
                String agentUserId = "";
                if (eoTraceInstancePath != null) {
                    HiDbTraceActionDict eoTraceActionDict = this.actionManager.getTraceActionDict(eoTraceInstancePath.getTraceActionDictId());
                    if (eoTraceActionDict == null)
                        eoTraceActionDict = this.actionManager.getTraceActionDictUseForNoAction(eoTraceInstancePath.getTraceInstanceIndexId());
                    agentUserId = (new HtTrace01AgentManager(this.languageType)).getAgentUser(relationTemplateId, null, eoTraceActionDict, userId);
                }
                HiDbTraceTemplateDict relationTemplateDict = this.templateManager.getTraceTemplateDict(relationTemplateId);
                HiDbTraceInstanceIndex eoTraceInstanceIndex = new HiDbTraceInstanceIndex();
                if (StringUtils.isNotBlank(agentUserId)) {
                    eoTraceInstanceIndex.setCreateUserId(agentUserId);
                    tracePathIndexList = this.pathManager.fetchTracePathIndexByUserId(relationTemplateId, agentUserId, (new HiOaMainClass())
                            .getDept(null, agentUserId).getHrDeptId());
                } else {
                    tracePathIndexList = this.pathManager.fetchTracePathIndexByUserId(relationTemplateId, userId, userDeptId);
                }
                if (tracePathIndexList.size() < 1)
                    return null;
                HiDbTracePathIndex relationTracePathIndex = tracePathIndexList.get(0);
                HiDbTraceInstanceIndex traceInstanceIndex = doCreateTraceInstanceIndex(relationTemplateDict, relationTracePathIndex, eoTraceInstanceIndex, currenIndex
                        .getCreateUserId());
                String relationMemo = traceTemplateRelation.getRelationMemo();
                HtTraceForm01Manager form01Manager = new HtTraceForm01Manager(this.languageType);
                JSONObject formJson = form01Manager.formTraceRelationValue(mainData, relationMemo, false);
                JSONObject mainObj = formJson.getJSONObject("MAIN");
                setAutoNum(traceInstanceIndex, mainObj);
                formJson.put("MAIN", mainObj);
                FormData relationFormData = form01Manager.getCommitFormData("relationForm", traceInstanceIndex, formJson);
                HtTrace05TraceOfficeManager officeManager = new HtTrace05TraceOfficeManager();
                officeManager.copyAttachmentAndDocFile(currenIndex, traceInstanceIndex);
                traceInstanceIndex.setRelationParentIndexId(currenIndex.getTraceInstanceIndexId());
                traceInstanceIndex.setOtherInstanceView(";" + currenIndex.getTraceInstanceIndexId() + ";");
                if (state.intValue() == 3) {
                    doSendTraceToFinished(traceInstanceIndex, relationFormData, (String)null, dataMap, userId, userDeptId, false, (List)null, false);
                } else {
                    doSaveTraceBySend(traceInstanceIndex, relationFormData, "", dataMap, userId, userDeptId, false, (List)null, true);
                }
                if (StringUtils.isNotBlank(currenIndex.getOtherInstanceView())) {
                    currenIndex.setOtherInstanceView(currenIndex.getOtherInstanceView() + traceInstanceIndex.getTraceInstanceIndexId() + ";");
                } else {
                    currenIndex.setOtherInstanceView(";" + traceInstanceIndex.getTraceInstanceIndexId() + ";");
                }
                return traceInstanceIndex.getTraceInstanceIndexId();
            }
            if (tx != null)
                tx.commit();
            return null;
        } catch (OaException e) {
            e.printStackTrace();
            if (tx != null)
                tx.rollback();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null)
                tx.rollback();
            throw new OaException("MSGtrace00004", "保存数据错误");
        } finally {
            if (tx != null && hiSession != null)
                hiSession.close();
        }
    }
    private void setAutoNum(HiDbTraceInstanceIndex traceInstanceIndex, JSONObject mainObj) throws OaException {
        if (mainObj == null)
            return;
        HtTrace01DefineNoManager defineNoManager = new HtTrace01DefineNoManager();
        HtTrace03TemplateManager templateManager = new HtTrace03TemplateManager();
        String templatedictId = traceInstanceIndex.getTraceTemplateDictId();
        try {
            HiDbTraceTemplateDict traceTemplateDict = templateManager.getCurrentTraceTemplate(traceInstanceIndex);
            if (traceTemplateDict != null)
                templatedictId = traceTemplateDict.getTraceTemplateDictId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<HiDbTraceUserDefineListNo> list = defineNoManager.fetchUserDefineListNo(traceInstanceIndex.getTraceTemplateDictId());
        if (list != null && !list.isEmpty()) {
            HiDbTraceUserDefineListNo no = list.get(0);
            List<HiDbTraceDefineField> fields = (new HtTrace03TemplateManager()).fetchTraceDefineField(templatedictId, null, true);
            if (fields == null || fields.isEmpty())
                throw new OaException("表单["+ templatedictId + "]没有字段定义");
            String realAutoNum = defineNoManager.getUserDefineListNo(false, no);
            for (int i = 0; i < fields.size(); i++) {
                HiDbTraceDefineField _field = fields.get(i);
                if ("SYS_SEL_AUTONUM".equals(_field.getShowType())) {
                    JSONObject obj = new JSONObject();
                    obj.put("v", OaTools.encodeBase64String(realAutoNum));
                    obj.put("h", OaTools.encodeBase64String(no.getUserDefineListNoId()));
                    mainObj.put(_field.getShowFieldName(), obj);
                    break;
                }
            }
        }
    }
    private HiDbTraceInstanceIndex runningClassSendNewTrace(String templateTitle, int traceMark, String userId, String userDeptId) {
        List<HiDbTraceTemplateDict> allTemplateArray = this.templateManager.fetchTraceTemplateDictByTitle(templateTitle, traceMark, userId, userDeptId);
        if (allTemplateArray == null || allTemplateArray.size() == 0) {
            this
                    .newTraceErrorMsg = this.newTraceErrorMsg + "\n" + templateTitle + this.systemMsg.message("MSGtrace00145", "在审批结束后发送新申请的时候没有找到合适的模板，请确认。");
            return null;
        }
        if (allTemplateArray.size() == 1) {
            HiDbTraceTemplateDict dict = this.templateManager.getTraceTemplateDict(((HiDbTraceTemplateDict)allTemplateArray
                    .get(0)).getTraceTemplateDictId());
            List<HiDbTracePathIndex> pathAry = this.pathManager.fetchTracePathIndexByUserId(dict.getTraceTemplateDictId(), userId, userDeptId);
            if (pathAry == null || pathAry.size() == 0) {
                this
                        .newTraceErrorMsg = this.newTraceErrorMsg + "\n" + templateTitle + this.systemMsg.message("MSGtrace00145", "在审批结束后发送新申请的时候没有找到合适的模板，请确认。");
                return null;
            }
            try {
                HiDbTracePathIndex index = pathAry.get(0);
                HiDbTraceInstanceIndex currentTraceInstanceIndex = doCreateTraceInstanceIndex(dict, index, new HiDbTraceInstanceIndex(), userId);
                this
                        .newTraceMsg = this.newTraceMsg + "\n" + templateTitle + this.systemMsg.message("MSGtrace00146", "在审批结束后发送新申请的时候没有找到合适的模板，请确认。");
                return currentTraceInstanceIndex;
            } catch (OaException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
    private Map relationSend(HiDbTraceInstanceIndex currenIndex, HiDbTraceInstancePath eoTraceInstancePath, HiDbTraceTemplateRelation traceTemplateRelation, Integer state, Map mainData, Map<String, Object> dataMap, String userId, String userDeptId, Session hiSession) throws OaException {
        Transaction tx = null;
        try {
            if (hiSession == null) {
                hiSession = TransactionManager.getInstance().getCurrentSession();
                tx = hiSession.beginTransaction();
            }
            String relationCondition = traceTemplateRelation.getRelationCondition();
            String relationTemplateId = traceTemplateRelation.getRelationTempId();
            boolean conditionTrue = false;
            if (relationCondition != null && !"".equals(relationCondition)) {
                List<String> conditionList = OaTools.partitionString(relationCondition, ';');
                if (conditionList.size() < 2)
                    return null;
                String conditionField = conditionList.get(0);
                if (conditionField == null)
                    return null;
                String formFieldValue = (String)mainData.get(conditionField);
                String hiddenFormFieldValue = (String)mainData.get(conditionField + "_hidden");
                String conidionValue = conditionList.get(1);
                if (conidionValue == null || "".equals(conidionValue))
                    return null;
                if (conidionValue.equals(formFieldValue))
                    conditionTrue = true;
                if (conidionValue.equals(hiddenFormFieldValue))
                    conditionTrue = true;
            }
            if (conditionTrue || relationCondition == null) {
                List<HiDbTracePathIndex> tracePathIndexList;
                HiDbTraceTemplateDict relationTemplateDict = this.templateManager.getTraceTemplateDict(relationTemplateId);
                String agentUserId = "";
                if (eoTraceInstancePath != null) {
                    HiDbTraceActionDict eoTraceActionDict = this.actionManager.getTraceActionDict(eoTraceInstancePath.getTraceActionDictId());
                    if (eoTraceActionDict == null)
                        eoTraceActionDict = this.actionManager.getTraceActionDictUseForNoAction(eoTraceInstancePath.getTraceInstanceIndexId());
                    agentUserId = (new HtTrace01AgentManager(this.languageType)).getAgentUser(relationTemplateId, null, eoTraceActionDict, userId);
                }
                HiDbTraceInstanceIndex eoTraceInstanceIndex = new HiDbTraceInstanceIndex();
                if (StringUtils.isNotBlank(agentUserId)) {
                    eoTraceInstanceIndex.setCreateUserId(agentUserId);
                    tracePathIndexList = this.pathManager.fetchTracePathIndexByUserId(relationTemplateId, agentUserId, (new HiOaMainClass())
                            .getDept(null, agentUserId).getHrDeptId());
                } else {
                    tracePathIndexList = this.pathManager.fetchTracePathIndexByUserId(relationTemplateId, userId, userDeptId);
                }
                HiDbTracePathIndex relationTracePathIndex = tracePathIndexList.get(0);
                HiDbTraceInstanceIndex traceInstanceIndex = doCreateTraceInstanceIndex(relationTemplateDict, relationTracePathIndex, eoTraceInstanceIndex, currenIndex
                        .getCreateUserId());
                String relationMemo = traceTemplateRelation.getRelationMemo();
                HtTraceForm01Manager form01Manager = new HtTraceForm01Manager(this.languageType);
                JSONObject formJson = form01Manager.formTraceRelationValue(mainData, relationMemo, false);
                JSONObject mainObj = formJson.getJSONObject("MAIN");
                setAutoNum(traceInstanceIndex, mainObj);
                formJson.put("MAIN", mainObj);
                FormData relationFormData = form01Manager.getCommitFormData("relationForm", traceInstanceIndex, formJson);
                HtTrace05TraceOfficeManager officeManager = new HtTrace05TraceOfficeManager();
                officeManager.copyAttachmentAndDocFile(currenIndex, traceInstanceIndex);
                traceInstanceIndex.setRelationParentIndexId(currenIndex.getTraceInstanceIndexId());
                traceInstanceIndex.setOtherInstanceView(";" + currenIndex.getTraceInstanceIndexId() + ";");
                HiDbTraceInstancePath hiTraceInstancePath = new HiDbTraceInstancePath();
                HiDbTracePathDetail hiTracePathDetail = this.pathManager.getTracePathDetail(relationTracePathIndex.getTracePathIndexId(), Integer.valueOf(1));
                Map nextStepMap = commCreatGetNextStepMap(this, userId, traceInstanceIndex, relationTracePathIndex, hiTraceInstancePath, hiTracePathDetail, (FormData)null, (String)null, (String)null, (String)null);
                if (StringUtils.isNotBlank(currenIndex.getOtherInstanceView())) {
                    currenIndex.setOtherInstanceView(currenIndex.getOtherInstanceView() + traceInstanceIndex.getTraceInstanceIndexId() + ";");
                } else {
                    currenIndex.setOtherInstanceView(";" + traceInstanceIndex.getTraceInstanceIndexId() + ";");
                }
                return doSendTrace(traceInstanceIndex, nextStepMap, relationFormData, (String)null, dataMap, (String)null, userId, userDeptId, (String)null, true, (List)null, true);
            }
            if (tx != null)
                tx.commit();
            return null;
        } catch (OaException e) {
            e.printStackTrace();
            if (tx != null)
                tx.rollback();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null)
                tx.rollback();
            throw new OaException("MSGtrace00004", "保存数据失败。");
        } finally {
            if (tx != null && hiSession != null)
                hiSession.close();
        }
    }
}
