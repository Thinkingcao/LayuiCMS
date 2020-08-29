package com.yzx.layuicms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yzx.layuicms.common.*;
import com.yzx.layuicms.domain.SysDept;
import com.yzx.layuicms.domain.SysPermission;
import com.yzx.layuicms.domain.SysUser;
import com.yzx.layuicms.domain.treeNode;
import com.yzx.layuicms.service.SysPermissionService;
import com.yzx.layuicms.service.SysRoleService;
import com.yzx.layuicms.service.SysUserService;
import com.yzx.layuicms.vo.deptVo;
import com.yzx.layuicms.vo.permissionVo;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 菜单管理控制类
 */
@RestController //返回JSON数据串
@RequestMapping("/menu")
public class menuController {

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private SysUserService userService;

    @Autowired
    private SysRoleService roleService;

    /**
     * 加载左侧导航树
     * @param permissionVo
     * @return
     */
    @RequestMapping("/loadIndexLeftMenuJson")
    public dataGridView loadIndexLeftMenuJson(permissionVo permissionVo) {
        //查询所有菜单
        QueryWrapper<SysPermission> queryWrapper = new QueryWrapper<>();
        //设置只能查询菜单
        queryWrapper.eq("type", constant.TYPE_MNEU);
        queryWrapper.eq("available", constant.AVAILABLE_TRUE);
        //获取当前登录的用户信息
        activerUser loginUser = (activerUser) SecurityUtils.getSubject().getPrincipal();
        SysUser user = loginUser.getUser();
        //创建一个用于装载权限信息的集合
        List<SysPermission> list = null;
        if (user.getType() == constant.USER_TYPE_SUPER) {
            //如果用户是超级管理员，直接获取所有资源树
            list = permissionService.list(queryWrapper);
        } else {
            //否则根据用户ID+角色+权限去查询
            //根据用户id查角色id
            Integer roleIdByUserId = this.userService.findRoleIdByUserId(user.getId());
            //根据角色id查询角色拥有的权限id
            List<Integer> permissionIdByRoleId = this.roleService.getPermissionIdByRoleId(roleIdByUserId);
            //设置筛选条件，展示的菜单id必须在查询结果中
            queryWrapper.in("id",permissionIdByRoleId);
            list = permissionService.list(queryWrapper);
        }

        //创建新的树节点集合
        List<treeNode> treeNodes = new ArrayList<>();
        //遍历当前用户的所有权限信息，放入节点集合中
        for (SysPermission p : list) {
            Integer id = p.getId();
            Integer pid = p.getPid();
            String title = p.getTitle();
            String icon = p.getIcon();
            String href = p.getHref();
            Boolean spread = p.getOpen() == constant.OPEN_TRUE ? true : false;
            treeNodes.add(new treeNode(id, pid, title, icon, href, spread));
        }
        //根据节点集合，构造层级关系
        List<treeNode> list2 = treeNodeBuilder.build(treeNodes, 1);

        //根据完整的层级关系创建完整的左侧导航树，转换为JSON返回
        return new dataGridView(list2);
    }

    /**
     * 加载菜单管理页面左侧导航树，从0开始遍历
     * @param permissionVo
     * @return
     */
    @RequestMapping("/loadMenuLeft")
    public dataGridView loadMenuLeft(permissionVo permissionVo) {
        //查询所有菜单
        QueryWrapper<SysPermission> queryWrapper = new QueryWrapper<>();
        //设置只能查询菜单
        queryWrapper.eq("type", constant.TYPE_MNEU);
        queryWrapper.eq("available", constant.AVAILABLE_TRUE);
        //获取当前登录的用户信息
        activerUser loginUser = (activerUser) SecurityUtils.getSubject().getPrincipal();
        SysUser user = loginUser.getUser();
        //创建一个用于装载权限信息的集合
        List<SysPermission> list = null;
        if (user.getType() == constant.USER_TYPE_SUPER) {
            //如果用户是超级管理员，直接获取所有资源树
            list = permissionService.list(queryWrapper);
        } else {
            //否则根据用户ID+角色+权限去查询
            //根据用户id查角色id
            Integer roleIdByUserId = this.userService.findRoleIdByUserId(user.getId());
            //根据角色id查询角色拥有的权限id
            List<Integer> permissionIdByRoleId = this.roleService.getPermissionIdByRoleId(roleIdByUserId);
            //设置筛选条件，展示的菜单id必须在查询结果中
            queryWrapper.in("id",permissionIdByRoleId);
            list = permissionService.list(queryWrapper);
        }

        //创建新的树节点集合
        List<treeNode> treeNodes = new ArrayList<>();
        //遍历当前用户的所有权限信息，放入节点集合中
        for (SysPermission p : list) {
            Integer id = p.getId();
            Integer pid = p.getPid();
            String title = p.getTitle();
            String icon = p.getIcon();
            String href = p.getHref();
            Boolean spread = p.getOpen() == constant.OPEN_TRUE ? true : false;
            treeNodes.add(new treeNode(id, pid, title, icon, href, spread));
        }
        //根据节点集合，构造层级关系
        List<treeNode> list2 = treeNodeBuilder.build(treeNodes, 0);

        //根据完整的层级关系创建完整的左侧导航树，转换为JSON返回
        return new dataGridView(list2);
    }

    /**
     * 获取所有菜单信息
     * @param perVo
     * @return
     */
    @RequestMapping("/loadAllMenu")
    public dataGridView loadAllMenu(permissionVo perVo) {
        //开启分页，根据页面传来的分页信息对查询结果进行分页
        IPage<SysPermission> page = new Page<>(perVo.getPage(),perVo.getLimit());

        QueryWrapper<SysPermission> wrapper = new QueryWrapper<>();
        wrapper.eq("type",constant.TYPE_MNEU);
        wrapper.like(StringUtils.isNotEmpty(perVo.getTitle()), "title",perVo.getTitle());
        wrapper.and(perVo.getId() != null && perVo.getId() != null,Wrapper -> Wrapper.eq("id", perVo.getId())
                .or()
                .eq("pid", perVo.getId()));
        wrapper.orderByAsc("ordernum");

        //使用服务类根据page对象和筛选条件进行分页数据查找
        this.permissionService.page(page, wrapper);

        //以查询到的所有数据树和数据本身作为参数，创建新的dataGridView对象返回JSON字符串
        return new dataGridView((long) page.getRecords().size(), page.getRecords());
    }

    /**
     * 添加菜单
     * @param permission
     * @return
     */
    @RequestMapping("/addMenu")
    public resultObj addMenu(SysPermission permission) {
        try {
            //获取主体对象
            Subject subject = SecurityUtils.getSubject();
            //对主体对象的权限认证，是否有权限进行操作
            if(subject.isPermitted("menu:create")){
                //存在
                permission.setType("menu");
                this.permissionService.save(permission);
            }else{
                //不存在
                return resultObj.AUTH_ERROR;
            }
            return resultObj.ADD_SUCCESS;
        }catch (Exception e){
            return resultObj.ADD_ERROR;
        }
    }

    /**
     * 删除菜单
     * @param id
     * @return
     */
    @RequestMapping("/deleteMenu")
    public resultObj deleteMenu(Integer id) {
        try {
            //获取主体对象
            Subject subject = SecurityUtils.getSubject();
            //对主体对象的权限认证，是否有权限进行操作
            if(subject.isPermitted("menu:delete")){
                //存在
                this.permissionService.removeById(id);
            }else{
                //不存在
                return resultObj.AUTH_ERROR;
            }
            return resultObj.DELETE_SUCCESS;
        }catch (Exception e){
            return resultObj.DELETE_ERROR;
        }
    }

    /**
     * 更新菜单信息
     * @param permission
     * @return
     */
    @RequestMapping("/updateMenu")
    public resultObj updateDept(SysPermission permission) {
        try {
            //获取主体对象
            Subject subject = SecurityUtils.getSubject();
            //对主体对象的权限认证，是否有权限进行操作
            if(subject.isPermitted("menu:update")){
                //存在
                this.permissionService.updateById(permission);
            }else{
                //不存在
                return resultObj.AUTH_ERROR;
            }
            return resultObj.UPDATE_SUCCESS;
        }catch (Exception e){
            return resultObj.UPDATE_ERROR;
        }
    }

    /**
     * 检查该菜单是否有子部门
     * @param id
     * @return
     */
    @RequestMapping("/checkMenu")
    public resultObj checkParent(Integer id) {
        //获取主体对象
        Subject subject = SecurityUtils.getSubject();
        //对主体对象的权限认证，是否有权限进行操作
        if(subject.isPermitted("menu:delete")){
            //存在
            QueryWrapper<SysPermission> wrapper = new QueryWrapper<>();
            wrapper.eq("pid",id);
            List<SysPermission> list = this.permissionService.list(wrapper);

            return list.size() > 0 ? resultObj.DELETE_ERROR : resultObj.DELETE_SUCCESS;
        }else{
            //不存在
            return resultObj.AUTH_ERROR;
        }
    }

}
