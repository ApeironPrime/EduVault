# EduVault — Tài khoản mentor, 05/10/2026

## Tài khoản và phạm vi

Đã cấu hình tài khoản Firebase `mentor@gmail.com` theo email và mật khẩu chủ dự án cung cấp. Mật khẩu không được lưu trong repository. Tài khoản có custom claim `mentor: true`, không có claim admin.

Mentor được phân công người học hiện tại; tiến độ demo 33% được giữ nguyên. Đăng nhập bằng email/mật khẩu trong EduVault. Nếu tài khoản đã đăng nhập trước khi được cấp quyền, đăng xuất và đăng nhập lại để nhận token mới.

## Chức năng

- Đăng nhập vào màn hình mentor riêng thay vì lộ trình dành cho người học.
- Xem học viên được giao, chương trình, chặng đang mở và bằng chứng.
- Mở liên kết bằng chứng, nghiệm thu đạt hoặc yêu cầu bổ sung với nhận xét.
- Xác nhận nghiệm thu trong transaction; chặn duyệt dữ liệu đã được thay thế hoặc đã được xử lý.
- Đạt một chặng mở chặng tiếp theo; yêu cầu bổ sung không tăng tiến độ và người học có thể nộp lại.
- Người học nhận thay đổi qua listener Firestore, không phải đăng xuất để cập nhật tiến độ.
- Đăng xuất tài khoản mentor trong app.

## Phân quyền

`mentorAssignments/{mentorUid}/learners/{learnerUid}` chứa phân công và `programId`. Mentor chỉ liệt kê phân công của mình, đọc đúng học viên/chương trình được giao. Client không được tạo hoặc sửa phân công.

Rules kiểm tra claim mentor, tình trạng tài khoản, phân công, bằng chứng PENDING, chặng/prerequisites, người duyệt, thời gian server và phần trăm hoàn thành. Người học không tự cấp quyền hoặc tự nghiệm thu. Mentor không tự duyệt mình, không quản trị toàn bộ người dùng và không sửa link/nội dung bằng chứng khi duyệt.

Hồ sơ vẫn giữ trường role để tương thích dữ liệu; ứng dụng lấy vai trò thực tế từ Firebase ID token. Khi thu hồi claim cần thu hồi phiên/token theo quy trình quản trị Firebase; khóa mentor trong hồ sơ cũng làm Rules từ chối truy cập.

## Vận hành

`firebase/scripts/provision-mentor.cjs` dùng phiên đăng nhập CLI của chủ dự án và ba biến môi trường `EDUVAULT_MENTOR_EMAIL`, `EDUVAULT_MENTOR_PASSWORD`, `EDUVAULT_LEARNER_UID`. Script không thay mật khẩu của tài khoản đã có; tài khoản đã có phải xác thực thành công bằng mật khẩu được cung cấp. Script giữ các claims khác, từ chối dùng tài khoản admin hoặc dùng chính tài khoản người học làm mentor.

`firebase/scripts/verify-mentor-client.mjs` dùng một tài khoản kiểm thử tạm riêng để xác nhận claim, phạm vi truy cập, yêu cầu bổ sung, nộp lại, duyệt ba chặng và hoàn thành 100% trên Firebase thật. Sau kiểm tra, tài khoản và dữ liệu tạm được dọn. Không dùng điểm Quiz để mở chặng.

Không bật billing hoặc Cloud Functions; luồng này dùng Firebase Authentication và Firestore trên Spark. Bản hiện tại vẫn là một chương trình mẫu; phân công do chủ dự án cấu hình, chưa có màn hình quản lý phân công hoặc lịch sử nhiều lần nghiệm thu.

## Kết quả kiểm tra

- Build debug thành công; 152 kiểm thử unit, 20 kiểm thử Firestore Rules và 7 kiểm thử giao diện đều đạt. Lint không có lỗi, còn 24 cảnh báo.
- Kiểm tra trên Firebase thật đạt: từ chối truy cập ngoài phân công, yêu cầu bổ sung, nộp lại, chặn duyệt bằng chứng cũ và nghiệm thu 3 chặng đến 100%. Đã dọn tài khoản/dữ liệu kiểm thử tạm.
- Đã cài APK cuối và đăng nhập trực tiếp bằng tài khoản mentor trên emulator: mở đúng màn Nghiệm thu đào tạo, thấy Aether ở 33%, 1/3 chặng đã đạt và 0 bằng chứng chờ nghiệm thu. Khi người học nộp bằng chứng, tải lại danh sách để xem và duyệt.
- Log gần thời điểm đăng nhập không ghi nhận crash hoặc ANR mới. Emulator được để ở màn mentor để kiểm tra tiếp.


## Bổ sung quản lý nhiệm vụ theo chặng

Màn mentor có ba mục Học viên, Nhiệm vụ và Bài nộp. Mentor chọn học viên được phân công, soạn hoặc sửa nhiệm vụ của từng chặng: tên, yêu cầu/tiêu chí nghiệm thu, hạn nộp dd/MM/yyyy và liên kết HTTPS tới học liệu. Lưu sẽ giao cho đúng học viên; người học nhận nội dung qua listener ở cả Lộ trình và Nhiệm vụ. Có xác nhận khi khôi phục nhiệm vụ mặc định.

Mô hình hiện tại có một nhiệm vụ thực hành chính cho mỗi chặng; phần yêu cầu có thể chứa nhiều đầu việc/tiêu chí. Chưa hỗ trợ danh sách nhiều task độc lập trong cùng chặng hoặc giao hàng loạt. Không thay đổi chương trình hay tiến độ chung khi sửa đề. Task riêng lưu thêm trong `taskDefinitions` của assignment, tương thích dữ liệu cũ. Hạn nộp dùng để theo dõi, không chặn nộp muộn.

Chặn đổi nhiệm vụ đã nghiệm thu hoặc đang có bài nộp PENDING, cả trong app và Rules. Cho phép mentor soạn trước task của chặng chưa mở. Người học không được sửa task, mentor ngoài phân công không được ghi dữ liệu. Transaction đọc lại dữ liệu để phát hiện thay đổi khi đang mở form.

Khi nghiệm thu, lưu bản bằng chứng/nhận xét gần nhất của từng chặng vào `reviewedEvidence`; bài của chặng trước vẫn xem được khi chuyển chặng. Đây chưa phải lịch sử tất cả lần nộp. Dữ liệu nghiệm thu trước khi có chức năng này chưa có bản lưu để phục hồi; không tự tạo bằng chứng giả. Tab Bài nộp cho xem link, ghi chú và nhận xét; người học cũng xem lại link đã nghiệm thu từ tab Nhiệm vụ.


Kiểm tra phần bổ sung: 159 unit test và 25 kiểm thử Rules đạt; bộ 8 kiểm thử giao diện đạt, sau sửa tương thích bộ kiểm tra ngày cho Android API 24/25 đã chạy lại 2 kiểm thử mentor đạt. Build cuối thành công, lint 0 lỗi/24 cảnh báo. Rules đã triển khai lên dự án `appeduvault`. Script kiểm tra Firebase thật đã xác nhận tạo/sửa/khôi phục nhiệm vụ, bảo vệ task đang chờ duyệt/đã đạt, quyền truy cập, nộp lại và lưu bằng chứng qua ba chặng; tài khoản tạm đã dọn, Aether giữ 33%.

Máy gần hết RAM trong lúc kiểm thử đồng thời Gradle và emulator; sau kiểm tra đã dừng daemon và tiến trình Firestore emulator cục bộ. Hộp thoại hệ thống không phản hồi còn hiện khi mở thủ công; `dumpsys activity lastanr` ghi nhận Pixel Launcher. Khởi động lại emulator sau khi giải phóng RAM để kiểm tra app.

Sau khởi động lại, đã đăng nhập trực tiếp mentor@gmail.com vào bản cuối: thấy Học viên/Nhiệm vụ/Bài nộp, Aether 33%, Chặng 1 không cho đổi đề và Chặng 2/3 có nút soạn. Đã mở form Chặng 2 và xác nhận đủ tên, yêu cầu/tiêu chí, hạn nộp, học liệu, nút Lưu & giao nhiệm vụ. Không lưu thay đổi vào task của Aether trong kiểm tra thủ công này; emulator để ở form để chủ dự án xem.
