"use client"
import { memo, useState, useEffect } from "react"
import { Link, useLocation } from "react-router-dom"
import LayoutLogin from "../Layout/LayoutLogin"
import HeaderLogin from "../Layout/HeaderLogin"
import { symptomService } from "../../services/symptomService"
import { patientService } from "../../services/patientService"
import MiniChatbot from "../Doctor/MiniChatBot"
import "../../css/PatientProfile.css"
import "../../css/SymptomDisplay.css"

const SymptomDisplayLayout = () => {
    const [selectedPatient, setSelectedPatient] = useState(null)
    const location = useLocation()
    const [patientData, setPatientData] = useState(null)
    const [symptomsData, setSymptomsData] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)
    const [debugInfo, setDebugInfo] = useState({})

    // Lấy patientId từ URL params
    const getPatientIdFromUrl = () => {
        const urlParams = new URLSearchParams(location.search)
        return urlParams.get('patientId')
    }

    // Test connection trước khi fetch data
    const testApiConnection = async () => {
        try {
            console.log("Testing API connections...")

            // Test symptom API
            const symptomTest = await symptomService.testConnection()
            console.log("Symptom API test successful:", symptomTest)

            // Test patient endpoints
            await patientService.testPatientEndpoints()

            setDebugInfo(prev => ({ ...prev, apiTest: "success", symptomTest }))
            return true
        } catch (error) {
            console.error("API connection test failed:", error)
            setDebugInfo(prev => ({ ...prev, apiTest: "failed", apiError: error.message }))
            return false
        }
    }

    // Fetch patient data và symptoms data
    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true)
                const patientId = getPatientIdFromUrl()

                // Lấy danh sách bệnh nhân
                const patients = await patientService.getAllPatients()
                const patient = patients.find(p => String(p.patient_id) === String(patientId))
                setPatientData(patient)
                setSelectedPatient(patient)

                // Lấy triệu chứng
                let symptoms = []
                if (patientId) {
                    symptoms = await symptomService.getSymptomsByPatientId(patientId)
                } else {
                    symptoms = await symptomService.getAllSymptoms()
                }
                setSymptomsData(symptoms)
            } catch (err) {
                setSymptomsData([])
            } finally {
                setLoading(false)
            }
        }
        fetchData()
    }, [location.search])

    const formatDate = (dateString) => {
        if (!dateString) return "N/A"
        const date = new Date(dateString)
        return date.toLocaleDateString('vi-VN')
    }

    // Loading state
    if (loading) {
        return (
            <>
                <HeaderLogin />
                <LayoutLogin>
                    <div className="doctor-page">
                        <div className="patient-list-container">
                            <div className="symptom-display-container">
                                <div style={{ textAlign: "center", padding: "2rem" }}>
                                    <div>🔄 Đang tải thông tin triệu chứng...</div>
                                    <div style={{ marginTop: "20px", fontSize: "14px", color: "#666" }}>
                                        <div>Patient ID: {getPatientIdFromUrl()}</div>
                                        <div>API Test: {debugInfo.apiTest || "pending"}</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <MiniChatbot />
                </LayoutLogin>
            </>
        )
    }

    // No data state
    if (!patientData) {
        return (
            <>
                <HeaderLogin />
                <LayoutLogin>
                    <div className="doctor-page">
                        <div className="patient-list-container">
                            <div className="symptom-display-container">
                                <div style={{ textAlign: "center", padding: "2rem" }}>
                                    <div>📝 Không tìm thấy thông tin bệnh nhân</div>
                                    <div style={{ marginTop: "15px", fontSize: "14px", color: "#666" }}>
                                        Debug: {JSON.stringify(debugInfo, null, 2)}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <MiniChatbot />
                </LayoutLogin>
            </>
        )
    }

    return (
        <>
            <HeaderLogin />
            <LayoutLogin>
                <button
                    onClick={() => window.history.back()}
                    style={{
                        position: 'fixed',
                        top: '20px',
                        left: '20px',
                        zIndex: 99999,
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px',
                        padding: '10px 16px',
                        background: '#fff',
                        border: '2px solid #007bff',
                        borderRadius: '8px',
                        color: '#007bff',
                        fontSize: '14px',
                        fontWeight: '500',
                        cursor: 'pointer',
                        fontFamily: 'inherit'
                    }}
                    title="Quay lại"
                >
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M19 12H5M12 19l-7-7 7-7"/>
                    </svg>
                    <span>Quay lại</span>
                </button>
                <div className="doctor-page">
                    <div className="patient-list-container">
                        <div className="symptom-display-container">
                            {/* Thông tin bệnh nhân */}
                            <div className="symptom-patient-info" style={{marginBottom: '2rem'}}>
                                <h2 style={{marginBottom: '1rem'}}>🩺 Thông tin bệnh nhân</h2>
                                <div style={{color:'#888', marginBottom:'1rem'}}>
                                    Ngày khám: {new Date().toLocaleDateString('vi-VN')} - {new Date().toLocaleTimeString('vi-VN', {hour: '2-digit', minute: '2-digit'})}
                                </div>
                                <div style={{display:'flex', gap:'2rem', background:'#f8f9fa', borderRadius:'10px', padding:'1.5rem 2rem', marginBottom:'1.5rem'}}>
                                    <div style={{flex:1}}>
                                        <div><b>Mã bệnh nhân:</b> {patientData?.patient_code || 'N/A'}</div>
                                        <div><b>Tuổi/Giới tính:</b> {patientData?.age || 'N/A'} tuổi - {patientData?.gender || 'N/A'}</div>
                                    </div>
                                    <div style={{flex:1}}>
                                        <div><b>Họ và tên:</b> {patientData?.full_name || 'N/A'}</div>
                                        <div><b>Số điện thoại:</b> {patientData?.phone || 'N/A'}</div>
                                    </div>
                                </div>
                            </div>

                            {/* Hiển thị triệu chứng từ database */}
                            <div className="symptoms-section">
                                <h3>Lịch sử triệu chứng ({symptomsData.length} bản ghi)</h3>
                                <table className="records-table">
                                    <thead>
                                    <tr>
                                        <th>Mã BN</th>
                                        <th>Họ tên</th>
                                        <th>Tuổi</th>
                                        <th>Triệu chứng chính</th>
                                        <th>Chi tiết triệu chứng</th>
                                        <th>Khác</th>
                                        <th>Ngày ghi nhận</th>
                                        <th>Trạng thái</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {symptomsData.length > 0 ? (
                                        symptomsData.map((s, idx) => {
                                            // Tách thông tin nhân viên ghi nhận khỏi other_symptoms
                                            let other = s.other_symptoms || '';
                                            let nhanVien = '';
                                            const lines = other.split('\n');
                                            const filtered = lines.filter(line => {
                                                if (line.trim().toLowerCase().startsWith('recorded by')) {
                                                    nhanVien = line.replace('Recorded By:', '').trim();
                                                    return false;
                                                }
                                                return true;
                                            });
                                            return (
                                                <tr key={s.id || idx}>
                                                    <td>{s.patient_code || 'N/A'}</td>
                                                    <td>{s.patient_name || 'Không xác định'}</td>
                                                    <td>{s.patient_age || 'N/A'}</td>
                                                    <td>{s.main_symptom || 'Không có thông tin'}</td>
                                                    <td>
                                                        {s.detailed_symptoms ?
                                                            s.detailed_symptoms.split('\n').map((line, i) =>
                                                                <div key={i}>
                                                                    {line.replace('Severity:', 'Mức độ:')
                                                                        .replace('Onset:', 'Khởi phát:')
                                                                        .replace('Duration:', 'Thời gian:')
                                                                        .replace('Pain Scale:', 'Thang điểm đau:')}
                                                                </div>
                                                            ) : 'Không có chi tiết'
                                                        }
                                                    </td>
                                                    <td>
                                                        {filtered.length > 0 ?
                                                            filtered.map((line, i) =>
                                                                <div key={i}>
                                                                    {line.replace('Priority:', 'Ưu tiên')
                                                                        .replace('Additional Notes:', 'Ghi chú')}
                                                                </div>
                                                            ) : 'Không có ghi chú'
                                                        }
                                                    </td>
                                                    <td>{s.created_at ? new Date(s.created_at).toLocaleString('vi-VN') : 'Chưa xác định'}</td>
                                                    <td>Đã ghi nhận</td>
                                                </tr>
                                            )
                                        })
                                    ) : (
                                        <tr><td colSpan={8}>Chưa có ghi nhận triệu chứng nào.</td></tr>
                                    )}
                                    </tbody>
                                </table>

                                {/* Hiển thị nhân viên tiếp nhận nếu có */}
                                {symptomsData.length > 0 && (() => {
                                    // Lấy nhân viên ghi nhận mới nhất
                                    let nhanVien = '';
                                    const other = symptomsData[symptomsData.length-1].other_symptoms || '';
                                    other.split('\n').forEach(line => {
                                        if (line.trim().toLowerCase().startsWith('recorded by')) {
                                            nhanVien = line.replace('Recorded By:', '').trim();
                                        }
                                    });
                                    return nhanVien ? (
                                        <div style={{marginTop:'10px', fontStyle:'italic', color:'#444'}}>
                                            Nhân viên tiếp nhận: <b>{nhanVien}</b>
                                        </div>
                                    ) : null;
                                })()}
                            </div>

                            {/* Tóm tắt */}
                            <div className="symptoms-summary">
                                <h3 className="summary-title">
                                    📊 Tóm tắt thông tin
                                </h3>
                                <div className="summary-grid">
                                    <div className="summary-item">
                                        <span className="summary-label">Số bản ghi triệu chứng:</span>
                                        <div className="summary-value">{symptomsData.length}</div>
                                    </div>
                                    <div className="summary-item">
                                        <span className="summary-label">Bản ghi mới nhất:</span>
                                        <div className="summary-value">
                                            {symptomsData.length > 0
                                                ? (symptomsData[symptomsData.length - 1]?.created_at ?
                                                    formatDate(symptomsData[symptomsData.length - 1]?.created_at) : 'Chưa xác định')
                                                : "Chưa có"
                                            }
                                        </div>
                                    </div>
                                    <div className="summary-item">
                                        <span className="summary-label">Trạng thái:</span>
                                        <div className="summary-value">
                                            {symptomsData.length > 0 ? "Có dữ liệu" : "Chưa có dữ liệu"}
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {/* Nút hành động */}
                            <div className="action-buttons">
                                <button
                                    className="btn btn-secondary"
                                    onClick={() => window.history.back()}
                                >
                                    ← Quay lại
                                </button>

                                <Link to={`/MedicalReportForm?patientId=${selectedPatient?.patient_id}`}>
                                    <button className="btn btn-secondary">📄 Tạo báo cáo</button>
                                </Link>
                            </div>

                            {/* Debug section */}
                            {debugInfo && Object.keys(debugInfo).length > 0 && (
                                <div style={{
                                    marginTop: "20px",
                                    padding: "15px",
                                    background: "#f8f9fa",
                                    borderRadius: "8px",
                                    fontSize: "12px"
                                }}>
                                    <details>
                                        <summary>Debug Information</summary>
                                        <pre>{JSON.stringify(debugInfo, null, 2)}</pre>
                                    </details>
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                {/* Mini Chatbot - Luôn hiển thị */}
                <MiniChatbot />
            </LayoutLogin>
        </>
    )
}
export default memo(SymptomDisplayLayout)
