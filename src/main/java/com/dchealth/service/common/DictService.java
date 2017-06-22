package com.dchealth.service.common;

import com.dchealth.entity.common.YunDictitem;
import com.dchealth.entity.common.YunDicttype;
import com.dchealth.facade.common.BaseFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/16.
 */
@Controller
@Produces("application/json")
@Path("dict")
@Service
public class DictService {


    @Autowired
    private BaseFacade baseFacade;

    /**
     * 获取字典列表
     *
     * @param typeName 字典名称，如果为空或者不写则返回所有的值
     * @return
     */
    @GET
    @Path("list-type-by-name")
    public List<YunDicttype> listDictType(@QueryParam("typeName") String typeName,@QueryParam("userId")String userId) {
        String hql = "from YunDicttype where 1=1 ";
        if (typeName != null && !"".equals(typeName)) {
            hql += " and typeName like '%" + typeName + "%'";
        }
        if (userId != null && !"".equals(userId)) {
            hql += " and userId = '" + userId + "'";
        }
        return baseFacade.createQuery(YunDicttype.class, hql, new ArrayList<Object>()).getResultList();
}

    /**
     * 获取所有的字典项目
     *
     * @param typeId
     * @return
     */
    @GET
    @Path("list-item-by-type")
    public List<YunDictitem> listDictItemByTypeId(@QueryParam("typeId") String typeId) {
        String hql = "from YunDictitem as item where item.typeIdDm='" + typeId + "'";
        return baseFacade.createQuery(YunDictitem.class, hql, new ArrayList<Object>()).getResultList();
    }


    /**
     * 添加新的字典
     *
     * @param yunDicttype
     * @return
     */
    @Path("add-new-type")
    @Transactional
    @POST
    public Response addDictType(YunDicttype yunDicttype) {
        YunDicttype dicttype = baseFacade.merge(yunDicttype);
        return Response.status(Response.Status.OK).entity(dicttype).build();
    }


    /**
     * 单个添加字典项目
     *
     * @param yunDictitem
     * @return
     */
    @POST
    @Transactional
    @Path("add-new-item")
    public Response addDictItem(YunDictitem yunDictitem) {
        YunDictitem merge = baseFacade.merge(yunDictitem);
        return Response.status(Response.Status.OK).entity(merge).build();
    }

    /**
     * 添加字典项目
     * @param yunDictitems
     * @return
     */
    @POST
    @Transactional
    @Path("add-new-items")
    public Response addDictItmes(List<YunDictitem> yunDictitems) {
        for (YunDictitem item : yunDictitems) {
            baseFacade.merge(item);
        }
        return Response.status(Response.Status.OK).entity(yunDictitems).build();
    }


    /**
     * 删除某一下字典
     * @param serialNo
     * @return
     */
    @POST
    @Transactional
    @Path("del-item")
    public Response removeDictItem(@QueryParam("serialNo") String serialNo){
        YunDictitem yunDictitem = baseFacade.get(YunDictitem.class, serialNo);
        baseFacade.remove(yunDictitem);
        return Response.status(Response.Status.OK).entity(yunDictitem).build();
    }

    /**
     * 删除所有的类别
     * @param typeId
     * @return
     */
    @POST
    @Transactional
    @Path("del-type")
    public Response removeDictType(@QueryParam("typeId")String typeId){
        try {
            String itemHql ="from YunDictitem as item where item.typeIdDm='"+typeId+"'";
            List<YunDictitem> yunDictitems = baseFacade.createQuery(YunDictitem.class,itemHql,new ArrayList<Object>()).getResultList();
            List<String> itemIds = new ArrayList<String>();
            for(YunDictitem dictitem:yunDictitems){
                itemIds.add(dictitem.getSerialNo());
            }

            String typeHql = "from YunDicttype as type where type.typeId='"+typeId+"'";
            List<YunDicttype> yunDicttypes = baseFacade.createQuery(YunDicttype.class,typeHql,new ArrayList<Object>()).getResultList();
            List<String> typeIds = new ArrayList<>() ;
            for (YunDicttype dicttype:yunDicttypes){
                typeIds.add(dicttype.getTypeId()) ;
            }
            baseFacade.removeByStringIds(YunDictitem.class,itemIds);
            baseFacade.removeByStringIds(YunDicttype.class,typeIds);
            return Response.status(Response.Status.OK).entity(yunDictitems).build();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

}
