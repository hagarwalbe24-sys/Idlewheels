package com.idlewheels.controller;

import com.idlewheels.dto.SearchForm;
import com.idlewheels.service.SearchService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/listings")
    public String search(@Valid @ModelAttribute("searchForm") SearchForm form,
                         BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("searchInvalid", true);
            model.addAttribute("searchPage", searchService.emptyPage(form.getPage()));
        } else {
            model.addAttribute("searchInvalid", false);
            model.addAttribute("searchPage", searchService.search(form));
        }
        return "index";
    }
}
