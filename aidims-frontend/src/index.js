import "./apiInterceptor.js"
import ReactDOM from "react-dom/client"
import HomePage from "./pages/Guest/index.js"
import { BrowserRouter, Routes, Route } from "react-router-dom"
import Login from "./pages/Login/Login.js"
import Register from "./pages/Register/Register.js"
import IndexDoctor from "./pages/Doctor/indexDoctor.js"
import PatientProfile from "./pages/Doctor/PatientProfile.js"
import MedicalReportForm from "./pages/Doctor/MedicalReportForm.js"
import IndexReceptionist from "./pages/Receptionist/IndexReceptionist.js"
import PatientForm from "./pages/Receptionist/PatientForm.js"
import SymptomRecord from "./pages/Receptionist/SymptomRecord.js"
import AssignDoctor from "./pages/Receptionist/AssignDoctor.js"
import IndexTechnician from "./pages/Technician/IndexTechnician.js"
import ImportDicom from "./pages/Technician/ImportDicom.js"
import VerifyImages from "./pages/Technician/VerifyImages.js"
import AssignImages from "./pages/Technician/AssignImages.js"
import IndexAdmin from "./pages/Admin/IndexAdmin.js"
import UserManagement from "./pages/Admin/UserManagement.js"
import SystemMonitoring from "./pages/Admin/SystemMonitoring.js"
import SystemSettings from "./pages/Admin/SystemSettings.js"
import About from "./pages/Guest/about.js"
import Contact from "./pages/Guest/Contact.js"
import Feature from "./pages/Guest/Feature.js"
import User from "./pages/User.js"
import DicomViewer from "./pages/Doctor/DicomViewer.js"
import CompareImages from "./pages/Doctor/CompareImages.js"
import SymptomDisplayLayout from "./pages/Doctor/SymptomDisplay.js"
import CreateImagingRequest from "./pages/Doctor/CreateImagingRequest.js"
import MiniChatBot from "./pages/Doctor/MiniChatBot.js"
import DoctorLogin from "./pages/Login/DoctorLogin.js"
import ReceptionistLogin from "./pages/Login/ReceptionistLogin.js"
import TechnicianLogin from "./pages/Login/TechnicianLogin.js"
import AdminLogin from "./pages/Login/AdminLogin.js"
import { ProtectedRoute, PublicRoute } from "./components/ProtectedRoute.js"

const root = ReactDOM.createRoot(document.getElementById("root"))
root.render(
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />
        
        {/* Auth / Public Routes - /login (role selection) is public. Role logins redirect if matching role. */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/LoginRegister" element={<Login />} />
        <Route path="/login/doctor" element={<PublicRoute restrictedRole="doctor"><DoctorLogin /></PublicRoute>} />
        <Route path="/login/receptionist" element={<PublicRoute restrictedRole="receptionist"><ReceptionistLogin /></PublicRoute>} />
        <Route path="/login/technician" element={<PublicRoute restrictedRole="technician"><TechnicianLogin /></PublicRoute>} />
        <Route path="/login/admin" element={<PublicRoute restrictedRole="admin"><AdminLogin /></PublicRoute>} />

        {/* Doctor Routes */}
        <Route path="/IndexDoctor" element={<ProtectedRoute allowedRoles={["doctor"]}><IndexDoctor /></ProtectedRoute>} />
        <Route path="/doctor/patients" element={<ProtectedRoute allowedRoles={["doctor"]}><PatientProfile /></ProtectedRoute>} />
        <Route path="/PatientProfile" element={<ProtectedRoute allowedRoles={["doctor"]}><PatientProfile /></ProtectedRoute>} />
        <Route path="/doctor/reports" element={<ProtectedRoute allowedRoles={["doctor"]}><MedicalReportForm /></ProtectedRoute>} />
        <Route path="MedicalReportForm" element={<ProtectedRoute allowedRoles={["doctor"]}><MedicalReportForm /></ProtectedRoute>} />
        <Route path="/doctor/dicom-viewer" element={<ProtectedRoute allowedRoles={["doctor"]}><DicomViewer /></ProtectedRoute>} />
        <Route path="/doctor/compare-images" element={<ProtectedRoute allowedRoles={["doctor"]}><CompareImages /></ProtectedRoute>} />
        <Route path="/doctor/symptom" element={<ProtectedRoute allowedRoles={["doctor"]}><SymptomDisplayLayout /></ProtectedRoute>} />
        <Route path="/SymptomDisplay" element={<ProtectedRoute allowedRoles={["doctor"]}><SymptomDisplayLayout /></ProtectedRoute>} />
        <Route path="/doctor/imagereport" element={<ProtectedRoute allowedRoles={["doctor"]}><CreateImagingRequest /></ProtectedRoute>} />
        <Route path="/CreateImagingRequest" element={<ProtectedRoute allowedRoles={["doctor"]}><CreateImagingRequest /></ProtectedRoute>} />
        <Route path="/MiniChatBot" element={<ProtectedRoute allowedRoles={["doctor"]}><MiniChatBot /></ProtectedRoute>} />

        {/* Receptionist Routes */}
        <Route path="/IndexReceptionist" element={<ProtectedRoute allowedRoles={["receptionist"]}><IndexReceptionist /></ProtectedRoute>} />
        <Route path="/receptionist" element={<ProtectedRoute allowedRoles={["receptionist"]}><IndexReceptionist /></ProtectedRoute>} />
        <Route path="/receptionist/patients" element={<ProtectedRoute allowedRoles={["receptionist"]}><PatientForm /></ProtectedRoute>} />
        <Route path="/receptionist/symptoms" element={<ProtectedRoute allowedRoles={["receptionist"]}><SymptomRecord /></ProtectedRoute>} />
        <Route path="/receptionist/assign" element={<ProtectedRoute allowedRoles={["receptionist"]}><AssignDoctor /></ProtectedRoute>} />
        <Route path="/patient" element={<ProtectedRoute allowedRoles={["receptionist"]}><PatientForm /></ProtectedRoute>} />

        {/* Technician Routes */}
        <Route path="/IndexTechnician" element={<ProtectedRoute allowedRoles={["technician"]}><IndexTechnician /></ProtectedRoute>} />
        <Route path="/technician" element={<ProtectedRoute allowedRoles={["technician"]}><IndexTechnician /></ProtectedRoute>} />
        <Route path="/technician/import-dicom" element={<ProtectedRoute allowedRoles={["technician"]}><ImportDicom /></ProtectedRoute>} />
        <Route path="/technician/verify-images" element={<ProtectedRoute allowedRoles={["technician"]}><VerifyImages /></ProtectedRoute>} />
        <Route path="/technician/assign-images" element={<ProtectedRoute allowedRoles={["technician"]}><AssignImages /></ProtectedRoute>} />

        {/* Admin Routes */}
        <Route path="/IndexAdmin" element={<ProtectedRoute allowedRoles={["admin"]}><IndexAdmin /></ProtectedRoute>} />
        <Route path="/admin" element={<ProtectedRoute allowedRoles={["admin"]}><IndexAdmin /></ProtectedRoute>} />
        <Route path="/admin/users" element={<ProtectedRoute allowedRoles={["admin"]}><UserManagement /></ProtectedRoute>} />
        <Route path="/admin/system" element={<ProtectedRoute allowedRoles={["admin"]}><SystemMonitoring /></ProtectedRoute>} />
        <Route path="/admin/settings" element={<ProtectedRoute allowedRoles={["admin"]}><SystemSettings /></ProtectedRoute>} />

        {/* Guest Routes */}
        <Route path="/About" element={<About />} />
        <Route path="/Contact" element={<Contact />} />
        <Route path="/Feature" element={<Feature />} />
        <Route path="/User" element={<ProtectedRoute><User /></ProtectedRoute>} />
      </Routes>
    </BrowserRouter>,
)
