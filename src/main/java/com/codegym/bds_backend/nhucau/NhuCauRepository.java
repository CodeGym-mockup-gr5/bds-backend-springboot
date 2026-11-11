package com.codegym.bds_backend.nhucau;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NhuCauRepository extends JpaRepository<NhuCau, Long>, JpaSpecificationExecutor<NhuCau> {
}
