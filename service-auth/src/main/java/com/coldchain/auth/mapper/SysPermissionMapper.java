package com.coldchain.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coldchain.auth.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 鏉冮檺Mapper
 *
 * @author Alnnt
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

        /**
         * 鏍规嵁鐢ㄦ埛ID鏌ヨ鏉冮檺鍒楄〃
         */
        @Select("SELECT DISTINCT p.* FROM sys_permission p " +
                        "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
                        "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
                        "WHERE ur.user_id = #{userId} AND p.deleted = 0 AND p.status = 1 " +
                        "ORDER BY p.sort")
        List<SysPermission> selectByUserId(@Param("userId") Long userId);

        /**
         * 鏍规嵁瑙掕壊ID鏌ヨ鏉冮檺鍒楄〃
         */
        @Select("SELECT p.* FROM sys_permission p " +
                        "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
                        "WHERE rp.role_id = #{roleId} AND p.deleted = 0 AND p.status = 1 " +
                        "ORDER BY p.sort")
        List<SysPermission> selectByRoleId(@Param("roleId") Long roleId);

        /**
         * 鏍规嵁鐖剁骇ID鏌ヨ瀛愭潈闄?
         */
        @Select("SELECT * FROM sys_permission WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort")
        List<SysPermission> selectByParentId(@Param("parentId") Long parentId);
}
