package com.example.blooddonor;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Controller
public class BloodDonorController {

    private final JdbcTemplate jdbcTemplate;

    public BloodDonorController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // HOME
    @GetMapping("/")
    public String home() {
        return "index";
    }


    // =========================
    // USER REGISTER
    // =========================

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String name,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam String city,
            @RequestParam String bloodGroup,
            Model model) {

        try {

            String sql =
                    "INSERT INTO users " +
                    "(username,password,name,phone,email,city,blood_group) " +
                    "VALUES (?,?,?,?,?,?,?)";

            jdbcTemplate.update(
                    sql,
                    username,
                    password,
                    name,
                    phone,
                    email,
                    city,
                    bloodGroup
            );

            model.addAttribute(
                    "message",
                    "Registration successful!"
            );

            return "login";

        } catch (Exception e) {

            model.addAttribute(
                    "error",
                    "Username already exists!"
            );

            return "register";
        }
    }


    // =========================
    // USER LOGIN
    // =========================

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            Model model) {

        String sql =
                "SELECT * FROM users " +
                "WHERE username=? AND password=?";

        List<User> users = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    User u = new User();

                    u.setId(rs.getInt("id"));
                    u.setUsername(
                            rs.getString("username")
                    );
                    u.setPassword(
                            rs.getString("password")
                    );
                    u.setName(
                            rs.getString("name")
                    );
                    u.setPhone(
                            rs.getString("phone")
                    );
                    u.setEmail(
                            rs.getString("email")
                    );
                    u.setCity(
                            rs.getString("city")
                    );
                    u.setBloodGroup(
                            rs.getString("blood_group")
                    );

                    return u;
                },
                username,
                password
        );

        if (!users.isEmpty()) {

            model.addAttribute(
                    "user",
                    users.get(0)
            );

            return "dashboard";
        }

        model.addAttribute(
                "error",
                "Invalid username or password"
        );

        return "login";
    }


    // =========================
    // SHOW DONORS
    // =========================

    @GetMapping("/donors")
    public String donors(
            @RequestParam(required = false)
            String bloodGroup,
            Model model) {

        String sql;

        List<Donor> donors;

        if (bloodGroup == null ||
                bloodGroup.equals("ALL")) {

            sql =
                    "SELECT * FROM donors " +
                    "WHERE available=true";

            donors = getDonors(sql);

        } else {

            sql =
                    "SELECT * FROM donors " +
                    "WHERE blood_group=? " +
                    "AND available=true";

            donors = jdbcTemplate.query(
                    sql,
                    (rs, rowNum) -> {

                        Donor d = new Donor();

                        d.setId(
                                rs.getInt("id")
                        );

                        d.setName(
                                rs.getString("name")
                        );

                        d.setBloodGroup(
                                rs.getString("blood_group")
                        );

                        d.setPhone(
                                rs.getString("phone")
                        );

                        d.setCity(
                                rs.getString("city")
                        );

                        d.setAvailable(
                                rs.getBoolean("available")
                        );

                        return d;
                    },
                    bloodGroup
            );
        }

        model.addAttribute(
                "donors",
                donors
        );

        return "donors";
    }


    private List<Donor> getDonors(String sql) {

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    Donor d = new Donor();

                    d.setId(
                            rs.getInt("id")
                    );

                    d.setName(
                            rs.getString("name")
                    );

                    d.setBloodGroup(
                            rs.getString("blood_group")
                    );

                    d.setPhone(
                            rs.getString("phone")
                    );

                    d.setCity(
                            rs.getString("city")
                    );

                    d.setAvailable(
                            rs.getBoolean("available")
                    );

                    return d;
                }
        );
    }


    // =========================
    // ADMIN LOGIN
    // =========================

    @GetMapping("/admin")
    public String adminLogin() {
        return "admin-login";
    }

    @PostMapping("/admin")
    public String adminLogin(
            @RequestParam String username,
            @RequestParam String password,
            Model model) {

        if (username.equals("admin")
                && password.equals("admin123")) {

            List<Donor> donors =
                    getDonors("SELECT * FROM donors");

            model.addAttribute(
                    "donors",
                    donors
            );

            return "admin";
        }

        model.addAttribute(
                "error",
                "Invalid admin login"
        );

        return "admin-login";
    }


    // =========================
    // ADD DONOR
    // =========================

    @PostMapping("/admin/add")
    public String addDonor(
            @RequestParam String name,
            @RequestParam String bloodGroup,
            @RequestParam String phone,
            @RequestParam String city) {

        String sql =
                "INSERT INTO donors " +
                "(name,blood_group,phone,city,available) " +
                "VALUES (?,?,?,?,true)";

        jdbcTemplate.update(
                sql,
                name,
                bloodGroup,
                phone,
                city
        );

        return "redirect:/admin";
    }


    // =========================
    // DELETE DONOR
    // =========================

    @GetMapping("/admin/delete/{id}")
    public String deleteDonor(
            @PathVariable int id) {

        jdbcTemplate.update(
                "DELETE FROM donors WHERE id=?",
                id
        );

        return "redirect:/admin";
    }


    // =========================
    // EXCEL DOWNLOAD
    // =========================

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadExcel()
            throws Exception {

        List<Donor> donors =
                getDonors(
                        "SELECT * FROM donors " +
                        "WHERE available=true"
                );

        Workbook workbook =
                new XSSFWorkbook();

        Sheet sheet =
                workbook.createSheet("Blood Donors");

        Row header =
                sheet.createRow(0);

        header.createCell(0)
                .setCellValue("Name");

        header.createCell(1)
                .setCellValue("Blood Group");

        header.createCell(2)
                .setCellValue("Phone");

        header.createCell(3)
                .setCellValue("City");

        header.createCell(4)
                .setCellValue("Available");

        int rowNumber = 1;

        for (Donor donor : donors) {

            Row row =
                    sheet.createRow(rowNumber++);

            row.createCell(0)
                    .setCellValue(
                            donor.getName()
                    );

            row.createCell(1)
                    .setCellValue(
                            donor.getBloodGroup()
                    );

            row.createCell(2)
                    .setCellValue(
                            donor.getPhone()
                    );

            row.createCell(3)
                    .setCellValue(
                            donor.getCity()
                    );

            row.createCell(4)
                    .setCellValue(
                            donor.isAvailable()
                    );
        }

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        workbook.write(output);

        workbook.close();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=blood_donors.xlsx"
                )
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .body(output.toByteArray());
    }
}