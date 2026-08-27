Mức độ phức tạp phù hợp cho dự án này là **Mid-level hướng Production (Modular Monolith kết hợp Event-Driven)**. Với tech stack Spring Boot, PostgreSQL, Redis, Kafka và Docker, đây không còn là một project CRUD đơn thuần mà là một hệ thống chịu tải giả lập, đủ sức làm điểm nhấn mạnh trên CV cho vị trí Backend Developer.

**Cấu trúc tính năng cốt lõi (Core Features)**

- **Auth & User Module:** Đăng ký, đăng nhập, phân quyền (RBAC - Role-Based Access Control) sử dụng JWT, tích hợp Refresh Token lưu Redis.
- **Product & Catalog Module:** Quản lý danh mục, sản phẩm, tìm kiếm và phân trang.
    - *Điểm nhấn kỹ thuật:* Sử dụng **Redis Cache** để cache danh sách sản phẩm hot hoặc chi tiết sản phẩm nhằm giảm tải cho PostgreSQL.
- **Order & Checkout Module:** Giỏ hàng, tạo đơn hàng và thanh toán giả lập.
    - *Điểm nhấn kỹ thuật:* Xử lý **Kafka Event-Driven**. Khi đơn hàng được tạo thành công, bắn event `order.created` qua Kafka để các service/module khác xử lý bất đồng bộ (trừ tồn kho, gửi email xác nhận).
- **Inventory Module:** Quản lý kho hàng, xử lý tranh chấp khi nhiều người mua cùng lúc (Race condition / Pessimistic Locking hoặc Optimistic Locking).

**Ứng dụng Tech Stack thực tế**

- **Spring Boot:** Xây dựng các module tuân thủ Clean Architecture hoặc Layered Architecture rõ ràng.
- **PostgreSQL:** Thiết kế Database chuẩn hóa (Normalization), có đánh index (B-Tree, GIN) cho các trường tìm kiếm nhiều.
- **Redis:**
    - Lưu trữ Refresh Token (có TTL).
    - Cache Aside Pattern cho sản phẩm.
    - Distributed Lock (Redisson) để chống overselling khi flash sale.
- **Kafka:**
    - Producer phát sự kiện thanh toán thành công/tạo đơn hàng.
    - Consumer xử lý bất đồng bộ (gửi thông báo, cập nhật thống kê).
- **Docker & Docker Compose:** Đưa toàn bộ hệ thống (App, PostgreSQL, Redis, Kafka, Zookeeper) lên container để chạy một câu lệnh là start toàn bộ hệ thống.
- **Unit & Integration Tests:** Viết JUnit 5 + Mockito cho Service layer và Testcontainers cho Repository/Integration test (cực kỳ ghi điểm trong CV).