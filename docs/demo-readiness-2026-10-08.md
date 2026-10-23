# Kiểm tra và hoàn thiện luồng demo — 08/10/2026

## Lỗi từ bản quay

- Nộp bài báo chưa xác nhận được bằng chứng, trong khi mentor nhận được bài: code cũ gộp transaction ghi thành công và lần đọc xác nhận vào một kết quả. Lỗi đọc sau commit làm giao diện báo nộp thất bại.
- Sau yêu cầu bổ sung, timeline vẫn báo Chờ duyệt: kiểm tra có bằng chứng thay vì trạng thái PENDING.

## Thay đổi

- Chỉ báo gửi thành công sau khi transaction đã được Firestore xác nhận. Đọc lại receipt có thời gian tối đa 4 giây; mất mạng hoặc chưa có dữ liệu sau commit không biến ghi thành công thành thất bại. Mốc thời gian chưa biết được giữ là 0, không tự tạo giờ server; listener cập nhật dữ liệu xác nhận.
- Listener nhận thay đổi metadata, bỏ các snapshot còn pending hoặc từ cache. Timeline và danh sách nhiệm vụ dùng chung hàm tính trạng thái; yêu cầu bổ sung trở về nhiệm vụ cần làm, chặng sau vẫn khóa.
- Kết quả đọc chậm không ghi đè nhận xét/duyệt mới từ listener. Đăng xuất hủy request đang chạy, tránh cập nhật dữ liệu sang phiên khác.
- Tách nhận xét mentor thành nội dung đọc rõ; form nộp bài có yêu cầu thực hành và nhận xét bổ sung, trạng thái chờ duyệt, bàn phím cho URL và ẩn bàn phím khi gửi.
- Phản hồi gửi thành công rõ ràng; mentor giao bài/duyệt cũng không báo lỗi ghi chỉ vì lần đọc sau commit gặp lỗi.
- Bỏ thuật ngữ Firestore Rules khỏi thông báo đăng nhập cho người dùng.
- Giới hạn Gradle heap 2 GiB, 2 worker và không build song song để giảm áp lực RAM khi chạy emulator. Vẫn dừng daemon trước kiểm tra thủ công.

## Phạm vi

Firebase Spark, không bật billing và không đổi schema dữ liệu đào tạo. Mỗi chặng vẫn có một nhiệm vụ thực hành chính. Quiz là tự kiểm tra, chưa lưu lịch sử qua backend trên Spark; không dùng kết quả Quiz để mở chặng. Bản lưu bằng chứng giữ kết quả gần nhất của từng chặng, chưa phải lịch sử mọi lần nộp.

Tài khoản Aether đã ở 66% sau bản quay; không tự đặt lại hoặc nghiệm thu thêm cho tài khoản đó. Kiểm thử Firebase thật dùng tài khoản QA riêng, cấp phân công cho mentor hiện tại rồi dọn khi xong.

## Kết quả xác minh

- Build APK debug thành công; 167 kiểm thử logic đạt. Android lint: 0 lỗi, 24 cảnh báo còn lại.
- Xác nhận 15 tình huống Android giao diện/backend qua các lượt chạy. Lượt rộng ban đầu đạt 14/15; một regression test dùng nhầm ID production với dữ liệu preview đã được sửa, sau đó cả 5 tình huống EvidenceFlowTest chạy lại đều đạt trên bản cuối.
- LiveWorkflowTest dùng repository Android và Firebase Spark thật: giao nhiệm vụ, chặn nhảy chặng, nộp, yêu cầu bổ sung, nộp lại, duyệt và mở khóa lần lượt tới 100%; kiểm tra quyền mentor, chặn sửa bài đang chờ/đã đạt và chặn người học tự tăng tiến độ.
- Tài khoản QA và phân công tạm đã được dọn; đối chiếu tài khoản Aether vẫn giữ 2/3 chặng, 66%.
- Dừng Gradle daemon và cài APK cuối lên emulator. Kiểm tra thủ công đăng nhập, Lộ trình, mở/hủy form nộp Chặng 3, Nhiệm vụ và Cá nhân: số liệu đồng bộ 66%, 2 hoàn thành, 1 cần làm; nút nộp nằm trên dock. Không thấy crash/ANR trong log của lượt kiểm tra này.

Các kết quả này xác minh phạm vi đã kiểm tra; không thay thế kiểm thử tải, mọi kích thước thiết bị hoặc mọi điều kiện mất mạng.
