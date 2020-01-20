package thestoreforcarscharging.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import thestoreforcarscharging.dto.ChargingSessionDTO;
import thestoreforcarscharging.dto.ErrorDTO;
import thestoreforcarscharging.dto.StatsCounterRepresentationDTO;
import thestoreforcarscharging.enums.StatsCounter;
import thestoreforcarscharging.service.SessionService;

import java.util.Collection;

import static thestoreforcarscharging.enums.StatsCounter.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.http.HttpStatus.*;

@RestController
public class ShopController {
    private static Logger LOGGER = LoggerFactory.getLogger(ShopController.class);

    @Autowired
    private MessageSource messageSource;
    @Autowired
    private SessionService sessionService;

    @PostMapping("/chargingSessions")
    public ResponseEntity<?> chargingSessions(@RequestBody ChargingSessionDTO csDTO) {

        if (isNull(csDTO.getStationId()) || csDTO.getStationId().isEmpty()) {
            final String errMsg = messageSource.getMessage("empty.station.id", new Object[]{}, null);
            LOGGER.error(messageSource.getMessage("empty.station.id.error", new Object[]{errMsg}, null));

            return new ResponseEntity<>(
                    new ErrorDTO<>(errMsg),
                    UNPROCESSABLE_ENTITY);
        }

        sessionService.addNewSession(csDTO);

        return new ResponseEntity<>(csDTO, OK);
    }

    @GetMapping("/chargingSessions/summary")
    public ResponseEntity<StatsCounterRepresentationDTO> chargingSessionsSummary() {
        return new ResponseEntity<>(INSTANCE.getRepresentation(), OK);
    }

    @GetMapping("/chargingSessions")
    public ResponseEntity<Collection<ChargingSessionDTO>> printAllChargingSessions() {
        return new ResponseEntity<>(sessionService.printStore(), OK);
    }

    @PutMapping("/chargingSessions/{id}")
    public ResponseEntity<?> stopChargingSessions(@PathVariable String id) {
        String res = sessionService.changeStatus(id);

        if (nonNull(res)) {
            LOGGER.error(messageSource.getMessage("change.status.finished.error", new Object[]{res}, null));
            return new ResponseEntity<>(new ErrorDTO<>(res), NOT_FOUND);
        }


        LOGGER.info(messageSource.getMessage("change.status.finished", new Object[]{id}, null));
        return new ResponseEntity<>(OK);
    }
}