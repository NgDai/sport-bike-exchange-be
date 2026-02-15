package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.InspectionReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IInspectionReportRepository extends JpaRepository<InspectionReport, Integer> {
}
