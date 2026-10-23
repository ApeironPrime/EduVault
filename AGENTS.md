# Hướng dẫn sản phẩm EduVault

Đọc `PRODUCT.md` trước khi thay đổi chức năng, giao diện hoặc dữ liệu mẫu. Đây là định vị đã được chủ dự án chốt: đào tạo và đánh giá intern/fresher trong doanh nghiệp phần mềm.

Giữ luồng lộ trình → nhiệm vụ → bằng chứng → nghiệm thu → mở chặng. Quiz chỉ hỗ trợ tự kiểm tra. Không đưa trở lại nghiệp vụ sinh viên, sàn tài liệu, XP, credit, bảng xếp hạng hoặc nội dung ôn thi.

Phân biệt vai trò nghiệp vụ với chức năng đã triển khai; không quảng bá AI, quyền mentor hoặc tích hợp doanh nghiệp chưa hoạt động. Bản hiện tại dùng Spark; không tự bật billing.

Giữ tương thích dữ liệu Firebase hiện có khi sửa nhãn; không sửa schema hoặc xóa dữ liệu để đổi câu chữ. Tiến độ, nhiệm vụ và khóa chặng phải dùng cùng nguồn dữ liệu. Tôn trọng bố cục đã được duyệt, chỉ sửa khi có yêu cầu hoặc có lỗi cụ thể.

Khi hoàn tất build/test và không còn công việc Gradle, dừng daemon bằng `gradlew.bat --stop` trước khi kiểm tra thủ công trên emulator của máy này. Daemon giữ RAM đã từng khiến máy chỉ còn khoảng 500 MB trống, kèm ANR ở launcher và app; không kết luận mọi ANR là lỗi app hoặc mọi ANR là lỗi emulator khi chưa đọc log.
