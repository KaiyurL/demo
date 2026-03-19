package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.common.ResultCode;
import com.example.demo.entity.User;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/users")
public class UserController {

    // 模拟数据库（线程安全的Map）
    private static final ConcurrentHashMap<Long, User> userMap = new ConcurrentHashMap<>();
    private static Long idCounter = 1L;

    // 1. 获取用户信息（查）
    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable("id") Long id) {
        User user = userMap.get(id);
        if (user == null) {
            // 使用 ResultCode 枚举返回错误
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        // 返回成功，数据为 user 对象
        return Result.success(user);
    }

    // 2. 新增用户（增）
    @PostMapping
    public Result<User> createUser(@RequestBody User user) {
        // 设置ID
        user.setId(idCounter++);
        // 保存用户
        userMap.put(user.getId(), user);
        // 返回成功，数据为新增的用户
        return Result.success(user);
    }

    // 3. 全量更新用户信息（改）
    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable("id") Long id, @RequestBody User user) {
        if (!userMap.containsKey(id)) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        user.setId(id);
        userMap.put(id, user);
        return Result.success(user);
    }

    // 4. 删除用户（删）
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable("id") Long id) {
        User removedUser = userMap.remove(id);
        if (removedUser == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        return Result.success("删除成功，已移除 ID 为 " + id + " 的用户");
    }

    // 5. 获取所有用户列表（额外添加，便于测试）
    @GetMapping
    public Result<List<User>> getAllUsers() {
        return Result.success(new ArrayList<>(userMap.values()));
    }

    // 6. 触发异常测试（故意除以零）- 展示全局异常处理的重要性
    @GetMapping("/test/error")
    public Result<String> testError() {
        int a = 1 / 0;  // 故意触发算术异常
        return Result.success("不会执行到这里");
    }
}