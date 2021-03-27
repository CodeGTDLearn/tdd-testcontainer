package com.testcontainer.databuilder;

import com.github.javafaker.Faker;
import com.testcontainer.api.Customer;
import lombok.Builder;
import lombok.Getter;

import java.util.Locale;

@Builder
@Getter
public class CustomerBuilder {

    private Customer customer;

    private static Faker faker = new Faker(new Locale("en-CA.yml"));

    public static CustomerBuilder customerWithName() {
        Customer customer1 = new Customer();
        customer1.setEmail(faker.internet().emailAddress());
        customer1.setRating(faker.number().numberBetween(1,55));
        return CustomerBuilder.builder().customer(customer1).build();
    }

    public static CustomerBuilder customerWithNameButEmailIsNull() {
        Customer customer1 = new Customer();
        customer1.setEmail(null);
        customer1.setRating(faker.number().numberBetween(1,55));
        return CustomerBuilder.builder().customer(customer1).build();
    }

    public static CustomerBuilder customerWithIdAndName(String id) {
        Customer customer1 = new Customer();
        customer1.setId(id);
        customer1.setEmail(faker.internet().emailAddress());
        customer1.setRating(faker.number().numberBetween(1,55));
        return CustomerBuilder.builder().customer(customer1).build();
    }

    public Customer create() {
        return this.customer;
    }
}
