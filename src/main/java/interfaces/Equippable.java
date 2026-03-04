package interfaces;

import logic.entities.Player;

/**
 * Interface สำหรับไอเทมที่สามารถสวมใส่ได้ เช่น อาวุธ, ชุดเกราะ
 */
public interface Equippable {

    /**
     * ประเภทของช่องสวมใส่
     */
    enum SlotType { WEAPON, ARMOR }

    /**
     * สวมใส่ไอเทมให้กับตัวละคร บวก stat ที่ไอเทมให้
     * @param player ตัวละครที่จะสวมใส่ไอเทม
     */
    void equip(Player player);

    /**
     * ถอดไอเทมออกจากตัวละคร ลบ stat ที่ไอเทมเคยให้
     * @param player ตัวละครที่จะถอดไอเทม
     */
    void unequip(Player player);

    /**
     * คืนค่าประเภทของช่องสวมใส่ (WEAPON หรือ ARMOR)
     */
    SlotType getSlotType();

    /**
     * คืนค่า stat ที่ไอเทมนี้เพิ่มให้ในรูปแบบ String
     */
    String getStatBonusDescription();
}
