package com.example.appevemsuserinterests.Service.UserInterest;

import com.example.appevecommon.Models.User.UserInterest;
import com.example.appevecommon.Service.UpdatableService;
import com.example.appevemsuserinterests.Repository.IUserInterestRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.UserInterestBaseDto;
import org.openapitools.model.UserInterestDto;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class UserInterestService extends UpdatableService<
        UserInterest, UserInterestDto, UserInterestBaseDto, UserInterestMapper, UserInterestFilter> {

    public UserInterestService(IUserInterestRepository repository, UserInterestMapper mapper) {
        super(repository, mapper);
    }

    public UserInterestDto create(UserInterestBaseDto dto) {
        log.info("Asignando interés: usuario={}, categoría={}", dto.getUserId(), dto.getCategoryId());

        UserInterest entity = mapper.toEntity(dto);
        if (entity.getMarkedAt() == null) {
            entity.setMarkedAt(LocalDate.now());
        }
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public Specification<UserInterest> toSpecification(UserInterestFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter != null) {
                if (filter.getUserId() != null) {
                    predicates.add(cb.equal(root.get("user").get("id"), filter.getUserId()));
                }
                if (filter.getCategoryId() != null) {
                    predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
