package com.dchealth.util;

import com.dchealth.entity.common.YunUsers;
import com.dchealth.facade.common.BaseFacade;

import java.util.List;

/**
 * Created by Administrator on 2017/11/1.
 */
public class GroupQuerySqlUtil {

    public static String getGroupSql(String doctorId){
        String haveGroupSql = "select v.user_id from research_group_vs_user as v,(select g.id from research_group as g,research_group_vs_user as v" +
                " where g.id = v.group_id and g.status<>'-1' and ((v.user_id = '"+doctorId+"' and v.creater_flag ='1') or (v.user_id = '"+doctorId+"' and " +
                "g.data_share_level = 'A'))) as b where v.group_id = b.id ";
        return haveGroupSql;
    }

    /**
     * 根据查询的集合拼接用户id信息
     * @param ctlist
     * @return
     */
    public static String getUserIdsByList(List ctlist) {
        if(ctlist==null || ctlist.isEmpty()){
            return "";
        }
        StringBuffer ids = new StringBuffer("");
        for(int i=0;i<ctlist.size();i++){
            ids.append("'").append(ctlist.get(i)).append("',");
        }
        String userIds = ids.toString();
        userIds = userIds.substring(0,userIds.length()-1);
        return userIds;
    }

    public static List getResearchAssistantSql(String doctorId,String type,BaseFacade baseFacade){
        String sql = "";
        if("0".equals(type)){//表示查询医生下的研究助手
            sql +=  "select assistant as user_id from research_assistant where user_id = '"+doctorId+"'";
        }else{//表示研究助手查询其导师
            sql +=  "select user_id from research_assistant where assistant = '"+doctorId+"'";
        }
        return baseFacade.createNativeQuery(sql).getResultList();
    }
    /**
     * 通过医生id获取该医生所在组的成员信息
     * @param doctorId
     * @return
     */
    public static String getUserIds(String doctorId,BaseFacade baseFacade) {
        String userIds = "";
        try {
            YunUsers yunUsers = UserUtils.getYunUsers();
            if("DOCTOR_ASSISTANT".equals(yunUsers.getRolename())){//如果是研究助手 查看自己录入的和导师录入的病历
                List assistantList = getResearchAssistantSql(doctorId,"1",baseFacade);
                if(assistantList!=null && !assistantList.isEmpty()){
                    userIds = "'"+assistantList.get(0)+"','"+doctorId+"'";
                }
            }else{
                String haveGroupSql = GroupQuerySqlUtil.getGroupSql(doctorId);
                List ctlist = baseFacade.createNativeQuery(haveGroupSql).getResultList();
                List assisList = getResearchAssistantSql(doctorId,"0",baseFacade);
                ctlist.addAll(assisList);
                userIds = GroupQuerySqlUtil.getUserIdsByList(ctlist);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return userIds;
    }
}
