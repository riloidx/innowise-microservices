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
import com.innowise.userservice.util.ValidationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private UserMapper mapper;

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private Pageable pageable;

    @InjectMocks
    private UserServiceImpl service;


    @Test
    void createShouldSaveAndReturnDtoWhenEmailNotTaken() {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setEmail("test@test.com");
        createDto.setName("Test");
        createDto.setSurname("Test");
        createDto.setBirthDate(LocalDate.of(1990, 1, 1));

        User userEntity = User.builder()
                .id(1L)
                .name("Test")
                .surname("Test")
                .email("test@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .active(true)
                .build();

        UserResponseDto userDto = UserResponseDto.builder()
                .id(1L)
                .name("Test")
                .surname("Test")
                .email("test@example.com")
                .birthDate(LocalDate.of(2000, 1, 1))
                .active(true)
                .build();

        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(mapper.toEntity(createDto)).thenReturn(userEntity);
        when(userRepo.save(userEntity)).thenReturn(userEntity);
        when(mapper.toDto(userEntity)).thenReturn(userDto);

        UserResponseDto result = service.create(createDto);

        assertEquals(userDto, result);
        verify(userRepo).findByEmail("test@test.com");
        verify(userRepo).save(userEntity);
        verify(mapper).toEntity(createDto);
        verify(mapper).toDto(userEntity);
    }

    @Test
    void createShouldThrowWhenEmailTaken() {
        UserCreateDto createDto = new UserCreateDto();
        String email = "test@test.com";
        createDto.setEmail(email);

        User existingUser = User.builder().id(1L).email(email).build();
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(existingUser));

        assertThrows(UserAlreadyExistsException.class, () -> service.create(createDto));
        verify(userRepo).findByEmail(email);
        verifyNoMoreInteractions(userRepo, mapper);
    }

    @Test
    void updateShouldSaveAndReturnDtoWhenValid() {
        long userId = 1L;

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setId(userId);
        updateDto.setEmail("new@test.com");

        User currentUser = User.builder()
                .id(userId)
                .email("old@test.com")
                .active(true)
                .build();

        User updatedUser = User.builder()
                .id(userId)
                .email("new@test.com")
                .active(true)
                .build();

        UserResponseDto userDto = UserResponseDto.builder().id(userId).email("new@test.com").build();

        doNothing().when(validationUtil).validateMatchingIds(userId, updateDto.getId());
        when(userRepo.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userRepo.findByEmail("new@test.com")).thenReturn(Optional.empty());
        doNothing().when(mapper).updateEntityFromDto(updateDto, currentUser);
        when(userRepo.save(currentUser)).thenReturn(updatedUser);
        when(mapper.toDto(updatedUser)).thenReturn(userDto);

        UserResponseDto result = service.update(userId, updateDto);

        assertEquals(userDto, result);
        verify(validationUtil).validateMatchingIds(userId, updateDto.getId());
        verify(userRepo).findById(userId);
        verify(userRepo).save(currentUser);
        verify(mapper).updateEntityFromDto(updateDto, currentUser);
        verify(mapper).toDto(updatedUser);
    }

    @Test
    void deleteShouldCallRepoWhenUserExists() {
        long userId = 1L;
        User user = User.builder().id(userId).build();
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        service.delete(userId);

        verify(userRepo).findById(userId);
        verify(userRepo).deleteById(userId);
    }

    @Test
    void changeStatusShouldUpdateActiveWhenDifferent() {
        long userId = 1L;
        User user = User.builder().id(userId).active(false).build();
        User updatedUser = User.builder().id(userId).active(true).build();
        UserResponseDto dto = UserResponseDto.builder().id(userId).active(true).build();

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(userRepo.save(user)).thenReturn(updatedUser);
        when(mapper.toDto(updatedUser)).thenReturn(dto);

        UserResponseDto result = service.changeStatus(userId, true);

        assertEquals(dto, result);
        verify(userRepo).save(user);
        verify(mapper).toDto(updatedUser);
    }

    @Test
    void changeStatusShouldThrowWhenStatusAlreadySet() {
        long userId = 1L;
        User user = User.builder().id(userId).active(true).build();

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> service.changeStatus(userId, true));
        verify(userRepo).findById(userId);
        verifyNoMoreInteractions(userRepo, mapper);
    }

    @Test
    void findByIdReturnUserWhenUserExists() {
        long id = 1L;

        User user = User.builder().
                id(id).
                name("Test").
                build();

        when(userRepo.findById(id)).thenReturn(Optional.of(user));

        User result = service.findById(id);

        assertEquals(user, result);
        verify(userRepo).findById(id);
    }

    @Test
    void findByIdThrowExceptionWhenUserDoesNotExist() {
        long id = 1L;
        when(userRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.findById(id));
        verify(userRepo).findById(id);
    }

    @Test
    void findAllShouldReturnEmptyPageWhenAllParametersNull() {
        Page<User> usersPage = new PageImpl<>(List.of());
        Page<UserResponseDto> dtoPage = new PageImpl<>(List.of());

        when(userRepo.findAll(any(Specification.class), eq(pageable))).thenReturn(usersPage);
        when(mapper.toDto(usersPage)).thenReturn(dtoPage);

        Page<UserResponseDto> result = service.findAll(null, null, null, null, pageable);

        assertEquals(dtoPage, result);
        verify(userRepo).findAll(any(Specification.class), eq(pageable));
        verify(mapper).toDto(usersPage);
    }

    @Test
    void findAllShouldBuildSpecificationWhenSomeParametersProvided() {
        String name = "Alice";
        Boolean active = true;
        String email = "alice@test.com";

        User user = User.builder()
                .id(1L)
                .name(name)
                .surname("Smith")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email(email)
                .active(active)
                .build();

        UserResponseDto userDto = UserResponseDto.builder()
                .id(1L)
                .name(name)
                .surname("Smith")
                .build();

        Page<User> usersPage = new PageImpl<>(List.of(user));
        Page<UserResponseDto> dtoPage = new PageImpl<>(List.of(userDto));

        when(userRepo.findAll(any(Specification.class), eq(pageable))).thenReturn(usersPage);
        when(mapper.toDto(usersPage)).thenReturn(dtoPage);

        Page<UserResponseDto> result = service.findAll(name, null, null, active, pageable);

        assertEquals(dtoPage, result);
        verify(userRepo).findAll(any(Specification.class), eq(pageable));
        verify(mapper).toDto(usersPage);
    }

    @Test
    void findAllShouldBuildSpecificationWhenAllParametersProvided() {
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        String name = "Test";
        String surname = "Test";
        String email = "test@test.com";
        Boolean active = true;

        User user = User.builder()
                .id(1L)
                .name(name)
                .surname(surname)
                .birthDate(birthDate)
                .email(email)
                .active(active)
                .build();

        UserResponseDto userDto = UserResponseDto.builder()
                .id(1L)
                .name(name)
                .surname(surname)
                .birthDate(birthDate)
                .email(email)
                .active(active)
                .build();

        Page<User> usersPage = new PageImpl<>(List.of(user));
        Page<UserResponseDto> dtoPage = new PageImpl<>(List.of(userDto));

        when(userRepo.findAll(any(Specification.class), eq(pageable))).thenReturn(usersPage);
        when(mapper.toDto(usersPage)).thenReturn(dtoPage);

        Page<UserResponseDto> result = service.findAll(name, surname, birthDate, active, pageable);

        assertEquals(dtoPage, result);
        verify(userRepo).findAll(any(Specification.class), eq(pageable));
        verify(mapper).toDto(usersPage);
    }

}