# ProjectIot - Fall Detection Android App

Ứng dụng Android IoT giám sát và cảnh báo ngã dành cho người cao tuổi.

## Chức năng chính
- Đăng nhập/Đăng ký với Firebase Authentication
- Giám sát real-time phát hiện người ngã
- Hiển thị hình ảnh từ camera IoT
- Thông báo push khi phát hiện ngã
- Button gọi điện khẩn cấp
- Lịch sử các sự kiện

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
