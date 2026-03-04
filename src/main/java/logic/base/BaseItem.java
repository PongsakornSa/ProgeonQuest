package logic.base;

/**
 * คลาสแม่ของ Item ทุกประเภท ประกอบด้วยข้อมูลพื้นฐานของไอเทม
 */
public abstract class BaseItem {

    protected String id;           // รหัสเฉพาะของไอเทม
    protected String name;         // ชื่อไอเทม
    protected String description;  // คำอธิบาย
    protected String imagePath;    // path รูปจาก resources
    protected int sellPrice;       // ราคาขาย
    protected int buyPrice;        // ราคาซื้อ
    protected int quantity;        // จำนวนที่มี (สำหรับ stackable)

    /**
     * Constructor พื้นฐาน
     */
    public BaseItem(String id, String name, String description, String imagePath, int buyPrice, int sellPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.quantity = 1;
    }

    /**
     * ตรวจสอบว่า Item นี้ stack ได้หรือไม่
     */
    public abstract boolean isStackable();

    /**
     * คืนค่าประเภทของ Item เพื่อแสดงใน UI
     */
    public abstract String getItemType();

    // ===== Getters =====
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImagePath() { return imagePath; }
    public int getSellPrice() { return sellPrice; }
    public int getBuyPrice() { return buyPrice; }
    public int getQuantity() { return quantity; }

    // ===== Setters =====
    public void setQuantity(int quantity) { this.quantity = quantity; }

    /**
     * เพิ่มจำนวน (stack)
     */
    public void addQuantity(int amount) { this.quantity += amount; }

    /**
     * ลดจำนวน คืนค่า true ถ้ายังมีเหลืออยู่
     */
    public boolean decreaseQuantity(int amount) {
        this.quantity -= amount;
        return this.quantity > 0;
    }

    @Override
    public String toString() {
        return name + (quantity > 1 ? " x" + quantity : "");
    }
}
