package com.courier.delivery.controllers;

import com.courier.delivery.dao.CourierDetailsDAO;
import com.courier.delivery.dao.UserDAO;
import com.courier.delivery.dto.BasicDTO;
import com.courier.delivery.dto.RegisterRequestDTO;
import com.courier.delivery.dto.UpdateCourierDTO;
import com.courier.delivery.enums.UserRoleEnum;
import com.courier.delivery.exceptions.CourierNotFoundException;
import com.courier.delivery.exceptions.UserNotFoundException;
import com.courier.delivery.models.CourierDetails;
import com.courier.delivery.models.User;
import com.courier.delivery.utils.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/agent")
public class AgentController {
    @Autowired
    UserDAO userDAO;
    @Autowired
    CourierDetailsDAO courierDetailsDAO;
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courier/getAll")
    public ResponseEntity<BasicDTO<List<CourierDetails>>> courierGetAll(@RequestHeader(HttpHeaders.AUTHORIZATION) String token){

        String userEmail = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
        Optional<User> us = userDAO.findUserByEmail(userEmail);
        if(us.isEmpty())
            throw new UserNotFoundException();

        List<CourierDetails> courierDetailsOptional = courierDetailsDAO.findByAgent(us.get());
        return new ResponseEntity<>(new BasicDTO<>(true, "Orders List", courierDetailsOptional), HttpStatus.OK);
    }
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/courier/update/{orderId}")
    public ResponseEntity<BasicDTO<CourierDetails>> courierUpdate(@PathVariable("orderId") Long orderId, @RequestBody UpdateCourierDTO r){
        Optional<CourierDetails> courierDetailsOptional = courierDetailsDAO.findById(orderId);
        if(courierDetailsOptional.isEmpty())
            throw new CourierNotFoundException();
        CourierDetails courierDetails = courierDetailsOptional.get();
        courierDetails.setStatus(r.getStatus());
        courierDetails.setExpectedDeliveryDate(r.getExpectedDeliveryDate());
        courierDetails.setCurrentLocation(r.getCurrentLocation());
        courierDetailsDAO.save(courierDetails);
        return new ResponseEntity<>(new BasicDTO<>(true, "Status updated", courierDetails), HttpStatus.OK);
    }
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/profile")
    public ResponseEntity<BasicDTO<User>> profile(@RequestHeader(HttpHeaders.AUTHORIZATION) String token){
        String userEmail = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
        Optional<User> us = userDAO.findUserByEmail(userEmail);
        if(us.isEmpty())
            throw new UserNotFoundException();
        return new ResponseEntity<>(new BasicDTO<>(true, "profile data", us.get()), HttpStatus.OK);
    }
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/profile")
    public ResponseEntity<BasicDTO<User>> updateProfile(@RequestHeader(HttpHeaders.AUTHORIZATION) String token ,@RequestBody RegisterRequestDTO registerRequestDTO) {
        String userEmail = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
        Optional<User> us = userDAO.findUserByEmail(userEmail);
        if(us.isEmpty())
            throw new UserNotFoundException();
        User user = us.get();
        user.setMobileNo(registerRequestDTO.getMobileNo());
        user.setFirstName(registerRequestDTO.getFirstName());
        user.setLastName(registerRequestDTO.getLastName());
        user.setEmail(registerRequestDTO.getEmail());
        user.setRole(UserRoleEnum.USER);
        user.setActive(true);
        user.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));
        userDAO.save(user);
        return new ResponseEntity<>(new BasicDTO<>(true, "Updated", user), HttpStatus.CREATED);
    }
}
