package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.common.ResultCode;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 模拟数据库（用于测试受保护资源）
    private static final ConcurrentHashMap<Long, User> userMap = new ConcurrentHashMap<>();
    private static Long idCounter = 1L;

    // ========== 使用 Service 层的业务接口 ==========

    /**
     * 用户注册
     * POST /api/users
     */
    @PostMapping
    public Result<String> register(@RequestBody UserDTO userDTO) {
        return userService.register(userDTO);
    }

    /**
     * 用户登录
     * POST /api/users/login
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody UserDTO userDTO) {
        return userService.login(userDTO);
    }

    // ========== 受保护的资源接口 ==========

    /**
     * 获取用户信息（受保护资源，需要 Token）
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable("id") Long id) {
        return userService.getUserById(id);
    }

    /**
     * 分页查询用户列表 - 路径为 GET /api/users/page
     */
    @GetMapping("/page")
    public Result<Object> getUserPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize) {
        return userService.getUserPage(pageNum, pageSize);
    }

    /**
     * 创建用户（受保护资源，需要 Token）
     * POST /api/users/test
     */
    @PostMapping("/test")
    public Result<User> createUser(@RequestBody User user) {
        user.setId(idCounter++);
        userMap.put(user.getId(), user);
        return Result.success(user);
    }

    /**
     * 获取所有用户（受保护资源，需要 Token）
     * GET /api/users/list
     */
    @GetMapping("/list")
    public Result<List<User>> getAllUsers() {
        return Result.success(new ArrayList<>(userMap.values()));
    }

    /**
     * 更新用户（受保护资源，需要 Token）
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable("id") Long id, @RequestBody User user) {
        if (!userMap.containsKey(id)) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        user.setId(id);
        userMap.put(id, user);
        return Result.success(user);
    }

    /**
     * 删除用户（受保护资源，需要 Token）
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable("id") Long id) {
        User removedUser = userMap.remove(id);
        if (removedUser == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        return Result.success("删除成功，已移除 ID 为 " + id + " 的用户");
    }
}
