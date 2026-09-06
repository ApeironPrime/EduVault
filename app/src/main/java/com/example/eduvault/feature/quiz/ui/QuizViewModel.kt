package com.example.eduvault.feature.quiz.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.eduvault.domain.repository.AuthRepository
import com.example.eduvault.domain.usecase.GetPublishedDocumentsUseCase
import com.example.eduvault.domain.repository.DocumentRepository
import com.example.eduvault.domain.repository.AiRepository
import com.example.eduvault.domain.repository.NotificationRepository
import com.example.eduvault.domain.model.NotificationType
import javax.inject.Inject
import com.example.eduvault.feature.library.ui.toLibraryDoc

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getPublishedDocuments: GetPublishedDocumentsUseCase,
    private val documentRepository: DocumentRepository,
    private val aiRepository: AiRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _availableDocs = MutableStateFlow<List<com.example.eduvault.feature.library.ui.LibraryDoc>>(emptyList())
    val availableDocs: StateFlow<List<com.example.eduvault.feature.library.ui.LibraryDoc>> = _availableDocs.asStateFlow()

    private val allQuizSetsList = mutableListOf<QuizSet>()
    private val localQuestionsCache = mutableMapOf<String, List<com.example.eduvault.feature.quiz.ui.QuizQuestion>>()


    private var timerJob: Job? = null



    private val microQuestions = listOf(
        QuizQuestion(
            id = "q1_1",
            text = "Đường cầu của một hàng hóa thường dốc xuống dưới từ trái sang phải là do:",
            hint = "Gợi ý: Hãy nghĩ tới mức độ thỏa mãn tăng thêm khi mua thêm một sản phẩm.",
            options = listOf(
                QuizAnswer("A", "Quy luật lợi ích cận biên giảm dần"),
                QuizAnswer("B", "Thu nhập của người tiêu dùng ngày càng tăng"),
                QuizAnswer("C", "Giá cả hàng hóa thay thế tăng lên"),
                QuizAnswer("D", "Chi phí sản xuất biên ngày càng tăng")
            ),
            correctOption = "A",
            explanation = "Theo quy luật lợi ích cận biên giảm dần, khi tiêu dùng thêm một đơn vị sản phẩm, mức độ thỏa dụng tăng thêm (lợi ích cận biên) sẽ giảm đi. Do đó, người tiêu dùng chỉ sẵn sàng mua thêm sản phẩm tiếp theo ở mức giá thấp hơn."
        ),
        QuizQuestion(
            id = "q1_2",
            text = "Khi giá của hàng hóa X tăng lên làm giảm lượng cầu đối với hàng hóa Y thì X và Y là:",
            hint = "Gợi ý: Cầu giảm khi giá của sản phẩm đi kèm tăng.",
            options = listOf(
                QuizAnswer("A", "Hai hàng hóa thay thế nhau"),
                QuizAnswer("B", "Hai hàng hóa bổ sung cho nhau"),
                QuizAnswer("C", "Hai hàng hóa độc lập hoàn toàn"),
                QuizAnswer("D", "Hai hàng hóa thứ cấp")
            ),
            correctOption = "B",
            explanation = "Hai hàng hóa là bổ sung nếu việc tăng giá của một hàng hóa kéo theo việc giảm lượng cầu của hàng hóa kia (do chúng thường được tiêu dùng cùng nhau, ví dụ: xăng và xe máy)."
        ),
        QuizQuestion(
            id = "q1_3",
            text = "Trạng thái cân bằng của thị trường đạt được khi nào?",
            hint = "Gợi ý: Tại mức giá cân bằng, cung gặp cầu.",
            options = listOf(
                QuizAnswer("A", "Lượng cung đúng bằng lượng cầu tại một mức giá xác định"),
                QuizAnswer("B", "Mọi người bán đều bán hết sạch hàng hóa mang đi"),
                QuizAnswer("C", "Mức giá bán ra đạt trạng thái thấp nhất lịch sử"),
                QuizAnswer("D", "Chính phủ áp đặt mức giá trần bắt buộc")
            ),
            correctOption = "A",
            explanation = "Trạng thái cân bằng thị trường xảy ra tại điểm giao nhau giữa đường cung và đường cầu, nghĩa là lượng người bán muốn bán đúng bằng lượng người mua muốn mua tại mức giá cân bằng thị trường."
        ),
        QuizQuestion(
            id = "q1_4",
            text = "Hệ số co giãn của cầu theo giá đối với một hàng hóa là -2 có nghĩa là gì?",
            hint = "Gợi ý: Xem xét tỷ lệ phần trăm thay đổi của lượng cầu so với giá.",
            options = listOf(
                QuizAnswer("A", "Giá hàng hóa tăng 10% làm cho lượng cầu giảm 20%"),
                QuizAnswer("B", "Lượng cầu hàng hóa tăng 10% làm cho giá giảm 20%"),
                QuizAnswer("C", "Giá hàng hóa giảm 10% làm cho lượng cầu giảm 20%"),
                QuizAnswer("D", "Giá hàng hóa tăng 10% làm cho lượng cầu tăng 20%")
            ),
            correctOption = "A",
            explanation = "Hệ số co giãn cầu theo giá phản ánh tốc độ thay đổi lượng cầu khi giá biến động. Với hệ số bằng -2 (trị tuyệt đối bằng 2), giá tăng 10% sẽ làm cầu giảm đi 10% * 2 = 20%."
        ),
        QuizQuestion(
            id = "q1_5",
            text = "Một doanh nghiệp tối đa hóa lợi nhuận của mình khi sản xuất ở mức sản lượng nào?",
            hint = "Gợi ý: So sánh doanh thu thu thêm và chi phí bỏ thêm cho đơn vị sản phẩm cuối cùng.",
            options = listOf(
                QuizAnswer("A", "Doanh thu biên bằng chi phí biên (MR = MC)"),
                QuizAnswer("B", "Doanh thu tổng thể đạt trị số lớn nhất"),
                QuizAnswer("C", "Tổng chi phí sản xuất được cắt giảm tối đa"),
                QuizAnswer("D", "Giá bán hàng hóa được đẩy lên cao nhất")
            ),
            correctOption = "A",
            explanation = "Nguyên lý tối đa hóa lợi nhuận của doanh nghiệp (cho cả cạnh tranh hoàn hảo lẫn độc quyền) yêu cầu sản xuất tại điểm mà Doanh thu cận biên (MR) bằng Chi phí cận biên (MC). Nếu sản xuất nhiều hơn hoặc ít hơn điểm này, lợi nhuận đều sẽ bị giảm sút."
        )
    )

    // Bộ câu hỏi cho môn Marketing (id = "2")
    private val marketingQuestions = listOf(
        QuizQuestion(
            id = "q2_1",
            text = "Bước đầu tiên và quan trọng nhất trong quá trình nghiên cứu Marketing là gì?",
            hint = "Gợi ý: Phải biết rõ mình cần tìm câu trả lời cho vấn đề gì trước đã.",
            options = listOf(
                QuizAnswer("A", "Xác định rõ ràng vấn đề và mục tiêu nghiên cứu cụ thể"),
                QuizAnswer("B", "Xây dựng kế hoạch thu thập thông tin"),
                QuizAnswer("C", "Triển khai thu thập dữ liệu trên diện rộng"),
                QuizAnswer("D", "Phân tích số liệu và viết báo cáo kết quả")
            ),
            correctOption = "A",
            explanation = "Xác định chính xác vấn đề và mục tiêu giúp tiết kiệm ngân sách, định hướng đúng đắn tất cả các khâu sau này của dự án nghiên cứu nghiên cứu marketing."
        ),
        QuizQuestion(
            id = "q2_2",
            text = "Hệ thống Marketing hỗn hợp truyền thống (4P Mix) bao gồm các yếu tố nào?",
            hint = "Gợi ý: Bốn trụ cột bán hàng cốt lõi.",
            options = listOf(
                QuizAnswer("A", "Product, Price, Place, Promotion"),
                QuizAnswer("B", "People, Price, Place, Promotion"),
                QuizAnswer("C", "Product, Price, Position, Promotion"),
                QuizAnswer("D", "Product, Packaging, Place, Promotion")
            ),
            correctOption = "A",
            explanation = "Mô hình 4P cổ điển đề xuất bởi McCarthy bao gồm: Product (Sản phẩm), Price (Giá cả), Place (Phân phối), và Promotion (Chiêu thị / Xúc tiến bán hàng)."
        ),
        QuizQuestion(
            id = "q2_3",
            text = "Thuật ngữ 'Phân khúc thị trường' (Market Segmentation) được hiểu là:",
            hint = "Gợi ý: Chia nhỏ thị trường lớn thành các nhóm nhỏ tương đồng.",
            options = listOf(
                QuizAnswer("A", "Chia thị trường lớn không đồng nhất thành các nhóm nhỏ đồng nhất hơn về nhu cầu"),
                QuizAnswer("B", "Tập trung truyền thông sản phẩm đến tất cả mọi tầng lớp nhân dân"),
                QuizAnswer("C", "Lựa chọn một tệp khách hàng mục tiêu duy nhất để kinh doanh"),
                QuizAnswer("D", "Định vị hình ảnh thương hiệu trong tâm trí người dùng")
            ),
            correctOption = "A",
            explanation = "Phân khúc thị trường là bước phân chia một thị trường không đồng nhất thành các phần thị trường (phân khúc) đồng nhất hơn về mặt nhân khẩu, địa lý hoặc hành vi để có thể thiết kế các chiến dịch phù hợp."
        ),
        QuizQuestion(
            id = "q2_4",
            text = "Một chu kỳ sống điển hình của sản phẩm (Product Life Cycle) diễn ra theo trật tự nào?",
            hint = "Gợi ý: Từ khi thai nghén ra mắt đến lúc kết thúc.",
            options = listOf(
                QuizAnswer("A", "Triển khai ra mắt, Tăng trưởng nhanh, Bão hòa doanh số, Suy thoái"),
                QuizAnswer("B", "Hình thành ý tưởng, Nghiên cứu thử nghiệm, Sản xuất đại trà, Bán hàng"),
                QuizAnswer("C", "Tăng trưởng nóng, Triển khai thử nghiệm, Bão hòa, Băng hoại"),
                QuizAnswer("D", "Giới thiệu sản phẩm, Bão hòa thị trường, Phát triển, Suy thoái")
            ),
            correctOption = "A",
            explanation = "Đại đa số sản phẩm thương mại đều đi qua 4 giai đoạn tiêu chuẩn: Giới thiệu ra mắt (Introduction), Phát triển gia tốc (Growth), Đạt đỉnh bão hòa (Maturity), và kết thúc suy thoái thoái lui (Decline)."
        ),
        QuizQuestion(
            id = "q2_5",
            text = "Thương hiệu (Brand) mang lại giá trị cốt lõi nào cho khách hàng tiêu dùng?",
            hint = "Gợi ý: Tại sao người mua chuộng sản phẩm có tên tuổi hơn hàng vô danh?",
            options = listOf(
                QuizAnswer("A", "Giúp nhận diện nguồn gốc, tạo niềm tin và giảm thiểu rủi ro mua sắm"),
                QuizAnswer("B", "Làm cho sản phẩm trở nên thời thượng và có giá bán cao gấp đôi"),
                QuizAnswer("C", "Ràng buộc khách hàng bắt buộc phải trung thành lâu dài"),
                QuizAnswer("D", "Cung cấp chi tiết tất cả các thông số kỹ thuật bên trong sản phẩm")
            ),
            correctOption = "A",
            explanation = "Giá trị cốt lõi nhất của thương hiệu là nhận diện nguồn gốc xuất xứ, thiết lập chất lượng bảo đảm giúp người mua an tâm, giảm bớt chi phí tìm kiếm thông tin và giảm thiểu rủi ro mua nhầm hàng kém chất lượng."
        )
    )

    // Bộ câu hỏi cho môn Thống kê (id = "3")
    private val statisticsQuestions = listOf(
        QuizQuestion(
            id = "q3_1",
            text = "Trong thống kê, giá trị xuất hiện nhiều nhất trong một tập dữ liệu được gọi là:",
            hint = "Gợi ý: Từ mang ý nghĩa 'phổ biến nhất' hay 'xu hướng'.",
            options = listOf(
                QuizAnswer("A", "Số trung bình (Mean)"),
                QuizAnswer("B", "Số yếu vị (Mode)"),
                QuizAnswer("C", "Số trung vị (Median)"),
                QuizAnswer("D", "Độ lệch chuẩn (Standard Deviation)")
            ),
            correctOption = "B",
            explanation = "Số yếu vị (Mode) là giá trị có tần số xuất hiện lớn nhất trong tập hợp dữ liệu thống kê."
        ),
        QuizQuestion(
            id = "q3_2",
            text = "Tổng xác suất của toàn bộ các biến cố sơ cấp cấu thành một không gian mẫu bằng:",
            hint = "Gợi ý: Tổng thể của tất cả các khả năng chắc chắn xảy ra.",
            options = listOf(
                QuizAnswer("A", "0"),
                QuizAnswer("B", "0.5"),
                QuizAnswer("C", "1"),
                QuizAnswer("D", "Vô hạn")
            ),
            correctOption = "C",
            explanation = "Theo tiên đề xác suất Kolmogorov, xác suất của biến cố chắc chắn (toàn bộ không gian mẫu) luôn luôn bằng 1."
        ),
        QuizQuestion(
            id = "q3_3",
            text = "Sai lầm loại I (Type I error) xảy ra khi nào trong quá trình kiểm định giả thuyết thống kê?",
            hint = "Gợi ý: Bác bỏ một sự thật đúng đắn.",
            options = listOf(
                QuizAnswer("A", "Bác bỏ giả thuyết H0 khi H0 thực tế là Đúng"),
                QuizAnswer("B", "Chấp nhận giả thuyết H0 khi H0 thực tế là Sai"),
                QuizAnswer("C", "Bác bỏ giả thuyết H1 khi H1 thực tế là Đúng"),
                QuizAnswer("D", "Chấp nhận giả thuyết H1 khi H1 thực tế là Sai")
            ),
            correctOption = "A",
            explanation = "Sai lầm loại I xảy ra khi ta bác bỏ giả thuyết không (H0) mặc dù trong thực tế giả thuyết H0 đó hoàn toàn đúng (xác suất xảy ra sai lầm này gọi là mức ý nghĩa alpha)."
        ),
        QuizQuestion(
            id = "q3_4",
            text = "Đại lượng nào đo lường mức độ phân tán hoặc biến động của các số liệu so với giá trị trung bình?",
            hint = "Gợi ý: Khoảng cách lệch chuẩn bình phương trung bình.",
            options = listOf(
                QuizAnswer("A", "Tần số tích lũy"),
                QuizAnswer("B", "Độ lệch chuẩn (Standard Deviation)"),
                QuizAnswer("C", "Khoảng tứ phân vị"),
                QuizAnswer("D", "Hệ số tương quan")
            ),
            correctOption = "B",
            explanation = "Độ lệch chuẩn là thước đo mức độ phân tán của dữ liệu xung quanh giá trị trung bình cộng. Độ lệch chuẩn càng lớn chứng tỏ dữ liệu biến động càng nhiều."
        ),
        QuizQuestion(
            id = "q3_5",
            text = "Phân phối xác suất liên tục Gauss trong tự nhiên còn được gọi phổ biến với tên là:",
            hint = "Gợi ý: Hình chuông đối xứng chuẩn mực.",
            options = listOf(
                QuizAnswer("A", "Phân phối chuẩn (Normal distribution)"),
                QuizAnswer("B", "Phân phối nhị thức"),
                QuizAnswer("C", "Phân phối Poisson"),
                QuizAnswer("D", "Phân phối t-Student")
            ),
            correctOption = "A",
            explanation = "Phân phối chuẩn (hay phân phối Gauss) là phân phối xác suất liên tục cực kỳ quan trọng, có đồ thị dạng hình chuông đối xứng đặc trưng xuất hiện trong rất nhiều hiện tượng tự nhiên."
        )
    )

    // Bộ câu hỏi cho môn Luật đại cương (id = "4")
    private val lawQuestions = listOf(
        QuizQuestion(
            id = "q4_1",
            text = "Văn bản pháp lý nào có hiệu lực pháp lý cao nhất trong hệ thống pháp luật Việt Nam?",
            hint = "Gợi ý: Đạo luật cơ bản, gốc rễ của mọi đạo luật.",
            options = listOf(
                QuizAnswer("A", "Hiến pháp"),
                QuizAnswer("B", "Bộ luật Hình sự"),
                QuizAnswer("C", "Luật Dân sự"),
                QuizAnswer("D", "Nghị định của Chính phủ")
            ),
            correctOption = "A",
            explanation = "Hiến pháp là luật cơ bản của Nhà nước, có hiệu lực pháp lý tối cao. Mọi văn bản quy phạm pháp luật khác đều phải phù hợp và không được trái với Hiến pháp."
        ),
        QuizQuestion(
            id = "q4_2",
            text = "Năng lực hành vi dân sự của cá nhân đạt mức đầy đủ khi đạt các điều kiện nào sau đây?",
            hint = "Gợi ý: Tuổi thành niên và trí tuệ bình thường.",
            options = listOf(
                QuizAnswer("A", "Từ lúc vừa sinh ra khỏe mạnh"),
                QuizAnswer("B", "Từ đủ 16 tuổi trở lên"),
                QuizAnswer("C", "Từ đủ 18 tuổi trở lên và không bị mất/hạn chế năng lực hành vi"),
                QuizAnswer("D", "Đã lập gia đình hoặc đi làm có lương")
            ),
            correctOption = "C",
            explanation = "Theo Bộ luật Dân sự, người từ đủ 18 tuổi trở lên (trừ trường hợp mất, hạn chế năng lực hành vi dân sự hoặc có khó khăn trong nhận thức) có năng lực hành vi dân sự đầy đủ."
        ),
        QuizQuestion(
            id = "q4_3",
            text = "Cơ quan nào của nước Cộng hòa xã hội chủ nghĩa Việt Nam có quyền lập hiến và lập pháp cao nhất?",
            hint = "Gợi ý: Do cử tri cả nước trực tiếp bầu ra.",
            options = listOf(
                QuizAnswer("A", "Quốc hội"),
                QuizAnswer("B", "Chính phủ"),
                QuizAnswer("C", "Tòa án nhân dân tối cao"),
                QuizAnswer("D", "Chủ tịch nước")
            ),
            correctOption = "A",
            explanation = "Quốc hội là cơ quan đại biểu cao nhất của Nhân dân, cơ quan quyền lực nhà nước cao nhất, thực hiện quyền lập hiến và lập pháp."
        ),
        QuizQuestion(
            id = "q4_4",
            text = "Mọi hành vi vi phạm pháp luật đầy đủ đều được cấu thành từ 4 yếu tố nào?",
            hint = "Gợi ý: Bao gồm bên trong, bên ngoài, người làm và cái bị xâm hại.",
            options = listOf(
                QuizAnswer("A", "Mặt khách quan, Mặt chủ quan, Chủ thể, Khách thể"),
                QuizAnswer("B", "Hành động cố ý, Hậu quả nghiêm trọng, Vũ khí, Nhân chứng"),
                QuizAnswer("C", "Người phạm tội, Động cơ, Hiện trường, Tang vật"),
                QuizAnswer("D", "Quy tắc đạo đức, Hành vi sai lệch, Hình phạt, Sự lên án")
            ),
            correctOption = "A",
            explanation = "Cấu thành vi phạm pháp luật bao gồm: Chủ thể (người thực hiện), Khách thể (quan hệ được bảo vệ bị xâm hại), Mặt khách quan (hành vi bên ngoài) và Mặt chủ quan (tâm lý lỗi bên trong)."
        ),
        QuizQuestion(
            id = "q4_5",
            text = "Quy phạm pháp luật là quy tắc xử sự do nhà nước ban hành mang tính chất đặc trưng nào?",
            hint = "Gợi ý: Áp dụng rộng rãi và có tính cưỡng chế.",
            options = listOf(
                QuizAnswer("A", "Khuyến khích đạo đức tự nguyện"),
                QuizAnswer("B", "Bắt buộc chung (Tính quyền lực nhà nước)"),
                QuizAnswer("C", "Tùy nghi áp dụng theo sở thích"),
                QuizAnswer("D", "Giới hạn trong nội bộ một công ty")
            ),
            correctOption = "B",
            explanation = "Quy phạm pháp luật do Nhà nước ban hành hoặc thừa nhận, được Nhà nước bảo đảm thực hiện bằng quyền lực pháp lý mang tính bắt buộc chung đối với mọi cá nhân, tổ chức."
        )
    )

    // Bộ câu hỏi cho môn Tài chính (id = "5")
    private val financeQuestions = listOf(
        QuizQuestion(
            id = "q5_1",
            text = "Giá trị hiện tại (PV) của một khoản tiền thu hoạch trong tương lai sẽ biến động thế nào nếu lãi suất chiết khấu tăng lên?",
            hint = "Gợi ý: Chiết khấu càng mạnh thì giá trị quy về hiện tại càng bé.",
            options = listOf(
                QuizAnswer("A", "Tăng lên tỷ lệ thuận"),
                QuizAnswer("B", "Giảm đi"),
                QuizAnswer("C", "Không thay đổi"),
                QuizAnswer("D", "Luôn bằng 0")
            ),
            correctOption = "B",
            explanation = "Giá trị hiện tại PV = FV / (1 + r)^n. Do lãi suất chiết khấu r nằm dưới mẫu số, khi r tăng lên thì PV chắc chắn sẽ giảm đi."
        ),
        QuizQuestion(
            id = "q5_2",
            text = "Hệ thống phân tích tài chính DuPont phân rã chỉ tiêu hiệu quả ROE (Tỷ suất sinh lời trên vốn chủ sở hữu) thành mấy nhân tố chính?",
            hint = "Gợi ý: Doanh thu, tài sản và đòn bẩy.",
            options = listOf(
                QuizAnswer("A", "2 nhân tố"),
                QuizAnswer("B", "3 nhân tố (Biên lợi nhuận ròng, Vòng quay tài sản, Hệ số nhân vốn chủ sở hữu)"),
                QuizAnswer("C", "4 nhân tố"),
                QuizAnswer("D", "5 nhân tố")
            ),
            correctOption = "B",
            explanation = "Mô hình DuPont truyền thống phân rã ROE = Biên lợi nhuận ròng (Net Profit Margin) * Vòng quay tổng tài sản (Asset Turnover) * Hệ số đòn bẩy tài chính (Equity Multiplier)."
        ),
        QuizQuestion(
            id = "q5_3",
            text = "Rủi ro hệ thống (Systematic risk / Market risk) trong đầu tư tài chính được hiểu là:",
            hint = "Gợi ý: Rủi ro tác động toàn thị trường như thiên tai, lãi suất vĩ mô.",
            options = listOf(
                QuizAnswer("A", "Có thể loại bỏ triệt để nhờ đa dạng hóa danh mục đầu tư"),
                QuizAnswer("B", "Không thể loại bỏ hoàn toàn bằng cách đa dạng hóa"),
                QuizAnswer("C", "Chỉ xuất phát từ nội bộ năng lực yếu kém của một doanh nghiệp"),
                QuizAnswer("D", "Lỗi phát sinh do hư hỏng phần mềm giao dịch chứng khoán")
            ),
            correctOption = "B",
            explanation = "Rủi ro hệ thống ảnh hưởng đến toàn bộ nền kinh tế hoặc thị trường tài chính vĩ mô (ví dụ lạm phát, chiến tranh), do đó không thể triệt tiêu hoàn toàn bằng cách đa dạng hóa danh mục."
        ),
        QuizQuestion(
            id = "q5_4",
            text = "Chi phí cơ hội của việc cá nhân quyết định tích trữ nhiều tiền mặt trong két sắt là gì?",
            hint = "Gợi ý: Mất đi khoản lợi ích nếu đem số tiền đó đầu tư sinh lời.",
            options = listOf(
                QuizAnswer("A", "Phí dịch vụ ngân hàng gửi rút"),
                QuizAnswer("B", "Thuế thu nhập cá nhân"),
                QuizAnswer("C", "Khoản lãi suất hoặc lợi ích từ cơ hội đầu tư thay thế bị bỏ lỡ"),
                QuizAnswer("D", "Phí bảo dưỡng két sắt định kỳ")
            ),
            correctOption = "C",
            explanation = "Chi phí cơ hội là lợi ích tối đa bị mất đi khi lựa chọn phương án này thay vì phương án khác. Giữ tiền mặt sẽ làm mất khoản lãi suất ngân hàng hoặc lợi nhuận từ chứng khoán."
        ),
        QuizQuestion(
            id = "q5_5",
            text = "Công thức tính Giá trị tương lai (FV) của một khoản tiền gửi ban đầu PV nhận lãi kép r% mỗi năm sau n kỳ hạn là:",
            hint = "Gợi ý: Lũy thừa theo thời gian.",
            options = listOf(
                QuizAnswer("A", "FV = PV * (1 + r)^n"),
                QuizAnswer("B", "FV = PV * (1 + r * n)"),
                QuizAnswer("C", "FV = PV / (1 + r)^n"),
                QuizAnswer("D", "FV = PV + r * n")
            ),
            correctOption = "A",
            explanation = "Công thức tính lãi kép chuẩn mực xác định: Giá trị tương lai FV bằng Giá trị hiện tại PV nhân với hệ số tích lũy (1 + r) lũy thừa n kỳ hạn gửi."
        )
    )

    // Bộ câu hỏi cho môn Vi sinh học (id = "6")
    private val biologyQuestions = listOf(
        QuizQuestion(
            id = "q6_1",
            text = "Tế bào vi khuẩn có đặc điểm cấu tạo cơ bản nào khác biệt hoàn toàn với tế bào động vật?",
            hint = "Gợi ý: Không có màng bao bọc vật chất di truyền.",
            options = listOf(
                QuizAnswer("A", "Không có màng tế bào bao quanh"),
                QuizAnswer("B", "Chưa có nhân hoàn chỉnh (Nhân sơ - Prokaryote)"),
                QuizAnswer("C", "Không có ribosome tổng hợp protein"),
                QuizAnswer("D", "Kích thước khổng lồ dễ nhìn bằng mắt thường")
            ),
            correctOption = "B",
            explanation = "Vi khuẩn là sinh vật nhân sơ, tế bào của chúng không có màng nhân bao bọc vật chất di truyền và thiếu các bào quan có màng ngăn."
        ),
        QuizQuestion(
            id = "q6_2",
            text = "Phương thức sinh sản chủ yếu và phổ biến nhất của hầu hết các loài vi khuẩn là gì?",
            hint = "Gợi ý: Tự phân chia từ 1 thành 2 tế bào giống hệt nhau.",
            options = listOf(
                QuizAnswer("A", "Sinh sản hữu tính giao phối"),
                QuizAnswer("B", "Trực phân phân đôi (Binary Fission)"),
                QuizAnswer("C", "Nảy chồi sinh bào tử hữu tính"),
                QuizAnswer("D", "Tiếp hợp chuyển gene")
            ),
            correctOption = "B",
            explanation = "Vi khuẩn chủ yếu sinh sản vô tính bằng hình thức trực phân phân đôi: tế bào lớn lên, nhân đôi DNA và phân chia vách tế bào để tạo ra 2 tế bào con."
        ),
        QuizQuestion(
            id = "q6_3",
            text = "Virus được giới khoa học coi là dạng sống đặc biệt phi tế bào vì:",
            hint = "Gợi ý: Ký sinh bắt buộc, không thể tự chuyển hóa vật chất độc lập.",
            options = listOf(
                QuizAnswer("A", "Không có cấu tạo tế bào và chỉ nhân lên khi ký sinh trong tế bào vật chủ"),
                QuizAnswer("B", "Có kích thước khổng lồ hơn cả tế bào nấm men"),
                QuizAnswer("C", "Có thể tự thực hiện quang hợp tạo ra tinh bột"),
                QuizAnswer("D", "Sở hữu hệ thống ribosome dịch mã vô cùng phức tạp")
            ),
            correctOption = "A",
            explanation = "Virus là thực thể phi tế bào cực nhỏ, chỉ gồm vỏ protein và lõi acid nucleic. Chúng không có trao đổi chất độc lập và bắt buộc phải ký sinh trong tế bào sinh vật chủ để nhân bản."
        ),
        QuizQuestion(
            id = "q6_4",
            text = "Trong hệ sinh thái, vai trò quan trọng nhất của vi sinh vật hoại sinh phân hủy là gì?",
            hint = "Gợi ý: Chuyển xác động thực vật chết thành muối vô cơ trả lại cho đất.",
            options = listOf(
                QuizAnswer("A", "Giải phóng khí oxy qua quang hợp đại dương"),
                QuizAnswer("B", "Phân giải chất hữu cơ phức tạp thành chất vô cơ nuôi cây"),
                QuizAnswer("C", "Tấn công và tiêu diệt các sinh vật tiêu thụ lớn"),
                QuizAnswer("D", "Cố định năng lượng mặt trời thành tinh bột")
            ),
            correctOption = "B",
            explanation = "Vi sinh vật phân hủy đóng vai trò khép kín chu trình vật chất: phân hủy xác động thực vật hữu cơ thành muối khoáng vô cơ cho thực vật tái hấp thu."
        ),
        QuizQuestion(
            id = "q6_5",
            text = "Thuốc kháng sinh (Antibiotics) thông thường có tác dụng tiêu diệt hoặc ức chế chọn lọc đối với nhóm vi sinh vật nào?",
            hint = "Gợi ý: KHÔNG có tác dụng đối với bệnh do virus gây ra.",
            options = listOf(
                QuizAnswer("A", "Vi khuẩn (Bacteria)"),
                QuizAnswer("B", "Virus"),
                QuizAnswer("C", "Động vật nguyên sinh ký sinh"),
                QuizAnswer("D", "Tất cả các loài côn trùng gây hại")
            ),
            correctOption = "A",
            explanation = "Kháng sinh là những chất có khả năng tiêu diệt hoặc ức chế đặc hiệu sự phát triển của vi khuẩn bằng cách phá hủy vách tế bào hoặc ức chế tổng hợp protein của chúng. Kháng sinh hoàn toàn vô tác dụng với virus."
        )
    )

    init {
        loadAvailableDocuments()
        loadQuizSets()
        loadLeaderboard()
        loadUserStats()
    }

    fun loadAvailableDocuments() {
        viewModelScope.launch {
            getPublishedDocuments().onSuccess { firestoreDocs ->
                _availableDocs.value = firestoreDocs.map { it.toLibraryDoc() }
            }.onFailure {
                _availableDocs.value = emptyList()
            }
        }
    }

    fun loadQuizSets() {
        viewModelScope.launch {
            authRepository.getQuizSets().onSuccess { firestoreSets ->
                val localAiSets = allQuizSetsList.filter { it.id.startsWith("ai_") }
                allQuizSetsList.clear()
                allQuizSetsList.addAll(localAiSets)
                allQuizSetsList.addAll(firestoreSets)
                
                _uiState.update { state ->
                    val filtered = if (state.selectedSubject == "Tất cả") {
                        allQuizSetsList
                    } else {
                        allQuizSetsList.filter { it.subject.contains(state.selectedSubject, ignoreCase = true) }
                    }
                    state.copy(quizSets = filtered)
                }
            }.onFailure {
                val localAiSets = allQuizSetsList.filter { it.id.startsWith("ai_") }
                allQuizSetsList.clear()
                allQuizSetsList.addAll(localAiSets)
                
                _uiState.update { state ->
                    val filtered = if (state.selectedSubject == "Tất cả") {
                        allQuizSetsList
                    } else {
                        allQuizSetsList.filter { it.subject.contains(state.selectedSubject, ignoreCase = true) }
                    }
                    state.copy(quizSets = filtered)
                }
            }
        }
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            try {
                // 1. Lấy thông tin user hiện tại
                val currentUserResult = authRepository.getCurrentUser()
                val currentUser = currentUserResult.getOrNull()

                // 2. Lấy toàn bộ users từ Firestore
                val allUsersResult = authRepository.getAllUsers()
                val allUsers = allUsersResult.getOrElse { emptyList() }

                // 3. Map Firestore users sang LeaderboardUser
                val realLeaderboardUsers = allUsers.map { u ->
                    // Sử dụng totalXp thực tế của user
                    val score = u.totalXp
                    
                    val isMe = currentUser != null && u.uid == currentUser.uid
                    val displayName = if (u.fullName.isNotEmpty()) u.fullName else "Học viên ẩn danh"
                    
                    val universityName = u.university.ifEmpty { "" }
                    
                    LeaderboardUser(
                        rank = 0, // rank sẽ tính động sau khi sort
                        name = displayName,
                        university = universityName,
                        score = score,
                        isMe = isMe
                    )
                }

                // 4. Lọc ra: chỉ hiện user có XP >= 1, hoặc là chính mình (luôn hiện)
                val filteredList = realLeaderboardUsers.filter { it.score >= 1 || it.isMe }

                // 5. Sắp xếp giảm dần theo điểm số score
                val sortedList = filteredList.sortedByDescending { it.score }

                // 6. Gán thứ hạng rank động từ 1 trở đi
                val finalLeaderboard = sortedList.mapIndexed { index, user ->
                    user.copy(rank = index + 1)
                }

                // 7. Cập nhật state
                _uiState.update { it.copy(leaderboard = finalLeaderboard) }
            } catch (e: Exception) {
                _uiState.update { it.copy(leaderboard = emptyList()) }
            }
        }
    }

    fun loadUserStats() {
        viewModelScope.launch {
            authRepository.getCurrentUser().onSuccess { user ->
                if (user != null) {
                    val compRate = if (user.quizCount > 0) {
                        (user.quizAverageScore * 10).toInt().coerceIn(0, 100)
                    } else 0
                    
                    _uiState.update { state ->
                        state.copy(
                            progress = QuizProgress(
                                avgScore = user.quizAverageScore,
                                completedCount = user.quizCount,
                                completionRate = compRate
                            )
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(progress = QuizProgress(0.0f, 0, 0))
                    }
                }
            }.onFailure {
                _uiState.update { state ->
                    state.copy(progress = QuizProgress(0.0f, 0, 0))
                }
            }
        }
    }


    // ─── Event Handlers ───────────────────────────────────────────────────────

    /**
     * Lọc danh sách bộ đề theo chủ đề môn học
     */
    fun onSubjectSelected(subject: String) {
        _uiState.update { state ->
            val filtered = if (subject == "Tất cả") {
                allQuizSetsList
            } else {
                allQuizSetsList.filter { it.subject.contains(subject, ignoreCase = true) }
            }
            state.copy(
                selectedSubject = subject,
                quizSets = filtered
            )
        }
    }

    private var lastAiCallTime: Long = 0

    /**
     * Tự động sinh đề thi trắc nghiệm học thuật bằng Gemini AI và bắt đầu làm bài.
     */
    fun generateAndStartQuiz(
        docId: String? = null,
        docTitle: String,
        count: Int,
        difficulty: Difficulty,
        format: String = "Trắc nghiệm",
        docContent: String,
        onError: (String) -> Unit
    ) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAiCallTime < 60000) {
            val remainSeconds = 60 - (currentTime - lastAiCallTime) / 1000
            onError("Bạn thao tác quá nhanh. Vui lòng đợi trong $remainSeconds giây để tránh spam API.")
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            var contentToUse = docContent

            // Nếu có docId, thử truy xuất nội dung lý thuyết thực tế từ cache trước
            if (!docId.isNullOrEmpty()) {
                documentRepository.getCachedDocViewerContent(docId).onSuccess { cachedContent ->
                    if (cachedContent != null && cachedContent.content.trim().isNotEmpty()) {
                        contentToUse = cachedContent.content
                    }
                }
            }

            if (contentToUse.trim().isEmpty()) {
                contentToUse = "Tài liệu môn học $docTitle ôn tập học thuật đại cương và chuyên ngành."
            }

            aiRepository.generateQuizFromDocument(
                title = docTitle,
                type = "Tài liệu ôn tập",
                content = contentToUse,
                count = count,
                format = format
            ).onSuccess { generatedQuestions ->
                lastAiCallTime = System.currentTimeMillis()
                
                // Sinh thông báo sự kiện tạo Quiz AI thành công thực tế lưu vào Firestore
                viewModelScope.launch {
                    authRepository.getCurrentUser().onSuccess { user ->
                        if (user != null) {
                            notificationRepository.addNotification(
                                userId = user.uid,
                                title = "🧩 Đề ôn tập AI",
                                content = "EduVault vừa hoàn tất thiết kế bộ đề ôn tập $format gồm $count câu hỏi cho môn học '${docTitle}'!",
                                type = NotificationType.AI
                            )
                        }
                    }
                }

                val customSet = QuizSet(
                    id = "ai_${System.currentTimeMillis()}",
                    subject = docTitle,
                    title = "Quiz AI ($format): $docTitle",
                    difficulty = difficulty,
                    questionCount = count,
                    playCount = "1",
                    isNew = true,
                    bgIndex = (Math.abs(docTitle.hashCode()) % 6)
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        screenMode = QuizScreenMode.PLAYER,
                        activeQuizSet = customSet,
                        questions = generatedQuestions,
                        currentQuestionIndex = 0,
                        selectedOption = null,
                        isAnswerLocked = false,
                        remainingSeconds = 60,
                        answersMap = emptyMap(),
                        correctAnswersCount = 0,
                        timeSpentSeconds = 0,
                        xpEarned = 0,
                        quizFormat = format
                    )
                }

                // Lưu cache câu hỏi tạm thời của phiên để hỗ trợ chơi lại
                localQuestionsCache[customSet.id] = generatedQuestions

                startTimer()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false) }
                onError(e.localizedMessage ?: "Quá trình tạo đề thi AI gặp sự cố định dạng. Vui lòng bấm thử lại.")
            }
        }
    }

    /**
     * Bắt đầu chơi bộ đề trắc nghiệm
     */
    fun startQuiz(quizSet: QuizSet) {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            val fetchedQuestions = localQuestionsCache[quizSet.id]
                ?: authRepository.getQuizQuestions(quizSet.id).getOrElse { emptyList() }

            if (fetchedQuestions.isEmpty()) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            val finalQuestions = mutableListOf<QuizQuestion>()
            val count = quizSet.questionCount
            // Cycle through fetchedQuestions to fill up to 'count' questions
            var cycleIndex = 0
            while (finalQuestions.size < count) {
                val q = fetchedQuestions[cycleIndex % fetchedQuestions.size]
                finalQuestions.add(q.copy(id = "${q.id}_${finalQuestions.size}"))
                cycleIndex++
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    screenMode = QuizScreenMode.PLAYER,
                    activeQuizSet = quizSet,
                    questions = finalQuestions,
                    currentQuestionIndex = 0,
                    selectedOption = null,
                    isAnswerLocked = false,
                    remainingSeconds = 60,
                    answersMap = emptyMap(),
                    correctAnswersCount = 0,
                    timeSpentSeconds = 0,
                    xpEarned = 0
                )
            }

            startTimer()
        }
    }

    /**
     * Chọn phương án trả lời
     */
    fun selectOption(optionKey: String) {
        val state = _uiState.value
        if (state.isAnswerLocked) return // Không cho chọn lại sau khi đã khóa

        val currentQuestion = state.questions.getOrNull(state.currentQuestionIndex) ?: return
        val isCorrect = currentQuestion.correctOption == optionKey

        val updatedAnswersMap = state.answersMap.toMutableMap()
        updatedAnswersMap[state.currentQuestionIndex] = optionKey

        val updatedCorrectCount = if (isCorrect) state.correctAnswersCount + 1 else state.correctAnswersCount
        val xpReward = if (isCorrect) state.xpEarned + 20 else state.xpEarned // 20 XP cho mỗi câu đúng

        stopTimer() // Dừng bộ đếm ngược khi đã chọn đáp án

        _uiState.update {
            it.copy(
                selectedOption = optionKey,
                isAnswerLocked = true,
                answersMap = updatedAnswersMap,
                correctAnswersCount = updatedCorrectCount,
                xpEarned = xpReward
            )
        }
    }

    /**
     * Chuyển sang câu hỏi tiếp theo hoặc hiển thị màn hình kết quả
     */
    fun nextQuestion() {
        val state = _uiState.value
        val nextIndex = state.currentQuestionIndex + 1

        if (nextIndex < state.questions.size) {
            // Chuyển sang câu hỏi kế tiếp
            _uiState.update {
                it.copy(
                    currentQuestionIndex = nextIndex,
                    selectedOption = null,
                    isAnswerLocked = false,
                    remainingSeconds = 60
                )
            }
            startTimer() // Khởi động lại đếm ngược cho câu hỏi mới
        } else {
            // Hoàn thành bộ đề -> Chuyển sang màn hình Kết Quả
            stopTimer()
            _uiState.update {
                it.copy(
                    screenMode = QuizScreenMode.RESULTS,
                    // Cập nhật điểm thưởng thêm 50 XP nếu đạt điểm tối đa
                    xpEarned = if (it.correctAnswersCount == it.questions.size) it.xpEarned + 50 else it.xpEarned
                )
            }

            // TỰ ĐỘNG GHI NHẬN THÀNH TÍCH ĐIỂM THI ĐUA VÀ XP VÀO FIRESTORE AN TOÀN QUA TRANSACTION
            viewModelScope.launch {
                val questionsSize = _uiState.value.questions.size
                if (questionsSize > 0) {
                    val correctCount = _uiState.value.correctAnswersCount
                    val score10 = (correctCount.toFloat() / questionsSize) * 10f
                    val earnedXp = _uiState.value.xpEarned
                    authRepository.updateUserQuizStats(score10, earnedXp)
                }
            }
        }
    }

    /**
     * Bỏ qua câu hỏi hiện tại (Đánh dấu sai)
     */
    fun skipQuestion() {
        val state = _uiState.value
        if (state.isAnswerLocked) {
            // Nếu đã trả lời xong và bấm nút, hoạt động giống như phím Tiếp theo
            nextQuestion()
            return
        }

        // Đánh dấu bỏ qua là trả lời sai
        val updatedAnswersMap = state.answersMap.toMutableMap()
        updatedAnswersMap[state.currentQuestionIndex] = "" // Rỗng nghĩa là bỏ qua/không trả lời

        stopTimer()

        _uiState.update {
            it.copy(
                selectedOption = "",
                isAnswerLocked = true,
                answersMap = updatedAnswersMap
            )
        }
    }

    /**
     * Chơi lại bài trắc nghiệm hiện tại
     */
    fun retryQuiz() {
        val activeSet = _uiState.value.activeQuizSet
        if (activeSet != null) {
            startQuiz(activeSet)
        }
    }

    /**
     * Thoát bài trắc nghiệm, quay lại Dashboard chính
     */
    fun exitQuiz() {
        stopTimer()
        _uiState.update {
            it.copy(
                screenMode = QuizScreenMode.DASHBOARD,
                activeQuizSet = null,
                questions = emptyList(),
                currentQuestionIndex = 0,
                selectedOption = null,
                isAnswerLocked = false,
                answersMap = emptyMap(),
                correctAnswersCount = 0,
                timeSpentSeconds = 0,
                xpEarned = 0
            )
        }
        loadLeaderboard()
    }

    // ─── Timer Logic ──────────────────────────────────────────────────────────

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { state ->
                    val nextRemaining = state.remainingSeconds - 1
                    val nextTimeSpent = state.timeSpentSeconds + 1

                    if (nextRemaining <= 0) {
                        // Hết thời gian -> Tự động khóa đáp án là sai để hiển thị giải thích
                        stopTimer()
                        val updatedAnswersMap = state.answersMap.toMutableMap()
                        updatedAnswersMap[state.currentQuestionIndex] = "" // Đáp án rỗng

                        state.copy(
                            remainingSeconds = 0,
                            timeSpentSeconds = nextTimeSpent,
                            selectedOption = "",
                            isAnswerLocked = true,
                            answersMap = updatedAnswersMap
                        )
                    } else {
                        state.copy(
                            remainingSeconds = nextRemaining,
                            timeSpentSeconds = nextTimeSpent
                        )
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
