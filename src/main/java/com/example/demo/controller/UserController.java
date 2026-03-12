package com.example.demo.controller;

import com.example.demo.common.Result;
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
            return Result.error("用户不存在，ID：" + id);
        }
        return Result.success("查询成功", user);
    }

    // 2. 新增用户（增）
    @PostMapping
    public Result<User> createUser(@RequestBody User user) {
        // 设置ID
        user.setId(idCounter++);
        // 保存用户
        userMap.put(user.getId(), user);
        return Result.success("新增成功，接收到用户：" + user.getName() + "，年龄：" + user.getAge(), user);
    }

    // 3. 全量更新用户信息（改）
    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable("id") Long id, @RequestBody User user) {
        if (!userMap.containsKey(id)) {
            return Result.error("用户不存在，无法更新，ID：" + id);
        }
        user.setId(id);
        userMap.put(id, user);
        return Result.success("更新成功，ID " + id + " 的用户已修改为：" + user.getName(), user);
    }

    // 4. 删除用户（删）
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable("id") Long id) {
        User removedUser = userMap.remove(id);
        if (removedUser == null) {
            return Result.error("用户不存在，无法删除，ID：" + id);
        }
        return Result.success("删除成功，已移除 ID 为 " + id + " 的用户");
    }

    // 5. 获取所有用户列表（额外添加，便于测试）
    @GetMapping
    public Result<List<User>> getAllUsers() {
        return Result.success("查询成功", new ArrayList<>(userMap.values()));
    }

    // 6. 触发异常测试（故意除以零）
    @GetMapping("/test/error")
    public Result<String> testError() {
        int a = 1 / 0;  // 故意触发算术异常
        return Result.success("不会执行到这里");
    }
}