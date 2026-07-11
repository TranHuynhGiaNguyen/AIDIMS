import React from "react"
import "../../css/auth.css"

function Login() {
  return (
    <div className="auth-page-standalone">
      <div className="auth-container">
        <div className="auth-card">
          <div className="auth-header">
            <div className="auth-icon">🏥</div>
            <h1>Hệ thống AIDIMS</h1>
            <p>Vui lòng chọn vai trò của bạn để đăng nhập</p>
          </div>

          <div className="quick-access">
            <a href="/login/doctor" className="quick-btn doctor">
              <span className="quick-btn-icon">👨‍⚕️</span>
              <span>Bác sĩ</span>
            </a>
            <a href="/login/receptionist" className="quick-btn receptionist">
              <span className="quick-btn-icon">👩‍💼</span>
              <span>Tiếp nhận</span>
            </a>
            <a href="/login/technician" className="quick-btn technician">
              <span className="quick-btn-icon">👨‍🔬</span>
              <span>Kỹ thuật viên</span>
            </a>
            <a href="/login/admin" className="quick-btn admin">
              <span className="quick-btn-icon">👨‍💼</span>
              <span>Quản trị viên</span>
            </a>
          </div>

          <div className="auth-footer" style={{ borderTop: "none", paddingTop: 0 }}>
            <a href="/" className="back-home">
              ← Quay về trang chủ
            </a>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Login;
