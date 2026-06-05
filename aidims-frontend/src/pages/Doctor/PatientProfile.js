"use client"

import { memo, useState, useEffect } from "react"
import { Link } from 'react-router-dom';
import LayoutLogin from "../Layout/LayoutLogin"
import { patientService } from "../../services/patientService"
import "../../css/PatientProfile.css"

const PatientProfile = () => {
  const [patients, setPatients] = useState([])
  const [selectedPatient, setSelectedPatient] = useState(null)
  const [showModal, setShowModal] = useState(false)
  const [activeTab, setActiveTab] = useState("general")
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [statusFilter, setStatusFilter] = useState("all") // Thêm filter cho trạng thái

  // Load patients from database (same source as receptionist)
  useEffect(() => {
    const fetchPatients = async () => {
      try {
        setLoading(true)
        const patientsData = await patientService.getAllPatients()

        // Transform data for doctor view
        const transformedPatients = patientsData.map((patient) => ({
          id: patient.patient_id,
          patientCode: patient.patient_code,
          fullName: patient.full_name,
          dateOfBirth: patient.date_of_birth,
          gender: patient.gender,
          phone: patient.phone,
          email: patient.email,
          address: patient.address,
          identityNumber: patient.identity_number,
          insuranceNumber: patient.insurance_number,
          bloodType: patient.blood_type,
          allergies: patient.allergies || "Không có",
          medicalHistory: patient.medical_history || "Chưa có thông tin",
          // Vital signs from receptionist
          temperature: patient.temperature,
          heartRate: patient.heart_rate,
          bloodPressureSystolic: patient.blood_pressure_systolic,
          bloodPressureDiastolic: patient.blood_pressure_diastolic,
          respiratoryRate: patient.respiratory_rate,
          oxygenSaturation: patient.oxygen_saturation,
          weight: patient.weight,
          height: patient.height,
          // Default fields for doctor view
          chiefComplaint: "Chưa khám",
          specialty: "Tổng quát",
          status: patient.status || "Chờ khám", // Sử dụng trạng thái từ DB hoặc mặc định
          priority: "Bình thường",
          visitDate: new Date().toISOString().split("T")[0],
          completedAt: patient.completed_at || null, // Thời gian hoàn thành khám
        }))

        setPatients(transformedPatients)
      } catch (err) {
        console.error("Lỗi:", err)
        setError("Không thể tải danh sách bệnh nhân")
        setPatients([])
      } finally {
        setLoading(false)
      }
    }

    fetchPatients()
  }, [])

  const handlePatientClick = (patient) => {
    setSelectedPatient(patient)
    setShowModal(true)
  }

  const closeModal = () => {
    setShowModal(false)
    setSelectedPatient(null)
    setActiveTab("general")
  }

  // Hàm xử lý hoàn thành khám
  const handleCompleteExamination = (patientId) => {
    setPatients(prevPatients =>
        prevPatients.map(patient =>
            patient.id === patientId
                ? {
                  ...patient,
                  status: "Đã khám",
                  completedAt: new Date().toISOString()
                }
                : patient
        )
    )

    // Cập nhật selectedPatient nếu đang xem modal của bệnh nhân này
    if (selectedPatient && selectedPatient.id === patientId) {
      setSelectedPatient(prev => ({
        ...prev,
        status: "Đã khám",
        completedAt: new Date().toISOString()
      }))
    }

    // Tự động chuyển về tab "Đã khám" để người dùng thấy bệnh nhân vừa hoàn thành
    setStatusFilter("completed")

    // Đóng modal sau khi hoàn thành
    setTimeout(() => {
      closeModal()
    }, 1000)

    // Hiển thị thông báo thành công
    alert("✅ Đã hoàn thành khám bệnh nhân!")
  }

  // Hàm xử lý trở về trạng thái chưa khám
  const handleBackToWaiting = (patientId) => {
    const confirmBack = window.confirm("⚠️ Bạn có chắc muốn đưa bệnh nhân này trở lại trạng thái chờ khám?")

    if (confirmBack) {
      setPatients(prevPatients =>
          prevPatients.map(patient =>
              patient.id === patientId
                  ? {
                    ...patient,
                    status: "Chờ khám",
                    completedAt: null // Xóa thời gian hoàn thành
                  }
                  : patient
          )
      )

      // Cập nhật selectedPatient nếu đang xem modal của bệnh nhân này
      if (selectedPatient && selectedPatient.id === patientId) {
        setSelectedPatient(prev => ({
          ...prev,
          status: "Chờ khám",
          completedAt: null
        }))
      }

      // Tự động chuyển về tab "Chưa khám"
      setStatusFilter("all")

      // Đóng modal sau khi cập nhật
      setTimeout(() => {
        closeModal()
      }, 1000)

      // Hiển thị thông báo thành công
      alert("🔄 Đã đưa bệnh nhân trở lại trạng thái chờ khám!")
    }
  }

  const calculateAge = (dateOfBirth) => {
    if (!dateOfBirth) return "N/A"

    const today = new Date()
    const birthDate = new Date(dateOfBirth)
    let age = today.getFullYear() - birthDate.getFullYear()
    const monthDiff = today.getMonth() - birthDate.getMonth()
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--
    }
    return age
  }

  const getPriorityColor = (priority) => {
    switch (priority) {
      case "Khẩn cấp":
        return "#dc3545"
      case "Ưu tiên":
        return "#fd7e14"
      default:
        return "#28a745"
    }
  }

  // Lọc bệnh nhân theo trạng thái
  const filteredPatients = patients.filter(patient => {
    if (statusFilter === "all") return patient.status === "Chờ khám" // Chỉ hiển thị bệnh nhân chưa khám
    if (statusFilter === "waiting") return patient.status === "Chờ khám"
    if (statusFilter === "completed") return patient.status === "Đã khám"
    return true
  })

  // Loading state
  if (loading) {
    return (
        <LayoutLogin>

          <div className="doctor-page">
            <div className="patient-list-container">
              <div className="page-header">
                <h2>📋 Danh sách bệnh nhân</h2>
                <p>Đang tải dữ liệu...</p>
              </div>
              <div style={{ textAlign: "center", padding: "2rem" }}>
                <div>🔄 Đang tải danh sách bệnh nhân...</div>
              </div>
            </div>
          </div>
        </LayoutLogin>
    )
  }

  // Error state
  if (error) {
    return (
        <LayoutLogin>
          <div className="doctor-page">
            <div className="patient-list-container">
              <div className="page-header">
                <h2>📋 Danh sách bệnh nhân</h2>
                <p>Có lỗi xảy ra</p>
              </div>
              <div style={{ textAlign: "center", padding: "2rem", color: "red" }}>
                <div>❌ {error}</div>
                <button onClick={() => window.location.reload()} style={{ marginTop: "1rem", padding: "0.5rem 1rem" }}>
                  Thử lại
                </button>
              </div>
            </div>
          </div>
        </LayoutLogin>
    )
  }

  return (
      <LayoutLogin>
        <div className="doctor-page">
          <div className="patient-list-container">
            <div className="page-header">
              <h2>📋 Danh sách bệnh nhân</h2>
              <p>Xem thông tin chi tiết hồ sơ bệnh nhân do nhân viên tiếp nhận tạo</p>
            </div>

            <div className="patient-stats">
              <div className="stat-card">
                <div className="stat-label">Tổng bệnh nhân</div>
                <div className="stat-number">{patients.length}</div>
              </div>
              <div className="stat-card">
                <div className="stat-label">Có sinh hiệu</div>
                <div className="stat-number">
                  {patients.filter((p) => p.temperature || p.heartRate || p.bloodPressureSystolic).length}
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-label">Chờ khám</div>
                <div className="stat-number">{patients.filter((p) => p.status === "Chờ khám").length}</div>
              </div>
              <div className="stat-card">
                <div className="stat-label">Đã khám</div>
                <div className="stat-number">{patients.filter((p) => p.status === "Đã khám").length}</div>
              </div>
            </div>

            {/* Thêm bộ lọc trạng thái */}
            <div className="status-filter">
              <button
                  className={`filter-btn ${statusFilter === "all" ? "active" : ""}`}
                  onClick={() => setStatusFilter("all")}
              >
                🏥 Chưa khám ({patients.filter(p => p.status === "Chờ khám").length})
              </button>
              <button
                  className={`filter-btn ${statusFilter === "waiting" ? "active" : ""}`}
                  onClick={() => setStatusFilter("waiting")}
              >
                ⏳ Chờ khám ({patients.filter(p => p.status === "Chờ khám").length})
              </button>
              <button
                  className={`filter-btn ${statusFilter === "completed" ? "active" : ""}`}
                  onClick={() => setStatusFilter("completed")}
              >
                ✅ Đã khám ({patients.filter(p => p.status === "Đã khám").length})
              </button>
            </div>

            <div className="table-container">
              {filteredPatients.length === 0 ? (
                  <div style={{ textAlign: "center", padding: "2rem" }}>
                    <div>📝 Không có bệnh nhân nào {statusFilter === "all" ? "chưa khám" : statusFilter === "waiting" ? "chờ khám" : statusFilter === "completed" ? "đã khám" : "trong hệ thống"}</div>
                  </div>
              ) : (
                  <table className="patient-table">
                    <thead>
                    <tr>
                      <th>Mã BN</th>
                      <th>Họ và tên</th>
                      <th>Tuổi</th>
                      <th>Giới tính</th>
                      <th>Số điện thoại</th>
                      <th>Email</th>
                      <th>Sinh hiệu</th>
                      <th>Trạng thái</th>
                      {statusFilter === "completed" && <th>Hoàn thành</th>}
                    </tr>
                    </thead>
                    <tbody>
                    {filteredPatients.map((patient) => (
                        <tr key={patient.id} onClick={() => handlePatientClick(patient)} className="patient-row">
                          <td>{patient.patientCode}</td>
                          <td className="patient-name">{patient.fullName}</td>
                          <td>{calculateAge(patient.dateOfBirth)}</td>
                          <td>{patient.gender}</td>
                          <td>{patient.phone}</td>
                          <td>{patient.email}</td>
                          <td>
                            {patient.temperature || patient.heartRate || patient.bloodPressureSystolic ? (
                                <span className="vital-indicator">✅ Có</span>
                            ) : (
                                <span className="vital-indicator-none">❌ Chưa có</span>
                            )}
                          </td>
                          <td>
                            <span className={`status-badge ${patient.status === "Đã khám" ? "completed" : ""}`}>
                              {patient.status}
                            </span>
                          </td>
                          {statusFilter === "completed" && (
                              <td>
                                {patient.completedAt && new Date(patient.completedAt).toLocaleString("vi-VN")}
                              </td>
                          )}
                        </tr>
                    ))}
                    </tbody>
                  </table>
              )}
            </div>
          </div>

          {showModal && selectedPatient && (
              <div className="modal-overlay" onClick={closeModal}>
                <div className="medical-record-modal" onClick={(e) => e.stopPropagation()}>
                  <div className="modal-header">
                    <button className="close-btn" onClick={closeModal}>
                      ×
                    </button>
                    <h3>🏥 HỒ SƠ BỆNH ÁN</h3>
                    <p>Mã bệnh nhân: {selectedPatient.patientCode}</p>
                    <div className="patient-status-header">
                      <span className={`status-badge-large ${selectedPatient.status === "Đã khám" ? "completed" : ""}`}>
                        {selectedPatient.status === "Đã khám" ? "✅ Đã khám xong" : "⏳ Chờ khám"}
                      </span>
                    </div>
                  </div>

                  <div className="modal-tabs">
                    <div
                        className={`tab-item ${activeTab === "general" ? "active" : ""}`}
                        onClick={() => setActiveTab("general")}
                    >
                      <div className="tab-icon">👤</div>
                      <span>Thông tin chung</span>
                    </div>
                    <div
                        className={`tab-item ${activeTab === "medical" ? "active" : ""}`}
                        onClick={() => setActiveTab("medical")}
                    >
                      <div className="tab-icon">🩺</div>
                      <span>Thông tin y tế</span>
                    </div>
                    {/* Hiển thị tab sinh hiệu nếu có dữ liệu */}
                    {(selectedPatient.temperature ||
                        selectedPatient.heartRate ||
                        selectedPatient.bloodPressureSystolic ||
                        selectedPatient.weight) && (
                        <div
                            className={`tab-item ${activeTab === "vital" ? "active" : ""}`}
                            onClick={() => setActiveTab("vital")}
                        >
                          <div className="tab-icon">📊</div>
                          <span>Sinh hiệu</span>
                        </div>
                    )}
                  </div>

                  <div className="modal-content">
                    {activeTab === "general" && (
                        <div className="medical-info-section">
                          <h4>📋 THÔNG TIN BỆNH NHÂN</h4>

                          <div className="info-grid">
                            <div className="info-row">
                              <span className="info-label">Họ và tên:</span>
                              <span className="info-value">{selectedPatient.fullName}</span>
                            </div>

                            <div className="info-row">
                              <span className="info-label">Ngày sinh:</span>
                              <span className="info-value">{selectedPatient.dateOfBirth}</span>
                            </div>

                            <div className="info-row">
                              <span className="info-label">Tuổi:</span>
                              <span className="info-value">{calculateAge(selectedPatient.dateOfBirth)} tuổi</span>
                            </div>

                            <div className="info-row">
                              <span className="info-label">Giới tính:</span>
                              <span className="info-value">{selectedPatient.gender}</span>
                            </div>

                            <div className="info-row">
                              <span className="info-label">Số điện thoại:</span>
                              <span className="info-value">{selectedPatient.phone}</span>
                            </div>

                            <div className="info-row">
                              <span className="info-label">Email:</span>
                              <span className="info-value">{selectedPatient.email}</span>
                            </div>

                            <div className="info-row">
                              <span className="info-label">Địa chỉ:</span>
                              <span className="info-value">{selectedPatient.address}</span>
                            </div>

                            <div className="info-row">
                              <span className="info-label">CMND/CCCD:</span>
                              <span className="info-value">{selectedPatient.identityNumber}</span>
                            </div>

                            <div className="info-row">
                              <span className="info-label">Số BHYT:</span>
                              <span className="info-value">{selectedPatient.insuranceNumber}</span>
                            </div>

                            <div className="info-row">
                              <span className="info-label">Nhóm máu:</span>
                              <span className="info-value">{selectedPatient.bloodType}</span>
                            </div>
                          </div>
                        </div>
                    )}

                    {activeTab === "medical" && (
                        <div className="medical-info-section">
                          <h4>🩺 THÔNG TIN Y TẾ</h4>

                          <div className="info-grid">
                            <div className="info-row">
                              <span className="info-label">Triệu chứng chính:</span>
                              <span className="info-value">{selectedPatient.chiefComplaint}</span>
                            </div>

                            <div className="info-row">
                              <span className="info-label">Tiền sử bệnh:</span>
                              <span className="info-value">{selectedPatient.medicalHistory}</span>
                            </div>

                            <div className="info-row">
                              <span className="info-label">Dị ứng:</span>
                              <span className="info-value">{selectedPatient.allergies}</span>
                            </div>

                            <div className="info-row">
                              <span className="info-label">Chuyên khoa:</span>
                              <span className="info-value">{selectedPatient.specialty}</span>
                            </div>

                            <div className="info-row">
                              <span className="info-label">Trạng thái:</span>
                              <span className="info-value">
                                <span className={`status-badge ${selectedPatient.status === "Đã khám" ? "completed" : ""}`}>
                                  {selectedPatient.status}
                                </span>
                              </span>
                            </div>

                            <div className="info-row">
                              <span className="info-label">Ngày khám:</span>
                              <span className="info-value">{selectedPatient.visitDate}</span>
                            </div>

                            {selectedPatient.completedAt && (
                                <div className="info-row">
                                  <span className="info-label">Hoàn thành khám:</span>
                                  <span className="info-value">
                                  {new Date(selectedPatient.completedAt).toLocaleString("vi-VN")}
                                </span>
                                </div>
                            )}
                          </div>
                        </div>
                    )}

                    {activeTab === "vital" && (
                        <div className="medical-info-section">
                          <h4>📊 SINH HIỆU (do nhân viên tiếp nhận ghi nhận)</h4>

                          <div className="vital-signs-grid">
                            {selectedPatient.temperature && (
                                <div className="vital-card">
                                  <div className="vital-icon">🌡️</div>
                                  <div className="vital-value">{selectedPatient.temperature}°C</div>
                                  <div className="vital-label">Nhiệt độ</div>
                                </div>
                            )}

                            {selectedPatient.heartRate && (
                                <div className="vital-card">
                                  <div className="vital-icon">💓</div>
                                  <div className="vital-value">{selectedPatient.heartRate}</div>
                                  <div className="vital-label">Nhịp tim (lần/phút)</div>
                                </div>
                            )}

                            {selectedPatient.bloodPressureSystolic && selectedPatient.bloodPressureDiastolic && (
                                <div className="vital-card">
                                  <div className="vital-icon">🩸</div>
                                  <div className="vital-value">
                                    {selectedPatient.bloodPressureSystolic}/{selectedPatient.bloodPressureDiastolic}
                                  </div>
                                  <div className="vital-label">Huyết áp (mmHg)</div>
                                </div>
                            )}

                            {selectedPatient.respiratoryRate && (
                                <div className="vital-card">
                                  <div className="vital-icon">🫁</div>
                                  <div className="vital-value">{selectedPatient.respiratoryRate}</div>
                                  <div className="vital-label">Nhịp thở (lần/phút)</div>
                                </div>
                            )}

                            {selectedPatient.oxygenSaturation && (
                                <div className="vital-card">
                                  <div className="vital-icon">🔵</div>
                                  <div className="vital-value">{selectedPatient.oxygenSaturation}%</div>
                                  <div className="vital-label">SpO2</div>
                                </div>
                            )}

                            {selectedPatient.weight && (
                                <div className="vital-card">
                                  <div className="vital-icon">⚖️</div>
                                  <div className="vital-value">{selectedPatient.weight} kg</div>
                                  <div className="vital-label">Cân nặng</div>
                                </div>
                            )}

                            {selectedPatient.height && (
                                <div className="vital-card">
                                  <div className="vital-icon">📏</div>
                                  <div className="vital-value">{selectedPatient.height} cm</div>
                                  <div className="vital-label">Chiều cao</div>
                                </div>
                            )}
                          </div>
                        </div>
                    )}

                    <div className="modal-actions">
                      <Link to={`/CreateImagingRequest?patientId=${selectedPatient.id}`}>
                        <button className="btn btn-primary">📝 Tạo yêu cầu chụp</button>
                      </Link>
                      <Link to={`/SymptomDisplay?patientId=${selectedPatient.id}`}>
                        <button className="btn btn-secondary">📄 Xem triệu chứng</button>
                      </Link>
                      <Link to={`/MedicalReportForm?patientId=${selectedPatient.id}`}>
                        <button className="btn btn-secondary">📄 Tạo báo cáo</button>
                      </Link>

                      {/* Hiển thị nút tùy theo trạng thái */}
                      {selectedPatient.status === "Đã khám" ? (
                          <div className="status-actions">
                          <span className="completed-badge">
                            ✅ Đã hoàn thành khám
                          </span>
                            <button
                                className="btn btn-warning"
                                onClick={() => handleBackToWaiting(selectedPatient.id)}
                            >
                              🔄 Trở về chờ khám
                            </button>
                          </div>
                      ) : (
                          <button
                              className="btn btn-success"
                              onClick={() => handleCompleteExamination(selectedPatient.id)}
                          >
                            ✅ Hoàn thành khám
                          </button>
                      )}
                    </div>
                  </div>
                </div>
              </div>
          )}
        </div>
      </LayoutLogin>
  )
}

export default memo(PatientProfile)