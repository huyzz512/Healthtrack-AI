# Phân tích yêu cầu và đề xuất UI/UX Android

**Trạng thái:** Bản làm việc của Thành viên 1  
**Phạm vi:** Phân tích yêu cầu, UI/UX và triển khai/đánh giá phía Android theo phân công Thành viên 1
**Cập nhật:** 27-11-2026

Tài liệu này xác định phần việc Android của Thành viên 1. Đây là tài liệu phân tích và thiết kế sơ bộ, chưa phải phê duyệt triển khai các luồng nghiệp vụ UC-01 đến UC-20. Thành viên 2 đang hoàn thiện Foundation v0.1 theo thông tin phân công; Thành viên 1 không chỉnh sửa Backend, database, Swagger/OpenAPI hoặc contract chung.

## 1. Bối cảnh và căn cứ

### 1.1. Phạm vi Foundation

Foundation v0.1 đã được nghiệm thu VERIFIED/COMPLETE theo [trạng thái dự án](../governance/project-status.md). Theo phân công hiện hành, Thành viên 1 phụ trách Android UI/UX, cấu trúc Android, MVVM/điều hướng, tích hợp JWT phía ứng dụng, giao diện CRUD và Dashboard/biểu đồ; Thành viên 2 phụ trách REST API, xác thực phía máy chủ, persistence và các API CRUD/thống kê. Công việc Android chỉ tích hợp theo contract đã công bố, không thay đổi API dùng chung hoặc tự hoàn thiện toàn bộ UC-01 đến UC-20.

### 1.2. Tài liệu đã đối chiếu trong repository

- `AGENTS.md`, `android/AGENTS.md`: phân quyền sở hữu, kiến trúc và quy tắc Android.
- `docs/architecture/android-architecture.md`: Presentation → Domain → Data, MVVM/UDF và trạng thái UI.
- `docs/architecture/system-overview.md`: ranh giới Android/Backend và các hệ thống ngoài.
- `docs/api/integration-contract.md`, `docs/api/openapi.json`: contract Foundation hiện có.
- `docs/domain/use-case-map.md`, `docs/domain/data-provenance.md`: danh sách nhóm use case và nguồn gốc dữ liệu.
- `docs/security/security-baseline.md`: quyền sở hữu dữ liệu, consent và bảo vệ thông tin nhạy cảm.
- `docs/governance/project-status.md`, `docs/governance/open-questions.md`, `docs/governance/foundation-v0.1-acceptance.md`: trạng thái, câu hỏi mở và nghiệm thu được ghi trong repository.

### 1.3. Điểm cần đối chiếu về trạng thái dự án

Trạng thái hiện hành trong governance ghi Foundation v0.1 đã hoàn tất, OQ-012 đã được giải quyết và CI nền tảng đã chạy. Backend Phase 1 đã có implementation candidate; Android review và nghiệm thu chung theo CCR-002 vẫn đang chờ. Tài liệu Android này không tự đánh dấu phê duyệt thay cho hai bên.

Tài liệu phân tích nghiệp vụ đã duyệt v0.2 nằm ngoài repository; checksum và traceability được ghi trong kế hoạch Backend Phase 1. Không dùng bản đề xuất v0.1 làm căn cứ thay thế. Trước khi triển khai thêm use case, cần đối chiếu tài liệu đã duyệt và contract API hiện hành.

### 1.4. Phần Android đã bắt đầu triển khai

Dashboard tiếng Việt dùng nền `#F7F9FC`, xanh ngọc `#0F766E` và BottomBar Material 3. Trang Tổng quan có header ngày/thêm nhanh, thẻ AI insight, lưới 2×2 pastel cho Hoạt động, Tâm trạng, Dinh dưỡng, Giấc ngủ, nhắc uống nước và lối tắt. Tab Hôm nay có dải chọn ngày cùng thẻ chỉ số màu đỏ; các tab dưới gồm Tổng quan, Hôm nay, Nguồn, Hồ sơ. Các số bước, tâm trạng, calories, giấc ngủ, nước và lời khuyên AI ở Dashboard/Hôm nay là dữ liệu mẫu, có nhãn rõ; nút `+250ml` thay đổi tiến độ demo trên màn hình nhưng không ghi thành bản ghi sức khỏe. Các số này chỉ dùng trình diễn, không thay cho dữ liệu Backend/Health Connect.

Màn Tổng quan sử dụng `androidx.compose.material:material-icons-extended` theo yêu cầu dùng Material Icons Rounded/Outlined. Phương án thay thế là tự vẽ vector/Canvas hoặc ký tự Unicode; thư viện Compose chính thức được chọn để có bộ icon nhất quán và hỗ trợ accessibility. Thư viện không thêm quyền, dịch vụ mạng hoặc SDK bên thứ ba; đánh đổi là có thêm dependency/artifact ở build, nên chỉ import các icon được màn này sử dụng. Đây là dependency được thêm cho thiết kế Dashboard.

Room có migration v1→v2 cho bảng bản ghi cục bộ dùng chung. Hồ sơ sức khỏe, chỉ số cơ thể, dấu hiệu sinh tồn, dinh dưỡng và hoạt động hỗ trợ thêm, sửa, xóa bản ghi cục bộ; nhật ký cảm xúc hỗ trợ ghi nhận, xem lịch sử, sửa và xóa trên thiết bị. Những dữ liệu này chưa đồng bộ lên Backend. Theo UC-13 trong tài liệu đề xuất `Nhóm 1-HealthTrack_AL.docx`, cảm xúc có năm lựa chọn, ghi chú tùy chọn, validation bắt buộc và không đưa ra chẩn đoán.

Android đã tích hợp các endpoint Auth/Users trong CCR-002 candidate: đăng nhập, đăng ký/xác minh OTP, khôi phục/đặt lại mật khẩu, refresh/logout, tải hồ sơ và cập nhật họ tên. Access/refresh token được mã hóa AES-GCM bằng Android Keystore và lưu qua DataStore; refresh được tuần tự hóa phía client. Đây mới là tích hợp ứng viên, chưa phải nghiệm thu chung. OTP provider hiện là mock, không gửi hoặc tiết lộ mã thật nên không thể xác minh thủ công trọn luồng đăng ký/khôi phục.

OpenAPI hiện chỉ có API health, auth và users; chưa có API CRUD sức khỏe hoặc thống kê Dashboard. Vì vậy các CRUD sức khỏe chỉ lưu cục bộ và Dashboard/biểu đồ vẫn là dữ liệu trình diễn có nhãn, không phải dữ liệu máy chủ. Nhắc lịch hệ điều hành, Health Connect read/sync, tải tệp, PDF thật và hội thoại AI còn phụ thuộc API/provider hoặc tích hợp được duyệt; UI không được báo các thao tác đó đã hoàn thành khi chưa có dịch vụ tương ứng.

Debug Android mặc định dùng `http://127.0.0.1:3001/api/v1/`; có thể thay bằng `-PhealthtrackApiBaseUrl=...` hoặc `HEALTHTRACK_API_BASE_URL`. Với LDPlayer, cần cấu hình `adb reverse` theo hướng dẫn khởi chạy. HTTP cleartext chỉ được cho phép ở build debug. Build release vẫn cần URL HTTPS thật được bàn giao; fallback hiện là địa chỉ không định tuyến `api.invalid`.

### 1.5. Trạng thái theo phân công Thành viên 1

| Hạng mục | Trạng thái Android | Việc còn lại / phụ thuộc |
|---|---|---|
| UI/UX, Android project, MVVM và điều hướng | Có các màn hình Compose, ViewModel/UDF, điều hướng Dashboard/Auth/Account và giao diện CRUD cục bộ. | Tiếp tục theo use case được duyệt; không coi giao diện cục bộ là đồng bộ máy chủ. |
| Đăng nhập Android và JWT | Auth/Users API candidate được nối; token lưu mã hóa; refresh được tuần tự hóa; luồng khôi phục được kiểm thử trên thiết bị với danh tính tổng hợp. | Android review và nghiệm thu chung CCR-002 còn mở; chưa xác minh đăng nhập bằng tài khoản test hợp lệ hoặc nhận OTP thật. |
| CRUD giao diện | Các bản ghi sức khỏe hỗ trợ CRUD cục bộ trên Android. | API CRUD sức khỏe chưa có trong OpenAPI; cần Backend bàn giao contract trước khi tích hợp đồng bộ. |
| Dashboard và biểu đồ | Giao diện và biểu đồ tương tác đã có; dữ liệu mẫu được gắn nhãn. | API tổng hợp/thống kê chưa có; giữ nhãn demo, không trình bày dữ liệu mẫu như số liệu máy chủ. |
| Kiểm thử tích hợp và tài liệu Android | Build, unit test, lint; kiểm thử trên LDPlayer cho giao diện và luồng khôi phục mẫu; tài liệu Android được cập nhật. | Kiểm thử auth đầy đủ cần tài khoản hợp lệ và OTP provider có thể nhận mã; demo nghiệp vụ cần dữ liệu/API được duyệt. |

## 2. Phạm vi công việc của Thành viên 1

### Bao gồm

- Phân tích yêu cầu sử dụng ứng dụng Android, luồng màn hình và trạng thái giao diện.
- Thiết kế UI/UX, Compose UI, điều hướng, ViewModel và trạng thái Android.
- Android gọi API qua lớp Data/repository theo endpoint và DTO đã được hai bên thống nhất.
- Tích hợp JWT ở phía ứng dụng, lưu phiên an toàn và xử lý trạng thái phiên theo contract đã duyệt.
- Health Connect phía Android: khả dụng, xin quyền, đọc, ánh xạ, cache và lập lịch đồng bộ theo contract.
- Nhắc lịch cục bộ, quyền thông báo và phản hồi trạng thái lịch trên thiết bị.
- Tài liệu kiến trúc, giao diện và hướng dẫn sử dụng phần Android.

### Không bao gồm

- NestJS, REST API, application service, Prisma, PostgreSQL, ERD/migration, logic xác thực/phân quyền phía máy chủ.
- API thống kê, tạo báo cáo, provider AI/OTP/PDF/S3 và Swagger/OpenAPI.
- Tự đổi endpoint, DTO, enum, error code, đơn vị đo, quy ước ngày giờ, JWT policy hoặc giao thức đồng bộ.
- Tự đặt ngưỡng cảnh báo y tế, chẩn đoán, kê đơn hoặc hành vi cấp cứu.
- Hoàn thiện toàn bộ UC-01 đến UC-20 trong phạm vi Foundation.

## 3. Phân tích yêu cầu Android

| Hạng mục | Yêu cầu Android | Căn cứ / phụ thuộc | Tiêu chí phía Android |
|---|---|---|---|
| Nền tảng ứng dụng | Dùng Kotlin, Compose và cấu trúc package theo feature hiện có; không dựng lại nền tảng do Thành viên 2 đang hoàn thiện. | `android/AGENTS.md`, `android-architecture.md`, mã nguồn `android/`. | Phần UI mới đặt đúng feature; không làm thay đổi kiến trúc nền khi chưa thống nhất. |
| Kiến trúc màn hình | Presentation → Domain → Data; ViewModel giữ trạng thái và phát sự kiện; UI không gọi trực tiếp Retrofit, Room hoặc Health Connect. | Android architecture và quy tắc Android. | Mọi thao tác UI đi qua event/ViewModel/use case/repository phù hợp. |
| Điều hướng | Xác định điểm vào, đường đi, quay lại và trạng thái đăng nhập cho mỗi màn hình. | Navigation Foundation hiện có; yêu cầu nghiệp vụ cần đối chiếu. | Không có màn hình mồ côi; đích đến không hợp lệ được xử lý an toàn. |
| Trạng thái dùng chung | Thiết kế trạng thái đang tải, có dữ liệu, rỗng, lỗi, offline, không khả dụng và cần quyền khi phù hợp. | `LoadableState.kt`, `android/AGENTS.md`. | Mỗi màn hình có nội dung giải thích và hành động khôi phục phù hợp. |
| Đăng nhập/đăng ký | Form, validation, OTP, quên/đặt lại mật khẩu và trạng thái phiên; tích hợp Android theo API candidate đã công bố. | CCR-002/OpenAPI có Auth/Users Phase 1; nghiệm thu chung còn chờ, OTP provider vẫn là mock. | Không tự thêm trường; không ghi log password, OTP hoặc token; xử lý theo status/error code; xác minh luồng hồi phục theo đúng endpoint. |
| CRUD giao diện | Danh sách, chi tiết, tạo mới và chỉnh sửa theo từng nghiệp vụ được ưu tiên. | Use-case map; API/DTO nghiệp vụ cần được Backend cung cấp. | Trường và validation có căn cứ; lỗi server hiển thị rõ; phân biệt bản ghi nhập tay với bản ghi nhập từ thiết bị. |
| Dashboard/biểu đồ | Hiển thị dữ liệu tổng hợp do Backend cung cấp, đúng đơn vị và khoảng thời gian. | Contract quy định Backend tổng hợp, Android hiển thị. | Không tạo dữ liệu sức khỏe giả; dữ liệu thiếu thể hiện là thiếu, không thay bằng 0. |
| Health Connect | Trình bày trạng thái khả dụng/quyền; đọc, ánh xạ và cache trên Android; bảo toàn provenance. | ADR-004, contract và `data-provenance.md`; DTO đồng bộ cần duyệt chung. | Từ chối quyền không làm hỏng chức năng khác; không sửa bản ghi Health Connect thành dữ liệu nhập tay. |
| Nhắc lịch | Cho phép thao tác cấu hình theo yêu cầu; xử lý permission và lịch thông báo trên thiết bị. | Contract phân chia Backend lưu cấu hình, Android lập lịch cục bộ. | Phân biệt trạng thái đã lưu trên server với trạng thái thông báo trên thiết bị. |
| Hồ sơ y tế/tệp | Thiết kế chọn tệp, tiến trình, danh sách và lỗi khi API sẵn sàng. | Contract quy định PDF/JPG/JPEG/PNG, tối đa 10 MB, lưu trữ private. | Không lộ URL public hoặc thông tin S3; lỗi định dạng/dung lượng có hướng xử lý. |
| AI và consent | Thiết kế chế độ thông tin chung, xin consent trước khi gửi ngữ cảnh sức khỏe và nhãn nội dung AI. | Contract AI; Backend duy nhất gọi nhà cung cấp AI. | Ứng dụng không gọi provider; không gửi dữ liệu sức khỏe khi chưa có consent tương ứng. |
| Accessibility | Nhãn thao tác rõ, lỗi form gắn với trường, biểu đồ có mô tả văn bản và không truyền đạt ý nghĩa chỉ bằng màu. | Yêu cầu chất lượng UI Android; thương hiệu/ngôn ngữ còn cần chốt. | Luồng chính dùng được với công nghệ hỗ trợ; trạng thái quan trọng có biểu đạt ngoài màu sắc. |

## 4. Kiến trúc thông tin và luồng người dùng đề xuất

Đây là cấu trúc để thảo luận, chưa quyết định thứ tự ưu tiên sản phẩm.

```text
Mở ứng dụng
├── Chưa có phiên → Đăng nhập
│   ├── Tạo tài khoản → Nhập thông tin → Xác thực OTP
│   └── Quên mật khẩu → Đặt lại mật khẩu → Đăng nhập
└── Đã có phiên → Dashboard
    ├── Hồ sơ sức khỏe
    ├── Chỉ số cơ thể
    ├── Dấu hiệu sinh tồn
    ├── Hồ sơ y tế
    ├── Nhắc lịch
    ├── Dinh dưỡng
    ├── Tâm trạng
    ├── Hoạt động
    ├── Health Connect
    ├── Phân tích
    ├── Báo cáo
    └── Trợ lý AI / Consent
```

Ứng dụng hiện là Foundation shell và đang mở Dashboard. Việc đổi điểm vào theo trạng thái phiên chỉ thực hiện sau khi thống nhất với trạng thái Foundation và session lifecycle của contract. Không mặc định toàn bộ nhánh trên phải triển khai cùng lúc.

## 5. Phác thảo UI/UX

Phác thảo dạng chữ để xác định thành phần và hành vi chính. Màu sắc, thương hiệu, nội dung chính thức và trường nghiệp vụ cần được xác nhận trước khi làm giao diện hoàn chỉnh.

### 5.1. Đăng nhập

```text
┌────────────────────────────────┐
│           HealthTrack AI        │
│             Đăng nhập           │
│ [Email hoặc số điện thoại     ] │
│ [Mật khẩu                    ◉] │
│ [          Đăng nhập           ] │
│       Quên mật khẩu?             │
│ Chưa có tài khoản? Đăng ký       │
│                                  │
│ Lỗi xác thực / trạng thái tải    │
└────────────────────────────────┘
```

Email hay số điện thoại và quy tắc chuẩn hóa phải đối chiếu với phân tích nghiệp vụ. Nút gửi hiển thị tiến trình và chống gửi lặp. Lỗi ánh xạ bằng HTTP status/error code; không phân tích chuỗi message để quyết định logic.

### 5.2. Đăng ký và OTP

```text
Đăng ký → Thông tin tài khoản → Mật khẩu → Gửi đăng ký
        → Nhập OTP 6 số → Xác nhận → Thành công
```

Contract mô tả OTP 6 chữ số, thời hạn 5 phút, giới hạn nhập sai và quy tắc gửi lại. Giao diện cần thể hiện thời gian chờ và trạng thái nút gửi lại, nhưng không hiển thị hay ghi log OTP. Trường đăng ký và nội dung hướng dẫn mật khẩu cần đối chiếu tài liệu nghiệp vụ đã duyệt.

### 5.3. Dashboard

```text
┌────────────────────────────────┐
│ Xin chào / ngày đang xem         │
│ Tóm tắt chỉ số (dữ liệu API)    │
│ Biểu đồ xu hướng                 │
│ Bản ghi gần đây                  │
│ Thao tác nhanh                   │
│ Điều hướng đến các chức năng     │
└────────────────────────────────┘
```

Khi chưa có API tổng hợp, dùng trạng thái chưa có dữ liệu/chức năng chưa khả dụng. Không điền số liệu mẫu giống dữ liệu sức khỏe thật.

### 5.4. Mẫu màn hình CRUD

```text
Tên chức năng                 [+ Thêm]
Lọc / khoảng ngày (nếu được duyệt)
┌────────────────────────────────┐
│ Giá trị + đơn vị                │
│ Thời điểm · nguồn dữ liệu       │
│ Mở chi tiết                     │
└────────────────────────────────┘
Đang tải / Chưa có dữ liệu / Offline / Lỗi

Thêm hoặc chỉnh sửa:
  Trường và đơn vị đã được duyệt
  Lỗi nhập liệu tại từng trường
  [Hủy]                       [Lưu]
```

Mỗi feature cần danh sách trường riêng theo tài liệu nghiệp vụ và DTO đã công bố. Hiển thị nguồn `USER_INPUT`, `HEALTH_CONNECT`, `IMPORTED` hoặc `SYSTEM_GENERATED` khi phù hợp. Bản ghi nhập từ thiết bị không được trình bày như dữ liệu nhập tay có thể sửa.

### 5.5. Health Connect và nhắc lịch

- Health Connect: chưa khả dụng, cần cấp quyền, sẵn sàng, đang đồng bộ, lỗi đồng bộ; chỉ hiển thị lần đồng bộ cuối nếu hợp đồng có dữ liệu đó. Giải thích quyền trước khi yêu cầu và cho phép dùng các chức năng không phụ thuộc Health Connect khi người dùng từ chối.
- Nhắc lịch: biểu mẫu cấu hình → đã lưu → trạng thái quyền thông báo/lập lịch trên thiết bị. Thông báo rõ khi cấu hình đã lưu nhưng thiết bị chưa thể gửi nhắc.

### 5.6. Phân tích, báo cáo và AI

- **Phân tích:** bộ chọn khoảng thời gian, đơn vị, chú giải biểu đồ và trạng thái không có dữ liệu; biểu đồ có mô tả bằng văn bản.
- **Báo cáo:** chỉ thiết kế luồng yêu cầu/trạng thái/tải xuống theo endpoint và cơ chế truy cập đã được phát hành; không tự giả định thời gian hoàn tất.
- **Trợ lý AI:** tách chế độ hỏi thông tin chung khỏi chế độ dùng ngữ cảnh sức khỏe; xin consent trước; gắn nhãn AI và dùng nội dung an toàn đã được duyệt.

## 6. Quy tắc trải nghiệm dùng chung

- Duy trì nhất quán tiêu đề, nút hành động chính, khoảng cách, cách hiển thị lỗi và hành vi quay lại.
- Trạng thái nghiệp vụ nằm trong ViewModel/repository; trạng thái tạm thời thuần giao diện có thể nằm ở presentation.
- Thời điểm UTC hiển thị theo múi giờ thiết bị; ngày lịch giữ đúng ngữ nghĩa ngày, không tự dịch ngày do đổi múi giờ.
- Đơn vị đo hiển thị rõ; enum mới/không nhận biết phải có giá trị dự phòng, không làm ứng dụng crash.
- Phân biệt rõ danh sách rỗng, mất mạng, dữ liệu cache, dịch vụ không khả dụng và thiếu quyền.
- Không đưa token, OTP, mật khẩu hoặc payload y tế nhạy cảm vào log hay thông báo không cần thiết.

## 7. Trình tự làm Phần 1 và 2

1. Nhận và đọc tài liệu phân tích nghiệp vụ đã duyệt; đối chiếu trường dữ liệu, validation, luồng và nội dung thông báo.
2. Cùng hai thành viên xác nhận trạng thái Foundation đang ở Phase L hay đã đạt điều kiện nghiệm thu; ghi nhận một trạng thái thống nhất trong governance bởi Integration Owner.
3. Chọn use case cho đợt triển khai đầu tiên; không mặc định làm toàn bộ UC-01–UC-20.
4. Với từng use case được chọn, xác nhận endpoint/DTO đã sẵn sàng; ghi contract gap để xử lý chung, không tự dựng API.
5. Dựng wireframe/Compose preview cho đăng nhập, Dashboard và một luồng CRUD được ưu tiên; rà soát các trạng thái lỗi, rỗng, offline, quyền và accessibility.
6. Chốt checklist màn hình và tiêu chí nghiệm thu Android trước khi triển khai nghiệp vụ.

## 8. Câu hỏi còn mở

| Câu hỏi | Người cần phối hợp | Tác động |
|---|---|---|
| Tài liệu `HealthTrack_AI_Hoan_thien_phan_tich_v0.2.docx` đang ở đâu và có thể đưa vào kho tài liệu dự án không? | Chủ dự án | Chưa thể xác nhận đầy đủ trường, validation và luồng nghiệp vụ. |
| Trạng thái Foundation chính thức là đang ở Phase L hay đã VERIFIED/COMPLETE như một số tài liệu repo ghi? | Integration Owner + hai thành viên | Tránh nghiệm thu dựa trên trạng thái không thống nhất. |
| Đợt đầu ưu tiên những UC nào? | Chủ dự án + hai thành viên | Quyết định phạm vi wireframe và thứ tự triển khai. |
| API/DTO nghiệp vụ nào đã hoàn tất cho các UC được chọn? | Thành viên 2 cung cấp, Thành viên 1 rà soát | Quyết định khả năng tích hợp Android. |
| Quy tắc session khi mở app, refresh thất bại, đăng xuất hoặc khóa tài khoản là gì? | Hai thành viên; Backend sở hữu chính sách | Chốt điều hướng và thông báo xác thực phía Android. |
| Health Connect hỗ trợ loại dữ liệu và thiết bị nào trong đợt đầu? | Thành viên 1 + Thành viên 2 | Chốt quyền cần xin, nội dung đồng ý và trạng thái đồng bộ. |
| Bộ nhận diện, ngôn ngữ, nội dung tiếng Việt/Anh và yêu cầu accessibility nào bắt buộc? | Chủ dự án + Thành viên 1 | Hoàn thiện màu sắc, copy, semantics và localization. |

## 9. Điều kiện bàn giao sang triển khai Android

- [ ] Có tài liệu nghiệp vụ đã duyệt và yêu cầu màn hình truy vết được về tài liệu đó.
- [ ] Phạm vi use case của đợt đầu được thống nhất.
- [ ] Mỗi luồng đã chọn có API/DTO và owner rõ ràng; contract gap được ghi nhận.
- [ ] Màn hình xác định trạng thái loading, empty, error, offline/unavailable và permission khi phù hợp.
- [ ] Đơn vị, múi giờ, provenance, enum không nhận biết và consent được mô tả.
- [ ] Accessibility, nội dung tiếng Việt/Anh và trạng thái biểu đồ được rà soát.
- [ ] Không có thay đổi Backend hoặc shared contract đơn phương trong hạng mục Android.
