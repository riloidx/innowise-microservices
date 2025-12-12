package com.innowise.userservice.service;

import com.innowise.userservice.dto.request.UserCreateDto;
import com.innowise.userservice.dto.request.UserUpdateDto;
import com.innowise.userservice.dto.response.UserResponseDto;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.BadRequestException;
import com.innowise.userservice.exception.UserAlreadyExistsException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.specification.UserSpecification;
import com.innowise.userservice.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final UserMapper mapper;
    private final ValidationUtil validationUtil;

    @Override
    @Transactional
    @CachePut(value = "user", key = "#result.id")
    public UserResponseDto create(UserCreateDto userCreateDto) {
        checkEmailNotTaken(userCreateDto.getEmail());
        User user = mapper.toEntity(userCreateDto);

        User savedUser = userRepo.save(user);

        return mapper.toDto(savedUser);
    }

    @Override
    @Transactional
    @CachePut(value = "user", key = "#result.id")
    public UserResponseDto update(long id, UserUpdateDto userUpdateDto) {
        User curUser = getValidatedUserForUpdate(id, userUpdateDto);
        mapper.updateEntityFromDto(userUpdateDto, curUser);

        return mapper.toDto(userRepo.save(curUser));
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "user", key = "#id"),
                    @CacheEvict(value = "cards", key = "#id")
            })
    public void delete(long id) {
        findById(id);

        userRepo.deleteById(id);
    }

    @Override
    @Transactional
    @CachePut(value = "user", key = "#result.id")
    public UserResponseDto changeStatus(long id, boolean status) {
        User curUser = getValidatedUserForChangingStatus(id, status);

        curUser = userRepo.save(curUser);

        return mapper.toDto(curUser);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(long id) {
        return userRepo.findById(id).
                orElseThrow(() -> new UserNotFoundException("id", String.valueOf(id)));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "user", key = "#id")
    public UserResponseDto findDtoById(long id) {
        return mapper.toDto(findById(id));
    }

    @Override
    @Cacheable(value = "user", key = "#email")
    public UserResponseDto findDtoByEmail(String email) {
        User user = userRepo.findByEmail(email).
                orElseThrow(() -> new UserNotFoundException("email", email));
        return mapper.toDto(user);
    }

    @Override
    public Page<UserResponseDto> findAll(String name,
                                         String surname,
                                         LocalDate birthDate,
                                         Boolean active,
                                         Pageable pageable) {
        Specification<User> spec = configureSpecification(name, surname, birthDate, active);

        Page<User> users = userRepo.findAll(spec, pageable);

        return mapper.toDto(users);
    }

    private Specification<User> configureSpecification(String name,
                                                       String surname,
                                                       LocalDate birthDate,
                                                       Boolean active) {
        Specification<User> spec = Specification.unrestricted();

        if (name != null) {
            spec = spec.and(UserSpecification.hasName(name));
        }

        if (surname != null) {
            spec = spec.and(UserSpecification.hasSurname(surname));
        }

        if (birthDate != null) {
            spec = spec.and(UserSpecification.hasBirthDate(birthDate));
        }

        if (active != null) {
            spec = spec.and(UserSpecification.isActive(active));
        }

        return spec;

    }

    private void checkEmailNotTaken(String email) {
        userRepo.findByEmail(email)
                .ifPresent(u -> {
                    throw new UserAlreadyExistsException("email", email);
                });
    }

    @Transactional(readOnly = true)
    public User getValidatedUserForUpdate(long id, UserUpdateDto userUpdateDto) {
        validationUtil.validateMatchingIds(id, userUpdateDto.getId());

        User curUser = findById(id);

        if (!curUser.getEmail().equals(userUpdateDto.getEmail())) {
            checkEmailNotTaken(userUpdateDto.getEmail());
        }

        return curUser;
    }

    @Transactional(readOnly = true)
    public User getValidatedUserForChangingStatus(long id, boolean active) {
        User user = findById(id);

        if (active == user.getActive()) {
            throw new BadRequestException("User with id=" + id + " have status=" + (active ? "active" : "inactive"));
        }

        user.setActive(active);

        return user;
    }
}
