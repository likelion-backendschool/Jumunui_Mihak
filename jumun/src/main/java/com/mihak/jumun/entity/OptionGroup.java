package com.mihak.jumun.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OptionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OPTIONGROUP_ID")
    private Long id;

    private String name;
    private boolean isMultiple;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORE_ID")
    private Store store;

    @OneToMany(mappedBy = "optionGroup", cascade = CascadeType.ALL)
    private List<OptionAndOptionGroup> optionAndOptionGroups = new ArrayList<>();

    public void changeOptionGroup(String name, boolean isMultiple) {
        this.name = name;
        this.isMultiple = isMultiple;
    }
}
