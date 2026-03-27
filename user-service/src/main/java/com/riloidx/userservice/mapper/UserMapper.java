package com.riloidx.userservice.mapper;

import com.riloidx.userservice.dto.request.UserCreateDto;
import com.riloidx.userservice.dto.request.UserUpdateDto;
import com.riloidx.userservice.dto.response.UserResponseDto;
import com.riloidx.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", uses = {PaymentCardMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    User toEntity(UserCreateDto userCreateDto);

    UserResponseDto toDto(User user);

    default Page<UserResponseDto> toDto(Page<User> users) {
        return users.map(this::toDto);
    }

    void updateEntityFromDto(UserUpdateDto userUpdateDto,
                             @MappingTarget User user);
}
