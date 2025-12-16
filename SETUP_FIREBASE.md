# Hướng dẫn cấu hình Firebase

## ⚠️ BẢO MẬT: Không commit file `google-services.json` lên Git

### Bước 1: Tạo file google-services.json
1. Copy file template:
   ```bash
   copy app\google-services.json.template app\google-services.json
   ```

2. Mở Firebase Console: https://console.firebase.google.com
3. Chọn project: **iotproject-c4618**
4. Vào **Project Settings** > **General**
5. Scroll xuống phần **Your apps** > Chọn app Android
6. Click **Download google-services.json**
7. Copy nội dung vào `app/google-services.json`

### Bước 2: Thay thế thông tin
Hoặc thay thế các giá trị trong file template:
- `YOUR_PROJECT_NUMBER` → Project number
- `YOUR_PROJECT_ID` → Project ID
- `YOUR_STORAGE_BUCKET` → Storage bucket
- `YOUR_MOBILE_SDK_APP_ID` → Mobile SDK App ID
- `YOUR_FIREBASE_API_KEY` → Firebase API Key

### ✅ File đã được thêm vào .gitignore
File `google-services.json` sẽ không bị push lên Git repository.

### Cho team members mới:
1. Yêu cầu admin Firebase cung cấp file `google-services.json`
2. Hoặc download trực tiếp từ Firebase Console
3. Đặt file vào thư mục `app/`
