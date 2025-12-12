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
import com.innowise.userservice.specification.PaymentCardSpecification;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentCardServiceImpl implements PaymentCardService {

    private final PaymentCardRepository paymentCardRepo;
    private final PaymentCardMapper mapper;
    private final ValidationUtil validationUtil;
    private final UserService userService;
    private final CardProperties cardProperties;

    @Override
    @Transactional
    @Caching(put = {
            @CachePut(value = "card", key = "#result.id")
    },
            evict = {
                    @CacheEvict(value = "cards", key = "#paymentCardCreateDto.userId"),
                    @CacheEvict(value = "user", key = "#paymentCardCreateDto.userId")
            })
    public PaymentCardResponseDto create(PaymentCardCreateDto paymentCardCreateDto) {
        PaymentCard paymentCard = getValidatedCardForCreation(paymentCardCreateDto);

        PaymentCard savedCard = paymentCardRepo.save(paymentCard);

        return mapper.toDto(savedCard);
    }

    @Override
    @Transactional
    @Caching(put = {
            @CachePut(value = "card", key = "#id")
    },
            evict = {
                    @CacheEvict(value = "cards", key = "#paymentCardUpdateDto.id")
            })
    public PaymentCardResponseDto update(long id, PaymentCardUpdateDto paymentCardUpdateDto) {
        PaymentCard existingCard = getValidatedCardForUpdate(id, paymentCardUpdateDto);

        mapper.updateEntityFromDto(paymentCardUpdateDto, existingCard);
        PaymentCard updatedCard = paymentCardRepo.save(existingCard);

        return mapper.toDto(updatedCard);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "card", key = "#id"),
            @CacheEvict(value = "cards", key = "@paymentCardServiceImpl.findById(#id).user.id", beforeInvocation = true),
            @CacheEvict(value = "user", key = "@paymentCardServiceImpl.findById(#id).user.id", beforeInvocation = true),
    })
    public void delete(long id) {
        findById(id);

        paymentCardRepo.deleteById(id);
    }

    @Override
    @Transactional
    @Caching(put = {
            @CachePut(value = "card", key = "#id")
    },
            evict = {
                    @CacheEvict(value = "cards", key = "@paymentCardServiceImpl.findById(#id).user.id")
            })
    public PaymentCardResponseDto changeStatus(long id, boolean active) {
        PaymentCard card = getValidatedCardForChangingStatus(id, active);

        card = paymentCardRepo.save(card);
        return mapper.toDto(card);
    }

    @Override
    public PaymentCard findById(long id) {
        return paymentCardRepo.findById(id).
                orElseThrow(() -> new PaymentCardNotFoundException("id", String.valueOf(id)));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "card", key = "#id")
    public PaymentCardResponseDto findDtoById(long id) {
        return mapper.toDto(findById(id));
    }

    @Override
    public Page<PaymentCardResponseDto> findAll(Boolean active,
                                                LocalDate expiresAfter,
                                                LocalDate expiresBefore,
                                                Pageable pageable) {
        Specification<PaymentCard> spec = configureSpecification(active,
                expiresAfter,
                expiresBefore);

        Page<PaymentCard> paymentCards = paymentCardRepo.findAll(spec, pageable);

        return mapper.toDto(paymentCards);
    }

    @Override
    @Cacheable(value = "cards", key = "#userId")
    public List<PaymentCardResponseDto> findAllByUserId(long userId) {
        List<PaymentCard> paymentCards = paymentCardRepo.findAllByUserId(userId);

        return mapper.toDto(paymentCards);
    }

    @Override
    public Long getUserIdByCardId(long cardId) {
        PaymentCard card = findById(cardId);
        return card.getUser().getId();
    }

    private Specification<PaymentCard> configureSpecification(Boolean active,
                                                              LocalDate expiresAfter,
                                                              LocalDate expiresBefore) {

        Specification<PaymentCard> spec = Specification.unrestricted();

        if (active != null) {
            spec = spec.and(PaymentCardSpecification.isActive(active));
        }

        if (expiresAfter != null) {
            spec = spec.and(PaymentCardSpecification.expiresAfter(expiresAfter));
        }

        if (expiresBefore != null) {
            spec = spec.and(PaymentCardSpecification.expiresBefore(expiresBefore));
        }

        return spec;

    }

    private void checkCardNumberNotTaken(String cardNumber) {
        paymentCardRepo.findByNumber(cardNumber)
                .ifPresent(card -> {
                    throw new PaymentCardAlreadyExistsException("number", cardNumber);
                });
    }

    private PaymentCard getValidatedCardForCreation(PaymentCardCreateDto paymentCardCreateDto) {
        checkCardNumberNotTaken(paymentCardCreateDto.getNumber());

        User user = userService.findById(paymentCardCreateDto.getUserId());

        if (user.getPaymentCards().size() >= cardProperties.getMaxLimit()) {
            throw new PaymentCardLimitExceededException("Maximum number of cards (" +
                    cardProperties.getMaxLimit() + ") exceeded");
        }

        return prepareCard(paymentCardCreateDto, user);
    }

    private PaymentCard prepareCard(PaymentCardCreateDto paymentCardCreateDto, User user) {
        PaymentCard card = mapper.toEntity(paymentCardCreateDto);
        card.setUser(user);
        card.setHolder(user.getName() + " " + user.getSurname());

        return card;
    }

    private PaymentCard getValidatedCardForUpdate(long id, PaymentCardUpdateDto dto) {
        validationUtil.validateMatchingIds(id, dto.getId());

        PaymentCard existingCard = findById(id);

        if (!existingCard.getNumber().equals(dto.getNumber())) {
            checkCardNumberNotTaken(dto.getNumber());
        }

        return existingCard;
    }

    private PaymentCard getValidatedCardForChangingStatus(long id, boolean active) {
        PaymentCard card = findById(id);

        if (active == card.getActive()) {
            throw new BadRequestException("Card with id=" + id + " have status=" + (active ? "active" : "inactive"));
        }

        card.setActive(active);

        return card;
    }


}
