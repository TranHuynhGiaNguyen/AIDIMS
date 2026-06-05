import { memo, useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import LayoutLogin from "../Layout/LayoutLogin";
import "../../css/MedicalReportForm.css";
import diagnosticReportService from '../../services/diagnosticReportService';
import { patientService } from "../../services/patientService";

const MedicalReportForm = () => {
    const [searchParams] = useSearchParams();

    const [formData, setFormData] = useState({
        // Nhận dạng bệnh nhân
        firstName: '',
        lastName: '',
        dateOfBirth: '',
        gender: '',
        address: '',

        // Bác sĩ giới thiệu
        referringDoctor: '',
        doctorSpecialty: '',

        // Lịch sử lâm sàng
        clinicalHistory: '',
        symptoms: [],

        // Chẩn đoán
        diagnosis: '',
        findings: '',
        recommendations: ''
    });

    const [loading, setLoading] = useState(false);
    const [patientLoading, setPatientLoading] = useState(false);
    const [message, setMessage] = useState({ type: '', text: '' });
    const [reportCode, setReportCode] = useState('BC20241211001');
    const [showReportsList, setShowReportsList] = useState(false);
    const [selectedPatientInfo, setSelectedPatientInfo] = useState(null);

    // New states for reports list
    const [reportsLoading, setReportsLoading] = useState(false);
    const [reportsList, setReportsList] = useState([]);

    // BƯỚC QUAN TRỌNG: Auto-fill khi component load
    useEffect(() => {
        const patientId = searchParams.get('patientId');
        console.log("URL patientId:", patientId);

        if (patientId) {
            loadPatientData(patientId);
        }

        generateNewReportCode();
    }, [searchParams]);

    const loadPatientData = async (patientId) => {
        try {
            setPatientLoading(true);

            // Fetch patient data from API
            const patientData = await patientService.getPatientById(patientId);

            if (patientData) {
                fillPatientData(patientData);
                setSelectedPatientInfo(patientData);
                setMessage({
                    type: 'success',
                    text: `Đã tự động điền thông tin bệnh nhân: ${patientData.full_name}`
                });
            }
        } catch (error) {
            console.error('Error loading patient data:', error);
            setMessage({
                type: 'error',
                text: 'Không thể tải thông tin bệnh nhân. Vui lòng nhập thủ công.'
            });
        } finally {
            setPatientLoading(false);
        }
    };

    const fillPatientData = (patientData) => {
        // Split full name into first and last name
        const fullName = patientData.full_name || patientData.fullName || '';
        const nameParts = fullName.trim().split(' ');
        const firstName = nameParts.pop() || ''; // Last word as first name
        const lastName = nameParts.join(' ') || ''; // Rest as last name

        setFormData(prev => ({
            ...prev,
            firstName: firstName,
            lastName: lastName,
            dateOfBirth: patientData.date_of_birth || patientData.dateOfBirth || '',
            gender: patientData.gender || '',
            address: patientData.address || '',
            // You can also pre-fill other fields if available
            clinicalHistory: patientData.medical_history || patientData.medicalHistory || ''
        }));
    };

    const generateNewReportCode = async () => {
        try {
            console.log("🔢 Generating new report code...");
            const response = await diagnosticReportService.generateReportCode();
            console.log("🔢 Generate code response:", response);

            // Kiểm tra response structure từ DiagnosticReportController
            if (response && response.data) {
                // API trả về { success: true, message: "...", data: "BC20250624001" }
                setReportCode(response.data);
                console.log("✅ Generated report code from API:", response.data);
            } else if (response && typeof response === 'string') {
                // Trường hợp trả về trực tiếp string
                setReportCode(response);
                console.log("✅ Generated report code (direct):", response);
            } else {
                // Fallback nếu API response không đúng format
                throw new Error("Invalid API response format");
            }
        } catch (error) {
            console.error('❌ Error generating report code from API:', error);
            console.log("🔄 Using fallback method...");

            // Fallback: tạo mã báo cáo ngẫu nhiên
            const today = new Date();
            const dateStr = today.toISOString().slice(0, 10).replace(/-/g, ''); // YYYYMMDD
            const randomNum = String(Math.floor(Math.random() * 1000)).padStart(3, '0');
            const fallbackCode = `BC${dateStr}${randomNum}`;

            setReportCode(fallbackCode);
            console.log("✅ Generated fallback report code:", fallbackCode);

            setMessage({
                type: 'warning',
                text: `⚠️ Sử dụng mã báo cáo tạm thời: ${fallbackCode}`
            });
        }
    };

    // New function to load all reports
    const loadReportsList = async () => {
        try {
            setReportsLoading(true);
            const response = await diagnosticReportService.getAllReports();

            // Kiểm tra response structure - có thể là response.data hoặc trực tiếp response
            const reportsData = response?.data || response;

            if (Array.isArray(reportsData)) {
                setReportsList(reportsData);
                console.log("✅ Loaded reports list:", reportsData);
            } else {
                console.log("📝 Response structure:", response);
                throw new Error("Invalid reports response format");
            }
        } catch (error) {
            console.error('❌ Error loading reports list:', error);
            setMessage({
                type: 'error',
                text: `Không thể tải danh sách báo cáo: ${error.message}`
            });
        } finally {
            setReportsLoading(false);
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleCheckboxChange = (e) => {
        const { name, value, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: checked
                ? [...prev[name], value]
                : prev[name].filter(item => item !== value)
        }));
    };

    const handleCreateReport = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage({ type: '', text: '' });

        try {
            // Validate required fields - SỬA: cho phép lastName trống nếu firstName có
            if (!formData.firstName || (!formData.lastName && formData.firstName.length < 2)) {
                throw new Error('Vui lòng nhập đầy đủ họ tên');
            }

            if (!formData.diagnosis) {
                throw new Error('Vui lòng nhập kết quả chẩn đoán');
            }

            // Sử dụng transformFormDataToApi() có sẵn
            const apiData = diagnosticReportService.transformFormDataToApi(formData);
            apiData.reportCode = reportCode;

            console.log('🚀 Sending data to API:', apiData);

            // Sử dụng createReport() có sẵn
            const response = await diagnosticReportService.createReport(apiData);

            if (response) {
                setMessage({ type: 'success', text: `Báo cáo ${reportCode} đã được lưu thành công vào cơ sở dữ liệu!` });

                setTimeout(() => {
                    resetForm();
                    generateNewReportCode();
                }, 3000);
            } else {
                throw new Error('Có lỗi xảy ra khi lưu báo cáo');
            }

        } catch (error) {
            console.error('❌ Submit error:', error);
            setMessage({ type: 'error', text: `Lỗi: ${error.message}` });
        } finally {
            setLoading(false);
        }
    };

    const handleViewReports = async () => {
        if (!showReportsList) {
            await loadReportsList();
        }
        setShowReportsList(!showReportsList);
    };

    const resetForm = () => {
        // Don't reset patient info if it was auto-filled
        const patientId = searchParams.get('patientId');

        if (patientId && selectedPatientInfo) {
            // Keep patient info, only reset medical fields
            setFormData(prev => ({
                ...prev,
                referringDoctor: '',
                doctorSpecialty: '',
                symptoms: [],
                diagnosis: '',
                findings: '',
                recommendations: ''
            }));
        } else {
            // Reset everything
            setFormData({
                firstName: '',
                lastName: '',
                dateOfBirth: '',
                gender: '',
                address: '',
                referringDoctor: '',
                doctorSpecialty: '',
                clinicalHistory: '',
                symptoms: [],
                diagnosis: '',
                findings: '',
                recommendations: ''
            });
        }

        setMessage({ type: '', text: '' });
    };

    // Function to format date
    const formatDate = (dateString) => {
        try {
            return new Date(dateString).toLocaleDateString('vi-VN');
        } catch {
            return dateString;
        }
    };

    // Function to get status badge style
    const getStatusBadgeClass = (status) => {
        switch (status) {
            case 'Hoàn thành':
                return 'status-badge status-completed';
            case 'Bản nháp':
                return 'status-badge status-draft';
            default:
                return 'status-badge status-draft'; // Default to draft styling
        }
    };

    // Function to handle report download
    const handleDownloadReport = (report) => {
        try {
            console.log('📄 Downloading report:', report.reportCode);

            // Generate PDF content
            const pdfContent = generateReportPDF(report);

            // Create blob and download
            const blob = new Blob([pdfContent], { type: 'text/html' });
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = `BaoCao_${report.reportCode}_${formatDate(report.createdAt).replace(/\//g, '')}.html`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);

            setMessage({
                type: 'success',
                text: `✅ Đã tải báo cáo ${report.reportCode}`
            });

            // Clear message after 3 seconds
            setTimeout(() => {
                setMessage({ type: '', text: '' });
            }, 3000);

        } catch (error) {
            console.error('❌ Error downloading report:', error);
            setMessage({
                type: 'error',
                text: `❌ Lỗi khi tải báo cáo: ${error.message}`
            });
        }
    };

    // Function to handle report printing
    const handlePrintReport = (report) => {
        try {
            console.log('🖨️ Printing report:', report.reportCode);

            // Generate PDF content with print-optimized CSS
            const printContent = generateReportPDF(report, true);

            // Create new window for printing
            const printWindow = window.open('', '_blank', 'width=900,height=700,scrollbars=yes');
            printWindow.document.write(printContent);
            printWindow.document.close();

            // Show instructions and auto-print
            printWindow.onload = () => {
                setTimeout(() => {
                    printWindow.focus();

                    // Show alert with instructions
                    printWindow.alert(`
🖨️ HƯỚNG DẪN IN BÁO CÁO

📋 Báo cáo: ${report.reportCode}

🔧 CÁCH 1 - TỰ ĐỘNG:
• Hộp thoại Print sẽ mở trong 3 giây
• Chọn máy in hoặc "Save as PDF"
• Click "Print"

🔧 CÁCH 2 - THỦ CÔNG:
• Nhấn Ctrl+P (Windows) hoặc Cmd+P (Mac)
• Chọn cài đặt in phù hợp
• Click "Print"

⚙️ CÀI ĐẶT GỢI Ý:
• Paper: A4
• Layout: Portrait (dọc)
• Margins: Normal
• Background graphics: Bật
                    `);

                    // Auto trigger print dialog after 3 seconds
                    setTimeout(() => {
                        printWindow.print();
                    }, 3000);

                    // Close window after printing (optional)
                    printWindow.onafterprint = () => {
                        const shouldClose = printWindow.confirm('Đã in xong? Click OK để đóng cửa sổ.');
                        if (shouldClose) {
                            printWindow.close();
                        }
                    };
                }, 1000);
            };

            setMessage({
                type: 'success',
                text: `🖨️ Đang mở báo cáo ${report.reportCode} để in. Làm theo hướng dẫn trong cửa sổ mới!`
            });

            // Clear message after 5 seconds
            setTimeout(() => {
                setMessage({ type: '', text: '' });
            }, 5000);

        } catch (error) {
            console.error('❌ Error printing report:', error);
            setMessage({
                type: 'error',
                text: `❌ Lỗi khi in báo cáo: ${error.message}`
            });
        }
    };

    // Function to generate PDF-ready HTML content
    const generateReportPDF = (report, forPrint = false) => {
        // Extract patient info from impression
        const impressionLines = report.impression ? report.impression.split('\n') : [];
        const patientName = impressionLines.find(line => line.startsWith('Bệnh nhân:'))?.replace('Bệnh nhân: ', '') || 'Không có thông tin';
        const dateOfBirth = impressionLines.find(line => line.startsWith('Ngày sinh:'))?.replace('Ngày sinh: ', '') || '';
        const gender = impressionLines.find(line => line.startsWith('Giới tính:'))?.replace('Giới tính: ', '') || '';
        const address = impressionLines.find(line => line.startsWith('Địa chỉ:'))?.replace('Địa chỉ: ', '') || '';
        const clinicalHistory = impressionLines.find(line => line.startsWith('Lịch sử lâm sàng:'))?.replace('Lịch sử lâm sàng: ', '') || '';

        // Additional CSS for print optimization
        const printCSS = forPrint ? `
        @media print {
            body { 
                margin: 0;
                padding: 20px;
                font-size: 12pt;
                line-height: 1.4;
            }
            .header {
                break-inside: avoid;
                page-break-after: avoid;
            }
            .section {
                break-inside: avoid;
                page-break-inside: avoid;
                margin: 15px 0;
            }
            .footer {
                position: fixed;
                bottom: 0;
                width: 100%;
                page-break-inside: avoid;
            }
            .report-code {
                position: fixed;
                top: 10px;
                right: 10px;
            }
            .no-print {
                display: none !important;
            }
        }
        @page {
            size: A4;
            margin: 1cm;
        }
        ` : '';

        return `
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo cáo chẩn đoán - ${report.reportCode}</title>
    <style>
        body {
            font-family: 'Times New Roman', serif;
            line-height: 1.6;
            margin: 40px;
            color: #333;
        }
        .header {
            text-align: center;
            border-bottom: 3px solid #2c3e50;
            padding-bottom: 20px;
            margin-bottom: 30px;
        }
        .hospital-name {
            font-size: 24px;
            font-weight: bold;
            color: #2c3e50;
            margin-bottom: 5px;
        }
        .department {
            font-size: 16px;
            color: #7f8c8d;
            margin-bottom: 20px;
        }
        .report-title {
            font-size: 20px;
            font-weight: bold;
            color: #e74c3c;
            text-transform: uppercase;
        }
        .section {
            margin: 25px 0;
            padding: 15px;
            border-left: 4px solid #3498db;
            background-color: #f8f9fa;
        }
        .section-title {
            font-size: 16px;
            font-weight: bold;
            color: #2c3e50;
            margin-bottom: 10px;
            text-transform: uppercase;
        }
        .info-row {
            display: flex;
            margin: 8px 0;
        }
        .info-label {
            font-weight: bold;
            min-width: 150px;
            color: #34495e;
        }
        .info-value {
            flex: 1;
            color: #2c3e50;
        }
        .findings-section {
            background-color: #fff3cd;
            border-left-color: #ffc107;
        }
        .recommendations-section {
            background-color: #d4edda;
            border-left-color: #28a745;
        }
        .footer {
            margin-top: 50px;
            padding-top: 20px;
            border-top: 2px solid #ecf0f1;
            display: flex;
            justify-content: space-between;
        }
        .signature-box {
            text-align: center;
            min-width: 200px;
        }
        .signature-title {
            font-weight: bold;
            margin-bottom: 60px;
        }
        .signature-name {
            border-top: 1px solid #333;
            padding-top: 5px;
        }
        .report-code {
            position: absolute;
            top: 20px;
            right: 20px;
            background: #e74c3c;
            color: white;
            padding: 8px 15px;
            border-radius: 5px;
            font-weight: bold;
        }
        
        /* Print instructions for user */
        .print-instructions {
            background: #e3f2fd;
            border: 1px solid #2196f3;
            border-radius: 5px;
            padding: 15px;
            margin: 20px 0;
            text-align: center;
        }
        
        ${printCSS}
    </style>
</head>
<body>
    <div class="report-code">${report.reportCode}</div>
    
    ${forPrint ? `
    <div class="print-instructions no-print">
        <h3>🖨️ Hướng dẫn in báo cáo</h3>
        <p><strong>Bước 1:</strong> Nhấn Ctrl+P (Windows/Linux) hoặc Cmd+P (Mac)</p>
        <p><strong>Bước 2:</strong> Chọn máy in và các tùy chọn in</p>
        <p><strong>Bước 3:</strong> Nhấn "In" để in báo cáo</p>
        <p><em>Trang này sẽ tự động đóng sau khi in xong</em></p>
    </div>
    ` : ''}
    
    <div class="header">
        <div class="hospital-name">BỆNH VIỆN ĐA KHOA AIDIMS</div>
        <div class="department">Khoa Chẩn đoán Hình ảnh</div>
        <div class="report-title">Báo cáo Chẩn đoán Hình ảnh</div>
    </div>

    <div class="section">
        <div class="section-title">🏥 Thông tin Bệnh nhân</div>
        <div class="info-row">
            <div class="info-label">Họ và tên:</div>
            <div class="info-value">${patientName}</div>
        </div>
        <div class="info-row">
            <div class="info-label">Ngày sinh:</div>
            <div class="info-value">${dateOfBirth}</div>
        </div>
        <div class="info-row">
            <div class="info-label">Giới tính:</div>
            <div class="info-value">${gender}</div>
        </div>
        <div class="info-row">
            <div class="info-label">Địa chỉ:</div>
            <div class="info-value">${address}</div>
        </div>
    </div>

    <div class="section">
        <div class="section-title">👨‍⚕️ Thông tin Bác sĩ</div>
        <div class="info-row">
            <div class="info-label">Bác sĩ giới thiệu:</div>
            <div class="info-value">${report.referringDoctorName || 'Không có thông tin'}</div>
        </div>
        <div class="info-row">
            <div class="info-label">Chuyên khoa:</div>
            <div class="info-value">${report.referringDoctorSpecialty || 'Không có thông tin'}</div>
        </div>
    </div>

    <div class="section">
        <div class="section-title">📋 Lịch sử Lâm sàng</div>
        <div class="info-value">${clinicalHistory || 'Không có thông tin'}</div>
    </div>

    <div class="section findings-section">
        <div class="section-title">🔍 Kết quả Chẩn đoán</div>
        <div class="info-value">${report.findings || 'Chưa có kết quả'}</div>
    </div>

    <div class="section recommendations-section">
        <div class="section-title">💡 Khuyến nghị</div>
        <div class="info-value">${report.recommendations || 'Chưa có khuyến nghị'}</div>
    </div>

    <div class="section">
        <div class="section-title">📊 Thông tin Báo cáo</div>
        <div class="info-row">
            <div class="info-label">Mã báo cáo:</div>
            <div class="info-value">${report.reportCode}</div>
        </div>
        <div class="info-row">
            <div class="info-label">Loại báo cáo:</div>
            <div class="info-value">${report.reportType || 'Sơ bộ'}</div>
        </div>
        <div class="info-row">
            <div class="info-label">Trạng thái:</div>
            <div class="info-value">${report.status}</div>
        </div>
        <div class="info-row">
            <div class="info-label">Ngày tạo:</div>
            <div class="info-value">${formatDate(report.createdAt)}</div>
        </div>
        <div class="info-row">
            <div class="info-label">Ngày cập nhật:</div>
            <div class="info-value">${formatDate(report.updatedAt)}</div>
        </div>
    </div>

    <div class="footer">
        <div class="signature-box">
            <div class="signature-title">Bác sĩ đọc kết quả</div>
            <div class="signature-name">BS. ${report.referringDoctorName || '_______________'}</div>
        </div>
        <div class="signature-box">
            <div class="signature-title">Ngày lập báo cáo</div>
            <div class="signature-name">${formatDate(report.createdAt)}</div>
        </div>
    </div>
</body>
</html>`;
    };

    return (
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
            <div className="report-form-page">
                <section className="report-form-section">
                    <h1>📝 Phiếu xét nghiệm bệnh lý</h1>

                    {/* HIỂN THỊ: Thông tin bệnh nhân đã chọn */}
                    {selectedPatientInfo && (
                        <div className="patient-info-banner">
                            <div className="patient-banner">
                                <h3>👤 Bệnh nhân đã chọn</h3>
                                <div className="patient-details">
                                    <span><strong>Tên:</strong> {selectedPatientInfo.full_name || selectedPatientInfo.fullName}</span>
                                    <span><strong>Mã BN:</strong> {selectedPatientInfo.patient_code || selectedPatientInfo.patientCode}</span>
                                    <span><strong>Giới tính:</strong> {selectedPatientInfo.gender}</span>
                                    <span><strong>SĐT:</strong> {selectedPatientInfo.phone}</span>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* HIỂN THỊ: Loading bệnh nhân */}
                    {patientLoading && (
                        <div className="loading-banner">
                            <div>🔄 Đang tải thông tin bệnh nhân...</div>
                        </div>
                    )}

                    {/* Message Display */}
                    {message.text && (
                        <div className={`message ${message.type === 'success' ? 'message-success' : message.type === 'warning' ? 'message-warning' : 'message-error'}`}>
                            {message.text}
                        </div>
                    )}

                    {/* Report Code Display */}
                    {reportCode && (
                        <div className="report-code-display">
                            <strong>Mã báo cáo: {reportCode}</strong>
                        </div>
                    )}

                    <form className="report-form" onSubmit={handleCreateReport}>
                        {/* Nhận dạng bệnh nhân */}
                        <fieldset className="form-group">
                            <legend>👤 Nhận dạng bệnh nhân {selectedPatientInfo && <span className="auto-filled">✅ Tự động điền</span>}</legend>

                            <div className="patient-info-section">
                                <label>
                                    Tên
                                    <input
                                        type="text"
                                        name="firstName"
                                        placeholder="Tên"
                                        value={formData.firstName}
                                        onChange={handleInputChange}
                                        className={selectedPatientInfo ? 'auto-filled-field' : ''}
                                    />
                                </label>

                                <label>
                                    Ngày sinh
                                    <input
                                        type="date"
                                        name="dateOfBirth"
                                        value={formData.dateOfBirth}
                                        onChange={handleInputChange}
                                        className={selectedPatientInfo ? 'auto-filled-field' : ''}
                                    />
                                </label>

                                <label>
                                    Giới tính
                                    <select
                                        name="gender"
                                        value={formData.gender}
                                        onChange={handleInputChange}
                                        className={selectedPatientInfo ? 'auto-filled-field' : ''}
                                    >
                                        <option value="">-- Chọn giới tính --</option>
                                        <option value="Nam">Nam</option>
                                        <option value="Nữ">Nữ</option>
                                        <option value="Khác">Khác</option>
                                    </select>
                                </label>

                                <label>
                                    Địa chỉ
                                    <input
                                        type="text"
                                        name="address"
                                        placeholder="123 Đường ABC, Quận XYZ"
                                        value={formData.address}
                                        onChange={handleInputChange}
                                        className={selectedPatientInfo ? 'auto-filled-field' : ''}
                                    />
                                </label>
                            </div>
                        </fieldset>

                        {/* Bác sĩ giới thiệu */}
                        <fieldset className="form-group">
                            <legend>🩺 Bác sĩ giới thiệu</legend>

                            <div className="doctor-info-section">
                                <label>
                                    Tên bác sĩ giới thiệu
                                    <input
                                        type="text"
                                        name="referringDoctor"
                                        placeholder="BS. Nguyễn Văn B"
                                        value={formData.referringDoctor}
                                        onChange={handleInputChange}
                                    />
                                </label>

                                <label>
                                    Chuyên khoa
                                    <select
                                        name="doctorSpecialty"
                                        value={formData.doctorSpecialty}
                                        onChange={handleInputChange}
                                    >
                                        <option value="">-- Chọn chuyên khoa --</option>
                                        <option value="Bác sĩ gia đình">Bác sĩ gia đình</option>
                                        <option value="Bác sĩ phẫu thuật">Bác sĩ phẫu thuật</option>
                                        <option value="Bác sĩ ung thư">Bác sĩ ung thư</option>
                                        <option value="Bác sĩ nội khoa">Bác sĩ nội khoa</option>
                                        <option value="Khác">Khác</option>
                                    </select>
                                </label>
                            </div>
                        </fieldset>

                        {/* Lịch sử lâm sàng */}
                        <fieldset className="form-group">
                            <legend>📋 Lịch sử lâm sàng</legend>

                            <div className="clinical-history-section">
                                <label>
                                    Triệu chứng
                                    <div className="symptoms-checkboxes">
                                        {['Sốt', 'Đau', 'Khối u', 'Viêm', 'Khác'].map(symptom => (
                                            <label key={symptom} className="checkbox-label">
                                                <input
                                                    type="checkbox"
                                                    name="symptoms"
                                                    value={symptom}
                                                    checked={formData.symptoms.includes(symptom)}
                                                    onChange={handleCheckboxChange}
                                                />
                                                {symptom}
                                            </label>
                                        ))}
                                    </div>
                                </label>

                                <label>
                                    Ghi chú lâm sàng
                                    <textarea
                                        name="clinicalHistory"
                                        rows="3"
                                        placeholder="Ví dụ: ho kéo dài, sưng hạch cổ,..."
                                        value={formData.clinicalHistory}
                                        onChange={handleInputChange}
                                        className={selectedPatientInfo ? 'auto-filled-field' : ''}
                                    ></textarea>
                                </label>
                            </div>
                        </fieldset>

                        {/* Chẩn đoán */}
                        <fieldset className="form-group">
                            <legend>🔍 Chẩn đoán</legend>

                            <div className="diagnosis-section">
                                <label className="full-width">
                                    Kết quả chẩn đoán
                                    <textarea
                                        name="diagnosis"
                                        rows="3"
                                        placeholder="Kết quả xét nghiệm, phát hiện bệnh lý hoặc bất thường..."
                                        value={formData.diagnosis}
                                        onChange={handleInputChange}
                                    ></textarea>
                                </label>

                                <label>
                                    Các phát hiện chi tiết
                                    <textarea
                                        name="findings"
                                        rows="3"
                                        placeholder="Chi tiết các phát hiện từ xét nghiệm..."
                                        value={formData.findings}
                                        onChange={handleInputChange}
                                    ></textarea>
                                </label>

                                <label>
                                    Khuyến nghị
                                    <textarea
                                        name="recommendations"
                                        rows="3"
                                        placeholder="Khuyến nghị điều trị hoặc theo dõi..."
                                        value={formData.recommendations}
                                        onChange={handleInputChange}
                                    ></textarea>
                                </label>
                            </div>
                        </fieldset>

                        <div className="button-group">
                            <button
                                type="submit"
                                className={`submit-button ${loading ? 'loading' : ''}`}
                                disabled={loading}
                            >
                                {loading ? 'Đang tạo...' : 'Tạo báo cáo'}
                            </button>

                            <button
                                type="button"
                                className={`view-reports-button ${reportsLoading ? 'loading' : ''}`}
                                onClick={handleViewReports}
                                disabled={reportsLoading}
                            >
                                {reportsLoading ? 'Đang tải...' : showReportsList ? 'Ẩn danh sách' : 'Xem báo cáo'}
                            </button>
                        </div>
                    </form>

                    {/* Reports List Display */}
                    {showReportsList && (
                        <div className="reports-list-section">
                            <h2>📊 Danh sách báo cáo chẩn đoán</h2>

                            {reportsLoading ? (
                                <div className="loading-spinner">🔄 Đang tải danh sách báo cáo...</div>
                            ) : reportsList.length === 0 ? (
                                <div className="empty-reports">
                                    <p>📋 Chưa có báo cáo nào được tạo</p>
                                </div>
                            ) : (
                                <div className="reports-table-container">
                                    <table className="reports-table">
                                        <thead>
                                        <tr>
                                            <th>Mã báo cáo</th>
                                            <th>Bệnh nhân</th>
                                            <th>Bác sĩ giới thiệu</th>
                                            <th>Chuyên khoa</th>
                                            <th>Trạng thái</th>
                                            <th>Ngày tạo</th>
                                            <th>Ngày cập nhật</th>
                                            <th>Thao tác</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {reportsList.map((report) => (
                                            <tr key={report.reportId}>
                                                <td className="report-code-cell">
                                                    <strong>{report.reportCode}</strong>
                                                </td>
                                                <td>
                                                    {/* Extract patient name from impression field */}
                                                    {report.impression ?
                                                        (() => {
                                                            const lines = report.impression.split('\n');
                                                            const patientLine = lines.find(line => line.startsWith('Bệnh nhân:'));
                                                            return patientLine ? patientLine.replace('Bệnh nhân: ', '') : 'Không có thông tin';
                                                        })() :
                                                        'Không có thông tin'
                                                    }
                                                </td>
                                                <td>{report.referringDoctorName || 'Không có'}</td>
                                                <td>{report.referringDoctorSpecialty || 'Không có'}</td>
                                                <td>
                                                        <span className={getStatusBadgeClass(report.status)}>
                                                            {report.status}
                                                        </span>
                                                </td>
                                                <td>{formatDate(report.createdAt)}</td>
                                                <td>{formatDate(report.updatedAt)}</td>
                                                <td className="action-cell">
                                                    <div className="action-buttons">
                                                        <button
                                                            className="download-btn"
                                                            onClick={() => handleDownloadReport(report)}
                                                            title={`Tải báo cáo ${report.reportCode}`}
                                                        >
                                                            📄 Tải HTML
                                                        </button>
                                                        <button
                                                            className="print-btn"
                                                            onClick={() => handlePrintReport(report)}
                                                            title={`In báo cáo ${report.reportCode}`}
                                                        >
                                                            🖨️ In báo cáo
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                        ))}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </div>
                    )}
                </section>
            </div>
        </LayoutLogin>
    );
};

export default memo(MedicalReportForm);