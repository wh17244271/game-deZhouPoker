package com.dezhou.poker.repository;

import com.dezhou.poker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户仓库接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    Optional<User> findByUsername(String username);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    Boolean existsByUsername(String username);
}