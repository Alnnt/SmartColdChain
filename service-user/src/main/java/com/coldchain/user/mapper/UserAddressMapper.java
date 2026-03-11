package com.coldchain.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coldchain.user.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户地址Mapper
 *
 * @author Alnnt
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {

    /**
     * 查询用户的所有地址
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    @Select("SELECT * FROM t_user_address WHERE user_id = #{userId} AND deleted = 0 ORDER BY is_default DESC, create_time DESC")
    List<UserAddress> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的默认地址
     *
     * @param userId 用户ID
     * @return 默认地址
     */
    @Select("SELECT * FROM t_user_address WHERE user_id = #{userId} AND is_default = 1 AND deleted = 0 LIMIT 1")
    UserAddress selectDefaultByUserId(@Param("userId") Long userId);

    /**
     * 取消用户的所有默认地址
     *
     * @param userId 用户ID
     */
    @Update("UPDATE t_user_address SET is_default = 0 WHERE user_id = #{userId} AND deleted = 0")
    void cancelDefaultByUserId(@Param("userId") Long userId);
}
