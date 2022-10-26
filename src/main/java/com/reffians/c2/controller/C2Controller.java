package com.reffians.c2.controller;

import com.reffians.c2.model.*;
import com.reffians.c2.model.Command.Status;
import com.reffians.c2.service.C2Service;
import com.reffians.c2.model.User;
import java.util.Optional;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** The REST Controller of the C2 Application, performs routing of all REST API
 * calls to this service. **/
@RestController
public class C2Controller {
  @Autowired
  private C2Service c2Service;
  

  private static final Logger logger = LoggerFactory.getLogger(C2Controller.class);

  /** GET commands for a beacon. Returns 200 OK and an array of command Command
   * objects on success, 400 Bad Request with an error message on failure.
   * @param beaconid A non-negative integer used to identify the beacon.
   * @param status An optional argument specifying the status of the command.
   * Can be one of "pending", "sent", "executed", or "finished". If no status
   * is supplied, commands of any status are retrieved.
   * @return A list of command objects. A command object is contains integer
   * identifier "id", integer "beaconid" of the corresponding beacon, user-defined
   * string "content", and string "status" that is one of "pending", "sent",
   * "executed", or "finished".
   */
  @GetMapping("/beacon/command")
  public ResponseEntity<?> getCommandBeacon(@RequestParam Integer beaconid,
      @RequestParam Optional<String> status) {
    logger.info("GET commands from beacon with beaconid: {}, status: {}",
        beaconid, status.orElse("NULL"));

    if (beaconid < 0) {
      logger.info("GET commands from beacon with negative beaconid: {}", beaconid);
      return responseBadRequest("Invalid beaconid: supplied beaconid is negative.");
    }

    if (!status.isPresent()) {
      return responseOk(c2Service.getCommands(beaconid));
    }

    if (!Command.isValidStatus(status.get())) {
      logger.info("GET commands from beacon with invalid status: {}", status);
      return responseBadRequest("Invalid status.");
    }

    return responseOk(c2Service.getCommands(beaconid, Status.valueOf(status.get())));
  }


  /** POST create beacon. **/
  @PostMapping("/beacon/create")
  public ResponseEntity<?> createBeacon(@RequestParam String username) {
    logger.info("POST create beacon for user with username: {}",
        username);
    List<User> thisUser = c2Service.getUsers(username);
    if (thisUser.size() == 0) {
      logger.info("POST create beacon for non-existent user: {}", username);
      return responseBadRequest();
    } else {
      c2Service.createBeacon(username);
      return new ResponseEntity<>("Beacon Created", HttpStatus.OK);
    }
  }

  private static <T> ResponseEntity<?> responseOk(@Nullable T body) {
    return new ResponseEntity<>(body, HttpStatus.OK);
  }

  private static <T> ResponseEntity<?> responseBadRequest(@Nullable T body) {
    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }
  
  /* POST Register User. */
  @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> registerUser(@RequestBody User user) {
    String username = user.username;
    String password = user.password;
    List<User> thisUser = c2Service.getUsers(username);
    if (thisUser.size() == 0) {
      c2Service.addUser(username, password);
      return new ResponseEntity<>("Registered", HttpStatus.OK);
    } else {
      return new ResponseEntity<>("User Already Exists", HttpStatus.OK);
    }
   }   

  /* POST Login User. */
  @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> login(@RequestBody User user) {
    String username = user.username;
    String password = user.password;
    List<User> thisUser = c2Service.getUsers(username, password);
    if (thisUser.size() != 0) {
      return new ResponseEntity<>("logged in", HttpStatus.OK);
    } else {
      return new ResponseEntity<>("user does not exist or password incorrect", HttpStatus.OK);
    }
  }
}
