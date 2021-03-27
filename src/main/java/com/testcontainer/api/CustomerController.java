package com.testcontainer.api;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@AllArgsConstructor
@Slf4j
@RestController
@RequestMapping("customer")
public class CustomerController {

    private final ICustomerService service;


    @GetMapping
    @ResponseStatus(OK)
    public Flux<Customer> findAll() {
        return service.findAll();
    }


    @PostMapping
    @ResponseStatus(CREATED)
    public Mono<Customer> save(@RequestBody Customer customer) {
        return service.save(customer);
    }


    @DeleteMapping
    @ResponseStatus(NO_CONTENT)
    public Mono<Void> removeAll() {
        return service.deleteAll();
    }


    @PostMapping("saveRollback")
    @ResponseStatus(CREATED)
    public Flux<Customer> saveList_IfThrowExceptionExecutesTheRollback
            (@RequestBody List<Customer> customerList) {
        return service.saveList_IfThrowExceptionExecutesTheRollback(customerList);
    }
}
