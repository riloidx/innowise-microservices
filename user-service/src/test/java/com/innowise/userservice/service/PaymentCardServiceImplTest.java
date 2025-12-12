package com.innowise.userservice.service;

import com.innowise.userservice.config.CardProperties;
import com.innowise.userservice.dto.request.PaymentCardCreateDto;
import com.innowise.userservice.dto.request.PaymentCardUpdateDto;
import com.innowise.userservice.dto.response.PaymentCardResponseDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.BadRequestException;
import com.innowise.userservice.exception.PaymentCardAlreadyExistsException;
import com.innowise.userservice.exception.PaymentCardLimitExceededException;
import com.innowise.userservice.exception.PaymentCardNotFoundException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.util.ValidationUtil;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceImplTest {

    @Mock
    private PaymentCardRepository paymentCardRepo;

    @Mock
    private PaymentCardMapper mapper;

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private UserService userService;

    @Mock
    private CardProperties cardProperties;

    @Mock
    private Pageable pageable;


    @InjectMocks
    private PaymentCardServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(cardProperties.getMaxLimit()).thenReturn(5);
    }

    @Test
    void createShouldSaveAndReturnDtoWhenValid() {
        PaymentCardCreateDto createDto = new PaymentCardCreateDto();
        createDto.setUserId(1L);
        createDto.setNumber("12341234");

        User user = User.builder()
                .id(1L)
                .name("Test")
                .surname("Test")
                .paymentCards(new ArrayList<>())
                .build();

        PaymentCard cardEntity = PaymentCard.builder().id(1L).number("12341234").build();
        PaymentCardResponseDto cardDto = PaymentCardResponseDto.builder().id(1L).number("12341234").build();

        when(paymentCardRepo.findByNumber("12341234")).thenReturn(Optional.empty());
        when(userService.findById(1L)).thenReturn(user);
        when(mapper.toEntity(createDto)).thenReturn(cardEntity);
        when(paymentCardRepo.save(cardEntity)).thenReturn(cardEntity);
        when(mapper.toDto(cardEntity)).thenReturn(cardDto);

        PaymentCardResponseDto result = service.create(createDto);

        assertEquals(cardDto, result);
        verify(paymentCardRepo).findByNumber("12341234");
        verify(userService).findById(1L);
        verify(paymentCardRepo).save(cardEntity);
        verify(mapper).toDto(cardEntity);
    }

    @Test
    void createShouldThrowWhenNumberAlreadyExists() {
        PaymentCardCreateDto createDto = new PaymentCardCreateDto();
        createDto.setNumber("12341234");

        PaymentCard existing = PaymentCard.builder().id(1L).number("12341234").build();
        when(paymentCardRepo.findByNumber("12341234")).thenReturn(Optional.of(existing));

        assertThrows(PaymentCardAlreadyExistsException.class, () -> service.create(createDto));
        verify(paymentCardRepo).findByNumber("12341234");
        verifyNoMoreInteractions(paymentCardRepo, userService, mapper);
    }

    @Test
    void createShouldThrowWhenUserExceedsCardLimit() {
        PaymentCardCreateDto createDto = new PaymentCardCreateDto();
        createDto.setUserId(1L);
        createDto.setNumber("12341234");

        List<PaymentCard> cards = new ArrayList<>();
        for (int i = 0; i < 5; i++) cards.add(PaymentCard.builder().id((long) i).build());

        User user = User.builder().id(1L).paymentCards(cards).build();

        when(paymentCardRepo.findByNumber("12341234")).thenReturn(Optional.empty());
        when(userService.findById(1L)).thenReturn(user);

        assertThrows(PaymentCardLimitExceededException.class, () -> service.create(createDto));
    }

    @Test
    void updateShouldSaveAndReturnDtoWhenValid() {
        long id = 1L;
        PaymentCardUpdateDto updateDto = new PaymentCardUpdateDto();
        updateDto.setId(id);
        updateDto.setNumber("12341234");

        PaymentCard existingCard = PaymentCard.builder().id(id).number("12341234").build();
        PaymentCard updatedCard = PaymentCard.builder().id(id).number("12341234").build();
        PaymentCardResponseDto dto = PaymentCardResponseDto.builder().id(id).number("12341234").build();

        doNothing().when(validationUtil).validateMatchingIds(id, updateDto.getId());
        when(paymentCardRepo.findById(id)).thenReturn(Optional.of(existingCard));
        doNothing().when(mapper).updateEntityFromDto(updateDto, existingCard);
        when(paymentCardRepo.save(existingCard)).thenReturn(updatedCard);
        when(mapper.toDto(updatedCard)).thenReturn(dto);

        PaymentCardResponseDto result = service.update(id, updateDto);

        assertEquals(dto, result);
        verify(paymentCardRepo).findById(id);
        verify(paymentCardRepo).save(existingCard);
        verify(mapper).updateEntityFromDto(updateDto, existingCard);
        verify(mapper).toDto(updatedCard);
    }

    @Test
    void changeStatusShouldSaveAndReturnDtoWhenStatusChanges() {
        long id = 1L;
        PaymentCard card = PaymentCard.builder().id(id).active(false).build();
        PaymentCard savedCard = PaymentCard.builder().id(id).active(true).build();
        PaymentCardResponseDto dto = PaymentCardResponseDto.builder().id(id).active(true).build();

        when(paymentCardRepo.findById(id)).thenReturn(Optional.of(card));
        when(paymentCardRepo.save(card)).thenReturn(savedCard);
        when(mapper.toDto(savedCard)).thenReturn(dto);

        PaymentCardResponseDto result = service.changeStatus(id, true);

        assertEquals(dto, result);
        verify(paymentCardRepo).findById(id);
        verify(paymentCardRepo).save(card);
        verify(mapper).toDto(savedCard);
    }

    @Test
    void changeStatusShouldThrowWhenStatusAlreadySet() {
        long id = 1L;
        PaymentCard card = PaymentCard.builder().id(id).active(true).build();

        when(paymentCardRepo.findById(id)).thenReturn(Optional.of(card));

        assertThrows(BadRequestException.class, () -> service.changeStatus(id, true));
        verify(paymentCardRepo).findById(id);
        verifyNoMoreInteractions(paymentCardRepo, mapper);
    }

    @Test
    void findByIdReturnCardWhenCardExists() {
        long id = 1L;

        PaymentCard paymentCard = PaymentCard.builder().
                id(id).
                number("12341234").
                build();

        when(paymentCardRepo.findById(id)).thenReturn(Optional.of(paymentCard));

        PaymentCard result = service.findById(id);

        assertEquals(paymentCard, result);
        verify(paymentCardRepo).findById(id);
    }

    @Test
    void findByIdThrowExceptionWhenCardDoesNotExist() {
        long id = 1L;
        when(paymentCardRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(PaymentCardNotFoundException.class, () -> service.findById(id));
        verify(paymentCardRepo).findById(id);
    }

    @Test
    void findDtoByIdShouldReturnDtoWhenCardExists() {
        long id = 1L;
        PaymentCard card = PaymentCard.builder().id(id).build();
        PaymentCardResponseDto dto = PaymentCardResponseDto.builder().id(id).build();

        when(paymentCardRepo.findById(id)).thenReturn(Optional.of(card));
        when(mapper.toDto(card)).thenReturn(dto);

        PaymentCardResponseDto result = service.findDtoById(id);

        assertEquals(dto, result);
        verify(paymentCardRepo).findById(id);
        verify(mapper).toDto(card);
    }

    @Test
    void findAllShouldReturnPageWhenSpecProvided() {
        Boolean active = true;
        LocalDate expiresAfter = LocalDate.of(2025, 1, 1);
        LocalDate expiresBefore = LocalDate.of(2025, 12, 31);

        PaymentCard card = PaymentCard.builder().id(1L).build();
        Page<PaymentCard> cardPage = new PageImpl<>(List.of(card));
        Page<PaymentCardResponseDto> dtoPage = new PageImpl<>(List.of(PaymentCardResponseDto.builder().id(1L).build()));

        when(paymentCardRepo.findAll(any(Specification.class), eq(pageable))).thenReturn(cardPage);
        when(mapper.toDto(cardPage)).thenReturn(dtoPage);

        Page<PaymentCardResponseDto> result = service.findAll(active, expiresAfter, expiresBefore, pageable);

        assertEquals(dtoPage, result);
        verify(paymentCardRepo).findAll(any(Specification.class), eq(pageable));
        verify(mapper).toDto(cardPage);
    }

    @Test
    void findAllByUserIdShouldReturnListWhenCardsExist() {
        long userId = 1L;
        PaymentCard card = PaymentCard.builder().id(1L).build();
        List<PaymentCard> cards = List.of(card);
        List<PaymentCardResponseDto> dtos = List.of(PaymentCardResponseDto.builder().id(1L).build());

        when(paymentCardRepo.findAllByUserId(userId)).thenReturn(cards);
        when(mapper.toDto(cards)).thenReturn(dtos);

        List<PaymentCardResponseDto> result = service.findAllByUserId(userId);

        assertEquals(dtos, result);
        verify(paymentCardRepo).findAllByUserId(userId);
        verify(mapper).toDto(cards);
    }

}