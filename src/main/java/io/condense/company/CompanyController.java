package io.condense.company;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private static final int RESULT_LIMIT = 10;

    private final CompanyDirectory directory;

    public CompanyController(CompanyDirectory directory) {
        this.directory = directory;
    }

    @GetMapping
    public List<CompanySearchResult> search(@RequestParam(name = "q", defaultValue = "") String query) {
        return directory.search(query, RESULT_LIMIT);
    }
}
