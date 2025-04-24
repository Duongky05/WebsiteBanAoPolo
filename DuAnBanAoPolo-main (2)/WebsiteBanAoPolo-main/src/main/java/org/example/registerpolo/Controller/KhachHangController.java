package org.example.registerpolo.Controller;

import org.example.registerpolo.Entity.KhachHang;
import org.example.registerpolo.Repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
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
    public String danhSachKhachHang(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) Boolean updateSuccess,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<KhachHang> khachHangPage = khachHangRepo.findAll(pageable);
        
        // Log thông tin phân trang
        System.out.println("=== THÔNG TIN PHÂN TRANG ===");
        System.out.println("Trang hiện tại: " + page);
        System.out.println("Kích thước mỗi trang: " + size);
        System.out.println("Tổng số trang: " + khachHangPage.getTotalPages());
        System.out.println("Tổng số khách hàng: " + khachHangPage.getTotalElements());
        System.out.println("Số khách hàng trong trang hiện tại: " + khachHangPage.getContent().size());
        System.out.println("=========================");
        
        if (updateSuccess != null) {
            if (updateSuccess) {
                model.addAttribute("messageType", "success");
                model.addAttribute("message", "Cập nhật thông tin khách hàng thành công!");
            } else {
                model.addAttribute("messageType", "warning");
                model.addAttribute("message", "Có lỗi xảy ra khi cập nhật thông tin khách hàng!");
            }
        }
        
        model.addAttribute("khachHang", new KhachHang());
        model.addAttribute("listKhachHang", khachHangPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", khachHangPage.getTotalPages());
        model.addAttribute("totalItems", khachHangPage.getTotalElements());
        model.addAttribute("size", size);
        
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
            // Giữ lại mã KH
            khachHang.setMaKH(existingKH.getMaKH());
            // Cập nhật trạng thái từ form
            System.out.println("Trạng thái mới: " + khachHang.getTrangThai());
            khachHangRepo.save(khachHang);
            return "redirect:/khach-hang?updateSuccess=true";
        }
        return "redirect:/khach-hang?updateError=true";
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

    // Xử lý tìm kiếm khách hàng
    @GetMapping("/search")
    public String searchKhachHang(
            @RequestParam(required = false) String sdt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<KhachHang> khachHangPage;
        
        if (sdt != null && !sdt.trim().isEmpty()) {
            // Tìm kiếm theo số điện thoại
            KhachHang khachHang = khachHangRepo.findBySdt(sdt);
            if (khachHang != null) {
                // Nếu tìm thấy, tạo một Page với một phần tử
                List<KhachHang> content = List.of(khachHang);
                khachHangPage = new PageImpl<>(content, pageable, 1);
                model.addAttribute("messageType", "success");
                model.addAttribute("message", "Đã tìm thấy khách hàng với số điện thoại: " + sdt);
            } else {
                // Nếu không tìm thấy, tạo một Page rỗng
                khachHangPage = new PageImpl<>(List.of(), pageable, 0);
                model.addAttribute("messageType", "warning");
                model.addAttribute("message", "Không tìm thấy khách hàng với số điện thoại: " + sdt);
            }
        } else {
            // Nếu không có số điện thoại, hiển thị tất cả
            khachHangPage = khachHangRepo.findAll(pageable);
        }
        
        // Log thông tin phân trang
        System.out.println("=== THÔNG TIN PHÂN TRANG TÌM KIẾM ===");
        System.out.println("Số điện thoại tìm kiếm: " + sdt);
        System.out.println("Trang hiện tại: " + page);
        System.out.println("Kích thước mỗi trang: " + size);
        System.out.println("Tổng số trang: " + khachHangPage.getTotalPages());
        System.out.println("Tổng số khách hàng: " + khachHangPage.getTotalElements());
        System.out.println("Số khách hàng trong trang hiện tại: " + khachHangPage.getContent().size());
        System.out.println("=========================");
        
        model.addAttribute("khachHang", new KhachHang());
        model.addAttribute("listKhachHang", khachHangPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", khachHangPage.getTotalPages());
        model.addAttribute("totalItems", khachHangPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sdt", sdt); // Giữ lại giá trị tìm kiếm
        
        return "KhachHang/khach-hang";
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
}
