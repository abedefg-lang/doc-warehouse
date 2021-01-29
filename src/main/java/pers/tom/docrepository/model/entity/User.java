package pers.tom.docrepository.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lijia
 * @description 用户实体对象
 * @date 2021-01-29 13:27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User{

    /**主键id*/
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**username*/
    @TableField("username")
    private String username;

    /**password*/
    @TableField("password")
    private String password;

    /**上次登录时间*/
    @TableField("last_login_time")
    private Long lastLoginTime;

    /**是否被逻辑删除*/
    @TableLogic("deleted")
    private Boolean deleted;
}