package gift.option.repository;

import gift.option.domain.Option;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {

    Optional<Option> findByNameAndProductId(String optionName, Long productId);

    List<Option> findByProductId(Long productId);

    void deleteByProductId(Long productId);
}
