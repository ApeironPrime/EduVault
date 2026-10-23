# EduVault — Firebase thật trên Spark

Bản Debug và Release hiện dùng project `appeduvault` thật. Không cần Firebase Emulator khi mở app. Cấu hình và script demo cục bộ cũ đã được bỏ; Emulator vẫn dùng riêng cho kiểm thử Rules.

## Những chức năng dùng cloud thật

- Đăng nhập/đăng ký bằng Firebase Authentication, bao gồm tài khoản Firebase thật đã có.
- Đọc chương trình tại `trainingPrograms/software-fresher-foundation`.
- Tạo lộ trình 0% cho người dùng chưa có lộ trình; giữ nguyên tiến độ hiện có.
- Nộp link HTTPS và ghi chú; lưu trạng thái chờ duyệt, thời gian do Firestore xác nhận.
- Đọc lại bằng chứng từ server sau khi lưu. Không báo thành công khi chưa được server xác nhận.
- Hồ sơ, ghi chú, thông báo và học liệu có sẵn vẫn đọc từ Firestore theo quyền truy cập hiện tại.

Trong Firebase Console, mở Firestore > users > UID của người dùng > trainingAssignments > software-fresher-foundation. Khi nộp bằng chứng, trường `evidence` thay đổi trực tiếp trên cloud. Đây là dữ liệu thật, không phải dữ liệu lưu trên máy.

## Quyền và nghiệm thu

Người học chỉ được tạo lộ trình với tiến độ 0% và cập nhật bằng chứng của mình. Không được tự sửa `completedModuleIds`, `completionPercent` hay tự duyệt bằng chứng. Quản trị có custom claim `admin` có thể cập nhật tiến độ; chủ dự án cũng có thể quản lý trực tiếp qua Firebase Console. Màn hình app hiển thị chặng và trạng thái theo dữ liệu được lưu.

Chặng đang mở là chặng đầu tiên chưa hoàn thành. Các chặng phía sau có ổ khóa và không nhận bằng chứng, kể cả khi gửi yêu cầu trực tiếp lên Firestore. `modulePrerequisites` trong chương trình xác định các chặng cần được mentor duyệt; việc gửi bằng chứng trạng thái `PENDING` không mở khóa chặng tiếp theo. Nhiệm vụ và hộp thoại lấy đúng chặng đang mở, không gắn cứng Chặng 2.

Tài khoản test `hanryit6012@gmail.com` được đặt theo kịch bản demo Chặng 2 bằng `node firebase/scripts/seed-stage-two-demo.cjs`: Chặng 1 đã đạt, 33%, Chặng 2 đang làm, Chặng 3 khóa. Dữ liệu có nhãn `demoScenario`, được mô tả là dữ liệu demo trong tài liệu và có bản sao lưu; không phải một sự kiện nghiệm thu thực tế. Tài khoản mới vẫn bắt đầu ở Chặng 1 với 0%.

## Giới hạn bản Spark

- AI trực tuyến, upload file lên Cloud Storage và các thao tác cần Cloud Functions bị chặn với thông báo rõ ràng.
- Bằng chứng dùng link GitHub/Drive HTTPS có quyền chia sẻ phù hợp.
- App không yêu cầu bật Blaze hoặc trả khoản kích hoạt 800.000đ.
- Giữ project trên Spark để không có thanh toán theo lượt sử dụng; vượt hạn mức miễn phí có thể khiến chức năng tạm ngừng. Bản app không tự đổi gói billing của project.

## Tài khoản mentor

Mentor đăng nhập bằng tài khoản Firebase riêng, nhận custom claim `mentor: true`; không có quyền admin. Phân công nằm tại `mentorAssignments/{mentorUid}/learners/{learnerUid}` với `programId`. Người dùng không thể tự tạo phân công hoặc tự sửa vai trò.

Mentor chỉ đọc hồ sơ/lộ trình của học viên được giao và nghiệm thu bằng chứng PENDING. Nghiệm thu đạt tăng tiến độ đúng một chặng; yêu cầu bổ sung giữ tiến độ và ghi nhận xét bắt buộc. Firestore transaction và Rules chặn thay bằng chứng, duyệt lại, tự duyệt và mở vượt chặng. Người học nhận trạng thái mới qua listener Firestore.

Chủ dự án dùng `firebase/scripts/provision-mentor.cjs` với biến môi trường email, mật khẩu và UID học viên để tạo/cấp quyền. Mật khẩu không được lưu vào script hoặc tài liệu. Chi tiết nằm ở [mentor-account-2026-10-05.md](docs/mentor-account-2026-10-05.md).

## Thiết lập lại backend

```powershell
.\firebase\node_modules\.bin\firebase.cmd deploy --only 'firestore:rules,firestore:indexes' --project appeduvault
node firebase/scripts/seed-spark-training.cjs
```

Script tạo chương trình nếu chưa có; nếu đã có thì chỉ bổ sung danh sách ID và quan hệ chặng trước phục vụ kiểm tra quyền, không sửa tiến độ/bằng chứng của người dùng. Không triển khai Functions hoặc Storage.

## Kiểm tra

Rules được kiểm tra với Emulator. Kiểm thử Android trực tuyến chỉ chạy khi chủ động bật `liveSpark=true`; dùng tài khoản riêng có nhãn “Kiểm thử Spark” và xóa tài khoản Auth sau khi chạy. Dọn hồ sơ thử nghiệm bằng `node firebase/scripts/spark-maintenance.cjs --cleanup-verification`.

Đã qua 12 kiểm tra Rules, 8 kiểm tra TrainingViewModel và luồng Firebase client SDK trực tuyến (`node firebase/scripts/verify-spark-client.mjs`). Ngày 04/10/2026, kiểm thử Android trực tuyến `LiveSparkBackendTest` đã chạy thành công trên máy ảo: tải chương trình, tạo lộ trình, lưu bằng chứng và đọc lại từ Firestore thật; không tự tăng tiến độ.

Project đã gỡ liên kết billing. Script `node firebase/scripts/spark-maintenance.cjs` xác nhận billing không bật và không còn tài khoản billing liên kết. Tài khoản Google Cloud Billing riêng của bạn không bị đóng hay xóa.

Ngày 04/10/2026 đã sửa hồ sơ cũ thiếu `isBlocked` (nguyên nhân không đăng ký được lộ trình), lưu trữ tài liệu sinh viên cũ và thay bằng ba bài đọc Fresher trên Firestore. Các thông báo mẫu cũ được sao lưu trước khi dọn; bản sao ở `.firebase-backups/`, được loại khỏi Git. Không thay vai trò, credit hay tiến độ nghiệm thu của người dùng.

Script `node firebase/scripts/migrate-fresher.cjs` chỉ hiển thị số lượng cần sửa. Thêm `--apply` để sao lưu và áp dụng; không mở quyền toàn bộ database. Hồ sơ bị khóa vẫn giữ nguyên trạng thái khóa. Tài liệu cũ chuyển sang `ARCHIVED`, không đổi tên một PDF sinh viên thành tài liệu doanh nghiệp.

Sau đợt dọn code ngày 04/10/2026: bỏ các UI trang chủ cũ không được gọi, nhánh kết nối Emulator trong app và callback tự đánh dấu hoàn thành; chuyển dữ liệu preview ra khỏi ViewModel. Thông báo và nhật ký dùng chung một lần đọc; trang chủ không tải danh mục không hiển thị. Đã qua 134 kiểm thử đơn vị, 3 kiểm thử Android (gồm Firebase thật), build Debug/Release và Lint với 0 lỗi, 25 cảnh báo. Tài khoản thực tế đã đăng nhập và tải đủ ba chặng sau khi cài lại bản cuối.

Sau sửa khóa chặng: 135 kiểm thử đơn vị, 14 kiểm thử Rules và 4 kiểm thử Android đã qua; kiểm tra Firebase SDK trực tuyến xác nhận gửi vượt chặng bị từ chối. Rules đã triển khai, bản Debug cuối đã cài và kiểm tra bằng tài khoản demo 33%. Tiêu đề nhật ký là “Bắt đầu chương trình Onboarding”.
