/**
 * Import function triggers from the 2nd Gen API
 */
const { onValueCreated } = require("firebase-functions/v2/database");
const { setGlobalOptions } = require("firebase-functions/v2");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");
const axios = require("axios");

admin.initializeApp();
setGlobalOptions({ region: "us-central1", maxInstances: 10 });

// ================= CẤU HÌNH IFTTT (THAY CỦA BẠN VÀO ĐÂY) =================
const IFTTT_EVENT = "fall_detected"; // Tên event bạn đặt ở bước 2
const IFTTT_KEY = "gO1FtxtAeaxDaTSqZsUcfsdrT3r9mR29ZPE0zFpGk0q"; // Key lấy ở bước 3
// ========================================================================

exports.makeVoipCallOnFall = onValueCreated("/data1/{messageId}", async (event) => {
    const newData = event.data.val();

    if (!newData) return null;

    logger.info("Nhận dữ liệu mới:", newData);

    // Kiểm tra nếu type là "fall"
    if (newData.type === "fall") {
        logger.info("⚠️ PHÁT HIỆN TÉ NGÃ -> Đang kích hoạt cuộc gọi IFTTT...");

        const url = `https://maker.ifttt.com/trigger/fall_detected/with/key/gO1FtxtAeaxDaTSqZsUcfsdrT3r9mR29ZPE0zFpGk0q`;

        try {
            // Gọi Webhook để kích hoạt cuộc gọi
            await axios.post(url, {
                value1: "Người bệnh", // Có thể truyền thêm dữ liệu nếu muốn
                value2: "Phòng ngủ"
            });
            logger.info("✅ Đã kích hoạt cuộc gọi IFTTT thành công!");
        } catch (error) {
            logger.error("❌ Lỗi gọi IFTTT:", error.message);
        }
    } else {
        logger.info("Dữ liệu an toàn (Type: " + newData.type + ")");
    }
    return null;
});