package com.example.healthprofile;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.healthprofile.model.HealthTip;

import java.util.ArrayList;
import java.util.List;

public class HealthTipDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvContent, tvCategory;
    private CardView cardCategory;
    private ImageView ivCategory, btnBack;

    private int tipId;
    private HealthTip tip;
    private List<HealthTip> allTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_tip_detail);

        tipId = getIntent().getIntExtra("tip_id", 0);
        if (tipId == 0) {
            Toast.makeText(this, "Lỗi: Không tìm thấy mẹo sức khỏe", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadAllTips();
        loadTipDetail();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvTitle = findViewById(R.id.tv_title);
        tvContent = findViewById(R.id.tv_content);
        tvCategory = findViewById(R.id.tv_category);
        cardCategory = findViewById(R.id.card_category);
        ivCategory = findViewById(R.id.iv_category);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadAllTips() {
        allTips = new ArrayList<>();

        // Load lại danh sách giống HealthTipsActivity
        allTips.add(new HealthTip(1, "Uống đủ nước mỗi ngày",
                "Cơ thể cần 2-3 lít nước mỗi ngày để duy trì sức khỏe tốt. Uống nước đều đặn giúp cải thiện tuần hoàn máu, thải độc tố và duy trì làn da khỏe mạnh. Nên uống nước vào buổi sáng sau khi thức dậy và trước mỗi bữa ăn.\n\nMột số lợi ích của việc uống đủ nước:\n• Cải thiện chức năng não bộ và tăng cường tập trung\n• Giúp giảm cân hiệu quả\n• Làm đẹp da, giảm mụn và nếp nhăn\n• Ngăn ngừa sỏi thận\n• Cải thiện hệ tiêu hóa",
                "diet"));

        allTips.add(new HealthTip(2, "Ăn nhiều rau xanh và trái cây",
                "Rau xanh và trái cây cung cấp vitamin, khoáng chất và chất xơ cần thiết cho cơ thể. Nên ăn ít nhất 5 phần rau củ quả mỗi ngày với nhiều màu sắc khác nhau để đảm bảo đa dạng dinh dưỡng.\n\nMẹo chọn rau củ quả:\n• Ưu tiên rau củ quả tươi, theo mùa\n• Chọn nhiều màu sắc khác nhau\n• Kết hợp cả rau sống và rau nấu chín\n• Ăn cả vỏ khi có thể để tận dụng chất xơ\n• Hạn chế nước ép trái cây, nên ăn trái cây nguyên quả",
                "diet"));

        allTips.add(new HealthTip(3, "Hạn chế đường và muối",
                "Tiêu thụ quá nhiều đường và muối có thể dẫn đến các vấn đề sức khỏe như tiểu đường, cao huyết áp. Nên kiểm soát lượng đường dưới 50g/ngày và muối dưới 5g/ngày.\n\nCách giảm đường và muối:\n• Đọc nhãn dinh dưỡng trước khi mua thực phẩm\n• Nấu ăn tại nhà thay vì ăn ngoài\n• Thay thế đường bằng mật ong hoặc trái cây\n• Dùng gia vị thảo mộc thay muối\n• Tránh đồ ăn chế biến sẵn",
                "diet"));

        allTips.add(new HealthTip(4, "Tập thể dục 30 phút mỗi ngày",
                "Vận động thường xuyên giúp tăng cường sức khỏe tim mạch, xương khớp và tinh thần. Có thể bắt đầu với các hoạt động đơn giản như đi bộ, chạy bộ nhẹ hoặc yoga. Quan trọng là duy trì đều đặn hơn là cường độ cao.\n\nLợi ích của tập thể dục:\n• Tăng cường tim mạch và phổi\n• Giảm nguy cơ mắc bệnh mãn tính\n• Cải thiện tâm trạng và giảm stress\n• Tăng cường cơ bắp và xương\n• Giúp ngủ ngon hơn\n• Kiểm soát cân nặng",
                "exercise"));

        allTips.add(new HealthTip(5, "Kéo giãn cơ thể mỗi sáng",
                "Kéo giãn giúp tăng tính linh hoạt, giảm căng thẳng cơ bắp và cải thiện tư thế. Dành 5-10 phút mỗi sáng để kéo giãn toàn thân, đặc biệt chú ý đến vùng cổ, vai và lưng.\n\nCác động tác kéo giãn cơ bản:\n• Kéo giãn cổ: nghiêng đầu sang 2 bên\n• Kéo giãn vai: xoay vai tròn\n• Kéo giãn lưng: cúi người chạm chân\n• Kéo giãn chân: duỗi chân và kéo mũi bàn chân\n• Kéo giãn tay: duỗi thẳng và xoay cổ tay",
                "exercise"));

        allTips.add(new HealthTip(6, "Đi bộ sau bữa ăn",
                "Đi bộ nhẹ nhàng 10-15 phút sau bữa ăn giúp hỗ trợ tiêu hóa, kiểm soát đường huyết và giảm cảm giác đầy bụng. Tránh vận động mạnh ngay sau ăn.\n\nTác dụng của đi bộ sau ăn:\n• Cải thiện tiêu hóa thức ăn\n• Giảm đường huyết sau ăn\n• Ngăn ngừa trào ngược dạ dày\n• Giảm cảm giác buồn ngủ\n• Đốt cháy calo thừa",
                "exercise"));

        allTips.add(new HealthTip(7, "Ngủ đủ 7-8 tiếng mỗi đêm",
                "Giấc ngủ chất lượng rất quan trọng cho sức khỏe thể chất và tinh thần. Nên đi ngủ và thức dậy vào giờ cố định, tránh sử dụng điện thoại trước khi ngủ 30 phút, và tạo môi trường phòng ngủ thoáng mát, yên tĩnh.\n\nMẹo ngủ ngon:\n• Tắt đèn và tắt thiết bị điện tử\n• Giữ nhiệt độ phòng 18-22°C\n• Tránh caffeine sau 15h chiều\n• Không ăn no trước khi ngủ\n• Tập thở sâu hoặc thiền trước khi ngủ\n• Đọc sách để thư giãn",
                "mental_health"));

        allTips.add(new HealthTip(8, "Thực hành thiền và hít thở sâu",
                "Thiền và hít thở sâu giúp giảm stress, cải thiện sự tập trung và tăng cường sức khỏe tinh thần. Chỉ cần 5-10 phút mỗi ngày để thực hành hít thở sâu hoặc thiền định.\n\nKỹ thuật hít thở 4-7-8:\n1. Hít vào qua mũi trong 4 giây\n2. Nín thở trong 7 giây\n3. Thở ra qua miệng trong 8 giây\n4. Lặp lại 4-5 lần\n\nLợi ích: giảm lo âu, cải thiện giấc ngủ, tăng năng lượng",
                "mental_health"));

        allTips.add(new HealthTip(9, "Dành thời gian cho sở thích",
                "Tham gia các hoạt động yêu thích giúp giảm căng thẳng, tăng cảm giác hạnh phúc. Có thể là đọc sách, nghe nhạc, vẽ tranh, làm vườn hay bất kỳ hoạt động nào bạn thích.\n\nGợi ý các hoạt động giải trí:\n• Đọc sách hoặc nghe audiobook\n• Học một kỹ năng mới\n• Chơi nhạc cụ\n• Vẽ tranh hoặc làm đồ thủ công\n• Làm vườn\n• Nấu ăn\n• Chụp ảnh\n• Viết nhật ký",
                "mental_health"));

        allTips.add(new HealthTip(10, "Rửa tay thường xuyên",
                "Rửa tay bằng xà phòng ít nhất 20 giây, đặc biệt trước khi ăn, sau khi đi vệ sinh và khi về nhà. Đây là cách đơn giản nhất để ngăn ngừa lây nhiễm vi khuẩn và virus.\n\n6 bước rửa tay đúng cách:\n1. Làm ướt tay\n2. Xoa xà phòng đều khắp tay\n3. Chà xát giữa các ngón tay\n4. Chà mu bàn tay\n5. Chà đầu ngón tay và móng tay\n6. Rửa sạch và lau khô",
                "prevention"));

        allTips.add(new HealthTip(11, "Tiêm phòng đầy đủ",
                "Vaccine giúp phòng ngừa nhiều bệnh nguy hiểm. Nên tiêm phòng đầy đủ theo lịch và cập nhật các vaccine mới khi cần thiết, đặc biệt là vaccine cúm hàng năm.\n\nCác vaccine quan trọng:\n• Vaccine cúm (hàng năm)\n• Vaccine COVID-19\n• Vaccine viêm gan B\n• Vaccine uốn ván (mỗi 10 năm)\n• Vaccine HPV (cho người trẻ)\n• Vaccine phế cầu (cho người cao tuổi)",
                "prevention"));

        allTips.add(new HealthTip(12, "Khám sức khỏe định kỳ",
                "Khám sức khỏe định kỳ 6 tháng hoặc 1 năm một lần giúp phát hiện sớm các vấn đề sức khỏe. Nên làm xét nghiệm máu, đo huyết áp và các chỉ số sức khỏe cơ bản.\n\nCác xét nghiệm nên làm:\n• Xét nghiệm máu tổng quát\n• Đo huyết áp\n• Đo đường huyết\n• Xét nghiệm cholesterol\n• Chụp X-quang ngực (hàng năm)\n• Khám nha khoa (6 tháng/lần)\n• Khám mắt (1-2 năm/lần)",
                "prevention"));

        allTips.add(new HealthTip(13, "Tránh hút thuốc và rượu bia",
                "Thuốc lá và rượu bia có hại cho sức khỏe, gây ra nhiều bệnh nguy hiểm như ung thư, gan, phổi. Nên từ bỏ hoàn toàn hoặc hạn chế tối đa việc sử dụng các chất này.\n\nTác hại của thuốc lá:\n• Ung thư phổi, thanh quản\n• Bệnh tim mạch\n• Đột quỵ\n• COPD\n• Lão hóa sớm\n\nTác hại của rượu:\n• Xơ gan\n• Ung thư gan\n• Suy giảm trí nhớ\n• Tăng huyết áp",
                "general"));

        allTips.add(new HealthTip(14, "Giữ vệ sinh răng miệng",
                "Đánh răng 2 lần/ngày, dùng chỉ nha khoa và khám nha sĩ định kỳ 6 tháng/lần. Vệ sinh răng miệng tốt không chỉ bảo vệ răng mà còn phòng ngừa các bệnh về tim mạch.\n\nQuy trình vệ sinh răng miệng:\n• Đánh răng sau bữa sáng và trước khi ngủ\n• Dùng bàn chải lông mềm\n• Đánh răng ít nhất 2 phút\n• Dùng chỉ nha khoa mỗi ngày\n• Súc miệng bằng nước muối\n• Thay bàn chải 3 tháng/lần",
                "general"));

        allTips.add(new HealthTip(15, "Duy trì cân nặng hợp lý",
                "Cân nặng hợp lý giúp giảm nguy cơ mắc nhiều bệnh như tiểu đường, tim mạch, khớp. Tính BMI để biết cân nặng lý tưởng và duy trì thông qua chế độ ăn uống và vận động hợp lý.\n\nCông thức BMI: Cân nặng (kg) / Chiều cao² (m)\n• BMI < 18.5: Thiếu cân\n• BMI 18.5-24.9: Bình thường\n• BMI 25-29.9: Thừa cân\n• BMI ≥ 30: Béo phì\n\nCách duy trì cân nặng:\n• Ăn đủ chất, đủ bữa\n• Tập thể dục đều đặn\n• Ngủ đủ giấc\n• Giảm stress",
                "general"));

        allTips.add(new HealthTip(16, "Bảo vệ da khỏi ánh nắng",
                "Sử dụng kem chống nắng SPF 30+ khi ra ngoài, mặc quần áo che chắn và đội mũ rộng vành. Tránh phơi nắng vào giữa trúa (10h-14h) khi tia UV mạnh nhất.\n\nCách bảo vệ da:\n• Thoa kem chống nắng 15 phút trước khi ra ngoài\n• Thoa lại mỗi 2 tiếng\n• Mặc quần áo dài tay màu tối\n• Đội mũ rộng vành và đeo kính\n• Tránh phơi nắng 10h-14h\n• Uống đủ nước để giữ ẩm cho da",
                "prevention"));

        allTips.add(new HealthTip(17, "Kiểm soát stress hiệu quả",
                "Stress kéo dài ảnh hưởng xấu đến sức khỏe. Tìm cách giảm stress phù hợp như tập yoga, thiền, nghe nhạc, hoặc tâm sự với người thân. Biết nói 'không' khi cần thiết.\n\nCách giảm stress:\n• Tập thể dục đều đặn\n• Thiền hoặc yoga\n• Nghe nhạc thư giãn\n• Dành thời gian với bạn bè\n• Viết nhật ký\n• Học cách quản lý thời gian\n• Nói 'không' với công việc quá tải",
                "mental_health"));

        allTips.add(new HealthTip(18, "Ăn sáng đầy đủ",
                "Bữa sáng là bữa ăn quan trọng nhất trong ngày, cung cấp năng lượng cho cả ngày làm việc. Nên ăn sáng trong vòng 1 giờ sau khi thức dậy với đủ protein, carb và chất béo lành mạnh.\n\nThực đơn bữa sáng lý tưởng:\n• Protein: trứng, thịt, cá, đậu\n• Carb phức: yến mạch, bánh mì nguyên cám\n• Chất béo tốt: bơ, hạt, dầu ô liu\n• Vitamin: trái cây, rau xanh\n• Sữa hoặc sữa chua\n\nTránh: đồ chiên, đồ ngọt, thức ăn nhanh",
                "diet"));

        allTips.add(new HealthTip(19, "Hạn chế thức khuya",
                "Thức khuya thường xuyên làm rối loạn nhịp sinh học, giảm sức đề kháng và ảnh hưởng đến tinh thần. Nên đi ngủ trước 23h và duy trì giấc ngủ liên tục ít nhất 7-8 tiếng.\n\nTác hại của thức khuya:\n• Suy giảm hệ miễn dịch\n• Tăng nguy cơ béo phì\n• Rối loạn nội tiết\n• Giảm trí nhớ và tập trung\n• Tăng nguy cơ trầm cảm\n• Lão hóa da sớm\n\nMẹo đi ngủ sớm:\n• Tắt thiết bị điện tử trước 30 phút\n• Tạo thói quen đi ngủ cố định\n• Tránh caffeine buổi tối",
                "general"));

        allTips.add(new HealthTip(20, "Duy trì mối quan hệ xã hội",
                "Kết nối với gia đình, bạn bè giúp cải thiện sức khỏe tinh thần và kéo dài tuổi thọ. Dành thời gian trò chuyện, chia sẻ và tham gia các hoạt động cộng đồng.\n\nLợi ích của kết nối xã hội:\n• Giảm stress và lo âu\n• Tăng cảm giác hạnh phúc\n• Giảm nguy cơ trầm cảm\n• Tăng tuổi thọ\n• Cải thiện sức khỏe tim mạch\n• Tăng cường trí nhớ\n\nCách duy trì mối quan hệ:\n• Gọi điện cho người thân thường xuyên\n• Tham gia các câu lạc bộ\n• Tình nguyện cộng đồng\n• Tổ chức gặp gỡ bạn bè",
                "mental_health"));
    }

    private void loadTipDetail() {
        for (HealthTip t : allTips) {
            if (t.getId() == tipId) {
                tip = t;
                break;
            }
        }

        if (tip != null) {
            displayTipDetail();
        } else {
            Toast.makeText(this, "Không tìm thấy mẹo sức khỏe", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayTipDetail() {
        tvTitle.setText(tip.getTitle());
        tvContent.setText(tip.getContent());
        tvCategory.setText(tip.getCategoryName());

        cardCategory.setCardBackgroundColor(tip.getCategoryColor());
        ivCategory.setImageResource(tip.getCategoryIcon());
    }
}