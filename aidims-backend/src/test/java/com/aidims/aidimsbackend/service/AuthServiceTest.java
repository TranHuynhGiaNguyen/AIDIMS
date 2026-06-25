package com.aidims.aidimsbackend.service;

import com.aidims.aidimsbackend.dto.LoginRequest;
import com.aidims.aidimsbackend.dto.LoginResponse;
import com.aidims.aidimsbackend.entity.Role;
import com.aidims.aidimsbackend.entity.User;
import com.aidims.aidimsbackend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Unit Tests (Cross-role Login Bug Verification)")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("❌ Lỗi nghiệp vụ: Đăng nhập sai vai trò yêu cầu phải thất bại (Đăng nhập chéo vai trò)")
    void login_IncorrectRole_ShouldFail() {
        User mockUser = new User();
        mockUser.setUsername("doctor_user");
        mockUser.setPassword("doctorPass");
        mockUser.setActive(true);
        Role mockRole = new Role("doctor", "Doctor Role");
        mockUser.setRole(mockRole);

        when(userRepository.findByUsername("doctor_user")).thenReturn(Optional.of(mockUser));

        // Tài khoản có vai trò 'doctor', nhưng cố gắng đăng nhập với vai trò 'receptionist'
        // Do LoginRequest thiếu trường role và AuthService không kiểm tra role, đăng nhập vẫn thành công.
        // Kỳ vọng kiểm thử: API phải trả về trạng thái "error".
        LoginRequest request = new LoginRequest();
        request.setUsername("doctor_user");
        request.setPassword("doctorPass");

        LoginResponse response = authService.login(request);

        // Vì AuthService hiện tại không so khớp vai trò yêu cầu từ frontend, response.getStatus() sẽ là "success".
        // Assertion này sẽ thất bại để chỉ ra lỗi đăng nhập chéo vai trò.
        assertEquals("error", response.getStatus(), "Đăng nhập với tài khoản sai vai trò yêu cầu phải thất bại");
    }
}
