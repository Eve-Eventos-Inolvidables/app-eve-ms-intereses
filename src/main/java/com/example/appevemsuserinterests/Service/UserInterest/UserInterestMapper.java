package com.example.appevemsuserinterests.Service.UserInterest;

import com.example.appevecommon.Models.Event.Addons.EventCategory;
import com.example.appevecommon.Models.User.User;
import com.example.appevecommon.Models.User.UserInterest;
import com.example.appevecommon.Service.Utilities.UpdatableMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.openapitools.model.UserInterestBaseDto;
import org.openapitools.model.UserInterestDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserInterestMapper
        extends UpdatableMapper<UserInterest, UserInterestDto, UserInterestBaseDto> {

    @Override
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "category.id", target = "categoryId")
    UserInterestDto toDto(UserInterest entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "archived", ignore = true)
    @Mapping(source = "userId", target = "user")
    @Mapping(source = "categoryId", target = "category")
    UserInterest toEntity(UserInterestBaseDto dto);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "archived", ignore = true)
    @Mapping(source = "userId", target = "user")
    @Mapping(source = "categoryId", target = "category")
    void updateEntityFromDto(UserInterestBaseDto dto, @MappingTarget UserInterest entity);

    default User userFromId(Long id) {
        if (id == null) return null;
        User user = new User();
        user.setId(id);
        return user;
    }

    default EventCategory categoryFromId(Long id) {
        if (id == null) return null;
        EventCategory category = new EventCategory();
        category.setId(id);
        return category;
    }
}