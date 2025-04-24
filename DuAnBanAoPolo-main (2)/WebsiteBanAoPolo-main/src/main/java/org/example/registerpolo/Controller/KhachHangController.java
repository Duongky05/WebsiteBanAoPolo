package org.example.registerpolo.Controller;

import org.example.registerpolo.Entity.KhachHang;
import org.example.registerpolo.Repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/khach-hang")
public class KhachHangController {

    @Autowired
    private KhachHangRepository khachHangRepo;

    // Danh sách khách hàng
    @GetMapping
    public String danhSachKhachHang(Model model) {
        model.addAttribute("khachHang", new KhachHang());
        model.addAttribute("listKhachHang", khachHangRepo.findAll());
        return "KhachHang/khach-hang";
    }

    // Chi tiết khách hàng
    @GetMapping("/detail/{id}")
    public String detailKhachHang(@PathVariable int id, Model model) {
        KhachHang kh = khachHangRepo.findById(id).orElse(null);
        if (kh == null) {
            return "redirect:/khach-hang";
        }
        model.addAttribute("khachHang", kh);
        return "KhachHang/UpdateKhachHang";
    }

    // Form cập nhật khách hàng
    @PostMapping("/update/{id}")
    public String updateKhachHang(@PathVariable int id, @ModelAttribute("khachHang") KhachHang khachHang) {
        KhachHang existingKH = khachHangRepo.findById(id).orElse(null);
        if (existingKH != null) {
            // Giữ lại mã KH và trạng thái cũ
            khachHang.setMaKH(existingKH.getMaKH());
            khachHang.setTrangThai(existingKH.getTrangThai());
            khachHangRepo.save(khachHang);
        }
        return "redirect:/khach-hang";
    }

    // Hiển thị form thêm khách hàng
    @GetMapping("/add")
    public String formThemKH(Model model) {
        KhachHang kh = new KhachHang();
        kh.setTrangThai(true); // Đặt trạng thái mặc định
        model.addAttribute("khachHang", kh);
        return "KhachHang/AddKhachHang";
    }

    // Xử lý lưu khách hàng
    @PostMapping("/add")
    public String themKhachHang(@ModelAttribute("khachHang") KhachHang khachHang) {
        khachHang.setMaKH(generateMaKH()); // Tự động tạo mã KH
        khachHang.setTrangThai(true); // Đảm bảo trạng thái true
        khachHangRepo.save(khachHang);
        return "redirect:/khach-hang";
    }

    // Generate mã KH tự động dạng KH001, KH002, ...
    private String generateMaKH() {
        KhachHang lastKH = khachHangRepo.findTopByOrderByIdDesc();
        if (lastKH == null || lastKH.getMaKH() == null) {
            return "KH001";
        }

        String lastMa = lastKH.getMaKH(); // ví dụ: KH007
        int number = Integer.parseInt(lastMa.substring(2)); // lấy số từ mã cuối
        return String.format("KH%03d", number + 1); // KH008
    }

    // Xử lý tìm kiếm khách hàng
    @GetMapping("/search")
    public String searchKhachHang(@RequestParam(required = false) String sdt, Model model) {
        if (sdt != null && !sdt.trim().isEmpty()) {
            KhachHang khachHang = khachHangRepo.findBySdt(sdt);
            if (khachHang != null) {
                model.addAttribute("listKhachHang", List.of(khachHang));
            } else {
                model.addAttribute("listKhachHang", List.of());
                model.addAttribute("message", "Không tìm thấy khách hàng với số điện thoại: " + sdt);
            }
        } else {
            model.addAttribute("listKhachHang", khachHangRepo.findAll());
        }
        model.addAttribute("sdt", sdt); // Giữ lại giá trị tìm kiếm
        return "KhachHang/khach-hang";
    }

}
