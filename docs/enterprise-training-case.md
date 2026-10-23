# EduVault — Bài toán đào tạo nhân sự mới

Định vị chính thức nằm tại [PRODUCT.md](../PRODUCT.md). Tài liệu này diễn giải bài toán bảo vệ đồ án theo định vị đó.

## Vấn đề cần giải quyết

Intern/fresher cần biết thứ tự học và đầu việc phải hoàn thành để sẵn sàng tham gia dự án. Người phụ trách cần bằng chứng thực hành và trạng thái nghiệm thu, thay vì chỉ đếm lượt đọc tài liệu hoặc điểm Quiz.

## Giải pháp

EduVault tổ chức một chương trình onboarding theo các chặng có điều kiện mở khóa. Mỗi chặng có học liệu, nhiệm vụ thực hành và bài tự kiểm tra. Người học nộp liên kết bằng chứng; người có quyền nghiệm thu quyết định đạt/chưa đạt và tiến độ.

Chương trình mẫu gồm:

1. Quy chuẩn làm việc và bảo mật thông tin.
2. Setup môi trường và Git.
3. Mock project và nghiệm thu.

Quiz giúp tự lượng giá kiến thức, không tự mở khóa chặng. Trạng thái chờ duyệt không đồng nghĩa đã hoàn thành. Nhiệm vụ và lộ trình dùng cùng nguồn dữ liệu.

## Giá trị cần đo

- Tỷ lệ chặng được nghiệm thu và số người hoàn thành chương trình.
- Thời gian xử lý bằng chứng và số lần cần bổ sung.
- Số nhiệm vụ cần làm, chờ duyệt và đã đạt.
- Nhóm kiến thức cần củng cố từ kết quả tự kiểm tra.

Đây là chỉ số đề xuất, không phải kết quả thử nghiệm đã được xác nhận.

## Phạm vi đồ án

Bản Spark dùng Firebase Authentication và Firestore thật để đọc học liệu, lưu hồ sơ, lộ trình và bằng chứng. Các chức năng AI trực tuyến, upload Storage hoặc quản trị qua Cloud Functions bị giới hạn trong cấu hình hiện tại. Mentor có tài khoản riêng và nghiệm thu bằng chứng cho học viên được phân công ngay trong app. Quyền dựa trên custom claim và phân công phía Firebase.

Dữ liệu demo minh họa Chặng 1 đã đạt, Chặng 2 đang làm, Chặng 3 khóa; tài khoản mới bắt đầu ở 0%. Không coi dữ liệu demo là lịch sử nghiệm thu thật.

Xem [giới hạn triển khai](enterprise-readiness.md) và [cấu hình Spark](../SPARK-0-PHI.md). Không trình bày EduVault như hệ thống chính thức của FPT hoặc một công ty cụ thể.
