package tp.project.cinema.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tp.project.cinema.dto.HallDto;
import tp.project.cinema.dto.Mapping.HallMapping;
import tp.project.cinema.dto.SeatDto;
import tp.project.cinema.exception.ResourceNotFoundException;
import tp.project.cinema.model.Hall;
import tp.project.cinema.model.HallType;
import tp.project.cinema.repository.HallRepository;
import tp.project.cinema.repository.HallTypeRepository;
import tp.project.cinema.repository.SeatRepository;
import tp.project.cinema.repository.SessionRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HallService {

    private final HallRepository hallRepository;
    private final HallTypeRepository hallTypeRepository;
    private final SeatRepository seatRepository;
    private final SessionRepository sessionRepository;
    private final HallMapping hallMapping;

    public List<HallDto> getAllHalls() {
        return hallRepository.findAll().stream()
                .map(hallMapping::toDto)
                .collect(Collectors.toList());
    }

    public HallDto getHallById(Short id) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Зал с ID " + id + " не найден"));
        return hallMapping.toDto(hall);
    }

    public List<HallDto> getAvailableHalls() {
        return hallRepository.findByStatus("AVAILABLE").stream()
                .map(hallMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<HallDto> getHallsByType(String type) {
        return hallRepository.findByHallTypeTypeName(type).stream()
                .map(hallMapping::toDto)
                .collect(Collectors.toList());
    }

    public List<SeatDto> getHallSeats(Short hallId) {
        if (!hallRepository.existsById(hallId)) {
            throw new ResourceNotFoundException("Зал с ID " + hallId + " не найден");
        }

        return seatRepository.findByHallHallId(hallId).stream()
                .map(seat -> {
                    SeatDto dto = new SeatDto();
                    dto.setSeatId(seat.getSeat_id());
                    dto.setRowNumber(seat.getRow_number());
                    dto.setSeatNumber(seat.getSeat_number());
                    dto.setSeatType(seat.getSeat_type().getType_name());
                    dto.setPriceMultiplier(seat.getSeat_type().getPrice_multiplier());
                    dto.setHallId(seat.getHall().getHall_id());
                    dto.setBasePrice(seat.getHall().getBase_price());
                    dto.setHallName(seat.getHall().getHall_name());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public HallDto getHallWithLayout(Short hallId) {
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new ResourceNotFoundException("Зал с ID " + hallId + " не найден"));

        HallDto dto = hallMapping.toDto(hall);
        dto.setSeatList(getHallSeats(hallId));
        return dto;
    }

    public HallDto createHall(HallDto hallDto) {
        // Проверяем существование типа зала
        HallType hallType = hallTypeRepository.findByTypeName(hallDto.getHallType())
                .orElseGet(() -> {
                    // Создаем новый тип зала, если не существует
                    HallType newType = new HallType();
                    newType.setType_name(hallDto.getHallType());
                    newType.setDescription("Автоматически созданный тип зала");
                    return hallTypeRepository.save(newType);
                });

        Hall hall = hallMapping.toEntity(hallDto);
        hall.setHall_type(hallType);
        hall.setStatus("AVAILABLE");

        // Проверяем обязательные поля
        if (hall.getBase_price() == null) {
            hall.setBase_price(new BigDecimal("300.00")); // цена по умолчанию
        }
        if (hall.getRows_count() <= 0) {
            hall.setRows_count((short) 10); // рядов по умолчанию
        }
        if (hall.getSeats_per_row() <= 0) {
            hall.setSeats_per_row((short) 15); // мест в ряду по умолчанию
        }

        Hall savedHall = hallRepository.save(hall);
        return hallMapping.toDto(savedHall);
    }

    public HallDto updateHall(Short id, HallDto hallDto) {
        Hall existingHall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Зал с ID " + id + " не найден"));

        // Обновляем тип зала, если указан
        if (hallDto.getHallType() != null && !hallDto.getHallType().isEmpty()) {
            HallType hallType = hallTypeRepository.findByTypeName(hallDto.getHallType())
                    .orElseGet(() -> {
                        HallType newType = new HallType();
                        newType.setType_name(hallDto.getHallType());
                        newType.setDescription("Автоматически созданный тип зала");
                        return hallTypeRepository.save(newType);
                    });
            existingHall.setHall_type(hallType);
        }

        // Обновляем остальные поля
        if (hallDto.getHallName() != null && !hallDto.getHallName().isEmpty()) {
            existingHall.setHall_name(hallDto.getHallName());
        }
        if (hallDto.getBasePrice() != null) {
            existingHall.setBase_price(hallDto.getBasePrice());
        }
        if (hallDto.getRows_count() != null) {
            existingHall.setRows_count(hallDto.getRows_count());
        }
        if (hallDto.getSeatsPerRow() != null) {
            existingHall.setSeats_per_row(hallDto.getSeatsPerRow());
        }
        if (hallDto.getStatus() != null && !hallDto.getStatus().isEmpty()) {
            existingHall.setStatus(hallDto.getStatus());
        }

        Hall updatedHall = hallRepository.save(existingHall);
        return hallMapping.toDto(updatedHall);
    }

    public HallDto updateHallStatus(Short id, String status) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Зал с ID " + id + " не найден"));

        // Проверяем допустимые статусы
        if (!List.of("AVAILABLE", "MAINTENANCE", "CLOSED", "RESERVED").contains(status.toUpperCase())) {
            throw new IllegalArgumentException("Недопустимый статус зала: " + status);
        }

        hall.setStatus(status.toUpperCase());
        Hall updatedHall = hallRepository.save(hall);
        return hallMapping.toDto(updatedHall);
    }

    public List<HallDto> findAvailableHallsWithCapacity(Integer requiredCapacity) {
        if (requiredCapacity == null || requiredCapacity <= 0) {
            requiredCapacity = 1;
        }

        return hallRepository.findAvailableHallsWithCapacity(requiredCapacity).stream()
                .map(hallMapping::toDto)
                .collect(Collectors.toList());
    }

    public Integer getTotalSeatingCapacity() {
        Integer capacity = hallRepository.getTotalSeatingCapacity();
        return capacity != null ? capacity : 0;
    }
}