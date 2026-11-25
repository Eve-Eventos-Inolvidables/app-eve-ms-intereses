package com.example.appevemsuserinterests.Service.UserInterest;

import com.example.appevecommon.Models.User.UserInterest;
import com.example.appevecommon.Service.UpdatableService;
import com.example.appevecommon.Service.Utilities.Responses.Ok.PageResult;
import com.example.appevemsuserinterests.Config.RedisCacheConfig;
import com.example.appevemsuserinterests.Repository.IUserInterestRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.UserInterestBaseDto;
import org.openapitools.model.UserInterestDto;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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

    private static final String KEY_PREFIX = "api_user_interest:";

    private final CacheManager cacheManager;

    public UserInterestService(IUserInterestRepository repository,
                               UserInterestMapper mapper,
                               CacheManager cacheManager) {
        super(repository, mapper);
        this.cacheManager = cacheManager;
    }

    @Override
    @Cacheable(value = RedisCacheConfig.USER_INTEREST_SECTION, key = "'" + KEY_PREFIX + "' + #id")
    public UserInterestDto getById(Long id) {
        log.debug("Leyendo interés id={} de la base de datos", id);
        return super.getById(id);
    }

    @CachePut(value = RedisCacheConfig.USER_INTEREST_SECTION, key = "'" + KEY_PREFIX + "' + #result.id")
    public UserInterestDto create(UserInterestBaseDto dto) {
        log.info("Asignando interés: usuario={}, categoría={}", dto.getUserId(), dto.getCategoryId());

        UserInterest entity = mapper.toEntity(dto);
        if (entity.getMarkedAt() == null) {
            entity.setMarkedAt(LocalDate.now());
        }
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @CachePut(value = RedisCacheConfig.USER_INTEREST_SECTION, key = "'" + KEY_PREFIX + "' + #id")
    public UserInterestDto update(Long id, UserInterestBaseDto dto) {
        log.info("Actualizando interés id={}", id);
        return super.update(id, dto);
    }

    @Override
    @CacheEvict(value = RedisCacheConfig.USER_INTEREST_SECTION, key = "'" + KEY_PREFIX + "' + #id")
    public boolean delete(Long id) {
        log.info("Archivando interés id={}", id);
        boolean deleted = super.delete(id);
        log.debug("Resultado del borrado lógico id={}: {}", id, deleted);
        return deleted;
    }

    @Override
    public PageResult<UserInterestDto> getByFilter(UserInterestFilter filter) {
        PageResult<UserInterestDto> page = super.getByFilter(filter);
        Cache cache = cacheManager.getCache(RedisCacheConfig.USER_INTEREST_SECTION);
        if (cache != null) {
            page.items().forEach(item -> cache.put(KEY_PREFIX + item.getId(), item));
        }
        return page;
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
