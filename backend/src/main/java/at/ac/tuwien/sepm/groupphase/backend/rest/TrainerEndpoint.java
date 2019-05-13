package at.ac.tuwien.sepm.groupphase.backend.rest;

import at.ac.tuwien.sepm.groupphase.backend.rest.dto.TrainerDto;
import at.ac.tuwien.sepm.groupphase.backend.service.ITrainerService;
import at.ac.tuwien.sepm.groupphase.backend.util.mapper.TrainerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exception that occur within the underlying services will be reported and rethrown.
 *
 * All exception are annotated with @ResponseStatus(...)
 * Therefore Spring will automatically map such an thrown exception to an appropriate HTTP response
 * status (i.e. the status which was specified in annotation)
 */


@RestController
@RequestMapping("/api/talendar/trainers")
public class TrainerEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerEndpoint.class);

    private final ITrainerService trainerService;
    private final TrainerMapper mapper;

    @Autowired
    public TrainerEndpoint(ITrainerService trainerService, TrainerMapper mapper) {
        this.trainerService = trainerService;
        this.mapper = mapper;
    }

    @PostMapping
    public TrainerDto createNewTrainer(@RequestBody TrainerDto trainerDto) throws Exception {
        LOGGER.info("Incoming POST Trainer Request");

        try {
            return mapper.entityToTrainerDto(trainerService.save(mapper.dtoToTrainerEntity(trainerDto)));
        }
        catch(Exception e) {
            LOGGER.error("POST Request Could Not Be Served Successfully - : {}", e.getMessage(), e);
            throw e;
        }
    }
}
