package com.dchealth.service.rare;

import com.dchealth.VO.DocumentDataElement;
import com.dchealth.VO.Hversion;
import com.dchealth.VO.ModelTemplateVo;
import com.dchealth.VO.YunDisTemplateVo;
import com.dchealth.entity.rare.YunDisTemplet;
import com.dchealth.entity.rare.YunReleaseTemplet;
import com.dchealth.facade.common.BaseFacade;
import com.dchealth.util.JSONUtil;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/27.
 */
@Controller
@Produces("application/json")
@Path("work-flow")
public class WorkFlowService {

    @Autowired
    private BaseFacade baseFacade ;

    /**
     * 获取工作流模板
     * @param dcode 疾病编码
     * @param title 工作流标题
     * @param doctorId 医生ID
     * @return
     */
    @GET
    @Path("list-work-flow")
    public List<YunDisTemplateVo> listWorkFlow(@QueryParam("dcode") String dcode , @QueryParam("title") String title, @QueryParam("doctorId") String doctorId){
        String hql = "from YunDisTemplet as t where t.mblx='WORK'" ;
        List<YunDisTemplateVo> yunDisTemplateVos = new ArrayList<>() ;
        if(!"".equals(dcode)&&null!=dcode){
            hql +=" and t.dcode='"+dcode+"'";
        }
        if(!"".equals(title)&&null!=title){
            hql +=" and t.title='"+title+"'";
        }
        if(!"".equals(doctorId)&&null!=doctorId){
            hql +=" and t.doctorId='"+doctorId+"'";
        }

        List<YunDisTemplet> yunDisTemplets = baseFacade.createQuery(YunDisTemplet.class,hql,new ArrayList<Object>()).getResultList();

        for(YunDisTemplet templet:yunDisTemplets){
            YunDisTemplateVo vo = new YunDisTemplateVo();
            vo.setDcode(templet.getDcode());
            vo.setDeptId(templet.getDeptId());
            vo.setId(templet.getId());
            vo.setDoctorId(templet.getDoctorId());
            vo.setMblx(templet.getMblx());
            vo.setModifyDate(templet.getModifyDate());
            vo.setTitle(templet.getTitle());
            vo.setNote(templet.getNote());
            String mbsj = templet.getMbsj();
            if(!"".equals(mbsj)&&null!=mbsj){
                List<ModelTemplateVo> vos = (List<ModelTemplateVo>) JSONUtil.JSONToObj(templet.getMbsj(),new ArrayList<ModelTemplateVo>().getClass());
                vo.setMbsj(vos);
            }else{
                vo.setMbsj(new ArrayList<ModelTemplateVo>());
            }
            yunDisTemplateVos.add(vo);
        }
        return yunDisTemplateVos;

    }


    /**
     * 修改工作流模板
     * @param yunDisTemplateVo
     * @return
     */
    @POST
    @Path("merge-work-flow")
    @Transactional
    public Response mergeWorkFlow(YunDisTemplateVo yunDisTemplateVo) throws IOException, JSONException {

        YunDisTemplet yunDisTemplet = new YunDisTemplet();
        yunDisTemplet.setDcode(yunDisTemplateVo.getDcode());
        yunDisTemplet.setDeptId(yunDisTemplateVo.getDeptId());
        yunDisTemplet.setId(yunDisTemplateVo.getId());
        yunDisTemplet.setDoctorId(yunDisTemplateVo.getDoctorId());
        yunDisTemplet.setMblx(yunDisTemplateVo.getMblx());
        yunDisTemplet.setModifyDate(yunDisTemplateVo.getModifyDate());
        yunDisTemplet.setTitle(yunDisTemplateVo.getTitle());
        yunDisTemplet.setNote(yunDisTemplateVo.getNote());
        String mbsj = JSONUtil.objectToJson(yunDisTemplateVo.getMbsj()).toString();
        yunDisTemplet.setMbsj(mbsj);
        YunDisTemplet merge = baseFacade.merge(yunDisTemplet);
        return  Response.status(Response.Status.OK).entity(merge).build();
    }


    /***
     * 新建病人获取工作流数据
     * @param dcode
     * @param title
     * @param doctorId
     * @return
     */
    @GET
    @Path("list-work-flow-item")
    public List<ModelTemplateVo> getWorkFlowDocumentElement(@QueryParam("dcode") String dcode,@QueryParam("title") String title,@QueryParam("doctorId") String doctorId){
        String hql = "from YunReleaseTemplet as t where t.hstatus='R' and t.dcode='"+dcode+"' and t.title='"+title+"'" ;
        List<ModelTemplateVo> documentDataElements = new ArrayList<>();

        List<YunReleaseTemplet> resultList = baseFacade.createQuery(YunReleaseTemplet.class, hql, new ArrayList<Object>()).getResultList();
        for(YunReleaseTemplet templet:resultList){
            Hversion hversion= (Hversion) JSONUtil.JSONToObj(templet.getHversion(),Hversion.class);
            if(doctorId.equals(hversion.getDoctor())&&templet.getMbsj()!=null&&!"".equals(templet.getMbsj())){
                List<ModelTemplateVo> mbsj = (List<ModelTemplateVo>) JSONUtil.JSONToObj(templet.getMbsj(),new ArrayList<ModelTemplateVo>().getClass());
                documentDataElements.addAll(mbsj);
            }
        }


        //如果没有发布数据，则用私有数据
        if(documentDataElements.size()==0){
            List<YunDisTemplateVo> yunDisTemplateVos = listWorkFlow(dcode, title, doctorId);
            for (YunDisTemplateVo vo:yunDisTemplateVos){
                documentDataElements.addAll(vo.getMbsj());
            }
        }

        return documentDataElements;
    }

}
