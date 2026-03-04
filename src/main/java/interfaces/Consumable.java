package interfaces;

import logic.entities.Player;

/**
 * Interface สำหรับไอเทมที่สามารถกดใช้ได้ เช่น potion, ระเบิด
 */
public interface Consumable {

    /**
     * ใช้ไอเทมบนตัวละครที่กำหนด
     * @param target ตัวละครที่จะใช้ไอเทมด้วย
     * @return ข้อความอธิบายผลการใช้ไอเทม
     */
    String use(Player target);

    /**
     * คืนค่าคำอธิบายเอฟเฟคของไอเทม
     */
    String getEffectDescription();
}
