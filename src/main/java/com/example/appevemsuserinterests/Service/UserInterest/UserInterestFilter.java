package com.example.appevemsuserinterests.Service.UserInterest;

import com.example.appevecommon.Service.Utilities.Responses.PagedFilter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInterestFilter extends PagedFilter {

    private Long userId;
    private Long categoryId;
}