Feature('Receptionist Operations - Ghi nhận Triệu chứng Bệnh nhân');

Scenario('Lễ tân ghi nhận triệu chứng cho bệnh nhân BN999', async ({ I }) => {

    // ===============================
    // Đăng nhập
    // ===============================
    I.amOnPage('/login/receptionist');

    I.fillField('Tên đăng nhập', '2');
    I.fillField('Mật khẩu', '2');
    I.click('Đăng nhập');

    I.waitForText('Bắt đầu ca làm việc', 10);
    I.click('Bắt đầu ca làm việc');

    I.wait(2);

    // ===============================
    // Trang triệu chứng
    // ===============================
    I.amOnPage('/receptionist/symptoms');

    I.seeInCurrentUrl('/receptionist/symptoms');

    I.waitForText('Ghi nhận Triệu chứng', 20);

    // ===================================================
    // Chờ load danh sách bệnh nhân
    // ===================================================
    I.waitForElement('select', 20);

    I.wait(3);

    // ===================================================
    // Chọn option có chứa BN999
    // ===================================================
    const optionValue = await I.grabAttributeFrom(
        '//select/option[contains(text(),"BN999")]',
        'value'
    );

    I.selectOption('select', optionValue);

    I.wait(1);

    // ===================================================
    // Mô tả triệu chứng
    // ===================================================
    I.fillField(
        '//textarea[contains(@placeholder, "Mô tả chi tiết triệu chứng chính")]',
        'sốt cao, bình thường'
    );

    // ===================================================
    // Mức độ nghiêm trọng
    // ===================================================
    I.selectOption(
        '//div[label[contains(.,"Mức độ nghiêm trọng")]]/select',
        'Nhẹ'
    );

    // ===================================================
    // Mức độ ưu tiên
    // ===================================================
    I.selectOption(
        '//div[label[contains(.,"Mức độ ưu tiên")]]/select',
        'Ưu tiên'
    );

    // ===================================================
    // Khởi phát (Dropdown select theo mã React nguồn)
    // ===================================================
    I.selectOption(
        '//div[label[contains(.,"Thời gian khởi phát")]]/select',
        'Không rõ'
    );

    // ===================================================
    // Duration (ĐÃ FIX: Ô nhập text input theo mã React nguồn)
    // ===================================================
    I.fillField(
        '//input[contains(@placeholder, "VD: 2 giờ, 1 ngày, 1 tuần")]',
        '2 days'
    );

    // ===================================================
    // Pain Scale
    // ===================================================
    I.fillField(
        'input[type="number"]',
        '1'
    );

    // ===================================================
    // Chọn triệu chứng (Toggle nhanh qua text click)
    // ===================================================
    I.click('Đau ngực');
    I.click('Hồi hộp');
    I.click('Ho khan');

    // ===================================================
    // Triệu chứng khác
    // ===================================================
    I.fillField(
        '//textarea[contains(@placeholder, "Ghi rõ các triệu chứng khác")]',
        'không'
    );

    // ===================================================
    // Ghi chú
    // ===================================================
    I.fillField(
        '//textarea[contains(@placeholder, "Thông tin bổ sung về tình trạng")]',
        'bệnh nhân không khoẻ, khó thở'
    );

    // ===================================================
    // Lưu
    // ===================================================
    I.click('💾 Lưu thông tin triệu chứng');

    I.acceptPopup();

    I.wait(4);

    // ===================================================
    // Kiểm tra lịch sử
    // ===================================================
    I.waitForText('BN999', 20);

    // Click vào icon con mắt/nút chi tiết nằm chung hàng (tr) với mã bệnh nhân BN999
    I.click('//tr[td[contains(.,"BN999")]]//button');

    I.waitForText('CHI TIẾT TRIỆU CHỨNG', 10);

    I.see('sốt cao, bình thường');

    I.see('bệnh nhân không khoẻ, khó thở');

});