package com.example.appevemsuserinterests.Controller;

import com.example.appevecommon.Controller.BaseController;
import com.example.appevecommon.Models.User.UserInterest;
import com.example.appevecommon.Service.Utilities.Responses.Response;
import com.example.appevecommon.Service.Utilities.Responses.ResponseFactory;
import com.example.appevemsuserinterests.Service.UserInterest.UserInterestFilter;
import com.example.appevemsuserinterests.Service.UserInterest.UserInterestService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.openapitools.model.UserInterestBaseDto;
import org.openapitools.model.UserInterestDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user-interests")
@SecurityRequirement(name = "BearerAuth")
public class UserInterestController extends
        BaseController<UserInterest, UserInterestDto, UserInterestFilter, UserInterestService> {

    public UserInterestController(UserInterestService service) {
        super(service);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','CLIENTE')")
    public Response<UserInterestDto> create(@RequestBody UserInterestBaseDto dto) {
        log.info("POST /user-interests userId={} categoryId={}", dto.getUserId(), dto.getCategoryId());
        return ResponseFactory.resourceCreated(service.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','CLIENTE')")
    public Response<UserInterestDto> update(@PathVariable Long id,
                                            @RequestBody UserInterestBaseDto dto) {
        log.info("PUT /user-interests/{}", id);
        return ResponseFactory.ok(service.update(id, dto));
    }
}
