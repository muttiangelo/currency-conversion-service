package com.mutti.currencyconversionservice.controller;

import com.mutti.currencyconversionservice.CurrencyExchangeProxy;
import com.mutti.currencyconversionservice.bean.CurrencyConversion;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("currency-conversion")
public class CurrencyConversionController {

    private final Environment env;

    private final CurrencyExchangeProxy currencyExchangeProxy;

    public CurrencyConversionController(Environment env, CurrencyExchangeProxy currencyExchangeProxy) {
        this.env = env;
        this.currencyExchangeProxy = currencyExchangeProxy;
    }

    /**
     *
     * this specific method use RestTemplate to talk to other microservice
     *
     * @param from the currency from conversion
     * @param to the currency to conversion
     * @param quantity to be converted
     * @return a @CurrencyConversion object
     */
    @GetMapping("from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion getConversionByFromToAndQuantity(@PathVariable String from,
                                                               @PathVariable String to,
                                                               @PathVariable BigDecimal quantity) {
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("from", from);
        uriVariables.put("to", to);
        ResponseEntity<CurrencyConversion> responseEntity = new RestTemplate().getForEntity("http://localhost:8000/currency-exchange/from/{from}/to/{to}",
                CurrencyConversion.class, uriVariables);

        CurrencyConversion currencyConversion = responseEntity.getBody() != null ? responseEntity.getBody() : new CurrencyConversion();

        return new CurrencyConversion(currencyConversion.getId(), currencyConversion.getFrom(), currencyConversion.getTo(),
                quantity, currencyConversion.getConversionMultiple(), quantity.multiply(currencyConversion.getConversionMultiple()),
                currencyConversion.getEnvironment() + " rest template");
    }


    /**
     *
     * this specific method use @Feign to talk to other microservice
     *
     * @param from the currency from conversion
     * @param to the currency to conversion
     * @param quantity to be converted
     * @return a @CurrencyConversion object
     */
    @GetMapping("feign/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion getConversionByFromToAndQuantityFeign(@PathVariable String from,
                                                                    @PathVariable String to,
                                                                    @PathVariable BigDecimal quantity) {

        CurrencyConversion currencyConversion = currencyExchangeProxy.getCurrencyExchange(from, to) != null ?
                currencyExchangeProxy.getCurrencyExchange(from, to)
                : new CurrencyConversion();

        return new CurrencyConversion(currencyConversion.getId(), currencyConversion.getFrom(), currencyConversion.getTo(),
                quantity, currencyConversion.getConversionMultiple(), quantity.multiply(currencyConversion.getConversionMultiple()),
                currencyConversion.getEnvironment() + " feign");
    }
}
