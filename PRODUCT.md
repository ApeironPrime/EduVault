# EduVault — Định vị sản phẩm cố định

## Mục đích

EduVault hỗ trợ **đào tạo và đánh giá intern/fresher trong doanh nghiệp phần mềm**, từ khi bắt đầu onboarding đến khi được nghiệm thu để tham gia dự án.

Giá trị cốt lõi là một lộ trình có kiểm soát: người học biết cần làm gì, nộp bằng chứng thực hành và xem trạng thái nghiệm thu; người phụ trách đánh giá bằng chứng để quyết định mở chặng tiếp theo.

## Đối tượng và vai trò

- **Người học:** intern/fresher đang tham gia chương trình đào tạo.
- **Mentor:** soạn/giao nhiệm vụ thực hành theo chặng cho học viên được phân công, hướng dẫn và nghiệm thu bài nộp. Có tài khoản riêng; có thể sửa yêu cầu, hạn nộp và liên kết học liệu trước khi bài đang chờ duyệt hoặc đã nghiệm thu.
- **Quản trị đào tạo:** quản lý học liệu, tài khoản và tiến độ theo quyền được cấp phía backend.

## Luồng chính

1. Đăng nhập và nhận chương trình onboarding.
2. Xem chặng đang mở, nhiệm vụ và học liệu liên quan.
3. Đọc học liệu, ghi chú và làm Quiz để tự kiểm tra kiến thức.
4. Hoàn thành bài thực hành, nộp liên kết bằng chứng.
5. Chờ nghiệm thu; nếu chưa đạt thì bổ sung hoặc làm lại.
6. Khi được duyệt, tiến độ cập nhật và chặng tiếp theo mở.

Chương trình mẫu gồm Quy chuẩn & bảo mật → Setup môi trường & Git → Mock project. Không gắn cứng người học ở Chặng 2 hoặc ở 33%; nhiệm vụ và tiến độ phải lấy từ cùng dữ liệu lộ trình.

**Quiz, đọc tài liệu và gửi bằng chứng chưa được duyệt không tự mở khóa chặng.** Người học không được tự sửa kết quả nghiệm thu hoặc phần trăm hoàn thành.

## Vai trò của các màn hình

| Màn hình | Mục đích |
| --- | --- |
| Lộ trình | Tiến độ, chặng đang mở, nhiệm vụ cần làm và nhật ký đào tạo |
| Nhiệm vụ | Đầu việc thực hành, trạng thái nộp/duyệt và bài tự kiểm tra |
| Học liệu | Nội dung đã kiểm duyệt phục vụ chương trình, tìm kiếm và lưu để đọc lại |
| Cá nhân | Họ tên, email đăng nhập, đơn vị/phòng ban, ảnh đại diện và bảo mật tài khoản |
| Mentor | Học viên được phân công, nhiệm vụ từng chặng, bài nộp và nghiệm thu |
| Quản trị | Công việc quản lý đúng quyền, không thay bằng số liệu giả khi thiếu quyền |

## Quy chuẩn nội dung

- Dùng ngôn ngữ đào tạo doanh nghiệp: chương trình, chặng, nhiệm vụ, bằng chứng, mentor, nghiệm thu, đơn vị/phòng ban.
- Dữ liệu mẫu phải liên quan phát triển phần mềm và onboarding. Không dùng môn đại học, thi cuối kỳ hoặc bảng tin sinh viên.
- Không dùng XP, bảng xếp hạng, credit đổi tài liệu, lượt tải hay đánh giá sao làm động lực hoặc thước đo năng lực.
- Không biến học liệu thành sàn mua bán/chia sẻ cộng đồng. Nội dung đóng góp phải phục vụ chương trình và được kiểm duyệt.
- Không thêm trường hồ sơ không phục vụ luồng chính, hoặc đổi nhãn để hợp thức hóa dữ liệu sai. Tên, đơn vị và tiến độ phải đúng dữ liệu thật; dữ liệu minh họa phải được mô tả trong tài liệu demo.
- Giữ trường dữ liệu cũ khi cần tương thích Firebase; việc đổi schema cần migration riêng.

## Phạm vi triển khai hiện tại

Bản hiện tại dùng Firebase Spark cho xác thực, học liệu và dữ liệu đào tạo. Bằng chứng là liên kết HTTPS; các tính năng phụ thuộc Cloud Functions, AI trực tuyến hoặc upload Storage đang bị giới hạn theo cấu hình Spark. AI chỉ là hỗ trợ học tập, không phải điều kiện để luồng chính hoạt động và không quyết định nghiệm thu.

Không tuyên bố đây là hệ thống chính thức của một công ty cụ thể, có SSO/HR integration, phân quyền đa tổ chức hoặc AI hoạt động khi chưa triển khai. Xem [phạm vi triển khai](SPARK-0-PHI.md) và [giới hạn MVP](docs/enterprise-readiness.md).

Mọi thay đổi giao diện, dữ liệu và chức năng tiếp theo phải phục vụ luồng chính trên. Thay đổi định vị cần được chủ dự án yêu cầu rõ ràng.
