package org.example.drools_persistance_spring.controller;

import lombok.AllArgsConstructor;
import org.example.drools_persistance_spring.api.request.CreateWalletRequest;
import org.example.drools_persistance_spring.api.request.GetWalletRequest;
import org.example.drools_persistance_spring.api.request.SessionDumpRequest;
import org.example.drools_persistance_spring.api.response.GetWalletResponse;
import org.example.drools_persistance_spring.api.response.GetWalletsResponse;
import org.example.drools_persistance_spring.api.response.SessionDumpResponse;
import org.example.drools_persistance_spring.api.response.StandardResponse;
import org.example.drools_persistance_spring.service.DroolsService;
import org.example.facts.data.Wallet;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

/**
 * Created by maksim.drobyshev on 23-Sep-20.
 */
@RestController
@RequestMapping(value = "/api/wallet", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class WalletApiController {
    private final DroolsService droolsService;

    @PostMapping(path = "/createWallet", consumes = MediaType.APPLICATION_JSON_VALUE)
    public StandardResponse createWallet(@RequestBody @Validated CreateWalletRequest request) {
        droolsService.processFacts(request.getWallet().getClientId(), request.getWallet());
        return new StandardResponse("Ok");
    }

    @PostMapping(path = "/getWallets", consumes = MediaType.APPLICATION_JSON_VALUE)
    public StandardResponse getWallets(@RequestBody @Validated GetWalletRequest request) {
        QueryResults queryResultsRows = droolsService.processQuery(request.getClientId(), "getWallet", request.getWalletId());
        if(queryResultsRows.size() > 1) {
            GetWalletsResponse response = new GetWalletsResponse();
            response.setResultCode("MoreThanOne");
            response.setWallets(new ArrayList<>());
            for(QueryResultsRow row : queryResultsRows) {
                Wallet wallet = (Wallet) row.get("wallet");
                response.getWallets().add(wallet);
            }
            return response;
        } else if(queryResultsRows.size() == 1) {
            Wallet wallet = (Wallet) queryResultsRows.iterator().next().get("wallet");
            return new GetWalletResponse(wallet);
        }
        return new StandardResponse("Empty");
    }

    @PostMapping(path = "/sessionDump", consumes = MediaType.APPLICATION_JSON_VALUE)
    public StandardResponse sessionDump(@RequestBody @Validated SessionDumpRequest request) {
        return new SessionDumpResponse(droolsService.sessionDump(request.getClientId()));
    }
}
