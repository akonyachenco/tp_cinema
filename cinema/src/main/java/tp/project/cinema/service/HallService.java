package tp.project.cinema.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tp.project.cinema.dto.HallDto;
import tp.project.cinema.dto.Mapping.HallMapping;
import tp.project.cinema.dto.SeatDto;
import tp.project.cinema.exception.ResourceNotFoundException;
import tp.project.cinema.model.Hall;
import tp.project.cinema.repository.HallRepository;
import tp.project.cinema.repository.SeatRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HallService {

    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;
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
        Hall hall = hallMapping.toEntity(hallDto);
        hall.setStatus("AVAILABLE");

        Hall savedHall = hallRepository.save(hall);
        return hallMapping.toDto(savedHall);
    }

    public HallDto updateHall(Short id, HallDto hallDto) {
        Hall existingHall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Зал с ID " + id + " не найден"));

        existingHall.setHall_name(hallDto.getHallName());
        existingHall.setBase_price(hallDto.getBasePrice());
        existingHall.setRows_count(hallDto.getRows_count());
        existingHall.setSeats_per_row(hallDto.getSeatsPerRow());
        existingHall.setStatus(hallDto.getStatus());

        Hall updatedHall = hallRepository.save(existingHall);
        return hallMapping.toDto(updatedHall);
    }

    public HallDto updateHallStatus(Short id, String status) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Зал с ID " + id + " не найден"));

        hall.setStatus(status);
        Hall updatedHall = hallRepository.save(hall);
        return hallMapping.toDto(updatedHall);
    }
}