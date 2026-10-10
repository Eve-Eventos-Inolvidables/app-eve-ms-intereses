package com.example.appevemsuserinterests.Repository;

import com.example.appevecommon.Models.User.UserInterest;
import com.example.appevecommon.Repository.IBaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserInterestRepository extends IBaseRepository<UserInterest> {

    boolean existsByUserIdAndCategoryId(Long userId, Long categoryId);
}
