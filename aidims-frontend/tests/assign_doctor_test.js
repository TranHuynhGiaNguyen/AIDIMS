Feature('Receptionist Operations - Điều phối Bác sĩ Chuyên khoa (Assign UI)');

Scenario('Lễ tân điều phối bệnh nhân BN999 sang chuyên khoa Hô hấp cho bác sĩ Cường', async ({ I }) => {

    // ======================================================
    // 1. ĐĂNG NHẬP
    // ======================================================
    I.amOnPage('/login/receptionist');

    I.fillField('Tên đăng nhập', '2');
    I.fillField('Mật khẩu', '2');
    I.click('Đăng nhập');

    I.waitForText('Bắt đầu ca làm việc', 15);
    I.click('Bắt đầu ca làm việc');

    I.wait(2);

    // ======================================================
    // 2. MỞ TRANG CHỈ ĐỊNH BÁC SĨ
    // ======================================================
    I.amOnPage('/receptionist/assign');

    I.seeInCurrentUrl('/receptionist/assign');

    I.waitForText('Chuyển hồ sơ đến Bác sĩ', 20);

    // ======================================================
    // 3. CHỜ REACT LOAD DANH SÁCH BỆNH NHÂN
    // ======================================================
    I.waitForElement('select',20);

    I.waitForFunction(() => {

        const options=document.querySelectorAll("select option");

        return [...options].some(o=>o.textContent.includes("BN999"));

    },20);

    const patientValue = await I.grabAttributeFrom(
        '//select[1]/option[contains(.,"BN999")]',
        'value'
    );

    I.say('Patient Value = ' + patientValue);

    I.selectOption('//select[1]',patientValue);

    I.wait(2);

    // ======================================================
    // 4. CHỌN CHUYÊN KHOA HÔ HẤP
    // ======================================================
    I.waitForFunction(() => {

        const selects = document.querySelectorAll("select");
    
        if (selects.length < 2) return false;
    
        return [...selects[1].options]
            .some(o => o.textContent.includes("Hô hấp"));
    
    },20);
    
    const specialtyValue = await I.grabAttributeFrom(
    '//label[contains(.,"Chọn chuyên khoa")]/following-sibling::select/option[contains(.,"Hô hấp")]',
    'value'
    );
    
    I.say("Specialty = " + specialtyValue);
    
    I.selectOption(
    '//label[contains(.,"Chọn chuyên khoa")]/following-sibling::select',
    specialtyValue
    );
    
    I.wait(3);

    // ======================================================
    // React sẽ gọi API load bác sĩ
    // ======================================================
    I.waitForFunction(() => {

        const doctorSelect=document.querySelectorAll("select")[2];

        return doctorSelect && doctorSelect.options.length>1;

    },20);

    // ======================================================
    // 5. CHỌN BÁC SĨ CƯỜNG
    // ======================================================
    // ===================================================
// BƯỚC 3: CHỌN BÁC SĨ LÊ VĂN CƯỜNG
// ===================================================

// Chờ dropdown bác sĩ có dữ liệu
I.waitForFunction(() => {
    const doctorSelect = document.querySelectorAll("select")[2];
    if (!doctorSelect) return false;

    return [...doctorSelect.options].some(o =>
        o.textContent.includes("Lê Văn Cường")
    );
}, 20);

// Chọn trực tiếp theo text
I.selectOption(
    '//label[contains(.,"Chọn bác sĩ")]/following-sibling::select',
    'Lê Văn Cường - Hô hấp'
);

I.wait(2);

    // ======================================================
    // 6. CHỌN MỨC ĐỘ ƯU TIÊN
    // ======================================================
    I.checkOption('//input[@type="radio" and @value="Ưu tiên"]');

    // ======================================================
    // 7. GHI CHÚ
    // ======================================================
    I.fillField(
        'textarea',
        'bệnh nhân suy hô hấp thở hấp hối'
    );

    // // ======================================================
    // // 8. LƯU
    // // ======================================================
    // I.click('button[type="submit"]');

    // I.acceptPopup();

    // I.wait(4);

    // // ======================================================
    // // 9. KIỂM TRA LỊCH SỬ
    // // ======================================================
    // I.waitForText('BN999',20);

    // I.see('BN999');

    // I.see('Ưu tiên');

    // I.see('bệnh nhân suy hô hấp thở hấp hối');

});