package com.bicycle.marketplace.repository;

import com.bicycle.marketplace.entities.BicycleLibrary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IBicycleLibraryRepository extends JpaRepository<BicycleLibrary, Integer> {

    // Tìm theo tên brand (không phân biệt hoa thường)
    @Query("SELECT b FROM BicycleLibrary b WHERE LOWER(b.brand.name) LIKE LOWER(CONCAT('%', :brandName, '%'))")
    List<BicycleLibrary> findByBrandName(@Param("brandName") String brandName);

    // Tìm theo tên brand + năm sản xuất
    @Query("SELECT b FROM BicycleLibrary b WHERE LOWER(b.brand.name) LIKE LOWER(CONCAT('%', :brandName, '%')) AND b.yearManufacture = :year")
    List<BicycleLibrary> findByBrandNameAndYear(@Param("brandName") String brandName, @Param("year") int year);
}
