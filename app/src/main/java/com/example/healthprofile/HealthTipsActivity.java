package com.example.healthprofile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthprofile.adapter.HealthTipAdapter;
import com.example.healthprofile.model.HealthTip;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class HealthTipsActivity extends AppCompatActivity {

    private SearchView searchView;
    private ChipGroup chipGroupCategory;
    private RecyclerView rvHealthTips;
    private LinearLayout emptyState;
    private ImageButton btnBack;
    private List<HealthTip> tipsList;
    private List<HealthTip> filteredList;
    private HealthTipAdapter adapter;
    private String selectedCategory = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_tips);

        initViews();
        loadHealthTips();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        searchView = findViewById(R.id.search_view);
        chipGroupCategory = findViewById(R.id.chip_group_category);
        rvHealthTips = findViewById(R.id.rv_health_tips);
        emptyState = findViewById(R.id.empty_state);

        btnBack.setOnClickListener(v -> finish());

        rvHealthTips.setLayoutManager(new LinearLayoutManager(this));
        tipsList = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new HealthTipAdapter(this, filteredList, tip -> {
            Intent intent = new Intent(this, HealthTipDetailActivity.class);
            intent.putExtra("tip_id", tip.getId());
            startActivity(intent);
        });
        rvHealthTips.setAdapter(adapter);

        // Setup search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterTips(newText, selectedCategory);
                return true;
            }
        });

        // Setup category filter
        setupCategoryChips();
    }

    private void setupCategoryChips() {
        chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedCategory = "all";
            } else {
                int checkedId = checkedIds.get(0);
                Chip chip = findViewById(checkedId);
                if (chip != null) {
                    selectedCategory = (String) chip.getTag();
                }
            }
            filterTips(searchView.getQuery().toString(), selectedCategory);
        });

        // Set default selection
        Chip chipAll = findViewById(R.id.chip_all);
        if (chipAll != null) {
            chipAll.setChecked(true);
        }
    }

    private void loadHealthTips() {
        tipsList.clear();

        // Dữ liệu mẫu về dinh dưỡng
        tipsList.add(new HealthTip(1, "Uống đủ nước mỗi ngày",
                "Cơ thể cần 2-3 lít nước mỗi ngày để duy trì sức khỏe tốt. Uống nước đều đặn giúp cải thiện tuần hoàn máu, thải độc tố và duy trì làn da khỏe mạnh. Nên uống nước vào buổi sáng sau khi thức dậy và trước mỗi bữa ăn.",
                "diet"));

        tipsList.add(new HealthTip(2, "Ăn nhiều rau xanh và trái cây",
                "Rau xanh và trái cây cung cấp vitamin, khoáng chất và chất xơ cần thiết cho cơ thể. Nên ăn ít nhất 5 phần rau củ quả mỗi ngày với nhiều màu sắc khác nhau để đảm bảo đa dạng dinh dưỡng.",
                "diet"));

        tipsList.add(new HealthTip(3, "Hạn chế đường và muối",
                "Tiêu thụ quá nhiều đường và muối có thể dẫn đến các vấn đề sức khỏe như tiểu đường, cao huyết áp. Nên kiểm soát lượng đường dưới 50g/ngày và muối dưới 5g/ngày.",
                "diet"));

        // Dữ liệu về vận động
        tipsList.add(new HealthTip(4, "Tập thể dục 30 phút mỗi ngày",
                "Vận động thường xuyên giúp tăng cường sức khỏe tim mạch, xương khớp và tinh thần. Có thể bắt đầu với các hoạt động đơn giản như đi bộ, chạy bộ nhẹ hoặc yoga. Quan trọng là duy trì đều đặn hơn là cường độ cao.",
                "exercise"));

        tipsList.add(new HealthTip(5, "Kéo giãn cơ thể mỗi sáng",
                "Kéo giãn giúp tăng tính linh hoạt, giảm căng thẳng cơ bắp và cải thiện tư thế. Dành 5-10 phút mỗi sáng để kéo giãn toàn thân, đặc biệt chú ý đến vùng cổ, vai và lưng.",
                "exercise"));

        tipsList.add(new HealthTip(6, "Đi bộ sau bữa ăn",
                "Đi bộ nhẹ nhàng 10-15 phút sau bữa ăn giúp hỗ trợ tiêu hóa, kiểm soát đường huyết và giảm cảm giác đầy bụng. Tránh vận động mạnh ngay sau ăn.",
                "exercise"));

        // Dữ liệu về sức khỏe tinh thần
        tipsList.add(new HealthTip(7, "Ngủ đủ 7-8 tiếng mỗi đêm",
                "Giấc ngủ chất lượng rất quan trọng cho sức khỏe thể chất và tinh thần. Nên đi ngủ và thức dậy vào giờ cố định, tránh sử dụng điện thoại trước khi ngủ 30 phút, và tạo môi trường phòng ngủ thoáng mát, yên tĩnh.",
                "mental_health"));

        tipsList.add(new HealthTip(8, "Thực hành thiền và hít thở sâu",
                "Thiền và hít thở sâu giúp giảm stress, cải thiện sự tập trung và tăng cường sức khỏe tinh thần. Chỉ cần 5-10 phút mỗi ngày để thực hành hít thở sâu hoặc thiền định.",
                "mental_health"));

        tipsList.add(new HealthTip(9, "Dành thời gian cho sở thích",
                "Tham gia các hoạt động yêu thích giúp giảm căng thẳng, tăng cảm giác hạnh phúc. Có thể là đọc sách, nghe nhạc, vẽ tranh, làm vườn hay bất kỳ hoạt động nào bạn thích.",
                "mental_health"));

        // Dữ liệu về phòng bệnh
        tipsList.add(new HealthTip(10, "Rửa tay thường xuyên",
                "Rửa tay bằng xà phòng ít nhất 20 giây, đặc biệt trước khi ăn, sau khi đi vệ sinh và khi về nhà. Đây là cách đơn giản nhất để ngăn ngừa lây nhiễm vi khuẩn và virus.",
                "prevention"));

        tipsList.add(new HealthTip(11, "Tiêm phòng đầy đủ",
                "Vaccine giúp phòng ngừa nhiều bệnh nguy hiểm. Nên tiêm phòng đầy đủ theo lịch và cập nhật các vaccine mới khi cần thiết, đặc biệt là vaccine cúm hàng năm.",
                "prevention"));

        tipsList.add(new HealthTip(12, "Khám sức khỏe định kỳ",
                "Khám sức khỏe định kỳ 6 tháng hoặc 1 năm một lần giúp phát hiện sớm các vấn đề sức khỏe. Nên làm xét nghiệm máu, đo huyết áp và các chỉ số sức khỏe cơ bản.",
                "prevention"));

        // Dữ liệu tổng quát
        tipsList.add(new HealthTip(13, "Tránh hút thuốc và rượu bia",
                "Thuốc lá và rượu bia có hại cho sức khỏe, gây ra nhiều bệnh nguy hiểm như ung thư, gan, phổi. Nên từ bỏ hoàn toàn hoặc hạn chế tối đa việc sử dụng các chất này.",
                "general"));

        tipsList.add(new HealthTip(14, "Giữ vệ sinh răng miệng",
                "Đánh răng 2 lần/ngày, dùng chỉ nha khoa và khám nha sĩ định kỳ 6 tháng/lần. Vệ sinh răng miệng tốt không chỉ bảo vệ răng mà còn phòng ngừa các bệnh về tim mạch.",
                "general"));

        tipsList.add(new HealthTip(15, "Duy trì cân nặng hợp lý",
                "Cân nặng hợp lý giúp giảm nguy cơ mắc nhiều bệnh như tiểu đường, tim mạch, khớp. Tính BMI để biết cân nặng lý tưởng và duy trì thông qua chế độ ăn uống và vận động hợp lý.",
                "general"));

        tipsList.add(new HealthTip(16, "Bảo vệ da khỏi ánh nắng",
                "Sử dụng kem chống nắng SPF 30+ khi ra ngoài, mặc quần áo che chắn và đội mũ rộng vành. Tránh phơi nắng vào giữa trúa (10h-14h) khi tia UV mạnh nhất.",
                "prevention"));

        tipsList.add(new HealthTip(17, "Kiểm soát stress hiệu quả",
                "Stress kéo dài ảnh hưởng xấu đến sức khỏe. Tìm cách giảm stress phù hợp như tập yoga, thiền, nghe nhạc, hoặc tâm sự với người thân. Biết nói 'không' khi cần thiết.",
                "mental_health"));

        tipsList.add(new HealthTip(18, "Ăn sáng đầy đủ",
                "Bữa sáng là bữa ăn quan trọng nhất trong ngày, cung cấp năng lượng cho cả ngày làm việc. Nên ăn sáng trong vòng 1 giờ sau khi thức dậy với đủ protein, carb và chất béo lành mạnh.",
                "diet"));

        tipsList.add(new HealthTip(19, "Hạn chế thức khuya",
                "Thức khuya thường xuyên làm rối loạn nhịp sinh học, giảm sức đề kháng và ảnh hưởng đến tinh thần. Nên đi ngủ trước 23h và duy trì giấc ngủ liên tục ít nhất 7-8 tiếng.",
                "general"));

        tipsList.add(new HealthTip(20, "Duy trì mối quan hệ xã hội",
                "Kết nối với gia đình, bạn bè giúp cải thiện sức khỏe tinh thần và kéo dài tuổi thọ. Dành thời gian trò chuyện, chia sẻ và tham gia các hoạt động cộng đồng.",
                "mental_health"));

        filteredList.addAll(tipsList);
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void filterTips(String query, String category) {
        filteredList.clear();

        for (HealthTip tip : tipsList) {
            boolean matchesCategory = category.equals("all") || tip.getCategory().equals(category);
            boolean matchesQuery = query.isEmpty() ||
                    tip.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    tip.getContent().toLowerCase().contains(query.toLowerCase());

            if (matchesCategory && matchesQuery) {
                filteredList.add(tip);
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvHealthTips.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvHealthTips.setVisibility(View.VISIBLE);
        }
    }
}