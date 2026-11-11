package com.codegym.bds_backend.batdongsan;

import com.codegym.bds_backend.batdongsan.dto.BatDongSanSearchRequest;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class BatDongSanSpecifications {

    private static final char MYSQL_SAFE_LIKE_ESCAPE = '!';

    private BatDongSanSpecifications() {
    }

    public static Specification<BatDongSan> filterBatDongSan(
            BatDongSanSearchRequest request, TinhTrangBds tinhTrang, Long khachHangId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            addEqual(predicates, cb, root, "loaiBds", request.loaiBds());
            addEqual(predicates, cb, root, "viTri", request.viTri());
            addEqual(predicates, cb, root, "huong", request.huong());
            addEqual(predicates, cb, root, "tinhTrang", tinhTrang);
            addRange(predicates, cb, root, "dienTich", request.dienTichTu(), request.dienTichDen());
            addRange(predicates, cb, root, "gia", request.giaTu(), request.giaDen());
            if (khachHangId != null) {
                predicates.add(cb.equal(root.get("khachHang").get("id"), khachHangId));
            }
            if (StringUtils.hasText(request.tuKhoa())) {
                String pattern = "%" + escapeLike(request.tuKhoa().trim().toLowerCase(Locale.ROOT)) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.<String>get("maBds")), pattern, MYSQL_SAFE_LIKE_ESCAPE),
                        cb.like(cb.lower(root.<String>get("diaChi")), pattern, MYSQL_SAFE_LIKE_ESCAPE)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addEqual(List<Predicate> predicates, CriteriaBuilder cb, Root<BatDongSan> root,
            String attribute, Object value) {
        if (value != null) {
            predicates.add(cb.equal(root.get(attribute), value));
        }
    }

    private static void addRange(List<Predicate> predicates, CriteriaBuilder cb, Root<BatDongSan> root,
            String attribute, BigDecimal from, BigDecimal to) {
        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get(attribute), from));
        }
        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(root.<BigDecimal>get(attribute), to));
        }
    }

    static String escapeLike(String value) {
        return value
                .replace(String.valueOf(MYSQL_SAFE_LIKE_ESCAPE), "" + MYSQL_SAFE_LIKE_ESCAPE + MYSQL_SAFE_LIKE_ESCAPE)
                .replace("%", MYSQL_SAFE_LIKE_ESCAPE + "%")
                .replace("_", MYSQL_SAFE_LIKE_ESCAPE + "_");
    }
}
