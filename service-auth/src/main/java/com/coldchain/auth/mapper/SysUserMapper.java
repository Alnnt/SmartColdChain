package com.coldchain.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coldchain.auth.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 鐢ㄦ埛Mapper锛堟槧灏?t_user 琛級
 *
 * @author Alnnt
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT * FROM t_user WHERE username = #{username} AND deleted = 0")
    SysUser selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM t_user WHERE phone = #{phone} AND deleted = 0")
    SysUser selectByPhone(@Param("phone") String phone);
}
