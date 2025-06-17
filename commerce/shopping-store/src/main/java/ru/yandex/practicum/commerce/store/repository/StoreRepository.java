package ru.yandex.practicum.commerce.store.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.commerce.dto.store.ProductCategory;
import ru.yandex.practicum.commerce.store.model.Product;

import java.util.List;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Product, UUID> {
    List<Product> findAllByProductCategory(ProductCategory category, Pageable pageable);
}
