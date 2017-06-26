package com.dchealth.service.rare;

import com.dchealth.VO.Form;
import com.dchealth.VO.Hversion;
import com.dchealth.entity.rare.YunDisTemplet;
import com.dchealth.entity.rare.YunReleaseTemplet;
import com.dchealth.facade.common.BaseFacade;
import com.dchealth.util.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/6/21.
 */
@Controller
@Produces("application/json")
@Path("template")
public class TemplateService {

    @Autowired
    private BaseFacade baseFacade ;


    /**
     * 获取模板列表
     * @param dcode
     * @param doctorId
     * @param title
     * @param mblx
     * @return
     */
    @GET
    @Path("list")
    public List<YunDisTemplet> getDisTemplate(@QueryParam("dcode") String dcode,@QueryParam("doctorId") String doctorId,
                                              @QueryParam("title")String title,@QueryParam("mblx")String mblx,
                                              @QueryParam("deptId")String deptId,
                                              @QueryParam("pubFlag") String pubFlag) throws Exception {
        String hql = "from YunDisTemplet as t where 1=1" ;
        //所属疾病
        if(!"".equals(dcode)&&dcode!=null){
            hql+=" and t.dcode='"+dcode+"'" ;
        }
        //标题
        if(!"".equals(title)&&title!=null){
            hql+=" and t.title='"+title+"'" ;
        }
        //模板类型
        if(!"".equals(mblx)&&mblx!=null){
            hql+=" and t.mblx='"+mblx+"'" ;
        }


        if("".equals(pubFlag)||pubFlag==null){
            throw  new Exception("缺少pubFlag，公共私有数据标识。0表示私有数据，1表示公共数据");
        }

        if("1".equals(pubFlag)){
            hql += " and v.doctorId='0'" ;
        }

        if("0".equals(pubFlag)){
            if(doctorId==null||"".equals(doctorId)){
                throw  new Exception("缺少doctorId，用户标识");
            }
            if(deptId==null||"".equals(deptId)){
                throw  new Exception("缺少deptId，科室标识");
            }
            hql+=" and v.doctorId='"+doctorId+"' or (v.deptId='"+deptId+"' and v.deptId <>'0')" ;
        }

        List<YunDisTemplet> yunDisTemplets = baseFacade.createQuery(YunDisTemplet.class, hql, new ArrayList<Object>()).getResultList();
        return yunDisTemplets;
    }


    /**
     * 添加或者修改模板信息
     * @param yunDisTemplet
     * @return
     */
    @POST
    @Path("merge")
    @Transactional
    public Response mergeYunDisTemplate(YunDisTemplet yunDisTemplet){
        YunDisTemplet merge = baseFacade.merge(yunDisTemplet);
        return Response.status(Response.Status.OK).entity(merge).build();
    }


    /**
     * 获取模板设计的内容
     * @param templateId
     * @return
     * @throws Exception
     */
    @GET
    @Path("get-form-design")
    public Form getFormInfo(@QueryParam("templateId") String templateId) throws Exception {
        YunDisTemplet yunDisTemplet = baseFacade.get(YunDisTemplet.class,templateId);
        if(yunDisTemplet==null){
            throw  new Exception("找不到对应的模板信息");
        }
        String mbsj = yunDisTemplet.getMbsj() ;
        Form o = (Form) JSONUtil.JSONToObj(mbsj, Form.class);
        return o ;
    }


    /**
     * 模板设计保存
     * @param form
     * @param templateId
     * @return
     * @throws Exception
     */
    @POST
    @Path("merge-form-design")
    @Transactional
    public Response desginFormInfo(Form form,@QueryParam("templateId") String templateId) throws Exception {
        String mbsj = JSONUtil.objectToJson(form).toString();
        YunDisTemplet yunDisTemplet = baseFacade.get(YunDisTemplet.class, templateId);
        if(yunDisTemplet==null){
            throw new Exception("获取模板数据失败");
        }
        yunDisTemplet.setMbsj(mbsj);
        YunDisTemplet merge = baseFacade.merge(yunDisTemplet);
        return Response.status(Response.Status.OK).entity(merge).build();
    }


    /**
     * 发布表单或者工作流
     * @param templateId
     * @return
     */
    @Transactional
    @POST
    @Path("pub")
    public Response publishTemplateOrWork(@QueryParam("templateId") String templateId) throws Exception {

        YunDisTemplet yunDisTemplet = baseFacade.get(YunDisTemplet.class, templateId);
        if(yunDisTemplet==null){
            throw new Exception("") ;
        }

        String decode = yunDisTemplet.getDcode() ;
        String title = yunDisTemplet.getTitle() ;
        String doctorId = yunDisTemplet.getDoctorId() ;
        String deptId = yunDisTemplet.getDeptId();
        String hql = "from YunReleaseTemplate as r where r.dcode='"+decode+"'" +
                " and r.title='"+title+"'" ;
        Hversion hversion= new Hversion();
        hversion.setDept(deptId);
        hversion.setDoctor(doctorId);
        hversion.setNum("1");

        List<YunReleaseTemplet> resultList = baseFacade.createQuery(YunReleaseTemplet.class, hql, new ArrayList<Object>()).getResultList();
        for(YunReleaseTemplet templet:resultList){
            String hversion1 = templet.getHversion();
            Hversion tempHversion = (Hversion) JSONUtil.JSONToObj(hversion1, Hversion.class);
            if(doctorId.equals(tempHversion.getDoctor())&&deptId.equals(tempHversion.getDept())){
                baseFacade.remove(templet);
            }
        }
        YunReleaseTemplet yunReleaseTemplet = new YunReleaseTemplet();
        yunReleaseTemplet.setDcode(decode);
        yunReleaseTemplet.setHstatus("C");
        yunReleaseTemplet.setMblx(yunDisTemplet.getMblx());
        yunReleaseTemplet.setMbsj(yunDisTemplet.getMbsj());
        yunReleaseTemplet.setModifyDate((Timestamp) new Date());
        yunReleaseTemplet.setNote(yunDisTemplet.getNote());
        yunReleaseTemplet.setTitle(title);
        yunReleaseTemplet.setValuedata("");
        yunReleaseTemplet.setHversion(JSONUtil.objectToJson(hversion).toString());
        YunReleaseTemplet releaseTemplet = baseFacade.merge(yunReleaseTemplet);
        return Response.status(Response.Status.OK).entity(releaseTemplet).build();

    }

    /**
     * 发布确认
     * @param relaseId
     * @return
     * @throws Exception
     */
    @POST
    @Transactional
    @Path("pub-confirm")
    public Response publishConfirmTemplateOrWork(@QueryParam("relaseId") String relaseId) throws Exception {

        YunReleaseTemplet yunReleaseTemplet = baseFacade.get(YunReleaseTemplet.class, relaseId);
        if(yunReleaseTemplet==null){
            throw  new Exception("没有获取到对应的信息");
        }
        yunReleaseTemplet.setModifyDate((Timestamp) new Date());
        yunReleaseTemplet.setHstatus("R");
        YunReleaseTemplet releaseTemplet = baseFacade.merge(yunReleaseTemplet);
        return Response.status(Response.Status.OK).entity(releaseTemplet).build();

    }


}
