package com.mihak.jumun.optionGroup.controller;

import com.mihak.jumun.option.entity.Option;
import com.mihak.jumun.optionGroup.entity.OptionGroup;
import com.mihak.jumun.store.entity.Store;
import com.mihak.jumun.menuAndOptionGroup.service.MenuAndOptionGroupService;
import com.mihak.jumun.option.service.OptionService;
import com.mihak.jumun.optionAndOptionGroup.service.OptionAndOptionGroupService;
import com.mihak.jumun.optionGroup.service.OptionGroupService;
import com.mihak.jumun.optionGroup.dto.OptionGroupDetailDto;
import com.mihak.jumun.optionGroup.dto.OptionGroupFormDto;
import com.mihak.jumun.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class OptionGroupController {
    private final StoreService storeService;
    private final OptionGroupService optionGroupService;
    private final OptionService optionService;
    private final OptionAndOptionGroupService optionAndOptionGroupService;
    private final MenuAndOptionGroupService menuAndOptionGroupService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{storeSN}/admin/store/optionGroup")
    public String showCreateForm(Model model) {
        model.addAttribute("optionGroupFormDto", new OptionGroupFormDto());
        return "optionGroup/create_optionGroup";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{storeSN}/admin/store/optionGroup")
    public String save(@PathVariable String storeSN, @Valid OptionGroupFormDto optionGroupFormDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "optionGroup/create_optionGroup";
        }
        optionGroupFormDto.setStore(storeService.findBySerialNumber(storeSN));
        optionGroupService.save(optionGroupFormDto);
        return "redirect:/%s/admin/store/optionGroupList".formatted(storeSN);
    }

    /* ?????? ?????? ?????? */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{storeSN}/admin/store/optionGroupList")
    public String showOptionGroupList(@PathVariable String storeSN, Model model) {
        Store store = storeService.findBySerialNumber(storeSN);
        List<OptionGroup> optionGroups = optionGroupService.findAllByStore(store);
        model.addAttribute("optionGroups", optionGroups);
        return "optionGroup/optionGroup_list";
    }

    /* ?????? ?????? ?????? ????????? */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{storeSN}/admin/store/optionGroupDetail/{optionGroupId}")
    public String showOptionGroupDetail(@PathVariable String storeSN,
                                        @PathVariable Long optionGroupId,
                                        Model model){
        Store store = storeService.findBySerialNumber(storeSN);
        OptionGroup optionGroup = optionGroupService.findByIdAndStore(optionGroupId, store);
        model.addAttribute("optionGroupName", optionGroup.getName());
        model.addAttribute("optionGroupId", optionGroupId);
        model.addAttribute("OptionGroupDetailDto", new OptionGroupDetailDto());
        // ?????? ????????? ????????? ????????????
        List<Option> options = optionService.findAllByStore(store);
        model.addAttribute("options", options);

        // ??????????????? ????????? ????????? ????????? ????????? ????????????.
        List<Option> allByOptionGroup = optionAndOptionGroupService.getOptionsByOptionGroup(optionGroup);
        model.addAttribute("optionList", allByOptionGroup);

        return "optionGroup/optionGroup_detail";
    }
    // ?????? ?????? ????????????????????? ?????? ????????????.
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{storeSN}/admin/store/optionGroupDetail/{optionGroupId}")
    public String addOption(@PathVariable String storeSN,
                            @PathVariable Long optionGroupId,
                            @ModelAttribute OptionGroupDetailDto optionGroupDetailDto
                                    ) {
        Store store = storeService.findBySerialNumber(storeSN);
        OptionGroup optionGroup = optionGroupService.findByIdAndStore(optionGroupId, store);
        Option option = optionService.findById(optionGroupDetailDto.getOptionId());
        optionAndOptionGroupService.addOption(optionGroup, option);

        return "redirect:/%s/admin/store/optionGroupDetail/%s".formatted(storeSN, optionGroupId);
    }

    // ?????? ?????? ????????????????????? ?????? ????????????.
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{storeSN}/admin/store/optionGroupDetail/delete/{optionGroupId}/{optionId}")
    public String deleteOption(@PathVariable String storeSN,
                               @PathVariable Long optionGroupId,
                               @PathVariable Long optionId,
                               Model model) {
        model.addAttribute("optionId", optionId);
        Option option = optionService.findById(optionId);
        optionAndOptionGroupService.deleteByOption(option);
        return "redirect:/%s/admin/store/optionGroupDetail/%s".formatted(storeSN, optionGroupId);
    }


    /* ?????? ?????? ?????? */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{storeSN}/admin/store/optionGroup/modify/{optionGroupId}")
    public String showModifyForm(@PathVariable String storeSN, @PathVariable Long optionGroupId, Model model) {
        Store store = storeService.findBySerialNumber(storeSN);
        OptionGroup optionGroup = optionGroupService.findByIdAndStore(optionGroupId, store);
        OptionGroupFormDto optionGroupFormDto = optionGroupService.getOptionGroupFormDtoByOptionGroup(optionGroup);
        model.addAttribute("optionGroupFormDto", optionGroupFormDto);

        return "optionGroup/optionGroup_modify";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{storeSN}/admin/store/optionGroup/modify/{optionGroupId}")
    public String modify(@PathVariable String storeSN,
                         @PathVariable Long optionGroupId,
                         @Valid OptionGroupFormDto optionGroupFormDto,
                         Model model,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "optionGroup/optionGroup_modify";
        }
        optionGroupService.modify(optionGroupId, optionGroupFormDto);

        return "redirect:/%s/admin/store/optionGroupList".formatted(storeSN, optionGroupId);
    }

    /* ?????? ?????? ???????????? */
    @Transactional
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{storeSN}/admin/store/optionGroup/delete/{optionGroupId}")
    public String delete(@PathVariable String storeSN,
                         @PathVariable Long optionGroupId) {
        OptionGroup optionGroup = optionGroupService.findByIdAndStore(optionGroupId, storeService.findBySerialNumber(storeSN));
        optionAndOptionGroupService.deleteByOptionGroup(optionGroup);
        menuAndOptionGroupService.deleteByOptionGroup(optionGroup);
        optionGroupService.deleteByOptionGroup(optionGroup);

        return "redirect:/%s/admin/store/optionGroupList".formatted(storeSN, optionGroupId);
    }

}
