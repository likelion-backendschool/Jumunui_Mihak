package com.mihak.jumun.menu.dto;
import com.mihak.jumun.category.entity.Category;
import com.mihak.jumun.store.entity.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import javax.persistence.Lob;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuFormDto {

    @Nullable
    private Category category;

    @NotEmpty(message = "메뉴명을 입력해주세요.")
    private String name;

    @NotNull(message = "가격을 입력해주세요.")
    private Integer price;

    @Nullable
    private String imgUrl;

    @Lob
    @Nullable
    private String description;

    private Store store;

    private Long optionGroupId;

    private Boolean isLimitedSale;

    private Long quantity;

    public void setStore(Store store) {
        this.store = store;
    }

    public void setImgUrl(@Nullable String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
