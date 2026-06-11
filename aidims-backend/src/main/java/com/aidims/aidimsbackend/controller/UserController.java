package com.aidims.aidimsbackend.controller;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aidims.aidimsbackend.dto.UserDto;
import com.aidims.aidimsbackend.entity.Role;
import com.aidims.aidimsbackend.entity.User;
import com.aidims.aidimsbackend.repository.RoleRepository;
import com.aidims.aidimsbackend.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Map<String, String> ROLE_DISPLAY_NAMES = Map.of(
            "admin", "Quản trị viên",
            "doctor", "Bác sĩ",
            "receptionist", "Nhân viên tiếp nhận",
            "technician", "Kỹ thuật viên");

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userRepository.findAll()
                .stream()
                .map(user -> {
                    String internalRole = user.getRole().getRoleName();
                    String displayRole = ROLE_DISPLAY_NAMES.getOrDefault(internalRole, internalRole);
                    return new UserDto(
                            user.getUserId(),
                            user.getUsername(),
                            user.getFullName(),
                            displayRole,
                            user.getEmail(),
                            user.isActive());
                })
                .toList();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("userId", user.getUserId());
            result.put("username", user.getUsername());
            result.put("email", user.getEmail());
            result.put("fullName", user.getFullName());
            result.put("isActive", user.isActive());
            result.put("role", user.getRole().getRoleName());

            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy người dùng"));
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            user.setUserId(null);
            user.setActive(true);
            user.setRole(resolveRole(user.getRole()));

            User savedUser = userRepository.findByUsername(user.getUsername())
                    .or(() -> userRepository.findByEmail(user.getEmail()))
                    .map(existingUser -> {
                        existingUser.setPassword(user.getPassword());
                        existingUser.setFullName(user.getFullName());
                        existingUser.setEmail(user.getEmail());
                        existingUser.setPhone(user.getPhone());
                        existingUser.setRole(user.getRole());
                        existingUser.setActive(true);
                        return userRepository.save(existingUser);
                    })
                    .orElseGet(() -> insertUser(user));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("userId", savedUser.getUserId());
            data.put("username", savedUser.getUsername());
            data.put("fullName", savedUser.getFullName());
            data.put("email", savedUser.getEmail());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "success");
            response.put("message", "Tạo người dùng thành công");
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("status", "error");
            error.put("message", "Đã xảy ra lỗi khi tạo người dùng");
            error.put("error", e.getMessage());

            return ResponseEntity.status(500).body(error);
        }
    }

    private User insertUser(User user) {
        Long nextId = nextUserId();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        jdbcTemplate.update(
                "INSERT INTO `user` (user_id, username, password, role_id, full_name, email, phone, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                nextId,
                user.getUsername(),
                user.getPassword(),
                user.getRole().getRoleId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                true,
                now,
                now);

        return userRepository.findById(nextId).orElseGet(() -> {
            user.setUserId(nextId);
            return user;
        });
    }

    private Role resolveRole(Role incomingRole) throws Exception {
        if (incomingRole != null && incomingRole.getRoleId() != null) {
            Optional<Role> roleById = roleRepository.findById(incomingRole.getRoleId());
            if (roleById.isPresent()) {
                return roleById.get();
            }
        }

        if (incomingRole != null && incomingRole.getRoleName() != null) {
            Optional<Role> roleByName = roleRepository.findByRoleName(incomingRole.getRoleName());
            if (roleByName.isPresent()) {
                return roleByName.get();
            }
        }

        Optional<Role> defaultRole = roleRepository.findByRoleName("receptionist");
        if (defaultRole.isPresent()) {
            return defaultRole.get();
        }

        Optional<Role> anyRole = roleRepository.findAll().stream().findFirst();
        if (anyRole.isPresent()) {
            return anyRole.get();
        }

        throw new Exception("Không có role nào khả dụng trong hệ thống");
    }

    private Long nextUserId() {
        Long nextId = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(user_id), 0) + 1 FROM `user`", Long.class);
        return nextId != null ? nextId : 1L;
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        Optional<User> userOpt = userRepository.findById(id);
        System.out.println("Updating user with ID: " + id);
        System.out.println("Updated user details: " + updatedUser);

        Map<String, Object> response = new HashMap<>();

        if (userOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Không tìm thấy người dùng với ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        User existingUser = userOpt.get();
        existingUser.setPassword(updatedUser.getPassword());
        existingUser.setFullName(updatedUser.getFullName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhone(updatedUser.getPhone());

        User savedUser = userRepository.save(existingUser);

        if (savedUser.getRole() != null) {
            savedUser.getRole().setUsers(null);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", savedUser.getUserId());
        data.put("username", savedUser.getUsername());
        data.put("fullName", savedUser.getFullName());
        data.put("email", savedUser.getEmail());
        data.put("isActive", savedUser.isActive());
        data.put("role", savedUser.getRole().getRoleName());

        response.put("status", "success");
        response.put("message", "Cập nhật người dùng thành công");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/update-status/{id}")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        Optional<User> userOpt = userRepository.findById(id);
        Map<String, Object> response = new HashMap<>();

        if (userOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Không tìm thấy người dùng");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        User user = userOpt.get();
        Boolean isActive = payload.get("isActive");

        if (isActive == null) {
            response.put("status", "error");
            response.put("message", "Giá trị 'isActive' không hợp lệ");
            return ResponseEntity.badRequest().body(response);
        }

        user.setActive(isActive);
        User savedUser = userRepository.save(user);

        if (savedUser.getRole() != null) {
            savedUser.getRole().setUsers(null);
        }

        Map<String, Object> userData = new LinkedHashMap<>();
        userData.put("userId", savedUser.getUserId());
        userData.put("username", savedUser.getUsername());
        userData.put("fullName", savedUser.getFullName());
        userData.put("email", savedUser.getEmail());
        userData.put("isActive", savedUser.isActive());
        userData.put("role", savedUser.getRole().getRoleName());

        response.put("status", "success");
        response.put("message", "Cập nhật trạng thái thành công");
        response.put("data", userData);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("status", "error");
            error.put("message", "User not found with id = " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        userRepository.deleteById(id);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("message", "User deleted successfully");
        response.put("userId", id);

        return ResponseEntity.ok(response);
    }
}