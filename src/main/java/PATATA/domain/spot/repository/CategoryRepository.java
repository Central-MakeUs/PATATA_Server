package PATATA.domain.spot.repository;

import PATATA.domain.spot.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
