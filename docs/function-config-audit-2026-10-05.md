# EduVault — Kiểm tra tính năng và cấu hình, 05/10/2026

Đối chiếu code với Firebase thật của project `appeduvault`, theo [PRODUCT.md](../PRODUCT.md). Không thay đổi billing, cấp quyền admin hoặc sửa dữ liệu người dùng trong lần rà soát này.

## Cấu hình đã xác nhận

- Billing tắt, không liên kết tài khoản thanh toán. Debug và Release đều đặt `SPARK_ONLY=true`.
- Email/Password đã bật; Google provider đã bật. SHA-1 của debug keystore hiện tại khớp Android OAuth client trong `google-services.json`.
- Hai composite index cho `documents` và `notifications` đã triển khai, trạng thái `READY`.
- Source Firestore Rules trên project thật khớp với file Rules trong workspace.
- Chương trình `software-fresher-foundation` đang active, có ba chặng và cấu hình prerequisites.
- Có một Quiz `onboarding-security-check`, năm câu hỏi, gắn Chặng 1. Chưa có Quiz tương ứng cho Chặng 2/3.
- Tài khoản test không có custom claim `admin`. Trường role trong hồ sơ không đủ để vượt Firestore Rules.

## Ma trận tính năng

| Tính năng | Kết luận | Việc còn cần |
| --- | --- | --- |
| Đăng nhập/đăng ký email | Code và provider có sẵn; đăng nhập đã chạy thật ở lần QA trước | Kiểm tra đăng ký với tài khoản thử riêng nếu cần demo luồng mới |
| Google đăng nhập | Provider và SHA Debug phù hợp | Chưa hoàn tất OAuth tương tác trong lần rà soát này; thêm dấu vân tay khóa phát hành khi dùng APK ký khác |
| Quên/đổi mật khẩu | Gọi Firebase Auth thật, có xác thực liên kết và reauthentication | Chưa gửi email hay đổi mật khẩu thật; email reset mặc định mở trang web, chưa có app deep link tự nhận liên kết |
| Lộ trình/khóa chặng | Firestore thật; learner chỉ bắt đầu 0% và không tự tăng tiến độ | Hoàn thiện phía nghiệm thu để có luồng khép kín trong app |
| Nộp bằng chứng | Lưu link HTTPS, ghi chú, trạng thái PENDING và thời gian server; chỉ chặng đang mở được nộp | Người duyệt cần mở được liên kết; quyền chia sẻ GitHub/Drive do người nộp thiết lập |
| Nghiệm thu/mở chặng | Chưa có hàng đợi và thao tác duyệt/từ chối trong app; Console có thể quản lý bằng quyền chủ dự án | Thiếu code và phân quyền người duyệt, không chỉ thiếu một nút cấu hình. Cần trạng thái đạt/chưa đạt, nhận xét, cập nhật tiến độ và làm mới trạng thái người học |
| Quiz tự kiểm tra | Tải câu hỏi, làm bài và hiển thị kết quả | Lưu lượt làm bài gọi Functions và bị chặn trên Spark. Chưa có lịch sử bền vững cho kết quả mới; cần thiết kế lưu practice result riêng hoặc backend chấm/lưu, không cho Quiz tự mở chặng |
| Học liệu/tìm kiếm/lưu | Đọc nội dung đã duyệt và lưu danh sách vào hồ sơ | Nội dung Chặng 2/3 cần được rà về độ đầy đủ theo nhiệm vụ; hiện chỉ có một bộ Quiz |
| Ghi chú | Có đọc/lưu/xóa Firestore theo người dùng | Luồng lỗi đọc đã sửa để không mở ghi chú trống có nguy cơ ghi đè bản có sẵn; chưa thao tác lưu/xóa ghi chú thật trong audit |
| Hồ sơ/bảo mật | Họ tên, đơn vị và thông tin tài khoản lưu qua Auth/Firestore | Avatar chọn từ máy đang là tệp cục bộ: chưa đồng bộ ảnh sang thiết bị khác hoặc sau khi xóa dữ liệu app |
| Thông báo | Firestore, index sẵn sàng; có thông báo chào mừng | Chưa có FCM push và luồng thông báo mentor nghiệm thu tự động; không coi thông báo chào mừng là minh chứng cho các chức năng này |
| Quản trị nội dung/tài khoản | Có màn hình và một số truy vấn quản trị | Cần custom claim; duyệt/xóa/báo cáo/khóa tài khoản và quản lý danh mục hiện gọi Functions, không hoạt động đầy đủ trên cấu hình Spark |
| AI trực tuyến/upload Storage | Chủ động chặn theo cấu hình Spark | Tính năng bổ trợ; không cần bật billing để hoàn tất các luồng chính bằng Firestore và liên kết bằng chứng |

## Sửa trong lần rà soát

- Kết quả Quiz có trạng thái đang lưu/đã lưu/chưa lưu; không âm thầm bỏ qua lỗi lưu nữa. Nội dung kết quả và đáp án vẫn xem được khi backend không ghi nhận lượt làm bài.
- Chụp dữ liệu lượt làm bài trước khi gọi bất đồng bộ, hủy tác vụ lưu cũ khi bắt đầu bài khác hoặc thoát, tránh trạng thái lưu cũ xuất hiện ở phiên mới.
- Đọc ghi chú thất bại không đồng nghĩa ghi chú chưa tồn tại. Không mở bản trống khi đọc lỗi, để tránh ghi đè ghi chú đã lưu.

Build Debug thành công; 146 kiểm thử logic đạt, không có ca bỏ qua hoặc lỗi. Hai hồi quy mới kiểm tra lỗi lưu lượt làm Quiz và lỗi đọc ghi chú; không thực hiện nghiệm thu hay ghi đè dữ liệu cloud của người dùng trong audit.

## Ưu tiên tiếp theo

1. Hoàn thiện luồng người duyệt nghiệm thu bằng chứng và luồng người học nhận kết quả; đây là khoảng trống lớn nhất của nghiệp vụ chính.
2. Lưu lịch sử tự kiểm tra đúng phạm vi practice, gắn với người học và chặng; không dùng điểm client làm kết quả nghiệm thu chính thức.
3. Bổ sung Quiz/học liệu cho các chặng còn lại theo nhiệm vụ thực tế.
4. Kiểm thử Google OAuth, email reset, thay đổi hồ sơ và ghi chú thật bằng tài khoản thử riêng; cấu hình khóa ký/App Check đúng môi trường trước khi phát hành.

Các mục thiếu triển khai không thể khắc phục chỉ bằng bật provider hoặc cấp admin claim. Không cấp admin cho tài khoản người học để né lỗi quyền, không mở Rules cho mọi người và không bật Blaze trong audit này.

## Cập nhật sau khi triển khai mentor

Các dòng nghiệm thu/mentor ở ma trận trên mô tả thời điểm rà soát trước khi triển khai. Sau yêu cầu của chủ dự án, tài khoản mentor riêng, phân công học viên và nghiệm thu trong app đã được bổ sung trên Spark. Xem [báo cáo mentor](mentor-account-2026-10-05.md). Các hạn chế Quiz và Functions quản trị vẫn còn.
