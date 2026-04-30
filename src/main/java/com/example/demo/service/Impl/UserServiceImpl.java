package com.example.demo.service.Impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.common.ResultCode;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.UserInfo;
import com.example.demo.mapper.UserInfoMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.UserService;
import com.example.demo.vo.UserDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service // 必须添加该注解，将业务类交给 Spring 容器管理
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String CACHE_KEY_PREFIX = "user:detail:";

    @Override
    public Result<String> register(UserDTO userDTO) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userDTO.getUsername());
        User existed = userMapper.selectOne(queryWrapper);
        if (existed != null) {
            return Result.error(ResultCode.USER_HAS_EXISTED);
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());

        int inserted = userMapper.insert(user);
        if (inserted != 1) {
            return Result.error(ResultCode.USER_CREATE_FAILED);
        }

        return Result.success("注册成功，用户ID=" + user.getId());
    }

    @Override
    public Result<String> login(UserDTO userDTO) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userDTO.getUsername());
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        if (user.getPassword() == null || !user.getPassword().equals(userDTO.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR);
        }

        String jwt = jwtUtil.generateToken(userDTO.getUsername());

        return Result.success(jwt);
    }

    @Override
    public Result<Object> getUserPage(Integer pageNum, Integer pageSize) {
        // 1. 创建分页对象（参数 1：当前页码，参数 2：每页显示条数）
        Page<User> pageParam = new Page<>(pageNum, pageSize);

        // 2. 执行分页查询（参数 1：分页对象，参数 2：查询条件 Wrapper，这里传 null 代表查询全部）
        // 框架会自动执行一条 COUNT 语句查询总数，再拼接 LIMIT 执行分页
        Page<User> resultPage = userMapper.selectPage(pageParam, null);

        // 3. 返回结果（resultPage 中包含了 records 数据列表、total 总条数、pages 总页数）
        return Result.success(resultPage);
    }

    @Override
    public Result<User> getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @Override
    public Result<UserDetailVO> getUserDetail(Long userId) {
        String key = CACHE_KEY_PREFIX + userId;

        // 1. 先查缓存
        String json = redisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            try {
                UserDetailVO cacheVO = JSONUtil.toBean(json, UserDetailVO.class);
                return Result.success(cacheVO);
            } catch (Exception e) {
                // 缓存数据异常，删除脏缓存，继续查数据库
                redisTemplate.delete(key);
            }
        }

        // 2. 查数据库
        UserDetailVO detail = userInfoMapper.getUserDetail(userId);
        if (detail == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        // 3. 写缓存
        redisTemplate.opsForValue().set(
                key,
                JSONUtil.toJsonStr(detail),
                10,
                TimeUnit.MINUTES
        );

        return Result.success(detail);
    }

    @Override
    @Transactional
    public Result<String> updateUserInfo(UserInfo userInfo) {
        // 参数校验，userInfo 不能为空，并且 userId 不能为空，后面删除 Redis 缓存时要用
        if (userInfo == null || userInfo.getUserId() == null) {
            return Result.error("参数错误：userId 不能为空");
        }

        // 1. 更新数据库
        // 根据 userId 更新扩展信息，如果不存在则新增
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getUserId, userInfo.getUserId());
        UserInfo dbUserInfo = userInfoMapper.selectOne(queryWrapper);

        if (dbUserInfo == null) {
            userInfoMapper.insert(userInfo);
        } else {
            userInfo.setId(dbUserInfo.getId());
            userInfoMapper.updateById(userInfo);
        }

        // 2. 删除缓存（保证数据一致性）
        redisTemplate.delete(CACHE_KEY_PREFIX + userInfo.getUserId());

        return Result.success("更新成功");
    }

    @Override
    @Transactional
    public Result<String> deleteUser(Long userId) {
        // 1. 删除用户基本信息
        userMapper.deleteById(userId);

        // 2. 删除用户扩展信息
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getUserId, userId);
        userInfoMapper.delete(queryWrapper);

        // 3. 删除缓存
        redisTemplate.delete(CACHE_KEY_PREFIX + userId);

        return Result.success("删除成功");
    }
}
