package thestoreforcarscharging.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import thestoreforcarscharging.dto.ChargingSessionDTO;
import thestoreforcarscharging.enums.StatusEnum;

import java.util.*;

import static java.util.Objects.isNull;
import static thestoreforcarscharging.enums.StatsCounter.*;

/**
 * Business logic for the store represents unique charging session
 *
 * @author <a href="mailto:1arxemond1@gmail.com">Yuri Glushenkov</a>
 */
@Service
public class SessionService {
    @Autowired
    private MessageSource messageSource;

    private final Map<String, ChargingSessionDTO> store = new HashMap<>(1 << 30); // 1<<30 == 2^32

    public void addNewSession(ChargingSessionDTO csDTO) {
        store.put(csDTO.getId().toString(), csDTO);
        INSTANCE.incCurrent(csDTO);
    }

    public Collection<ChargingSessionDTO> printStore() {
        return store.values();
    }

    public String changeStatus(String id) {
        final ChargingSessionDTO csDTO = store.get(id);
        if (isNull(id) || id.isEmpty() || isNull(csDTO))
            return messageSource.getMessage("change.status.finished.id.does.not.exist", new Object[]{id}, null);

        if (csDTO.getStatus().equals(StatusEnum.FINISHED))
            return messageSource.getMessage("change.status.finished.id.already.has.been.stopped", new Object[]{id}, null);

        csDTO.changeStatusToFinish();
        INSTANCE.incFinished(csDTO);

        return null;
    }
}