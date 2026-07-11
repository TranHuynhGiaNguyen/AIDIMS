package com.aidims.aidimsbackend.service;

import com.aidims.aidimsbackend.dto.LoginRequest;
import com.aidims.aidimsbackend.dto.LoginResponse;
import com.aidims.aidimsbackend.entity.Role;
import com.aidims.aidimsbackend.entity.User;
import com.aidims.aidimsbackend.repository.UserRepository;
import com.aidims.aidimsbackend.config.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User mockDoctorUser;
    private User mockAdminUser;
    private User mockReceptionistUser;
    private User mockTechnicianUser;
    private Role doctorRole;
    private Role adminRole;
    private Role receptionistRole;
    private Role technicianRole;

    @BeforeEach
    void setUp() {
        doctorRole = new Role("doctor", "Doctor Role");
        adminRole = new Role("admin", "Admin Role");
        receptionistRole = new Role("receptionist", "Receptionist Role");
        technicianRole = new Role("technician", "Technician Role");

        mockDoctorUser = new User();
        mockDoctorUser.setUserId(1L);
        mockDoctorUser.setUsername("doctor_user");
        mockDoctorUser.setPassword("doctorPass");
        mockDoctorUser.setFullName("Doctor A");
        mockDoctorUser.setEmail("doctor@hospital.com");
        mockDoctorUser.setPhone("0901111111");
        mockDoctorUser.setActive(true);
        mockDoctorUser.setRole(doctorRole);

        mockAdminUser = new User();
        mockAdminUser.setUserId(2L);
        mockAdminUser.setUsername("admin_user");
        mockAdminUser.setPassword("adminPass");
        mockAdminUser.setFullName("Admin A");
        mockAdminUser.setEmail("admin@hospital.com");
        mockAdminUser.setPhone("0902222222");
        mockAdminUser.setActive(true);
        mockAdminUser.setRole(adminRole);

        mockReceptionistUser = new User();
        mockReceptionistUser.setUserId(3L);
        mockReceptionistUser.setUsername("rec_user");
        mockReceptionistUser.setPassword("recPass");
        mockReceptionistUser.setFullName("Receptionist A");
        mockReceptionistUser.setEmail("rec@hospital.com");
        mockReceptionistUser.setPhone("0903333333");
        mockReceptionistUser.setActive(true);
        mockReceptionistUser.setRole(receptionistRole);

        mockTechnicianUser = new User();
        mockTechnicianUser.setUserId(4L);
        mockTechnicianUser.setUsername("tech_user");
        mockTechnicianUser.setPassword("techPass");
        mockTechnicianUser.setFullName("Technician A");
        mockTechnicianUser.setEmail("tech@hospital.com");
        mockTechnicianUser.setPhone("0904444444");
        mockTechnicianUser.setActive(true);
        mockTechnicianUser.setRole(technicianRole);
    }

    private LoginRequest createRequest(String username, String password, String role) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setRole(role);
        return request;
    }

    // =============================================================
    // NHOM 1 - SUCCESS (TC1-TC4)
    // =============================================================

    @Nested
    @DisplayName("Nhom 1 - Dang nhap thanh cong")
    class SuccessTests {

        @Test
        @DisplayName("TC1 - Dang nhap Doctor thanh cong")
        void tc1_Login_Doctor_Success() {
            when(userRepository.findByUsername("doctor_user")).thenReturn(Optional.of(mockDoctorUser));
            when(jwtTokenProvider.generateToken(anyString(), anyString())).thenReturn("mocked_jwt_token");

            LoginRequest request = createRequest("doctor_user", "doctorPass", "doctor");
            LoginResponse response = authService.login(request);

            assertEquals("success", response.getStatus());
            assertNotNull(response.getData());
            assertEquals("doctor", response.getData().get("role"));
            assertEquals("Doctor A", response.getData().get("fullName"));
        }

        @Test
        @DisplayName("TC2 - Dang nhap Admin thanh cong")
        void tc2_Login_Admin_Success() {
            when(userRepository.findByUsername("admin_user")).thenReturn(Optional.of(mockAdminUser));
            when(jwtTokenProvider.generateToken(anyString(), anyString())).thenReturn("mocked_jwt_token");

            LoginRequest request = createRequest("admin_user", "adminPass", "admin");
            LoginResponse response = authService.login(request);

            assertEquals("success", response.getStatus());
            assertEquals("admin", response.getData().get("role"));
        }

        @Test
        @DisplayName("TC3 - Dang nhap Receptionist thanh cong")
        void tc3_Login_Receptionist_Success() {
            when(userRepository.findByUsername("rec_user")).thenReturn(Optional.of(mockReceptionistUser));
            when(jwtTokenProvider.generateToken(anyString(), anyString())).thenReturn("mocked_jwt_token");

            LoginRequest request = createRequest("rec_user", "recPass", "receptionist");
            LoginResponse response = authService.login(request);

            assertEquals("success", response.getStatus());
            assertEquals("receptionist", response.getData().get("role"));
        }

        @Test
        @DisplayName("TC4 - Dang nhap Technician thanh cong")
        void tc4_Login_Technician_Success() {
            when(userRepository.findByUsername("tech_user")).thenReturn(Optional.of(mockTechnicianUser));
            when(jwtTokenProvider.generateToken(anyString(), anyString())).thenReturn("mocked_jwt_token");

            LoginRequest request = createRequest("tech_user", "techPass", "technician");
            LoginResponse response = authService.login(request);

            assertEquals("success", response.getStatus());
            assertEquals("technician", response.getData().get("role"));
        }
    }

    // =============================================================
    // NHOM 2 - REQUEST NULL (TC5)
    // =============================================================

    @Nested
    @DisplayName("Nhom 2 - Request null")
    class RequestNullTests {

        @Test
        @DisplayName("TC5 - Request null -> Loi")
        void tc5_Login_Request_Null() {
            LoginResponse response = authService.login(null);

            assertEquals("error", response.getStatus());
            assertTrue(response.getMessage().contains("Yêu cầu đăng nhập không hợp lệ"));
        }
    }

    // =============================================================
    // NHOM 3 - ROLE KHONG HOP LE (TC6-TC8)
    // =============================================================

    @Nested
    @DisplayName("Nhom 3 - Role khong hop le")
    class InvalidRoleTests {

        @Test
        @DisplayName("TC6 - Role null -> Loi")
        void tc6_Login_Role_Null() {
            LoginRequest request = createRequest("doctor_user", "doctorPass", null);
            LoginResponse response = authService.login(request);

            assertEquals("error", response.getStatus());
            assertTrue(response.getMessage().contains("Vai trò đăng nhập không được để trống"));
        }

        @Test
        @DisplayName("TC7 - Role rong -> Loi")
        void tc7_Login_Role_Empty() {
            LoginRequest request = createRequest("doctor_user", "doctorPass", "");
            LoginResponse response = authService.login(request);

            assertEquals("error", response.getStatus());
            assertTrue(response.getMessage().contains("Vai trò đăng nhập không được để trống"));
        }

        @Test
        @DisplayName("TC8 - Role chi khoang trang -> Loi")
        void tc8_Login_Role_Whitespace() {
            LoginRequest request = createRequest("doctor_user", "doctorPass", "   ");
            LoginResponse response = authService.login(request);

            assertEquals("error", response.getStatus());
            assertTrue(response.getMessage().contains("Vai trò đăng nhập không được để trống"));
        }
    }

    // =============================================================
    // NHOM 4 - USER NOT FOUND (TC9)
    // =============================================================

    @Nested
    @DisplayName("Nhom 4 - User khong ton tai")
    class UserNotFoundTests {

        @Test
        @DisplayName("TC9 - User khong ton tai -> Loi")
        void tc9_Login_UserNotFound() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            LoginRequest request = createRequest("unknown", "anyPass", "doctor");
            LoginResponse response = authService.login(request);

            assertEquals("error", response.getStatus());
            assertTrue(response.getMessage().contains("Tài khoản không tồn tại"));
        }
    }

    // =============================================================
    // NHOM 5 - SAI MAT KHAU (TC10)
    // =============================================================

    @Nested
    @DisplayName("Nhom 5 - Sai mat khau")
    class WrongPasswordTests {

        @Test
        @DisplayName("TC10 - Mat khau sai -> Loi")
        void tc10_Login_WrongPassword() {
            when(userRepository.findByUsername("doctor_user")).thenReturn(Optional.of(mockDoctorUser));

            LoginRequest request = createRequest("doctor_user", "wrongPass", "doctor");
            LoginResponse response = authService.login(request);

            assertEquals("error", response.getStatus());
            assertTrue(response.getMessage().contains("Mật khẩu không chính xác"));
        }
    }

    // =============================================================
    // NHOM 6 - TAI KHOAN INACTIVE (TC11)
    // =============================================================

    @Nested
    @DisplayName("Nhom 6 - Tai khoan inactive")
    class InactiveUserTests {

        @Test
        @DisplayName("TC11 - Tai khoan bi vo hieu hoa -> Loi")
        void tc11_Login_Inactive() {
            mockDoctorUser.setActive(false);
            when(userRepository.findByUsername("doctor_user")).thenReturn(Optional.of(mockDoctorUser));

            LoginRequest request = createRequest("doctor_user", "doctorPass", "doctor");
            LoginResponse response = authService.login(request);

            assertEquals("error", response.getStatus());
            assertTrue(response.getMessage().contains("Tài khoản đã bị vô hiệu hóa"));
        }
    }

    // =============================================================
    // NHOM 7 - SAI VAI TRO (TC12)
    // =============================================================

    @Nested
    @DisplayName("Nhom 7 - Sai vai tro")
    class WrongRoleTests {

        @Test
        @DisplayName("TC12 - Dang nhap vai tro sai -> Loi")
        void tc12_Login_WrongRole() {
            when(userRepository.findByUsername("doctor_user")).thenReturn(Optional.of(mockDoctorUser));

            LoginRequest request = createRequest("doctor_user", "doctorPass", "receptionist");
            LoginResponse response = authService.login(request);

            assertEquals("error", response.getStatus());
            assertTrue(response.getMessage().contains("Tài khoản không thuộc vai trò yêu cầu"));
        }
    }

    // =============================================================
    // NHOM 8 - WHITE-BOX: EXCEPTION (TC13)
    // =============================================================

    @Nested
    @DisplayName("Nhom 8 - Exception")
    class ExceptionTests {

        @Test
        @DisplayName("TC13 - Repository nem exception -> Loi he thong")
        void tc13_Login_RepositoryException() {
            when(userRepository.findByUsername(anyString())).thenThrow(new RuntimeException("DB connection failed"));

            LoginRequest request = createRequest("doctor_user", "doctorPass", "doctor");
            LoginResponse response = authService.login(request);

            assertEquals("error", response.getStatus());
            assertTrue(response.getMessage().contains("Lỗi hệ thống"));
            assertTrue(response.getMessage().contains("DB connection failed"));
        }
    }
}
