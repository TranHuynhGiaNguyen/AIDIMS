import { memo, useState } from "react";
import LayoutLogin from "../Layout/LayoutLogin";
import "../../css/CompareImages.css";
import FloatingImageModal from "../../components/FloatingImageModal";
import MiniChatbot from "../Doctor/MiniChatBot";

const CompareImages = () => {
  const [keyword, setKeyword] = useState("");
  const [dicomImages, setDicomImages] = useState([]);
  const [searching, setSearching] = useState(false);
  const [openEditorImages, setOpenEditorImages] = useState([]); // Modal đang mở
  const [selectedImages, setSelectedImages] = useState([]); // Ảnh được chọn

  const handleSearch = async () => {
    if (!keyword.trim()) return;

    setSearching(true);
    try {
      const response = await fetch(
        `http://localhost:8080/api/compare-images/search?keyword=${encodeURIComponent(keyword)}`
      );
      const data = await response.json();
      const sorted = data.sort(
        (a, b) => new Date(a.dateTaken) - new Date(b.dateTaken)
      );
      setDicomImages(sorted);
    } catch (error) {
      console.error("❌ Lỗi khi tìm ảnh DICOM:", error);
      setDicomImages([]);
    } finally {
      setSearching(false);
    }
  };

  const toggleImageSelection = (url) => {
    setSelectedImages((prev) =>
      prev.includes(url) ? prev.filter((u) => u !== url) : [...prev, url]
    );
  };

  const handleOpenSelected = () => {
    const newModals = selectedImages.filter((url) => !openEditorImages.includes(url));
    setOpenEditorImages((prev) => [...prev, ...newModals]);
    setSelectedImages([]);
  };

  const handleCloseEditor = (imageUrl) => {
    setOpenEditorImages((prev) => prev.filter((url) => url !== imageUrl));
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
      <div className="doctor-page">
        <div className="dicom-list-container">
          <div className="page-header">
            <h2>🔍 So sánh ảnh DICOM</h2>
            <p>Nhập mã bệnh nhân hoặc tên để tìm tất cả ảnh đã chụp</p>
            <div style={{ margin: "10px 0", display: "flex", gap: "10px" }}>
              <input
                type="text"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder="Nhập mã bệnh nhân hoặc tên..."
                className="search-input"
                style={{ padding: "8px 12px", minWidth: "300px" }}
              />
              <button className="btn btn-primary" onClick={handleSearch}>
                🔍 Tìm kiếm
              </button>
            </div>
            {selectedImages.length > 0 && (
              <button
                className="btn btn-success"
                onClick={handleOpenSelected}
                style={{ marginTop: "10px" }}
              >
                🖼 Mở {selectedImages.length} ảnh đã chọn
              </button>
            )}
          </div>

          {searching && <p>⏳ Đang tìm ảnh...</p>}

          {dicomImages.length > 0 ? (
            <div className="table-container">
              <table className="dicom-table">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Mã BN</th>
                    <th>Loại chụp</th>
                    <th>Bộ phận</th>
                    <th>Tên file</th>
                    <th>Ngày tạo</th>
                    <th>Ảnh</th>
                  </tr>
                </thead>
                <tbody>
                  {dicomImages.map((img, index) => (
                    <tr key={img.id || index} className="dicom-row">
                      <td>{index + 1}</td>
                      <td>{img.patientCode}</td>
                      <td>{img.studyType || img.modality || "N/A"}</td>
                      <td>{img.bodyPart || "N/A"}</td>
                      <td>{img.fileName || "N/A"}</td>
                      <td>{img.dateTaken || "N/A"}</td>
                      <td>
                        <img
                          src={img.imageUrl}
                          alt={`DICOM ${index}`}
                          style={{
                            width: "100px",
                            cursor: "pointer",
                            border: selectedImages.includes(img.imageUrl)
                              ? "3px solid #007bff"
                              : "1px solid transparent",
                            borderRadius: "6px",
                          }}
                          onClick={() => toggleImageSelection(img.imageUrl)}
                        />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            !searching && <p>Không tìm thấy ảnh cho từ khóa: <b>{keyword}</b></p>
          )}

          {openEditorImages.map((url, index) => (
            <FloatingImageModal
              key={url}
              imageUrl={url}
              onClose={() => handleCloseEditor(url)}
              topOffset={10}
              leftOffset={10 + index * 35}  // sẽ lệch ngang mỗi modal
            />
          ))}
        </div>
        <MiniChatbot />
      </div>
    </LayoutLogin>
  );
};

export default memo(CompareImages);