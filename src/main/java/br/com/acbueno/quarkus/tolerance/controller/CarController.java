package br.com.acbueno.quarkus.tolerance.controller;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jboss.logging.Logger;

import br.com.acbueno.quarkus.tolerance.model.Car;
import br.com.acbueno.quarkus.tolerance.service.CarRepositoryService;

@Path("/car")
public class CarController {

  @Inject
  Logger logger;

  @Inject
  CarRepositoryService carRepositoryService;

  private AtomicLong counter = new AtomicLong(0);

  @GET
  @Retry(maxRetries = 4)
  public List<Car> getCars() {
    final Long invocatoionNumber = counter.getAndIncrement();

    maybeFault(String.format("CarController#getCars() invocation #%d failed", invocatoionNumber));
    logger.infof("CarController#getCars() invocation #%d returning successfully", invocatoionNumber);

    return carRepositoryService.getAllCar();
  }

  private void maybeFault(String failureLogMessage) {
    if (new Random().nextBoolean()) {
      logger.error(failureLogMessage);
      throw new RuntimeException("Controller Error");
    }
  }

  @GET
  @Path("/{id}/recommedations")
  @Timeout(250)
  @Fallback(fallbackMethod = "fallbackRecommedations")
  public List<Car> recommedations(@PathParam(value = "id") int id) {

    long stated = System.currentTimeMillis();
    final long invocationNunber = counter.getAndIncrement();

    try {
      randonDelay();
      logger.infof("CarController#recommedations() invocation #%d returning sucessufully", invocationNunber);
      return carRepositoryService.getRecommendations(id);
    } catch (InterruptedException e) {
      logger.errorf("CarController#recommedtaions() invocation #%d time out after %d ms", invocationNunber, System.currentTimeMillis() - stated);
      return null;
    }

  }

  private void randonDelay() throws InterruptedException {
    Thread.sleep(new Random().nextInt(500));
  }

  public List<Car> fallbackRecommedations(int id) {
    logger.infof("Falling back to RecommedationResource#fallbackRecommedations");
    return Collections.singletonList(carRepositoryService.getCarById(id));
  }

  @GET
  @Path("/{id}/availability")
  public Response availibilty(@PathParam(value = "id") int id) {
    final Long invocationNumber = counter.getAndIncrement();
    Car car = carRepositoryService.getCarById(id);

    if (car == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .build();
    }

    try {
      Integer availability = carRepositoryService.getAvailibity(car);
      logger.infof("CarController@availibity invocation #%d returning successfully", invocationNumber);

      return Response.ok(availability).build();
    } catch (RuntimeException e) {
      String message = e.getClass().getSimpleName() + " : " + e.getMessage();
      logger.errorf("CarController#availibity invocation #%d failed: %s", invocationNumber, message);

      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(message)
          .type(MediaType.TEXT_PLAIN_TYPE)
          .build();

    }

  }

}
