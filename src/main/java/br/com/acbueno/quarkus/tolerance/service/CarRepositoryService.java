package br.com.acbueno.quarkus.tolerance.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;

import br.com.acbueno.quarkus.tolerance.model.Car;
import br.com.acbueno.quarkus.tolerance.model.enums.Brand;

@ApplicationScoped
public class CarRepositoryService {

  private Map<Integer, Car> carList = new HashMap<>();

  SimpleDateFormat df = new SimpleDateFormat("yyyy");

  private AtomicLong counter = new AtomicLong(0);

  public CarRepositoryService() throws ParseException {
    Date dateFactoryVersa = df.parse("2016");
    Date dateModelVersa = df.parse("2017");
    Date dateFactorySentra = df.parse("2018");
    Date dateModelSentra = df.parse("2019");
    carList.put(1, new Car(1L, "Versa", dateFactoryVersa ,dateModelVersa , Brand.NISSAN));
    carList.put(1, new Car(1L, "Sentra", dateFactorySentra ,dateModelSentra , Brand.NISSAN));
  }

  public List<Car> getAllCar(){
    return new ArrayList<>(carList.values());
  }

  public Car getCarById(Integer id) {
    return carList.get(id);
  }


  public List<Car> getRecommendations(Integer id) {
      if (id == null) {
          return Collections.emptyList();
      }
      return carList.values().stream()
              .filter(c -> !id.equals(c.getId()))
              .limit(2)
              .collect(Collectors.toList());
  }

  @CircuitBreaker(requestVolumeThreshold = 4)
  public Integer getAvailibity(Car car) {
    maybeFail();
    return new Random().nextInt(30);
  }

  private void maybeFail() {
    final long invocationNumber = counter.getAndIncrement();
    if(invocationNumber % 4 > 1) {
      throw new RuntimeException("Service failed");
    }
  }



}
