# EduVault — Phạm vi thực tế và giới hạn MVP

Tuân theo [định vị sản phẩm](../PRODUCT.md): đào tạo và đánh giá intern/fresher trong doanh nghiệp phần mềm. Không dùng tài liệu này để khẳng định chức năng chưa chạy trong bản hiện tại.

## Những phần hiện có

- Firebase Authentication và Firestore thật trên cấu hình Spark.
- Chương trình mẫu ba chặng, tiến độ và nhiệm vụ cùng nguồn dữ liệu.
- Chặng đầu tiên chưa hoàn thành là chặng đang mở; các chặng phía sau bị khóa.
- Nộp bằng chứng bằng liên kết HTTPS, lưu trạng thái chờ duyệt và đọc xác nhận từ server.
- Người học không được tự sửa tiến độ hoặc tự duyệt bằng chứng. Quyền cập nhật nghiệm thu phụ thuộc backend; có thể quản lý qua Firebase Console bằng tài khoản chủ dự án.
- Học liệu đã kiểm duyệt, ghi chú, hồ sơ và Quiz tự kiểm tra. Quiz không quyết định mở khóa chặng.
- Mentor có tài khoản riêng, đọc học viên được phân công, duyệt hoặc yêu cầu bổ sung bằng chứng; người học nhận thay đổi qua listener Firestore.
- Giao diện quản trị xử lý thiếu quyền; trường role trong hồ sơ không thay thế Firebase custom claims.

## Giới hạn phải trình bày rõ

| Chủ đề | Bản hiện tại | Hướng phát triển |
| --- | --- | --- |
| Danh tính | Có tự đăng ký cho demo | Tài khoản được mời/cấp, SSO theo tổ chức |
| Chương trình | Một chương trình mẫu | Gán chương trình theo nhóm và đợt |
| Mentor | Tài khoản riêng, học viên được giao và nghiệm thu trong app | Quản lý phân công, lịch sử nhiều lần và báo cáo theo đợt |
| Quản trị | Một số thao tác cần Functions/custom claims; tài khoản test có thể thiếu quyền | Hoàn thiện luồng quản trị theo quyền thực tế |
| Bằng chứng | Link HTTPS tới GitHub/Drive có quyền chia sẻ phù hợp | Lưu tệp có kiểm soát và lịch sử sửa đổi |
| AI/Storage | Bị giới hạn ở cấu hình Spark hiện tại | Bổ trợ tùy cấu hình và chính sách dữ liệu; không thay mentor |
| Tổ chức | Chưa có phân tách nhiều doanh nghiệp | Tenant, phạm vi chương trình và phân quyền theo tổ chức |
| Tiến độ demo | Kịch bản minh họa, không phải nghiệm thu thật | Ghi nhận sự kiện duyệt có người thực hiện và thời gian |

## Câu trả lời khi bảo vệ

**EduVault dùng để làm gì?** Đưa intern/fresher đi qua chương trình onboarding có kiểm soát, từ học liệu và nhiệm vụ đến bằng chứng thực hành và nghiệm thu trước khi tham gia dự án.

**Điểm Quiz cao có tự hoàn thành chặng không?** Không. Quiz phục vụ tự kiểm tra; đạt chặng phụ thuộc nghiệm thu bằng chứng thực hành.

**AI có thay mentor không?** Không. AI là hỗ trợ học tập tùy cấu hình; luồng chính không phụ thuộc AI. Bản Spark hiện tại không cung cấp AI trực tuyến.

**Đây có phải hệ thống của FPT không?** Không. Đây là đồ án giải quyết bài toán chung của doanh nghiệp phần mềm.

**33% là tiến độ thật hay demo?** Phần trăm được hiển thị từ dữ liệu lộ trình. Tài khoản minh họa được seed Chặng 1 đã đạt; phải nói rõ đó là kịch bản demo. Tài khoản mới bắt đầu từ Chặng 1 ở 0%.

Xem [cấu hình Spark](../SPARK-0-PHI.md) và [báo cáo kiểm tra UI](ui-audit-2026-10-05.md).
