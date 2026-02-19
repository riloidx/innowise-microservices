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
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        log.info("Creating new user with email: {}", userCreateDto.getEmail());
        
        checkEmailNotTaken(userCreateDto.getEmail());
        User user = mapper.toEntity(userCreateDto);

        User savedUser = userRepo.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return mapper.toDto(savedUser);
    }

    @Override
    @Transactional
    @CachePut(value = "user", key = "#result.id")
    public UserResponseDto update(long id, UserUpdateDto userUpdateDto) {
        log.info("Updating user with ID: {}", id);
        
        User curUser = getValidatedUserForUpdate(id, userUpdateDto);
        mapper.updateEntityFromDto(userUpdateDto, curUser);

        User savedUser = userRepo.save(curUser);
        log.info("User updated successfully with ID: {}", id);
        
        return mapper.toDto(savedUser);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "user", key = "#id"),
                    @CacheEvict(value = "cards", key = "#id")
            })
    public void delete(long id) {
        log.info("Deleting user with ID: {}", id);
        
        findById(id);

        userRepo.deleteById(id);
        log.info("User deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional
    @CachePut(value = "user", key = "#result.id")
    public UserResponseDto changeStatus(long id, boolean status) {
        log.info("Changing user status - ID: {}, new status: {}", id, status ? "active" : "inactive");
        
        User curUser = getValidatedUserForChangingStatus(id, status);

        curUser = userRepo.save(curUser);
        log.info("User status changed successfully for ID: {}", id);

        return mapper.toDto(curUser);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(long id) {
        log.debug("Finding user by ID: {}", id);
        return userRepo.findById(id).
                orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new UserNotFoundException("id", String.valueOf(id));
                });
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
        log.debug("Finding user by email: {}", email);
        User user = userRepo.findByEmail(email).
                orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UserNotFoundException("email", email);
                });
        return mapper.toDto(user);
    }

    @Override
    public Page<UserResponseDto> findAll(String name,
                                         String surname,
                                         LocalDate birthDate,
                                         Boolean active,
                                         Pageable pageable) {
        log.debug("Finding all users with filters - name: {}, surname: {}, active: {}", name, surname, active);
        
        Specification<User> spec = configureSpecification(name, surname, birthDate, active);

        Page<User> users = userRepo.findAll(spec, pageable);
        log.debug("Found {} users", users.getTotalElements());

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
                    log.warn("User with email already exists: {}", email);
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
            log.warn("User with ID {} already has status: {}", id, active ? "active" : "inactive");
            throw new BadRequestException("User with id=" + id + " have status=" + (active ? "active" : "inactive"));
        }

        user.setActive(active);

        return user;
    }
}
