package com.mihak.jumun.category;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
@Setter
@Getter
public class CategoryForm {

    @NotEmpty(message = "카테고리명은 필수항목입니다.")
    private String name;
}
