package br.com.acbueno.quarkus.tolerance.model;

import java.util.Date;
import br.com.acbueno.quarkus.tolerance.model.enums.Brand;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Car {

  private Long id;

  private String modelName;

  private Date yearFacotry;

  private Date yearModel;

  private Brand brandName;

}
