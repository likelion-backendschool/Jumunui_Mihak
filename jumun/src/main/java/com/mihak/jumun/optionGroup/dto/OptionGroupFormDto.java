package com.mihak.jumun.optionGroup.dto;

import com.mihak.jumun.store.entity.Store;
import lombok.*;

import javax.validation.constraints.NotEmpty;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionGroupFormDto {
    @NotEmpty(message = "옵션그룹명을 입력해주세요!")
    private String name;

    private Boolean isMultiple;

    private Store store;

}
