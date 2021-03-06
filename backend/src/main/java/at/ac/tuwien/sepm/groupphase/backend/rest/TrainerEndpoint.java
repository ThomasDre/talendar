package at.ac.tuwien.sepm.groupphase.backend.rest;

import at.ac.tuwien.sepm.groupphase.backend.exceptions.BackendException;
import at.ac.tuwien.sepm.groupphase.backend.exceptions.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.rest.dto.TrainerDto;
import at.ac.tuwien.sepm.groupphase.backend.service.ITrainerService;
import at.ac.tuwien.sepm.groupphase.backend.service.exceptions.ServiceException;
import at.ac.tuwien.sepm.groupphase.backend.service.exceptions.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.util.mapper.TrainerMapper;
import org.aspectj.weaver.ast.Not;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Exception that occur within the underlying services will be reported and rethrown.
 *
 * All exception are annotated with @ResponseStatus(...)
 * Therefore Spring will automatically map such an thrown exception to an appropriate HTTP response
 * status (i.e. the status which was specified in annotation)
 */
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:8080" })
@RestController
@RequestMapping("/api/v1/talendar/trainers")
public class TrainerEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainerEndpoint.class);

    private final ITrainerService trainerService;
    private final TrainerMapper mapper;


    @Autowired
    public TrainerEndpoint(ITrainerService trainerService, TrainerMapper mapper) {
        this.trainerService = trainerService;
        this.mapper = mapper;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public TrainerDto getOneTrainerById(@PathVariable("id") Long id) throws ServiceException, NotFoundException {
        LOGGER.info("Incoming Request To Retrieve Trainer With ID {}", id);

        try {
            return mapper.entityToTrainerDto(trainerService.getById(id));
        }
        catch(ServiceException e) {
            LOGGER.error("GET Request unsuccessful: " + e.getMessage(), e);
            throw new ServiceException("Etwas ist leider am Server schiefgelaufen", e);
        }
        catch(NotFoundException e) {
            LOGGER.error("GET Request unsuccessful: " + e.getMessage(), e);
            throw new NotFoundException("Der gesuchte Trainer existiert nicht", e);
        }
    }


    @RequestMapping(method = RequestMethod.GET)
    public List<TrainerDto> getAllTrainers() throws ServiceException {
        LOGGER.info("Incoming Request To Retrieve List Of All Trainers");

        try {
            return trainerService.getAll()
                                 .stream()
                                 .map(mapper::entityToTrainerDto)
                                 .collect(Collectors.toList());
        }
        catch(ServiceException e) {
            LOGGER.error("GET Request unsuccessful: " + e.getMessage(), e);
            throw new ServiceException("Etwas ist leider am Server schiefgelaufen", e);
        }
    }


    @RequestMapping(method = RequestMethod.PUT)
    public TrainerDto updateTrainer(@RequestBody TrainerDto trainerDto) throws ServiceException, ValidationException, NotFoundException {
        LOGGER.info("Incoming Request To Update An Existing Trainer With Id {}",
                    trainerDto.getId()
        );

        try {
            return mapper.entityToTrainerDto(
                trainerService.update(mapper.dtoToTrainerEntity(trainerDto)));
        }
        catch(ServiceException e) {
            LOGGER.error("PATCH Request unsuccessful: " + e.getMessage(), e);
            throw new ServiceException("Etwas ist leider am Server schiefgelaufen", e);
        }
        catch(ValidationException e) {
            LOGGER.error("PATCH Request unsuccessful " + e.getMessage(), e);
            throw new ValidationException(e.getMessage(), e);
        }
        catch(NotFoundException e) {
            LOGGER.error("PATCH Request unsuccessful: " + e.getMessage(), e);
            throw new NotFoundException(
                "Es konnte nicht geupdated werden. Der Trainer existiert nicht", e);
        }
    }


    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public TrainerDto createNewTrainer(@RequestBody TrainerDto trainerDto) throws ValidationException, ServiceException {
        LOGGER.info("Incoming POST Trainer Request");

        try {
            return mapper.entityToTrainerDto(trainerService.save(mapper.dtoToTrainerEntity(trainerDto)));
        } catch(ValidationException e) {
            LOGGER.error("POST Request unsuccessful: " + e.getMessage(), e);
            throw new ValidationException(e.getMessage(), e);
        }
        catch(ServiceException e) {
            LOGGER.error("POST Request unsuccessful: " + e.getMessage(), e);
            throw new ServiceException("Etwas ist leider am Server schiefgelaufen", e);
        }
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteTrainer(@PathVariable("id") Long id) throws NotFoundException, ServiceException {
        LOGGER.info("Incoming DELETE Trainer Request");

        try {
            trainerService.delete(id);
        }
        catch(NotFoundException e) {
            LOGGER.error("DELETE Request unsuccessful: " + e.getMessage(), e);
            throw new NotFoundException(
                "Es konnte nicht gelöscht werden. Der Trainer existiert nicht", e);
        }
        catch(ServiceException e) {
            LOGGER.error("DELETE Request unsuccessful: " + e.getMessage());
            throw new ServiceException("Etwas ist leider am Server schiefgelaufen", e);
        }
    }
}
