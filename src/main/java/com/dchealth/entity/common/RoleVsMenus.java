package com.dchealth.entity.common;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Created by Administrator on 2017/6/22.
 */
@Entity
@Table(name = "role_vs_menus", schema = "emhbase", catalog = "")
public class RoleVsMenus {
    private String id;
    private String roleId;
    private String menuDict;

    @Id
    @Column(name = "id")
    @GenericGenerator(name="generator",strategy = "uuid.hex")
    @GeneratedValue(generator = "generator")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic
    @Column(name = "role_id")
    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    @Basic
    @Column(name = "menu_dict")
    public String getMenuDict() {
        return menuDict;
    }

    public void setMenuDict(String menuDict) {
        this.menuDict = menuDict;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoleVsMenus that = (RoleVsMenus) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (roleId != null ? !roleId.equals(that.roleId) : that.roleId != null) return false;
        if (menuDict != null ? !menuDict.equals(that.menuDict) : that.menuDict != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (roleId != null ? roleId.hashCode() : 0);
        result = 31 * result + (menuDict != null ? menuDict.hashCode() : 0);
        return result;
    }
}
