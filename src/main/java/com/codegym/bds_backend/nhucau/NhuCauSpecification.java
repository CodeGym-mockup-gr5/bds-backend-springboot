package com.codegym.bds_backend.nhucau;

import com.codegym.bds_backend.nhucau.dto.NhuCauKhachHangFilter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class NhuCauSpecification {
    private NhuCauSpecification() {
    }

    public static Specification<NhuCau> timNhuCauCuaKhachHang(Long khachHangId, NhuCauKhachHangFilter filter) {
        List<Specification<NhuCau>> dieuKien = new ArrayList<>();
        dieuKien.add((root, query, cb) -> cb.equal(root.get("khachHang").get("id"), khachHangId));
        if (filter.loaiBds() != null) {
            dieuKien.add((root, query, cb) -> cb.equal(root.get("loaiBds"), filter.loaiBds()));
        }
        if (filter.viTri() != null) {
            dieuKien.add((root, query, cb) -> cb.equal(root.get("viTri"), filter.viTri()));
        }
        if (filter.dienTich() != null) {
            dieuKien.add((root, query, cb) -> cb.and(
                    cb.lessThanOrEqualTo(root.get("dienTichMin"), filter.dienTich()),
                    cb.greaterThanOrEqualTo(root.get("dienTichMax"), filter.dienTich())));
        }
        return Specification.allOf(dieuKien);
    }
}
