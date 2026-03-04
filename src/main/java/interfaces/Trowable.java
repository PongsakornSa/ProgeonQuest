package interfaces;

import logic.base.BaseEntity;

/**
 * Interface สำหรับไอเทมที่สามารถขว้างใส่ศัตรูในระหว่างการต่อสู้ได้ เช่น ระเบิด
 */
public interface Trowable {

    /**
     * ขว้างไอเทมใส่เป้าหมาย
     * @param target เป้าหมายที่จะถูกขว้าง
     * @return ข้อความอธิบายผลที่เกิดขึ้น
     */
    String throwAt(BaseEntity target);

    /**
     * คืนค่าพลังของการขว้าง
     */
    int getThrowPower();

    /**
     * คืนค่าประเภทดาเมจของการขว้าง (physical/magic)
     */
    String getDamageType();
}
