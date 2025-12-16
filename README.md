# ProjectIot - Fall Detection Android App

Ứng dụng Android IoT giám sát và cảnh báo ngã dành cho người cao tuổi.
## Giao diện & Tính năng:

### 1️⃣ **Màn hình Home - Dashboard Real-time**
**Cách hoạt động:**
- App lắng nghe Firebase node `data1` theo thời gian thực
- Khi có event mới (từ thiết bị IoT/Camera):
  - Nếu `type="fall"` → CardView đổi sang **màu đỏ** + text "⚠️ PHÁT HIỆN NGÃ!"
  - Nếu `type="detect"` hoặc khác → CardView giữ **màu xanh** + text "HỆ THỐNG AN TOÀN"
- **Hiển thị ảnh camera**: Ảnh Base64 từ Firebase được decode và hiển thị real-time
- **Nút SOS Khẩn cấp**: FAB màu đỏ 📞 - Bấm là gọi ngay (đọc số từ Settings)

**Giải thích "HỆ THỐNG AN TOÀN":**
- Đây là trạng thái mặc định khi không có sự cố ngã
- CardView sẽ tự động đổi màu dựa trên event mới nhất từ Firebase
- Logic: `if (type == "fall")` → Đỏ, ngược lại → Xanh

### 2️⃣ **Màn hình Notification - Lịch sử sự kiện**
- **Format timestamp**: Hiển thị ngày giờ dễ đọc (VD: `14:30 - 25/12/2025`)
- **Phân loại màu sắc**:
  - 🔴 Màu đỏ + chữ in đậm cho cảnh báo NGÃ
  - 🟢 Màu xanh lá cho trạng thái bình thường
- **Thumbnail ảnh**: Hiển thị ảnh thumbnail Base64 bên cạnh mỗi thông báo
- **Zoom ảnh**: Click vào thumbnail để xem ảnh full screen (mới!)
- **CardView design**: Layout đẹp hơn với bo góc và shadow

### 3️⃣ **Màn hình Settings - Cấu hình linh hoạt**
- **Đổi số điện thoại khẩn cấp**: Lưu vào SharedPreferences, không hardcode
- **Thông tin người được giám sát**: Tên và địa chỉ thiết bị
- **Nút Test System**: Gửi thông báo test để kiểm tra hệ thống

### 4️⃣ **Cải tiến kỹ thuật**
- Số điện thoại khẩn cấp được đọc từ SharedPreferences (không hardcode)
- Xử lý timestamp linh hoạt (cả giây và milliseconds)
- Decode Base64 image an toàn với try-catch
- Image viewer dialog với khả năng zoom ảnh

## Chức năng chính
- Đăng nhập/Đăng ký với Firebase Authentication
- Giám sát real-time phát hiện người ngã (tự động đổi màu CardView)
- Hiển thị hình ảnh từ camera IoT (Base64 decode)
- Thông báo push khi phát hiện ngã (với màu sắc phân biệt)
- Button gọi điện khẩn cấp (có thể tùy chỉnh số)
- Lịch sử các sự kiện (với thumbnail và timestamp đẹp)
- **Zoom ảnh full screen** khi click vào thumbnail (CẬP NHẬT MỚI!)
- Cài đặt linh hoạt (số điện thoại, thông tin người dùng)

## Yêu cầu
- Android Studio Hedgehog hoặc mới hơn
- Android SDK 24+
- Firebase project đã setup
- Kotlin 1.9+

## Cài đặt

### 1. Clone repository
```bash
git clone <repository-url>
cd ProjectIot
```

### 2. Cấu hình Firebase

Xem hướng dẫn chi tiết tại: [SETUP_FIREBASE.md](SETUP_FIREBASE.md)

Tóm tắt:
```bash
# Copy template
copy app\google-services.json.template app\google-services.json

# Sau đó download file google-services.json từ Firebase Console
# và thay thế nội dung
```

### 3. Build project
```bash
gradlew build
```

### 4. Run app
Chọn device hoặc emulator và click Run trong Android Studio.

## Cấu trúc dự án
```
app/src/main/java/com/danh/myapplication/
├── MainActivity.kt          # Activity chính
├── HomeFragment.kt          # Màn hình giám sát
├── LoginFragment.kt         # Đăng nhập
├── RegisterFragment.kt      # Đăng ký
├── NotificationFragment.kt  # Lịch sử thông báo
├── SettingFragment.kt       # Cài đặt
├── DataEvent.kt            # Model dữ liệu
├── Notification.kt         # Model thông báo
└── NotificationAdapter.kt  # Adapter RecyclerView
```

## 📱 Firebase Structure
```json
data1/
  ├── {timestamp1}/
  │   ├── type: "fall" | "detect"
  │   ├── imageUrl: "base64_encoded_image"
  │   └── timestamp: "2025-12-16 10:30:45"
  └── {timestamp2}/
      └── ...
```

## Team
Đảm bảo mọi thành viên team đều:
1. Có quyền truy cập Firebase Console
2. Đã download file `google-services.json`
3. Đặt file vào `app/` trước khi build

## License
[Your License Here]
